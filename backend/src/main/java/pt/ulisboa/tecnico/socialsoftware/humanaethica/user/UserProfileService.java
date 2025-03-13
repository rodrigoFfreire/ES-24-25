package pt.ulisboa.tecnico.socialsoftware.humanaethica.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.repository.UserRepository;
import pt.ulisboa.tecnico.socialsoftware.humanaethica.user.repository.VolunteerProfileRepository;

@Service
public class UserProfileService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private VolunteerProfileRepository profileRepository;

    // TODO Implement createVolunteerProfile Service

    // TODO Implement getVolunteerProfile Service
}
