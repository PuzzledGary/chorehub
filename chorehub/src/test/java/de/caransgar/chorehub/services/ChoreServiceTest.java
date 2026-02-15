package de.caransgar.chorehub.services;

import de.caransgar.chorehub.dto.ChoreDTO;
import de.caransgar.chorehub.dto.CreateChoreRequest;
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

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class ChoreServiceTest {

    @Autowired
    private ChoreService choreService;

    @Autowired
    private UserService userService;

    @Autowired
    private ChoreRepository choreRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Clear existing data
        choreRepository.deleteAll();
        userRepository.deleteAll();

        // Create a test user
        testUser = userRepository.save(new User("John Doe", "JD"));
    }

    // ========== Valid Creation Tests ==========

    @Test
    void testCreateChoreWithAllFields() {
        // Given
        CreateChoreRequest request = new CreateChoreRequest(
                "Clean Kitchen",
                "Weekly kitchen cleaning",
                RecurrenceType.AFTER_COMPLETION,
                "P7D", // 7 days instead of 1 week which is not supported by Duration.parse()
                testUser.getName());

        // When
        ChoreDTO result = choreService.createChore(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Clean Kitchen");
        assertThat(result.getDescription()).isEqualTo("Weekly kitchen cleaning");
        assertThat(result.getRecurrenceType()).isEqualTo(RecurrenceType.AFTER_COMPLETION);
        assertThat(result.getRecurrencePattern()).isEqualTo("P7D");
        assertThat(result.getAssignedUsername()).isEqualTo("John Doe");
        assertThat(result.getCreatedDate()).isNotNull();
    }

    @Test
    void testCreateChoreWithoutDescription() {
        // Given
        CreateChoreRequest request = new CreateChoreRequest(
                "Vacuum",
                null,
                RecurrenceType.ONETIME,
                null,
                testUser.getName());

        // When
        ChoreDTO result = choreService.createChore(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Vacuum");
        assertThat(result.getDescription()).isNull();
        assertThat(result.getRecurrenceType()).isEqualTo(RecurrenceType.ONETIME);
    }

    @Test
    void testCreateChoreWithoutUsername() {
        // Given
        CreateChoreRequest request = new CreateChoreRequest(
                "Unassigned Task",
                "A task with no assigned user",
                RecurrenceType.ONETIME,
                null,
                null);

        // When
        ChoreDTO result = choreService.createChore(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAssignedUsername()).isNull();
    }

    @Test
    void testCreateChoreWithFixedScheduleRecurrence() {
        // Given
        CreateChoreRequest request = new CreateChoreRequest(
                "Monthly Report",
                "Generate monthly report",
                RecurrenceType.FIXED_SCHEDULE,
                "0 0 0 1 * *", // First day of every month at midnight
                testUser.getName());

        // When
        ChoreDTO result = choreService.createChore(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRecurrenceType()).isEqualTo(RecurrenceType.FIXED_SCHEDULE);
        assertThat(result.getRecurrencePattern()).isEqualTo("0 0 0 1 * *");
    }

    @Test
    void testCreateChoreIsPersisted() {
        // Given
        CreateChoreRequest request = new CreateChoreRequest(
                "Test Persistence",
                "Verify data persistence",
                RecurrenceType.ONETIME,
                null,
                null);

        // When
        ChoreDTO result = choreService.createChore(request);

        // Then - Verify it was actually saved
        var savedChore = choreRepository.findById(result.getId());
        assertThat(savedChore).isPresent();
        assertThat(savedChore.get().getName()).isEqualTo("Test Persistence");
    }

    // ========== Validation Tests ==========

    @Test
    void testCreateChoreWithEmptyName() {
        // Given
        CreateChoreRequest request = new CreateChoreRequest(
                "",
                "Description",
                RecurrenceType.ONETIME,
                null,
                null);

        // Then - Should throw exception
        assertThatThrownBy(() -> choreService.createChore(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Chore name cannot be empty");
    }

    @Test
    void testCreateChoreWithNullName() {
        // Given
        CreateChoreRequest request = new CreateChoreRequest(
                null,
                "Description",
                RecurrenceType.ONETIME,
                null,
                null);

        // Then - Should throw exception
        assertThatThrownBy(() -> choreService.createChore(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Chore name cannot be empty");
    }

    @Test
    void testCreateChoreWithNameExceedingMaxLength() {
        // Given - Create a 300 character name (exceeds 255 limit)
        String longName = "a".repeat(300);
        CreateChoreRequest request = new CreateChoreRequest(
                longName,
                "Description",
                RecurrenceType.ONETIME,
                null,
                null);

        // Then - Should throw exception
        assertThatThrownBy(() -> choreService.createChore(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Chore name cannot exceed 255 characters");
    }

    @Test
    void testCreateChoreWithNullRecurrenceType() {
        // Given
        CreateChoreRequest request = new CreateChoreRequest(
                "Valid Name",
                "Description",
                null,
                null,
                null);

        // Then - Should throw exception
        assertThatThrownBy(() -> choreService.createChore(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Recurrence type is required");
    }

    @Test
    void testCreateChoreWithDescriptionExceedingMaxLength() {
        // Given - Create a 1100 character description (exceeds 1000 limit)
        String longDescription = "a".repeat(1100);
        CreateChoreRequest request = new CreateChoreRequest(
                "Valid Name",
                longDescription,
                RecurrenceType.ONETIME,
                null,
                null);

        // Then - Should throw exception
        assertThatThrownBy(() -> choreService.createChore(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Chore description cannot exceed 1000 characters");
    }

    // ========== Recurrence Pattern Validation Tests ==========

    @Test
    void testCreateChoreWithInvalidCronPattern() {
        // Given - Invalid cron expression
        CreateChoreRequest request = new CreateChoreRequest(
                "Invalid Cron",
                "Test",
                RecurrenceType.FIXED_SCHEDULE,
                "invalid cron", // Invalid pattern
                null);

        // Then - Should throw exception
        assertThatThrownBy(() -> choreService.createChore(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid cron pattern");
    }

    @Test
    void testCreateChoreWithMissingCronPattern() {
        // Given - Missing cron pattern for FIXED_SCHEDULE
        CreateChoreRequest request = new CreateChoreRequest(
                "No Cron Pattern",
                "Test",
                RecurrenceType.FIXED_SCHEDULE,
                null,
                null);

        // Then - Should throw exception
        assertThatThrownBy(() -> choreService.createChore(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Recurrence pattern is required for FIXED_SCHEDULE chores");
    }

    @Test
    void testCreateChoreWithInvalidDurationPattern() {
        // Given - Invalid ISO-8601 duration
        CreateChoreRequest request = new CreateChoreRequest(
                "Invalid Duration",
                "Test",
                RecurrenceType.AFTER_COMPLETION,
                "not a duration", // Invalid pattern
                null);

        // Then - Should throw exception
        assertThatThrownBy(() -> choreService.createChore(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid duration pattern");
    }

    @Test
    void testCreateChoreWithMissingDurationPattern() {
        // Given - Missing duration pattern for AFTER_COMPLETION
        CreateChoreRequest request = new CreateChoreRequest(
                "No Duration Pattern",
                "Test",
                RecurrenceType.AFTER_COMPLETION,
                null,
                null);

        // Then - Should throw exception
        assertThatThrownBy(() -> choreService.createChore(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Recurrence pattern is required for AFTER_COMPLETION chores");
    }

    @Test
    void testCreateChoreWithOneTimeAndPattern() {
        // Given - ONETIME chore with pattern (should be invalid)
        CreateChoreRequest request = new CreateChoreRequest(
                "One Time Task",
                "Test",
                RecurrenceType.ONETIME,
                "P1D", // Pattern not allowed for ONETIME
                null);

        // Then - Should throw exception
        assertThatThrownBy(() -> choreService.createChore(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Recurrence pattern should not be set for ONETIME chores");
    }

    // ========== User Resolution Tests ==========

    @Test
    void testCreateChoreWithNonExistentUsername() {
        // Given - Username that doesn't exist
        CreateChoreRequest request = new CreateChoreRequest(
                "Valid Name",
                "Test",
                RecurrenceType.ONETIME,
                null,
                "NonExistent User");

        // Then - Should throw exception
        assertThatThrownBy(() -> choreService.createChore(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User with name 'NonExistent User' not found");
    }

    @Test
    void testCreateChoreWithEmptyUsername() {
        // Given - Empty username string
        CreateChoreRequest request = new CreateChoreRequest(
                "Valid Name",
                "Test",
                RecurrenceType.ONETIME,
                null,
                "   " // Whitespace only
        );

        // When/Then - Empty username should be treated as null (unassigned)
        ChoreDTO result = choreService.createChore(request);
        assertThat(result.getAssignedUsername()).isNull();
    }

    @Test
    void testCreateChoreAndRetrieveDTOMapping() {
        // Given
        CreateChoreRequest request = new CreateChoreRequest(
                "Mapping Test",
                "Test DTO mapping",
                RecurrenceType.AFTER_COMPLETION,
                "P14D", // 14 days instead of 2 weeks
                testUser.getName());

        // When
        ChoreDTO dto = choreService.createChore(request);

        // Then - Verify DTO contains correct mapped values
        assertThat(dto.getName()).isEqualTo("Mapping Test");
        assertThat(dto.getDescription()).isEqualTo("Test DTO mapping");
        assertThat(dto.getRecurrenceType()).isEqualTo(RecurrenceType.AFTER_COMPLETION);
        assertThat(dto.getRecurrencePattern()).isEqualTo("P14D");
        assertThat(dto.getAssignedUsername()).isEqualTo("John Doe");
        assertThat(dto.getId()).isPositive();
        assertThat(dto.getCreatedDate()).isNotNull();
        assertThat(dto.getLastCompletedDate()).isNull();
        assertThat(dto.getNextDueDate()).isNotNull();
    }

    // ========== Edge Case Tests ==========

    @Test
    void testCreateChoreWithMaxLengthName() {
        // Given - Name with exactly 255 characters
        String maxName = "a".repeat(255);
        CreateChoreRequest request = new CreateChoreRequest(
                maxName,
                "Test",
                RecurrenceType.ONETIME,
                null,
                null);

        // When/Then - Should succeed
        ChoreDTO result = choreService.createChore(request);
        assertThat(result.getName()).isEqualTo(maxName);
    }

    @Test
    void testCreateChoreWithMaxLengthDescription() {
        // Given - Description with exactly 1000 characters
        String maxDescription = "a".repeat(1000);
        CreateChoreRequest request = new CreateChoreRequest(
                "Test Chore",
                maxDescription,
                RecurrenceType.ONETIME,
                null,
                null);

        // When/Then - Should succeed
        ChoreDTO result = choreService.createChore(request);
        assertThat(result.getDescription()).isEqualTo(maxDescription);
    }

    @Test
    void testCreateMultipleChoresToSameUser() {
        // Given
        CreateChoreRequest request1 = new CreateChoreRequest(
                "Task 1",
                "First task",
                RecurrenceType.ONETIME,
                null,
                testUser.getName());
        CreateChoreRequest request2 = new CreateChoreRequest(
                "Task 2",
                "Second task",
                RecurrenceType.ONETIME,
                null,
                testUser.getName());

        // When
        ChoreDTO result1 = choreService.createChore(request1);
        ChoreDTO result2 = choreService.createChore(request2);

        // Then
        assertThat(result1.getId()).isNotEqualTo(result2.getId());
        assertThat(result1.getAssignedUsername()).isEqualTo(result2.getAssignedUsername());
    }

    // ========== getDueChores Tests ==========

    @Test
    void testGetDueChoresReturnsEmptyListWhenNoChoresToday() {
        // Given - No chores in repository
        choreRepository.deleteAll();

        // When
        var dueChores = choreService.getDueChores();

        // Then
        assertThat(dueChores).isEmpty();
    }

    @Test
    void testGetDueChoresReturnsChoresToday() {
        // Given
        User user = userRepository.save(new User("Bob Smith", "BS"));

        Chore dueChore = new Chore(
                "Clean Kitchen",
                "Due today",
                RecurrenceType.ONETIME,
                null,
                user);
        dueChore.setNextDueDate(java.time.LocalDateTime.now().minusHours(1)); // Due 1 hour ago
        choreRepository.save(dueChore);

        Chore futureChore = new Chore(
                "Paint House",
                "Due later",
                RecurrenceType.ONETIME,
                null,
                user);
        futureChore.setNextDueDate(java.time.LocalDateTime.now().plusDays(5)); // Due in 5 days
        choreRepository.save(futureChore);

        // When
        var dueChores = choreService.getDueChores();

        // Then
        assertThat(dueChores).hasSize(1);
        assertThat(dueChores).contains(dueChore);
        assertThat(dueChores).doesNotContain(futureChore);
    }

    @Test
    void testGetDueChoresReturnsBothDueAndOverdueChores() {
        // Given
        User user = userRepository.save(new User("Clara Johnson", "CJ"));

        Chore overdueChore = new Chore(
                "Fix Roof",
                "Overdue",
                RecurrenceType.ONETIME,
                null,
                user);
        overdueChore.setNextDueDate(java.time.LocalDateTime.now().minusDays(2)); // Overdue 2 days
        choreRepository.save(overdueChore);

        Chore dueNowChore = new Chore(
                "Mow Lawn",
                "Due now",
                RecurrenceType.ONETIME,
                null,
                user);
        dueNowChore.setNextDueDate(java.time.LocalDateTime.now().minusMinutes(30)); // Due 30 mins ago
        choreRepository.save(dueNowChore);

        // When
        var dueChores = choreService.getDueChores();

        // Then
        assertThat(dueChores).hasSize(2);
        assertThat(dueChores).containsExactlyInAnyOrder(overdueChore, dueNowChore);
    }

    // ========== getDueChores(User) Tests ==========

    @Test
    void testGetDueChoresForUserReturnsEmptyWhenNoChoresForUser() {
        // Given
        User alice = userRepository.save(new User("Alice", "A"));
        User bob = userRepository.save(new User("Bob", "B"));

        Chore bobsChore = new Chore(
                "Bob's Task",
                "Bob's chore",
                RecurrenceType.ONETIME,
                null,
                bob);
        bobsChore.setNextDueDate(java.time.LocalDateTime.now().minusHours(1));
        choreRepository.save(bobsChore);

        // When
        var alicesDue = choreService.getDueChores(alice);

        // Then
        assertThat(alicesDue).isEmpty();
    }

    @Test
    void testGetDueChoresForUserReturnOnlyUsersDueChores() {
        // Given
        User alice = userRepository.save(new User("Alice", "A"));
        User bob = userRepository.save(new User("Bob", "B"));

        Chore aliceDueChore = new Chore(
                "Alice's Due Task",
                "Assigned to Alice",
                RecurrenceType.ONETIME,
                null,
                alice);
        aliceDueChore.setNextDueDate(java.time.LocalDateTime.now().minusHours(1));
        choreRepository.save(aliceDueChore);

        Chore bobDueChore = new Chore(
                "Bob's Due Task",
                "Assigned to Bob",
                RecurrenceType.ONETIME,
                null,
                bob);
        bobDueChore.setNextDueDate(java.time.LocalDateTime.now().minusHours(1));
        choreRepository.save(bobDueChore);

        Chore aliceFutureChore = new Chore(
                "Alice's Future Task",
                "Future for Alice",
                RecurrenceType.ONETIME,
                null,
                alice);
        aliceFutureChore.setNextDueDate(java.time.LocalDateTime.now().plusDays(3));
        choreRepository.save(aliceFutureChore);

        // When
        var alicesDue = choreService.getDueChores(alice);

        // Then
        assertThat(alicesDue).hasSize(1);
        assertThat(alicesDue).contains(aliceDueChore);
        assertThat(alicesDue).doesNotContain(bobDueChore);
        assertThat(alicesDue).doesNotContain(aliceFutureChore);
    }

    @Test
    void testGetDueChoresForUserWithMultipleDueChores() {
        // Given
        User user = userRepository.save(new User("David", "D"));

        Chore chore1 = new Chore("Task 1", "First due task", RecurrenceType.ONETIME, null, user);
        chore1.setNextDueDate(java.time.LocalDateTime.now().minusHours(2));
        choreRepository.save(chore1);

        Chore chore2 = new Chore("Task 2", "Second due task", RecurrenceType.ONETIME, null, user);
        chore2.setNextDueDate(java.time.LocalDateTime.now().minusMinutes(30));
        choreRepository.save(chore2);

        Chore chore3 = new Chore("Task 3", "Future task", RecurrenceType.ONETIME, null, user);
        chore3.setNextDueDate(java.time.LocalDateTime.now().plusDays(2));
        choreRepository.save(chore3);

        // When
        var usersDueChores = choreService.getDueChores(user);

        // Then
        assertThat(usersDueChores).hasSize(2);
        assertThat(usersDueChores).containsExactlyInAnyOrder(chore1, chore2);
        assertThat(usersDueChores).doesNotContain(chore3);
    }

    @Test
    void testGetDueChoresForUserIgnoresUnassignedChores() {
        // Given
        User assignedUser = userRepository.save(new User("Eve", "E"));

        Chore assignedDueChore = new Chore(
                "Assigned Task",
                "Assigned chore",
                RecurrenceType.ONETIME,
                null,
                assignedUser);
        assignedDueChore.setNextDueDate(java.time.LocalDateTime.now().minusHours(1));
        choreRepository.save(assignedDueChore);

        Chore unassignedDueChore = new Chore(
                "Unassigned Task",
                "No assignment",
                RecurrenceType.ONETIME,
                null,
                null); // No assigned user
        unassignedDueChore.setNextDueDate(java.time.LocalDateTime.now().minusHours(1));
        choreRepository.save(unassignedDueChore);

        // When
        var assignedUsersDue = choreService.getDueChores(assignedUser);

        // Then
        assertThat(assignedUsersDue).hasSize(1);
        assertThat(assignedUsersDue).contains(assignedDueChore);
        assertThat(assignedUsersDue).doesNotContain(unassignedDueChore);
    }

}
