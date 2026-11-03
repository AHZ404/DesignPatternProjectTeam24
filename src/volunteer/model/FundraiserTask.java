package volunteer.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.time.LocalDateTime;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class FundraiserTask extends VolunteerTask {
    private static final long serialVersionUID = 1L;

    private double targetAmount;

    private FundraiserTask() {
    }

    public FundraiserTask(String title, LocalDateTime start, LocalDateTime end) {
        super(title, start, end);
    }

    @Override
    public TaskType getType() {
        return TaskType.FUNDRAISER;
    }

    @Override
    public void execute() {
        status = TaskStatus.IN_PROGRESS;
        System.out.println("Fundraiser '" + title + "' started: collecting donations toward EGP " + targetAmount);
    }

    @Override
    public String getTypeDetails() {
        return "Target: " + targetAmount;
    }

    public double getTargetAmount() { return targetAmount; }
    public void setTargetAmount(double targetAmount) {
        if (targetAmount < 0) throw new IllegalArgumentException("Target amount cannot be negative.");
        this.targetAmount = targetAmount;
    }
}
