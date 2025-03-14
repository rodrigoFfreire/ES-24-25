package pt.ulisboa.tecnico.socialsoftware.humanaethica.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.dto.VolunteerProfileDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.auth.domain.AuthUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import jakarta.validation.Valid;
import java.security.Principal;

@RestController
public class UserProfileController {
    @Autowired
    private UserProfileService userProfileService;

    @PostMapping("/users/{volunteerId}/profile")
    @PreAuthorize("(hasRole('ROLE_VOLUNTEER'))")
    public VolunteerProfileDto createVolunteerProfile(Principal principal, @PathVariable Integer volunteerId, @Valid @RequestBody VolunteerProfileDto volunteerProfileDto) {
        int userId = ((AuthUser) ((Authentication) principal).getPrincipal()).getUser().getId();
        return userProfileService.createVolunteerProfile(volunteerId, volunteerProfileDto);
    }

    @GetMapping("/users/{volunteerId}/profile")
    public VolunteerProfileDto getVolunteerProfile(@PathVariable Integer volunteerId) {
        return userProfileService.getVolunteerProfile(volunteerId);
    }
}
