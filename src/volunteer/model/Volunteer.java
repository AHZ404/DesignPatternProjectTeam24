package volunteer.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class Volunteer extends User {
    private static final long serialVersionUID = 1L;

    private List<String> preferences = new ArrayList<>();
    private VolunteerStatus status = VolunteerStatus.ACTIVE;
    private final List<Skill> skills = new ArrayList<>();
    private final List<AvailabilitySlot> availability = new ArrayList<>();
    private double approvedHours;

    private Volunteer() {
    }

    public Volunteer(int userId, String name, String email, String phone, List<String> preferences) {
        super(userId, name, email, phone);
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Volunteer name is required.");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("A valid email is required.");
        }
        if (preferences != null) {
            this.preferences = new ArrayList<>(preferences);
        }
    }

    public void addSkill(Skill skill) {
        if (hasSkill(skill.getName())) {
            throw new IllegalArgumentException(name + " already has the skill " + skill.getName() + ".");
        }
        skills.add(skill);
    }

    public void removeSkill(int skillId) {
        boolean removed = skills.removeIf(s -> s.getSkillId() == skillId);
        if (!removed) {
            throw new IllegalArgumentException("Skill " + skillId + " not found.");
        }
    }

    public boolean hasSkill(String skillName) {
        return skills.stream().anyMatch(s -> s.matches(skillName));
    }

    public void addAvailability(AvailabilitySlot slot) {
        availability.add(slot);
    }

    public void removeAvailability(int slotId) {
        boolean removed = availability.removeIf(s -> s.getSlotId() == slotId);
        if (!removed) {
            throw new IllegalArgumentException("Availability slot " + slotId + " not found.");
        }
    }

    public boolean isAvailable(LocalDateTime start, LocalDateTime end) {
        return availability.stream().anyMatch(slot -> slot.covers(start, end));
    }

    public double getApprovedHours() {
        return approvedHours;
    }

    public void addApprovedHours(double hours) {
        this.approvedHours += hours;
    }

    public void deactivate() { status = VolunteerStatus.INACTIVE; }
    public void activate() { status = VolunteerStatus.ACTIVE; }
    public boolean isActive() { return status == VolunteerStatus.ACTIVE; }

    public List<String> getPreferences() { return Collections.unmodifiableList(preferences); }
    public void setPreferences(List<String> preferences) { this.preferences = new ArrayList<>(preferences); }
    public VolunteerStatus getStatus() { return status; }
    public List<Skill> getSkills() { return Collections.unmodifiableList(skills); }
    public List<AvailabilitySlot> getAvailability() { return Collections.unmodifiableList(availability); }

    @Override
    public String toString() {
        return "#" + userId + " " + name;
    }
}
