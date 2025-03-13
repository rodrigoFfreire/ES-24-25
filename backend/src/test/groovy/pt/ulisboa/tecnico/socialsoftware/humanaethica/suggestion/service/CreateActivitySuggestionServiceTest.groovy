package pt.ulisboa.tecnico.socialsoftware.humanaethica.suggestion.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.suggestion.domain.ActivitySuggestion
import pt.ulisboa.tecnico.socialsoftware.humanaethica.suggestion.dto.ActivitySuggestionDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler
import spock.lang.Unroll

@DataJpaTest
class CreateActivitySuggestionServiceTest extends SpockTest {
    public static final String EXIST = 'exist'
    public static final String NO_EXIST = 'noExist'
    def volunteer
    def institution

    def setup() {
        institution = institutionService.getDemoInstitution()
        volunteer = authUserService.loginDemoVolunteerAuth().getUser()
    }

    def 'create activitySuggestion' () {
        given:
        def activitySuggestionDto = new ActivitySuggestionDto()
        activitySuggestionDto.setName(ACTIVITY_NAME_1)
        activitySuggestionDto.setRegion(ACTIVITY_REGION_1)
        activitySuggestionDto.setParticipantsNumberLimit(1)
        activitySuggestionDto.setDescription(ACTIVITY_DESCRIPTION_1)
        activitySuggestionDto.setApplicationDeadline(DateHandler.toISOString(IN_SEVEN_DAYS))
        activitySuggestionDto.startingDate = DateHandler.toISOString(IN_TWO_DAYS)
        activitySuggestionDto.endingDate = DateHandler.toISOString(IN_THREE_DAYS)

        when:
        def result = activitySuggestionService.createActivitySuggestion(volunteer.id, institution.id, activitySuggestionDto)

        then: "good creation"
        result.name == ACTIVITY_NAME_1
        result.region == ACTIVITY_REGION_1
        result.participantsNumberLimit == 1
        result.description == ACTIVITY_DESCRIPTION_1
        result.startingDate == DateHandler.toISOString(IN_TWO_DAYS)
        result.endingDate == DateHandler.toISOString(IN_THREE_DAYS)
        result.applicationDeadline == DateHandler.toISOString(IN_SEVEN_DAYS)
        result.institution.id == institution.id

        and: "the activitySuggestion is saved in the database"
        activitySuggestionRepository.findAll().size() == 1
        
        and: "the data is correct"
        def storedActivitySuggestion = activitySuggestionRepository.findById(result.id).get()
        storedActivitySuggestion.name == ACTIVITY_NAME_1
        storedActivitySuggestion.region == ACTIVITY_REGION_1
        storedActivitySuggestion.participantsNumberLimit == 1
        storedActivitySuggestion.description == ACTIVITY_DESCRIPTION_1
        storedActivitySuggestion.startingDate == IN_TWO_DAYS
        storedActivitySuggestion.endingDate == IN_THREE_DAYS
        storedActivitySuggestion.applicationDeadline == IN_SEVEN_DAYS
        storedActivitySuggestion.institution.id == institution.id
    }

    @Unroll
    def 'invalid arguments: volunteerId=#volunteerId | institutionId=#institutionId'() {
        given:
        def activitySuggestionDto = new ActivitySuggestionDto()

        when:
        def result = activitySuggestionService.createActivitySuggestion(getVolunteerId(volunteerId), getInstitutionId(institutionId), getActivitySuggestionDto(suggestionValue,activitySuggestionDto))

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == errorMessage
        and:
        activitySuggestionRepository.findAll().size() == 0

        where:
        volunteerId | institutionId | suggestionValue || errorMessage
        null        | EXIST         | EXIST           || ErrorMessage.USER_NOT_FOUND
        NO_EXIST    | EXIST         | EXIST           || ErrorMessage.USER_NOT_FOUND
        EXIST       | null          | EXIST           || ErrorMessage.INSTITUTION_NOT_FOUND
        EXIST       | NO_EXIST      | EXIST           || ErrorMessage.INSTITUTION_NOT_FOUND
        EXIST       | EXIST         | null            || ErrorMessage.INVALID_ACTIVITY_SUGGESTION_DTO
    }

    def 'error: admin creates activitySuggestion' () {
        given:
        def admin = demoService.getDemoAdmin()
        def activitySuggestionDto = new ActivitySuggestionDto()

        when:
        def result = activitySuggestionService.createActivitySuggestion(admin.id, institution.id,activitySuggestionDto)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == ErrorMessage.ONLY_VOLUNTEERS_CAN_SUGGEST
        and:
        activitySuggestionRepository.findAll().size() == 0
    }

    def 'error: member creates activitySuggestion' () {
        given:
        def member = authUserService.loginDemoMemberAuth().getUser()
        def activitySuggestionDto = new ActivitySuggestionDto()

        when:
        def result = activitySuggestionService.createActivitySuggestion(member.id, institution.id, activitySuggestionDto)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == ErrorMessage.ONLY_VOLUNTEERS_CAN_SUGGEST
        and:
        activitySuggestionRepository.findAll().size() == 0
    }


    def getVolunteerId(volunteerId) {
        if (volunteerId == EXIST)
            return volunteer.id
        else if (volunteerId == NO_EXIST)
            return 222
        else
            return null
    }

    def getInstitutionId(institutionId) {
        if (institutionId == EXIST)
            return institution.id
        else if (institutionId == NO_EXIST)
            return 222
        else
            return null
    }

    def getActivitySuggestionDto(value, activitySuggestionDto) {
        if (value == EXIST) {
            return activitySuggestionDto
        }
        return null
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
