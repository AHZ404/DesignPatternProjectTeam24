package beneficiary.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public final class Beneficiary implements Serializable {
    private static final long serialVersionUID = 1L;
    public enum Status { PENDING, IN_PROGRESS, FULFILLED }

    private int id;
    private String name;
    private String contact;
    private String address;
    private Status status;

    // Required by Jackson for JSON deserialization
    public Beneficiary() { }

    public Beneficiary(
            @JsonProperty("id") int id,
            @JsonProperty("name") String name,
            @JsonProperty("contact") String contact,
            @JsonProperty("address") String address,
            @JsonProperty("status") Status status) {
        this.id = id;
        this.name = name;
        this.contact = contact;
        this.address = address;
        this.status = status;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getContact() { return contact; }
    public String getAddress() { return address; }
    public Status getStatus() { return status; }

    @Override
    public String toString() { return id + " - " + name; }
}