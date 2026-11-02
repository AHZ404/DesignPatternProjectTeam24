package beneficiary.service;

import beneficiary.model.*;
import database.JsonRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public final class BeneficiaryService {
    private final JsonRepository repository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public BeneficiaryService(JsonRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    private static String now() {
        return LocalDateTime.now().format(FORMATTER);
    }

    // --- BENEFICIARY CRUD ---
    public Beneficiary register(String name, String contact, String address) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name required");
        return repository.update(db -> {
            Beneficiary b = new Beneficiary(db.nextUserId++, name.trim(), contact.trim(), address.trim(), Beneficiary.Status.PENDING);
            db.beneficiaries.put(b.getId(), b);
            return b;
        });
    }

    public List<Beneficiary> listBeneficiaries(String query) {
        return repository.read(db -> {
            if (query == null || query.isBlank()) return new ArrayList<>(db.beneficiaries.values());
            String q = query.trim().toLowerCase();
            List<Beneficiary> result = new ArrayList<>();
            for (Beneficiary b : db.beneficiaries.values()) {
                if (String.valueOf(b.getId()).contains(q) ||
                        b.getName().toLowerCase().contains(q) ||
                        b.getContact().toLowerCase().contains(q)) {
                    result.add(b);
                }
            }
            return result;
        });
    }

    public void update(int id, String name, String contact, String address) {
        repository.update(db -> {
            Beneficiary existing = db.beneficiaries.get(id);
            if (existing == null) throw new IllegalArgumentException("Beneficiary not found ID: " + id);
            Beneficiary updated = new Beneficiary(id, name, contact, address, existing.getStatus());
            db.beneficiaries.put(id, updated);
            return null;
        });
    }

    public void deleteBeneficiary(int id) {
        repository.update(db -> {
            if (!db.beneficiaries.containsKey(id)) throw new IllegalArgumentException("Beneficiary not found ID: " + id);
            db.beneficiaries.remove(id);
            return null;
        });
    }

    public void setStatus(int id, Beneficiary.Status status) {
        repository.update(db -> {
            Beneficiary existing = db.beneficiaries.get(id);
            if (existing == null) throw new IllegalArgumentException("Beneficiary not found ID: " + id);
            Beneficiary updated = new Beneficiary(id, existing.getName(), existing.getContact(), existing.getAddress(), status);
            db.beneficiaries.put(id, updated);
            return null;
        });
    }

    // --- NEEDS & REQUESTS CRUD ---
    public NeedAssessment addNeeds(int beneficiaryId, String resourceNeeds, String serviceNeeds, String notes) {
        return repository.update(db -> {
            if (!db.beneficiaries.containsKey(beneficiaryId)) throw new IllegalArgumentException("Beneficiary not found");
            NeedAssessment n = new NeedAssessment(db.nextId++, beneficiaryId, resourceNeeds, serviceNeeds, notes, now());
            db.needs.put(n.getId(), n);
            return n;
        });
    }

    public List<NeedAssessment> listNeeds() {
        return repository.read(db -> new ArrayList<>(db.needs.values()));
    }

    public AssistanceRequest requestResource(int beneficiaryId, int resourceId, int quantity, String description) {
        return repository.update(db -> {
            if (!db.beneficiaries.containsKey(beneficiaryId)) throw new IllegalArgumentException("Beneficiary not found");
            if (!db.resources.containsKey(resourceId)) throw new IllegalArgumentException("Resource not found");
            AssistanceRequest r = new AssistanceRequest(
                    db.nextId++, beneficiaryId, AssistanceRequest.Type.RESOURCE, resourceId,
                    quantity, 0, description, AssistanceRequest.Status.PENDING, now()
            );
            db.requests.put(r.getId(), r);
            return r;
        });
    }

    public AssistanceRequest requestService(int beneficiaryId, String description) {
        return repository.update(db -> {
            if (!db.beneficiaries.containsKey(beneficiaryId)) throw new IllegalArgumentException("Beneficiary not found");
            AssistanceRequest r = new AssistanceRequest(
                    db.nextId++, beneficiaryId, AssistanceRequest.Type.SERVICE, 0,
                    1, 0, description, AssistanceRequest.Status.PENDING, now()
            );
            db.requests.put(r.getId(), r);
            return r;
        });
    }

    public List<AssistanceRequest> listRequests() {
        return repository.read(db -> new ArrayList<>(db.requests.values()));
    }

    public void cancelRequest(int requestId) {
        repository.update(db -> {
            AssistanceRequest r = db.requests.get(requestId);
            if (r == null) throw new IllegalArgumentException("Request not found ID: " + requestId);
            AssistanceRequest cancelled = new AssistanceRequest(
                    r.getId(), r.getBeneficiaryId(), r.getType(), r.getResourceId(),
                    r.getQuantity(), r.getFulfilledQuantity(), r.getDescription(), AssistanceRequest.Status.CANCELLED, r.getCreatedAt()
            );
            db.requests.put(requestId, cancelled);
            return null;
        });
    }

    // --- INVENTORY / RESOURCE CRUD ---
    public Resource addResource(String name, String category, int stock) {
        return repository.update(db -> {
            Resource r = new Resource(db.nextId++, name, category, stock);
            db.resources.put(r.getId(), r);
            return r;
        });
    }

    public List<Resource> listResources() {
        return repository.read(db -> new ArrayList<>(db.resources.values()));
    }

    public void restock(int resourceId, int amount) {
        repository.update(db -> {
            Resource existing = db.resources.get(resourceId);
            if (existing == null) throw new IllegalArgumentException("Resource not found");
            Resource updated = new Resource(resourceId, existing.getName(), existing.getCategory(), existing.getStock() + amount);
            db.resources.put(resourceId, updated);
            return null;
        });
    }

    public void deleteResource(int resourceId) {
        repository.update(db -> {
            if (!db.resources.containsKey(resourceId)) throw new IllegalArgumentException("Resource not found");
            db.resources.remove(resourceId);
            return null;
        });
    }

    public ResourceDistribution distribute(int beneficiaryId, int resourceId, int quantity, int requestId, String notes) {
        return repository.update(db -> {
            Beneficiary b = db.beneficiaries.get(beneficiaryId);
            if (b == null) throw new IllegalArgumentException("Beneficiary not found");
            Resource r = db.resources.get(resourceId);
            if (r == null) throw new IllegalArgumentException("Resource not found");

            if (r.getStock() < quantity) throw new IllegalArgumentException("Insufficient stock: only " + r.getStock() + " available");

            // Update resource stock
            Resource updatedResource = new Resource(resourceId, r.getName(), r.getCategory(), r.getStock() - quantity);
            db.resources.put(resourceId, updatedResource);

            // Update linked assistance request if present
            if (requestId > 0) {
                AssistanceRequest req = db.requests.get(requestId);
                if (req != null) {
                    int newFulfilled = req.getFulfilledQuantity() + quantity;
                    AssistanceRequest.Status newStatus = newFulfilled >= req.getQuantity() ?
                            AssistanceRequest.Status.FULFILLED : AssistanceRequest.Status.IN_PROGRESS;
                    AssistanceRequest updatedReq = new AssistanceRequest(
                            req.getId(), req.getBeneficiaryId(), req.getType(), req.getResourceId(),
                            req.getQuantity(), newFulfilled, req.getDescription(), newStatus, req.getCreatedAt()
                    );
                    db.requests.put(requestId, updatedReq);
                }
            }

            ResourceDistribution dist = new ResourceDistribution(db.nextId++, beneficiaryId, resourceId, quantity, requestId, notes, now());
            db.distributions.put(dist.getId(), dist);
            return dist;
        });
    }

    public List<ResourceDistribution> listDistributions() {
        return repository.read(db -> new ArrayList<>(db.distributions.values()));
    }

    // --- SERVICES & IMPACT CRUD ---
    public ServiceRecord recordService(int beneficiaryId, String serviceName, int requestId, String notes) {
        return repository.update(db -> {
            if (!db.beneficiaries.containsKey(beneficiaryId)) throw new IllegalArgumentException("Beneficiary not found");
            if (requestId > 0) {
                AssistanceRequest req = db.requests.get(requestId);
                if (req != null) {
                    int newFulfilled = req.getFulfilledQuantity() + 1;
                    AssistanceRequest.Status newStatus = newFulfilled >= req.getQuantity() ?
                            AssistanceRequest.Status.FULFILLED : AssistanceRequest.Status.IN_PROGRESS;
                    AssistanceRequest updatedReq = new AssistanceRequest(
                            req.getId(), req.getBeneficiaryId(), req.getType(), req.getResourceId(),
                            req.getQuantity(), newFulfilled, req.getDescription(), newStatus, req.getCreatedAt()
                    );
                    db.requests.put(requestId, updatedReq);
                }
            }
            ServiceRecord s = new ServiceRecord(db.nextId++, beneficiaryId, requestId, serviceName, notes, now());
            db.services.put(s.getId(), s);
            return s;
        });
    }

    public List<ServiceRecord> listServices() {
        return repository.read(db -> new ArrayList<>(db.services.values()));
    }

    public ImpactRecord recordImpact(int beneficiaryId, String outcome, String notes) {
        return repository.update(db -> {
            if (!db.beneficiaries.containsKey(beneficiaryId)) throw new IllegalArgumentException("Beneficiary not found");
            ImpactRecord imp = new ImpactRecord(db.nextId++, beneficiaryId, outcome, notes, now());
            db.impacts.put(imp.getId(), imp);
            return imp;
        });
    }

    public List<ImpactRecord> listImpacts() {
        return repository.read(db -> new ArrayList<>(db.impacts.values()));
    }

    public String history(int beneficiaryId) {
        return repository.read(db -> {
            StringBuilder sb = new StringBuilder("Assistance History for Beneficiary #" + beneficiaryId + "\n\n");
            sb.append("DISTRIBUTIONS:\n");
            for (ResourceDistribution d : db.distributions.values()) {
                if (d.getBeneficiaryId() == beneficiaryId) {
                    sb.append("- ").append(d.getCreatedAt()).append(": Resource #").append(d.getResourceId())
                            .append(" x").append(d.getQuantity()).append("\n");
                }
            }
            sb.append("\nSERVICES:\n");
            for (ServiceRecord s : db.services.values()) {
                if (s.getBeneficiaryId() == beneficiaryId) {
                    sb.append("- ").append(s.getCreatedAt()).append(": ").append(s.getServiceName()).append("\n");
                }
            }
            return sb.toString();
        });
    }

    public String impactSummary(int beneficiaryId) {
        return repository.read(db -> {
            StringBuilder sb = new StringBuilder("Impact Summary for Beneficiary #" + beneficiaryId + "\n\n");
            for (ImpactRecord i : db.impacts.values()) {
                if (i.getBeneficiaryId() == beneficiaryId) {
                    sb.append("- ").append(i.getCreatedAt()).append(": ").append(i.getOutcome()).append("\n");
                }
            }
            return sb.toString();
        });
    }

    public List<CrudMatrix.Row> crudMatrix() {
        return CrudMatrix.rows();
    }
}