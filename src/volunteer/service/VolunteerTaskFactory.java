package volunteer.service;

import volunteer.model.FundraiserTask;
import volunteer.model.OutreachTask;
import volunteer.model.TaskType;
import volunteer.model.VolunteerTask;
import volunteer.model.WorkshopTask;

import java.time.LocalDateTime;

public class VolunteerTaskFactory {
    public VolunteerTask createTask(TaskType type, String title, LocalDateTime start, LocalDateTime end) {
        if (type == null) {
            throw new IllegalArgumentException("Task type is required.");
        }
        switch (type) {
            case FUNDRAISER:
                return new FundraiserTask(title, start, end);
            case WORKSHOP:
                return new WorkshopTask(title, start, end);
            case OUTREACH:
                return new OutreachTask(title, start, end);
            default:
                throw new IllegalArgumentException("Unknown task type: " + type);
        }
    }
}
