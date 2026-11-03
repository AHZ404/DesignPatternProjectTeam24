package eventmanagement.model;

public class EventTicket {
    private final int ticketId;
    private final String ticketCode;
    private final String status;

    public EventTicket(int ticketId, String ticketCode) {
        this.ticketId = ticketId;
        this.ticketCode = ticketCode;
        this.status = "VALID";
    }

    public boolean validate() {
        return status.equals("VALID");
    }

    public int getTicketId() { return ticketId; }
    public String getTicketCode() { return ticketCode; }
    public String getStatus() { return status; }
}
