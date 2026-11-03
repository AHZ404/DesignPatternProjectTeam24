package donation.model;

public class DebitCardPayment implements IPaymentStrategy {
    private static final long serialVersionUID = 1L;
    private String transactionId;

    private DebitCardPayment() { } // for Jackson

    public DebitCardPayment(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    @Override
    public boolean ProcessPayment(double amount) {
        System.out.println("Processing debit card payment of $" + amount + " [TxID: " + transactionId + "]");
        return true;
    }

    @Override
    public String getMethodName() {
        return "Debit Card";
    }
}