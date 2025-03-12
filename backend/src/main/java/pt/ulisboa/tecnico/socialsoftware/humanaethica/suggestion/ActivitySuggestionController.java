
package pt.ulisboa.tecnico.socialsoftware.humanaethica.suggestion;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import java.util.List;
import java.security.Principal;

import pt.ulisboa.tecnico.socialsoftware.humanaethica.auth.domain.AuthUser;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.suggestion.dto.ActivitySuggestionDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.suggestion.ActivitySuggestionService;

@RestController
@RequestMapping("/suggestions")
public class ActivitySuggestionController {

    private final ActivitySuggestionService activitySuggestionService;

    public ActivitySuggestionController(ActivitySuggestionService activitySuggestionService) {
        this.activitySuggestionService = activitySuggestionService;
    }

    @PostMapping("/{institutionId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ActivitySuggestionDto createActivitySuggestion(
            Principal principal,
            @PathVariable Integer institutionId,
            @RequestBody ActivitySuggestionDto activitySuggestionDto) {
            
        int userId = ((AuthUser) ((Authentication) principal).getPrincipal()).getUser().getId();

        return activitySuggestionService.createActivitySuggestion(userId, institutionId, activitySuggestionDto);
    }
}
