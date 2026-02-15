package de.caransgar.chorehub.services;

import de.caransgar.chorehub.entity.User;
import de.caransgar.chorehub.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void testGetUserByName() {
        // Given
        User user = userRepository.save(new User("John Doe", "JD"));

        // When
        Optional<User> result = userService.getUserByName("John Doe");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("John Doe");
        assertThat(result.get().getShortname()).isEqualTo("JD");
    }

    @Test
    void testGetUserByNameNotFound() {
        // Given - User does not exist

        // When
        Optional<User> result = userService.getUserByName("NonExistent");

        // Then
        assertThat(result).isNotPresent();
    }

    @Test
    void testGetUserByNameWithMultipleUsers() {
        // Given
        userRepository.save(new User("Alice", "A"));
        userRepository.save(new User("Bob", "B"));
        userRepository.save(new User("Charlie", "C"));

        // When
        Optional<User> result = userService.getUserByName("Bob");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Bob");
    }

    @Test
    void testGetUserByNameCaseSensitive() {
        // Given
        userRepository.save(new User("John", "J"));

        // When - Wrong case
        Optional<User> result = userService.getUserByName("john");

        // Then - Should not find (case sensitive)
        assertThat(result).isEmpty();
    }

}
