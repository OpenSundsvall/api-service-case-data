package se.sundsvall.casedata.integration.db.model.enums;

import lombok.Getter;

public enum FacilityType {

    // ByggR - Ärendeklasser (Used for CaseType=NYBYGGNAD_ANSOKAN_OM_BYGGLOV)

    ONE_FAMILY_HOUSE("EB", "enbostadshus"),
    APARTMENT_BLOCK("FB", "flerbostadshus"),
    WEEKEND_COTTAGE("FRI", "fritidshus"),
    OFFICE_BUILDING("KB", "kontorsbyggnad"),
    INDUSTRIAL_BUILDING("IND", "industribyggnad"),
    GARAGE("GA", "garage"),
    CARPORT("CP", "carport"),
    STOREHOUSE("FÖRR", "förråd"),
    GREENHOUSE("VX", "växthus"),
    GUEST_HOUSE("GÄST", "gäststuga"),
    WAREHOUSE("LB", "lagerbyggnad"),
    WORKSHOP_BUILDING("VERK", "verkstadsbyggnad"),
    RESTAURANT("REST", "restaurang"),
    SCHOOL("SKOL", "skola"),
    PRESCHOOL("FÖRS", "förskola"),
    // Parkering & Cykelparkering
    PARKING("PAR", "parkering"),
    DEPOT("UPP", "upplag"),
    MARINA("SMÅBH", "småbåtshamn"),
    WALL("MUR", "mur"),
    PALING("PL", "plank"),
    RECYCLING_STATION("ÅTER", "återvinningsstation"),
    OTHER("ÖVRI", "övrigt"),

    // ByggR - Ärendeslag (Used for CaseType=ANMALAN_ATTEFALL)
    FURNISHING_OF_ADDITIONAL_DWELLING("INRED", "inredning av ytterligare bostad"),
    ANCILLARY_BUILDING("KB", "komplementbyggnad"),
    ANCILLARY_HOUSING_BUILDING("KBB", "komplementbostadsbyggnad"),
    DORMER("TAK", "takkupa"),
    EXTENSION("TILL", "tillbyggnad");

    @Getter
    private final String value;
    @Getter
    private final String description;

    FacilityType(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
