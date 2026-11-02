package donation.model;

public class CheckPayment implements IPaymentStrategy {
    private static final long serialVersionUID = 1L;
    private String checkNumber;
    private String bankName;

    public CheckPayment(String checkNumber, String bankName) {
        this.checkNumber = checkNumber;
        this.bankName = bankName;
    }

    public String getCheckNumber() { return checkNumber; }
    public String getBankName() { return bankName; }

    @Override
    public boolean ProcessPayment(double amount) {
        System.out.println("Processing check payment of $" + amount + " [Check #: " + checkNumber + ", Bank: " + bankName + "]");
        return true;
    }

    @Override
    public String getMethodName() { return "Check"; }
}