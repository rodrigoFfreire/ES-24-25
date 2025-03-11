package pt.ulisboa.tecnico.socialsoftware.humanaethica.suggestion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.suggestion.domain.ActivitySuggestion;

import java.util.List;
import java.util.Set;

@Repository
@Transactional
public interface ActivitySuggestionRepository extends JpaRepository<ActivitySuggestion, Integer> {
}