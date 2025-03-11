package pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain.InstitutionProfile;

@Repository
@Transactional
public interface InstitutionProfileRepository extends JpaRepository<InstitutionProfile, Integer> {
    @Query(value = "SELECT * FROM institution_profiles ip WHERE ip.institution_id = :institutionId", nativeQuery = true)
    Optional<InstitutionProfile> findInstitutionProfileById(Integer institutionId);
}
