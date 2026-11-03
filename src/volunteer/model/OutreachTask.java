package volunteer.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.time.LocalDateTime;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class OutreachTask extends VolunteerTask {
    private static final long serialVersionUID = 1L;

    private String targetArea = "";
    private String communityGroup = "";

    private OutreachTask() {
    }

    public OutreachTask(String title, LocalDateTime start, LocalDateTime end) {
        super(title, start, end);
    }

    @Override
    public TaskType getType() {
        return TaskType.OUTREACH;
    }

    @Override
    public void execute() {
        status = TaskStatus.IN_PROGRESS;
        System.out.println("Outreach '" + title + "' started in " + targetArea + " for " + communityGroup);
    }

    @Override
    public String getTypeDetails() {
        return "Area: " + targetArea;
    }

    public String getTargetArea() { return targetArea; }
    public void setTargetArea(String targetArea) { this.targetArea = targetArea; }
    public String getCommunityGroup() { return communityGroup; }
    public void setCommunityGroup(String communityGroup) { this.communityGroup = communityGroup; }
}
