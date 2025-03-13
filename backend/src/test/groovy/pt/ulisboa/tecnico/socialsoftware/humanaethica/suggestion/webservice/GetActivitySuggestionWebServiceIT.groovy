package pt.ulisboa.tecnico.socialsoftware.humanaethica.suggestion.webservice

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.suggestion.dto.ActivitySuggestionDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.suggestion.domain.ActivitySuggestion
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain.Institution
import pt.ulisboa.tecnico.socialsoftware.humanaethica.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Member
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler
import java.time.temporal.ChronoUnit


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GetActivitySuggestionWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    private def institution
    private def volunteer

    def setup() {
        deleteAll()

        webClient = WebClient.create("http://localhost:" + port)
        headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)

        institution = institutionService.getDemoInstitution()
        volunteer = createVolunteer(USER_2_NAME, USER_2_USERNAME, USER_2_EMAIL, AuthUser.Type.DEMO, User.State.APPROVED)

        def suggestionDto = createActivitySuggestionDto(
            SUGGESTION_NAME_1,
            SUGGESTION_REGION_1,
            SUGGESTION_PARTICIPANTS_LIMIT_1,
            SUGGESTION_DESCRIPTION_1,
            NOW.truncatedTo(ChronoUnit.MICROS),
            IN_TWO_DAYS.truncatedTo(ChronoUnit.MICROS),
            IN_SEVEN_DAYS.truncatedTo(ChronoUnit.MICROS),
            ActivitySuggestion.State.IN_REVIEW
        )
        def suggestion = new ActivitySuggestion(institution, volunteer, suggestionDto)
        activitySuggestionRepository.save(suggestion)    
    }


    def "get suggestions as member of institution"() {
        given:

        createMember(USER_3_NAME, USER_3_USERNAME, USER_3_PASSWORD, USER_3_EMAIL, AuthUser.Type.NORMAL, institution, User.State.APPROVED)
        normalUserLogin(USER_3_USERNAME, USER_3_PASSWORD)

        when:
        def response = webClient.get()
                .uri("/suggestions/" + institution.id + "/suggestions")
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .retrieve()
                .bodyToFlux(ActivitySuggestionDto.class)
                .collectList()
                .block()

        then:
        response.size() == 1
        response.get(0).name == SUGGESTION_NAME_1
        response.get(0).region == SUGGESTION_REGION_1
        response.get(0).description == SUGGESTION_DESCRIPTION_1
        response.get(0).participantsNumberLimit == SUGGESTION_PARTICIPANTS_LIMIT_1
        DateHandler.toLocalDateTime(response.get(0).startingDate).truncatedTo(ChronoUnit.MICROS) == NOW.truncatedTo(ChronoUnit.MICROS)
        DateHandler.toLocalDateTime(response.get(0).endingDate).truncatedTo(ChronoUnit.MICROS) == IN_TWO_DAYS.truncatedTo(ChronoUnit.MICROS)
        DateHandler.toLocalDateTime(response.get(0).applicationDeadline).truncatedTo(ChronoUnit.MICROS) == IN_SEVEN_DAYS.truncatedTo(ChronoUnit.MICROS)
        response.get(0).state == "IN_REVIEW"

        cleanup:
        deleteAll()
    }

    def "get suggestions as member of another institution"() {
        given:
        def otherInstitution = new Institution(INSTITUTION_1_NAME, INSTITUTION_1_EMAIL, INSTITUTION_1_NIF)
        institutionRepository.save(otherInstitution)
        createMember(USER_3_NAME, USER_3_USERNAME, USER_3_PASSWORD, USER_3_EMAIL, AuthUser.Type.NORMAL, otherInstitution, User.State.APPROVED)
        normalUserLogin(USER_3_USERNAME, USER_3_PASSWORD)

        when:
        def response = webClient.get()
                .uri("/suggestions/" + institution.id + "/suggestions")
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .retrieve()
                .bodyToFlux(ActivitySuggestionDto.class)
                .collectList()
                .block()

        then:
        def error = thrown(WebClientResponseException)
        error.statusCode == HttpStatus.FORBIDDEN        

        cleanup:
        deleteAll()
    }

    def "volunteer cannot get suggestions"() {
        given:
        demoVolunteerLogin()
        
        when:
        webClient.get()
                .uri("/suggestions/" +institution.id + "/suggestions")
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .retrieve()
                .bodyToMono(ActivitySuggestionDto.class)
                .block()

        then:
        def error = thrown(WebClientResponseException)
        error.statusCode == HttpStatus.FORBIDDEN

        cleanup:
        deleteAll()
    }

    def "admin cannot get suggestion"() {
        given:
        demoAdminLogin()
        
        when:
        webClient.get()
                .uri("/suggestions/" +institution.id + "/suggestions")
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .retrieve()
                .bodyToMono(ActivitySuggestionDto.class)
                .block()

        then:
        def error = thrown(WebClientResponseException)
        error.statusCode == HttpStatus.FORBIDDEN

        cleanup:
        deleteAll()
    }
    
    def "non-authenticated user cannot get suggestion"() {
        given:
        headers.remove(HttpHeaders.AUTHORIZATION)

        when:
        webClient.get()
                .uri("/suggestions/" + institution.id + "/suggestions")
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .retrieve()
                .bodyToMono(ActivitySuggestionDto.class)
                .block()

        then:
        def error = thrown(WebClientResponseException)
        error.statusCode == HttpStatus.FORBIDDEN

        cleanup:
        deleteAll()
    }

    def "member gets a suggestion that doesnt exist"() {
        given:
        demoMemberLogin()

        when:
        webClient.get()
                .uri("/suggestions/" + institution.id + "/suggestions/999") // 999 should be an invalid suggestion ID
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .retrieve()
                .bodyToMono(ActivitySuggestionDto.class)
                .block()

        then:
        def error = thrown(WebClientResponseException)
        error.statusCode == HttpStatus.NOT_FOUND

        cleanup:
        deleteAll()
    }
}
