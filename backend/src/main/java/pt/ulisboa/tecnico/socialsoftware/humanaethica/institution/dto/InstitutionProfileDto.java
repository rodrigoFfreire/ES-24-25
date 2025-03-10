package pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.dto;

import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.dto.AssessmentDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain.InstitutionProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InstitutionProfileDto {
    
    private Integer id;
    private InstitutionDto institution;
    private String shortDescription;
    private int numMembers;
    private int numActivities;
    private int numAssessments;
    private int numVolunteers;
    private float averageRating;
    private List<AssessmentDto> assessments = new ArrayList<>();

    public InstitutionProfileDto() {
    }

    public InstitutionProfileDto(InstitutionProfile institutionProfile) {
        this.id = institutionProfile.getId();
        
        if (institutionProfile.getInstitution() != null) {
            this.institution = new InstitutionDto(institutionProfile.getInstitution());
        }
        
        this.shortDescription = institutionProfile.getShortDescription();
        this.numMembers = institutionProfile.getNumMembers();
        this.numActivities = institutionProfile.getNumActivities();
        this.numAssessments = institutionProfile.getNumAssessments();
        this.numVolunteers = institutionProfile.getNumVolunteers();
        this.averageRating = institutionProfile.getAverageRating();
        
        if (institutionProfile.getAssessments() != null) {
            this.assessments = institutionProfile.getAssessments().stream()
                .map(AssessmentDto::new)
                .collect(Collectors.toList());
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public InstitutionDto getInstitution() {
        return institution;
    }

    public void setInstitution(InstitutionDto institution) {
        this.institution = institution;
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

    public List<AssessmentDto> getAssessments() {
        return assessments;
    }

    public void setAssessments(List<AssessmentDto> assessments) {
        this.assessments = assessments;
    }
}
