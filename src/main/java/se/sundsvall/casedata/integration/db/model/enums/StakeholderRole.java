package se.sundsvall.casedata.integration.db.model.enums;

import lombok.Getter;

public enum StakeholderRole {

    // Kontrollansvarig
    CONTROL_OFFICIAL("KOA"),
    // Sökande
    APPLICANT("SOK"),
    // Fastighetsägare
    PROPERTY_OWNER("FAG"),
    // Betalningsansvarig
    PAYMENT_PERSON("BETA"),

    INVOICE_RECIPENT("Fakturamottagare"),
    OPERATOR("Verksamhetsutövare"),

    CONTACT_PERSON("KPER"),
    ADMINISTRATOR("Handläggare"),
    FELLOW_APPLICANT("Medsökande"),
    DRIVER("Förare"),
    PASSENGER("Passagerare"),
    DOCTOR("Läkare");

    @Getter
    private final String text;

    StakeholderRole(String text) {
        this.text = text;
    }

}
