package volunteer.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.io.Serializable;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class Skill implements Serializable {
    private static final long serialVersionUID = 1L;

    private int skillId;
    private String name;
    private SkillLevel level;
    private boolean certified;

    private Skill() {
    }

    public Skill(int skillId, String name, SkillLevel level, boolean certified) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Skill name is required.");
        }
        this.skillId = skillId;
        this.name = name.trim();
        this.level = level == null ? SkillLevel.BEGINNER : level;
        this.certified = certified;
    }

    public void updateLevel(SkillLevel level) {
        this.level = level;
    }

    public boolean matches(String skillName) {
        return name.equalsIgnoreCase(skillName.trim());
    }

    public int getSkillId() { return skillId; }
    public String getName() { return name; }
    public SkillLevel getLevel() { return level; }
    public boolean isCertified() { return certified; }
    public void setCertified(boolean certified) { this.certified = certified; }

    @Override
    public String toString() {
        return name + " (" + level + (certified ? ", certified" : "") + ")";
    }
}
