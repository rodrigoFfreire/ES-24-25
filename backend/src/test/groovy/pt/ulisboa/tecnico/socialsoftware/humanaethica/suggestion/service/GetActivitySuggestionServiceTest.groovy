package pt.ulisboa.tecnico.socialsoftware.humanaethica.suggestion.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.suggestion.domain.ActivitySuggestion
import pt.ulisboa.tecnico.socialsoftware.humanaethica.suggestion.dto.ActivitySuggestionDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain.Institution
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.User

@DataJpaTest
class GetActivitySuggestionsServiceTest extends SpockTest {
    def setup() {
        def institution = createInstitution(INSTITUTION_1_NAME, INSTITUTION_1_EMAIL, INSTITUTION_1_NIF)
        def volunteer = createVolunteer(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL, AuthUser.Type.DEMO, User.State.APPROVED)

        given: "suggestion info"
        def suggestionDto = createActivitySuggestionDto(SUGGESTION_NAME_1, SUGGESTION_REGION_1, SUGGESTION_PARTICIPANTS_LIMIT_1, SUGGESTION_DESCRIPTION_1,
                IN_ONE_DAY, IN_TWO_DAYS, IN_THREE_DAYS, ActivitySuggestion.State.IN_REVIEW)
        
        and: "a suggestion"
        def suggestion = new ActivitySuggestion(institution, volunteer, suggestionDto)
        activitySuggestionRepository.save(suggestion)

        and: "another suggestion"
        suggestionDto.name = SUGGESTION_NAME_2
        suggestion = new ActivitySuggestion(institution, volunteer, suggestionDto)
        activitySuggestionRepository.save(suggestion)
    }   

    def 'get two activity suggestions'() {
        when:
        def result = activitySuggestionService.getActivitySuggestionsByInstitution(INSTITUTION_1_NAME)

        then:
        result.size() == 2
        result.get(0).name == SUGGESTION_NAME_1
        result.get(1).name == SUGGESTION_NAME_2
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
