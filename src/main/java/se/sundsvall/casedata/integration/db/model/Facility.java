package se.sundsvall.casedata.integration.db.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;
import se.sundsvall.casedata.integration.db.listeners.FacilityListener;
import se.sundsvall.casedata.integration.db.model.enums.FacilityType;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Entity(name = "facility")
@EntityListeners(FacilityListener.class)
@Getter
@Setter
public class Facility extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "errand_id", foreignKey = @ForeignKey(name = "FK_facility_errand_id"))
    @JsonBackReference
    private Errand errand;

    @Column(name = "description")
    private String description;

    @Embedded
    private Address address;

    @Column(name = "facility_collection_name")
    private String facilityCollectionName;

    @Column(name = "main_facility")
    private boolean mainFacility;

    @Column(name = "facility_type")
    private FacilityType facilityType;

    @ElementCollection
    @CollectionTable(name = "facility_extra_parameters",
            joinColumns = @JoinColumn(name = "facility_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_facility_extra_parameters_facility_id")))
    @MapKeyColumn(name = "extra_parameter_key")
    @Column(name = "extra_parameter_value")
    private Map<String, String> extraParameters = new HashMap<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Facility facility)) return false;
        if (!super.equals(o)) return false;
        return mainFacility == facility.mainFacility && Objects.equals(description, facility.description) && Objects.equals(address, facility.address) && Objects.equals(facilityCollectionName, facility.facilityCollectionName) && facilityType == facility.facilityType && Objects.equals(extraParameters, facility.extraParameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), description, address, facilityCollectionName, mainFacility, facilityType, extraParameters);
    }

    @Override
    public String toString() {
        long errandId = errand == null ? 0 : errand.getId();

        return "Facility{" +
                "errand.id=" + errandId +
                ", description='" + description + '\'' +
                ", address=" + address +
                ", facilityCollectionName='" + facilityCollectionName + '\'' +
                ", mainFacility=" + mainFacility +
                ", facilityType=" + facilityType +
                ", extraParameters=" + extraParameters +
                "} " + super.toString();
    }

}
