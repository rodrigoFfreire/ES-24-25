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
import pt.ulisboa.tecnico.socialsoftware.humanaethica.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.User

import spock.lang.Unroll

@DataJpaTest
class GetActivitySuggestionsServiceTest extends SpockTest {

    public static final String EXIST = 'exist'
    public static final String NO_EXIST = 'noExist'
    def member
    def institution
    def volunteer

    def setup() {
        institution = institutionService.getDemoInstitution()
        member = createMember(USER_1_NAME, USER_1_USERNAME, USER_1_PASSWORD, USER_1_EMAIL, AuthUser.Type.DEMO, institution, User.State.APPROVED)
        volunteer = createVolunteer(USER_2_NAME, USER_2_USERNAME, USER_2_EMAIL, AuthUser.Type.DEMO, User.State.APPROVED)

        def suggestionDto = new ActivitySuggestionDto()
        suggestionDto.setName(SUGGESTION_NAME_1)
        suggestionDto.setRegion(SUGGESTION_REGION_1)
        suggestionDto.setParticipantsNumberLimit(1)
        suggestionDto.setDescription(SUGGESTION_DESCRIPTION_1)
        suggestionDto.setApplicationDeadline(DateHandler.toISOString(IN_SEVEN_DAYS))
        suggestionDto.setStartingDate(DateHandler.toISOString(IN_TWO_DAYS))
        suggestionDto.setEndingDate(DateHandler.toISOString(IN_THREE_DAYS))

        def suggestion = new ActivitySuggestion(institution, volunteer, suggestionDto)
        activitySuggestionRepository.save(suggestion)
    }

    def "member gets suggestions successfully"() {
        when:
        def result = activitySuggestionService.getInstitutionActivitySuggestions(member.id, institution.id)

        then:
        result.size() == 1
        result.get(0).name == SUGGESTION_NAME_1
    }

    @Unroll
    def "invalid get: memberId=#memberId | institutionId=#institutionId"() {
        when:
        activitySuggestionService.getInstitutionActivitySuggestions(getMemberId(memberId), getInstitutionId(institutionId))

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == errorMessage

        where:
        memberId | institutionId || errorMessage
        null     | EXIST         || ErrorMessage.USER_NOT_FOUND
        NO_EXIST | EXIST         || ErrorMessage.USER_NOT_FOUND
        EXIST    | null          || ErrorMessage.INSTITUTION_NOT_FOUND
        EXIST    | NO_EXIST      || ErrorMessage.INSTITUTION_NOT_FOUND
    }

    def getMemberId(memberId) {
        if (memberId == EXIST) return member.id
        if (memberId == NO_EXIST) return 999
        return null
    }

    def getInstitutionId(institutionId) {
        if (institutionId == EXIST) return institution.id
        if (institutionId == NO_EXIST) return 999
        return null
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
