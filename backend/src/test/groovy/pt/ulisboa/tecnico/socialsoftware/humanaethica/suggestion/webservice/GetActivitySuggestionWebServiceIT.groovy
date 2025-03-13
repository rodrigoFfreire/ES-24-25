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
import pt.ulisboa.tecnico.socialsoftware.humanaethica.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.User


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GetActivitySuggestionsWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    private def institution
    private def member

    def setup() {
        deleteAll()

        webClient = WebClient.create("http://localhost:" + port)
        headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)

        institution = institutionService.getDemoInstitution()
        member = createMember(USER_1_NAME, USER_1_USERNAME, USER_1_PASSWORD, USER_1_EMAIL, AuthUser.Type.DEMO, institution, User.State.APPROVED)

        def suggestionDto = createActivitySuggestionDto(
            SUGGESTION_NAME_1,
            SUGGESTION_REGION_1,
            SUGGESTION_PARTICIPANTS_LIMIT_1,
            SUGGESTION_DESCRIPTION_1,
            NOW,
            IN_TWO_DAYS,
            IN_SEVEN_DAYS,
            ActivitySuggestion.State.IN_REVIEW
        )
        activitySuggestionRepository.save(suggestionDto.toEntity(institution, member))
    }

    def "get suggestions as member"() {
        given:
        def authDto = demoMemberLogin()
        headers.setBearerAuth(authDto.getToken())

        when:
        def response = webClient.get()
                .uri("/suggestions/${institution.id}")
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .retrieve()
                .bodyToFlux(ActivitySuggestionDto.class)
                .collectList()
                .block()

        then:
        response.size() == 1
        response.get(0).name == SUGGESTION_NAME_1
    }

    def "volunteer cannot get suggestions"() {
        given:
        def authDto = demoVolunteerLogin()
        headers.setBearerAuth(authDto.getToken())

        when:
        webClient.get()
                .uri("/suggestions/${institution.id}")
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .retrieve()
                .bodyToMono(ActivitySuggestionDto.class)
                .block()

        then:
        def error = thrown(WebClientResponseException)
        error.statusCode == HttpStatus.FORBIDDEN
    }

    def "non-authenticated user cannot get suggestions"() {
        given:
        headers.remove(HttpHeaders.AUTHORIZATION)

        when:
        webClient.get()
                .uri("/suggestions/${institution.id}")
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .retrieve()
                .bodyToMono(ActivitySuggestionDto.class)
                .block()

        then:
        def error = thrown(WebClientResponseException)
        error.statusCode == HttpStatus.FORBIDDEN
    }
}
