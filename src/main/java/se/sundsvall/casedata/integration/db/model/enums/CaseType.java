package se.sundsvall.casedata.integration.db.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(example = "PARKING_PERMIT")
public enum CaseType {

    // BYGGR
    NYBYGGNAD_ANSOKAN_OM_BYGGLOV("BUILD"),
    ANMALAN_ATTEFALL("BUILD"),
    // ECOS
    REGISTRERING_AV_LIVSMEDEL("ENV"),
    ANMALAN_INSTALLATION_VARMEPUMP("ENV"),
    ANSOKAN_TILLSTAND_VARMEPUMP("ENV"),
    ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP("ENV"),
    ANMALAN_INSTALLTION_ENSKILT_AVLOPP_UTAN_WC("ENV"),
    ANMALAN_ANDRING_AVLOPPSANLAGGNING("ENV"),
    ANMALAN_ANDRING_AVLOPPSANORDNING("ENV"),
    ANMALAN_HALSOSKYDDSVERKSAMHET("ENV"),

    PARKING_PERMIT("PRH"),
    PARKING_PERMIT_RENEWAL("PRH"),
    LOST_PARKING_PERMIT("PRH");

    @Getter
    private final String abbreviation;

    CaseType(String abbreviation) {
        this.abbreviation = abbreviation;
    }
}
