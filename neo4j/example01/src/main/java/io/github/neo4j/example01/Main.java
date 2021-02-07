package io.github.neo4j.example01;

import java.io.File;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;

import org.neo4j.configuration.connectors.BoltConnector;
import org.neo4j.configuration.helpers.SocketAddress;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Value;
import org.neo4j.driver.internal.value.PathValue;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path.Segment;
import org.neo4j.driver.types.Relationship;
import org.neo4j.exceptions.KernelException;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.api.procedure.GlobalProcedures;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// FIXME Server is not starting when apoc is added to classpath
//import apoc.path.PathExplorer;

public class Main {
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
	
	public static void main(String[] args) {
		File databaseDirectory = new File("/tmp/neo4j-hello-db");
		Path path = Path.of(databaseDirectory.toURI());
		
		// depends on version: use file or use path
		DatabaseManagementService managementService = new DatabaseManagementServiceBuilder(path)
			.setConfig(BoltConnector.enabled, true)
			.setConfig(BoltConnector.listen_address, new SocketAddress("localhost", 7687))
			.build();
		registerShutdownHook(managementService);
		
		// FIXME Server is not starting when apoc is added to classpath
//		GraphDatabaseService db = managementService.database("DEFAULT_DATABASE_NAME");
//		registerProcedure(db, PathExplorer.class);
		
		Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("admin", "admin"));
		
		cleanup(driver);
		
		createSimpleGraph(driver);
		
		try (Session session = driver.session()) {
			listNodes(session);
			
			listDetailsForPerson(session, "Bruce Willis");
			
			listShortestPath(session);
			
			// FIXME Server is not starting when apoc is added to classpath
//			Result run = session.run(
//				"MATCH (p:Person) WHERE p.name = 'Bruce Willis' "
//				+ "CALL apoc.subgraphAll(p, {maxLevel:5, labelFilter:'-PRODUCER'}) "
//				+ "YIELD nodes, relationships RETURN nodes, relationships");
//			List<Record> records = run.list();
		}
		
		driver.close();
		managementService.shutdown();
	}

	 public static void registerProcedure(GraphDatabaseService db, Class<?>...procedures) {
         GlobalProcedures globalProcedures = ((GraphDatabaseAPI) db).getDependencyResolver().resolveDependency(GlobalProcedures.class);
     for (Class<?> procedure : procedures) {
         try {
             globalProcedures.registerProcedure(procedure, true);
             globalProcedures.registerFunction(procedure, true);
             globalProcedures.registerAggregationFunction(procedure, true);
         } catch (KernelException e) {
             throw new RuntimeException("while registering " + procedure, e);
         }
     }
 }
	
	private static void listShortestPath(Session session) {
		LOGGER.info("----");
		LOGGER.info("Listing shortest path");
		
		Result run = session.run(
			"MATCH (p1:Person),(p2:Person), "
			+ "p = shortestPath((p1)-[*..15]-(p2)) " 
			+ "WHERE p1.name = 'Bruce Willis' AND p2.name = 'Samuel L. Jackson' "
			+ "RETURN p");
		List<Record> records = run.list();
		for (Record record : records) {
			for (String key : record.keys()) {
				Value value = record.get(key);
				
				if (value.getClass().equals(PathValue.class)) {
					Iterator<Segment> iterator = value.asPath().iterator();
					while (iterator.hasNext()) {
						Segment next = iterator.next();
						Node startNode = next.start();
						Relationship relationship = next.relationship();
						Node endNode = next.end();
						LOGGER.info("- ({})-[{}]-({}) (({})-[{}]-({}))",
							startNode.id(), 
							relationship.id(), 
							endNode.id(), 
							startNode.get("name"),
							relationship.type(),
							endNode.get("name"));
					}
				}
			}
		}
	}

	private static List<Record> listDetailsForPerson(Session session, String name) {
		LOGGER.info("----");
		LOGGER.info("List some informations about {}:", name);
		Result run = session.run("MATCH (n) WHERE n.name = '"+name+"' RETURN id(n) as id,labels(n) as labels,keys(n) as keys");
		List<Record> records = run.list();
		for (Record record : records) {
			long id = record.get("id").asLong();
			List<String> labels = record.get("labels").asList(Objects::toString);
			List<String> keys = record.get("keys").asList(Objects::toString);
			
			LOGGER.info("-id: {}", id);
			LOGGER.info("-labels: {}", labels);
			LOGGER.info("-keys: {}", keys);
		}
		return records;
	}

	private static void listNodes(Session session) {
		LOGGER.info("List all nodes with their properties:");
		Result run = session.run("MATCH (n) RETURN n");
		List<Record> records = run.list();
		for (Record record : records) {
			Node node = record.get("n").asNode();
			LOGGER.info("  Found node:");
			for (Entry<String, Object> kv : node.asMap().entrySet()) {
				LOGGER.info("    -{} {} {}", node.id(), kv.getKey(), kv.getValue());
			}
		}
	}
	
	/**
	 * Creates the following simple graph
	 * <pre>
	 * 
	 *               ACTOR 
	 * Bruce Willis ----> The Sixth Sence
	 *      |
	 *      |
	 *      | ACTOR
	 *      |
	 *      |
	 *      v       ACTOR
	 * Die Hard 3 <----- Samuel L. Jackson
	 * 
	 * 
	 * </pre>
	 * @param driver
	 */
	private static void createSimpleGraph(Driver driver) {
		try (Session session = driver.session()) {
			// Create single two nodes and one edge connecting them
			session.run("CREATE (p:Person) SET p.name='Bruce Willis', p.born = 1955");
			session.run("CREATE (m:Movie) SET m.name='The Sixth Sense', m.year = 1999");
			session.run("MATCH (p:Person),(m:Movie) WHERE p.name = 'Bruce Willis' AND m.name = 'The Sixth Sense' CREATE (p)-[r:ACTOR]->(m)");
			
			// Create nodes and relations in a single query
			session.run("CREATE path=(p:Person { name:'Samuel L. Jackson' })-[:ACTOR]->(m:Movie { name:'Die Hard 3'})");
			
			// Create edge from Bruce Willis to Die Hard 3
			session.run("MATCH (p:Person),(m:Movie) WHERE p.name = 'Bruce Willis' AND m.name = 'Die Hard 3' CREATE (p)-[r:ACTOR { role:'John McClane'}]->(m)");
		}
	}

	private static void cleanup(Driver driver) {
		try (Session session = driver.session()) {
			// Cleanup everything
			session.run("MATCH (n) DETACH DELETE n");
		}
	}

	private static void registerShutdownHook(final DatabaseManagementService managementService) {
		// Registers a shutdown hook for the Neo4j instance so that it
		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
		// running application).
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				managementService.shutdown();
			}
		});
	}
}
