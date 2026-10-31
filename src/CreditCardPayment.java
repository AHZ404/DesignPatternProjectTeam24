public class CreditCardPayment implements IPaymentStrategy {
    private String transactionId;

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
}