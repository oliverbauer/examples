package io.github.orientdb.example.entities;

import java.util.List;

import lombok.Data;

@Data
public class Movie {
	private String name;
	private String director;
	private Book basedOn;
	private List<Actor> actors;
}
