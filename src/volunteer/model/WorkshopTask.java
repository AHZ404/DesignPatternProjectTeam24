package volunteer.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class WorkshopTask extends VolunteerTask {
    private static final long serialVersionUID = 1L;

    private String topic = "";
    private List<String> materials = new ArrayList<>();

    private WorkshopTask() {
    }

    public WorkshopTask(String title, LocalDateTime start, LocalDateTime end) {
        super(title, start, end);
    }

    @Override
    public TaskType getType() {
        return TaskType.WORKSHOP;
    }

    @Override
    public void execute() {
        status = TaskStatus.IN_PROGRESS;
        System.out.println("Workshop '" + title + "' started on topic: " + topic + " (materials: " + materials + ")");
    }

    @Override
    public String getTypeDetails() {
        return "Topic: " + topic;
    }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public List<String> getMaterials() { return materials; }
    public void setMaterials(List<String> materials) { this.materials = new ArrayList<>(materials); }
}
