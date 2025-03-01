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
        nameIsRequired();
        regionIsRequired();
        descriptionIsRequired();
        hasOneToFiveParticipants();
        applicationDeadlineIsRequired();
        startingDateIsRequired();
        endingDateIsRequired();
        applicationBeforeStart();
        startBeforeEnd();
        themesAreApproved();
        nameIsUnique();
        suspensionJustificationTextSize();
        suspensionBeforeEnd();
    }

    private void nameIsRequired() {
        if (this.name == null || this.name.trim().isEmpty()) {
            throw new HEException(ACTIVITY_NAME_INVALID, this.name);
        }
    }

    private void regionIsRequired() {
        if (this.region == null || this.region.trim().isEmpty()) {
            throw new HEException(ACTIVITY_REGION_NAME_INVALID, this.region);
        }
    }

    private void descriptionIsRequired() {
        if (this.description == null || this.description.trim().isEmpty()) {
            throw new HEException(ACTIVITY_DESCRIPTION_INVALID, this.description);
        }
    }


    private void hasOneToFiveParticipants() {
        if (this.participantsNumberLimit == null || this.participantsNumberLimit <= 0 || this.participantsNumberLimit > 5) {
            throw new HEException(ACTIVITY_SHOULD_HAVE_ONE_TO_FIVE_PARTICIPANTS);
        }
    }

    private void applicationDeadlineIsRequired() {
        if (this.applicationDeadline == null) {
            throw new HEException(ACTIVITY_INVALID_DATE, "Enrollment deadline");
        }
    }

    private void startingDateIsRequired() {
        if (this.startingDate == null) {
            throw new HEException(ACTIVITY_INVALID_DATE, "starting date");
        }
    }

    private void endingDateIsRequired() {
        if (this.endingDate == null) {
            throw new HEException(ACTIVITY_INVALID_DATE, "ending date");
        }
    }

    private void applicationBeforeStart() {
        if (!this.applicationDeadline.isBefore(this.startingDate)) {
            throw new HEException(ACTIVITY_APPLICATION_DEADLINE_AFTER_START);
        }
    }

    private void startBeforeEnd() {
        if (!this.startingDate.isBefore(this.endingDate)) {
            throw new HEException(ACTIVITY_START_AFTER_END);
        }
    }

    private void themesAreApproved() {
        for (Theme theme : this.themes) {
            if (theme.getState() != Theme.State.APPROVED) {
                throw new HEException(THEME_NOT_APPROVED, theme.getCompleteName());
            }
        }
    }

    private void nameIsUnique() {
        if (this.institution.getActivities().stream()
                .anyMatch(activity -> activity != this && activity.getName().equals(this.getName()))) {
            throw new HEException(ACTIVITY_ALREADY_EXISTS);
        }
    }

    private void suspensionJustificationTextSize() {
        if (this.state != State.SUSPENDED) {
            return;
        }

        if (this.suspensionJustification == null) {
            throw new HEException(ACTIVITY_SUSPENSION_JUSTIFICATION_INVALID);
        }

        var textSize = this.suspensionJustification.length();
        if (textSize < MIN_JUSTIFICATION_SIZE || textSize > MAX_JUSTIFICATION_SIZE) {
            throw new HEException(ACTIVITY_SUSPENSION_JUSTIFICATION_INVALID);
        }
    }

    private void suspensionBeforeEnd() {
        if (this.suspensionDate != null && this.suspensionDate.isAfter(this.endingDate)) {
            throw new HEException(ACTIVITY_SUSPENSION_AFTER_END);
        }
    }
}
