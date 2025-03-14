package pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain

import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.dto.InstitutionProfileDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.domain.Assessment
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException
import static pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage.*

import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDate
import java.time.LocalDateTime

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
        result.assessments.size() == 5
    }

    def "fail if institution is null"() {
        given:
        institutionProfileDto = new InstitutionProfileDto(
            shortDescription: "Valid Description",
            assessmentIds: [1, 2, 3]
        )

        when:
        new InstitutionProfile(null, institutionProfileDto)

        then:
        def error = thrown(HEException)
        error.errorMessage == INSTITUTION_NOT_FOUND
    }

    @Unroll
    def "description length validation: #desc - should #result"() {
        given:
        institutionProfileDto = new InstitutionProfileDto(
            shortDescription: desc,
            numMembers: 0,
            numActivities: 0,
            numAssessments: 5,
            numVolunteers: 7,
            averageRating: 4.5,
            assessmentIds: [1, 2, 3, 4, 5]
        )

        when:
        def profile = null
        def thrown = false
        
        try {
            profile = new InstitutionProfile(institution, institutionProfileDto)
        } catch (HEException e) {
            thrown = e.errorMessage == INVALID_DESCRIPTION_LENGTH
        }

        then:
        thrown == shouldThrow

        where:
        desc                            | shouldThrow | result
        null                            | true        | "fail"
        ""                              | true        | "fail"
        "Short"                         | true        | "fail"
        "Ten chars.."                   | false       | "pass"
        "Exactly ten"                   | false       | "pass"
        "This is a long enough description for the profile" | false | "pass"
    }

    @Unroll
    def "selected assessments validation: #selectedCount of #totalCount assessments - should #result"() {
        given:
        def mockInstitution = Mock(Institution)
        def assessments = []
        def selectedIds = []
        
        // Create the specified number of mock assessments
        for (int i = 1; i <= totalCount; i++) {
            def assessment = Mock(Assessment)
            assessment.getId() >> i
            assessment.getReviewDate() >> LocalDate.now().minusDays(i).atStartOfDay()
            assessments.add(assessment)
            
            // Add to selected IDs based on the requested count
            if (i <= selectedCount) {
                selectedIds.add(i)
            }
        }
        
        mockInstitution.getAssessments() >> assessments
        mockInstitution.getActivities() >> []
        mockInstitution.getMembers() >> []
        
        institutionProfileDto = new InstitutionProfileDto(
            shortDescription: "Valid Description",
            numMembers: 0,
            numActivities: 0,
            numAssessments: totalCount,
            numVolunteers: 7,
            averageRating: 4.5,
            assessmentIds: selectedIds
        )

        when:
        def profile = null
        def thrown = false
        
        try {
            profile = new InstitutionProfile(mockInstitution, institutionProfileDto)
        } catch (HEException e) {
            thrown = e.errorMessage == INSUFFICIENT_SELECTED_ASSESSMENTS
        }

        then:
        thrown == shouldThrow

        where:
        selectedCount | totalCount | shouldThrow | result
        2             | 5          | true        | "fail"  // 40% < 50%
        2             | 6          | true        | "fail"  // 50% - 1
        3             | 5          | false       | "pass"  // Exactly 60%
        3             | 6          | false       | "pass"  // Exactly 50%
        4             | 6          | false       | "pass"  // Just over 50%
        5             | 5          | false       | "pass"  // 100%
    }
    
    @Unroll
    def "recent assessments validation: #recentSelected of #recentTotal recent assessments - should #result"() {
        given:
        def mockInstitution = Mock(Institution)
        def assessments = []
        def allIds = []
        def selectedIds = []
        
        // Create 10 mock assessments with different dates
        for (int i = 1; i <= 10; i++) {
            def assessment = Mock(Assessment)
            assessment.getId() >> i
            
            // Make the first recentTotal assessments recent (sorted by date descending)
            if (i <= recentTotal) {
                assessment.getReviewDate() >> LocalDate.now().minusDays(i).atStartOfDay()
            } else {
                assessment.getReviewDate() >> LocalDate.now().minusDays(30 + i).atStartOfDay()
            }
            
            assessments.add(assessment)
            allIds.add(i)
        }
        
        // Select all non-recent assessments
        for (int i = recentTotal + 1; i <= 10; i++) {
            selectedIds.add(i)
        }
        
        // Add the specified number of recent assessments
        for (int i = 1; i <= recentSelected; i++) {
            selectedIds.add(i)
        }
        
        mockInstitution.getAssessments() >> assessments
        mockInstitution.getActivities() >> []
        mockInstitution.getMembers() >> []
        
        institutionProfileDto = new InstitutionProfileDto(
            shortDescription: "Valid Description",
            numMembers: 0,
            numActivities: 0,
            numAssessments: 10,
            numVolunteers: 7,
            averageRating: 4.5,
            assessmentIds: selectedIds
        )

        when:
        def profile = null
        def thrown = false
        
        try {
            profile = new InstitutionProfile(mockInstitution, institutionProfileDto)
        } catch (HEException e) {
            thrown = e.errorMessage == INSUFFICIENT_RECENT_ASSESSMENTS
        }

        then:
        thrown == shouldThrow

        where:
        recentSelected | recentTotal | shouldThrow | result
        0             | 3           | true        | "fail"  // 0% recent
        1             | 3           | true        | "fail"  // One under threshold
        1             | 5           | true        | "fail"  // 20% of recent required 
        2             | 3           | false       | "pass"  // 66% of recent required
        3             | 3           | false       | "pass"  // 100% of recent
    }

    def "selectAssessments validates that assessments belong to institution"() {
        given:
        def invalidId = 999
        institutionProfileDto = new InstitutionProfileDto(
            shortDescription: "Valid Description",
            assessmentIds: [1, 2, invalidId] // Invalid ID not in institution
        )

        when:
        new InstitutionProfile(institution, institutionProfileDto)

        then:
        def error = thrown(HEException)
        error.errorMessage == ASSESSMENT_NOT_FROM_INSTITUTION
    }

    def "setInstitution establishes bidirectional relationship"() {
        given:
        institutionProfileDto = new InstitutionProfileDto(
            shortDescription: "Valid Description",
            assessmentIds: [1, 2, 3, 4, 5]
        )
        def profile = new InstitutionProfile()

        when:
        profile.setInstitution(institution)

        then:
        1 * institution.setProfile(profile)
        profile.getInstitution() == institution
    }

    def "getters and setters work correctly"() {
        given:
        def profile = new InstitutionProfile()
        
        when:
        profile.setShortDescription("Test Description")
        profile.setNumActivities(5)
        profile.setNumMembers(10)
        profile.setNumAssessments(15)
        profile.setNumVolunteers(20)
        profile.setAverageRating(4.7f)
        
        then:
        profile.getShortDescription() == "Test Description"
        profile.getNumActivities() == 5
        profile.getNumMembers() == 10
        profile.getNumAssessments() == 15
        profile.getNumVolunteers() == 20
        profile.getAverageRating() == 4.7f
    }
}
