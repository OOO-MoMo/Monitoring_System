package ru.momo.monitoring.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import ru.momo.monitoring.exceptions.ResourceNotFoundException;
import ru.momo.monitoring.services.UserService;
import ru.momo.monitoring.services.impl.SecurityServiceImpl;
import ru.momo.monitoring.store.entities.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private SecurityServiceImpl securityService;

    private User testUser;
    private final String testEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail(testEmail);

        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUser_WhenAuthenticatedWithUserDetails_ShouldReturnUser() {
        // Arrange
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(testEmail);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userService.getByEmail(testEmail)).thenReturn(testUser);

        // Act
        User currentUser = securityService.getCurrentUser();

        // Assert
        assertNotNull(currentUser);
        assertEquals(testEmail, currentUser.getEmail());
        verify(userService).getByEmail(testEmail);
    }

    @Test
    void getCurrentUser_WhenAuthenticatedWithStringPrincipal_ShouldReturnUser() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(testEmail);
        when(userService.getByEmail(testEmail)).thenReturn(testUser);

        // Act
        User currentUser = securityService.getCurrentUser();

        // Assert
        assertNotNull(currentUser);
        assertEquals(testEmail, currentUser.getEmail());
        verify(userService).getByEmail(testEmail);
    }

    @Test
    void getCurrentUser_WhenNoAuthentication_ShouldThrowIllegalStateException() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(null);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> securityService.getCurrentUser());
        assertEquals("No authenticated user found in Security Context.", exception.getMessage());
        verify(userService, never()).getByEmail(anyString());
    }

    @Test
    void getCurrentUser_WhenNotAuthenticated_ShouldThrowIllegalStateException() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> securityService.getCurrentUser());
        assertEquals("No authenticated user found in Security Context.", exception.getMessage());
        verify(userService, never()).getByEmail(anyString());
    }

    @Test
    void getCurrentUser_WhenPrincipalIsAnonymousUser_ShouldThrowIllegalStateException() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> securityService.getCurrentUser());
        assertEquals("No authenticated user found in Security Context.", exception.getMessage());
        verify(userService, never()).getByEmail(anyString());
    }

    @Test
    void getCurrentUser_WhenPrincipalIsUnexpectedType_ShouldThrowClassCastException() {
        // Arrange
        Object unexpectedPrincipal = new Object();
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(unexpectedPrincipal);

        // Act & Assert
        ClassCastException exception = assertThrows(ClassCastException.class,
                () -> securityService.getCurrentUser());
        assertTrue(exception.getMessage().contains("Unexpected principal type found in Security Context:"));
        verify(userService, never()).getByEmail(anyString());
    }

    @Test
    void getCurrentUser_WhenUserServiceThrowsNotFound_ShouldPropagateException() {
        // Arrange
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(testEmail);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userService.getByEmail(testEmail)).thenThrow(new ResourceNotFoundException("User not found"));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> securityService.getCurrentUser());
        verify(userService).getByEmail(testEmail);
    }

}