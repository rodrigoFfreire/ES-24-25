package pt.ulisboa.tecnico.socialsoftware.humanaethica.institution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain.Institution;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.domain.InstitutionProfile;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.repository.InstitutionProfileRepository;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.repository.InstitutionRepository;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.institution.dto.InstitutionProfileDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.domain.Assessment;

import java.util.ArrayList;
import java.util.List;

import static pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage.*;

@Service
public class InstitutionProfileService {

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private InstitutionProfileRepository institutionProfileRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public InstitutionProfileDto createInstitutionProfile(Integer institutionId, InstitutionProfileDto institutionProfileDto) {
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new HEException(INSTITUTION_NOT_FOUND, institutionId));

        if (institutionProfileRepository.findInstitutionProfileById(institutionId).isPresent()) {
            throw new HEException(INSTITUTION_PROFILE_ALREADY_EXISTS, institutionId);
        }

        InstitutionProfile institutionProfile = new InstitutionProfile(
                institution,
                institutionProfileDto.getShortDescription(),
                institutionProfileDto.getNumMembers(),
                institutionProfileDto.getNumActivities(),
                institutionProfileDto.getNumAssessments(),
                institutionProfileDto.getNumVolunteers(),
                institutionProfileDto.getAverageRating()
        );

        if (institution.getAssessments() != null && !institution.getAssessments().isEmpty()) {
            institutionProfile.setAssessments(new ArrayList<>(institution.getAssessments()));
        }

        institutionProfileRepository.save(institutionProfile);

        return new InstitutionProfileDto(institutionProfile);
    }
}
