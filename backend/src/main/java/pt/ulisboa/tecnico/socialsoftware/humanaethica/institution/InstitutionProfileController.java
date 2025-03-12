package pt.ulisboa.tecnico.socialsoftware.humanaethica.institution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
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
}