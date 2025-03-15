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
import spock.mock.DetachedMockFactory
import spock.lang.Unroll

@DataJpaTest
class GetInstitutionProfileTest extends SpockTest {
    @Autowired
    Mailer mailerMock
    
    Institution institution
    Member member1, member2, member3
    Activity activity1, activity2
    Assessment assessment1, assessment2, assessment3
    InstitutionProfile profile
    
    def setup() {
        institution = createInstitution(INSTITUTION_1_NAME, INSTITUTION_1_EMAIL, INSTITUTION_1_NIF)

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
        
        def volunteer1 = createVolunteer(USER_1_NAME, USER_1_PASSWORD, USER_1_EMAIL, AuthUser.Type.NORMAL, User.State.APPROVED)
        def volunteer2 = createVolunteer(USER_2_NAME, USER_2_PASSWORD, USER_2_EMAIL, AuthUser.Type.NORMAL, User.State.APPROVED)
        def volunteer3 = createVolunteer(USER_3_NAME, USER_3_PASSWORD, USER_3_EMAIL, AuthUser.Type.NORMAL, User.State.APPROVED)
        
        assessment1 = createAssessmentWithDate(institution, volunteer1, ASSESSMENT_REVIEW_1, NOW.minusDays(10))
        assessment2 = createAssessmentWithDate(institution, volunteer2, ASSESSMENT_REVIEW_2, NOW.minusDays(5))
        assessment3 = createAssessmentWithDate(institution, volunteer3, ASSESSMENT_REVIEW_2, NOW.minusDays(2))

        // Create profile for the institution
        def profileDto = new InstitutionProfileDto(
            shortDescription: "This is a test description for the institution profile.",
            assessmentIds: [assessment2.getId(), assessment3.getId()]
        )
        
        profile = new InstitutionProfile(institution, profileDto)
        institutionProfileRepository.save(profile)
    }

    def "get institution profile successfully"() {
        when:
        def result = institutionProfileService.getInstitutionProfile(institution.getId())
        
        then: "profile is retrieved successfully"
        result != null
        result.shortDescription == "This is a test description for the institution profile."
        result.numMembers == 3
        result.numActivities == 2
        result.numAssessments == 3
        
        and: "the correct assessments are returned"
        result.assessments.size() == 2
        result.assessments.collect { it.id }.containsAll([assessment2.getId(), assessment3.getId()])
    }
    
    def "fail to get profile when institution doesn't exist"() {
        when:
        institutionProfileService.getInstitutionProfile(9999)
        
        then:
        def exception = thrown(HEException)
        exception.getErrorMessage() == ErrorMessage.INSTITUTION_PROFILE_NOT_FOUND
    }
    
    def "fail to get profile when institution doesn't have a profile"() {
        given: "another institution without a profile"
        def otherInstitution = createInstitution("Other Institution", "other@institution.org", "111222333")
        
        when:
        institutionProfileService.getInstitutionProfile(otherInstitution.getId())
        
        then:
        def exception = thrown(HEException)
        exception.getErrorMessage() == ErrorMessage.INSTITUTION_PROFILE_NOT_FOUND
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
