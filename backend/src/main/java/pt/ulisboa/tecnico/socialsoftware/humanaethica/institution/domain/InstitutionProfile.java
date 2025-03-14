package pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain;

import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.dto.InstitutionProfileDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException;

import jakarta.persistence.*;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.domain.Assessment;
import static pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "institution_profiles")
public class InstitutionProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "institution_id", nullable = false, unique = true)
    private Institution institution;

    @Column(name = "short_description", length = 255, nullable = false)
    private String shortDescription;

    @Column(name = "num_activities")
    private int numActivities;

    @Column(name = "num_members")
    private int numMembers;

    @Column(name = "num_assessments")
    private int numAssessments;

    @Column(name = "num_volunteers")
    private int numVolunteers;

    @OneToMany
    private List<Assessment> assessments = new ArrayList<>();

    @Column(name = "average_rating")
    private float averageRating;

    public InstitutionProfile() {}

    public InstitutionProfile(Institution institution, InstitutionProfileDto institutionProfileDto) {
        if (institution == null) {
            throw new HEException(INSTITUTION_NOT_FOUND, null);
        }
        
        if (institution.getProfile() != null) {
            throw new HEException(INSTITUTION_PROFILE_ALREADY_EXISTS, institution.getId());
        }

        this.institution = institution;
        setShortDescription(institutionProfileDto.getShortDescription());
        setNumActivities(institution.getActivities().size());
        selectAssessments(institution, institutionProfileDto.getAssessmentIds());
        setNumAssessments(institution.getAssessments().size());
        setNumMembers(institution.getMembers().size());
        setNumVolunteers(institutionProfileDto.getNumVolunteers());
        setAverageRating(institutionProfileDto.getAverageRating());
        setInstitution(institution);

        verifyInvariants();
    }
    
    public void selectAssessments(Institution institution, List<Integer> assessmentIds) {
        Set<Integer> validAssessmentIds = institution.getAssessments().stream()
                .map(Assessment::getId)
                .collect(Collectors.toSet());

        if (!validAssessmentIds.containsAll(assessmentIds)) {
            throw new HEException(ASSESSMENT_NOT_FROM_INSTITUTION);
        }

        List<Assessment> selectedAssessments = institution.getAssessments().stream()
                .filter(assessment -> assessmentIds.contains(assessment.getId()))
                .collect(Collectors.toList());

        setAssessments(selectedAssessments);
        verifySelectedAssessments();
    }

    public void verifyInvariants() {
        verifyDescriptionLength();
        verifyRecentAssessments();
        verifySelectedAssessments();
    }

    private void verifyDescriptionLength() {
        if (shortDescription == null || shortDescription.length() < 10) {
            throw new HEException(INVALID_DESCRIPTION_LENGTH);
        }
    }

    private void verifyRecentAssessments() {
        if (assessments.size() > 0) {
            int totalAssessments = assessments.size();
            
            List<Assessment> allInstitutionAssessments = institution.getAssessments(); 
            
            allInstitutionAssessments.sort((a1, a2) -> a2.getReviewDate().compareTo(a1.getReviewDate()));
            
            int recentThreshold = (int) Math.ceil(totalAssessments * 0.2);
            
            List<Assessment> mostRecentAssessments = allInstitutionAssessments.stream()
                    .limit(recentThreshold)
                    .toList();
            
            long recentAssessmentsCount = assessments.stream()
                    .filter(mostRecentAssessments::contains)
                    .count();
            
            if (recentAssessmentsCount < recentThreshold) {
                throw new HEException(INSUFFICIENT_RECENT_ASSESSMENTS);
            }
        }
    }
    
    private void verifySelectedAssessments() {
        int selectedAssessmentsCount = assessments.size();
        int totalAssessments = institution.getAssessments().size();

        if (selectedAssessmentsCount < totalAssessments * 0.5) {
            System.out.println("Selected assessments count: " + selectedAssessmentsCount + ", total assessments super unique count: " + totalAssessments);
            throw new HEException(INSUFFICIENT_SELECTED_ASSESSMENTS);
        }
    }
    
    public Integer getId() {
        return id;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
        this.institution.setProfile(this);
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public int getNumMembers() {
        return numMembers;
    }

    public void setNumMembers(int numMembers) {
        this.numMembers = numMembers;
    }

    public int getNumActivities() {
        return numActivities;
    }

    public void setNumActivities(int numActivities) {
        this.numActivities = numActivities;
    }

    public int getNumAssessments() {
        return numAssessments;
    }

    public void setNumAssessments(int numAssessments) {
        this.numAssessments = numAssessments;
    }

    public int getNumVolunteers() {
        return numVolunteers;
    }

    public void setNumVolunteers(int numVolunteers) {
        this.numVolunteers = numVolunteers;
    }

    public float getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(float averageRating) {
        this.averageRating = averageRating;
    }

    public List<Assessment> getAssessments() {
        return assessments;
    }

    private void setAssessments(List<Assessment> assessments) {
        this.assessments = assessments;
    }
}

