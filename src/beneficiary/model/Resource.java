package beneficiary.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public final class Resource implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id, stock;
    private String name, category;

    public Resource() { } // Jackson default constructor

    public Resource(
            @JsonProperty("id") int id,
            @JsonProperty("name") String name,
            @JsonProperty("category") String category,
            @JsonProperty("stock") int stock) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.stock = stock;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public int getStock() { return stock; }

    @Override
    public String toString() { return id + " - " + name + " (available: " + stock + ")"; }
}