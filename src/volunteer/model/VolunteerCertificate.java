package volunteer.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.io.Serializable;

import java.time.LocalDate;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class VolunteerCertificate implements Serializable {
    private static final long serialVersionUID = 1L;

    private int certificateId;
    private int volunteerId;
    private String volunteerName;
    private CertificateType type;
    private LocalDate issueDate = LocalDate.now();
    private double totalHours;
    private int completedTasks;

    private VolunteerCertificate() {
    }

    public VolunteerCertificate(int certificateId, int volunteerId, String volunteerName,
                                CertificateType type, double totalHours, int completedTasks) {
        this.certificateId = certificateId;
        this.volunteerId = volunteerId;
        this.volunteerName = volunteerName;
        this.type = type;
        this.totalHours = totalHours;
        this.completedTasks = completedTasks;
    }

    public String generate() {
        String line = "=".repeat(52);
        String body = (type == CertificateType.HOURS_MILESTONE)
                ? "for completing " + formatHours(totalHours) + " approved volunteer hours"
                : "for participating in " + completedTasks + " volunteer task(s)";
        return line + "\n"
                + "            CERTIFICATE OF " + (type == CertificateType.HOURS_MILESTONE ? "SERVICE" : "PARTICIPATION") + "\n"
                + line + "\n\n"
                + "  This certifies that\n\n"
                + "      " + volunteerName + "\n\n"
                + "  is recognised " + body + ".\n\n"
                + "  Certificate No: VC-" + String.format("%05d", certificateId) + "\n"
                + "  Issue date:     " + issueDate + "\n"
                + "  Total hours:    " + formatHours(totalHours) + "\n\n"
                + line;
    }

    private static String formatHours(double h) {
        return (h == Math.floor(h)) ? String.valueOf((long) h) : String.valueOf(h);
    }

    public int getCertificateId() { return certificateId; }
    public int getVolunteerId() { return volunteerId; }
    public CertificateType getType() { return type; }
    public LocalDate getIssueDate() { return issueDate; }
    public double getTotalHours() { return totalHours; }
}
