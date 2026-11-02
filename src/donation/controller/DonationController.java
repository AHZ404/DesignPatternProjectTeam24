package donation.controller;

import donation.model.*;
import donation.service.DonationService;

import java.util.List;
import java.util.Objects;

/** MVC Controller for Donations following the exact pattern of BeneficiaryController. */
public class DonationController {
    private final DonationService donationService;

    public DonationController(DonationService donationService) {
        this.donationService = Objects.requireNonNull(donationService);
    }

    // Donors
    public Donor addDonor(String name, String email, String phone, String taxId) {
        return donationService.addDonor(name, email, phone, taxId);
    }

    public List<Donor> listDonors() {
        return donationService.listDonors();
    }

    public void updateDonor(int id, String name, String email, String phone, String taxId) {
        donationService.updateDonor(id, name, email, phone, taxId);
    }

    public void deleteDonor(int id) {
        donationService.deleteDonor(id);
    }

    // Donations
    public Donation recordDonation(int donorId, double amount, IPaymentStrategy strategy) {
        return donationService.recordDonation(donorId, amount, strategy);
    }

    public List<Donation> listDonations() {
        return donationService.listDonations();
    }

    public void deleteDonation(int donationId) {
        donationService.deleteDonation(donationId);
    }

    // Documents
    public String generateReceipt(int donationId) {
        return donationService.generateReceipt(donationId);
    }

    public String generateTaxSummary(int donorId, int year) {
        return donationService.generateTaxSummary(donorId, year);
    }
}