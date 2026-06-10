package com.AIExpense.ExpenseTracker.user.service;

import com.AIExpense.ExpenseTracker.user.dto.UserRequest;
import com.AIExpense.ExpenseTracker.user.dto.UserResponse;
import com.AIExpense.ExpenseTracker.user.entity.User;
import com.AIExpense.ExpenseTracker.common.exception.EmailAlreadyExistsException;
import com.AIExpense.ExpenseTracker.common.exception.UserNotFoundByEmailException;
import com.AIExpense.ExpenseTracker.common.exception.UserNotFoundException;
import com.AIExpense.ExpenseTracker.user.mapper.UserMapper;
import com.AIExpense.ExpenseTracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse createUser(UserRequest userRequest) {
        log.info("Creating user with email: {}", userRequest.email());

        if (userRepository.existsByEmail(userRequest.email())) {
            throw new EmailAlreadyExistsException("Email already exists: " + userRequest.email());
        }

        User user = User.builder()
                .name(userRequest.name())
                .email(userRequest.email())
                .password(passwordEncoder.encode(userRequest.password()))
                .build();

        User savedUser = userRepository.save(user);
        log.info("User created with id: {}", savedUser.getId());
        return UserMapper.toResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {

        log.info("Fetching user with email: {}", email);
        return userRepository.findByEmail(email)
                .map(UserMapper::toResponse)
                .orElseThrow(() -> new UserNotFoundByEmailException("User not found with email: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {

        log.info("Fetching user with id: {}", id);
        return userRepository.findById(id)
                .map(UserMapper::toResponse)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {

        log.info("Fetching all users");
        return userRepository.findAll()
                .stream().map(UserMapper::toResponse)
                .toList();
    }
}
