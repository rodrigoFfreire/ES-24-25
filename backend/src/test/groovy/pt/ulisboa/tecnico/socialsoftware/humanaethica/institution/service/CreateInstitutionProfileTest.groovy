package pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.humanaethica.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.humanaethica.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain.Institution
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain.InstitutionProfile
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.dto.InstitutionProfileDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.Mailer
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Member
import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.domain.Assessment
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.theme.domain.Theme
import spock.mock.DetachedMockFactory
import spock.lang.Unroll

@DataJpaTest
class CreateInstitutionProfileTest extends SpockTest {
    @Autowired
    Mailer mailerMock
    
    Institution institution
    Institution otherInstitution
    Member member1, member2, member3
    Activity activity1, activity2, activity3
    Assessment assessment1, assessment2, assessment3, assessment4
    InstitutionProfileDto validProfileDto
    
    def setup() {
        institution = createInstitution(INSTITUTION_1_NAME, INSTITUTION_1_EMAIL, INSTITUTION_1_NIF)
        otherInstitution = createInstitution("Other Institution", "other@institution.org", "111222333")

        member1 = createMember(USER_1_NAME, USER_1_USERNAME, USER_1_PASSWORD, USER_1_EMAIL, 
                AuthUser.Type.NORMAL, institution, User.State.APPROVED)
        member2 = createMember(USER_2_NAME, USER_2_USERNAME, USER_2_PASSWORD, USER_2_EMAIL, 
                AuthUser.Type.NORMAL, institution, User.State.APPROVED)
        member3 = createMember(USER_3_NAME, USER_3_USERNAME, USER_3_PASSWORD, USER_3_EMAIL, 
                AuthUser.Type.NORMAL, institution, User.State.APPROVED)

        def activityDto1 = createActivityDto(ACTIVITY_NAME_1, ACTIVITY_REGION_1, 3, ACTIVITY_DESCRIPTION_1,
                TWO_DAYS_AGO, ONE_DAY_AGO, NOW, null)
        activity1 = new Activity(activityDto1, institution, new ArrayList<>())
        activityRepository.save(activity1)
        
        def activityDto2 = createActivityDto(ACTIVITY_NAME_2, ACTIVITY_REGION_2, 3, ACTIVITY_DESCRIPTION_2,
                TWO_DAYS_AGO, ONE_DAY_AGO, NOW, null)
        activity2 = new Activity(activityDto2, institution, new ArrayList<>())
        activityRepository.save(activity2)

        def activityDto3 = createActivityDto(ACTIVITY_NAME_3, ACTIVITY_REGION_2, 3, ACTIVITY_DESCRIPTION_2,
                TWO_DAYS_AGO, ONE_DAY_AGO, NOW, null)
        activity3 = new Activity(activityDto3, otherInstitution, new ArrayList<>())
        activityRepository.save(activity3)
        
        def volunteer1 = createVolunteer(USER_1_NAME, USER_1_PASSWORD, USER_1_EMAIL, AuthUser.Type.NORMAL, User.State.APPROVED)
        def volunteer2 = createVolunteer(USER_2_NAME, USER_2_PASSWORD, USER_2_EMAIL, AuthUser.Type.NORMAL, User.State.APPROVED)
        def volunteer3 = createVolunteer(USER_3_NAME, USER_3_PASSWORD, USER_3_EMAIL, AuthUser.Type.NORMAL, User.State.APPROVED)
        
        assessment1 = createAssessmentWithDate(institution, volunteer1, ASSESSMENT_REVIEW_1, NOW.minusDays(10))
        assessment2 = createAssessmentWithDate(institution, volunteer2, ASSESSMENT_REVIEW_2, NOW.minusDays(5))
        assessment3 = createAssessmentWithDate(institution, volunteer3, ASSESSMENT_REVIEW_2, NOW.minusDays(2))

        validProfileDto = new InstitutionProfileDto(
            shortDescription: "This is a test description for the institution profile.",
            assessmentIds: [assessment2.getId(), assessment3.getId()]
        )
    }

    def "create institution profile successfully"() {
        when:
        def result = institutionProfileService.createInstitutionProfile(institution.getId(), validProfileDto)
        
        then: "profile is created successfully"
        institutionProfileRepository.count() == 1
        result.shortDescription == "This is a test description for the institution profile."
        result.numMembers == 3
        result.numActivities == 2
        result.numAssessments == 2
        
        and: "the selected assessments are stored"
        def storedProfile = institutionProfileRepository.findAll().get(0)
        storedProfile.getAssessments().size() == 2
        storedProfile.getAssessments().contains(assessment2)
        storedProfile.getAssessments().contains(assessment3)
    }
    
    def "fail to create profile when institution doesn't exist"() {
        when:
        institutionProfileService.createInstitutionProfile(9999, validProfileDto)
        
        then:
        def exception = thrown(HEException)
        exception.getErrorMessage() == ErrorMessage.INSTITUTION_NOT_FOUND
    }
    
    def "fail to create profile when description is too short"() {
        given: "a profile DTO with a short description"
        def shortDescDto = new InstitutionProfileDto(
            shortDescription: "Too short",
            assessmentIds: [assessment1.getId(), assessment2.getId(), assessment3.getId()]
        )
        
        when:
        institutionProfileService.createInstitutionProfile(institution.getId(), shortDescDto)
        
        then:
        def exception = thrown(HEException)
        exception.getErrorMessage() == ErrorMessage.INVALID_DESCRIPTION_LENGTH
    }
    
    def "fail to create profile when less than 50% of assessments are selected"() {
        given: "a profile DTO with only one assessment selected"
        def fewAssessmentsDto = new InstitutionProfileDto(
            shortDescription: "This is a test description for the institution profile.",
            assessmentIds: [assessment1.getId()]
        )
        
        when:
        institutionProfileService.createInstitutionProfile(institution.getId(), fewAssessmentsDto)
        
        then:
        def exception = thrown(HEException)
        exception.getErrorMessage() == ErrorMessage.INSUFFICIENT_SELECTED_ASSESSMENTS
    }
    
    def "fail to create profile when less than 20% of recent assessments are selected"() {
        given: "a profile DTO with no recent assessments selected"
        def noRecentAssessmentsDto = new InstitutionProfileDto(
            shortDescription: "This is a test description for the institution profile.",
            assessmentIds: [assessment1.getId(), assessment2.getId()]
        )
        
        when:
        institutionProfileService.createInstitutionProfile(institution.getId(), noRecentAssessmentsDto)
        
        then:
        def exception = thrown(HEException)
        exception.getErrorMessage() == ErrorMessage.INSUFFICIENT_RECENT_ASSESSMENTS
    }
    
    def "fail to create profile when assessment IDs don't exist"() {
        given: "a profile DTO with non-existent assessment IDs"
        def invalidAssessmentsDto = new InstitutionProfileDto(
            shortDescription: "This is a test description for the institution profile.",
            assessmentIds: [9999, 8888]
        )
        
        when:
        institutionProfileService.createInstitutionProfile(institution.getId(), invalidAssessmentsDto)
        
        then:
        def exception = thrown(HEException)
        exception.getErrorMessage() == ErrorMessage.ASSESSMENT_NOT_FROM_INSTITUTION
    }
    
    def "fail to create profile when assessments don't belong to the institution"() {
        given: "an assessment for another institution"
        def otherVolunteer = createVolunteer("Other User", "example password", "superexample@email.com", AuthUser.Type.NORMAL, User.State.APPROVED)
        def otherAssessment = createAssessment(otherInstitution, otherVolunteer, ASSESSMENT_REVIEW_1)
        
        and: "a profile DTO with that assessment"
        def wrongInstitutionDto = new InstitutionProfileDto(
            shortDescription: "This is a test description for the institution profile.",
            assessmentIds: [assessment1.getId(), assessment2.getId(), otherAssessment.getId()]
        )
        
        when:
        institutionProfileService.createInstitutionProfile(institution.getId(), wrongInstitutionDto)
        
        then:
        def exception = thrown(HEException)
        exception.getErrorMessage() == ErrorMessage.ASSESSMENT_NOT_FROM_INSTITUTION
    }
    
    def "fail to create profile when institution already has a profile"() {
        given: "an institution that already has a profile"
        institutionProfileService.createInstitutionProfile(institution.getId(), validProfileDto)
        
        when: "attempting to create another profile"
        institutionProfileService.createInstitutionProfile(institution.getId(), validProfileDto)
        
        then:
        def exception = thrown(HEException)
        exception.getErrorMessage() == ErrorMessage.INSTITUTION_PROFILE_ALREADY_EXISTS
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {
        def mockFactory = new DetachedMockFactory()

        @Bean
        Mailer mailer() {
            return mockFactory.Mock(Mailer)
        }
    }
}
