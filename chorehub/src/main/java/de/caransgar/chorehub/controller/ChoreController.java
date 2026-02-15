package de.caransgar.chorehub.controller;

import de.caransgar.chorehub.dto.CreateChoreRequest;
import de.caransgar.chorehub.entity.Chore;
import de.caransgar.chorehub.entity.User;
import de.caransgar.chorehub.dto.ChoreDTO;
import de.caransgar.chorehub.services.ChoreService;
import de.caransgar.chorehub.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chores")
public class ChoreController {

    private final ChoreService choreService;
    private final UserService userService;

    public ChoreController(ChoreService choreService, UserService userService) {
        this.choreService = choreService;
        this.userService = userService;
    }

    @GetMapping("/")
    public String getMethodName() {
        return "ChoreHub is running.";
    }

    /**
     * Create a new chore.
     * 
     * Performs basic validation of inputs before delegating to the service layer
     * for higher-level business logic validation.
     *
     * @param request the CreateChoreRequest containing chore details
     * @return ResponseEntity with the created chore and HTTP 201 status
     */
    @PostMapping
    public ResponseEntity<?> createChore(@RequestBody CreateChoreRequest request) {
        try {
            // Basic input validation
            if (request == null) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Request body cannot be null"));
            }

            if (request.getName() == null) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Chore name is required"));
            }

            if (request.getRecurrenceType() == null) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Recurrence type is required"));
            }

            // Delegate to service for business logic validation and creation
            ChoreDTO createdChore = choreService.createChore(request);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(createdChore);

        } catch (IllegalArgumentException e) {
            // Service-level validation errors
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            // Unexpected errors
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An unexpected error occurred: " + e.getMessage()));
        }
    }

    /**
     * Get all due or overdue chores.
     * 
     * Returns chores where nextDueDate is before tomorrow at 00:00.
     *
     * @return ResponseEntity with list of due ChoreDTO objects
     */
    @GetMapping("/due")
    public ResponseEntity<?> getDueChores() {
        try {
            var dueChores = choreService.getDueChores();
            var choresDTO = dueChores.stream()
                    .map(this::toChoreDTO)
                    .toList();
            return ResponseEntity.ok(choresDTO);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An unexpected error occurred: " + e.getMessage()));
        }
    }

    /**
     * Get all due or overdue chores for a specific user.
     * 
     * Returns chores assigned to the specified user where nextDueDate is before
     * tomorrow at 00:00.
     *
     * @param username the username of the assigned user
     * @return ResponseEntity with list of due ChoreDTO objects for the user
     */
    @GetMapping("/due/user/{username}")
    public ResponseEntity<?> getUserDueChores(@PathVariable String username) {
        try {
            User user = userService.getUserByName(username)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "User with name '" + username + "' not found"));

            var dueChores = choreService.getDueChores(user);
            var choresDTO = dueChores.stream()
                    .map(this::toChoreDTO)
                    .toList();
            return ResponseEntity.ok(choresDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An unexpected error occurred: " + e.getMessage()));
        }
    }

    /**
     * Simple error response DTO for API responses.
     */
    public static class ErrorResponse {
        public String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    /**
     * Converts a Chore entity to a ChoreDTO for API responses.
     * 
     * @param chore the Chore entity to convert
     * @return the ChoreDTO
     */
    private ChoreDTO toChoreDTO(Chore chore) {
        String assignedUsername = chore.getAssignedUser() != null ? chore.getAssignedUser().getName() : null;
        return new ChoreDTO(
                chore.getId(),
                chore.getName(),
                chore.getDescription(),
                chore.getRecurrenceType(),
                chore.getRecurrencePattern(),
                assignedUsername,
                chore.getCreatedDate(),
                chore.getLastCompletedDate(),
                chore.getNextDueDate());
    }
}
