package io.github.orientdb.example.entities;

import java.util.List;

import lombok.Data;

@Data
public class Book {
	private String title;
	private String author;
	private String publisher;
	private int release;
	private List<String> characters;
}
