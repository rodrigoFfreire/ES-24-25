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
    
    def setup() {
        // Optional: Add any setup code here if needed
    }

    def "create institution profile successfully"() {
        given: "an institution and profile dto"
        def institution = createInstitution(INSTITUTION_1_NAME, INSTITUTION_1_EMAIL, INSTITUTION_1_NIF)

        // Create members
        def member1 = createMember(USER_1_NAME,USER_1_USERNAME,USER_1_PASSWORD,USER_1_EMAIL, AuthUser.Type.NORMAL, institution, User.State.APPROVED)
        def member2 = createMember(USER_2_NAME,USER_2_USERNAME,USER_2_PASSWORD,USER_2_EMAIL, AuthUser.Type.NORMAL, institution, User.State.APPROVED)
        def member3 = createMember(USER_3_NAME,USER_3_USERNAME,USER_3_PASSWORD,USER_3_EMAIL, AuthUser.Type.NORMAL, institution, User.State.APPROVED)

        def activityDto1 = createActivityDto(ACTIVITY_NAME_1,ACTIVITY_REGION_1,3,ACTIVITY_DESCRIPTION_1,
                TWO_DAYS_AGO, ONE_DAY_AGO, NOW,null)
        def activity1 = new Activity(activityDto1, institution, new ArrayList<>())
        activityRepository.save(activity1)
        
        def activityDto2 = createActivityDto(ACTIVITY_NAME_2,ACTIVITY_REGION_2,3,ACTIVITY_DESCRIPTION_2,
                TWO_DAYS_AGO, ONE_DAY_AGO, NOW,null)
        def activity2 = new Activity(activityDto2, institution, new ArrayList<>())
        activityRepository.save(activity2)
        
        // Create volunteers for the assessments
        def volunteer1 = createVolunteer(USER_1_NAME, USER_1_PASSWORD, USER_1_EMAIL, AuthUser.Type.NORMAL, User.State.APPROVED)
        def volunteer2 = createVolunteer(USER_2_NAME, USER_2_PASSWORD, USER_2_EMAIL, AuthUser.Type.NORMAL, User.State.APPROVED)
        
        // Create real assessments using the SpockTest createAssessment method
        def assessment1 = createAssessment(institution, volunteer1, ASSESSMENT_REVIEW_1)
        def assessment3 = createAssessment(institution, volunteer2, ASSESSMENT_REVIEW_2)
        
        def profileDto = new InstitutionProfileDto(shortDescription: "This is a test description.")
        
        when:
        def result = institutionProfileService.createInstitutionProfile(institution.getId(), profileDto)
        
        then: "profile is created successfully"
        institutionProfileRepository.count() == 1
        result.shortDescription == "This is a test description."
        result.numMembers == 3
        result.numActivities == 2
        result.numAssessments == 2
    }

    // Rest of your test methods stay the same
    
    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {
        def mockFactory = new DetachedMockFactory()

        @Bean
        Mailer mailer() {
            return mockFactory.Mock(Mailer)
        }
    }
}
