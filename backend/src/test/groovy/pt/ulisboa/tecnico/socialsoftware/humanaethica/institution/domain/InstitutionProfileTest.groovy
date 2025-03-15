package pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain

import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.dto.InstitutionProfileDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.domain.Assessment
import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.dto.AssessmentDto
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer
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

    def "InstitutionProfileDto empty constructor initializes fields to defaults"() {
        when:
        def dto = new InstitutionProfileDto()

        then:
        dto.id == null
        dto.shortDescription == null
        dto.numMembers == 0
        dto.numActivities == 0
        dto.numAssessments == 0
        dto.numVolunteers == 0
        dto.averageRating == 0.0f
        dto.assessmentIds == []
        dto.assessments == []
    }

    def "InstitutionProfileDto constructor with params initializes shortDescription and assessmentIds"() {
        given:
        def description = "Test Desc"
        def ids = [1, 2, 3]

        when:
        def dto = new InstitutionProfileDto(description, ids)

        then:
        dto.shortDescription == description
        dto.assessmentIds == ids
        dto.numMembers == 0
        dto.assessments == []
    }

    def "InstitutionProfileDto constructor maps assessments to IDs and DTOs"() {
        given:
        // Create a mock Institution with ID
        def institution = Mock(Institution)
        institution.getId() >> 123
        
        // Create mock Assessment with Institution association
        def assessment = Mock(Assessment)
        assessment.getId() >> 5
        assessment.getInstitution() >> institution
        assessment.getVolunteer() >> Mock(Volunteer)
        
        // Setup profile mock
        def profile = Mock(InstitutionProfile)
        profile.getAssessments() >> [assessment]

        when:
        def dto = new InstitutionProfileDto(profile)

        then:
        dto.assessmentIds == [5]
        dto.assessments.size() == 1
        dto.assessments[0].class == AssessmentDto
    }

    def "InstitutionProfileDto constructor handles null assessments gracefully"() {
        given:
        def profile = Mock(InstitutionProfile)
        profile.getAssessments() >> null

        when:
        def dto = new InstitutionProfileDto(profile)

        then:
        dto.assessmentIds == []
        dto.assessments == []
    }

    def "Setters modify all DTO fields correctly"() {
        given:
        def dto = new InstitutionProfileDto()
        def assessments = [new AssessmentDto()]

        when:
        dto.setId(1)
        dto.setShortDescription("Desc")
        dto.setNumMembers(2)
        dto.setNumActivities(3)
        dto.setNumAssessments(4)
        dto.setNumVolunteers(5)
        dto.setAverageRating(4.2f)
        dto.setAssessmentIds([10, 20])
        dto.setAssessments(assessments)

        then:
        dto.id == 1
        dto.shortDescription == "Desc"
        dto.numMembers == 2
        dto.numActivities == 3
        dto.numAssessments == 4
        dto.numVolunteers == 5
        dto.averageRating == 4.2f
        dto.assessmentIds == [10, 20]
        dto.assessments == assessments
    }
    def "InstitutionProfile constructor with Institution and DTO sets basic fields"() {
        given:
        def institution = Mock(Institution) {
            getActivities() >> []
            getMembers() >> []
            getAssessments() >> []
        }
        def dto = new InstitutionProfileDto(
            shortDescription: "Test Description", // Changed to 13 characters
            numVolunteers: 4,
            averageRating: 4.5f,
            assessmentIds: []
        )

        when:
        def profile = new InstitutionProfile(institution, dto)

        then:
        profile.shortDescription == "Test Description"
        profile.numVolunteers == 4
        profile.averageRating == 4.5f
        profile.numActivities == 0  // Because activities list is empty
        profile.numMembers == 0     // Because members list is empty
    }

    def "selectAssessments filters assessments by institution"() {
        given:
        def institution = Mock(Institution)
        def profile = new InstitutionProfile()
        def assessment1 = Mock(Assessment) { getId() >> 1 }
        def assessment2 = Mock(Assessment) { getId() >> 2 }
        assessment1.getInstitution() >> institution
        assessment2.getInstitution() >> institution
        institution.getAssessments() >> [assessment1, assessment2]
        profile.setInstitution(institution)

        when:
        profile.selectAssessments(institution, [1])

        then:
        profile.assessments == [assessment1]
    }

    def "deleteAssessment removes assessment from profile"() {
        given:
        def profile = new InstitutionProfile()
        def assessment = Mock(Assessment)
        profile.assessments = [assessment]

        when:
        profile.deleteAssessment(assessment)

        then:
        profile.assessments.isEmpty()
    }

    def "getId returns profile ID"() {
        given:
        def profile = new InstitutionProfile()
        profile.id = 123

        when:
        def result = profile.getId()

        then:
        result == 123
    }

    def "selectAssessments handles null assessmentIds by clearing assessments"() {
        given:
        def institution = Mock(Institution) {
            getAssessments() >> [Mock(Assessment), Mock(Assessment)]
        }
        def profile = new InstitutionProfile()
        profile.setInstitution(institution)

        when:
        profile.selectAssessments(institution, null) // Trigger null branch

        then:
        profile.assessments.isEmpty()
    }

    def "raise INSTITUTION_PROFILE_ALREADY_EXISTS when creating a profile for an institution that already has one"() {
        given:
        def institutionProfileDto = new InstitutionProfileDto(
            shortDescription: "Valid Description",
            numMembers: 0,
            numActivities: 0,
            numAssessments: 5,
            numVolunteers: 7,
            averageRating: 4.5,
            assessmentIds: [1, 2, 3, 4, 5]
        )
        def exampleInstitution = Mock(Institution)
        def otherProfile = Mock(InstitutionProfile)
        exampleInstitution.getProfile() >> otherProfile
        exampleInstitution.getId() >> 1
        when:
        def result = new InstitutionProfile(exampleInstitution, institutionProfileDto)
        then:
        def exception = thrown(HEException)
        exception.getErrorMessage() == INSTITUTION_PROFILE_ALREADY_EXISTS
    }
}
