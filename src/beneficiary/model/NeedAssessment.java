package beneficiary.model;

import java.io.Serializable;

public final class NeedAssessment implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int id, beneficiaryId;
    private final String resourceNeeds, serviceNeeds, notes, createdAt;
    public NeedAssessment(int id, int beneficiaryId, String resourceNeeds, String serviceNeeds, String notes, String createdAt) {
        this.id=id; this.beneficiaryId=beneficiaryId; this.resourceNeeds=resourceNeeds;
        this.serviceNeeds=serviceNeeds; this.notes=notes; this.createdAt=createdAt;
    }
    public int getId() { return id; }
    public int getBeneficiaryId() { return beneficiaryId; }
    public String getResourceNeeds() { return resourceNeeds; }
    public String getServiceNeeds() { return serviceNeeds; }
    public String getNotes() { return notes; }
    public String getCreatedAt() { return createdAt; }
}
