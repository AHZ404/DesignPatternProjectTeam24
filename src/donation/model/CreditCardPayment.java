package donation.model;

public class CreditCardPayment implements IPaymentStrategy {
    private static final long serialVersionUID = 1L;
    private String transactionId;

    private CreditCardPayment() { } // for Jackson

    public CreditCardPayment(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    @Override
    public boolean ProcessPayment(double amount) {
        System.out.println("Processing credit card payment of $" + amount + " [TxID: " + transactionId + "]");
        return true;
    }

    @Override
    public String getMethodName() {
        return "Credit Card";
    }
}