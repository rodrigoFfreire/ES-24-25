
package pt.ulisboa.tecnico.socialsoftware.humanaethica.suggestion;

import org.springframework.beans.factory.annotation.Autowired;
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
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Member;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.repository.UserRepository;
import java.util.List;
import java.util.Comparator;

import static pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage.*;

@Service
public class ActivitySuggestionService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private ActivitySuggestionRepository activitySuggestionRepository;


    @Transactional
    public ActivitySuggestionDto createActivitySuggestion(Integer userId, Integer institutionId, ActivitySuggestionDto activitySuggestionDto) {
        if (activitySuggestionDto == null) throw new HEException(INVALID_ACTIVITY_SUGGESTION_DTO);
        if (userId == null) throw new HEException(USER_NOT_FOUND);
        if (institutionId == null) throw new HEException(INSTITUTION_NOT_FOUND);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new HEException(USER_NOT_FOUND, userId.toString()));

        if (!(user instanceof Volunteer)) {
            throw new HEException(ONLY_VOLUNTEERS_CAN_SUGGEST);
        }
        Volunteer volunteer = (Volunteer) user;

        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new HEException(INSTITUTION_NOT_FOUND));


        ActivitySuggestion suggestion = new ActivitySuggestion(institution, volunteer, activitySuggestionDto);
        activitySuggestionRepository.save(suggestion);

        return new ActivitySuggestionDto(true, true, suggestion);
    }

    @Transactional
    public List<ActivitySuggestionDto> getInstitutionActivitySuggestions(Integer userId, Integer institutionId) {
        if (userId == null) throw new HEException(USER_NOT_FOUND);
        if (institutionId == null) throw new HEException(INSTITUTION_NOT_FOUND);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new HEException(USER_NOT_FOUND, userId));

        if (!(user instanceof Member)) {
            throw new HEException(ONLY_INSTITUTION_MEMBERS_CAN_GET_SUGGESTIONS);
        }

        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new HEException(INSTITUTION_NOT_FOUND, institutionId));
        
        Member member = (Member) user;

        if (member.getInstitution().getId() != institutionId){
            throw new HEException(ONLY_INSTITUTION_MEMBERS_CAN_GET_SUGGESTIONS);
        }

        return activitySuggestionRepository.getActivitySuggestionsByInstitutionId(institutionId).stream()
                .sorted(Comparator.comparing(ActivitySuggestion::getCreationDate))
                .map(activitySuggestion -> new ActivitySuggestionDto(true, true, activitySuggestion))
                .toList();
    }
}
