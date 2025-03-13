package pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.domain.Assessment
import pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.domain.Enrollment
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.domain.Participation
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.dto.ParticipationDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.dto.VolunteerProfileDto

@DataJpaTest
class CreateVolunteerProfileTest extends SpockTest {
    Volunteer volunteer = Mock()
    def participation1 = Mock(Participation)
    def participation2 = Mock(Participation)

    def "create volunteer profile with 1 selected participation and correct bio"() {
        given:
        def exampleParticipationDto = new ParticipationDto()
        exampleParticipationDto.id = 1
        participation1.getId() >> 1
        participation1.getMemberRating() >> 3
        participation2.getId() >> 2
        participation2.getMemberRating() >> 4

        volunteer.getParticipations() >> [participation1, participation2]
        volunteer.getAssessments() >> [new Assessment(), new Assessment(), new Assessment()]
        volunteer.getEnrollments() >> [new Enrollment(), new Enrollment()]

        def profileDto = new VolunteerProfileDto()
        profileDto.setVolunteerShortBio(VOLUNTEER_PROFILE_EXAMPLE_BIO)
        profileDto.setChosenParticipations(new ArrayList<ParticipationDto>([exampleParticipationDto]))

        when:
        def result = new VolunteerProfile(volunteer, profileDto)

        then:
        result.getShortBio() == VOLUNTEER_PROFILE_EXAMPLE_BIO
        result.getAverageRating() == 3.5
        result.getChosenParticipations().size() == 1
        result.getNumTotalParticipations() == 2
        result.getNumTotalAssessments() == 3
        result.getNumTotalEnrollments() == 2
        result.getVolunteer() == volunteer

        and:
        1 * volunteer.setProfile(_)
        1 * participation1.setVolunteerProfile(_)
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}