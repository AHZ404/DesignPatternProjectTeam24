package donation.model;

/** Concrete decorator (card payments only): the donor also pays a 3% processing fee, so the charity receives the full amount. */
public class FeeCoveredPayment extends PaymentDecorator {
    private static final long serialVersionUID = 1L;
    public static final double FEE_RATE = 0.03;

    private FeeCoveredPayment() { } // for Jackson

    public FeeCoveredPayment(IPaymentStrategy wrapped) {
        super(wrapped);
    }

    @Override
    public boolean ProcessPayment(double amount) {
        double fee = Math.round(amount * FEE_RATE * 100) / 100.0;
        System.out.println("Donor covers processing fee of $" + fee);
        return super.ProcessPayment(amount + fee); // charge amount + fee through the wrapped payment
    }

    @Override
    public String getMethodName() {
        return super.getMethodName() + " (+3% fee covered)";
    }
}
