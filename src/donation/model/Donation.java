package donation.model;

import java.io.Serializable;
import java.util.Date;

public class Donation implements Serializable {
    private static final long serialVersionUID = 1L;

    private int donationId;
    private int donorId;
    private double amount;
    private Date donationDate;
    private String status;
    private IPaymentStrategy paymentStrategy;

    public Donation(int donationId, int donorId, double amount, Date donationDate, String status, IPaymentStrategy paymentStrategy) {
        this.donationId = donationId;
        this.donorId = donorId;
        this.amount = amount;
        this.donationDate = donationDate;
        this.status = status;
        this.paymentStrategy = paymentStrategy;
    }

    public int getDonationId() {
        return donationId;
    }

    public void setDonationId(int donationId) {
        this.donationId = donationId;
    }

    public int getDonorId() {
        return donorId;
    }

    public void setDonorId(int donorId) {
        this.donorId = donorId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Date getDonationDate() {
        return donationDate;
    }

    public void setDonationDate(Date donationDate) {
        this.donationDate = donationDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public IPaymentStrategy getPaymentStrategy() {
        return paymentStrategy;
    }

    public void setPaymentStrategy(IPaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
    }

    public boolean processDonation() {
        if (paymentStrategy == null) {
            System.out.println("No payment strategy selected!");
            return false;
        }
        return paymentStrategy.ProcessPayment(this.amount);
    }
}