package de.caransgar.chorehub.services;

import de.caransgar.chorehub.entity.Chore;
import de.caransgar.chorehub.entity.User;
import de.caransgar.chorehub.repository.ChoreRepository;
import de.caransgar.chorehub.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final ChoreRepository choreRepository;

    public UserService(UserRepository userRepository, ChoreRepository choreRepository) {
        this.userRepository = userRepository;
        this.choreRepository = choreRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findByDeletedFalse();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findByIdAndDeletedFalse(id);
    }

    public User saveUser(User user) {
        if (user.getId() != null) {
            userRepository.findById(user.getId()).ifPresent(existingUser -> {
                user.setDeleted(existingUser.isDeleted());
                user.setDeletedAt(existingUser.getDeletedAt());
            });
        }
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("User with id '" + id + "' not found"));
        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public Optional<User> getUserByName(String name) {
        return userRepository.findByNameAndDeletedFalse(name);
    }

    public CleanupDeletedUsersResult cleanupDeletedUsers() {
        List<User> deletedUsers = userRepository.findByDeletedTrue();
        int totalUsers = deletedUsers.size();
        int totalUnassignedChores = 0;
        int totalDeletedUsers = 0;

        for (User user : deletedUsers) {
            List<Chore> assignedChores = choreRepository.findByAssignedUser(user);
            if (!assignedChores.isEmpty()) {
                for (Chore chore : assignedChores) {
                    chore.setAssignedUser(null);
                }
                choreRepository.saveAll(assignedChores);
                totalUnassignedChores += assignedChores.size();
            }

            boolean hasRemainingChores = choreRepository.countByAssignedUser(user) > 0;
            if (!hasRemainingChores) {
                userRepository.delete(user);
                totalDeletedUsers++;
            }
        }

        return new CleanupDeletedUsersResult(totalUsers, totalUnassignedChores, totalDeletedUsers);
    }

    public record CleanupDeletedUsersResult(int deletedUsersFound, int choresUnassigned, int usersDeleted) {}
}
