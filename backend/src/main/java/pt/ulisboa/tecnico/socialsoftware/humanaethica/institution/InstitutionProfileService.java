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
import java.util.List;

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
    public InstitutionProfileDto createInstitutionProfile(Integer institutionId, List<Integer> assessmentIds, InstitutionProfileDto institutionProfileDto) {
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new HEException(INSTITUTION_NOT_FOUND, institutionId));
        
        List<Assessment> assessments = new ArrayList<>();
        for (Integer assessmentId : assessmentIds) {
            Assessment assessment = assessmentRepository.findById(assessmentId)
                    .orElseThrow(() -> new HEException(ASSESSMENT_NOT_FOUND, assessmentId));
            assessments.add(assessment);
        }

        if (institutionProfileRepository.findInstitutionProfileByInstitutionId(institutionId).isPresent()) {
            throw new HEException(INSTITUTION_PROFILE_ALREADY_EXISTS, institutionId);
        }

        Integer memberCount = institution.getMembers() == null ? 0 : institution.getMembers().size();
        Integer assessmentsCount = assessmentRepository.countAssessmentsByInstitutionId(institutionId);
        List<Participation> participations = participationRepository.getParticipationsByInstitutionId(institutionId);

        
        Integer ratingSum = 0;
        float avgRating = 0;
        if (participations.size() > 0) {
            for (Participation participation : participations) {
                ratingSum += participation.getVolunteerRating();
            }
            avgRating = (float) ratingSum / (float) participations.size();
        }

        Integer activityCount = activityRepository.countActivitiesByInstitutionId(institutionId);
        Integer volunteerCount = userRepository.countUniqueVolunteersByInstitution(institutionId);

        InstitutionProfile institutionProfile = new InstitutionProfile(
                institution,
                assessments,
                institutionProfileDto,
                memberCount,
                activityCount,
                assessmentsCount,
                volunteerCount,
                avgRating
        );

        institutionProfileRepository.save(institutionProfile);

        return new InstitutionProfileDto(institutionProfile);
    }
}
