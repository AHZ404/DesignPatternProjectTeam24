package database;

import beneficiary.model.*;
import donation.model.*;
import java.io.Serializable;
import java.util.*;

public class Database implements Serializable {
    private static final long serialVersionUID = 1L;

    // Unified counter for all human user entities (Donors & Beneficiaries)
    public int nextUserId = 1;

    // Counter for transaction & inventory records
    public int nextId = 1;
    public int nextDonationId = 1;

    // Beneficiary Module
    public Map<Integer, Beneficiary> beneficiaries = new LinkedHashMap<>();
    public Map<Integer, NeedAssessment> needs = new LinkedHashMap<>();
    public Map<Integer, AssistanceRequest> requests = new LinkedHashMap<>();
    public Map<Integer, Resource> resources = new LinkedHashMap<>();
    public Map<Integer, ResourceDistribution> distributions = new LinkedHashMap<>();
    public Map<Integer, ServiceRecord> services = new LinkedHashMap<>();
    public Map<Integer, ImpactRecord> impacts = new LinkedHashMap<>();

    // Donation Module
    public Map<Integer, Donor> donors = new LinkedHashMap<>();
    public Map<Integer, Donation> donations = new LinkedHashMap<>();

    public Database() { }
}