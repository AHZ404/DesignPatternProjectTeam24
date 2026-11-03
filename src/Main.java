import beneficiary.controller.BeneficiaryController;
import beneficiary.service.BeneficiaryService;
import beneficiary.view.BeneficiaryPanel;

import database.JsonRepository;

import donation.controller.DonationController;
import donation.model.*;
import donation.service.DonationService;
import donation.view.DonationPanel;

import user.model.EventCoordinator;

import volunteer.controller.VolunteerController;
import volunteer.service.ConsoleVolunteerNotifier;
import volunteer.service.JsonVolunteerRepository;
import volunteer.service.VolunteerService;
import volunteer.service.VolunteerTaskFactory;
import volunteer.view.VolunteerPanel;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Paths;
import java.util.Date;

/** Unified application entry point using a single central JSON Database. */
public class Main {
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

            JFrame window = new JFrame("Non-Profit Organization Management System");
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            JTabbedPane tabs = new JTabbedPane();

            try {
                // Initialize the single shared JSON Database repository
                JsonRepository jsonDb = new JsonRepository(Paths.get("data", "database.json"));

                // Connect Beneficiary module to JSON Database
                BeneficiaryService beneficiaryService = new BeneficiaryService(jsonDb);
                BeneficiaryController beneficiaryController = new BeneficiaryController(beneficiaryService);

                // Connect Donation module to JSON Database
                DonationService donationService = new DonationService(jsonDb);
                DonationController donationController = new DonationController(donationService);

                EventCoordinator currentCoordinator = new EventCoordinator(0, "Event Coordinator", "coordinator@ngo.org", "");
                VolunteerService volunteerService = new VolunteerService(
                        new JsonVolunteerRepository(jsonDb), new VolunteerTaskFactory(), new ConsoleVolunteerNotifier());
                VolunteerController volunteerController = new VolunteerController(volunteerService, currentCoordinator);

                DonationPanel donationPanel = new DonationPanel(donationController);
                BeneficiaryPanel beneficiaryPanel = new BeneficiaryPanel(beneficiaryController);
                VolunteerPanel volunteerPanel = new VolunteerPanel(volunteerController);

                // Re-sync UI state when tabs are toggled
                tabs.addChangeListener(e -> {
                    if (tabs.getSelectedComponent() == beneficiaryPanel) {
                        beneficiaryPanel.refresh();
                    } else if (tabs.getSelectedComponent() == donationPanel) {
                        donationPanel.refresh();
                    } else if (tabs.getSelectedComponent() == volunteerPanel) {
                        volunteerPanel.refresh();
                    }
                });

                tabs.addTab("Donation Management", donationPanel);
                tabs.addTab("Beneficiary Management", beneficiaryPanel);
                tabs.addTab("Volunteer Management", volunteerPanel);

            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(window, "Cannot load JSON database: " + ex.getMessage(),
                        "Database Storage Error", JOptionPane.ERROR_MESSAGE);
            }

            window.add(tabs);
            window.setSize(1200, 780);
            window.setLocationRelativeTo(null);
            window.setVisible(true);
        });
    }
}