package pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.webservice
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.dto.InstitutionProfileDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain.Institution
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Member
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.humanaethica.auth.domain.AuthUser

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CreateInstitutionProfileWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port
    def institution
    def institutionProfileDto
    def member
    
    def setup() {
        deleteAll()
        webClient = WebClient.create("http://localhost:" + port)
        headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        
        // Create institution
        institution = createInstitution("Test Institution", "test@institution.com", "123456789")
        
        // Create a member that belongs to this institution
        member = createMember(USER_1_NAME, USER_1_USERNAME, USER_1_PASSWORD, USER_1_EMAIL, 
                AuthUser.Type.NORMAL, institution, User.State.APPROVED)
        
        // Create institution profile DTO
        institutionProfileDto = new InstitutionProfileDto(
                shortDescription: "Valid Description",
        )
    }
    
    def 'member creates institution profile successfully'() {
        given:
        // Login with the created member instead of using demoMemberLogin
        normalUserLogin(USER_1_USERNAME, USER_1_PASSWORD)
        
        when:
        def response = webClient.post()
                .uri('/institution-profile/' + institution.id)
                .headers(httpHeaders -> httpHeaders.putAll(headers))
                .bodyValue(institutionProfileDto)
                .retrieve()
                .bodyToMono(InstitutionProfileDto.class)
                .block()
        
        then:
        response.shortDescription == institutionProfileDto.shortDescription
        response.numMembers == 1
        response.numActivities == 0
        response.numAssessments == 0
        response.numVolunteers == 0
        response.averageRating == 0
        
        and:
        institutionProfileRepository.count() == 1
        def storedProfile = institutionProfileRepository.findAll().get(0)
        storedProfile.shortDescription == institutionProfileDto.shortDescription
        storedProfile.numMembers == 1
        storedProfile.numActivities == 0
        storedProfile.numAssessments == 0
        storedProfile.numVolunteers == 0
        storedProfile.averageRating == 0
        
        cleanup:
        deleteAll()
    }

    def 'unauthorized user creates institution profile'() {
        when:
        webClient.post()
            .uri('/institution-profile/' + institution.id)
            .headers(httpHeaders -> httpHeaders.putAll(headers))
            .bodyValue(institutionProfileDto)
            .retrieve()
            .bodyToMono(InstitutionProfileDto.class)
            .block()
        
        then:
        def exception = thrown(WebClientResponseException)
        exception.statusCode == HttpStatus.FORBIDDEN
    }
}
