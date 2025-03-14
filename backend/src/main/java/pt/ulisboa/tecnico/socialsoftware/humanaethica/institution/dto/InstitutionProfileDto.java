package pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.dto;

import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain.InstitutionProfile;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.dto.AssessmentDto;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InstitutionProfileDto {
    
    private Integer id;
    private String shortDescription;
    private int numMembers;
    private int numActivities;
    private int numAssessments;
    private int numVolunteers;
    private float averageRating;
    private List<Integer> assessmentIds = new ArrayList<>();
    private List<AssessmentDto> assessmentDto = new ArrayList<>();

    public InstitutionProfileDto() {
    }

    public InstitutionProfileDto(InstitutionProfile institutionProfile) {
        this.id = institutionProfile.getId();
        this.shortDescription = institutionProfile.getShortDescription();
        this.numMembers = institutionProfile.getNumMembers();
        this.numActivities = institutionProfile.getNumActivities();
        this.numAssessments = institutionProfile.getNumAssessments();

        this.numVolunteers = institutionProfile.getNumVolunteers();
        this.averageRating = institutionProfile.getAverageRating();
        
        if (institutionProfile.getAssessments() != null) {
            this.assessmentIds = institutionProfile.getAssessments().stream()
                .map(assessment -> assessment.getId()) // Extract only IDs
                .collect(Collectors.toList());
        }
        this.assessmentDto = institutionProfile.getAssessments().stream()
            .map(assessment -> new AssessmentDto(assessment))
            .toList();
    }

    public InstitutionProfileDto(String shortDescription, List<Integer> assessmentIds) {
        this.shortDescription = shortDescription;
        this.assessmentIds = assessmentIds;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public List<Integer> getAssessmentIds() {
        return assessmentIds;
    }

    public void setAssessmentIds(List<Integer> assessmentIds) {
        this.assessmentIds = assessmentIds;
    }

    public List<AssessmentDto> getAssessments() {
        return assessmentDto;
    }

    public void setAssessments(List<AssessmentDto> assessmentDto) {
        this.assessmentDto = assessmentDto;
    }
}

