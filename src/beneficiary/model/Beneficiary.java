package beneficiary.model;

import java.io.Serializable;

public final class Beneficiary implements Serializable {
    private static final long serialVersionUID = 1L;
    public enum Status { PENDING, IN_PROGRESS, FULFILLED }
    private final int id;
    private final String name;
    private final String contact;
    private final String address;
    private final Status status;

    public Beneficiary(int id, String name, String contact, String address, Status status) {
        this.id = id; this.name = name; this.contact = contact;
        this.address = address; this.status = status;
    }
    public int getId() { return id; }
    public String getName() { return name; }
    public String getContact() { return contact; }
    public String getAddress() { return address; }
    public Status getStatus() { return status; }
    @Override public String toString() { return id + " - " + name; }
}
