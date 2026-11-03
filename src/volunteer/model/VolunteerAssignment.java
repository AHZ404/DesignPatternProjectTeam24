package volunteer.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.io.Serializable;

import java.time.LocalDateTime;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class VolunteerAssignment implements Serializable {
    private static final long serialVersionUID = 1L;

    private int assignmentId;
    private int volunteerId;
    private int taskId;
    private String role;
    private AssignmentStatus status = AssignmentStatus.PENDING;
    private LocalDateTime assignedAt = LocalDateTime.now();
    private int assignedById;

    private VolunteerAssignment() {
    }

    public VolunteerAssignment(int assignmentId, int volunteerId, int taskId, String role, int assignedById) {
        this.assignmentId = assignmentId;
        this.volunteerId = volunteerId;
        this.taskId = taskId;
        this.role = (role == null || role.isBlank()) ? "Volunteer" : role.trim();
        this.assignedById = assignedById;
    }

    public void accept() {
        requireStatus(AssignmentStatus.PENDING, "accept");
        status = AssignmentStatus.ACCEPTED;
    }

    public void decline() {
        requireStatus(AssignmentStatus.PENDING, "decline");
        status = AssignmentStatus.DECLINED;
    }

    public void complete() {
        requireStatus(AssignmentStatus.ACCEPTED, "complete");
        status = AssignmentStatus.COMPLETED;
    }

    public void cancel() {
        if (!status.isActive()) {
            throw new IllegalStateException("Only pending or accepted assignments can be cancelled (current: " + status + ").");
        }
        status = AssignmentStatus.CANCELLED;
    }

    private void requireStatus(AssignmentStatus expected, String action) {
        if (status != expected) {
            throw new IllegalStateException("Cannot " + action + " an assignment that is " + status + ".");
        }
    }

    public int getAssignmentId() { return assignmentId; }
    public int getVolunteerId() { return volunteerId; }
    public int getTaskId() { return taskId; }
    public String getRole() { return role; }
    public AssignmentStatus getStatus() { return status; }
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public int getAssignedById() { return assignedById; }
}
