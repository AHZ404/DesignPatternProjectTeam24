package donation.model;

public class InKindPayment implements IPaymentStrategy {
    private static final long serialVersionUID = 1L;
    private String itemName;
    private String itemCategory;
    private int unitQuantity;

    public InKindPayment(String itemName, String itemCategory, int unitQuantity) {
        this.itemName = itemName;
        this.itemCategory = itemCategory;
        this.unitQuantity = unitQuantity;
    }

    public String getItemName() { return itemName; }
    public String getItemCategory() { return itemCategory; }
    public int getUnitQuantity() { return unitQuantity; }

    @Override
    public boolean ProcessPayment(double estimatedValue) {
        System.out.println("Processing in-kind donation: " + unitQuantity + "x " + itemName + " (Value: $" + estimatedValue + ")");
        return true;
    }

    @Override
    public String getMethodName() { return "In-Kind"; }
}