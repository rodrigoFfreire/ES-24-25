package pt.ulisboa.tecnico.socialsoftware.humanaethica.institution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;

import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.dto.InstitutionProfileDto;

@RestController
@RequestMapping("/institution-profile")
public class InstitutionProfileController {

    @Autowired
    private InstitutionProfileService institutionProfileService;

    @GetMapping("/{institutionId}")
    public InstitutionProfileDto getInstitutionProfile(@PathVariable Integer institutionId) {
        return institutionProfileService.getInstitutionProfile(institutionId);
    }

    @PostMapping("/{institutionId}")
    public InstitutionProfileDto createInstitutionProfile(
            @PathVariable Integer institutionId,
            @RequestBody InstitutionProfileDto institutionProfileDto) {

        var authentication = SecurityContextHolder.getContext().getAuthentication();

        // check if the user has the 'ROLE_MEMBER' authority
        boolean isMember = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_MEMBER"));

        if (!isMember) {
            throw new AccessDeniedException("User is not a member of this institution");
        }

        return institutionProfileService.createInstitutionProfile(institutionId, institutionProfileDto);
    }
}
