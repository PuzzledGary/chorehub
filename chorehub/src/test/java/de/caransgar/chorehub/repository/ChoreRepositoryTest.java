package de.caransgar.chorehub.repository;

import de.caransgar.chorehub.entity.Chore;
import de.caransgar.chorehub.entity.RecurrenceType;
import de.caransgar.chorehub.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ChoreRepositoryTest {

    @Autowired
    private ChoreRepository choreRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testSaveAndFindChore() {
        // Given
        User user = userRepository.save(new User("Test User", "TU"));
        Chore chore = new Chore("Clean Kitchen", "Weekly kitchen cleaning", RecurrenceType.AFTER_COMPLETION, "P1W", user);

        // When
        Chore savedChore = choreRepository.save(chore);

        // Then
        assertThat(savedChore.getId()).isNotNull();
        assertThat(savedChore.getName()).isEqualTo("Clean Kitchen");
        assertThat(savedChore.getRecurrenceType()).isEqualTo(RecurrenceType.AFTER_COMPLETION);
        assertThat(savedChore.getAssignedUser()).isEqualTo(user);
    }

    @Test
    void testFindById() {
        // Given
        User user = userRepository.save(new User("Another User", "AU"));
        Chore chore = new Chore("Wash Dishes", "Daily dish washing", RecurrenceType.ONETIME, null, user);
        Chore savedChore = choreRepository.save(chore);

        // When
        Optional<Chore> foundChore = choreRepository.findById(savedChore.getId());

        // Then
        assertThat(foundChore).isPresent();
        assertThat(foundChore.get().getName()).isEqualTo("Wash Dishes");
    }

    @Test
    void testFindByAssignedUser() {
        // Given
        User user1 = userRepository.save(new User("User1", "U1"));
        User user2 = userRepository.save(new User("User2", "U2"));
        Chore chore1 = new Chore("Mow Lawn", "Monthly lawn mowing", RecurrenceType.FIXED_SCHEDULE, "0 0 1 * *", user1);
        Chore chore2 = new Chore("Vacuum", "Weekly vacuuming", RecurrenceType.AFTER_COMPLETION, "P1W", user1);
        Chore chore3 = new Chore("Grocery Shopping", "Weekly shopping", RecurrenceType.AFTER_COMPLETION, "P1W", user2);
        choreRepository.save(chore1);
        choreRepository.save(chore2);
        choreRepository.save(chore3);

        // When
        List<Chore> choresForUser1 = choreRepository.findByAssignedUser(user1);

        // Then
        assertThat(choresForUser1).hasSize(2);
        assertThat(choresForUser1).extracting(Chore::getName).contains("Mow Lawn", "Vacuum");
    }

    @Test
    void testFindByRecurrenceType() {
        // Given
        User user1 = userRepository.save(new User("User1", "U1"));
        User user2 = userRepository.save(new User("User2", "U2"));
        Chore chore1 = new Chore("Clean Bathroom", "Monthly cleaning", RecurrenceType.FIXED_SCHEDULE, "0 0 1 * *", user1);
        Chore chore2 = new Chore("Take out trash", "Weekly task", RecurrenceType.AFTER_COMPLETION, "P1W", user1);
        Chore chore3 = new Chore("Fix sink", "One-time repair", RecurrenceType.ONETIME, null, user2);
        choreRepository.save(chore1);
        choreRepository.save(chore2);
        choreRepository.save(chore3);
        choreRepository.save(chore1);
        choreRepository.save(chore2);
        choreRepository.save(chore3);

        // When
        List<Chore> fixedChores = choreRepository.findByRecurrenceType(RecurrenceType.FIXED_SCHEDULE);
        List<Chore> flexibleChores = choreRepository.findByRecurrenceType(RecurrenceType.AFTER_COMPLETION);
        List<Chore> onetimeChores = choreRepository.findByRecurrenceType(RecurrenceType.ONETIME);

        // Then
        assertThat(fixedChores).hasSize(1);
        assertThat(flexibleChores).hasSize(1);
        assertThat(onetimeChores).hasSize(1);
    }

    @Test
    void testDeleteChore() {
        // Given
        User user = userRepository.save(new User("Delete User", "DU"));
        Chore chore = new Chore("Test Chore", "For deletion test", RecurrenceType.ONETIME, null, user);
        Chore savedChore = choreRepository.save(chore);

        // When
        choreRepository.deleteById(savedChore.getId());

        // Then
        Optional<Chore> deletedChore = choreRepository.findById(savedChore.getId());
        assertThat(deletedChore).isNotPresent();
    }
}