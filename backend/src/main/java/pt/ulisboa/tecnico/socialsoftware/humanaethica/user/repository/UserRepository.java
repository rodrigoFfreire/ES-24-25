package pt.ulisboa.tecnico.socialsoftware.humanaethica.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.User;

@Repository
@Transactional
public interface UserRepository extends JpaRepository<User, Integer> {
    @Query("SELECT COUNT(DISTINCT v.id) FROM User v JOIN Participation p ON p.volunteer.id = v.id " +
           "JOIN Activity a ON p.activity.id = a.id " +
           "WHERE a.institution.id = :institutionId AND v.role = 'VOLUNTEER' AND v.state != 'DELETED'")
    Integer countUniqueVolunteersByInstitution(Integer institutionId);
}
