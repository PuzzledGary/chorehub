package de.caransgar.chorehub.services;

import de.caransgar.chorehub.entity.Chore;
import de.caransgar.chorehub.entity.RecurrenceType;
import de.caransgar.chorehub.entity.User;
import de.caransgar.chorehub.repository.ChoreRepository;
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

    @Autowired
    private ChoreRepository choreRepository;

    @BeforeEach
    void setUp() {
        choreRepository.deleteAll();
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

    @Test
    void testDeleteUserSoftDeletesInsteadOfHardDeleting() {
        User saved = userRepository.save(new User("To Delete", "TD"));

        userService.deleteUser(saved.getId());

        Optional<User> rawUser = userRepository.findById(saved.getId());
        Optional<User> activeUser = userService.getUserById(saved.getId());
        assertThat(rawUser).isPresent();
        assertThat(rawUser.get().isDeleted()).isTrue();
        assertThat(rawUser.get().getDeletedAt()).isNotNull();
        assertThat(activeUser).isEmpty();
    }

    @Test
    void testCleanupDeletedUsersUnassignsChoresAndDeletesUser() {
        User user = userRepository.save(new User("Cleanup User", "CU"));
        Chore chore = new Chore("Cleanup Chore", "desc", RecurrenceType.ONETIME, null, user);
        choreRepository.save(chore);

        userService.deleteUser(user.getId());
        UserService.CleanupDeletedUsersResult result = userService.cleanupDeletedUsers();

        Optional<User> deletedUser = userRepository.findById(user.getId());
        Optional<Chore> updatedChore = choreRepository.findById(chore.getId());
        assertThat(result.deletedUsersFound()).isEqualTo(1);
        assertThat(result.choresUnassigned()).isEqualTo(1);
        assertThat(result.usersDeleted()).isEqualTo(1);
        assertThat(deletedUser).isEmpty();
        assertThat(updatedChore).isPresent();
        assertThat(updatedChore.get().getAssignedUser()).isNull();
    }

}
