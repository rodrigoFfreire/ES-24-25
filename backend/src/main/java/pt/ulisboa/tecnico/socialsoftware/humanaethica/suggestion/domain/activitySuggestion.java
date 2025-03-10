package pt.ulisboa.tecnico.socialsoftware.humanaethica.suggestion.domain;

import jakarta.persistence.*;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.dto.ActivityDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.enrollment.domain.Enrollment;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.report.domain.Report;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain.Institution;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.domain.Participation;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.theme.domain.Theme;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage.*;

@Entity
@Table(name = "suggestion")
public class activitySuggestion {
    private static final int MIN_JUSTIFICATION_SIZE = 10;
    private static final int MAX_JUSTIFICATION_SIZE = 250;

    public enum State {IN_REVIEW, APPROVED, REJECTED}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private String description;
    private String region;
    private LocalDateTime applicationDeadline;
    private LocalDateTime startingDate;
    private LocalDateTime endingDate;
    private Integer participantsNumberLimit;
    @Enumerated(EnumType.STRING)
    private activitySuggestion.State state = activitySuggestion.State.IN_REVIEW;
    @ManyToOne
    private Institution institution;
    @ManyToOne
    private Volunteer volunteer;
    @Column(name = "creation_date")
    private LocalDateTime creationDate;
    

    public activitySuggestion() {
    }

    public activitySuggestion(activitySuggestionDto activitysuggestionDto, Institution institution, Volunteer volunteer) {
        setInstitution(institution);
        setVolunteer(volunteer);
        setName(activitysuggestionDto.getName());
        setRegion(activitysuggestionDto.getRegion());
        setParticipantsNumberLimit(activitysuggestionDto.getParticipantsNumberLimit());
        setDescription(activitysuggestionDto.getDescription());
        setCreationDate(DateHandler.now());
        setStartingDate(DateHandler.toLocalDateTime(activitysuggestionDto.getStartingDate()));
        setEndingDate(DateHandler.toLocalDateTime(activitysuggestionDto.getEndingDate()));
        setApplicationDeadline(DateHandler.toLocalDateTime(activitysuggestionDto.getApplicationDeadline()));

        verifyInvariants();
    }

    public void update(activitySuggestionDto activitysuggestionDto) {
        setName(activitysuggestionDto.getName());
        setRegion(activitysuggestionDto.getRegion());
        setParticipantsNumberLimit(activitysuggestionDto.getParticipantsNumberLimit());
        setDescription(activitysuggestionDto.getDescription());
        setStartingDate(DateHandler.toLocalDateTime(activitysuggestionDto.getStartingDate()));
        setEndingDate(DateHandler.toLocalDateTime(activitysuggestionDto.getEndingDate()));
        setApplicationDeadline(DateHandler.toLocalDateTime(activitysuggestionDto.getApplicationDeadline()));


        verifyInvariants();
    }


    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
       this.name = name;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Integer getParticipantsNumberLimit() {
        return participantsNumberLimit;
    }

    public void setParticipantsNumberLimit(Integer participantsNumberLimit) {
        this.participantsNumberLimit = participantsNumberLimit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getStartingDate() {
        return startingDate;
    }

    public void setStartingDate(LocalDateTime startingDate) {
        this.startingDate = startingDate;
    }

    public LocalDateTime getEndingDate() {
        return endingDate;
    }

    public void setEndingDate(LocalDateTime endingDate) {
        this.endingDate = endingDate;
    }

    public LocalDateTime getApplicationDeadline() {
        return applicationDeadline;
    }

    public void setApplicationDeadline(LocalDateTime applicationDeadline) {
        this.applicationDeadline = applicationDeadline;
    }

    public activitySuggestion.State getState() {
        return state;
    }

    public void setState(activitySuggestion.State state) {
        this.state = state;
    }

    public void validate() {

        if (getState() == State.APPROVED) {
            throw new HEException(SUGGESTION_ALREADY_APPROVED, this.name);
        }
        setState(activitySuggestion.State.APPROVED);
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
        institution.addSuggestion(this); 
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setVolunteer(Volunteer volunteer) {
        this.volunteer = volunteer;
        volunteer.addSuggestion(this); //TODO ADD ON Volunteer
    }

    public Volunteer getVolunteer() {
        return volunteer;
    }

    private void verifyInvariants() {
        ensureDescriptionIsValid();
        ensureUniqueActivityName();
        ensureValidApplicationDeadline();
    }

    private void ensureDescriptionIsValid() {
        if (this.description == null || this.description.trim().length() < 10) {
            throw new HEException(ACTIVITY_SUGGESTION_DESCRIPTION_INVALID); 
        }
    }

    private void ensureUniqueActivityName() {
        if (this.volunteer.getSuggestions().stream()
                .anyMatch(suggestion -> suggestion != this && suggestion.getName().equalsIgnoreCase(this.name))) {
            throw new HEException(ACTIVITY_SUGGESTION_REPEATED); 
        }
    }

    private void ensureValidApplicationDeadline() {
        if (!this.applicationDeadline.isAfter(this.creationDate.plusDays(7))) {
            throw new HEException(ACTIVITY_SUGGESTION_INVALID_APPLICATION_DEADLINE);
        }
    }
}
