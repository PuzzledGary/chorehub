package de.caransgar.chorehub.controller;

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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class UserControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChoreRepository choreRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        choreRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testSoftDeleteUserEndpoint() throws Exception {
        User user = userRepository.save(new User("Delete Me", "DM"));

        mockMvc.perform(delete("/users/" + user.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        Optional<User> persisted = userRepository.findById(user.getId());
        assertThat(persisted).isPresent();
        assertThat(persisted.get().isDeleted()).isTrue();
        assertThat(persisted.get().getDeletedAt()).isNotNull();
    }

    @Test
    void testCleanupDeletedUsersEndpoint() throws Exception {
        User user = userRepository.save(new User("Cleanup", "CL"));
        Chore chore = choreRepository.save(new Chore("Task", "desc", RecurrenceType.ONETIME, null, user));
        user.setDeleted(true);
        userRepository.save(user);

        mockMvc.perform(post("/users/cleanup-soft-deleted")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deletedUsersFound").value(1))
                .andExpect(jsonPath("$.choresUnassigned").value(1))
                .andExpect(jsonPath("$.usersDeleted").value(1));

        assertThat(userRepository.findById(user.getId())).isEmpty();
        Optional<Chore> updatedChore = choreRepository.findById(chore.getId());
        assertThat(updatedChore).isPresent();
        assertThat(updatedChore.get().getAssignedUser()).isNull();
    }
}
