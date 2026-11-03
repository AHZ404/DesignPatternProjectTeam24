package volunteer.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.io.Serializable;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class AvailabilitySlot implements Serializable {
    private static final long serialVersionUID = 1L;

    private int slotId;
    private DayOfWeek day;
    private LocalTime startTime;
    private LocalTime endTime;

    private AvailabilitySlot() {
    }

    public AvailabilitySlot(int slotId, DayOfWeek day, LocalTime startTime, LocalTime endTime) {
        if (day == null || startTime == null || endTime == null) {
            throw new IllegalArgumentException("Day, start time and end time are required.");
        }
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("End time must be after start time.");
        }
        this.slotId = slotId;
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public boolean covers(LocalDateTime start, LocalDateTime end) {
        if (!start.toLocalDate().equals(end.toLocalDate())) {
            return false;
        }
        return start.getDayOfWeek() == day
                && !start.toLocalTime().isBefore(startTime)
                && !end.toLocalTime().isAfter(endTime);
    }

    public int getSlotId() { return slotId; }
    public DayOfWeek getDay() { return day; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }

    @Override
    public String toString() {
        return day + " " + startTime + "-" + endTime;
    }
}
