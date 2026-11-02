package beneficiary.controller;

import beneficiary.model.*;
import beneficiary.service.BeneficiaryService;
import java.util.List;

/** MVC Controller: UI events go here, delegating to BeneficiaryService. */
public final class BeneficiaryController {
    private final BeneficiaryService service;

    public BeneficiaryController(BeneficiaryService service) {
        this.service = service;
    }

    public Beneficiary register(String name, String contact, String address) {
        return service.register(name, contact, address);
    }

    public void update(int id, String name, String contact, String address) {
        service.update(id, name, contact, address);
    }

    public void deleteBeneficiary(int id) {
        service.deleteBeneficiary(id);
    }

    public void deleteResource(int resourceId) {
        service.deleteResource(resourceId);
    }

    public List<CrudMatrix.Row> crudMatrix() {
        return service.crudMatrix();
    }

    public void setStatus(int id, Beneficiary.Status status) {
        service.setStatus(id, status);
    }

    public List<Beneficiary> listBeneficiaries(String search) {
        return service.listBeneficiaries(search);
    }

    public NeedAssessment addNeeds(int id, String resources, String services, String notes) {
        return service.addNeeds(id, resources, services, notes);
    }

    public List<NeedAssessment> listNeeds() {
        return service.listNeeds();
    }

    public AssistanceRequest requestResource(int beneficiaryId, int resourceId, int qty, String desc) {
        return service.requestResource(beneficiaryId, resourceId, qty, desc);
    }

    public AssistanceRequest requestService(int beneficiaryId, String desc) {
        return service.requestService(beneficiaryId, desc);
    }

    public void cancelRequest(int requestId) {
        service.cancelRequest(requestId);
    }

    public List<AssistanceRequest> listRequests() {
        return service.listRequests();
    }

    public Resource addResource(String name, String category, int initial) {
        return service.addResource(name, category, initial);
    }

    public void restock(int resourceId, int qty) {
        service.restock(resourceId, qty);
    }

    public List<Resource> listResources() {
        return service.listResources();
    }

    public ResourceDistribution distribute(int beneficiaryId, int resourceId, int qty, int requestId, String notes) {
        return service.distribute(beneficiaryId, resourceId, qty, requestId, notes);
    }

    public List<ResourceDistribution> listDistributions() {
        return service.listDistributions();
    }

    public ServiceRecord recordService(int beneficiaryId, String name, int requestId, String notes) {
        return service.recordService(beneficiaryId, name, requestId, notes);
    }

    public List<ServiceRecord> listServices() {
        return service.listServices();
    }

    public ImpactRecord recordImpact(int beneficiaryId, String outcome, String notes) {
        return service.recordImpact(beneficiaryId, outcome, notes);
    }

    public List<ImpactRecord> listImpacts() {
        return service.listImpacts();
    }

    public String impactSummary(int beneficiaryId) {
        return service.impactSummary(beneficiaryId);
    }

    public String history(int beneficiaryId) {
        return service.history(beneficiaryId);
    }
}