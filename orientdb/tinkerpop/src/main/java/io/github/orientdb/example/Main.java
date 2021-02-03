package io.github.orientdb.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import org.apache.tinkerpop.gremlin.orientdb.OrientGraphFactory;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import io.github.orientdb.example.data.WikipediaDataset;
import io.github.orientdb.example.entities.Actor;
import io.github.orientdb.example.entities.Movie;

public class Main {
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
	
	public static void main(String[] args) throws IOException {
		File tempDir = Files.createTempDirectory("").toFile();
		
		createMovieYamlFilesFromWikipedia(tempDir.getAbsolutePath());
		
		OrientGraphFactory orientGraphFactory = new OrientGraphFactory("remote:localhost:2424/demodb");
		GraphTraversalSource g = orientGraphFactory.getTx().traversal();
		
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		mapper.findAndRegisterModules();
		mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		
	    for (File file : tempDir.listFiles()) {
			InputStream is = new FileInputStream(file);
			Movie movie = mapper.readValue(is, Movie.class);
			
			String movieName = movie.getName();
			List<Actor> actors = movie.getActors();
			
			GraphTraversal<Vertex,Vertex> movieV = g.addV("Movie");
			movieV = movieV.property("name", movieName);
			
			Vertex movieVertex = movieV.next(); // submit to database
			LOGGER.info("Added Movie with name {}", movieName);
			
			if (actors != null && !actors.isEmpty()) {
				for (Actor actor : actors) {
					
					String personName = actor.getName();
					
					boolean exists = g.V().hasLabel("Person").has("name", personName).hasNext();
					Vertex characterVertex;
					if (!exists) {
						LOGGER.info("  Created Person(real) with name {}. Created actor to Movie {}", personName, movieName);
						characterVertex = g
							.addV("Person")
							.property("name", personName)
							.next();
						characterVertex.addEdge("Actor", movieVertex);
					} else {
						LOGGER.info("  Found Person(real) with name {}. Created actor to Movie {}", personName, movieName);
						characterVertex = g.V().has("name", personName).next();
						characterVertex.addEdge("Actor", movieVertex);
					}
				}
			}
		}
	    
	    g.tx().commit();
	    orientGraphFactory.close();
	}

	private static void createMovieYamlFilesFromWikipedia(String downloadPath) {
		List<String> urls = Arrays.asList(
			"https://de.wikipedia.org/wiki/Stirb_langsam",
			"https://de.wikipedia.org/wiki/Stirb_langsam_2",
			"https://de.wikipedia.org/wiki/Terminator_(Film)",
			"https://de.wikipedia.org/wiki/Shining_(1980)",
			"https://de.wikipedia.org/wiki/The_Sixth_Sense",
			"https://de.wikipedia.org/wiki/Zimmer_1408",
			"https://de.wikipedia.org/wiki/The_Expendables",
			"https://de.wikipedia.org/wiki/Terminator:_Die_Erl%C3%B6sung",
			"https://de.wikipedia.org/wiki/Odd_Thomas",
			"https://de.wikipedia.org/wiki/Armageddon_%E2%80%93_Das_j%C3%BCngste_Gericht",
			"https://de.wikipedia.org/wiki/Pearl_Harbor_(Film)",
			"https://de.wikipedia.org/wiki/Terminator_2_%E2%80%93_Tag_der_Abrechnung",
			"https://de.wikipedia.org/wiki/Terminator_3_%E2%80%93_Rebellion_der_Maschinen",
			"https://de.wikipedia.org/wiki/Die_totale_Erinnerung_%E2%80%93_Total_Recall",
			"https://de.wikipedia.org/wiki/L%C3%A9on_%E2%80%93_Der_Profi",
			"https://de.wikipedia.org/wiki/22_Bullets",
			"https://de.wikipedia.org/wiki/Die_purpurnen_Fl%C3%BCsse_2_%E2%80%93_Die_Engel_der_Apokalypse",
			"https://de.wikipedia.org/wiki/Die_purpurnen_Fl%C3%BCsse_(Film)",
			"https://de.wikipedia.org/wiki/Matrix_(Film)",
			"https://de.wikipedia.org/wiki/Event_Horizon_%E2%80%93_Am_Rande_des_Universums",
			"https://de.wikipedia.org/wiki/Matrix_Reloaded",
			"https://de.wikipedia.org/wiki/Matrix_Revolutions",
			"https://de.wikipedia.org/wiki/Resident_Evil:_Apocalypse",
			"https://de.wikipedia.org/wiki/Resident_Evil_(Film)",
			"https://de.wikipedia.org/wiki/Train_to_Busan", // from south-korea... so maybe isolated subgraph
			"https://de.wikipedia.org/wiki/Toni_Erdmann", // from germany... so maybe isolated subgraph
			"https://de.wikipedia.org/wiki/Silent_Hill_(Film)",
			"https://de.wikipedia.org/wiki/Silent_Hill:_Revelation",
			"https://de.wikipedia.org/wiki/Oldboy_(2003)" // from south-korea... so maybe isolated subgraph
		);
		
		urls.stream().forEach(url -> WikipediaDataset.createYaml(url, downloadPath));
	}
}
