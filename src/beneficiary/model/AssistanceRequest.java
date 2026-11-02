package beneficiary.model;

import java.io.Serializable;

public final class AssistanceRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    public enum Type { RESOURCE, SERVICE }
    public enum Status { PENDING, IN_PROGRESS, FULFILLED, CANCELLED }
    private final int id, beneficiaryId, resourceId, quantity, fulfilledQuantity;
    private final Type type;
    private final String description, createdAt;
    private final Status status;

    public AssistanceRequest(int id, int beneficiaryId, Type type, int resourceId, int quantity,
            int fulfilledQuantity, String description, Status status, String createdAt) {
        this.id=id; this.beneficiaryId=beneficiaryId; this.type=type; this.resourceId=resourceId;
        this.quantity=quantity; this.fulfilledQuantity=fulfilledQuantity; this.description=description;
        this.status=status; this.createdAt=createdAt;
    }
    public int getId() { return id; }
    public int getBeneficiaryId() { return beneficiaryId; }
    public Type getType() { return type; }
    public int getResourceId() { return resourceId; }
    public int getQuantity() { return quantity; }
    public int getFulfilledQuantity() { return fulfilledQuantity; }
    public String getDescription() { return description; }
    public Status getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
}
