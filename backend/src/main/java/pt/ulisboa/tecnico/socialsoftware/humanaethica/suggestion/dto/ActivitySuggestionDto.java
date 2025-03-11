package pt.ulisboa.tecnico.socialsoftware.humanaethica.suggestion.dto;

import pt.ulisboa.tecnico.socialsoftware.humanaethica.suggestion.domain.ActivitySuggestion;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.dto.InstitutionDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.dto.UserDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.utils.DateHandler;

public class ActivitySuggestionDto {
    private Integer id;
    private String name;
    private String description;
    private String region;
    private Integer participantsNumberLimit;
    private String startingDate;
    private String endingDate;
    private String applicationDeadline;
    private String creationDate;
    private String state;
    private InstitutionDto institution;
    private UserDto volunteer;

    public ActivitySuggestionDto() {
    }

    public ActivitySuggestionDto(boolean deepCopyInstitution, boolean deepCopyVolunteer, ActivitySuggestion activitySuggestion) {
        setId(activitySuggestion.getId());
        setName(activitySuggestion.getName());
        setRegion(activitySuggestion.getRegion());
        setParticipantsNumberLimit(activitySuggestion.getParticipantsNumberLimit());
        setDescription(activitySuggestion.getDescription());

        setState(activitySuggestion.getState().name());
        setCreationDate(DateHandler.toISOString(activitySuggestion.getCreationDate()));
        setStartingDate(DateHandler.toISOString(activitySuggestion.getStartingDate()));
        setEndingDate(DateHandler.toISOString(activitySuggestion.getEndingDate()));
        setApplicationDeadline(DateHandler.toISOString(activitySuggestion.getApplicationDeadline()));

        if (deepCopyInstitution && activitySuggestion.getInstitution() != null) {
            setInstitution(new InstitutionDto(activitySuggestion.getInstitution(), false, false));
        }

        if (deepCopyVolunteer && activitySuggestion.getVolunteer() != null) {
            setVolunteer(new UserDto(activitySuggestion.getVolunteer()));
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getParticipantsNumberLimit() {
        return participantsNumberLimit;
    }

    public void setParticipantsNumberLimit(Integer participantsNumberLimit) {
        this.participantsNumberLimit = participantsNumberLimit;
    }

    public String getStartingDate() {
        return startingDate;
    }

    public void setStartingDate(String startingDate) {
        this.startingDate = startingDate;
    }

    public String getEndingDate() {
        return endingDate;
    }

    public void setEndingDate(String endingDate) {
        this.endingDate = endingDate;
    }

    public String getApplicationDeadline() {
        return applicationDeadline;
    }

    public void setApplicationDeadline(String applicationDeadline) {
        this.applicationDeadline = applicationDeadline;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public InstitutionDto getInstitution() {
        return institution;
    }

    public void setInstitution(InstitutionDto institution) {
        this.institution = institution;
    }

    public UserDto getVolunteer() {
        return volunteer;
    }

    public void setVolunteer(UserDto volunteer) {
        this.volunteer = volunteer;
    }

    @Override
    public String toString() {
        return "ActivitySuggestionDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", region='" + region + '\'' +
                ", participantsNumberLimit=" + participantsNumberLimit +
                ", description='" + description + '\'' +
                ", startingDate='" + startingDate + '\'' +
                ", endingDate='" + endingDate + '\'' +
                ", applicationDeadline='" + applicationDeadline + '\'' +
                ", creationDate='" + creationDate + '\'' +
                ", state='" + state + '\'' +
                ", institution=" + institution +
                ", volunteer=" + volunteer +
                '}';
    }
}
