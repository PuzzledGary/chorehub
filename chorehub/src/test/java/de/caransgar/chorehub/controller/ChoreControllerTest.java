package de.caransgar.chorehub.controller;

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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class ChoreControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChoreRepository choreRepository;

    private MockMvc mockMvc;
    private User testUser;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        choreRepository.deleteAll();
        userRepository.deleteAll();
        testUser = userRepository.save(new User("Alice Johnson", "AJ"));
    }

    // ========== Valid Request Tests ==========

    @Test
    void testCreateChoreWithValidRequest() throws Exception {
        // Given
        CreateChoreRequest request = new CreateChoreRequest(
                "Clean Bathroom",
                "Weekly bathroom cleaning",
                RecurrenceType.AFTER_COMPLETION,
                "P7D",
                testUser.getName());

        // When/Then
        mockMvc.perform(post("/chores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Clean Bathroom"))
                .andExpect(jsonPath("$.description").value("Weekly bathroom cleaning"))
                .andExpect(jsonPath("$.recurrenceType").value("AFTER_COMPLETION"))
                .andExpect(jsonPath("$.recurrencePattern").value("P7D"))
                .andExpect(jsonPath("$.assignedUsername").value("Alice Johnson"))
                .andExpect(jsonPath("$.createdDate").isNotEmpty());
    }

    @Test
    void testCreateChoreWithMinimalFields() throws Exception {
        // Given
        CreateChoreRequest request = new CreateChoreRequest(
                "Simple Task",
                null,
                RecurrenceType.ONETIME,
                null,
                null);

        // When/Then
        mockMvc.perform(post("/chores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Simple Task"))
                .andExpect(jsonPath("$.recurrenceType").value("ONETIME"))
                .andExpect(jsonPath("$.assignedUsername").value(nullValue()));
    }

    @Test
    void testCreateChoreWithFixedSchedule() throws Exception {
        // Given
        CreateChoreRequest request = new CreateChoreRequest(
                "Monthly Meeting",
                "First Monday of every month",
                RecurrenceType.FIXED_SCHEDULE,
                "0 0 10 1 * MON",
                testUser.getName());

        // When/Then
        mockMvc.perform(post("/chores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.recurrenceType").value("FIXED_SCHEDULE"))
                .andExpect(jsonPath("$.recurrencePattern").value("0 0 10 1 * MON"));
    }

    @Test
    void testCreateChoreWithoutUser() throws Exception {
        // Given
        CreateChoreRequest request = new CreateChoreRequest(
                "Unassigned Chore",
                "No user assigned",
                RecurrenceType.ONETIME,
                null,
                null);

        // When/Then
        mockMvc.perform(post("/chores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.assignedUsername").value(nullValue()));
    }

    // ========== Error Response Tests ==========

    @Test
    void testCreateChoreWithNullRequest() throws Exception {
        // When/Then
        mockMvc.perform(post("/chores")
                .contentType(MediaType.APPLICATION_JSON)
                .content("null"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request body cannot be null"));
    }

    @Test
    void testCreateChoreWithoutName() throws Exception {
        // Given - Request without name field
        String requestJson = "{\"recurrenceType\": \"ONETIME\"}";

        // When/Then
        mockMvc.perform(post("/chores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Chore name is required"));
    }

    @Test
    void testCreateChoreWithEmptyName() throws Exception {
        // Given
        CreateChoreRequest request = new CreateChoreRequest(
                "",
                "Test",
                RecurrenceType.ONETIME,
                null,
                null);

        // When/Then
        mockMvc.perform(post("/chores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Chore name cannot be empty"));
    }

    @Test
    void testCreateChoreWithoutRecurrenceType() throws Exception {
        // Given - Request without recurrence type
        String requestJson = "{\"name\": \"Test Task\"}";

        // When/Then
        mockMvc.perform(post("/chores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Recurrence type is required"));
    }

    @Test
    void testCreateChoreWithNameExceedingLimit() throws Exception {
        // Given - Name exceeding 255 characters
        String longName = "a".repeat(300);
        CreateChoreRequest request = new CreateChoreRequest(
                longName,
                "Test",
                RecurrenceType.ONETIME,
                null,
                null);

        // When/Then
        mockMvc.perform(post("/chores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Chore name cannot exceed 255 characters")));
    }

    @Test
    void testCreateChoreWithInvalidCronPattern() throws Exception {
        // Given - Invalid cron expression
        CreateChoreRequest request = new CreateChoreRequest(
                "Invalid Cron",
                "Test",
                RecurrenceType.FIXED_SCHEDULE,
                "not valid cron",
                null);

        // When/Then
        mockMvc.perform(post("/chores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Invalid cron pattern")));
    }

    @Test
    void testCreateChoreWithMissingCronPattern() throws Exception {
        // Given - FIXED_SCHEDULE without cron pattern
        CreateChoreRequest request = new CreateChoreRequest(
                "No Cron",
                "Test",
                RecurrenceType.FIXED_SCHEDULE,
                null,
                null);

        // When/Then
        mockMvc.perform(post("/chores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(containsString("Recurrence pattern is required for FIXED_SCHEDULE")));
    }

    @Test
    void testCreateChoreWithInvalidDurationPattern() throws Exception {
        // Given - Invalid ISO-8601 duration
        CreateChoreRequest request = new CreateChoreRequest(
                "Invalid Duration",
                "Test",
                RecurrenceType.AFTER_COMPLETION,
                "not a duration",
                null);

        // When/Then
        mockMvc.perform(post("/chores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Invalid duration pattern")));
    }

    @Test
    void testCreateChoreWithMissingDurationPattern() throws Exception {
        // Given - AFTER_COMPLETION without duration pattern
        CreateChoreRequest request = new CreateChoreRequest(
                "No Duration",
                "Test",
                RecurrenceType.AFTER_COMPLETION,
                null,
                null);

        // When/Then
        mockMvc.perform(post("/chores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(containsString("Recurrence pattern is required for AFTER_COMPLETION")));
    }

    @Test
    void testCreateChoreWithOneTimeAndPattern() throws Exception {
        // Given - ONETIME with a pattern (invalid)
        CreateChoreRequest request = new CreateChoreRequest(
                "One Time",
                "Test",
                RecurrenceType.ONETIME,
                "P1D",
                null);

        // When/Then
        mockMvc.perform(post("/chores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(containsString("Recurrence pattern should not be set for ONETIME")));
    }

    @Test
    void testCreateChoreWithNonExistentUser() throws Exception {
        // Given - Non-existent user
        CreateChoreRequest request = new CreateChoreRequest(
                "Assigned to Nobody",
                "Test",
                RecurrenceType.ONETIME,
                null,
                "NonExistent User");

        // When/Then
        mockMvc.perform(post("/chores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("User with name 'NonExistent User' not found")));
    }

    @Test
    void testCreateChoreWithDescriptionExceedingLimit() throws Exception {
        // Given - Description exceeding 1000 characters
        String longDescription = "a".repeat(1100);
        CreateChoreRequest request = new CreateChoreRequest(
                "Test Chore",
                longDescription,
                RecurrenceType.ONETIME,
                null,
                null);

        // When/Then
        mockMvc.perform(post("/chores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Chore description cannot exceed 1000")));
    }

    // ========== Data Persistence Tests ==========

    @Test
    void testCreateChoreIsPersistedInDatabase() throws Exception {
        // Given
        CreateChoreRequest request = new CreateChoreRequest(
                "Persistence Test",
                "Verify database save",
                RecurrenceType.ONETIME,
                null,
                null);

        // When
        mockMvc.perform(post("/chores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Then - Verify data was saved
        assertThat(choreRepository.findAll()).isNotEmpty();
        var savedChore = choreRepository.findAll().get(0);
        assertThat(savedChore.getName()).isEqualTo("Persistence Test");
    }

    @Test
    void testCreateMultipleChores() throws Exception {
        // Given & When
        for (int i = 1; i <= 3; i++) {
            CreateChoreRequest request = new CreateChoreRequest(
                    "Task " + i,
                    "Task number " + i,
                    RecurrenceType.ONETIME,
                    null,
                    null);

            mockMvc.perform(post("/chores")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        // Then - Verify all were saved
        assertThat(choreRepository.findAll()).hasSize(3);
    }

    // ========== Content Type Tests ==========

    @Test
    void testCreateChoreWithInvalidContentType() throws Exception {
        // Given
        CreateChoreRequest request = new CreateChoreRequest(
                "Test",
                "Test",
                RecurrenceType.ONETIME,
                null,
                null);

        // When/Then - Should handle invalid content type
        mockMvc.perform(post("/chores")
                .contentType(MediaType.TEXT_PLAIN)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnsupportedMediaType());
    }

    // ========== Get Due Chores Tests ==========

    @Test
    void testGetDueChoresReturnsEmptyListWhenNoDueChores() throws Exception {
        // Given - No due chores
        choreRepository.deleteAll();

        // When/Then
        mockMvc.perform(get("/chores/due")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testGetDueChoresReturnsDueChores() throws Exception {
        // Given
        User user = userRepository.save(new User("Frank Wilson", "FW"));

        Chore dueChore = new Chore(
                "Clean Windows",
                "Due now",
                RecurrenceType.ONETIME,
                null,
                user);
        dueChore.setNextDueDate(java.time.LocalDateTime.now().minusHours(1));
        choreRepository.save(dueChore);

        Chore futureChore = new Chore(
                "Repaint Walls",
                "Due later",
                RecurrenceType.ONETIME,
                null,
                user);
        futureChore.setNextDueDate(java.time.LocalDateTime.now().plusDays(5));
        choreRepository.save(futureChore);

        // When/Then
        mockMvc.perform(get("/chores/due")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Clean Windows"))
                .andExpect(jsonPath("$[0].assignedUsername").value(user.getName()));
    }

    @Test
    void testGetDueChoresReturnsMultipleDueChores() throws Exception {
        // Given
        User user = userRepository.save(new User("Grace Lee", "GL"));

        Chore chore1 = new Chore("Task 1", "First due", RecurrenceType.ONETIME, null, user);
        chore1.setNextDueDate(java.time.LocalDateTime.now().minusHours(2));
        choreRepository.save(chore1);

        Chore chore2 = new Chore("Task 2", "Second due", RecurrenceType.ONETIME, null, user);
        chore2.setNextDueDate(java.time.LocalDateTime.now().minusMinutes(30));
        choreRepository.save(chore2);

        Chore chore3 = new Chore("Task 3", "Future", RecurrenceType.ONETIME, null, user);
        chore3.setNextDueDate(java.time.LocalDateTime.now().plusDays(3));
        choreRepository.save(chore3);

        // When/Then
        mockMvc.perform(get("/chores/due")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Task 1", "Task 2")));
    }

    // ========== Get User Due Chores Tests ==========

    @Test
    void testGetUserDueChoresReturnsEmptyWhenNoUserChores() throws Exception {
        // Given
        User alice = userRepository.save(new User("Helen Anderson", "HA"));
        User bob = userRepository.save(new User("Ian Miller", "IM"));

        Chore bobsChore = new Chore("Bob's Task", "Bob's chore", RecurrenceType.ONETIME, null, bob);
        bobsChore.setNextDueDate(java.time.LocalDateTime.now().minusHours(1));
        choreRepository.save(bobsChore);

        // When/Then
        mockMvc.perform(get("/chores/due/user/" + alice.getName())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testGetUserDueChoresReturnOnlyUsersDueChores() throws Exception {
        // Given
        User alice = userRepository.save(new User("Jane Doe", "JD"));
        User bob = userRepository.save(new User("Kevin Brown", "KB"));

        Chore aliceDueChore = new Chore("Alice's Due", "For Alice", RecurrenceType.ONETIME, null, alice);
        aliceDueChore.setNextDueDate(java.time.LocalDateTime.now().minusHours(1));
        choreRepository.save(aliceDueChore);

        Chore bobDueChore = new Chore("Bob's Due", "For Bob", RecurrenceType.ONETIME, null, bob);
        bobDueChore.setNextDueDate(java.time.LocalDateTime.now().minusHours(1));
        choreRepository.save(bobDueChore);

        Chore aliceFutureChore = new Chore("Alice's Future", "Future", RecurrenceType.ONETIME, null, alice);
        aliceFutureChore.setNextDueDate(java.time.LocalDateTime.now().plusDays(3));
        choreRepository.save(aliceFutureChore);

        // When/Then
        mockMvc.perform(get("/chores/due/user/" + alice.getName())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Alice's Due"))
                .andExpect(jsonPath("$[0].assignedUsername").value(alice.getName()));
    }

    @Test
    void testGetUserDueChoresWithMultipleDueChores() throws Exception {
        // Given
        User user = userRepository.save(new User("Lisa White", "LW"));

        Chore chore1 = new Chore("Chore 1", "First", RecurrenceType.ONETIME, null, user);
        chore1.setNextDueDate(java.time.LocalDateTime.now().minusHours(2));
        choreRepository.save(chore1);

        Chore chore2 = new Chore("Chore 2", "Second", RecurrenceType.ONETIME, null, user);
        chore2.setNextDueDate(java.time.LocalDateTime.now().minusMinutes(45));
        choreRepository.save(chore2);

        Chore chore3 = new Chore("Chore 3", "Third", RecurrenceType.ONETIME, null, user);
        chore3.setNextDueDate(java.time.LocalDateTime.now().plusDays(2));
        choreRepository.save(chore3);

        // When/Then
        mockMvc.perform(get("/chores/due/user/" + user.getName())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Chore 1", "Chore 2")));
    }

    @Test
    void testGetUserDueChoresReturnsNotFoundForNonexistentUser() throws Exception {
        // When/Then
        mockMvc.perform(get("/chores/due/user/NonexistentUser")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User with name 'NonexistentUser' not found"));
    }

    @Test
    void testGetUserDueChoresIgnoresUnassignedChores() throws Exception {
        // Given
        User assignedUser = userRepository.save(new User("Mike Davis", "MD"));

        Chore assignedDue = new Chore("Assigned", "Assigned chore", RecurrenceType.ONETIME, null, assignedUser);
        assignedDue.setNextDueDate(java.time.LocalDateTime.now().minusHours(1));
        choreRepository.save(assignedDue);

        Chore unassignedDue = new Chore("Unassigned", "No one assigned", RecurrenceType.ONETIME, null, null);
        unassignedDue.setNextDueDate(java.time.LocalDateTime.now().minusHours(1));
        choreRepository.save(unassignedDue);

        // When/Then
        mockMvc.perform(get("/chores/due/user/" + assignedUser.getName())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Assigned"));
    }

}
