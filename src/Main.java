import beneficiary.controller.BeneficiaryController;
import beneficiary.model.Beneficiary;
import beneficiary.service.BeneficiaryService;
import beneficiary.view.BeneficiaryPanel;

import database.DatabaseObserver;
import database.JsonRepository;

import donation.controller.DonationController;
import donation.model.*;
import donation.service.DonationService;
import donation.view.DonationPanel;

import user.model.EventCoordinator;

import volunteer.controller.VolunteerController;
import volunteer.model.Volunteer;
import volunteer.service.ConsoleVolunteerNotifier;
import volunteer.service.JsonVolunteerRepository;
import volunteer.service.VolunteerService;
import volunteer.service.VolunteerTaskFactory;
import volunteer.view.VolunteerPanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Unified application entry point: a login screen, then one window per user type, all on one JSON Database. */
public class Main {
    private static final String ADMIN = "Admin", DONOR = "Donor", VOLUNTEER = "Volunteer", BENEFICIARY = "Beneficiary";
    private static final String ADMIN_PASSWORD = "admin";
    private static final String NEW_ACCOUNT = "+ Create new account";

    private static BeneficiaryService beneficiaryService;
    private static DonationService donationService;
    private static VolunteerService volunteerService;

    public static void main(String[] args) {
        if (args.length > 0 && "--donation-demo".equals(args[0])) {
            Donation donation = new Donation(1, 101, 150.00, new Date(), "COMPLETED", new CreditCardPayment("CC-99812"));
            donation.processDonation();
            donation.setPaymentStrategy(new CashPayment());
            donation.processDonation();
            return;
        }

        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("Graphical interface unavailable. Run with --donation-demo, or use a desktop Java installation.");
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) { }

            try {
                // Single shared JSON Database (Singleton) used by every module
                JsonRepository jsonDb = JsonRepository.getInstance();
                beneficiaryService = new BeneficiaryService(jsonDb);
                donationService = new DonationService(jsonDb);
                volunteerService = new VolunteerService(
                        new JsonVolunteerRepository(jsonDb), new VolunteerTaskFactory(), new ConsoleVolunteerNotifier());
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(null, "Cannot load JSON database: " + ex.getMessage(),
                        "Database Storage Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            showLogin();
        });
    }

    /** Login screen: pick a user type, then an account (or create one). Admin needs a password. */
    private static void showLogin() {
        while (true) {
            JComboBox<String> roleBox = new JComboBox<>(new String[]{ADMIN, DONOR, VOLUNTEER, BENEFICIARY});
            JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
            form.add(new JLabel("Log in as:"));
            form.add(roleBox);
            if (JOptionPane.showConfirmDialog(null, form, "Login", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
                System.exit(0);
            }
            String role = (String) roleBox.getSelectedItem();
            try {
                if (ADMIN.equals(role)) {
                    JPasswordField password = new JPasswordField(15);
                    if (JOptionPane.showConfirmDialog(null, password, "Admin password", JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) continue;
                    if (!ADMIN_PASSWORD.equals(new String(password.getPassword()))) {
                        JOptionPane.showMessageDialog(null, "Wrong password", "Login", JOptionPane.ERROR_MESSAGE);
                        continue;
                    }
                    openWindow(role, 0, "Administrator");
                    return;
                }
                Integer id = chooseAccount(role);
                if (id != null) {
                    openWindow(role, id, role + " #" + id);
                    return;
                }
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Login", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** Lists the existing accounts of the role; the user picks one or creates a new one. */
    private static Integer chooseAccount(String role) {
        List<Object> accounts = new ArrayList<>();
        accounts.add(NEW_ACCOUNT);
        if (DONOR.equals(role)) accounts.addAll(donationService.listDonors());
        if (VOLUNTEER.equals(role)) accounts.addAll(volunteerService.getAllVolunteers());
        if (BENEFICIARY.equals(role)) accounts.addAll(beneficiaryService.listBeneficiaries(""));

        Object picked = JOptionPane.showInputDialog(null, "Choose your account", role + " login",
                JOptionPane.QUESTION_MESSAGE, null, accounts.toArray(), accounts.get(accounts.size() > 1 ? 1 : 0));
        if (picked == null) return null;
        if (picked instanceof Donor) return ((Donor) picked).getDonorId();
        if (picked instanceof Volunteer) return ((Volunteer) picked).getUserId();
        if (picked instanceof Beneficiary) return ((Beneficiary) picked).getId();

        // Create a new account
        String third = DONOR.equals(role) ? "Tax ID" : VOLUNTEER.equals(role) ? "Phone" : "Address";
        String second = BENEFICIARY.equals(role) ? "Contact" : "Email";
        JTextField name = new JTextField(20), f2 = new JTextField(20), f3 = new JTextField(20);
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Name")); form.add(name);
        form.add(new JLabel(second)); form.add(f2);
        form.add(new JLabel(third)); form.add(f3);
        if (JOptionPane.showConfirmDialog(null, form, "New " + role, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return null;
        if (DONOR.equals(role)) return donationService.addDonor(name.getText(), f2.getText(), "", f3.getText()).getDonorId();
        if (VOLUNTEER.equals(role)) return volunteerService.registerVolunteer(name.getText(), f2.getText(), f3.getText(), List.of()).getUserId();
        return beneficiaryService.register(name.getText(), f2.getText(), f3.getText()).getId();
    }

    /** Builds the window for the given user type. Admin sees every module; other users see only their own data. */
    private static void openWindow(String role, int id, String who) {
        JFrame window = new JFrame("Non-Profit Organization Management System  |  " + who);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JComponent content;

        if (ADMIN.equals(role)) {
            EventCoordinator coordinator = new EventCoordinator(0, "Administrator", "admin@ngo.org", "");
            DonationPanel donationPanel = new DonationPanel(new DonationController(donationService));
            BeneficiaryPanel beneficiaryPanel = new BeneficiaryPanel(new BeneficiaryController(beneficiaryService));
            VolunteerPanel volunteerPanel = new VolunteerPanel(new VolunteerController(volunteerService, coordinator));
            JTabbedPane tabs = new JTabbedPane();
            tabs.addTab("Donation Management", donationPanel);
            tabs.addTab("Beneficiary Management", beneficiaryPanel);
            tabs.addTab("Volunteer Management", volunteerPanel);
            content = tabs;
            observe(window, donationPanel, beneficiaryPanel, volunteerPanel);
        } else if (DONOR.equals(role)) {
            DonationPanel panel = new DonationPanel(new DonationController(donationService), id);
            content = panel;
            observe(window, panel);
        } else if (VOLUNTEER.equals(role)) {
            Volunteer me = volunteerService.getVolunteer(id);
            VolunteerPanel panel = new VolunteerPanel(new VolunteerController(volunteerService, me), id);
            content = panel;
            observe(window, panel);
        } else {
            BeneficiaryPanel panel = new BeneficiaryPanel(new BeneficiaryController(beneficiaryService), id);
            content = panel;
            observe(window, panel);
        }

        JButton logout = new JButton("Log out");
        logout.addActionListener(e -> { window.dispose(); showLogin(); });
        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        top.add(new JLabel("Logged in as " + who + "   "));
        top.add(logout);

        window.add(top, BorderLayout.NORTH);
        window.add(content, BorderLayout.CENTER);
        window.setSize(1200, 780);
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }

    /** Observer pattern: the screens watch the database, and stop watching when their window closes. */
    private static void observe(JFrame window, DatabaseObserver... screens) {
        JsonRepository db = JsonRepository.getInstance();
        for (DatabaseObserver s : screens) db.registerObserver(s);
        window.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                for (DatabaseObserver s : screens) db.removeObserver(s);
            }
        });
    }
}
