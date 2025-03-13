package pt.ulisboa.tecnico.socialsoftware.humanaethica.user.webservice

import org.springframework.http.HttpStatus
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.dto.ParticipationDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.dto.VolunteerProfileDto


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CreateVolunteerProfileWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    def volunteer
    def profileDto
    def part1
    def part2

    def setup() {
        deleteAll()

        webClient = WebClient.create("http://localhost:" + port)
        headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)

        def institution = institutionService.getDemoInstitution()
        def activityDto1 = createActivityDto(ACTIVITY_NAME_1, ACTIVITY_REGION_1, 3, ACTIVITY_DESCRIPTION_1,
                TWO_DAYS_AGO, ONE_DAY_AGO, NOW, null)
        def activityDto2 = createActivityDto(ACTIVITY_NAME_2, ACTIVITY_REGION_2, 3, ACTIVITY_DESCRIPTION_2,
                TWO_DAYS_AGO, ONE_DAY_AGO, NOW, null)

        def activity1 = new Activity(activityDto1, institution, new ArrayList<>())
        def activity2 = new Activity(activityDto2, institution, new ArrayList<>())
        activityRepository.save(activity1)
        activityRepository.save(activity2)

        def partDto1 = new ParticipationDto()
        partDto1.memberReview = MEMBER_REVIEW
        partDto1.memberRating = 4
        def partDto2 = new ParticipationDto()
        partDto2.memberReview = MEMBER_REVIEW
        partDto2.memberRating = 3

        volunteer = createVolunteer(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL, AuthUser.Type.NORMAL, User.State.APPROVED)
        part1 = createParticipation(activity1, volunteer, partDto1)
        part2 = createParticipation(activity2, volunteer, partDto2)

        profileDto = new VolunteerProfileDto()
        profileDto.setVolunteerShortBio(VOLUNTEER_PROFILE_EXAMPLE_BIO)
        profileDto.setChosenParticipations(new ArrayList<ParticipationDto>([new ParticipationDto(part1, User.Role.MEMBER)]))

    }

    def "volunteer creates a profile "(){
        given:
        def volunteer_login = demoVolunteerLogin()

        when:
        def response = webClient.post()
                .uri('/users/' + volunteer.getId() + '/profile')
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .bodyValue(profileDto)
                .retrieve()
                .bodyToMono(VolunteerProfileDto.class)
                .block()
        then:
        response.getVolunteerShortBio() == VOLUNTEER_PROFILE_EXAMPLE_BIO
        response.getChosenParticipations().size() == 1
        and:
        volunteerProfileRepository.findAll().size() == 1
        def storedProfile = volunteerProfileRepository.findAll().get(0)
        storedProfile.getShortBio() == VOLUNTEER_PROFILE_EXAMPLE_BIO
        storedProfile.getAverageRating() == 3.5
        storedProfile.getNumTotalParticipations() == 2
        storedProfile.getNumTotalAssessments() == 0
        storedProfile.getNumTotalEnrollments() == 0

        cleanup:
        deleteAll()
    }
    def "volunteer creates a profile with an error"(){

        given:
        def volunteer_login = demoVolunteerLogin()
        profileDto.setVolunteerShortBio("ola")

        when:
        def response = webClient.post()
                .uri('/users/' + volunteer.getId() + '/profile')
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .bodyValue(profileDto)
                .retrieve()
                .bodyToMono(VolunteerProfileDto.class)
                .block()


        then:
        def error = thrown(WebClientResponseException)
        error.statusCode == HttpStatus.BAD_REQUEST
        volunteerProfileRepository.count() == 0

        cleanup:
        deleteAll()
    }

    def "member cannot create a volunteer profile"(){

        given:
        demoMemberLogin()

        when:
        def response = webClient.post()
                .uri('/users/' + volunteer.getId() + '/profile')
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .bodyValue(profileDto)
                .retrieve()
                .bodyToMono(VolunteerProfileDto.class)
                .block()


        then:
        def error = thrown(WebClientResponseException)
        error.statusCode == HttpStatus.FORBIDDEN
        volunteerProfileRepository.count() == 0

        cleanup:
        deleteAll()
    }
    def "admin cannot create a volunteer profile"(){

        given:
        demoAdminLogin()

        when:
        def response = webClient.post()
                .uri('/users/' + volunteer.getId() + '/profile')
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .bodyValue(profileDto)
                .retrieve()
                .bodyToMono(VolunteerProfileDto.class)
                .block()


        then:
        def error = thrown(WebClientResponseException)
        error.statusCode == HttpStatus.FORBIDDEN
        volunteerProfileRepository.count() == 0

        cleanup:
        deleteAll()
    }
}