package donation.model;

/** Decorator pattern: wraps any payment strategy and passes calls through, so subclasses can add behaviour. */
public abstract class PaymentDecorator implements IPaymentStrategy {
    private static final long serialVersionUID = 1L;
    protected IPaymentStrategy wrapped;

    protected PaymentDecorator() { } // for Jackson

    protected PaymentDecorator(IPaymentStrategy wrapped) {
        this.wrapped = wrapped;
    }

    public IPaymentStrategy getWrapped() { return wrapped; }

    @Override
    public boolean ProcessPayment(double amount) { return wrapped.ProcessPayment(amount); }

    @Override
    public String getMethodName() { return wrapped.getMethodName(); }
}
