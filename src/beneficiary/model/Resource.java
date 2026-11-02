package beneficiary.model;

import java.io.Serializable;

public final class Resource implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int id, stock;
    private final String name, category;
    public Resource(int id, String name, String category, int stock) {
        this.id=id; this.name=name; this.category=category; this.stock=stock;
    }
    public int getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public int getStock() { return stock; }
    @Override public String toString() { return id + " - " + name + " (available: " + stock + ")"; }
}
