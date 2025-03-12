package pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain;

import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.dto.InstitutionProfileDto;

import jakarta.persistence.*;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.domain.Assessment;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "institution_profiles")
public class InstitutionProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "institution_id")
    private Institution institution;

    @Column(name = "short_description", length = 255)
    private String shortDescription;

    @Column(name = "num_members")
    private int numMembers;

    @Column(name = "num_activities")
    private int numActivities;

    @Column(name = "num_assessments")
    private int numAssessments;

    @Column(name = "num_volunteers")
    private int numVolunteers;

    @Column(name = "average_rating")
    private float averageRating;

    @OneToMany()
    private List<Assessment> assessments = new ArrayList<>();

    public InstitutionProfile() {}

    public InstitutionProfile(Institution institution, List<Assessment> assessments, InstitutionProfileDto institutionProfileDto, int numMembers, 
                              int numActivities, int numAssessments, int numVolunteers, float averageRating) {
        setInstitution(institution);
        setAssessments(assessments);
        setShortDescription(institutionProfileDto.getShortDescription());
        setNumMembers(numMembers);
        setNumActivities(numActivities);
        setNumAssessments(numAssessments);
        setNumVolunteers(numVolunteers);
        setAverageRating(averageRating);

        verifyInvariants();
    }
    public void verifyInvariants() {
        verifyDescriptionLength();
        verifyRecentAssessments();
        verifySelectedAssessments();
    }

    private void verifyDescriptionLength() {
        if (shortDescription == null || shortDescription.length() < 10) {
            throw new IllegalArgumentException("A descrição da instituição deve ter pelo menos 10 caracteres.");
        }
    }

    private void verifyRecentAssessments() {
        if (assessments.size() > 0) {
            int totalAssessments = assessments.size();
            
            List<Assessment> allInstitutionAssessments = institution.getAssessments(); 
            
            allInstitutionAssessments.sort((a1, a2) -> a2.getReviewDate().compareTo(a1.getReviewDate()));
            
            int recentThreshold = (int) Math.ceil(allInstitutionAssessments.size() * 0.2);
            
            List<Assessment> mostRecentAssessments = allInstitutionAssessments.stream()
                    .limit(recentThreshold)
                    .toList();
            
            long recentAssessmentsCount = assessments.stream()
                    .filter(mostRecentAssessments::contains)
                    .count();
            
            if (recentAssessmentsCount < totalAssessments * 0.2) {
                throw new IllegalArgumentException("Pelo menos 20% das avaliações devem ser as mais recentes.");
            }
        }
    }
    
    private void verifySelectedAssessments() {
        int selectedAssessmentsCount = assessments.size();
        int totalAssessments = institution.getAssessments().size();

        if (selectedAssessmentsCount < totalAssessments * 0.5) {
            throw new IllegalArgumentException("Pelo menos 50% das avaliações devem ser selecionadas.");
        }
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
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

    public List<Assessment> getAssessments() {
        return assessments;
    }

    public void setAssessments(List<Assessment> assessments) {
        this.assessments = assessments;
    }

    public void addAssessment(Assessment assessment) {
        this.assessments.add(assessment);
    }

    public void removeAssessment(Assessment assessment) {
        this.assessments.remove(assessment);
    }
}

