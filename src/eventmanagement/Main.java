package eventmanagement;

public class Main {
    public static void main(String[] args) {
        Administrator admin = new Administrator(1, "Admin", "admin@ngo.org");
        Attendee sara = new Attendee(1, "Sara", "sara@mail.com");
        Attendee omar = new Attendee(2, "Omar", "omar@mail.com");
        EventController controller = new EventController();

        controller.createEvent();
        Event event = controller.getEvents().get(0);
        event.setTitle("Charity Gala");
        event.setLocation("Cairo");

        controller.registerAttendee(sara, event);
        controller.registerAttendee(omar, event);
        EventRegistration r1 = event.getRegistrations().get(0);
        System.out.println("Ticket: " + r1.getTicket().getTicketCode() + ", valid: " + r1.getTicket().validate());

        System.out.println("\n-- Admin updates the event --");
        event.setLocation("Giza");
        admin.updateEvent(event);
        controller.updateEvent(event);

        controller.checkIn(r1);
        System.out.println("\nSara attended: " + r1.getAttendanceRecord().isAttended());

        System.out.println("\n-- Admin cancels the event --");
        controller.cancelEvent(event);
    }
}
