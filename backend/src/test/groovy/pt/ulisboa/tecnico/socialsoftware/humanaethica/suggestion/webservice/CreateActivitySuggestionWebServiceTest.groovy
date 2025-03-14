package pt.ulisboa.tecnico.socialsoftware.humanaethica.suggestion.webservice

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain.Institution
import pt.ulisboa.tecnico.socialsoftware.humanaethica.suggestion.dto.ActivitySuggestionDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.suggestion.domain.ActivitySuggestion
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler

import java.time.Instant
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CreateActivitySuggestionWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    private String two_days
    private String three_days
    private String seven_days

    def institution
    def activitySuggestionDto

    def setup() {
        deleteAll()

        webClient = WebClient.create("http://localhost:" + port)
        headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)

        institution = institutionService.getDemoInstitution()

        two_days = DateHandler.toISOString(IN_TWO_DAYS.truncatedTo(ChronoUnit.MICROS))
        three_days = DateHandler.toISOString(IN_THREE_DAYS.truncatedTo(ChronoUnit.MICROS))
        seven_days = DateHandler.toISOString(IN_SEVEN_DAYS.truncatedTo(ChronoUnit.MICROS))
        

        activitySuggestionDto = new ActivitySuggestionDto()
        activitySuggestionDto.setName(ACTIVITY_NAME_1)
        activitySuggestionDto.setRegion(ACTIVITY_REGION_1)
        activitySuggestionDto.setParticipantsNumberLimit(1)
        activitySuggestionDto.setDescription(ACTIVITY_DESCRIPTION_1)
        activitySuggestionDto.setApplicationDeadline(seven_days)
        activitySuggestionDto.setStartingDate(two_days)
        activitySuggestionDto.setEndingDate(three_days)
        activitySuggestionDto.setState(ActivitySuggestion.State.IN_REVIEW.name())

    }

    def 'volunteer creates suggestion'() {
        given:
        demoVolunteerLogin()

        when:
        def response = webClient.post()
                .uri('/suggestions/' + institution.id)
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .bodyValue(activitySuggestionDto)
                .retrieve()
                .bodyToMono(ActivitySuggestionDto.class)
                .block()

        then:
        activitySuggestionDto.name == ACTIVITY_NAME_1
        activitySuggestionDto.region == ACTIVITY_REGION_1
        activitySuggestionDto.participantsNumberLimit == 1
        activitySuggestionDto.description == ACTIVITY_DESCRIPTION_1
        activitySuggestionDto.applicationDeadline == seven_days
        activitySuggestionDto.startingDate == two_days
        activitySuggestionDto.endingDate == three_days
        and:
        activitySuggestionRepository.getActivitySuggestionsByInstitutionId(institution.id).size() == 1
        def storedActivitySuggestion = activitySuggestionRepository.getActivitySuggestionsByInstitutionId(institution.id).get(0)
        storedActivitySuggestion.name == ACTIVITY_NAME_1
        storedActivitySuggestion.region == ACTIVITY_REGION_1
        storedActivitySuggestion.participantsNumberLimit == 1
        storedActivitySuggestion.description == ACTIVITY_DESCRIPTION_1
        DateHandler.toISOString(storedActivitySuggestion.applicationDeadline.truncatedTo(ChronoUnit.MICROS)) == seven_days
        DateHandler.toISOString(storedActivitySuggestion.startingDate.truncatedTo(ChronoUnit.MICROS)) == two_days
        DateHandler.toISOString(storedActivitySuggestion.endingDate.truncatedTo(ChronoUnit.MICROS)) == three_days    
        cleanup:
        deleteAll()
    }

    def 'volunteer create suggestion with error'() {
        given:
        demoVolunteerLogin()
        and:
        activitySuggestionDto.description = null

        when:
        def response = webClient.post()
                .uri('/suggestions/' + institution.id)
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .bodyValue(activitySuggestionDto)
                .retrieve()
                .bodyToMono(ActivitySuggestionDto.class)
                .block()

        then:
        def error = thrown(WebClientResponseException)
        error.statusCode == HttpStatus.BAD_REQUEST
        activitySuggestionRepository.count() == 0

        cleanup:
        deleteAll()
    }

    def 'member cannot create suggestion'() {
        given:
        demoMemberLogin()

        when:
        def response = webClient.post()
                .uri('/suggestions/' + institution.id)
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .bodyValue(activitySuggestionDto)
                .retrieve()
                .bodyToMono(ActivitySuggestionDto.class)
                .block()

        then:
        def error = thrown(WebClientResponseException)
        error.statusCode == HttpStatus.FORBIDDEN
        activitySuggestionRepository.count() == 0

        cleanup:
        deleteAll()
    }

    def 'admin cannot create suggestion'() {
        given:
        demoAdminLogin()

        when:
        def response = webClient.post()
                .uri('/suggestions/' + institution.id)
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .bodyValue(activitySuggestionDto)
                .retrieve()
                .bodyToMono(ActivitySuggestionDto.class)
                .block()

        then:
        def error = thrown(WebClientResponseException)
        error.statusCode == HttpStatus.FORBIDDEN
        activitySuggestionRepository.count() == 0

        cleanup:
        deleteAll()
    }

    def 'institution id doesnt exit'() {
        given:
        demoVolunteerLogin()

        when:
        def response = webClient.post()
                .uri('/suggestions/' + 222)
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .bodyValue(activitySuggestionDto)
                .retrieve()
                .bodyToMono(ActivitySuggestionDto.class)
                .block()

        then:
        def error = thrown(WebClientResponseException)
        error.statusCode == HttpStatus.BAD_REQUEST
        activitySuggestionRepository.count() == 0

        cleanup:
        deleteAll()
    }
}
