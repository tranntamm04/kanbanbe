package com.example.login;

import com.example.config.JwtTokenUtil;
import com.example.dto.JwtResponse;
import com.example.dto.LoginRequest;
import com.example.dto.RegisterRequest;
import com.example.entity.Role;
import com.example.entity.User;
import com.example.exception.AppException;
import com.example.repository.RoleRepository;
import com.example.repository.UserRepository;
import com.example.service.EmailService;
import com.example.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenUtil jwtTokenUtil;
    @Mock private EmailService emailService;

    private AuthServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AuthServiceImpl(
                userRepository,
                roleRepository,
                passwordEncoder,
                jwtTokenUtil,
                emailService
        );
    }

    @Test
    void registerNormalizesInputEncodesPasswordAndAssignsUserRole() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername(" alice ");
        request.setEmail(" Alice@Test.Dev ");
        request.setPassword("plain-password");
        Role userRole = Role.builder().id(1L).name("ROLE_USER").build();

        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@test.dev")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");

        service.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertThat(saved.getUsername()).isEqualTo("alice");
        assertThat(saved.getEmail()).isEqualTo("alice@test.dev");
        assertThat(saved.getPassword()).isEqualTo("encoded-password");
        assertThat(saved.getEnabled()).isTrue();
        assertThat(saved.getRoles()).containsExactly(userRole);
    }

    @Test
    void registerRejectsDuplicateUsername() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("alice");
        request.setEmail("alice@test.dev");
        request.setPassword("plain-password");

        when(userRepository.existsByUsername("alice")).thenReturn(true);

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(AppException.class)
                .hasMessage("USERNAME_EXISTS");

        verify(userRepository, never()).save(any());
    }

    @Test
    void loginReturnsJwtWhenPasswordMatches() {
        LoginRequest request = new LoginRequest();
        request.setUsername(" alice ");
        request.setPassword("plain-password");
        User user = User.builder()
                .id(10L)
                .username("alice")
                .password("encoded-password")
                .roles(Set.of(Role.builder().name("ROLE_USER").build()))
                .build();

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("plain-password", "encoded-password")).thenReturn(true);
        when(jwtTokenUtil.generateJwtToken("alice")).thenReturn("jwt-token");

        JwtResponse response = service.login(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUsername()).isEqualTo("alice");
    }

    @Test
    void loginRejectsWrongPassword() {
        LoginRequest request = new LoginRequest();
        request.setUsername("alice");
        request.setPassword("bad-password");
        User user = User.builder().username("alice").password("encoded-password").build();

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("bad-password", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> service.login(request))
                .isInstanceOf(AppException.class)
                .hasMessage("WRONG_PASSWORD");

        verify(jwtTokenUtil, never()).generateJwtToken(any());
    }

    @Test
    void forgotPasswordNormalizesEmailAndSendsOtp() {
        when(userRepository.findByEmail("alice@test.dev"))
                .thenReturn(Optional.of(User.builder().email("alice@test.dev").build()));

        service.forgotPassword(" Alice@Test.Dev ");

        verify(emailService).sendOtpEmail(eq("alice@test.dev"), matches("\\d{6}"));
    }

    @Test
    void resetPasswordRejectsInvalidOtp() {
        assertThatThrownBy(() -> service.resetPassword("alice@test.dev", "000000", "new-password"))
                .isInstanceOf(AppException.class)
                .hasMessage("INVALID_OTP");

        verify(userRepository, never()).save(any());
    }
}
