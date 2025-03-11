
package pt.ulisboa.tecnico.socialsoftware.humanaethica.suggestion;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

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
            @AuthenticationPrincipal Integer userId,
            @PathVariable Integer institutionId,
            @RequestBody ActivitySuggestionDto activitySuggestionDto) {

        return activitySuggestionService.createActivitySuggestion(userId, institutionId, activitySuggestionDto);
    }
}
