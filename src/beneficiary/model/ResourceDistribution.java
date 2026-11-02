package beneficiary.model;

import java.io.Serializable;

public final class ResourceDistribution implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int id, beneficiaryId, resourceId, quantity, requestId;
    private final String notes, createdAt;
    public ResourceDistribution(int id, int beneficiaryId, int resourceId, int quantity, int requestId, String notes, String createdAt) {
        this.id=id; this.beneficiaryId=beneficiaryId; this.resourceId=resourceId;
        this.quantity=quantity; this.requestId=requestId; this.notes=notes; this.createdAt=createdAt;
    }
    public int getId() { return id; }
    public int getBeneficiaryId() { return beneficiaryId; }
    public int getResourceId() { return resourceId; }
    public int getQuantity() { return quantity; }
    public int getRequestId() { return requestId; }
    public String getNotes() { return notes; }
    public String getCreatedAt() { return createdAt; }
}
