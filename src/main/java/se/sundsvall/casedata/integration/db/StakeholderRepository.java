package se.sundsvall.casedata.integration.db;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import se.sundsvall.casedata.integration.db.model.Stakeholder;
import se.sundsvall.casedata.integration.db.model.enums.StakeholderRole;

import java.util.List;

@JaversSpringDataAuditable
public interface StakeholderRepository extends JpaRepository<Stakeholder, Long>, JpaSpecificationExecutor<Stakeholder> {
    List<Stakeholder> findByRoles(StakeholderRole stakeholderRole);
}
