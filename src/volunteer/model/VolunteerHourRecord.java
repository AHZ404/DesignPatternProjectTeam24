package volunteer.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.io.Serializable;

import user.model.User;

import java.time.LocalDate;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class VolunteerHourRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private int recordId;
    private int assignmentId;
    private int volunteerId;
    private LocalDate date;
    private double hours;
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;
    private Integer approvedById;

    private VolunteerHourRecord() {
    }

    public VolunteerHourRecord(int recordId, int assignmentId, int volunteerId, LocalDate date, double hours) {
        if (hours <= 0 || hours > 24) {
            throw new IllegalArgumentException("Hours must be more than 0 and at most 24.");
        }
        if (date == null) {
            throw new IllegalArgumentException("Date is required.");
        }
        this.recordId = recordId;
        this.assignmentId = assignmentId;
        this.volunteerId = volunteerId;
        this.date = date;
        this.hours = hours;
    }

    public void approve(User approver) {
        decide(approver, ApprovalStatus.APPROVED);
    }

    public void reject(User approver) {
        decide(approver, ApprovalStatus.REJECTED);
    }

    private void decide(User approver, ApprovalStatus result) {
        if (approvalStatus != ApprovalStatus.PENDING) {
            throw new IllegalStateException("This record is already " + approvalStatus + ".");
        }
        if (approver == null) {
            throw new IllegalArgumentException("An approver is required.");
        }
        if (approver.getUserId() == volunteerId) {
            throw new IllegalStateException("Volunteers cannot approve their own hours.");
        }
        approvalStatus = result;
        approvedById = approver.getUserId();
    }

    public int getRecordId() { return recordId; }
    public int getAssignmentId() { return assignmentId; }
    public int getVolunteerId() { return volunteerId; }
    public LocalDate getDate() { return date; }
    public double getHours() { return hours; }
    public ApprovalStatus getApprovalStatus() { return approvalStatus; }
    public Integer getApprovedById() { return approvedById; }
}
