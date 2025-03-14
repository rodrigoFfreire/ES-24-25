package pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain

import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.dto.InstitutionProfileDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.domain.Assessment
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException
import static pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage.*

import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDate

class InstitutionProfileTest extends Specification {

    Institution institution = Mock()
    Assessment assessment1 = Mock()
    Assessment assessment2 = Mock()
    Assessment assessment3 = Mock()
    Assessment assessment4 = Mock()
    Assessment assessment5 = Mock()
    def institutionProfileDto

    def setup() {
        institution.getAssessments() >> [assessment1, assessment2, assessment3, assessment4, assessment5]
        institution.getActivities() >> []
        institution.getMembers() >> []

        assessment1.getReviewDate() >> LocalDate.now().minusDays(10).atStartOfDay()
        assessment2.getReviewDate() >> LocalDate.now().minusDays(20).atStartOfDay()
        assessment3.getReviewDate() >> LocalDate.now().minusDays(30).atStartOfDay()
        assessment4.getReviewDate() >> LocalDate.now().minusDays(40).atStartOfDay()
        assessment5.getReviewDate() >> LocalDate.now().minusDays(50).atStartOfDay()

        assessment1.getId() >> 1
        assessment2.getId() >> 2
        assessment3.getId() >> 3
        assessment4.getId() >> 4
        assessment5.getId() >> 5
    }

    def "create valid institution profile"() {
        given:
        institutionProfileDto = new InstitutionProfileDto(
            shortDescription: "Valid Description",
            numMembers: 0,
            numActivities: 0,
            numAssessments: 5,
            numVolunteers: 7,
            averageRating: 4.5,
            assessmentIds: [1, 2, 3, 4, 5]
        )

        when:
        def result = new InstitutionProfile(institution, institutionProfileDto)

        then:
        result.shortDescription == "Valid Description"
        result.numMembers == 0
        result.numActivities == 0
        result.numAssessments == 5
        result.numVolunteers == 7
        result.averageRating == 4.5
    }

    def "selected assessments invariant throws exception if less than 50% are selected"() {
        given:
        institution.getAssessments() >> [assessment1, assessment2, assessment3, assessment4, assessment5]
        institutionProfileDto = new InstitutionProfileDto(
            shortDescription: "Valid Description",
            numMembers: 0,
            numActivities: 5,
            numAssessments: 1,
            numVolunteers: 7,
            averageRating: 4.5,
            assessmentIds: [1] //Too few assessments, should fail
        )

        when:
        new InstitutionProfile(institution, institutionProfileDto)

        then:
        def error = thrown(HEException)
        error.errorMessage == INSUFFICIENT_SELECTED_ASSESSMENTS
    }

    def "recent assessments invariant throws exception if less than 20% are recent"() {
        given:
        assessment1.getReviewDate() >> LocalDate.now().minusDays(1)
        assessment2.getReviewDate() >> LocalDate.now().minusDays(30)
        assessment3.getReviewDate() >> LocalDate.now().minusDays(100)
        assessment4.getReviewDate() >> LocalDate.now().minusDays(200)
        assessment5.getReviewDate() >> LocalDate.now().minusDays(300)

        institution.getAssessments() >> [assessment1, assessment2, assessment3, assessment4, assessment5]

        institutionProfileDto = new InstitutionProfileDto(
            shortDescription: "Valid Description",
            numMembers: 10,
            numActivities: 0,
            numAssessments: 5,
            numVolunteers: 7,
            averageRating: 4.5,
            assessmentIds: [2, 3, 4, 5] //Less than 20% are recent
        )

        when:
        new InstitutionProfile(institution, institutionProfileDto)

        then:
        def error = thrown(HEException)
        error.errorMessage == INSUFFICIENT_RECENT_ASSESSMENTS
    }


}
