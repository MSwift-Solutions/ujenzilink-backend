package com.ujenzilink.ujenzilink_backend.auth.services;

import com.ujenzilink.ujenzilink_backend.auth.models.User;
import com.ujenzilink.ujenzilink_backend.auth.repositories.UserRepository;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class SignInService implements UserDetailsService {
    private final UserRepository userRepository;
    private final SignUpService signUpService;

    public SignInService(UserRepository userRepository, SignUpService signUpService) {
        this.userRepository = userRepository;
        this.signUpService = signUpService;
    }

    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findFirstByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("Username " + email + " not found, kindly register.");
        }
        if (!user.isEnabled()) {
            throw new DisabledException(
                    "Verify account first before trying to log in. Confirmation code was sent to " + user.getEmail());
        }
        return new org.springframework.security.core.userdetails.User(user.getEmail(),
                user.getPassword(), user.getAuthorities());
    }

    public User findUserByEmail(String email) {

        return userRepository.findFirstByEmail(email);
    }
}
