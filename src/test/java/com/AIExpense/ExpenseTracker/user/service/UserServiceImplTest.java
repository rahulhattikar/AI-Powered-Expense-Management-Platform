package com.AIExpense.ExpenseTracker.user.service;


import com.AIExpense.ExpenseTracker.common.exception.EmailAlreadyExistsException;
import com.AIExpense.ExpenseTracker.common.exception.UserNotFoundByEmailException;
import com.AIExpense.ExpenseTracker.common.exception.UserNotFoundException;
import com.AIExpense.ExpenseTracker.user.dto.UserRequest;
import com.AIExpense.ExpenseTracker.user.dto.UserResponse;
import com.AIExpense.ExpenseTracker.user.entity.Role;
import com.AIExpense.ExpenseTracker.user.entity.User;
import com.AIExpense.ExpenseTracker.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserRequest userRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Rahul")
                .email("rahul@test.com")
                .password("encodedpassword")
                .role(Role.USER)
                .build();

        userRequest = new UserRequest("Rahul",
                "rahul@test.com", "password123");
    }

    @Test
    @DisplayName("Should return UserResponse when email does not exist")
    void createUser_ShouldReturnUserResponse_WhenEmailNotExists() {
        // setup mock behaviour
        when(userRepository.existsByEmail("rahul@test.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedpassword");

        when(userRepository.save(any(User.class)))
                .thenReturn(testUser);

        UserResponse response = userService.createUser(userRequest);

        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo("rahul@test.com");
        assertThat(response.name()).isEqualTo("Rahul");


        verify(userRepository).existsByEmail("rahul@test.com");
        verify(userRepository).save(any(User.class));

    }

    @Test
    @DisplayName("Should throw EmailAlreadyExistsException when email exists")
    void createUser_ShouldThrowException_WhenEmailExists() {

        when(userRepository.existsByEmail("rahul@test.com"))
                .thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(userRequest))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("rahul@test.com");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should return UserResponse when user exists with given id")
    void getUserById_ShouldReturnUserResponse_WhenUserExists() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(testUser));

        UserResponse response = userService.getUserById(1L);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Rahul");
        assertThat(response.email()).isEqualTo("rahul@test.com");

        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("should throw UserNotException, when id does not exists")
    void getUserById_ShouldThrowUserNotFoundException_WhenUserNotFound() {
        when(userRepository.findById(2L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(2L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findById(2L);
    }

    @Test
    @DisplayName("Should return UserResponse when user exists with given email")
    void getUserByEmail_ShouldReturnUserResponse_WhenUserExists() {
        when(userRepository.findByEmail("rahul@test.com"))
                .thenReturn(Optional.of(testUser));

        UserResponse response = userService.getUserByEmail("rahul@test.com");

        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo("rahul@test.com");
        assertThat(response.name()).isEqualTo("Rahul");

        verify(userRepository).findByEmail("rahul@test.com");
    }

    @Test
    @DisplayName("should throw UserNotException, when email does not exists")
    void getUserByEmail_ShouldThrowUserNotFoundException_WhenUserNotFound() {
        when(userRepository.findByEmail("jay@test.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByEmail("jay@test.com"))
                .isInstanceOf(UserNotFoundByEmailException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findByEmail("jay@test.com");
    }

    @Test
    @DisplayName("Should return list of users when users exist")
    void getAllUsers_ShouldReturnListOfUsers_WhenUsersExist() {
        when(userRepository.findAll())
                .thenReturn(List.of(testUser));

        List<UserResponse> response = userService.getAllUsers();

        assertThat(response)
                .isNotNull()
                .isNotEmpty()
                .hasSize(1);
        assertThat(response.get(0).email()).isEqualTo("rahul@test.com");


        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("should return empty list, when users does not exists")
    void getAllUsers_ShouldReturnEmptyList_WhenNoUsersExist() {
        when(userRepository.findAll())
                .thenReturn(List.of());

        List<UserResponse> response = userService.getAllUsers();

        assertThat(response)
                .isNotNull()
                .isEmpty();


        verify(userRepository).findAll();
    }
}
