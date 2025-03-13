package pt.ulisboa.tecnico.socialsoftware.humanaethica.user.dto;

import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.dto.ParticipationDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.User;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.VolunteerProfile;

import java.util.List;

public class VolunteerProfileDto {
    private Integer id;
    private Integer volunteerId;
    private String shortBio;
    private Integer numTotalEnrollments;
    private Integer numTotalParticipations;
    private List<ParticipationDto> chosenParticipations;
    private Integer numTotalAssessments;
    private Double averageRating;

    public VolunteerProfileDto() {}

    public VolunteerProfileDto(VolunteerProfile volunteerProfile) {
        setId(volunteerProfile.getId());
        setVolunteerId(volunteerProfile.getVolunteerId());
        setAverageRating(volunteerProfile.getAverageRating());
        setVolunteerShortBio(volunteerProfile.getShortBio());
        setNumTotalEnrollments(volunteerProfile.getNumTotalEnrollments());
        setNumTotalParticipations(volunteerProfile.getNumTotalParticipations());
        setNumTotalAssessments(volunteerProfile.getNumTotalAssessments());

        setChosenParticipations(volunteerProfile.getChosenParticipations().stream()
                .map(participation -> new ParticipationDto(participation, User.Role.VOLUNTEER))
                .toList()
        );
    }

    public Integer getId() { return id; }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getVolunteerId() { return volunteerId; }

    public void setVolunteerId(Integer volunteerId) { this.volunteerId = volunteerId; }

    public List<ParticipationDto> getChosenParticipations(){
        return chosenParticipations;
    }

    public void setChosenParticipations(List<ParticipationDto> chosenParticipations){
        this.chosenParticipations = chosenParticipations;
    }

    public String getVolunteerShortBio() {
        return shortBio;
    }

    public void setVolunteerShortBio(String shortBio) {
        this.shortBio = shortBio;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setNumTotalEnrollments(Integer numTotalEnrollments) { this.numTotalEnrollments = numTotalEnrollments; }

    public void setNumTotalParticipations(Integer numTotalParticipations) {this.numTotalParticipations = numTotalParticipations; }

    public void setNumTotalAssessments(Integer numTotalAssessments) { this.numTotalAssessments = numTotalAssessments; }

    public Integer getNumTotalEnrollments() { return numTotalEnrollments; }

    public Integer getNumTotalParticipations() { return numTotalParticipations; }

    public Integer getNumTotalAssessments() { return numTotalAssessments; }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

}
