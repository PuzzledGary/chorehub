package de.caransgar.chorehub.controller;

import de.caransgar.chorehub.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/cleanup-soft-deleted")
    public ResponseEntity<CleanupDeletedUsersResponse> cleanupDeletedUsers() {
        UserService.CleanupDeletedUsersResult result = userService.cleanupDeletedUsers();
        return ResponseEntity.ok(new CleanupDeletedUsersResponse(
                result.deletedUsersFound(),
                result.choresUnassigned(),
                result.usersDeleted()));
    }

    public record CleanupDeletedUsersResponse(int deletedUsersFound, int choresUnassigned, int usersDeleted) {}
}
