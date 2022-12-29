package se.sundsvall.casedata.api.model.history;

import lombok.Data;

import java.util.List;

@Data
public class CommitMetadata{
	private String author;
	private String commitDateInstant;
	private double id;
	private List<Object> properties;
	private String commitDate;
}