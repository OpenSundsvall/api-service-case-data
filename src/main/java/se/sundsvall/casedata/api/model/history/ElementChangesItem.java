package se.sundsvall.casedata.api.model.history;

import lombok.Data;

@Data
public class ElementChangesItem{
	private String elementChangeType;
	private int index;
	private Value value;
}