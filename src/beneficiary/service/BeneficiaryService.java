package beneficiary.service;

import beneficiary.model.*;
import beneficiary.repository.BeneficiaryRepository;
import beneficiary.repository.BeneficiaryRepository.Data;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Business logic for the agreed BEN-01 through BEN-10 requirements. */
public final class BeneficiaryService {
    private final BeneficiaryRepository repository;
    private final InventoryManager inventory=InventoryManager.getInstance();
    public BeneficiaryService(Path path) { this(new BeneficiaryRepository(path)); }
    public BeneficiaryService(BeneficiaryRepository repository) { this.repository=Objects.requireNonNull(repository); }

    private static String required(String s, String field) {
        if (s==null || s.trim().isEmpty()) throw new IllegalArgumentException(field + " cannot be empty");
        return s.trim();
    }
    private static String optional(String s) { return s==null ? "" : s.trim(); }
    private static String time() { return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")); }
    private static int id(Data d) { return d.nextId++; }
    private static Beneficiary beneficiary(Data d,int id) {
        Beneficiary b=d.beneficiaries.get(id);
        if (b==null) throw new IllegalArgumentException("Beneficiary #" + id + " does not exist");
        return b;
    }
    private static Resource resource(Data d,int id) {
        Resource r=d.resources.get(id);
        if (r==null) throw new IllegalArgumentException("Resource #" + id + " does not exist");
        return r;
    }
    private static AssistanceRequest request(Data d,int id) {
        AssistanceRequest r=d.requests.get(id);
        if (r==null) throw new IllegalArgumentException("Request #" + id + " does not exist");
        return r;
    }

    // BEN-01, BEN-02, BEN-10
    public Beneficiary register(String name,String contact,String address) {
        final String n=required(name,"Name"), c=required(contact,"Contact");
        return repository.change(d -> {
            Beneficiary b=new Beneficiary(id(d),n,c,optional(address),Beneficiary.Status.PENDING);
            d.beneficiaries.put(b.getId(),b); return b;
        });
    }
    public Beneficiary update(int beneficiaryId,String name,String contact,String address) {
        final String n=required(name,"Name"), c=required(contact,"Contact");
        return repository.change(d -> {
            Beneficiary b=beneficiary(d,beneficiaryId);
            Beneficiary updated=new Beneficiary(b.getId(),n,c,optional(address),b.getStatus());
            d.beneficiaries.put(b.getId(),updated); return updated;
        });
    }
    public Beneficiary setNeedStatus(int beneficiaryId,Beneficiary.Status status) {
        Objects.requireNonNull(status,"status");
        return repository.change(d -> {
            Beneficiary b=beneficiary(d,beneficiaryId);
            Beneficiary updated=new Beneficiary(b.getId(),b.getName(),b.getContact(),b.getAddress(),status);
            d.beneficiaries.put(b.getId(),updated); return updated;
        });
    }
    public List<Beneficiary> beneficiaries() {
        return repository.read(d -> new ArrayList<>(d.beneficiaries.values()));
    }
    public List<Beneficiary> search(String query) {
        final String q=optional(query).toLowerCase(Locale.ROOT);
        return repository.read(d -> {
            // Prefer an exact numeric ID when it exists, instead of matching phone fragments.
            if (q.matches("[0-9]+")) {
                try {
                    Beneficiary exact=d.beneficiaries.get(Integer.parseInt(q));
                    if (exact!=null) return Collections.singletonList(exact);
                } catch (NumberFormatException ignored) { }
            }
            List<Beneficiary> matches=new ArrayList<>();
            for (Beneficiary b:d.beneficiaries.values()) {
                if (b.getName().toLowerCase(Locale.ROOT).contains(q)
                        || b.getContact().toLowerCase(Locale.ROOT).contains(q)
                        || String.valueOf(b.getId()).equals(q)) matches.add(b);
            }
            return matches;
        });
    }
    // BEN-03
    public NeedAssessment assessNeeds(int beneficiaryId,String resources,String services,String notes) {
        String r=optional(resources), s=optional(services);
        if (r.isEmpty() && s.isEmpty()) throw new IllegalArgumentException("Enter a resource need or service need");
        return repository.change(d -> {
            beneficiary(d,beneficiaryId);
            NeedAssessment a=new NeedAssessment(id(d),beneficiaryId,r,s,optional(notes),time());
            d.needs.put(a.getId(),a); return a;
        });
    }
    public List<NeedAssessment> assessments() { return repository.read(d -> new ArrayList<>(d.needs.values())); }

    // BEN-04: requests can be recorded independently of resource availability.
    public AssistanceRequest requestResource(int beneficiaryId,int resourceId,int quantity,String description) {
        if (quantity<=0) throw new IllegalArgumentException("Quantity must be positive");
        final String desc=required(description,"Request description");
        return repository.change(d -> {
            beneficiary(d,beneficiaryId); resource(d,resourceId);
            AssistanceRequest r=new AssistanceRequest(id(d),beneficiaryId,AssistanceRequest.Type.RESOURCE,
                    resourceId,quantity,0,desc,AssistanceRequest.Status.PENDING,time());
            d.requests.put(r.getId(),r); return r;
        });
    }
    public AssistanceRequest requestService(int beneficiaryId,String description) {
        final String desc=required(description,"Service description");
        return repository.change(d -> {
            beneficiary(d,beneficiaryId);
            AssistanceRequest r=new AssistanceRequest(id(d),beneficiaryId,AssistanceRequest.Type.SERVICE,
                    0,0,0,desc,AssistanceRequest.Status.PENDING,time());
            d.requests.put(r.getId(),r); return r;
        });
    }
    public AssistanceRequest cancelRequest(int requestId) {
        return repository.change(d -> {
            AssistanceRequest r=request(d,requestId);
            if (r.getStatus()==AssistanceRequest.Status.FULFILLED || r.getStatus()==AssistanceRequest.Status.CANCELLED)
                throw new IllegalArgumentException("Request already closed");
            if (r.getFulfilledQuantity()>0) throw new IllegalArgumentException("Cannot cancel a partially fulfilled request");
            AssistanceRequest changed=new AssistanceRequest(r.getId(),r.getBeneficiaryId(),r.getType(),
                    r.getResourceId(),r.getQuantity(),r.getFulfilledQuantity(),r.getDescription(),
                    AssistanceRequest.Status.CANCELLED,r.getCreatedAt());
            d.requests.put(requestId,changed); return changed;
        });
    }
    public List<AssistanceRequest> requests() { return repository.read(d -> new ArrayList<>(d.requests.values())); }

    // BEN-05, BEN-08
    public Resource addResource(String name,String category,int stock) {
        if (stock<0) throw new IllegalArgumentException("Initial stock cannot be negative");
        final String n=required(name,"Resource name"), c=required(category,"Category");
        return repository.change(d -> {
            Resource r=new Resource(id(d),n,c,stock);
            d.resources.put(r.getId(),r); return r;
        });
    }
    public Resource restock(int resourceId,int quantity) {
        return repository.change(d -> {
            inventory.replenish(d,resourceId,quantity);
            return resource(d,resourceId);
        });
    }
    public boolean canDistribute(int resourceId,int quantity) {
        return repository.read(d -> inventory.isAvailable(d,resourceId,quantity));
    }
    public List<Resource> resources() { return repository.read(d -> new ArrayList<>(d.resources.values())); }

    /** 0 requestId means an unlinked distribution. Stock and request are updated in one transaction. */
    public ResourceDistribution distribute(int beneficiaryId,int resourceId,int quantity,int requestId,String notes) {
        if (quantity<=0) throw new IllegalArgumentException("Quantity must be positive");
        return repository.change(d -> {
            beneficiary(d,beneficiaryId); resource(d,resourceId);
            AssistanceRequest req=null;
            if (requestId!=0) {
                req=request(d,requestId);
                if (req.getBeneficiaryId()!=beneficiaryId || req.getType()!=AssistanceRequest.Type.RESOURCE
                        || req.getResourceId()!=resourceId) throw new IllegalArgumentException("Request does not match beneficiary and resource");
                if (req.getStatus()==AssistanceRequest.Status.CANCELLED || req.getStatus()==AssistanceRequest.Status.FULFILLED)
                    throw new IllegalArgumentException("Request is closed");
                if (quantity>req.getQuantity()-req.getFulfilledQuantity())
                    throw new IllegalArgumentException("Quantity exceeds unfulfilled request amount");
            }
            inventory.deduct(d,resourceId,quantity);
            ResourceDistribution distribution=new ResourceDistribution(id(d),beneficiaryId,resourceId,quantity,requestId,optional(notes),time());
            d.distributions.put(distribution.getId(),distribution);
            if (req!=null) {
                int fulfilled=req.getFulfilledQuantity()+quantity;
                AssistanceRequest.Status status=fulfilled==req.getQuantity()
                        ? AssistanceRequest.Status.FULFILLED : AssistanceRequest.Status.IN_PROGRESS;
                d.requests.put(req.getId(),new AssistanceRequest(req.getId(),req.getBeneficiaryId(),req.getType(),
                        req.getResourceId(),req.getQuantity(),fulfilled,req.getDescription(),status,req.getCreatedAt()));
            }
            return distribution;
        });
    }
    public List<ResourceDistribution> distributions() { return repository.read(d -> new ArrayList<>(d.distributions.values())); }

    // BEN-06: optional service request is closed when the service is delivered.
    public ServiceRecord recordService(int beneficiaryId,String serviceName,int requestId,String notes) {
        final String name=required(serviceName,"Service name");
        return repository.change(d -> {
            beneficiary(d,beneficiaryId);
            if (requestId!=0) {
                AssistanceRequest req=request(d,requestId);
                if (req.getBeneficiaryId()!=beneficiaryId || req.getType()!=AssistanceRequest.Type.SERVICE)
                    throw new IllegalArgumentException("Service request does not match beneficiary");
                if (req.getStatus()==AssistanceRequest.Status.CANCELLED || req.getStatus()==AssistanceRequest.Status.FULFILLED)
                    throw new IllegalArgumentException("Request is closed");
                d.requests.put(req.getId(),new AssistanceRequest(req.getId(),req.getBeneficiaryId(),req.getType(),
                        0,0,0,req.getDescription(),AssistanceRequest.Status.FULFILLED,req.getCreatedAt()));
            }
            ServiceRecord record=new ServiceRecord(id(d),beneficiaryId,requestId,name,optional(notes),time());
            d.services.put(record.getId(),record); return record;
        });
    }
    public List<ServiceRecord> services() { return repository.read(d -> new ArrayList<>(d.services.values())); }

    // BEN-09
    public ImpactRecord recordImpact(int beneficiaryId,String outcome,String notes) {
        final String result=required(outcome,"Outcome");
        return repository.change(d -> {
            beneficiary(d,beneficiaryId);
            ImpactRecord impact=new ImpactRecord(id(d),beneficiaryId,result,optional(notes),time());
            d.impacts.put(impact.getId(),impact); return impact;
        });
    }
    public List<ImpactRecord> impacts() { return repository.read(d -> new ArrayList<>(d.impacts.values())); }

    // BEN-09: concise, evidence-based impact summary (no invented success scores).
    public String impactSummary(int beneficiaryId) {
        return repository.read(d -> {
            Beneficiary b=beneficiary(d,beneficiaryId);
            int needs=0, requests=0, completed=0, pending=0, units=0, serviceCount=0, outcomes=0;
            String lastOutcome="Not assessed yet";
            for (NeedAssessment r:d.needs.values()) if (r.getBeneficiaryId()==beneficiaryId) needs++;
            for (AssistanceRequest r:d.requests.values()) if (r.getBeneficiaryId()==beneficiaryId) {
                requests++;
                if (r.getStatus()==AssistanceRequest.Status.FULFILLED) completed++;
                if (r.getStatus()==AssistanceRequest.Status.PENDING || r.getStatus()==AssistanceRequest.Status.IN_PROGRESS) pending++;
            }
            for (ResourceDistribution r:d.distributions.values()) if (r.getBeneficiaryId()==beneficiaryId) units+=r.getQuantity();
            for (ServiceRecord r:d.services.values()) if (r.getBeneficiaryId()==beneficiaryId) serviceCount++;
            for (ImpactRecord r:d.impacts.values()) if (r.getBeneficiaryId()==beneficiaryId) {
                outcomes++; lastOutcome=r.getOutcome()+" ("+r.getCreatedAt()+")";
            }
            return "PROGRAM IMPACT SUMMARY\nBeneficiary: "+b.getName()+" (#"+b.getId()+")\n"
                    +"Need status: "+b.getStatus()+"\n"
                    +"Needs assessments: "+needs+"\n"
                    +"Assistance requests: "+requests+" (fulfilled: "+completed+", open: "+pending+")\n"
                    +"Resource units delivered: "+units+"\n"
                    +"Services delivered: "+serviceCount+"\n"
                    +"Outcome assessments: "+outcomes+"\n"
                    +"Latest recorded outcome: "+lastOutcome;
        });
    }

    // BEN-07: chronological assistance history across types.
    public String history(int beneficiaryId) {
        return repository.read(d -> {
            Beneficiary b=beneficiary(d,beneficiaryId);
            StringBuilder text=new StringBuilder("BENEFICIARY #"+b.getId()+" — "+b.getName()+"\n");
            text.append("Contact: ").append(b.getContact()).append("\nNeed status: ").append(b.getStatus()).append("\n\n");
            for (NeedAssessment a:d.needs.values()) if (a.getBeneficiaryId()==beneficiaryId)
                text.append("NEED  ").append(a.getCreatedAt()).append(" | Resources: ").append(a.getResourceNeeds())
                    .append(" | Services: ").append(a.getServiceNeeds()).append(" | ").append(a.getNotes()).append("\n");
            for (AssistanceRequest r:d.requests.values()) if (r.getBeneficiaryId()==beneficiaryId)
                text.append("REQUEST #").append(r.getId()).append(" ").append(r.getType())
                    .append(" ").append(r.getStatus()).append(" | ").append(r.getDescription()).append("\n");
            for (ResourceDistribution x:d.distributions.values()) if (x.getBeneficiaryId()==beneficiaryId)
                text.append("AID  ").append(x.getCreatedAt()).append(" | ").append(x.getQuantity())
                    .append(" x ").append(resource(d,x.getResourceId()).getName()).append("\n");
            for (ServiceRecord x:d.services.values()) if (x.getBeneficiaryId()==beneficiaryId)
                text.append("SERVICE  ").append(x.getCreatedAt()).append(" | ").append(x.getServiceName()).append("\n");
            for (ImpactRecord x:d.impacts.values()) if (x.getBeneficiaryId()==beneficiaryId)
                text.append("IMPACT  ").append(x.getCreatedAt()).append(" | ").append(x.getOutcome())
                    .append(" | ").append(x.getNotes()).append("\n");
            return text.toString();
        });
    }
}
