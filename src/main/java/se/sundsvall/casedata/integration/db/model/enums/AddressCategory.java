package se.sundsvall.casedata.integration.db.model.enums;

import lombok.Getter;

public enum AddressCategory {
    POSTAL_ADDRESS("Postadress"), INVOICE_ADDRESS("Fakturaadress"), VISITING_ADDRESS("Besöksadress");

    @Getter
    private final String text;
    AddressCategory(String text) {
        this.text = text;
    }
}
