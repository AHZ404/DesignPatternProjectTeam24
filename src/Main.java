import beneficiary.controller.BeneficiaryController;
import beneficiary.service.BeneficiaryService;
import beneficiary.view.BeneficiaryPanel;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Paths;
import java.util.Date;

/** Unified application entry point. Existing donation Strategy classes are unchanged. */
public class Main {
    public static void main(String[] args) {
        // Preserve the original console Strategy demonstration for teammates.
        if (args.length>0 && "--donation-demo".equals(args[0])) {
            Donation donation=new Donation(150.00,new Date(),new CreditCardPayment("CC-99812"));
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
            JFrame window=new JFrame("Non-Profit Organization Management System");
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            JTabbedPane tabs=new JTabbedPane();
            tabs.addTab("Donations (Strategy demo)",donationPanel());
            try {
                BeneficiaryService service=new BeneficiaryService(Paths.get("data","beneficiaries.dat"));
                tabs.addTab("Beneficiary Management",new BeneficiaryPanel(new BeneficiaryController(service)));
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(window,"Cannot load beneficiary records: "+ex.getMessage(),
                        "Beneficiary storage error",JOptionPane.ERROR_MESSAGE);
            }
            window.add(tabs);
            window.setSize(1150,740);
            window.setLocationRelativeTo(null);
            window.setVisible(true);
        });
    }
    private static JPanel donationPanel() {
        JPanel panel=new JPanel(new BorderLayout(12,12));
        panel.setBorder(BorderFactory.createEmptyBorder(22,22,22,22));
        JPanel inputs=new JPanel(new GridLayout(0,2,10,12));
        JTextField amount=new JTextField("150.00",18);
        JComboBox<String> method=new JComboBox<>(new String[]{"Cash","Credit Card","Debit Card"});
        JTextField transaction=new JTextField("TX-001",18);
        inputs.add(new JLabel("Donation amount:"));inputs.add(amount);
        inputs.add(new JLabel("Payment strategy:"));inputs.add(method);
        inputs.add(new JLabel("Demo transaction ID:"));inputs.add(transaction);
        JTextArea result=new JTextArea(11,56);result.setEditable(false);
        result.setFont(new Font(Font.MONOSPACED,Font.PLAIN,14));
        JButton process=new JButton("Run Existing Payment Strategy");
        process.addActionListener(e -> {
            try {
                double value=Double.parseDouble(amount.getText().trim());
                if (!Double.isFinite(value) || value<=0) throw new IllegalArgumentException("Enter a positive amount");
                String name=(String)method.getSelectedItem();
                IPaymentStrategy strategy;
                if ("Credit Card".equals(name)) strategy=new CreditCardPayment(transaction.getText().trim());
                else if ("Debit Card".equals(name)) strategy=new DebitCardPayment(transaction.getText().trim());
                else strategy=new CashPayment();
                Donation donation=new Donation(value,new Date(),strategy);
                boolean ok=donation.processDonation();
                result.append((ok?"SUCCESS (simulation)":"FAILED")+"  | "+name+" | "+value+" | "+new Date()+"\n");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel,"Amount must be numeric","Invalid input",JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(panel,ex.getMessage(),"Invalid input",JOptionPane.ERROR_MESSAGE);
            }
        });
        JPanel top=new JPanel(new BorderLayout(0,15));
        JLabel title=new JLabel("Donation Management — Existing Strategy Pattern Demo");
        title.setFont(title.getFont().deriveFont(Font.BOLD,18f));
        top.add(title,BorderLayout.NORTH);top.add(inputs,BorderLayout.CENTER);top.add(process,BorderLayout.SOUTH);
        panel.add(top,BorderLayout.NORTH);
        panel.add(new JScrollPane(result),BorderLayout.CENTER);
        JLabel note=new JLabel("Demo only: payment implementations print messages; no real payment processing or donation persistence.");
        panel.add(note,BorderLayout.SOUTH);
        return panel;
    }
}
