package pt.ulisboa.tecnico.socialsoftware.humanaethica.activitysuggestion;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activitysuggestion.dto.ActivitySuggestionDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.auth.domain.AuthUser;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/activitySuggestions")
public class ActivitySuggestionController {
    @Autowired
    private ActivitySuggestionService activitySuggestionService;

    @GetMapping("/institution/{institutionId}")
    @PreAuthorize("hasRole('ROLE_MEMBER') and hasPermission(#institutionId, 'INSTITUTION.MEMBER')")
    public List<ActivitySuggestionDto> getActivitySuggestions(@PathVariable Integer institutionId) {
        return activitySuggestionService.getActivitySuggestionsByInstitution(institutionId);
    }

    @GetMapping("/volunteer")
    @PreAuthorize("hasRole('ROLE_VOLUNTEER')")
    public List<ActivitySuggestionDto> getVolunteerActivitySuggestions(Principal principal) {
        int userId = ((AuthUser) ((Authentication) principal).getPrincipal()).getUser().getId();
        return activitySuggestionService.getActivitySuggestionsByVolunteer(userId);
    }

    @PutMapping("/institution/{institutionId}/{suggestionId}/approve")
    @PreAuthorize("hasRole('ROLE_MEMBER') and hasPermission(#institutionId, 'INSTITUTION.MEMBER')")
    public void approveActivitySuggestion(@PathVariable Integer institutionId, @PathVariable Integer suggestionId) {
        activitySuggestionService.approveActivitySuggestion(institutionId, suggestionId);
    }

    @PutMapping("/institution/{institutionId}/{suggestionId}/reject")
    @PreAuthorize("hasRole('ROLE_MEMBER') and hasPermission(#institutionId, 'INSTITUTION.MEMBER')")
    public void rejectActivitySuggestion(@PathVariable Integer institutionId, @PathVariable Integer suggestionId) {
        activitySuggestionService.rejectActivitySuggestion(institutionId, suggestionId);
    }

    @PostMapping("/institution/{institutionId}")
    @PreAuthorize("hasRole('ROLE_VOLUNTEER')")
    public ActivitySuggestionDto createActivitySuggestion(Principal principal, @PathVariable Integer institutionId, @Valid @RequestBody ActivitySuggestionDto activitySuggestionDto) {
        int userId = ((AuthUser) ((Authentication) principal).getPrincipal()).getUser().getId();
        return activitySuggestionService.createActivitySuggestion(userId, institutionId, activitySuggestionDto);
    }
}