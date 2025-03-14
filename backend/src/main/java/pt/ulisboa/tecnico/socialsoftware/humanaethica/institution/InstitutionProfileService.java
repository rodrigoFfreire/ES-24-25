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
import pt.ulisboa.tecnico.socialsoftware.humanaethica.assessment.AssessmentRepository;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.ParticipationRepository;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.participation.domain.Participation;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.activity.repository.ActivityRepository;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage.*;

@Service
public class InstitutionProfileService {

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private AssessmentRepository assessmentRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private ParticipationRepository participationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InstitutionProfileRepository institutionProfileRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public InstitutionProfileDto getInstitutionProfile(Integer institutionId) {
        InstitutionProfile institutionProfile = institutionProfileRepository.findInstitutionProfileById(institutionId)
                .orElseThrow(() -> new HEException(INSTITUTION_NOT_FOUND, institutionId));

    public InstitutionProfileDto createInstitutionProfile(Integer institutionId, InstitutionProfileDto institutionProfileDto) {
        // Verify institution exists
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new HEException(INSTITUTION_NOT_FOUND, institutionId));
        
        if (institutionProfileRepository.findInstitutionProfileByInstitutionId(institutionId).isPresent()) {
            throw new HEException(INSTITUTION_PROFILE_ALREADY_EXISTS, institutionId);
        }
        
        institutionProfileDto.setNumMembers(institution.getMembers() == null ? 0 : institution.getMembers().size());
        institutionProfileDto.setNumAssessments(assessmentRepository.countAssessmentsByInstitutionId(institutionId));
        institutionProfileDto.setNumActivities(activityRepository.countActivitiesByInstitutionId(institutionId));
        institutionProfileDto.setNumVolunteers(userRepository.countUniqueVolunteersByInstitution(institutionId));

        List<Participation> participations = participationRepository.getParticipationsByInstitutionId(institutionId);
        List<Participation> validRatedParticipations = participations.stream()
                .filter(p -> p.getVolunteerRating() != null)
                .collect(Collectors.toList());
        if (!validRatedParticipations.isEmpty()) {
            int ratingSum = validRatedParticipations.stream()
                    .mapToInt(Participation::getVolunteerRating)
                    .sum();
            institutionProfileDto.setAverageRating((float) ratingSum / validRatedParticipations.size());
        } else {
            institutionProfileDto.setAverageRating(0);
        }
        
        InstitutionProfile institutionProfile = new InstitutionProfile(institution, institutionProfileDto);
        
        institutionProfileRepository.save(institutionProfile);
        return new InstitutionProfileDto(institutionProfile);
    }
}
