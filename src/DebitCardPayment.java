public class DebitCardPayment implements IPaymentStrategy {
    private String transactionId;

    public DebitCardPayment(String transactionId) {
        this.transactionId = transactionId;
    }

    // Getter function for private attribute
    public String getTransactionId() {
        return transactionId;
    }

    @Override
    public boolean ProcessPayment(double amount) {
        System.out.println("Processing debit card payment of $" + amount + " [TxID: " + transactionId + "]");
        return true;
    }
}