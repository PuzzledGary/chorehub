# Agents Documentation

This document describes the various agents (components) that make up the ChoreHub system. Each agent has a specific responsibility in the chore management ecosystem.

## Overview

ChoreHub is composed of several agents that work together to provide a comprehensive chore management solution:

- **API Agent** (ChoreController) - Handles HTTP requests and responses
- **Business Logic Agent** (ChoreService) - Contains the core chore management logic
- **Data Persistence Agent** (JPA/Hibernate) - Manages database operations
- **Scheduling Agent** (Future) - Handles recurring chore scheduling
- **History Agent** (Future) - Manages chore completion history

## API Agent (ChoreController)

**Location**: `src/main/java/de/caransgar/chorehub/controller/ChoreController.java`

**Responsibilities**:
- Receive and process HTTP requests
- Validate input data
- Route requests to appropriate business logic
- Format and return responses

**Current Endpoints**:
- `GET /` - Health check

**Future Endpoints**:
- `GET /chores` - List all chores
- `POST /chores` - Create a new chore
- `PUT /chores/{id}` - Update a chore
- `DELETE /chores/{id}` - Delete a chore
- `POST /chores/{id}/complete` - Mark chore as completed
- `GET /users` - List users
- `GET /history` - Get chore completion history

## Business Logic Agent (ChoreService)

**Location**: `src/main/java/de/caransgar/chorehub/services/ChoreService.java`

**Responsibilities**:
- Implement chore management business rules
- Handle chore scheduling logic
- Validate chore data
- Coordinate with data persistence agent
- Manage user-chore relationships

**Current Features**:
- Basic CRUD operations for chores
- Retrieve chores by user ID

**Planned Features**:
- Create and update chores with different recurrence types
- Calculate next due dates for recurring chores
- Assign chores to users
- Track chore completion
- Generate chore statistics

## Data Persistence Agent

**Technology**: Spring Data JPA with Hibernate

**Location**: 
- Entities: `src/main/java/de/caransgar/chorehub/entity/`
- Repositories: `src/main/java/de/caransgar/chorehub/repository/`

**Responsibilities**:
- Map Java objects to database tables
- Execute CRUD operations
- Handle database transactions
- Optimize queries for performance

**Current Entities**:
- `Chore` - Represents a chore with properties like name, description, recurrence, assigned user
- `RecurrenceType` - Enum for recurrence types (FIXED, FLEXIBLE, ONETIME)
- `User` - Represents a household member with name and optional shortname

**Current Repositories**:
- `ChoreRepository` - Provides data access methods for Chore entities
- `UserRepository` - Provides data access methods for User entities

**Planned Entities**:
- `ChoreHistory` - Records of completed chores

## Scheduling Agent (Planned)

**Responsibilities**:
- Monitor due dates for recurring chores
- Trigger notifications when chores are due
- Handle different recurrence patterns (fixed dates, intervals)
- Integrate with external scheduling systems

## History Agent (Planned)

**Responsibilities**:
- Store records of completed chores
- Provide historical analytics
- Generate reports on chore completion patterns
- Support data export for backup

## Communication Between Agents

Agents communicate through well-defined interfaces:

- Controllers inject services via dependency injection
- Services use repositories for data access
- Asynchronous communication may be added for scheduling and notifications

## Configuration

Agent behavior can be configured through `application.yaml`:

```yaml
spring:
  application:
    name: chorehub
  datasource:
    url: jdbc:mariadb://localhost:3306/chorehub
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

## Monitoring and Logging

Each agent includes logging capabilities for debugging and monitoring. Spring Boot Actuator endpoints will be added for health checks and metrics.

## Future Extensions

- **Notification Agent** - Send reminders via email, push notifications, or Home Assistant
- **Integration Agent** - Connect with external systems like calendars or smart home devices
- **Analytics Agent** - Provide insights into chore completion patterns</content>
<parameter name="filePath">/home/ansgar/projects/chorehub/AGENTS.md