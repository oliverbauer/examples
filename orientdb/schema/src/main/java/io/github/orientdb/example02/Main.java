package io.github.orientdb.example02;

import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.out;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.outE;

import java.util.List;
import java.util.Map;

import org.apache.tinkerpop.gremlin.orientdb.OrientGraph;
import org.apache.tinkerpop.gremlin.orientdb.OrientGraphFactory;
import org.apache.tinkerpop.gremlin.process.traversal.Scope;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Column;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OClass.INDEX_TYPE;
import com.orientechnologies.orient.core.metadata.schema.OType;

/*
 * http://tinkerpop.apache.org/docs/3.3.8/tutorials/the-gremlin-console/
 * 
 * import static org.apache.tinkerpop.gremlin.structure.T.*;
 * import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.*;
 */

/**
 * Define graph-metadata, create TinkerGraph Modern Graph (cf. https://tinkerpop.apache.org/docs/current/reference/) manually and
 * fire some queries...
 */
public class Main {

	public static void main(String[] args) {
		OrientGraphFactory orientGraphFactory = new OrientGraphFactory("memory:mlm", "admin", "admin");
		OrientGraph graph = orientGraphFactory.getNoTx();

		defineMetadata(graph.database());
		create(graph);
		queryGremlin(graph);
		
		orientGraphFactory.close();
	}
	
	public static void defineMetadata(ODatabaseDocument db) {
		OClass personClass = db.createVertexClass("Person");
		personClass.createProperty("name", OType.STRING);
		personClass.createProperty("age", OType.INTEGER);
		personClass.createIndex("unique.person.name", INDEX_TYPE.UNIQUE, "name");
		personClass.createIndex("unique.person.age", INDEX_TYPE.NOTUNIQUE, "age");
		
		OClass softwareClass = db.createVertexClass("Software");
		softwareClass.createProperty("name", OType.STRING);
		softwareClass.createProperty("lang", OType.STRING);
		softwareClass.createIndex("unique.software.name", INDEX_TYPE.UNIQUE, "name");
		softwareClass.createIndex("unique.software.lang", INDEX_TYPE.NOTUNIQUE, "lang");

		db.createEdgeClass("created")
			.createProperty("weight", OType.DOUBLE)
			.setMin("0.0")
			.setMax("1.0");
		
		db.createEdgeClass("knows")
			.createProperty("weight", OType.DOUBLE)
			.setMin("0.0")
			.setMax("1.0");
	}
	
	public static void create(OrientGraph graph) {
		GraphTraversalSource g = graph.traversal();
		
		Vertex vadas = g.addV("Person").property("name", "Vadas").property("age", 27).next();
		Vertex marko = g.addV("Person").property("name", "Marko").property("age", 29).next();
		Vertex peter = g.addV("Person").property("name", "Peter").property("age", 35).next();
		Vertex josh = g.addV("Person").property("name", "Josh").property("age", 32).next();
		
		Vertex lop = g.addV("Software").property("name", "lop").property("lang","Java").next();
		Vertex ripple = g.addV("Software").property("name", "ripple").property("lang","Java").next();

		marko.addEdge("knows", vadas).property("weight", 0.5); // try to set it o 1.2 will result in an "OValidationException: The field 'knows.weight' is greater than 1.0"
		marko.addEdge("creaed", lop, "weight", 0.4);
		peter.addEdge("created", lop, "weight", 0.2);
		josh.addEdge("created", lop, "weight", 0.4);
		josh.addEdge("created", ripple, "weight", 1.0);
		marko.addEdge("knows", josh, "weight", 1.0);
	}
	
	public static void queryGremlin(OrientGraph graph) {
		GraphTraversalSource g = graph.traversal();
		
		Vertex marko = g.V().has("name","Marko").next();
		
		List<String> markoKnows = g
			.V()
			.has("name","Marko")
			.out("knows")
			.<String>values("name")
			.toList();
		System.out.println("Marko knows: "+markoKnows);

		List<Object> list = g
			.V(marko.id())
			.repeat(out()).times(2)
			.values("name")
			.toList();
		System.out.println("Depth 2 from Marko: "+list);
		
		boolean supportsTransactions = graph.features().graph().supportsTransactions();
		System.out.println(supportsTransactions);
		
		List<Map<String, Number>> list2 = g
			.V()
			.hasLabel("Person")
			.<String, Number>group()
				.by("name")
				.by(outE().values("weight").sum())
			.order(Scope.local)
			.by(Column.values)
			.toList();
		for (Map<String, Number> map : list2) {
			System.out.println("-"+map);
		}
	}
}
