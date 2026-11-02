package beneficiary.controller;

import beneficiary.model.*;
import beneficiary.service.BeneficiaryService;
import java.util.List;

/** MVC Controller: UI events go here, not directly to storage or inventory classes. */
public final class BeneficiaryController {
    private final BeneficiaryService service;
    public BeneficiaryController(BeneficiaryService service) { this.service=service; }
    public Beneficiary register(String name,String contact,String address) { return service.register(name,contact,address); }
    public Beneficiary update(int id,String name,String contact,String address) { return service.update(id,name,contact,address); }
    public Beneficiary deleteBeneficiary(int id) { return service.deleteBeneficiary(id); }
    public Resource deleteResource(int resourceId) { return service.deleteResource(resourceId); }
    public List<CrudMatrix.Row> crudMatrix() { return CrudMatrix.rows(); }
    public Beneficiary setStatus(int id,Beneficiary.Status status) { return service.setNeedStatus(id,status); }
    public List<Beneficiary> listBeneficiaries(String search) { return service.search(search); }
    public NeedAssessment addNeeds(int id,String resources,String services,String notes) { return service.assessNeeds(id,resources,services,notes); }
    public List<NeedAssessment> listNeeds() { return service.assessments(); }
    public AssistanceRequest requestResource(int beneficiaryId,int resourceId,int qty,String desc) { return service.requestResource(beneficiaryId,resourceId,qty,desc); }
    public AssistanceRequest requestService(int beneficiaryId,String desc) { return service.requestService(beneficiaryId,desc); }
    public AssistanceRequest cancelRequest(int requestId) { return service.cancelRequest(requestId); }
    public List<AssistanceRequest> listRequests() { return service.requests(); }
    public Resource addResource(String name,String category,int initial) { return service.addResource(name,category,initial); }
    public Resource restock(int resourceId,int qty) { return service.restock(resourceId,qty); }
    public List<Resource> listResources() { return service.resources(); }
    public ResourceDistribution distribute(int beneficiaryId,int resourceId,int qty,int requestId,String notes) {
        return service.distribute(beneficiaryId,resourceId,qty,requestId,notes);
    }
    public List<ResourceDistribution> listDistributions() { return service.distributions(); }
    public ServiceRecord recordService(int beneficiaryId,String name,int requestId,String notes) { return service.recordService(beneficiaryId,name,requestId,notes); }
    public List<ServiceRecord> listServices() { return service.services(); }
    public ImpactRecord recordImpact(int beneficiaryId,String outcome,String notes) { return service.recordImpact(beneficiaryId,outcome,notes); }
    public List<ImpactRecord> listImpacts() { return service.impacts(); }
    public String impactSummary(int beneficiaryId) { return service.impactSummary(beneficiaryId); }
    public String history(int beneficiaryId) { return service.history(beneficiaryId); }
}