package se.sundsvall.casedata.integration.db.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;
import se.sundsvall.casedata.integration.db.listeners.StakeholderListener;
import se.sundsvall.casedata.integration.db.model.enums.StakeholderRole;
import se.sundsvall.casedata.integration.db.model.enums.StakeholderType;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OrderColumn;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Entity(name = "stakeholder")
@EntityListeners(StakeholderListener.class)
@Getter
@Setter
public class Stakeholder extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "errand_id", foreignKey = @ForeignKey(name = "FK_stakeholder_errand_id"))
    @JsonBackReference
    private Errand errand;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private StakeholderType type;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "person_id")
    private String personId;

    @Column(name = "organization_name")
    private String organizationName;

    @Column(name = "organization_number")
    private String organizationNumber;

    @Column(name = "authorized_signatory")
    private String authorizedSignatory;

    @Column(name = "ad_account")
    private String adAccount;

    @ElementCollection
    @CollectionTable(name = "stakeholder_roles",
            joinColumns = @JoinColumn(name = "stakeholder_id", foreignKey = @ForeignKey(name = "FK_stakeholder_roles_stakeholder_id")))
    @OrderColumn(name = "role_order")
    @Enumerated(EnumType.STRING)
    private List<StakeholderRole> roles = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "stakeholder_addresses",
            joinColumns = @JoinColumn(name = "stakeholder_id", foreignKey = @ForeignKey(name = "FK_stakeholder_addresses_stakeholder_id")))
    @OrderColumn(name = "address_order")
    private List<Address> addresses = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "stakeholder_contact_information",
            joinColumns = @JoinColumn(name = "stakeholder_id", foreignKey = @ForeignKey(name = "FK_stakeholder_contact_information_stakeholder_id")))
    @OrderColumn(name = "contact_information_order")
    private List<ContactInformation> contactInformation = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "stakeholder_extra_parameters",
            joinColumns = @JoinColumn(name = "stakeholder_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_stakeholder_extra_parameters_stakeholder_id")))
    @MapKeyColumn(name = "extra_parameter_key")
    @Column(name = "extra_parameter_value")
    private Map<String, String> extraParameters = new HashMap<>();

    @Override
    public String toString() {
        long errandId = errand == null ? 0 : errand.getId();
        return "Stakeholder{" +
                "errand.id=" + errandId +
                ", type=" + type +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", personId='" + personId + '\'' +
                ", organizationName='" + organizationName + '\'' +
                ", organizationNumber='" + organizationNumber + '\'' +
                ", authorizedSignatory='" + authorizedSignatory + '\'' +
                ", adAccount='" + adAccount + '\'' +
                ", roles=" + roles +
                ", addresses=" + addresses +
                ", contactInformation=" + contactInformation +
                ", extraParameters=" + extraParameters +
                "} " + super.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Stakeholder that)) return false;
        if (!super.equals(o)) return false;
        return type == that.type && Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName) && Objects.equals(personId, that.personId) && Objects.equals(organizationName, that.organizationName) && Objects.equals(organizationNumber, that.organizationNumber) && Objects.equals(authorizedSignatory, that.authorizedSignatory) && Objects.equals(adAccount, that.adAccount) && Objects.equals(roles, that.roles) && Objects.equals(addresses, that.addresses) && Objects.equals(contactInformation, that.contactInformation) && Objects.equals(extraParameters, that.extraParameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), type, firstName, lastName, personId, organizationName, organizationNumber, authorizedSignatory, adAccount, roles, addresses, contactInformation, extraParameters);
    }

}
