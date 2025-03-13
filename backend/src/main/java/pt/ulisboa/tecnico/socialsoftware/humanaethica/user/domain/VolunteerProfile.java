package pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain;

import jakarta.persistence.*;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.domain.Participation;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.dto.ParticipationDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.dto.VolunteerProfileDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;

@Entity
public class VolunteerProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "volunteer_id")
    private Volunteer volunteer;

    @OneToMany(mappedBy = "volunteerProfile", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<Participation> chosenParticipations = new ArrayList<>();

    private String shortBio;
    private Integer numTotalEnrollments;
    private Integer numTotalParticipations;
    private Integer numTotalAssessments;
    private Double averageRating;

    public VolunteerProfile() {}

    public VolunteerProfile(Volunteer volunteer, VolunteerProfileDto volunteerProfileDto) {
        setVolunteer(volunteer);

        setChosenParticipations(volunteerProfileDto.getChosenParticipations());
        setShortBio(volunteerProfileDto.getVolunteerShortBio());

        updateNumTotalEnrollments();
        updateNumTotalParticipations();
        updateNumTotalAssessments();
        updateAverageRating();

        verifyInvariants();
    }

    public void setId(Integer id) { this.id = id; }

    public Integer getId() { return id; }

    public Integer getVolunteerId() { return volunteer.getId(); }

    public void setVolunteer(Volunteer volunteer) {
        this.volunteer = volunteer;
        this.volunteer.setProfile(this);
    }

    public Volunteer getVolunteer() { return volunteer; }

    public void setShortBio(String shortBio) { this.shortBio = shortBio; }

    public String getShortBio() { return shortBio; }

    public Integer getNumTotalEnrollments() { return numTotalEnrollments; }

    public void updateNumTotalEnrollments() {
        this.numTotalEnrollments = this.volunteer.getEnrollments().size();
    }

    public Integer getNumTotalParticipations() { return numTotalParticipations; }

    public void updateNumTotalParticipations() {
        this.numTotalParticipations = this.volunteer.getParticipations().size();
    }

    public Integer getNumTotalAssessments() { return numTotalAssessments; }

    public void updateNumTotalAssessments() {
        this.numTotalAssessments = this.volunteer.getAssessments().size();
    }

    public Double getAverageRating() { return averageRating; }

    public void updateAverageRating() {

        OptionalDouble average = this.volunteer.getParticipations().stream()
                .map(Participation::getMemberRating)
                .filter(Objects::nonNull)  // Excludes null values
                .mapToDouble(Integer::doubleValue)
                .average();

        // If there are values, update the average; otherwise, keep it as null
        this.averageRating = average.isPresent() ? average.getAsDouble() : null;

    }

    public List<Participation> getChosenParticipations() { return chosenParticipations; }

    public void setChosenParticipations(List<ParticipationDto> selectedParticipationDtos) {
        this.chosenParticipations.addAll(
                this.volunteer.getParticipations().stream()
                        .filter(participation -> selectedParticipationDtos.stream()
                                .anyMatch(dto -> dto.getId().equals(participation.getId())))
                        .peek(participation -> participation.setVolunteerProfile(this))
                        .toList()
        );
    }

    private void verifyInvariants() {
        // TODO Implement invariants
    }
}
