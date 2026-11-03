package volunteer;

import user.model.EventCoordinator;
import user.model.User;
import volunteer.controller.VolunteerController;
import volunteer.model.CertificateType;
import volunteer.model.SkillLevel;
import volunteer.model.TaskType;
import volunteer.model.Volunteer;
import volunteer.model.VolunteerAssignment;
import volunteer.model.VolunteerHourRecord;
import volunteer.model.VolunteerTask;
import volunteer.model.WorkshopTask;
import volunteer.model.FundraiserTask;
import volunteer.model.OutreachTask;
import volunteer.service.ConsoleVolunteerNotifier;
import volunteer.service.InMemoryVolunteerRepository;
import volunteer.service.VolunteerService;
import volunteer.service.VolunteerTaskFactory;
import volunteer.view.VolunteerPanel;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

public class VolunteerDemo {
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--console")) {
            ConsoleChecks.run();
            return;
        }
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            JFrame frame = new JFrame("Non-Profit Management System — Volunteer Management");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(createPanel(true));
            frame.setSize(1400, 820);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    public static VolunteerPanel createPanel(boolean withSampleData) {
        User coordinator = new EventCoordinator(1000, "Sara Mostafa (Event Coordinator)", "sara@ngo.org", "01000000000");
        VolunteerService service = createService();
        if (withSampleData) loadSampleData(service, coordinator);
        VolunteerController controller = new VolunteerController(service, coordinator);
        return new VolunteerPanel(controller);
    }

    public static VolunteerService createService() {
        return new VolunteerService(new InMemoryVolunteerRepository(), new VolunteerTaskFactory(), new ConsoleVolunteerNotifier());
    }

    public static void loadSampleData(VolunteerService s, User coordinator) {
        Volunteer mariam = s.registerVolunteer("Mariam Hassan", "mariam@example.com", "01011112222", List.of("Weekends", "Health"));
        s.addSkill(mariam.getUserId(), "First Aid", SkillLevel.EXPERT, true);
        s.addSkill(mariam.getUserId(), "Public Speaking", SkillLevel.INTERMEDIATE, false);
        s.addAvailability(mariam.getUserId(), DayOfWeek.SATURDAY, LocalTime.of(9, 0), LocalTime.of(17, 0));
        s.addAvailability(mariam.getUserId(), DayOfWeek.FRIDAY, LocalTime.of(15, 0), LocalTime.of(21, 0));

        Volunteer omar = s.registerVolunteer("Omar Adel", "omar@example.com", "01033334444", List.of("Education"));
        s.addSkill(omar.getUserId(), "Teaching", SkillLevel.EXPERT, false);
        s.addAvailability(omar.getUserId(), DayOfWeek.SATURDAY, LocalTime.of(10, 0), LocalTime.of(14, 0));

        Volunteer nour = s.registerVolunteer("Nour Ali", "nour@example.com", "01055556666", List.of("Logistics"));
        s.addSkill(nour.getUserId(), "Driving", SkillLevel.INTERMEDIATE, true);
        s.addAvailability(nour.getUserId(), DayOfWeek.FRIDAY, LocalTime.of(16, 0), LocalTime.of(22, 0));

        LocalDate nextSat = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SATURDAY));
        LocalDate nextFri = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.FRIDAY));
        LocalDate lastSat = LocalDate.now().with(TemporalAdjusters.previous(DayOfWeek.SATURDAY));

        VolunteerTask past = s.createTask(TaskType.OUTREACH, "Food Basket Distribution",
                lastSat.atTime(10, 0), lastSat.atTime(14, 0));
        past.setLocation("Ain Shams community centre");
        past.setRequiredSkills(List.of("First Aid"));
        ((OutreachTask) past).setTargetArea("Ain Shams");
        ((OutreachTask) past).setCommunityGroup("Families in need");
        s.saveTask(past);
        VolunteerAssignment done = s.assignTask(mariam.getUserId(), past.getTaskId(), "Team lead", coordinator);
        s.acceptAssignment(done.getAssignmentId());
        s.completeAssignment(done.getAssignmentId());
        VolunteerHourRecord hours = s.recordHours(done.getAssignmentId(), lastSat, 4);
        s.approveHours(hours.getRecordId(), coordinator);

        VolunteerTask workshop = s.createTask(TaskType.WORKSHOP, "Computer Basics for Kids",
                nextSat.atTime(10, 0), nextSat.atTime(13, 0));
        workshop.setLocation("Faculty of Engineering, Hall 3");
        workshop.setRequiredSkills(List.of("Teaching"));
        workshop.setMaxVolunteers(2);
        ((WorkshopTask) workshop).setTopic("Using a computer safely");
        s.saveTask(workshop);

        VolunteerTask fundraiser = s.createTask(TaskType.FUNDRAISER, "Winter Clothes Drive",
                nextFri.atTime(17, 0), nextFri.atTime(20, 0));
        fundraiser.setLocation("City Stars Mall entrance");
        fundraiser.setRequiredSkills(List.of("Public Speaking"));
        fundraiser.setMaxVolunteers(4);
        fundraiser.setEventId(1);
        ((FundraiserTask) fundraiser).setTargetAmount(20000);
        s.saveTask(fundraiser);

        VolunteerAssignment a = s.assignTask(omar.getUserId(), workshop.getTaskId(), "Instructor", coordinator);
        s.acceptAssignment(a.getAssignmentId());
        s.assignTask(mariam.getUserId(), fundraiser.getTaskId(), "Donation desk", coordinator);
    }

    static class ConsoleChecks {
        private static int passed, failed;

        static void run() {
            User coordinator = new EventCoordinator(1000, "Sara Mostafa", "sara@ngo.org", "010");
            VolunteerService s = createService();

            Volunteer ali = s.registerVolunteer("Ali Samir", "ali@example.com", "0100", List.of("Weekends"));
            Volunteer mona = s.registerVolunteer("Mona Fathy", "mona@example.com", "0101", List.of());
            check("VOL-01 register gives IDs 1 and 2", ali.getUserId() == 1 && mona.getUserId() == 2);
            expectError("VOL-01 duplicate email rejected", () -> s.registerVolunteer("X", "ALI@example.com", "1", List.of()));
            expectError("VOL-01 invalid email rejected", () -> s.registerVolunteer("X", "not-an-email", "1", List.of()));

            s.updateVolunteer(ali.getUserId(), "Ali Samir Hassan", "ali@example.com", "0109", List.of("Weekends"));
            check("VOL-02 update profile", s.getVolunteer(1).getName().equals("Ali Samir Hassan"));
            check("VOL-02 search by name", s.searchVolunteers("mona").size() == 1);

            s.addSkill(ali.getUserId(), "First Aid", SkillLevel.EXPERT, true);
            expectError("VOL-03 duplicate skill rejected", () -> s.addSkill(ali.getUserId(), "first aid", SkillLevel.BEGINNER, false));
            check("VOL-03 search by skill", s.searchVolunteers("first").size() == 1);
            s.addAvailability(ali.getUserId(), DayOfWeek.SATURDAY, LocalTime.of(9, 0), LocalTime.of(17, 0));
            s.addAvailability(mona.getUserId(), DayOfWeek.SATURDAY, LocalTime.of(9, 0), LocalTime.of(12, 0));
            expectError("VOL-04 end before start rejected",
                    () -> s.addAvailability(mona.getUserId(), DayOfWeek.MONDAY, LocalTime.of(12, 0), LocalTime.of(9, 0)));

            LocalDate sat = LocalDate.now().with(TemporalAdjusters.previous(DayOfWeek.SATURDAY));
            VolunteerTask t1 = s.createTask(TaskType.OUTREACH, "Clinic visit", sat.atTime(10, 0), sat.atTime(12, 0));
            VolunteerTask t2 = s.createTask(TaskType.WORKSHOP, "First aid class", sat.atTime(11, 0), sat.atTime(13, 0));
            VolunteerTask t3 = s.createTask(TaskType.FUNDRAISER, "Bake sale", sat.atTime(14, 0), sat.atTime(16, 0));
            check("Factory: OUTREACH -> OutreachTask", t1 instanceof OutreachTask);
            check("Factory: WORKSHOP -> WorkshopTask", t2 instanceof WorkshopTask);
            check("Factory: FUNDRAISER -> FundraiserTask", t3 instanceof FundraiserTask);
            t1.setRequiredSkills(List.of("First Aid"));
            t1.setMaxVolunteers(1);
            s.saveTask(t1);

            expectError("VOL-05 missing skill rejected", () -> s.assignTask(mona.getUserId(), t1.getTaskId(), "Helper", coordinator));
            expectError("VOL-05 not available rejected", () -> s.assignTask(mona.getUserId(), t3.getTaskId(), "Helper", coordinator));
            expectError("VOL-05 volunteer cannot assign self",
                    () -> s.assignTask(ali.getUserId(), t1.getTaskId(), "Helper", s.getVolunteer(ali.getUserId())));
            VolunteerAssignment a1 = s.assignTask(ali.getUserId(), t1.getTaskId(), "Medic", coordinator);
            check("VOL-05 eligible volunteer assigned (PENDING)", a1.getStatus().name().equals("PENDING"));
            check("VOL-05 task becomes FULL at max places", t1.isFull() && t1.getStatus().name().equals("FULL"));

            expectError("VOL-10 overlapping task rejected", () -> s.assignTask(ali.getUserId(), t2.getTaskId(), "Helper", coordinator));
            VolunteerAssignment a3 = s.assignTask(ali.getUserId(), t3.getTaskId(), "Seller", coordinator);
            check("VOL-10 non-overlapping task allowed", a3 != null);
            s.declineAssignment(a3.getAssignmentId());
            check("Decline frees the place", t3.getActiveAssignments() == 0);

            expectError("VOL-07 cannot log hours on PENDING", () -> s.recordHours(a1.getAssignmentId(), sat, 2));
            s.acceptAssignment(a1.getAssignmentId());
            expectError("VOL-07 more than 24 hours rejected", () -> s.recordHours(a1.getAssignmentId(), sat, 30));
            expectError("VOL-07 future date rejected", () -> s.recordHours(a1.getAssignmentId(), LocalDate.now().plusDays(3), 2));
            VolunteerHourRecord r1 = s.recordHours(a1.getAssignmentId(), sat, 2);
            expectError("VOL-07 volunteer cannot approve own hours", () -> s.approveHours(r1.getRecordId(), s.getVolunteer(ali.getUserId())));
            s.approveHours(r1.getRecordId(), coordinator);
            check("VOL-07 approved hours added", s.getVolunteer(ali.getUserId()).getApprovedHours() == 2);
            expectError("VOL-07 cannot approve twice", () -> s.approveHours(r1.getRecordId(), coordinator));

            expectError("VOL-09 participation needs a completed task", () -> s.issueCertificate(ali.getUserId(), CertificateType.PARTICIPATION));
            s.completeAssignment(a1.getAssignmentId());
            String cert = s.issueCertificate(ali.getUserId(), CertificateType.PARTICIPATION).generate();
            check("VOL-09 participation certificate issued", cert.contains("Ali Samir Hassan"));
            expectError("VOL-09 hours certificate needs 10 hours", () -> s.issueCertificate(ali.getUserId(), CertificateType.HOURS_MILESTONE));

            check("VOL-08 history lists both assignments", s.getHistory(ali.getUserId()).size() == 2);

            s.deactivateVolunteer(mona.getUserId());
            expectError("VOL-02 inactive volunteer cannot be assigned", () -> s.assignTask(mona.getUserId(), t2.getTaskId(), "x", coordinator));

            System.out.println();
            System.out.println(cert);
            System.out.println();
            System.out.println("RESULT: " + passed + " passed, " + failed + " failed");
            if (failed > 0) System.exit(1);
        }

        private static void check(String name, boolean ok) {
            if (ok) passed++; else failed++;
            System.out.println((ok ? "PASS  " : "FAIL  ") + name);
        }

        private static void expectError(String name, Runnable action) {
            try {
                action.run();
                check(name + "  (no error was thrown)", false);
            } catch (RuntimeException e) {
                check(name + "  -> \"" + e.getMessage() + "\"", true);
            }
        }
    }
}
