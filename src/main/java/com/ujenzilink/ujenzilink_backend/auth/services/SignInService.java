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

    public SignInService(UserRepository userRepository, SignUpService signUpService) {
        this.userRepository = userRepository;
    }

    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findFirstByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("Account not found. Please register.");
        }
        if (!user.isEnabled()) {
            throw new DisabledException("Account unverified. Please check your email for the confirmation code.");
        }
        return new org.springframework.security.core.userdetails.User(user.getEmail(),
                user.getPassword(), user.getAuthorities());
    }

    public User findUserByEmail(String email) {

        return userRepository.findFirstByEmail(email);
    }
}
