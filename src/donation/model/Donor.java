package donation.model;

import java.io.Serializable;
import java.util.Objects;

public class Donor implements Serializable {
    private static final long serialVersionUID = 1L;

    private int donorId;
    private String name;
    private String email;
    private String phone;
    private String taxId;

    public Donor() { }

    public Donor(int donorId, String name, String email, String phone, String taxId) {
        this.donorId = donorId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.taxId = taxId;
    }

    public int getDonorId() {
        return donorId;
    }

    public void setDonorId(int donorId) {
        this.donorId = donorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    @Override
    public String toString() {
        return donorId + " - " + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Donor)) return false;
        Donor donor = (Donor) o;
        return donorId == donor.donorId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(donorId);
    }
}