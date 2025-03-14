package pt.ulisboa.tecnico.socialsoftware.humanaethica.user.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.dto.ParticipationDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.VolunteerProfile
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.dto.VolunteerProfileDto
import spock.lang.Unroll

@DataJpaTest
class GetVolunteerProfileTest extends SpockTest {
    def volunteer
    def institution

    def setup() {
        institution = institutionService.getDemoInstitution()
        def activityDto1 = createActivityDto(ACTIVITY_NAME_1, ACTIVITY_REGION_1, 3, ACTIVITY_DESCRIPTION_1,
                TWO_DAYS_AGO, ONE_DAY_AGO, NOW, null)
        def activityDto2 = createActivityDto(ACTIVITY_NAME_2, ACTIVITY_REGION_2, 3, ACTIVITY_DESCRIPTION_2,
                TWO_DAYS_AGO, ONE_DAY_AGO, NOW, null)

        def activity1 = new Activity(activityDto1, institution, new ArrayList<>())
        def activity2 = new Activity(activityDto2, institution, new ArrayList<>())

        def partDto1 = new ParticipationDto()
        partDto1.memberReview = MEMBER_REVIEW
        partDto1.memberRating = 4
        def partDto2 = new ParticipationDto()
        partDto2.memberReview = MEMBER_REVIEW
        partDto2.memberRating = 3

        // Should have ID 2
        volunteer = createVolunteer(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL, AuthUser.Type.NORMAL, User.State.APPROVED)
        def part1 = createParticipation(activity1, volunteer, partDto1)
        createParticipation(activity2, volunteer, partDto2)

        def profileDto = new VolunteerProfileDto()
        profileDto.setVolunteerShortBio(VOLUNTEER_PROFILE_EXAMPLE_BIO)
        profileDto.setChosenParticipations(new ArrayList<ParticipationDto>([new ParticipationDto(part1, User.Role.MEMBER)]))
        volunteerProfileRepository.save(new VolunteerProfile(volunteer, profileDto))
    }


    def "get a volunteer profile sucessfully"() {
        given:
        def volunterId = volunteer.getId()

        when:
        def profile = userProfileService.getVolunteerProfile(volunterId)

        then:
        profile.getId() == 1
        profile.getVolunteerId() == volunteer.getId()
        profile.getAverageRating() == 3.5
        profile.getVolunteerShortBio() == VOLUNTEER_PROFILE_EXAMPLE_BIO
        profile.getNumTotalParticipations() == 2
        profile.getChosenParticipations().size() == 1
        profile.getNumTotalAssessments() == 0
        profile.getNumTotalEnrollments() == 0
    }

    @Unroll
    def "invalid arguments: volunteerId=#volunteerId"() {
        when:
        userProfileService.getVolunteerProfile(volunteerId)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == errorMessage

        where:
        volunteerId || errorMessage
        111         || ErrorMessage.USER_NOT_FOUND
        null        || ErrorMessage.USER_NOT_FOUND
    }

    def "invalid arguments: User is not a volunteer"() {
        given:
        def member = createMember(USER_2_NAME, USER_2_USERNAME, "password", USER_2_EMAIL, AuthUser.Type.NORMAL, institution, User.State.APPROVED)
        userRepository.save(member)

        when:
        userProfileService.getVolunteerProfile(member.getId())

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == ErrorMessage.USER_NOT_VOLUNTEER
    }

    def "invalid arguments: User volunteer has not setup a profile"() {
        given:
        def volunteer_no_profile = createVolunteer(USER_3_NAME, USER_3_USERNAME, USER_3_EMAIL, AuthUser.Type.NORMAL, User.State.APPROVED)
        userRepository.save(volunteer_no_profile)

        when:
        userProfileService.getVolunteerProfile(volunteer_no_profile.getId())

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == ErrorMessage.VOLUNTEER_PROFILE_NOT_FOUND
    }


    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}