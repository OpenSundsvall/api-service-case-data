package se.sundsvall.casedata.integration.db.model.enums;

import lombok.Getter;

public enum StakeholderType {

    PERSON(Constants.PERSON_VALUE), ORGANIZATION(Constants.ORGANIZATION_VALUE);

    @Getter
    private final String text;

    StakeholderType(String text) {
        this.text = text;
    }

    public static class Constants {
        private Constants() {

        }

        public static final String PERSON_VALUE = "PERSON";
        public static final String ORGANIZATION_VALUE = "ORGANIZATION";
    }


}