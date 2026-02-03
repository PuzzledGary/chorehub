package de.caransgar.chorehub.repository;

import de.caransgar.chorehub.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testSaveAndFindUser() {
        // Given
        User user = new User("John Doe", "JD");

        // When
        User savedUser = userRepository.save(user);

        // Then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getName()).isEqualTo("John Doe");
        assertThat(savedUser.getShortname()).isEqualTo("JD");
    }

    @Test
    void testFindById() {
        // Given
        User user = new User("Jane Smith", null);
        User savedUser = userRepository.save(user);

        // When
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("Jane Smith");
        assertThat(foundUser.get().getShortname()).isNull();
    }

    @Test
    void testFindByName() {
        // Given
        User user = new User("Alice", "A");
        userRepository.save(user);

        // When
        Optional<User> foundUser = userRepository.findByName("Alice");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getShortname()).isEqualTo("A");
    }

    @Test
    void testFindByShortname() {
        // Given
        User user = new User("Bob", "Bobby");
        userRepository.save(user);

        // When
        Optional<User> foundUser = userRepository.findByShortname("Bobby");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("Bob");
    }

    @Test
    void testDeleteUser() {
        // Given
        User user = new User("Test User", "TU");
        User savedUser = userRepository.save(user);

        // When
        userRepository.deleteById(savedUser.getId());

        // Then
        Optional<User> deletedUser = userRepository.findById(savedUser.getId());
        assertThat(deletedUser).isNotPresent();
    }
}