package volunteer.service;

import user.model.User;
import volunteer.model.AssignmentStatus;
import volunteer.model.AvailabilitySlot;
import volunteer.model.CertificateType;
import volunteer.model.Skill;
import volunteer.model.SkillLevel;
import volunteer.model.TaskStatus;
import volunteer.model.TaskType;
import volunteer.model.Volunteer;
import volunteer.model.VolunteerAssignment;
import volunteer.model.VolunteerCertificate;
import volunteer.model.VolunteerHourRecord;
import volunteer.model.VolunteerTask;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class VolunteerService {
    public static final double MILESTONE_HOURS = 10;

    private final VolunteerRepository repository;
    private final VolunteerTaskFactory taskFactory;
    private final VolunteerNotifier notifier;

    public VolunteerService(VolunteerRepository repository, VolunteerTaskFactory taskFactory, VolunteerNotifier notifier) {
        this.repository = repository;
        this.taskFactory = taskFactory;
        this.notifier = notifier;
    }

    public Volunteer registerVolunteer(String name, String email, String phone, List<String> preferences) {
        requireUniqueEmail(email, -1);
        Volunteer v = new Volunteer(repository.nextId("volunteer"), name, email, phone, preferences);
        repository.save(v);
        notifier.notify(v, "Welcome", "Your volunteer registration is confirmed. Your volunteer ID is " + v.getUserId() + ".");
        return v;
    }

    public Volunteer updateVolunteer(int volunteerId, String name, String email, String phone, List<String> preferences) {
        Volunteer v = getVolunteer(volunteerId);
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Volunteer name is required.");
        if (email == null || !email.contains("@")) throw new IllegalArgumentException("A valid email is required.");
        requireUniqueEmail(email, volunteerId);
        v.updateProfile(name.trim(), email.trim(), phone);
        v.setPreferences(preferences);
        repository.save(v);
        return v;
    }

    public void deactivateVolunteer(int volunteerId) {
        Volunteer v = getVolunteer(volunteerId);
        v.deactivate();
        repository.save(v);
    }

    public void activateVolunteer(int volunteerId) {
        Volunteer v = getVolunteer(volunteerId);
        v.activate();
        repository.save(v);
    }

    public List<Volunteer> searchVolunteers(String keyword) {
        if (keyword == null || keyword.isBlank()) return repository.findAll();
        String k = keyword.trim().toLowerCase(Locale.ROOT);
        return repository.findAll().stream().filter(v ->
                String.valueOf(v.getUserId()).equals(k)
                        || contains(v.getName(), k)
                        || contains(v.getEmail(), k)
                        || contains(v.getPhone(), k)
                        || v.getSkills().stream().anyMatch(s -> contains(s.getName(), k))
        ).collect(Collectors.toList());
    }

    public Volunteer getVolunteer(int volunteerId) {
        return repository.findById(volunteerId)
                .orElseThrow(() -> new IllegalArgumentException("Volunteer " + volunteerId + " not found."));
    }

    public List<Volunteer> getAllVolunteers() {
        return repository.findAll();
    }

    public Skill addSkill(int volunteerId, String skillName, SkillLevel level, boolean certified) {
        Volunteer v = getVolunteer(volunteerId);
        Skill s = new Skill(repository.nextId("skill"), skillName, level, certified);
        v.addSkill(s);
        repository.save(v);
        return s;
    }

    public void removeSkill(int volunteerId, int skillId) {
        Volunteer v = getVolunteer(volunteerId);
        v.removeSkill(skillId);
        repository.save(v);
    }

    public AvailabilitySlot addAvailability(int volunteerId, DayOfWeek day, LocalTime start, LocalTime end) {
        Volunteer v = getVolunteer(volunteerId);
        AvailabilitySlot slot = new AvailabilitySlot(repository.nextId("slot"), day, start, end);
        v.addAvailability(slot);
        repository.save(v);
        return slot;
    }

    public void removeAvailability(int volunteerId, int slotId) {
        Volunteer v = getVolunteer(volunteerId);
        v.removeAvailability(slotId);
        repository.save(v);
    }

    public VolunteerTask createTask(TaskType type, String title, LocalDateTime start, LocalDateTime end) {
        VolunteerTask task = taskFactory.createTask(type, title, start, end);
        task.setTaskId(repository.nextId("task"));
        repository.saveTask(task);
        return task;
    }

    public void saveTask(VolunteerTask task) {
        repository.saveTask(task);
    }

    public VolunteerTask getTask(int taskId) {
        return repository.findTaskById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task " + taskId + " not found."));
    }

    public List<VolunteerTask> getAllTasks() {
        return repository.findAllTasks();
    }

    public void cancelTask(int taskId) {
        VolunteerTask task = getTask(taskId);
        for (VolunteerAssignment a : repository.findAssignmentsByTask(taskId)) {
            if (a.getStatus().isActive()) {
                a.cancel();
                repository.saveAssignment(a);
                notifier.notify(getVolunteer(a.getVolunteerId()), "Task cancelled",
                        "The task '" + task.getTitle() + "' has been cancelled.");
            }
        }
        task.setStatus(TaskStatus.CANCELLED);
        repository.saveTask(task);
    }

    public List<String> checkEligibility(Volunteer v, VolunteerTask t) {
        List<String> problems = new ArrayList<>();
        if (!v.isActive()) {
            problems.add(v.getName() + " is inactive.");
        }
        for (String skill : t.getRequiredSkills()) {
            if (!v.hasSkill(skill)) problems.add("Missing required skill: " + skill + ".");
        }
        if (!v.isAvailable(t.getStartTime(), t.getEndTime())) {
            problems.add(v.getName() + " is not available on " + t.getStartTime().getDayOfWeek()
                    + " " + t.getStartTime().toLocalTime() + "-" + t.getEndTime().toLocalTime() + ".");
        }
        return problems;
    }

    public boolean isEligible(Volunteer v, VolunteerTask t) {
        return checkEligibility(v, t).isEmpty();
    }

    public boolean hasConflict(Volunteer v, VolunteerTask t) {
        return findConflict(v, t) != null;
    }

    private VolunteerTask findConflict(Volunteer v, VolunteerTask t) {
        for (VolunteerAssignment a : repository.findAssignmentsByVolunteer(v.getUserId())) {
            if (!a.getStatus().isActive() || a.getTaskId() == t.getTaskId()) continue;
            VolunteerTask other = getTask(a.getTaskId());
            if (other.overlaps(t)) return other;
        }
        return null;
    }

    public List<Volunteer> findSuitableVolunteers(int taskId) {
        VolunteerTask t = getTask(taskId);
        return repository.findAll().stream()
                .filter(v -> isEligible(v, t) && !hasConflict(v, t) && !isAssigned(v, t))
                .collect(Collectors.toList());
    }

    private boolean isAssigned(Volunteer v, VolunteerTask t) {
        return repository.findAssignmentsByTask(t.getTaskId()).stream()
                .anyMatch(a -> a.getVolunteerId() == v.getUserId() && a.getStatus().isActive());
    }

    public VolunteerAssignment assignTask(int volunteerId, int taskId, String role, User assignedBy) {
        Volunteer v = getVolunteer(volunteerId);
        VolunteerTask t = getTask(taskId);

        if (assignedBy == null) {
            throw new IllegalArgumentException("Only a coordinator can assign volunteers.");
        }
        if (assignedBy.getUserId() == volunteerId) {
            throw new IllegalStateException("Volunteers cannot assign themselves.");
        }
        if (t.getStatus() == TaskStatus.CANCELLED || t.getStatus() == TaskStatus.COMPLETED) {
            throw new IllegalStateException("Task '" + t.getTitle() + "' is " + t.getStatus() + ".");
        }
        if (t.isFull()) {
            throw new IllegalStateException("Task '" + t.getTitle() + "' is full (" + t.getMaxVolunteers() + " places).");
        }
        if (isAssigned(v, t)) {
            throw new IllegalStateException(v.getName() + " is already assigned to this task.");
        }
        List<String> problems = checkEligibility(v, t);
        if (!problems.isEmpty()) {
            throw new IllegalStateException("Not eligible: " + String.join(" ", problems));
        }
        VolunteerTask clash = findConflict(v, t);
        if (clash != null) {
            throw new IllegalStateException("Schedule conflict: " + v.getName() + " is already on '"
                    + clash.getTitle() + "' at an overlapping time.");
        }

        VolunteerAssignment a = new VolunteerAssignment(repository.nextId("assignment"),
                volunteerId, taskId, role, assignedBy.getUserId());
        t.placeTaken();
        repository.saveAssignment(a);
        repository.saveTask(t);
        notifier.notify(v, "New assignment", "You have been asked to join '" + t.getTitle() + "' on "
                + t.getStartTime() + " as " + a.getRole() + ". Please accept or decline.");
        return a;
    }

    public void acceptAssignment(int assignmentId) {
        VolunteerAssignment a = getAssignment(assignmentId);
        a.accept();
        repository.saveAssignment(a);
    }

    public void declineAssignment(int assignmentId) {
        VolunteerAssignment a = getAssignment(assignmentId);
        a.decline();
        freePlace(a);
        repository.saveAssignment(a);
    }

    public void completeAssignment(int assignmentId) {
        VolunteerAssignment a = getAssignment(assignmentId);
        a.complete();
        repository.saveAssignment(a);
    }

    public void cancelAssignment(int assignmentId) {
        VolunteerAssignment a = getAssignment(assignmentId);
        a.cancel();
        freePlace(a);
        repository.saveAssignment(a);
    }

    private void freePlace(VolunteerAssignment a) {
        VolunteerTask t = getTask(a.getTaskId());
        t.placeFreed();
        repository.saveTask(t);
    }

    public VolunteerAssignment getAssignment(int assignmentId) {
        return repository.findAssignmentById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment " + assignmentId + " not found."));
    }

    public List<VolunteerAssignment> getAllAssignments() {
        return repository.findAllAssignments();
    }

    public VolunteerHourRecord recordHours(int assignmentId, LocalDate date, double hours) {
        VolunteerAssignment a = getAssignment(assignmentId);
        if (a.getStatus() != AssignmentStatus.ACCEPTED && a.getStatus() != AssignmentStatus.COMPLETED) {
            throw new IllegalStateException("Hours can only be logged on accepted or completed assignments (this one is "
                    + a.getStatus() + ").");
        }
        if (date != null && date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Hours cannot be logged for a future date.");
        }
        VolunteerHourRecord r = new VolunteerHourRecord(repository.nextId("hour"), assignmentId,
                a.getVolunteerId(), date, hours);
        repository.saveHourRecord(r);
        return r;
    }

    public void approveHours(int recordId, User approver) {
        VolunteerHourRecord r = getHourRecord(recordId);
        r.approve(approver);
        Volunteer v = getVolunteer(r.getVolunteerId());
        v.addApprovedHours(r.getHours());
        repository.saveHourRecord(r);
        repository.save(v);
    }

    public void rejectHours(int recordId, User approver) {
        VolunteerHourRecord r = getHourRecord(recordId);
        r.reject(approver);
        repository.saveHourRecord(r);
    }

    public VolunteerHourRecord getHourRecord(int recordId) {
        return repository.findHourRecordById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Hour record " + recordId + " not found."));
    }

    public List<VolunteerHourRecord> getAllHourRecords() {
        return repository.findAllHourRecords();
    }

    public List<VolunteerHourRecord> getHourRecords(int volunteerId) {
        return repository.findHourRecordsByVolunteer(volunteerId);
    }

    public List<VolunteerAssignment> getHistory(int volunteerId) {
        getVolunteer(volunteerId);
        return repository.findAssignmentsByVolunteer(volunteerId).stream()
                .sorted(Comparator.comparing((VolunteerAssignment a) -> getTask(a.getTaskId()).getStartTime()).reversed())
                .collect(Collectors.toList());
    }

    public VolunteerCertificate issueCertificate(int volunteerId, CertificateType type) {
        Volunteer v = getVolunteer(volunteerId);
        long completed = repository.findAssignmentsByVolunteer(volunteerId).stream()
                .filter(a -> a.getStatus() == AssignmentStatus.COMPLETED).count();
        double hours = v.getApprovedHours();

        if (type == CertificateType.PARTICIPATION && completed == 0) {
            throw new IllegalStateException(v.getName() + " has no completed tasks yet.");
        }
        if (type == CertificateType.HOURS_MILESTONE && hours < MILESTONE_HOURS) {
            throw new IllegalStateException(v.getName() + " has " + formatHours(hours) + " approved hours; "
                    + formatHours(MILESTONE_HOURS) + " are needed for an hours certificate.");
        }
        VolunteerCertificate c = new VolunteerCertificate(repository.nextId("certificate"), volunteerId,
                v.getName(), type, hours, (int) completed);
        repository.saveCertificate(c);
        return c;
    }

    public List<VolunteerCertificate> getCertificates(int volunteerId) {
        return repository.findCertificatesByVolunteer(volunteerId);
    }

    private void requireUniqueEmail(String email, int exceptVolunteerId) {
        if (email == null) return;
        boolean taken = repository.findAll().stream()
                .anyMatch(v -> v.getUserId() != exceptVolunteerId && v.getEmail().equalsIgnoreCase(email.trim()));
        if (taken) throw new IllegalArgumentException("A volunteer with email " + email + " already exists.");
    }

    private static String formatHours(double h) {
        return (h == Math.floor(h)) ? String.valueOf((long) h) : String.valueOf(h);
    }

    private static boolean contains(String text, String keyword) {
        return text != null && text.toLowerCase(Locale.ROOT).contains(keyword);
    }
}
