package de.caransgar.chorehub.controller;

import de.caransgar.chorehub.dto.CreateChoreRequest;
import de.caransgar.chorehub.entity.Chore;
import de.caransgar.chorehub.dto.ChoreDTO;
import de.caransgar.chorehub.services.ChoreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chores")
public class ChoreController {

    private final ChoreService choreService;

    public ChoreController(ChoreService choreService) {
        this.choreService = choreService;
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

}
