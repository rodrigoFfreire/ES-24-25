package pt.ulisboa.tecnico.socialsoftware.humanaethica.suggestion.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain.Institution;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.repository.InstitutionRepository;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.suggestion.domain.ActivitySuggestion;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.suggestion.dto.ActivitySuggestionDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.suggestion.repository.ActivitySuggestionRepository;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.User;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.repository.UserRepository;

import static pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage.*;

@Service
public class ActivitySuggestionService {

    private final UserRepository userRepository;
    private final InstitutionRepository institutionRepository;
    private final ActivitySuggestionRepository activitySuggestionRepository;

    public ActivitySuggestionService(UserRepository userRepository, InstitutionRepository institutionRepository,
                                     ActivitySuggestionRepository activitySuggestionRepository) {
        this.userRepository = userRepository;
        this.institutionRepository = institutionRepository;
        this.activitySuggestionRepository = activitySuggestionRepository;
    }

    @Transactional
    public ActivitySuggestionDto createActivitySuggestion(Integer userId, Integer institutionId, ActivitySuggestionDto activitySuggestionDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new HEException(USER_NOT_FOUND, userId.toString()));

        if (!(user instanceof Volunteer)) {
            throw new HEException(ONLY_VOLUNTEERS_CAN_SUGGEST);
        }
        Volunteer volunteer = (Volunteer) user;

        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new HEException(INSTITUTION_NOT_FOUND, institutionId.toString()));

        ActivitySuggestion suggestion = new ActivitySuggestion(activitySuggestionDto, institution, volunteer);
        activitySuggestionRepository.save(suggestion);

        return new ActivitySuggestionDto(suggestion, false, false);
    }
}
