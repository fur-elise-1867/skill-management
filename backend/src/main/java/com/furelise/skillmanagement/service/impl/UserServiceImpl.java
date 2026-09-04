package com.furelise.skillmanagement.service.impl;

import com.furelise.skillmanagement.dto.UserResponse;
import com.furelise.skillmanagement.model.User;
import com.furelise.skillmanagement.repository.UserRepository;
import com.furelise.skillmanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * User service for retrieving current user profile.
 * Gets the authenticated user from SecurityContext instead of a faulty Principal bean.
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserResponse getCurrentUserProfile() {
        // Get current user from SecurityContext (fixes the NullPointerException bug)
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof User currentUser)) {
            throw new UsernameNotFoundException("No authenticated user found.");
        }

        // Fetch fresh data from DB
        var user = userRepository.findByEmail(currentUser.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));

        return UserResponse.from(user);
    }
}
