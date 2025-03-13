package pt.ulisboa.tecnico.socialsoftware.humanaethica.suggestion.domain

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.suggestion.dto.ActivitySuggestionDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.suggestion.domain.ActivitySuggestion
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain.Institution
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler
import spock.lang.Unroll

import java.time.LocalDateTime

@DataJpaTest
class CreateActivitySuggestionMethodTest extends SpockTest {
    Institution institution = Mock()
    Volunteer volunteer = Mock()
    ActivitySuggestion otherActivitySuggestion = Mock()
    def activitySuggestionDto

    def setup() {
        given: "activity suggestion info"
        activitySuggestionDto = new ActivitySuggestionDto()
        activitySuggestionDto.name = ACTIVITY_NAME_1
        activitySuggestionDto.region = ACTIVITY_REGION_1
        activitySuggestionDto.participantsNumberLimit = 2
        activitySuggestionDto.description = ACTIVITY_DESCRIPTION_1
        activitySuggestionDto.startingDate = DateHandler.toISOString(IN_TWO_DAYS)
        activitySuggestionDto.endingDate = DateHandler.toISOString(IN_THREE_DAYS)
        activitySuggestionDto.applicationDeadline = DateHandler.toISOString(IN_SEVEN_DAYS)
        activitySuggestionDto.setState(ActivitySuggestion.State.IN_REVIEW.name())
    }

    def "create activitySuggestion with volunteer and institution "() {
        given:
        otherActivitySuggestion.getName() >> ACTIVITY_NAME_2
        institution.getActivitySuggestions() >> [otherActivitySuggestion]
        volunteer.getActivitySuggestions() >> [otherActivitySuggestion]

        when:
        def result = new ActivitySuggestion(institution, volunteer, activitySuggestionDto)

        then: "check result"
        result.getInstitution() == institution
        result.getVolunteer() == volunteer
        result.getName() == ACTIVITY_NAME_1
        result.getRegion() == ACTIVITY_REGION_1
        result.getParticipantsNumberLimit() == 2
        result.getDescription() == ACTIVITY_DESCRIPTION_1
        result.getStartingDate() == IN_TWO_DAYS
        result.getEndingDate() == IN_THREE_DAYS
        result.getApplicationDeadline() == IN_SEVEN_DAYS
        result.getState() == ActivitySuggestion.State.IN_REVIEW

        and: "invocations"
        1 * institution.addActivitySuggestion(_)
        1 * volunteer.addActivitySuggestion(_)
    }

    @Unroll
    def "create activitySuggestion and violate invariant description must have at least 10 characters: description=#description"() {
        given:
        otherActivitySuggestion.getName() >> ACTIVITY_NAME_2
        institution.getActivitySuggestions() >> [otherActivitySuggestion]
        volunteer.getActivitySuggestions() >> [otherActivitySuggestion]


        and: "an activitySuggestion dto"
        activitySuggestionDto = new ActivitySuggestionDto()
        activitySuggestionDto.setName(ACTIVITY_NAME_1)
        activitySuggestionDto.setRegion(ACTIVITY_REGION_1)
        activitySuggestionDto.setParticipantsNumberLimit(1)
        activitySuggestionDto.setDescription(description)
        activitySuggestionDto.setApplicationDeadline(DateHandler.toISOString(IN_SEVEN_DAYS))
        activitySuggestionDto.setStartingDate(DateHandler.toISOString(IN_TWO_DAYS))
        activitySuggestionDto.setEndingDate(DateHandler.toISOString(IN_THREE_DAYS))
        activitySuggestionDto.setState(ActivitySuggestion.State.IN_REVIEW.name())

        when:
        new ActivitySuggestion(institution, volunteer, activitySuggestionDto)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == errorMessage

        where:
        description            || errorMessage
        null                   || ErrorMessage.ACTIVITY_SUGGESTION_DESCRIPTION_INVALID
        "  "                   || ErrorMessage.ACTIVITY_SUGGESTION_DESCRIPTION_INVALID
        "123456789"            || ErrorMessage.ACTIVITY_SUGGESTION_DESCRIPTION_INVALID
    }

    @Unroll
    def "create activity violate date precedence invariants: deadline=#deadline"() {
        given:
        otherActivitySuggestion.getName() >> ACTIVITY_NAME_2
        institution.getActivitySuggestions() >> [otherActivitySuggestion]
        volunteer.getActivitySuggestions() >> [otherActivitySuggestion]


        and: "an activitySuggestion dto"
        activitySuggestionDto = new ActivitySuggestionDto()
        activitySuggestionDto.setName(ACTIVITY_NAME_1)
        activitySuggestionDto.setRegion(ACTIVITY_REGION_1)
        activitySuggestionDto.setParticipantsNumberLimit(1)
        activitySuggestionDto.setDescription(ACTIVITY_DESCRIPTION_1)
        activitySuggestionDto.setApplicationDeadline(deadline instanceof LocalDateTime ? DateHandler.toISOString(deadline) : deadline as String)
        activitySuggestionDto.setStartingDate(DateHandler.toISOString(IN_ONE_DAY))
        activitySuggestionDto.setEndingDate(DateHandler.toISOString(IN_THREE_DAYS))
        activitySuggestionDto.setState(ActivitySuggestion.State.IN_REVIEW.name())

        when:
        new ActivitySuggestion(institution, volunteer, activitySuggestionDto)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == errorMessage

        where:
        deadline       | errorMessage
        THREE_DAYS_AGO | ErrorMessage.ACTIVITY_SUGGESTION_INVALID_APPLICATION_DEADLINE
        NOW            | ErrorMessage.ACTIVITY_SUGGESTION_INVALID_APPLICATION_DEADLINE
        IN_ONE_DAY     | ErrorMessage.ACTIVITY_SUGGESTION_INVALID_APPLICATION_DEADLINE
        IN_SIX_DAYS    | ErrorMessage.ACTIVITY_SUGGESTION_INVALID_APPLICATION_DEADLINE
     }

   
    @Unroll
    def "create activity violate unique name per volunteer invariant"() {
        given:
        otherActivitySuggestion.getName() >> ACTIVITY_NAME_2
        institution.getActivitySuggestions() >> [otherActivitySuggestion]
        volunteer.getActivitySuggestions() >> [otherActivitySuggestion]
        
        and: "an activity suggestion dto"
        activitySuggestionDto = new ActivitySuggestionDto()
        activitySuggestionDto.setName(ACTIVITY_NAME_2)
        activitySuggestionDto.setRegion(ACTIVITY_REGION_1)
        activitySuggestionDto.setParticipantsNumberLimit(1)
        activitySuggestionDto.setDescription(ACTIVITY_DESCRIPTION_1)
        activitySuggestionDto.setApplicationDeadline(DateHandler.toISOString(IN_SEVEN_DAYS))
        activitySuggestionDto.setStartingDate(DateHandler.toISOString(IN_TWO_DAYS))
        activitySuggestionDto.setEndingDate(DateHandler.toISOString(IN_THREE_DAYS))
        activitySuggestionDto.setState(ActivitySuggestion.State.IN_REVIEW.name())

        when:
        new ActivitySuggestion(institution, volunteer, activitySuggestionDto)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == ErrorMessage.ACTIVITY_SUGGESTION_REPEATED
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}