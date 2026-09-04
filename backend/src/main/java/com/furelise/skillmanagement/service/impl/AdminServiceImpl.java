package com.furelise.skillmanagement.service.impl;

import com.furelise.skillmanagement.dto.UserResponse;
import com.furelise.skillmanagement.model.User;
import com.furelise.skillmanagement.repository.UserRepository;
import com.furelise.skillmanagement.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Admin service for managing all users.
 */
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public void deleteUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        userRepository.deleteById(user.getId());
    }
}
