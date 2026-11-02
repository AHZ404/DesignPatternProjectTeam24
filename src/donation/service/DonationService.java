package donation.service;

import beneficiary.model.Resource;
import database.JsonRepository;
import donation.model.*;

import java.util.*;

public class DonationService {
    private final JsonRepository repository;

    public DonationService(JsonRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    // --- DONOR CRUD ---
    public Donor addDonor(String name, String email, String phone, String taxId) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Donor name required");
        return repository.update(db -> {
            Donor donor = new Donor(db.nextUserId++, name.trim(), email.trim(), phone.trim(), taxId.trim());
            db.donors.put(donor.getDonorId(), donor);
            return donor;
        });
    }

    public List<Donor> listDonors() {
        return repository.read(db -> new ArrayList<>(db.donors.values()));
    }

    public void updateDonor(int donorId, String name, String email, String phone, String taxId) {
        repository.update(db -> {
            Donor donor = db.donors.get(donorId);
            if (donor == null) throw new IllegalArgumentException("Donor not found ID: " + donorId);
            donor.setName(name);
            donor.setEmail(email);
            donor.setPhone(phone);
            donor.setTaxId(taxId);
            return null;
        });
    }

    public void deleteDonor(int donorId) {
        repository.update(db -> {
            if (!db.donors.containsKey(donorId)) throw new IllegalArgumentException("Donor not found ID: " + donorId);
            db.donors.remove(donorId);
            return null;
        });
    }

    // --- DONATION CRUD & INVENTORY SYNC ---
    public Donation recordDonation(int donorId, double amount, IPaymentStrategy paymentMethod) {
        return repository.update(db -> {
            Donor donor = db.donors.get(donorId);
            if (donor == null) throw new IllegalArgumentException("Donor not found ID: " + donorId);

            boolean success = paymentMethod.ProcessPayment(amount);
            String status = success ? "COMPLETED" : "FAILED";

            Donation donation = new Donation(db.nextDonationId++, donorId, amount, new Date(), status, paymentMethod);
            db.donations.put(donation.getDonationId(), donation);

            // Automatically inject items/funds into the shared Inventory
            if (success) {
                int resId = db.nextId++;
                Resource resource;
                if (paymentMethod instanceof InKindPayment) {
                    InKindPayment inKind = (InKindPayment) paymentMethod;
                    resource = new Resource(resId, inKind.getItemName(), inKind.getItemCategory(), inKind.getUnitQuantity());
                } else {
                    String method = paymentMethod.getMethodName();
                    resource = new Resource(resId, "Financial Funds (" + method + ")", "FINANCIAL", (int) amount);
                }
                db.resources.put(resource.getId(), resource);
            }
            return donation;
        });
    }

    public List<Donation> listDonations() {
        return repository.read(db -> new ArrayList<>(db.donations.values()));
    }

    public void deleteDonation(int donationId) {
        repository.update(db -> {
            if (!db.donations.containsKey(donationId)) throw new IllegalArgumentException("Donation not found ID: " + donationId);
            db.donations.remove(donationId);
            return null;
        });
    }

    public String generateReceipt(int donationId) {
        return repository.read(db -> {
            Donation d = db.donations.get(donationId);
            if (d == null) throw new IllegalArgumentException("Invalid donation ID");
            Donor donor = db.donors.get(d.getDonorId());

            StringBuilder sb = new StringBuilder();
            sb.append("=========================================\n");
            sb.append("           OFFICIAL DONATION RECEIPT      \n");
            sb.append("=========================================\n");
            sb.append("Receipt No   : REC-").append(d.getDonationId()).append("\n");
            sb.append("Date         : ").append(d.getDonationDate()).append("\n");
            sb.append("Donor Name   : ").append(donor != null ? donor.getName() : "N/A").append("\n");
            sb.append("Tax ID / SSN : ").append(donor != null ? donor.getTaxId() : "N/A").append("\n");
            sb.append("Method       : ").append(d.getPaymentStrategy().getMethodName()).append("\n");

            // Check if the payment strategy is an In-Kind donation
            if (d.getPaymentStrategy() instanceof InKindPayment) {
                InKindPayment inKind = (InKindPayment) d.getPaymentStrategy();
                sb.append("Item Name    : ").append(inKind.getItemName()).append("\n");
                sb.append("Category     : ").append(inKind.getItemCategory()).append("\n");
                sb.append("Quantity     : ").append(inKind.getUnitQuantity()).append("\n");
                sb.append("Est. Value   : $").append(String.format("%.2f", d.getAmount())).append("\n");
            } else {
                sb.append("Amount/Value : $").append(String.format("%.2f", d.getAmount())).append("\n");
            }

            sb.append("Status       : ").append(d.getStatus()).append("\n");
            sb.append("=========================================\n");
            sb.append("Thank you for your generous contribution!\n");
            return sb.toString();
        });
    }

    public String generateTaxSummary(int donorId, int year) {
        return repository.read(db -> {
            Donor donor = db.donors.get(donorId);
            if (donor == null) throw new IllegalArgumentException("Donor not found ID: " + donorId);

            double total = 0.0;
            StringBuilder sb = new StringBuilder();
            sb.append("=========================================\n");
            sb.append("      ANNUAL TAX DEDUCTION STATEMENT     \n");
            sb.append("=========================================\n");
            sb.append("Taxpayer Name : ").append(donor.getName()).append("\n");
            sb.append("Tax ID / SSN  : ").append(donor.getTaxId()).append("\n");
            sb.append("Tax Year      : ").append(year).append("\n");
            sb.append("-----------------------------------------\n");

            Calendar cal = Calendar.getInstance();
            for (Donation d : db.donations.values()) {
                if (d.getDonorId() == donorId && "COMPLETED".equalsIgnoreCase(d.getStatus())) {
                    cal.setTime(d.getDonationDate());
                    if (cal.get(Calendar.YEAR) == year) {
                        sb.append(String.format("- %s | %-12s | $%.2f\n", d.getDonationDate(), d.getPaymentStrategy().getMethodName(), d.getAmount()));
                        total += d.getAmount();
                    }
                }
            }
            sb.append("-----------------------------------------\n");
            sb.append("TOTAL TAX-DEDUCTIBLE CONTRIBUTIONS: $").append(String.format("%.2f", total)).append("\n");
            sb.append("=========================================\n");
            return sb.toString();
        });
    }
}