package donation.view;

import database.DatabaseObserver;
import donation.controller.DonationController;
import donation.model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

/** MVC View for Donation Management matching BeneficiaryPanel design. */
public final class DonationPanel extends JPanel implements DatabaseObserver {
    private final DonationController controller;
    private final JTable donorsTable = new JTable();
    private final JTable donationsTable = new JTable();
    private final Integer donorId; // null = admin view (all donors), otherwise the logged-in donor

    public DonationPanel(DonationController controller) { this(controller, null); }

    public DonationPanel(DonationController controller, Integer donorId) {
        super(new BorderLayout(8, 8)); // Valid because DonationPanel extends JPanel
        this.controller = Objects.requireNonNull(controller);
        this.donorId = donorId;
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel(donorId == null ? "Donation Management  |  Flexible Strategy & Automated Inventory"
                : "My Donor Account  |  Profile, Donations, Receipts & Tax Statements");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        add(title, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Donors & Records", donorsAndDonationsTab());
        add(tabs, BorderLayout.CENTER);

        refresh();
    }

    private static DefaultTableModel model(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    private static void setup(JTable table, DefaultTableModel model) {
        table.setModel(model);
        table.setRowHeight(25);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setReorderingAllowed(false);
    }

    private static JScrollPane pane(JTable table) { return new JScrollPane(table); }

    private static JPanel buttons(String label, JButton... buttons) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 7));
        if (label != null && !label.isEmpty()) panel.add(new JLabel(label));
        for (JButton button : buttons) panel.add(button);
        return panel;
    }

    private static JButton button(String name, Runnable task) {
        JButton b = new JButton(name);
        b.addActionListener(e -> task.run());
        return b;
    }

    private static JPanel withToolbar(JComponent content, JPanel toolbar) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private void action(Runnable runnable) {
        try {
            runnable.run();
            refresh();
        } catch (IllegalArgumentException | IllegalStateException | ArithmeticException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Input error", JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Operation failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int selectedId(JTable table) {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) throw new IllegalArgumentException("Select a row first");
        return (Integer) table.getModel().getValueAt(table.convertRowIndexToModel(viewRow), 0);
    }

    private String[] prompt(String title, String[] labels, String... defaults) {
        JPanel form = new JPanel(new GridLayout(labels.length, 2, 12, 12));
        JTextField[] fields = new JTextField[labels.length];
        for (int i = 0; i < labels.length; i++) {
            form.add(new JLabel(labels[i]));
            fields[i] = new JTextField(i < defaults.length ? defaults[i] : "", 24);
            form.add(fields[i]);
        }
        int answer = JOptionPane.showConfirmDialog(this, form, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) return null;
        String[] values = new String[fields.length];
        for (int i = 0; i < fields.length; i++) values[i] = fields[i].getText().trim();
        return values;
    }

    private Integer chooseDonor() {
        if (donorId != null) return donorId;
        List<Donor> items = controller.listDonors();
        if (items.isEmpty()) throw new IllegalArgumentException("Register a donor first");
        Object selected = JOptionPane.showInputDialog(this, "Choose Donor", "Select Donor",
                JOptionPane.QUESTION_MESSAGE, null, items.toArray(), items.get(0));
        return selected == null ? null : ((Donor) selected).getDonorId();
    }

    private JSplitPane donorsAndDonationsTab() {
        setup(donorsTable, model("ID", "Name", "Email", "Phone", "Tax ID"));
        setup(donationsTable, model("ID", "Donor ID", "Amount / Value", "Payment Method", "Status", "Date"));

        JButton addDonor = button("+ Register Donor", () -> action(() -> {
            String[] x = prompt("Register Donor", new String[]{"Name", "Email", "Phone", "Tax ID / SSN"});
            if (x != null) controller.addDonor(x[0], x[1], x[2], x[3]);
        }));

        JButton editDonor = button("Edit Donor", () -> action(() -> {
            int id = (donorId != null ? donorId : selectedId(donorsTable));
            Donor existing = null;
            for (Donor d : controller.listDonors()) if (d.getDonorId() == id) existing = d;
            if (existing == null) throw new IllegalArgumentException("Donor not found");
            String[] x = prompt("Update Donor", new String[]{"Name", "Email", "Phone", "Tax ID"},
                    existing.getName(), existing.getEmail(), existing.getPhone(), existing.getTaxId());
            if (x != null) controller.updateDonor(id, x[0], x[1], x[2], x[3]);
        }));

        JButton deleteDonor = button("Delete Donor", () -> action(() -> {
            int id = (donorId != null ? donorId : selectedId(donorsTable));
            if (JOptionPane.showConfirmDialog(this, "Delete donor #" + id + "?", "Confirm delete",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                controller.deleteDonor(id);
                if (donorId != null) SwingUtilities.getWindowAncestor(this).dispose(); // own account deleted
            }
        }));

        JButton taxSummary = button("Tax Statement", () -> action(() -> {
            int taxDonorId = (donorId != null ? donorId : selectedId(donorsTable));
            String yearStr = JOptionPane.showInputDialog(this, "Enter Tax Year:", String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
            if (yearStr != null && !yearStr.isBlank()) {
                int year = Integer.parseInt(yearStr.trim());
                JTextArea text = new JTextArea(controller.generateTaxSummary(taxDonorId, year), 18, 65);
                text.setEditable(false);
                text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
                JOptionPane.showMessageDialog(this, new JScrollPane(text), "Tax Deduction Statement", JOptionPane.INFORMATION_MESSAGE);
            }
        }));

        JButton addDonation = button("+ Record Donation", () -> action(() -> {
            Integer donorId = chooseDonor();
            if (donorId == null) return;

            String[] options = {"Cash", "Credit Card", "Debit Card", "Check", "In-Kind Item"};
            String choice = (String) JOptionPane.showInputDialog(this, "Select Donation Method:", "Payment Strategy",
                    JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            if (choice == null) return;

            IPaymentStrategy strategy;
            double amount = 0.0;

            if ("Cash".equals(choice)) {
                String val = JOptionPane.showInputDialog(this, "Enter Cash Amount ($):");
                if (val == null) return;
                amount = Double.parseDouble(val.trim());
                strategy = new CashPayment();
            } else if ("Credit Card".equals(choice)) {
                String[] x = prompt("Credit Card Payment", new String[]{"Amount ($)", "Transaction ID"}, "100.00", "TXN-" + System.currentTimeMillis() % 10000);
                if (x == null) return;
                amount = Double.parseDouble(x[0]);
                strategy = new CreditCardPayment(x[1]);
            } else if ("Debit Card".equals(choice)) {
                String[] x = prompt("Debit Card Payment", new String[]{"Amount ($)", "Transaction ID"}, "100.00", "TXN-" + System.currentTimeMillis() % 10000);
                if (x == null) return;
                amount = Double.parseDouble(x[0]);
                strategy = new DebitCardPayment(x[1]);
            } else if ("Check".equals(choice)) {
                String[] x = prompt("Check Payment", new String[]{"Amount ($)", "Check Number", "Bank Name"});
                if (x == null) return;
                amount = Double.parseDouble(x[0]);
                strategy = new CheckPayment(x[1], x[2]);
            } else {
                String[] x = prompt("In-Kind Item Donation", new String[]{"Item Name", "Category", "Quantity", "Estimated Value ($)"});
                if (x == null) return;
                amount = Double.parseDouble(x[3]);
                strategy = new InKindPayment(x[0], x[1], Integer.parseInt(x[2]));
            }

            controller.recordDonation(donorId, amount, strategy);
        }));

        JButton receipt = button("Print Receipt", () -> action(() -> {
            int donationId = selectedId(donationsTable);
            JTextArea text = new JTextArea(controller.generateReceipt(donationId), 16, 55);
            text.setEditable(false);
            text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
            JOptionPane.showMessageDialog(this, new JScrollPane(text), "Donation Receipt", JOptionPane.INFORMATION_MESSAGE);
        }));

        JButton deleteDonation = button("Delete Record", () -> action(() -> {
            int id = selectedId(donationsTable);
            if (JOptionPane.showConfirmDialog(this, "Delete donation record #" + id + "?", "Confirm delete",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                controller.deleteDonation(id);
            }
        }));

        JPanel donorButtons = donorId == null
                ? buttons("Registered Donors:", addDonor, editDonor, deleteDonor, taxSummary)
                : buttons("My Profile:", editDonor, deleteDonor, taxSummary);
        if (donorId != null) { editDonor.setText("Edit My Profile"); deleteDonor.setText("Delete My Account"); }
        JPanel donationButtons = donorId == null
                ? buttons("Donation Transactions:", addDonation, receipt, deleteDonation)
                : buttons("My Donations:", addDonation, receipt);
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                withToolbar(pane(donorsTable), donorButtons),
                withToolbar(pane(donationsTable), donationButtons));
        split.setResizeWeight(0.45);
        return split;
    }

    @Override
    public void databaseChanged() { refresh(); }

    public void refresh() {
        DefaultTableModel donorModel = (DefaultTableModel) donorsTable.getModel();
        donorModel.setRowCount(0);
        for (Donor d : controller.listDonors()) {
            if (donorId != null && d.getDonorId() != donorId) continue;
            donorModel.addRow(new Object[]{d.getDonorId(), d.getName(), d.getEmail(), d.getPhone(), d.getTaxId()});
        }

        DefaultTableModel donationModel = (DefaultTableModel) donationsTable.getModel();
        donationModel.setRowCount(0);
        for (Donation d : controller.listDonations()) {
            if (donorId != null && d.getDonorId() != donorId) continue;
            donationModel.addRow(new Object[]{
                    d.getDonationId(),
                    d.getDonorId(),
                    String.format("$%.2f", d.getAmount()),
                    d.getPaymentStrategy().getMethodName(),
                    d.getStatus(),
                    d.getDonationDate()
            });
        }
    }
}