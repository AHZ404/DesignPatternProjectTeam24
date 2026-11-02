package beneficiary.model;

import java.io.Serializable;

public final class ImpactRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int id, beneficiaryId;
    private final String outcome, notes, createdAt;
    public ImpactRecord(int id, int beneficiaryId, String outcome, String notes, String createdAt) {
        this.id=id; this.beneficiaryId=beneficiaryId; this.outcome=outcome;
        this.notes=notes; this.createdAt=createdAt;
    }
    public int getId() { return id; }
    public int getBeneficiaryId() { return beneficiaryId; }
    public String getOutcome() { return outcome; }
    public String getNotes() { return notes; }
    public String getCreatedAt() { return createdAt; }
}
