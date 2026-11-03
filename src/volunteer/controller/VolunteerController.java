package volunteer.controller;

import user.model.User;
import volunteer.model.AvailabilitySlot;
import volunteer.model.CertificateType;
import volunteer.model.FundraiserTask;
import volunteer.model.OutreachTask;
import volunteer.model.Skill;
import volunteer.model.SkillLevel;
import volunteer.model.TaskType;
import volunteer.model.Volunteer;
import volunteer.model.VolunteerAssignment;
import volunteer.model.VolunteerCertificate;
import volunteer.model.VolunteerHourRecord;
import volunteer.model.VolunteerTask;
import volunteer.model.WorkshopTask;
import volunteer.service.VolunteerService;
import volunteer.view.VolunteerPanel;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class VolunteerController {
    private final VolunteerService service;
    private final User currentUser;
    private VolunteerPanel view;

    public VolunteerController(VolunteerService service, User currentUser) {
        this.service = service;
        this.currentUser = currentUser;
    }

    public void setView(VolunteerPanel view) {
        this.view = view;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void handleRegister(String name, String email, String phone, String preferences) {
        run(() -> {
            Volunteer v = service.registerVolunteer(name, email, phone, splitList(preferences));
            view.showMessage("Registered " + v.getName() + " with ID " + v.getUserId() + ".");
        });
    }

    public void handleUpdateProfile(Integer volunteerId, String name, String email, String phone, String preferences) {
        run(() -> {
            service.updateVolunteer(require(volunteerId, "Select a volunteer first."), name, email, phone, splitList(preferences));
            view.showMessage("Profile updated.");
        });
    }

    public void handleDeactivate(Integer volunteerId) {
        run(() -> service.deactivateVolunteer(require(volunteerId, "Select a volunteer first.")));
    }

    public void handleActivate(Integer volunteerId) {
        run(() -> service.activateVolunteer(require(volunteerId, "Select a volunteer first.")));
    }

    public List<Volunteer> handleSearch(String keyword) {
        return service.searchVolunteers(keyword);
    }

    public void handleAddSkill(Integer volunteerId, String skillName, SkillLevel level, boolean certified) {
        run(() -> service.addSkill(require(volunteerId, "Select a volunteer first."), skillName, level, certified));
    }

    public void handleRemoveSkill(Integer volunteerId, Skill skill) {
        run(() -> {
            if (skill == null) throw new IllegalArgumentException("Choose a skill to remove.");
            service.removeSkill(require(volunteerId, "Select a volunteer first."), skill.getSkillId());
        });
    }

    public void handleAddAvailability(Integer volunteerId, DayOfWeek day, String start, String end) {
        run(() -> service.addAvailability(require(volunteerId, "Select a volunteer first."), day,
                parseTime(start, "start time"), parseTime(end, "end time")));
    }

    public void handleRemoveAvailability(Integer volunteerId, AvailabilitySlot slot) {
        run(() -> {
            if (slot == null) throw new IllegalArgumentException("Choose a time slot to remove.");
            service.removeAvailability(require(volunteerId, "Select a volunteer first."), slot.getSlotId());
        });
    }

    public void handleCreateTask(TaskType type, String title, String date, String start, String end,
                                 String location, String requiredSkills, int maxVolunteers,
                                 String eventId, String typeSpecificValue, String description) {
        run(() -> {
            LocalDate d = parseDate(date);
            LocalDateTime s = LocalDateTime.of(d, parseTime(start, "start time"));
            LocalDateTime e = LocalDateTime.of(d, parseTime(end, "end time"));
            Integer event = (eventId == null || eventId.isBlank()) ? null : parseInt(eventId, "Event ID");
            String extra = typeSpecificValue == null ? "" : typeSpecificValue.trim();
            if (type == TaskType.FUNDRAISER && !extra.isEmpty()) parseDouble(extra, "Target amount");
            if (maxVolunteers < 1) throw new IllegalArgumentException("A task needs at least 1 volunteer place.");

            VolunteerTask task = service.createTask(type, title, s, e);
            task.setLocation(location == null ? "" : location.trim());
            task.setDescription(description == null ? "" : description.trim());
            task.setRequiredSkills(splitList(requiredSkills));
            task.setMaxVolunteers(maxVolunteers);
            task.setEventId(event);
            applyTypeSpecific(task, extra);
            service.saveTask(task);
            view.showMessage("Created " + task.getType() + " task #" + task.getTaskId() + ".");
        });
    }

    private void applyTypeSpecific(VolunteerTask task, String value) {
        String v = value == null ? "" : value.trim();
        if (task instanceof FundraiserTask) {
            ((FundraiserTask) task).setTargetAmount(v.isEmpty() ? 0 : parseDouble(v, "Target amount"));
        } else if (task instanceof WorkshopTask) {
            ((WorkshopTask) task).setTopic(v);
        } else if (task instanceof OutreachTask) {
            ((OutreachTask) task).setTargetArea(v);
        }
    }

    public void handleCancelTask(Integer taskId) {
        run(() -> service.cancelTask(require(taskId, "Select a task first.")));
    }

    public void handleShowSuitable(Integer taskId) {
        run(() -> {
            List<Volunteer> list = service.findSuitableVolunteers(require(taskId, "Select a task first."));
            view.showMessage(list.isEmpty() ? "No eligible, free volunteers for this task."
                    : "Suitable volunteers:\n" + list.stream().map(Volunteer::toString).collect(Collectors.joining("\n")));
        });
    }

    public List<VolunteerTask> getTasks() {
        return service.getAllTasks();
    }

    public void handleAssignTask(Volunteer volunteer, VolunteerTask task, String role) {
        run(() -> {
            if (volunteer == null || task == null) throw new IllegalArgumentException("Choose a volunteer and a task.");
            VolunteerAssignment a = service.assignTask(volunteer.getUserId(), task.getTaskId(), role, currentUser);
            view.showMessage("Assignment #" + a.getAssignmentId() + " created (PENDING). " + volunteer.getName() + " has been notified.");
        });
    }

    public void handleRespondToAssignment(Integer assignmentId, boolean accept) {
        run(() -> {
            int id = require(assignmentId, "Select an assignment first.");
            if (accept) service.acceptAssignment(id); else service.declineAssignment(id);
        });
    }

    public void handleCompleteAssignment(Integer assignmentId) {
        run(() -> service.completeAssignment(require(assignmentId, "Select an assignment first.")));
    }

    public void handleCancelAssignment(Integer assignmentId) {
        run(() -> service.cancelAssignment(require(assignmentId, "Select an assignment first.")));
    }

    public void handleLogHours(Integer assignmentId, String date, String hours) {
        run(() -> service.recordHours(require(assignmentId, "Select an assignment first."),
                parseDate(date), parseDouble(hours, "Hours")));
    }

    public void handleApproveHours(Integer recordId, boolean approve) {
        run(() -> {
            int id = require(recordId, "Select an hour record first.");
            if (approve) service.approveHours(id, currentUser); else service.rejectHours(id, currentUser);
        });
    }

    public List<VolunteerAssignment> getAssignments() {
        return service.getAllAssignments();
    }

    public List<VolunteerHourRecord> getHourRecords() {
        return service.getAllHourRecords();
    }

    public List<VolunteerAssignment> getHistory(int volunteerId) {
        return service.getHistory(volunteerId);
    }

    public List<VolunteerHourRecord> getHourRecords(int volunteerId) {
        return service.getHourRecords(volunteerId);
    }

    public void handleIssueCertificate(Volunteer volunteer, CertificateType type) {
        run(() -> {
            if (volunteer == null) throw new IllegalArgumentException("Choose a volunteer first.");
            VolunteerCertificate c = service.issueCertificate(volunteer.getUserId(), type);
            view.showCertificate(c.generate());
        });
    }

    public List<Volunteer> getVolunteers() { return service.getAllVolunteers(); }
    public Volunteer getVolunteer(int id) { return service.getVolunteer(id); }
    public VolunteerTask getTask(int id) { return service.getTask(id); }

    private void run(Runnable action) {
        try {
            view.clearStatus();
            action.run();
            view.refresh();
        } catch (RuntimeException ex) {
            view.showError(ex.getMessage());
        }
    }

    private static int require(Integer id, String message) {
        if (id == null) throw new IllegalArgumentException(message);
        return id;
    }

    private static List<String> splitList(String text) {
        if (text == null || text.isBlank()) return List.of();
        return Arrays.stream(text.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
    }

    private static LocalTime parseTime(String text, String field) {
        try {
            return LocalTime.parse(text.trim());
        } catch (DateTimeParseException | NullPointerException e) {
            throw new IllegalArgumentException("Enter the " + field + " as HH:mm, for example 09:30.");
        }
    }

    private static LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text.trim());
        } catch (DateTimeParseException | NullPointerException e) {
            throw new IllegalArgumentException("Enter the date as yyyy-MM-dd, for example 2026-11-14.");
        }
    }

    private static int parseInt(String text, String field) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(field + " must be a whole number.");
        }
    }

    private static double parseDouble(String text, String field) {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException | NullPointerException e) {
            throw new IllegalArgumentException(field + " must be a number.");
        }
    }
}
