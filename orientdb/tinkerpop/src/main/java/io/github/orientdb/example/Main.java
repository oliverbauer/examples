package io.github.orientdb.example;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.tinkerpop.gremlin.orientdb.OrientGraphFactory;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import io.github.orientdb.example.entities.Actor;
import io.github.orientdb.example.entities.Book;
import io.github.orientdb.example.entities.Movie;

public class Main {
	public static void main(String args[]) throws JsonParseException, JsonMappingException, IOException, URISyntaxException {
		OrientGraphFactory orientGraphFactory = new OrientGraphFactory("remote:localhost:2424/demodb");
		GraphTraversalSource g = orientGraphFactory.getTx().traversal();
		
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		mapper.findAndRegisterModules();
		mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		
	    for (File file : getFilesFromResource("yaml/books")) {
			InputStream is = Main.class.getResourceAsStream("/yaml/books/"+file.getName());

			Book book = mapper.readValue(is, Book.class);
			
			GraphTraversal<Vertex,Vertex> bookV = g.addV("Book");
			bookV.property("title", book.getTitle());
			if (book.getAuthor() != null) {
				bookV.property("author", book.getAuthor());
			}
			if (book.getPublisher() != null) {
				bookV.property("publisher", book.getPublisher());
			}
			Vertex bookVertex = bookV.next(); // submit to database
			System.out.println("Added Book with title "+book.getTitle());
	
			// Author => Book
			GraphTraversal<Vertex, Vertex> authorTraversal = g.V().hasLabel("Person").has("name", book.getAuthor());
			if (authorTraversal.hasNext()) {
				Vertex authorVertex = authorTraversal.next();
				authorVertex.addEdge("Author_of", bookVertex);
			} else {
				Vertex authorVertex = g.addV("Person").property("name", book.getAuthor()).next();
				authorVertex.addEdge("Author_of", bookVertex);
			}
			
			if (book.getCharacters() != null && !book.getCharacters().isEmpty()) {
				for (String character : book.getCharacters()) {
					
					boolean exists = g.V().has("name", character).hasNext();
					
					Vertex characterVertex;
					if (!exists) {
						System.out.println("  Added Person with name "+character+" and connected it to book "+book.getTitle());
						characterVertex = g.addV("Person").property("name", character).next();
						g.addE("Character_of").from(characterVertex).to(bookVertex).next();
					} else {
						// Already connects? TODO if not: Add connection
						System.out.println("  Found Person with name "+character+". Connected it to book "+book.getTitle());
						characterVertex = g.V().has("name", character).next();
						g.addE("Character_of").from(characterVertex).to(bookVertex).next();
					}
				}
			}
	    }
		
	    for (File file : getFilesFromResource("yaml/movies")) {
			InputStream is = Main.class.getResourceAsStream("/yaml/movies/"+file.getName());
			Movie movie = mapper.readValue(is, Movie.class);
			
			String movieName = movie.getName();
			List<Actor> actors = movie.getActors();
			
			GraphTraversal<Vertex,Vertex> movieV = g.addV("Movie");
			movieV = movieV.property("name", movieName);
			
			Vertex movieVertex = movieV.next(); // submit to database
			System.out.println("Added Movie with name "+movieName);
			
			if (actors != null && !actors.isEmpty()) {
				for (Actor actor : actors) {
					
					String personName = actor.getName();
					
					boolean exists = g.V().hasLabel("Person").has("name", personName).hasNext();
					Vertex characterVertex;
					if (!exists) {
						System.out.println("  Created Person(real) with name "+personName+". Created actor to Movie "+movieName);
						characterVertex = g
							.addV("Person")
							.property("name", personName)
							.next();
						characterVertex.addEdge("Actor", movieVertex);
					} else {
						// Already connects? TODO if not: Add connection
						System.out.println("  Found Person(real) with name "+personName+". Created actor to Movie "+movieName);
						characterVertex = g.V().has("name", personName).next();
						characterVertex.addEdge("Actor", movieVertex);
					}
					
					String role = actor.getRole();
					exists = g.V().hasLabel("Person").has("name", role).hasNext();
					Vertex roleVertex;
					if (!exists) {
						System.out.println("  Created Person(role) with name "+role+". Created Role to Movie "+movieName);
						roleVertex = g
							.addV("Person")
							.property("name", role)
							.next();
						characterVertex.addEdge("Role", roleVertex);
						
						roleVertex.addEdge("Role", movieVertex);
					} else {
						// Already connects? TODO if not: Add connection
						System.out.println("  Found Person(role) with name "+role+". Created Role to Movie "+movieName);
						roleVertex = g.V().has("name", role).next();
						characterVertex.addEdge("Role", roleVertex);
						
						roleVertex.addEdge("Role", movieVertex);
					}
				}
			}
		}
	    
	    g.tx().commit();
	    orientGraphFactory.close();
	}
	
	/**
	 * Without '/'...
	 * 
	 * @param resourcePath
	 * @return
	 */
	private static File[] getFilesFromResource(String resourcePath) {
	    String path = Thread
	    	.currentThread()
	    	.getContextClassLoader()
	    	.getResource(resourcePath)
	    	.getPath();
	    return new File(path).listFiles();
	}
}
