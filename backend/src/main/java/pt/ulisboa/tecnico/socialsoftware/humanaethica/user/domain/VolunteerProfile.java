package pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain;

import java.util.ArrayList;

import jakarta.persistence.*;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.domain.Participation;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.dto.ParticipationDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.dto.VolunteerProfileDto;

import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;

import static pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage.*;

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
        this.averageRating = this.volunteer.getParticipations().stream()
                .map(Participation::getMemberRating)
                .filter(Objects::nonNull)
                .mapToDouble(Integer::doubleValue)
                .average()
                .orElse(Double.NaN);  // Use NaN as a placeholder for 'null'
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
        shortBioLength();
        validNumberOfParticipations();
        participationIsEvaluated();
    }

    private void shortBioLength() {
        if (this.shortBio == null || this.shortBio.trim().length() < 10) {
            throw new HEException(PROFILE_REQUIRES_SHORTBIO);
        }
    }

    private void participationIsEvaluated() {
        if (chosenParticipations.stream().anyMatch(participation -> participation.getMemberRating() == null)) {
            throw new HEException(PROFILE_REQUIRES_PARTICIPATION_EVALUATED);
        }
    }

    private void validNumberOfParticipations(){
        int ns = this.chosenParticipations.size();
        int tp = this.volunteer.getParticipations().size();
        int ta = (int) this.volunteer.getParticipations().stream()
                .filter(participation -> participation.getMemberRating() != null)
                .count();
        int minRequired = Math.min(tp / 2, ta);

        if (ns < minRequired) {
            throw new HEException(PROFILE_REQUIRES_VALID_NUMBER_PARTICIPATIONS);
        }
    }
}
