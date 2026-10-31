public class CashPayment implements IPaymentStrategy {

    public CashPayment() {

    }

    @Override
    public boolean ProcessPayment(double amount) {
        System.out.println("Processing cash payment of $" + amount);
        return true;
    }
}