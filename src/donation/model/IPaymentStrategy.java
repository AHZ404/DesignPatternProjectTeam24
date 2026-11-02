package donation.model;

import java.io.Serializable;

public interface IPaymentStrategy extends Serializable {
    boolean ProcessPayment(double amount);
    String getMethodName();
}