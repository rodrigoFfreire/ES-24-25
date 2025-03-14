package pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.humanaethica.SpockTest
import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.domain.Assessment
import pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.domain.Enrollment
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.domain.Participation
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.dto.ParticipationDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.dto.VolunteerProfileDto
import spock.lang.Unroll

@DataJpaTest
class CreateVolunteerProfileTest extends SpockTest {
    Volunteer volunteer = Mock()
    def participation1 = Mock(Participation)
    def participation2 = Mock(Participation)
    def participation3 = Mock(Participation)
    def participation4 = Mock(Participation)
    def participation5 = Mock(Participation)

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

    @Unroll
    def "create profile and violate shortbio >= 10 is required invariant"() {
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
        profileDto.setVolunteerShortBio(EXAMPLE_BIO)
        profileDto.setChosenParticipations(new ArrayList<ParticipationDto>([exampleParticipationDto]))

        when:
        new VolunteerProfile(volunteer, profileDto)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == errorMessage

        where:
        EXAMPLE_BIO       || errorMessage
        null              || ErrorMessage.PROFILE_REQUIRES_SHORTBIO
        "          "      || ErrorMessage.PROFILE_REQUIRES_SHORTBIO
        "lllllllll"       || ErrorMessage.PROFILE_REQUIRES_SHORTBIO
    }
    @Unroll
    def "create profile and violate participation evaluated invariant"() {
        given:
        def exampleParticipationDto = new ParticipationDto()
        exampleParticipationDto.id = 1
        participation1.getId() >> 1
        participation1.getMemberRating() >> memberRating
        participation1.getId() >> 2
        participation2.getMemberRating() >> 4

        volunteer.getParticipations() >> [participation1, participation2]
        volunteer.getAssessments() >> [new Assessment(), new Assessment(), new Assessment()]
        volunteer.getEnrollments() >> [new Enrollment(), new Enrollment()]

        def profileDto = new VolunteerProfileDto()
        profileDto.setVolunteerShortBio(VOLUNTEER_PROFILE_EXAMPLE_BIO)
        profileDto.setChosenParticipations(new ArrayList<ParticipationDto>([exampleParticipationDto]))

        when:
        new VolunteerProfile(volunteer, profileDto)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == errorMessage

        where:
        memberRating    || errorMessage
        null            || ErrorMessage.PROFILE_REQUIRES_PARTICIPATION_EVALUATED
    }

    @Unroll
    def "create profile and violate number of chosen participations"() {
        given:
        participation1.getId() >> 1
        participation1.getMemberRating() >> 4
        participation2.getId() >> 2
        participation2.getMemberRating() >> 4
        participation3.getId() >> 3
        participation3.getMemberRating() >> 4
        participation4.getId() >> 4
        participation4.getMemberRating() >> 5
        participation5.getId() >> 4
        participation5.getMemberRating() >> 5

        volunteer.getParticipations() >> [participation1, participation2, participation3, participation4, participation5]
        volunteer.getAssessments() >> [new Assessment(), new Assessment(), new Assessment()]
        volunteer.getEnrollments() >> [new Enrollment(), new Enrollment()]

        def profileDto = new VolunteerProfileDto()
        profileDto.setVolunteerShortBio(VOLUNTEER_PROFILE_EXAMPLE_BIO)

        profileDto.chosenParticipations = chosenParticipationIds.collect { id ->
            // Create the ParticipationDto based on the chosen id
            def participationDto = new ParticipationDto()
            participationDto.id = id
            return participationDto
        }

        when:
        new VolunteerProfile(volunteer, profileDto)

        then:
        def error = thrown(HEException)
        error.getErrorMessage() == ErrorMessage.PROFILE_REQUIRES_VALID_NUMBER_PARTICIPATIONS

        where:
        chosenParticipationIds      || errorMessage
        []                          || ErrorMessage.PROFILE_REQUIRES_VALID_NUMBER_PARTICIPATIONS
        [1]                         || ErrorMessage.PROFILE_REQUIRES_VALID_NUMBER_PARTICIPATIONS
        [3,5]                       || ErrorMessage.PROFILE_REQUIRES_VALID_NUMBER_PARTICIPATIONS
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}