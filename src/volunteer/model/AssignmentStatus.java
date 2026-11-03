package volunteer.model;

public enum AssignmentStatus {
    PENDING, ACCEPTED, DECLINED, COMPLETED, CANCELLED;

    public boolean isActive() {
        return this == PENDING || this == ACCEPTED;
    }
}
