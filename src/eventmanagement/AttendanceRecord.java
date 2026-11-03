package eventmanagement;

import java.util.Date;

public class AttendanceRecord {
    private final int attendanceId;
    private Date checkInTime;
    private boolean attended;

    public AttendanceRecord(int attendanceId) {
        this.attendanceId = attendanceId;
    }

    public void markPresent() {
        this.attended = true;
        this.checkInTime = new Date();
    }

    public int getAttendanceId() { return attendanceId; }
    public Date getCheckInTime() { return checkInTime; }
    public boolean isAttended() { return attended; }
}
