package pt.ulisboa.tecnico.socialsoftware.humanaethica.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.HEException;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.*;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.dto.VolunteerProfileDto;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.User;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.Volunteer;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.domain.VolunteerProfile;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.repository.UserRepository;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.repository.VolunteerProfileRepository;
import jakarta.persistence.PersistenceContext;

import static pt.ulisboa.tecnico.socialsoftware.humanaethica.exceptions.ErrorMessage.*;

@Service
public class UserProfileService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private VolunteerProfileRepository profileRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public VolunteerProfileDto createVolunteerProfile(Integer userId, VolunteerProfileDto volunteerProfileDto) {

        if (volunteerProfileDto == null) throw  new HEException(VOLUNTEER_PROFILE_REQUIRES_INFORMATION);

        if (userId == null) throw  new HEException(USER_NOT_FOUND);
        Volunteer volunteer = (Volunteer) this.userRepository.findById(userId).orElseThrow(() -> new HEException(USER_NOT_FOUND));

        VolunteerProfile volunteerProfile = new VolunteerProfile(volunteer, volunteerProfileDto);
        profileRepository.save(volunteerProfile);

        return new VolunteerProfileDto(volunteerProfile);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public VolunteerProfileDto getVolunteerProfile(Integer userId) {
        if (userId == null) { throw new HEException(USER_NOT_FOUND); }

        User user = this.userRepository.findById(userId).orElseThrow(() -> new HEException(USER_NOT_FOUND));
        if (!user.getRole().equals(User.Role.VOLUNTEER)) {
            throw new HEException(USER_NOT_VOLUNTEER, userId);
        }

        VolunteerProfile profile = ((Volunteer) user).getProfile();
        if (profile == null) {
            throw new HEException(VOLUNTEER_PROFILE_NOT_FOUND, userId);
        }

        return new VolunteerProfileDto(profile);
    }
}
