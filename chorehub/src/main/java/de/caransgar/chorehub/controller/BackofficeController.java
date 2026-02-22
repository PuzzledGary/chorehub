package de.caransgar.chorehub.controller;

import de.caransgar.chorehub.dto.CreateChoreRequest;
import de.caransgar.chorehub.dto.ChoreDTO;
import de.caransgar.chorehub.entity.Chore;
import de.caransgar.chorehub.entity.RecurrenceType;
import de.caransgar.chorehub.entity.User;
import de.caransgar.chorehub.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Backoffice Controller for ChoreHub
 * 
 * Handles the web UI for managing chores and users.
 * Delegates chore operations to the REST API endpoints in ChoreController.
 * User operations use UserService directly (no REST API yet).
 */
@Controller
@RequestMapping("/chorehub-ui")
public class BackofficeController {

    private static final Logger LOG = LoggerFactory.getLogger(BackofficeController.class);
    private static final String BASE_URL = "http://localhost:8080";
    private static final String CHORES_API = BASE_URL + "/chores";

    private final RestTemplate restTemplate;
    private final UserService userService;

    public BackofficeController(RestTemplate restTemplate, UserService userService) {
        this.restTemplate = restTemplate;
        this.userService = userService;
    }

    // ==================== Dashboard ====================

    /**
     * Display the main dashboard
     */
    @GetMapping
    public String dashboard(Model model) {
        try {
            // Fetch chores from REST API
            ResponseEntity<ChoreDTO[]> response = restTemplate.getForEntity(CHORES_API, ChoreDTO[].class);
            List<ChoreDTO> chores = response.getStatusCode() == HttpStatus.OK 
                ? Arrays.asList(response.getBody() != null ? response.getBody() : new ChoreDTO[0])
                : List.of();
            
            List<User> users = userService.getAllUsers();
            
            model.addAttribute("chores", chores);
            model.addAttribute("users", users);
            model.addAttribute("recurrenceTypes", RecurrenceType.values());
            
            return "backoffice/dashboard";
        } catch (Exception e) {
            LOG.error("Error loading dashboard: {}", e.getMessage());
            model.addAttribute("errorMessage", "Error loading data: " + e.getMessage());
            return "backoffice/dashboard";
        }
    }

    // ==================== Chore Management ====================

    /**
     * Display the create chore form
     */
    @GetMapping("/chores/new")
    public String showCreateChoreForm(Model model) {
        List<User> users = userService.getAllUsers();
        
        model.addAttribute("users", users);
        model.addAttribute("recurrenceTypes", RecurrenceType.values());
        model.addAttribute("chore", new CreateChoreRequest());
        
        return "backoffice/chore-form";
    }

    /**
     * Display the edit chore form
     */
    @GetMapping("/chores/{id}/edit")
    public String showEditChoreForm(@PathVariable Long id, Model model) {
        try {
            ResponseEntity<ChoreDTO> response = restTemplate.getForEntity(CHORES_API + "/" + id, ChoreDTO.class);
            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                LOG.warn("Chore not found: {}", id);
                return "redirect:/chorehub-ui";
            }
            
            List<User> users = userService.getAllUsers();
            
            model.addAttribute("chore", response.getBody());
            model.addAttribute("users", users);
            model.addAttribute("recurrenceTypes", RecurrenceType.values());
            model.addAttribute("isEdit", true);
            
            return "backoffice/chore-form";
        } catch (HttpClientErrorException.NotFound e) {
            LOG.warn("Chore not found: {}", id);
            return "redirect:/chorehub-ui";
        } catch (Exception e) {
            LOG.error("Error loading chore {}: {}", id, e.getMessage());
            return "redirect:/chorehub-ui";
        }
    }

    /**
     * Create a new chore via REST API
     */
    @PostMapping("/chores")
    public String createChore(@ModelAttribute("chore") CreateChoreRequest request,
                             RedirectAttributes redirectAttributes) {
        try {
            ResponseEntity<ChoreDTO> response = restTemplate.postForEntity(CHORES_API, request, ChoreDTO.class);
            if (response.getStatusCode() == HttpStatus.CREATED) {
                redirectAttributes.addFlashAttribute("successMessage", "Chore created successfully");
                return "redirect:/chorehub-ui";
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Error creating chore");
                return "redirect:/chorehub-ui/chores/new";
            }
        } catch (HttpClientErrorException e) {
            LOG.error("Error creating chore: {}", e.getMessage());
            String errorMsg = e.getResponseBodyAsString();
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating chore: " + errorMsg);
            return "redirect:/chorehub-ui/chores/new";
        } catch (Exception e) {
            LOG.error("Unexpected error creating chore: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Unexpected error: " + e.getMessage());
            return "redirect:/chorehub-ui/chores/new";
        }
    }

    /**
     * Update an existing chore via REST API
     */
    @PostMapping("/chores/{id}")
    public String updateChore(@PathVariable Long id,
                             @ModelAttribute("chore") CreateChoreRequest request,
                             RedirectAttributes redirectAttributes) {
        try {
            restTemplate.put(CHORES_API + "/" + id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Chore updated successfully");
            return "redirect:/chorehub-ui";
        } catch (HttpClientErrorException e) {
            LOG.error("Error updating chore {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating chore: " + e.getResponseBodyAsString());
            return "redirect:/chorehub-ui/chores/" + id + "/edit";
        } catch (Exception e) {
            LOG.error("Unexpected error updating chore {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Unexpected error: " + e.getMessage());
            return "redirect:/chorehub-ui/chores/" + id + "/edit";
        }
    }

    /**
     * Delete a chore via REST API
     */
    @PostMapping("/chores/{id}/delete")
    public String deleteChore(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            restTemplate.delete(CHORES_API + "/" + id);
            redirectAttributes.addFlashAttribute("successMessage", "Chore deleted successfully");
        } catch (HttpClientErrorException e) {
            LOG.error("Error deleting chore {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting chore: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            LOG.error("Unexpected error deleting chore {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Unexpected error: " + e.getMessage());
        }
        return "redirect:/chorehub-ui";
    }

    /**
     * Mark a chore as completed via REST API
     */
    @PostMapping("/chores/{id}/complete")
    public String completeChore(@PathVariable Long id,
                               @RequestParam(required = false) String notes,
                               RedirectAttributes redirectAttributes) {
        try {
            // Call the REST API endpoint to mark chore as done
            ResponseEntity<ChoreDTO> response = restTemplate.postForEntity(
                CHORES_API + "/" + id + "/done", 
                null, 
                ChoreDTO.class
            );
            if (response.getStatusCode() == HttpStatus.OK) {
                redirectAttributes.addFlashAttribute("successMessage", "Chore marked as completed");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Error completing chore");
            }
        } catch (HttpClientErrorException e) {
            LOG.error("Error completing chore {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error completing chore: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            LOG.error("Unexpected error completing chore {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Unexpected error: " + e.getMessage());
        }
        return "redirect:/chorehub-ui";
    }

    // ==================== User Management ====================

    /**
     * Display the create user form
     */
    @GetMapping("/users/new")
    public String showCreateUserForm(Model model) {
        model.addAttribute("user", new User());
        return "backoffice/user-form";
    }

    /**
     * Display the edit user form
     */
    @GetMapping("/users/{id}/edit")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        Optional<User> user = userService.getUserById(id);
        if (user.isEmpty()) {
            LOG.warn("User not found: {}", id);
            return "redirect:/chorehub-ui";
        }
        
        model.addAttribute("user", user.get());
        model.addAttribute("isEdit", true);
        
        return "backoffice/user-form";
    }

    /**
     * Create a new user (direct service call - no REST API yet)
     */
    @PostMapping("/users")
    public String createUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
            userService.saveUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "User created successfully");
            return "redirect:/chorehub-ui";
        } catch (Exception e) {
            LOG.error("Error creating user: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating user: " + e.getMessage());
            return "redirect:/chorehub-ui/users/new";
        }
    }

    /**
     * Update an existing user (direct service call - no REST API yet)
     */
    @PostMapping("/users/{id}")
    public String updateUser(@PathVariable Long id, @ModelAttribute User user,
                            RedirectAttributes redirectAttributes) {
        try {
            user.setId(id);
            userService.saveUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "User updated successfully");
            return "redirect:/chorehub-ui";
        } catch (Exception e) {
            LOG.error("Error updating user {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating user: " + e.getMessage());
            return "redirect:/chorehub-ui/users/" + id + "/edit";
        }
    }

    /**
     * Delete a user (direct service call - no REST API yet)
     */
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully");
        } catch (Exception e) {
            LOG.error("Error deleting user {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting user: " + e.getMessage());
        }
        return "redirect:/chorehub-ui";
    }
}

