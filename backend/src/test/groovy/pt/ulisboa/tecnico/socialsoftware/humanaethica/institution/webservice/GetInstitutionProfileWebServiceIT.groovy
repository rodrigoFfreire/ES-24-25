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
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain.InstitutionProfile
import pt.ulisboa.tecnico.socialsoftware.humanaethica.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GetInstitutionProfileWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port
    
    private Institution institution
    private InstitutionProfile profile
    private Activity activity1, activity2
    
    def setup() {
        deleteAll()
        
        webClient = WebClient.create("http://localhost:" + port)
        headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        
        // Create institution
        institution = createInstitution(INSTITUTION_1_NAME, INSTITUTION_1_EMAIL, INSTITUTION_1_NIF)
        
        // Create member for the institution
        createMember(USER_1_NAME, USER_1_USERNAME, USER_1_PASSWORD, USER_1_EMAIL, 
                AuthUser.Type.DEMO, institution, User.State.APPROVED)
        
        // Create additional members
        createMember(USER_2_NAME, USER_2_USERNAME, USER_2_PASSWORD, USER_2_EMAIL, 
                AuthUser.Type.NORMAL, institution, User.State.APPROVED)
        createMember(USER_3_NAME, USER_3_USERNAME, USER_3_PASSWORD, USER_3_EMAIL, 
                AuthUser.Type.NORMAL, institution, User.State.APPROVED)
        
        // Create activities
        def activityDto1 = createActivityDto(ACTIVITY_NAME_1, ACTIVITY_REGION_1, 3, ACTIVITY_DESCRIPTION_1,
                TWO_DAYS_AGO, ONE_DAY_AGO, NOW, null)
        activity1 = new Activity(activityDto1, institution, new ArrayList<>())
        activityRepository.save(activity1)
        
        def activityDto2 = createActivityDto(ACTIVITY_NAME_2, ACTIVITY_REGION_2, 3, ACTIVITY_DESCRIPTION_2,
                TWO_DAYS_AGO, ONE_DAY_AGO, NOW, null)
        activity2 = new Activity(activityDto2, institution, new ArrayList<>())
        activityRepository.save(activity2)
        
        // Create institution profile
        def profileDto = new InstitutionProfileDto()
        profileDto.setShortDescription("This is a test description for the institution profile.")
        
        profile = new InstitutionProfile(institution, profileDto)
        institutionProfileRepository.save(profile)
    }
    
    def 'non authenticated user gets institution profile'() {
        when:
        def response = webClient.get()
            .uri('/institution-profile/' + institution.id)
            .headers(httpHeaders -> httpHeaders.putAll(headers))
            .retrieve()
            .bodyToMono(InstitutionProfileDto.class)
            .block()
            
        then: "profile is retrieved successfully"
        response != null
        response.shortDescription == "This is a test description for the institution profile."
        response.numMembers == 3  // We created 3 members
        response.numActivities == 2
        response.numAssessments == 0
        
        and: "no assessments are returned"
        response.assessments.size() == 0
    }
    
    def 'authenticated user gets institution profile'() {
        given: "authenticated user"
        demoMemberLogin()
        
        when:
        def response = webClient.get()
            .uri('/institution-profile/' + institution.id)
            .headers(httpHeaders -> httpHeaders.putAll(headers))
            .retrieve()
            .bodyToMono(InstitutionProfileDto.class)
            .block()
            
        then: "profile is retrieved successfully"
        response != null
        response.shortDescription == "This is a test description for the institution profile."
        response.numMembers == 3
        response.numActivities == 2
        response.numAssessments == 0
        
        and: "no assessments are returned"
        response.assessments.size() == 0
    }
    
    def 'admin gets institution profile'() {
        given: "admin login"
        demoAdminLogin()
        
        when:
        def response = webClient.get()
            .uri('/institution-profile/' + institution.id)
            .headers(httpHeaders -> httpHeaders.putAll(headers))
            .retrieve()
            .bodyToMono(InstitutionProfileDto.class)
            .block()
            
        then: "profile is retrieved successfully"
        response != null
        response.shortDescription == "This is a test description for the institution profile."
        response.numMembers == 3
        response.numActivities == 2
        response.numAssessments == 0
        
        and: "no assessments are returned"
        response.assessments.size() == 0
    }
    
    def 'institution does not exist'() {
        when:
        webClient.get()
            .uri('/institution-profile/9999')
            .headers(httpHeaders -> httpHeaders.putAll(headers))
            .retrieve()
            .bodyToMono(InstitutionProfileDto.class)
            .block()
            
        then:
        def error = thrown(WebClientResponseException)
        error.statusCode == HttpStatus.BAD_REQUEST
    }
    
    def 'institution does not have a profile'() {
        given: "another institution without a profile"
        def otherInstitution = createInstitution("Other Institution", "other@institution.org", "111222333")
        
        when:
        webClient.get()
            .uri('/institution-profile/' + otherInstitution.id)
            .headers(httpHeaders -> httpHeaders.putAll(headers))
            .retrieve()
            .bodyToMono(InstitutionProfileDto.class)
            .block()
            
        then:
        def error = thrown(WebClientResponseException)
        error.statusCode == HttpStatus.BAD_REQUEST
    }
}
