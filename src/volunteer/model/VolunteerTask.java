package volunteer.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.io.Serializable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public abstract class VolunteerTask implements Serializable {
    private static final long serialVersionUID = 1L;

    protected int taskId;
    protected String title;
    protected String description = "";
    protected LocalDateTime startTime;
    protected LocalDateTime endTime;
    protected String location = "";
    protected List<String> requiredSkills = new ArrayList<>();
    protected int maxVolunteers = 1;
    protected TaskStatus status = TaskStatus.OPEN;
    protected Integer eventId;
    protected int activeAssignments;

    protected VolunteerTask() {
    }

    protected VolunteerTask(String title, LocalDateTime startTime, LocalDateTime endTime) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Task title is required.");
        }
        if (startTime == null || endTime == null || !endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("Task end time must be after its start time.");
        }
        this.title = title.trim();
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public boolean overlaps(VolunteerTask other) {
        return startTime.isBefore(other.endTime) && other.startTime.isBefore(endTime);
    }

    public boolean isFull() {
        return activeAssignments >= maxVolunteers;
    }

    public void placeTaken() {
        activeAssignments++;
        if (isFull()) status = TaskStatus.FULL;
    }

    public void placeFreed() {
        if (activeAssignments > 0) activeAssignments--;
        if (status == TaskStatus.FULL && !isFull()) status = TaskStatus.OPEN;
    }

    public abstract TaskType getType();

    public abstract void execute();

    public abstract String getTypeDetails();

    public int getTaskId() { return taskId; }
    public void setTaskId(int taskId) { this.taskId = taskId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public List<String> getRequiredSkills() { return Collections.unmodifiableList(requiredSkills); }
    public void setRequiredSkills(List<String> skills) { this.requiredSkills = new ArrayList<>(skills); }
    public int getMaxVolunteers() { return maxVolunteers; }
    public void setMaxVolunteers(int maxVolunteers) {
        if (maxVolunteers < 1) throw new IllegalArgumentException("A task needs at least 1 volunteer place.");
        this.maxVolunteers = maxVolunteers;
    }
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public Integer getEventId() { return eventId; }
    public void setEventId(Integer eventId) { this.eventId = eventId; }
    public int getActiveAssignments() { return activeAssignments; }

    @Override
    public String toString() {
        return "#" + taskId + " " + title + " [" + getType() + "]";
    }
}
