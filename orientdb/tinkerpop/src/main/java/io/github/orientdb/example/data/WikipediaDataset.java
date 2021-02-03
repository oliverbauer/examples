package io.github.orientdb.example.data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.LoggerFactory;

public class WikipediaDataset {
	public static void createYaml(String url, String outputPath) {
		Document doc = null;
		try {
			doc = Jsoup.connect(url).get();
		} catch (IOException e) {
			LoggerFactory.getLogger(WikipediaDataset.class).error("Unable to fetch url "+url+":", e);
			return;
		}
		
		String movieTitle = doc.title().replace(" – Wikipedia", "").replace("\\(Film\\)", "").trim();
		
		StringBuilder sb = new StringBuilder();
		if (movieTitle.contains(":")) {
			sb.append("name: \""+movieTitle+"\"\n");
		} else {
			sb.append("name: "+movieTitle+"\n");
		}
		sb.append("\n");
		sb.append("actors:\n");

		Elements trs = doc.select("table>tbody>tr");
		for (int i=0; i<=trs.size()-1; i++) {
			Element tr = trs.get(i);
			Elements span = tr.select("th>span");
			if (span.text().equals("Besetzung")) {
				Element besetzung = trs.get(i+1);
				Elements rows = besetzung.select("td>ul>li");
				for (Element row : rows) {
					Elements select = row.select("a");
					
					// only if action=edit not contained
					boolean autorHasWikipediaPage = !select.outerHtml().contains("action=edit");
					
					if (autorHasWikipediaPage) {
						String darsteller = select.text();
						String rolle = row.ownText().replace(":", "").trim();
						
						if (darsteller.length() > 0 && rolle.length() > 0) {
							sb.append("- name: "+darsteller+"\n");
							sb.append("  role: "+rolle+"\n");
						}
					}
				}
			}
		}
		
		String filename = outputPath+"/movie-"+movieTitle.replace(" ", "_")+".yaml";
		try (FileWriter fw = new FileWriter(new File(filename))) {
			fw.append(sb.toString());
			
			LoggerFactory.getLogger(WikipediaDataset.class).info("Created file {}", filename);
		} catch (IOException e) {
			LoggerFactory.getLogger(WikipediaDataset.class).error("Unable to write file to path "+outputPath+": ", e);
		}
	}
}
