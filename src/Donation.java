import java.util.Date;

public class Donation {
    private double amount;
    private Date donationDate;
    private IPaymentStrategy paymentStrategy;

    public Donation(double amount, Date donationDate, IPaymentStrategy paymentStrategy) {
        this.amount = amount;
        this.donationDate = donationDate;
        this.paymentStrategy = paymentStrategy;
    }

    public double getAmount() {
        return amount;
    }

    public Date getDonationDate() {
        return donationDate;
    }

    public IPaymentStrategy getPaymentStrategy() {
        return paymentStrategy;
    }

    public void setPaymentStrategy(IPaymentStrategy strategy) {
        this.paymentStrategy = strategy;
    }

    public boolean processDonation() {
        if (paymentStrategy == null) {
            System.out.println("No payment strategy selected!");
            return false;
        }
        return paymentStrategy.ProcessPayment(this.amount);
    }
}