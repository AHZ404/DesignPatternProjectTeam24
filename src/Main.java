import java.util.Date;

public class Main {
    public static void main(String[] args) {
        // Create a donation of $150.00
        Donation donation = new Donation(150.00, new Date(), new CreditCardPayment("CC-99812"));

        // Execute payment using initial Strategy (Credit Card)
        donation.processDonation();

        // Dynamically switch strategy at runtime to Cash
        donation.setPaymentStrategy(new CashPayment());
        donation.processDonation();
    }
}