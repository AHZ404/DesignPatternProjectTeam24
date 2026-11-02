package donation.model;

public class CashPayment implements IPaymentStrategy {
    private static final long serialVersionUID = 1L;

    public CashPayment() {
    }

    @Override
    public boolean ProcessPayment(double amount) {
        System.out.println("Processing cash payment of $" + amount);
        return true;
    }

    @Override
    public String getMethodName() {
        return "Cash";
    }
}