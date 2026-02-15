package de.caransgar.chorehub.services;
import de.caransgar.chorehub.dto.ChoreDTO;

import de.caransgar.chorehub.dto.CreateChoreRequest;
import de.caransgar.chorehub.entity.Chore;
import de.caransgar.chorehub.entity.User;
import de.caransgar.chorehub.repository.ChoreRepository;
import de.caransgar.chorehub.utils.TimeUtils;

import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
public class ChoreService {

    private final ChoreRepository choreRepository;
    private final UserService userService;

    public ChoreService(ChoreRepository choreRepository, UserService userService) {
        this.choreRepository = choreRepository;
        this.userService = userService;
    }

    public List<Chore> getAllChores() {
        return choreRepository.findAll();
    }

    /**
     * Provides list of all chores with `nextDueDate` before "tomorrow at 00:00".
     * 
     * @return A list of all due or overdue chores
     */
    public List<Chore> getDueChores() {
        return choreRepository.findByNextDueDateIsBefore(TimeUtils.getStartOfTomorrow());
    }

    /**
     * Provides list of all chores with `nextDueDate` before "tomorrow at 00:00".
     * 
     * @return A list of all due or overdue chores
     */
    public List<Chore> getDueChores(User user) {
        return choreRepository.findByNextDueDateIsBeforeAndAssignedUser(TimeUtils.getStartOfTomorrow(), user);
    }

    /**
     * Creates a new chore with business logic validation.
     * Validates recurrence patterns, user existence, and chore parameters.
     *
     * @param request the CreateChoreRequest containing chore details
     * @return the created ChoreDTO
     * @throws IllegalArgumentException if validation fails
     */
    public ChoreDTO createChore(CreateChoreRequest request) {
        // Business logic validation
        validateChoreCreation(request);

        // Resolve the assigned user if provided
        User assignedUser = null;
        if (request.getAssignedUsername() != null && !request.getAssignedUsername().isBlank()) {
            assignedUser = userService.getUserByName(request.getAssignedUsername())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "User with name '" + request.getAssignedUsername() + "' not found"));
        }

        // Create the chore entity
        Chore chore = new Chore(
                request.getName(),
                request.getDescription(),
                request.getRecurrenceType(),
                request.getRecurrencePattern(),
                assignedUser);

        // Save and return
        Chore savedChore = choreRepository.save(chore);
        return savedChore;
    }

    /**
     * Validates a chore creation request for business logic compliance.
     * 
     * @param request the CreateChoreRequest to validate
     * @throws IllegalArgumentException if any validation fails
     */
    private void validateChoreCreation(CreateChoreRequest request) {
        // Validate name
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Chore name cannot be empty");
        }
        if (request.getName().length() > 255) {
            throw new IllegalArgumentException("Chore name cannot exceed 255 characters");
        }

        // Validate recurrence type
        if (request.getRecurrenceType() == null) {
            throw new IllegalArgumentException("Recurrence type is required");
        }

        // Validate recurrence pattern based on type
        switch (request.getRecurrenceType()) {
            case FIXED_SCHEDULE:
                validateCronPattern(request.getRecurrencePattern());
                break;
            case AFTER_COMPLETION:
                validateDurationPattern(request.getRecurrencePattern());
                break;
            case ONETIME:
                if (request.getRecurrencePattern() != null && !request.getRecurrencePattern().isBlank()) {
                    throw new IllegalArgumentException(
                            "Recurrence pattern should not be set for ONETIME chores");
                }
                break;
        }

        // Validate description length if provided
        if (request.getDescription() != null && request.getDescription().length() > 1000) {
            throw new IllegalArgumentException("Chore description cannot exceed 1000 characters");
        }
    }

    /**
     * Validates that a string is a valid cron expression.
     *
     * @param pattern the pattern to validate
     * @throws IllegalArgumentException if pattern is not a valid cron expression
     */
    private void validateCronPattern(String pattern) {
        if (pattern == null || pattern.isBlank()) {
            throw new IllegalArgumentException(
                    "Recurrence pattern is required for FIXED_SCHEDULE chores");
        }
        try {
            CronExpression.parse(pattern);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Invalid cron pattern: " + e.getMessage());
        }
    }

    /**
     * Validates that a string is a valid ISO-8601 duration.
     *
     * @param pattern the pattern to validate
     * @throws IllegalArgumentException if pattern is not a valid duration
     */
    private void validateDurationPattern(String pattern) {
        if (pattern == null || pattern.isBlank()) {
            throw new IllegalArgumentException(
                    "Recurrence pattern is required for AFTER_COMPLETION chores");
        }
        try {
            Duration.parse(pattern);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Invalid duration pattern: " + e.getMessage());
        }
    }

    public List<Chore> getUsersChores(User user) {
        return choreRepository.findByAssignedUser(user);
    }

    public Optional<Chore> getChoreById(Long id) {
        return choreRepository.findById(id);
    }

    public Optional<Chore> markChoreAsDone(Long id) {
        Optional<Chore> choreOptional = getChoreById(id);

        if (choreOptional.isPresent()) {
            markChoreAsDone(choreOptional.get());
        }

        return getChoreById(id);
    }

    public Chore markChoreAsDone(Chore chore) {
        // TODO: Write History

        chore.completeChore();
        return saveChore(chore);
    }

    public Chore saveChore(Chore chore) {
        return choreRepository.save(chore);
    }

    public void deleteChore(Long id) {
        choreRepository.deleteById(id);
    }

    public List<Chore> getChoresByUser(User user) {
        return choreRepository.findByAssignedUser(user);
    }

}