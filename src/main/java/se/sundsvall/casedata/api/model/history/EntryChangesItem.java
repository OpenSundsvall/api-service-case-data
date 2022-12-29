package se.sundsvall.casedata.api.model.history;

import lombok.Data;

@Data
public class EntryChangesItem{
	private String entryChangeType;
	private String value;
	private String key;
}