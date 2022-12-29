package se.sundsvall.casedata.api.model.history;

import lombok.Data;

@Data
public class Value{
	private int cdoId;
	private String entity;
	private String valueObject;
	private String fragment;
	private OwnerId ownerId;
}