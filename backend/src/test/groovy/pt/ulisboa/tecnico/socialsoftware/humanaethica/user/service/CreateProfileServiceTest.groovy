package pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.domain.Activity
import pt.ulisboa.tecnico.socialsoftware.humanaethica.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.dto.ParticipationDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.dto.VolunteerProfileDto
import spock.lang.Unroll

@DataJpaTest
class CreateProfileServiceTest extends SpockTest {
    def activity
    def volunteer
    def setup() {

        def institution = institutionService.getDemoInstitution()

        volunteer = createVolunteer(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL, AuthUser.Type.NORMAL, User.State.APPROVED)

        def activityDto = createActivityDto(ACTIVITY_NAME_1,ACTIVITY_REGION_1,3,ACTIVITY_DESCRIPTION_1,
                TWO_DAYS_AGO, ONE_DAY_AGO, NOW,null)

        activity = new Activity(activityDto, institution, new ArrayList<>())
        activityRepository.save(activity)

    }

    def 'create profile as volunteer' () {

        given:
        def participationDto = new ParticipationDto()
        participationDto.memberRating = 5
        participationDto.memberReview = MEMBER_REVIEW

        def participation1 = createParticipation(activity, volunteer, participationDto)

        def volunteerProfileDto = new VolunteerProfileDto()
        volunteerProfileDto.setVolunteerShortBio(VOLUNTEER_PROFILE_EXAMPLE_BIO)
        volunteerProfileDto.setChosenParticipations(new ArrayList<ParticipationDto>([new ParticipationDto(participation1, User.Role.MEMBER)]))

        when:
        def result = userProfileService.createVolunteerProfile(volunteer.getId(), volunteerProfileDto)

        then:
        result.shortBio == VOLUNTEER_PROFILE_EXAMPLE_BIO
        result.volunteerId == volunteer.getId()
        and:
        volunteerProfileRepository.findAll().size() == 1
        def storedProfile = volunteerProfileRepository.findAll().get(0)
        storedProfile.getShortBio() == VOLUNTEER_PROFILE_EXAMPLE_BIO
        storedProfile.getId() == volunteer.getId()
        storedProfile.getNumTotalParticipations() == 1
        storedProfile.getChosenParticipations()*.id.containsAll([participation1.id])
    }

    @Unroll
    def 'invalid arguments: volunteerId=#volunteerId  '() {

        given:
        def participationDto = new ParticipationDto()
        participationDto.memberRating = 5
        participationDto.memberReview = MEMBER_REVIEW
        def participation1 = createParticipation(activity, volunteer, participationDto)

        def volunteerProfileDto = new VolunteerProfileDto()

        volunteerProfileDto.setVolunteerShortBio(VOLUNTEER_PROFILE_EXAMPLE_BIO)
        volunteerProfileDto.setChosenParticipations(new ArrayList<ParticipationDto>([new ParticipationDto(participation1, User.Role.MEMBER)]))

        when:
        userProfileService.createVolunteerProfile(volunteerId, volunteerProfileDto)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == errorMessage
        and:
        volunteerProfileRepository.findAll().size() == 0

        where:
        volunteerId          || errorMessage
        111                  || ErrorMessage.USER_NOT_FOUND
        null                 || ErrorMessage.USER_NOT_FOUND
    }

    def "invalid profileDto"() {
        when:
        VolunteerProfileDto result = userProfileService.createVolunteerProfile(volunteer.getId(), null)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == ErrorMessage.VOLUNTEER_PROFILE_REQUIRES_INFORMATION
        and:
        volunteerProfileRepository.findAll().size() == 0
    }



    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
