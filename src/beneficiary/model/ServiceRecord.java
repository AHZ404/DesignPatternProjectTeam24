package beneficiary.model;

import java.io.Serializable;

public final class ServiceRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int id, beneficiaryId, requestId;
    private final String serviceName, notes, createdAt;
    public ServiceRecord(int id, int beneficiaryId, int requestId, String serviceName, String notes, String createdAt) {
        this.id=id; this.beneficiaryId=beneficiaryId; this.requestId=requestId;
        this.serviceName=serviceName; this.notes=notes; this.createdAt=createdAt;
    }
    public int getId() { return id; }
    public int getBeneficiaryId() { return beneficiaryId; }
    public int getRequestId() { return requestId; }
    public String getServiceName() { return serviceName; }
    public String getNotes() { return notes; }
    public String getCreatedAt() { return createdAt; }
}
