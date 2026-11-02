package com.ga.lanceify.identity.security;

import com.ga.lanceify.identity.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MyUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public MyUserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .map(MyUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("User with email " + email + " not found"));
    }

    /**
     * Tokens store the user id, so the JWT filter looks users up by id.
     */
    public MyUserDetails loadUserById(String id) throws UsernameNotFoundException {
        return userRepository.findById(id)
                .map(MyUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("User with id " + id + " not found"));
    }
}
