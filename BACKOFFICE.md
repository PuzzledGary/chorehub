# ChoreHub Backoffice UI

## Overview

The ChoreHub Backoffice is a web-based user interface for managing chores and users. It's designed for quick testing and management of the chore system and is separate from the REST API.

**Path**: `/chorehub-ui`

## Features

### Dashboard
- **Overview**: View all chores and users at a glance
- **Quick Actions**: Mark chores as done, edit, or delete
- **Status Indicators**: Visual indicators for due dates (Overdue, Today, Upcoming)
- **Alerts**: Success/error messages for all operations

### Chore Management
- **Create Chores**: Add new chores with various recurrence types
- **Edit Chores**: Update existing chore details
- **Delete Chores**: Remove chores from the system
- **Mark Complete**: Record chore completion with optional notes

**Recurrence Types**:
1. **One-time**: A chore that doesn't repeat
2. **Fixed Schedule (Cron)**: Uses cron expressions for scheduling
   - Example: `0 0 * * *` (daily at midnight)
   - Example: `0 0 1 * *` (1st of each month)
3. **After Completion (Duration)**: Repeats after completion
   - Example: `P7D` (7 days)
   - Example: `PT2H` (2 hours)

### User Management
- **Create Users**: Add household members
- **Edit Users**: Update user information
- **Delete Users**: Remove users
- **View Assignments**: See how many chores each user has

## Technical Implementation

### Dependencies Added
- `spring-boot-starter-thymeleaf`: Template engine for HTML views

### New Files Created

**Controllers**:
- `BackofficeController.java`: Handles all backoffice web requests

**Templates** (Thymeleaf):
- `templates/backoffice/dashboard.html`: Main dashboard view
- `templates/backoffice/chore-form.html`: Create/edit chore form
- `templates/backoffice/user-form.html`: Create/edit user form

### Service Layer Enhancements

Added two new methods to `ChoreService`:

1. **`updateChore(Long id, CreateChoreRequest request)`**
   - Updates an existing chore with new data
   - Validates input before updating
   - Publishes updated state to MQTT

2. **`completeChore(Long id, String notes)`**
   - Records chore completion with optional notes
   - Recalculates next due date automatically
   - Updates MQTT state

## Routes

### Backoffice Views
- `GET /chorehub-ui` - Dashboard (all chores and users)
- `GET /chorehub-ui/chores/new` - Create chore form
- `GET /chorehub-ui/chores/{id}/edit` - Edit chore form
- `GET /chorehub-ui/users/new` - Create user form
- `GET /chorehub-ui/users/{id}/edit` - Edit user form

### Backoffice Actions (POST)
- `POST /chorehub-ui/chores` - Create chore
- `POST /chorehub-ui/chores/{id}` - Update chore
- `POST /chorehub-ui/chores/{id}/delete` - Delete chore
- `POST /chorehub-ui/chores/{id}/complete` - Mark chore as completed
- `POST /chorehub-ui/users` - Create user
- `POST /chorehub-ui/users/{id}` - Update user
- `POST /chorehub-ui/users/{id}/delete` - Delete user

## Original REST API (Unchanged)

The original REST API at `/chores` and other endpoints remain unchanged and fully functional. The backoffice is a completely separate interface.

## Design Features

### Responsive Design
- Mobile-friendly layout
- Adapts to different screen sizes
- Touch-friendly buttons and controls

### User Experience
- Auto-hiding alerts (5 seconds)
- Confirmation dialogs for destructive actions
- Clear visual indicators for chore status
- Intuitive form validation with helpful hints

### Styling
- Clean, modern interface
- Color-coded status indicators
- Consistent typography and spacing
- Professional color scheme (#2c3e50, #3498db, etc.)

## Testing the Backoffice

1. Start the application:
   ```bash
   cd chorehub && ./gradlew bootRun
   ```

2. Open your browser and navigate to:
   ```
   http://localhost:8080/chorehub-ui
   ```

3. You'll see the dashboard with:
   - List of all chores
   - List of all users
   - Buttons to create new chores and users

## Advanced Features

### Chore Form Dynamic Behavior
The chore creation form dynamically updates helpful hints based on the selected recurrence type:
- **One-time**: Shows that the chore won't repeat
- **Fixed Schedule**: Shows cron expression format explanation
- **After Completion**: Shows ISO-8601 duration format explanation

### Status Indicators
Chores display visual labels based on their due dates:
- 🔴 **Overdue**: Due date is in the past
- 🟠 **Today**: Due today
- 🟢 **Upcoming**: Due in the future

### Chore Completion
When marking a chore as complete:
- A history entry is created
- The last completion date is updated
- The next due date is recalculated automatically
- MQTT state is updated for Home Assistant integration

## Integration with MQTT and Home Assistant

All chore operations in the backoffice trigger MQTT updates, keeping Home Assistant and other MQTT subscribers in sync:
- Creating/updating chores updates MQTT discovery
- Completing chores updates their MQTT state
- Deleting chores removes MQTT discovery

## Security Notes

The backoffice currently has no authentication. For production use:
- Add Spring Security authentication
- Implement authorization for user/chore access
- Consider CSRF protection
- Add input sanitization for rich content

## Future Enhancements

Potential improvements:
- Bulk operations (delete multiple chores)
- Filtering and sorting options
- Chore history view
- Statistics and analytics
- Dark mode support
- Export/import functionality
- Recurring chore templates
