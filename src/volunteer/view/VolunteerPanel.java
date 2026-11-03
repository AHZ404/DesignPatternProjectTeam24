package volunteer.view;

import volunteer.controller.VolunteerController;
import volunteer.model.AvailabilitySlot;
import volunteer.model.CertificateType;
import volunteer.model.Skill;
import volunteer.model.SkillLevel;
import volunteer.model.TaskType;
import volunteer.model.Volunteer;
import volunteer.model.VolunteerAssignment;
import volunteer.model.VolunteerHourRecord;
import volunteer.model.VolunteerTask;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class VolunteerPanel extends JPanel {
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final VolunteerController controller;
    private boolean refreshing;
    private boolean dialogsEnabled = true;
    private final JLabel statusBar = new JLabel(" ");

    private final JTextField nameField = new JTextField(14);
    private final JTextField emailField = new JTextField(16);
    private final JTextField phoneField = new JTextField(11);
    private final JTextField prefsField = new JTextField(18);
    private final JTextField searchField = new JTextField(16);
    private final DefaultTableModel volunteerModel = table("ID", "Name", "Email", "Phone", "Status", "Skills", "Availability", "Approved hrs");
    private final JTable volunteerTable = new JTable(volunteerModel);
    private final JTextField skillNameField = new JTextField(12);
    private final JComboBox<SkillLevel> skillLevelBox = new JComboBox<>(SkillLevel.values());
    private final JCheckBox certifiedBox = new JCheckBox("Certified");
    private final JComboBox<Skill> skillBox = new JComboBox<>();
    private final JComboBox<DayOfWeek> dayBox = new JComboBox<>(DayOfWeek.values());
    private final JTextField slotStartField = new JTextField("09:00", 5);
    private final JTextField slotEndField = new JTextField("17:00", 5);
    private final JComboBox<AvailabilitySlot> slotBox = new JComboBox<>();

    private final JComboBox<TaskType> taskTypeBox = new JComboBox<>(TaskType.values());
    private final JTextField taskTitleField = new JTextField(16);
    private final JTextField taskDateField = new JTextField(LocalDate.now().plusDays(7).toString(), 9);
    private final JTextField taskStartField = new JTextField("10:00", 5);
    private final JTextField taskEndField = new JTextField("13:00", 5);
    private final JTextField taskLocationField = new JTextField(12);
    private final JTextField taskSkillsField = new JTextField(14);
    private final JSpinner taskMaxSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 500, 1));
    private final JTextField taskEventField = new JTextField(4);
    private final JLabel typeSpecificLabel = new JLabel();
    private final JTextField typeSpecificField = new JTextField(12);
    private final JTextField taskDescField = new JTextField(30);
    private final DefaultTableModel taskModel = table("ID", "Type", "Title", "Start", "End", "Location", "Required skills", "Places", "Status", "Details", "Event");
    private final JTable taskTable = new JTable(taskModel);

    private final JComboBox<Volunteer> assignVolunteerBox = new JComboBox<>();
    private final JComboBox<VolunteerTask> assignTaskBox = new JComboBox<>();
    private final JTextField roleField = new JTextField("Volunteer", 10);
    private final DefaultTableModel assignmentModel = table("ID", "Volunteer", "Task", "Task time", "Role", "Status", "Assigned at");
    private final JTable assignmentTable = new JTable(assignmentModel);
    private final JTextField hoursDateField = new JTextField(LocalDate.now().toString(), 9);
    private final JTextField hoursField = new JTextField("3", 4);
    private final DefaultTableModel hourModel = table("ID", "Assignment", "Volunteer", "Date", "Hours", "Approval", "Approved by");
    private final JTable hourTable = new JTable(hourModel);

    private final JComboBox<Volunteer> historyVolunteerBox = new JComboBox<>();
    private final DefaultTableModel historyModel = table("Assignment", "Task", "Type", "Start", "Role", "Status", "Hours logged");
    private final JTable historyTable = new JTable(historyModel);
    private final JLabel totalHoursLabel = new JLabel("Approved hours: 0");
    private final JComboBox<CertificateType> certTypeBox = new JComboBox<>(CertificateType.values());
    private final JTextArea certificateArea = new JTextArea(14, 54);

    public VolunteerPanel(VolunteerController controller) {
        this.controller = controller;
        controller.setView(this);
        setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Volunteers", buildVolunteersTab());
        tabs.addTab("Tasks", buildTasksTab());
        tabs.addTab("Assignments & Hours", buildAssignmentsTab());
        tabs.addTab("History & Certificates", buildHistoryTab());
        add(tabs, BorderLayout.CENTER);

        statusBar.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        add(statusBar, BorderLayout.SOUTH);
        refresh();
    }

    private JComponent buildVolunteersTab() {
        JPanel form = titled("Volunteer profile (VOL-01, VOL-02)", new GridLayout(0, 1));
        form.add(row(new JLabel("Name"), nameField, new JLabel("Email"), emailField, new JLabel("Phone"), phoneField,
                new JLabel("Preferences (comma separated)"), prefsField));
        form.add(row(
                button("Register", e -> controller.handleRegister(nameField.getText(), emailField.getText(), phoneField.getText(), prefsField.getText())),
                button("Update selected", e -> controller.handleUpdateProfile(selectedId(volunteerTable), nameField.getText(), emailField.getText(), phoneField.getText(), prefsField.getText())),
                button("Deactivate", e -> controller.handleDeactivate(selectedId(volunteerTable))),
                button("Activate", e -> controller.handleActivate(selectedId(volunteerTable))),
                button("Clear form", e -> clearVolunteerForm()),
                new JLabel("     Search"), searchField,
                button("Search", e -> refresh()),
                button("Show all", e -> { searchField.setText(""); refresh(); })));

        volunteerTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !refreshing) loadSelectedVolunteer();
        });

        JPanel skills = titled("Skills (VOL-03)", new FlowLayout(FlowLayout.LEFT));
        skills.add(new JLabel("Skill"));
        skills.add(skillNameField);
        skills.add(skillLevelBox);
        skills.add(certifiedBox);
        skills.add(button("Add skill", e -> controller.handleAddSkill(selectedId(volunteerTable), skillNameField.getText(),
                (SkillLevel) skillLevelBox.getSelectedItem(), certifiedBox.isSelected())));
        skillBox.setPreferredSize(new Dimension(230, skillBox.getPreferredSize().height));
        skills.add(skillBox);
        skills.add(button("Remove skill", e -> controller.handleRemoveSkill(selectedId(volunteerTable), (Skill) skillBox.getSelectedItem())));

        JPanel slots = titled("Weekly availability (VOL-04)", new FlowLayout(FlowLayout.LEFT));
        slots.add(dayBox);
        slots.add(new JLabel("from"));
        slots.add(slotStartField);
        slots.add(new JLabel("to"));
        slots.add(slotEndField);
        slots.add(button("Add slot", e -> controller.handleAddAvailability(selectedId(volunteerTable),
                (DayOfWeek) dayBox.getSelectedItem(), slotStartField.getText(), slotEndField.getText())));
        slotBox.setPreferredSize(new Dimension(190, slotBox.getPreferredSize().height));
        slots.add(slotBox);
        slots.add(button("Remove slot", e -> controller.handleRemoveAvailability(selectedId(volunteerTable), (AvailabilitySlot) slotBox.getSelectedItem())));

        JPanel bottom = new JPanel(new GridLayout(2, 1));
        bottom.add(skills);
        bottom.add(slots);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(form, BorderLayout.NORTH);
        panel.add(scroll(volunteerTable, "Volunteers — select a row to edit it or manage its skills and availability"), BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private JComponent buildTasksTab() {
        taskTypeBox.addActionListener(e -> updateTypeSpecificLabel());
        updateTypeSpecificLabel();

        JPanel form = titled("New task — created by VolunteerTaskFactory (Factory pattern)", new GridLayout(0, 1));
        form.add(row(new JLabel("Type"), taskTypeBox, new JLabel("Title"), taskTitleField,
                new JLabel("Date (yyyy-MM-dd)"), taskDateField, new JLabel("From"), taskStartField,
                new JLabel("To"), taskEndField, new JLabel("Location"), taskLocationField));
        form.add(row(new JLabel("Required skills (comma separated)"), taskSkillsField,
                new JLabel("Places"), taskMaxSpinner, new JLabel("Event ID (optional)"), taskEventField,
                typeSpecificLabel, typeSpecificField));
        form.add(row(new JLabel("Description"), taskDescField,
                button("Create task", e -> controller.handleCreateTask((TaskType) taskTypeBox.getSelectedItem(),
                        taskTitleField.getText(), taskDateField.getText(), taskStartField.getText(), taskEndField.getText(),
                        taskLocationField.getText(), taskSkillsField.getText(), (Integer) taskMaxSpinner.getValue(),
                        taskEventField.getText(), typeSpecificField.getText(), taskDescField.getText())),
                button("Cancel selected task", e -> controller.handleCancelTask(selectedId(taskTable))),
                button("Find suitable volunteers", e -> controller.handleShowSuitable(selectedId(taskTable)))));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(form, BorderLayout.NORTH);
        panel.add(scroll(taskTable, "Tasks"), BorderLayout.CENTER);
        return panel;
    }

    private JComponent buildAssignmentsTab() {
        JPanel assign = titled("Assign a volunteer (VOL-05, VOL-06) — eligibility and schedule conflicts are checked (VOL-10)",
                new FlowLayout(FlowLayout.LEFT));
        assign.add(new JLabel("Volunteer"));
        assign.add(assignVolunteerBox);
        assign.add(new JLabel("Task"));
        assign.add(assignTaskBox);
        assign.add(new JLabel("Role"));
        assign.add(roleField);
        assign.add(button("Assign", e -> controller.handleAssignTask((Volunteer) assignVolunteerBox.getSelectedItem(),
                (VolunteerTask) assignTaskBox.getSelectedItem(), roleField.getText())));
        assign.add(new JLabel("   Acting as: " + controller.getCurrentUser().getName()));

        JPanel assignmentButtons = row(
                button("Accept", e -> controller.handleRespondToAssignment(selectedId(assignmentTable), true)),
                button("Decline", e -> controller.handleRespondToAssignment(selectedId(assignmentTable), false)),
                button("Mark completed", e -> controller.handleCompleteAssignment(selectedId(assignmentTable))),
                button("Cancel", e -> controller.handleCancelAssignment(selectedId(assignmentTable))),
                new JLabel("      Log hours for selected assignment (VOL-07):  date"), hoursDateField,
                new JLabel("hours"), hoursField,
                button("Log hours", e -> controller.handleLogHours(selectedId(assignmentTable), hoursDateField.getText(), hoursField.getText())));

        JPanel top = new JPanel(new BorderLayout());
        top.add(scroll(assignmentTable, "Assignments — select one to accept, decline, complete, cancel or log hours"), BorderLayout.CENTER);
        top.add(assignmentButtons, BorderLayout.SOUTH);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(scroll(hourTable, "Hour records — a coordinator approves or rejects them"), BorderLayout.CENTER);
        bottom.add(row(button("Approve", e -> controller.handleApproveHours(selectedId(hourTable), true)),
                button("Reject", e -> controller.handleApproveHours(selectedId(hourTable), false))), BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, bottom);
        split.setResizeWeight(0.6);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(assign, BorderLayout.NORTH);
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private JComponent buildHistoryTab() {
        historyVolunteerBox.addActionListener(e -> { if (!refreshing) refreshHistory(); });

        JPanel top = titled("Volunteer history (VOL-08) and certificates (VOL-09)", new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Volunteer"));
        top.add(historyVolunteerBox);
        top.add(totalHoursLabel);
        top.add(new JLabel("    Certificate"));
        top.add(certTypeBox);
        top.add(button("Issue certificate", e -> controller.handleIssueCertificate(
                (Volunteer) historyVolunteerBox.getSelectedItem(), (CertificateType) certTypeBox.getSelectedItem())));

        certificateArea.setEditable(false);
        certificateArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane certScroll = new JScrollPane(certificateArea);
        certScroll.setBorder(BorderFactory.createTitledBorder("Certificate preview"));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scroll(historyTable, "Past and current assignments"), certScroll);
        split.setResizeWeight(0.55);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(top, BorderLayout.NORTH);
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    public void refresh() {
        refreshing = true;
        try {
            Integer selectedVolunteer = selectedId(volunteerTable);

            volunteerModel.setRowCount(0);
            for (Volunteer v : controller.handleSearch(searchField.getText())) {
                volunteerModel.addRow(new Object[]{v.getUserId(), v.getName(), v.getEmail(), v.getPhone(), v.getStatus(),
                        join(v.getSkills()), join(v.getAvailability()), v.getApprovedHours()});
            }
            reselect(volunteerTable, selectedVolunteer);

            taskModel.setRowCount(0);
            for (VolunteerTask t : controller.getTasks()) {
                taskModel.addRow(new Object[]{t.getTaskId(), t.getType(), t.getTitle(), DT.format(t.getStartTime()),
                        DT.format(t.getEndTime()), t.getLocation(), String.join(", ", t.getRequiredSkills()),
                        t.getActiveAssignments() + "/" + t.getMaxVolunteers(), t.getStatus(), t.getTypeDetails(),
                        t.getEventId() == null ? "" : t.getEventId()});
            }

            Integer selectedAssignment = selectedId(assignmentTable);
            assignmentModel.setRowCount(0);
            for (VolunteerAssignment a : controller.getAssignments()) {
                VolunteerTask t = controller.getTask(a.getTaskId());
                assignmentModel.addRow(new Object[]{a.getAssignmentId(), controller.getVolunteer(a.getVolunteerId()).getName(),
                        t.getTitle(), DT.format(t.getStartTime()), a.getRole(), a.getStatus(), DT.format(a.getAssignedAt())});
            }
            reselect(assignmentTable, selectedAssignment);

            hourModel.setRowCount(0);
            for (VolunteerHourRecord r : controller.getHourRecords()) {
                hourModel.addRow(new Object[]{r.getRecordId(), r.getAssignmentId(), controller.getVolunteer(r.getVolunteerId()).getName(),
                        r.getDate(), r.getHours(), r.getApprovalStatus(), r.getApprovedById() == null ? "" : "user #" + r.getApprovedById()});
            }

            List<Volunteer> all = controller.getVolunteers();
            reloadCombo(assignVolunteerBox, all.stream().filter(Volunteer::isActive).collect(Collectors.toList()));
            reloadCombo(historyVolunteerBox, all);
            reloadCombo(assignTaskBox, controller.getTasks().stream()
                    .filter(t -> t.getStatus() != volunteer.model.TaskStatus.CANCELLED
                            && t.getStatus() != volunteer.model.TaskStatus.COMPLETED)
                    .collect(Collectors.toList()));

            loadSelectedVolunteerLists();
            refreshHistory();
        } finally {
            refreshing = false;
        }
    }

    private void refreshHistory() {
        historyModel.setRowCount(0);
        Volunteer v = (Volunteer) historyVolunteerBox.getSelectedItem();
        if (v == null) {
            totalHoursLabel.setText("Approved hours: 0");
            return;
        }
        List<VolunteerHourRecord> records = controller.getHourRecords(v.getUserId());
        for (VolunteerAssignment a : controller.getHistory(v.getUserId())) {
            VolunteerTask t = controller.getTask(a.getTaskId());
            double logged = records.stream().filter(r -> r.getAssignmentId() == a.getAssignmentId())
                    .mapToDouble(VolunteerHourRecord::getHours).sum();
            historyModel.addRow(new Object[]{a.getAssignmentId(), t.getTitle(), t.getType(), DT.format(t.getStartTime()),
                    a.getRole(), a.getStatus(), logged});
        }
        totalHoursLabel.setText("Approved hours: " + controller.getVolunteer(v.getUserId()).getApprovedHours());
    }

    public void showMessage(String message) {
        statusBar.setForeground(new Color(0x1B5E20));
        statusBar.setText(message.replace('\n', ' '));
        if (dialogsEnabled) JOptionPane.showMessageDialog(this, message, "Volunteers", JOptionPane.INFORMATION_MESSAGE);
    }

    public void showError(String message) {
        statusBar.setForeground(new Color(0xB71C1C));
        statusBar.setText(message);
        if (dialogsEnabled) JOptionPane.showMessageDialog(this, message, "Cannot complete action", JOptionPane.WARNING_MESSAGE);
    }

    public void clearStatus() {
        statusBar.setText(" ");
    }

    public void showCertificate(String text) {
        certificateArea.setText(text);
        statusBar.setForeground(new Color(0x1B5E20));
        statusBar.setText("Certificate issued.");
    }

    public void setDialogsEnabled(boolean enabled) {
        this.dialogsEnabled = enabled;
    }

    private void loadSelectedVolunteer() {
        Integer id = selectedId(volunteerTable);
        if (id == null) return;
        Volunteer v = controller.getVolunteer(id);
        nameField.setText(v.getName());
        emailField.setText(v.getEmail());
        phoneField.setText(v.getPhone());
        prefsField.setText(String.join(", ", v.getPreferences()));
        refreshing = true;
        try {
            loadSelectedVolunteerLists();
        } finally {
            refreshing = false;
        }
    }

    private void loadSelectedVolunteerLists() {
        Integer id = selectedId(volunteerTable);
        if (id == null) {
            reloadCombo(skillBox, List.of());
            reloadCombo(slotBox, List.of());
            return;
        }
        Volunteer v = controller.getVolunteer(id);
        reloadCombo(skillBox, v.getSkills());
        reloadCombo(slotBox, v.getAvailability());
    }

    private void clearVolunteerForm() {
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        prefsField.setText("");
        volunteerTable.clearSelection();
    }

    private void updateTypeSpecificLabel() {
        TaskType t = (TaskType) taskTypeBox.getSelectedItem();
        if (t == null) return;
        switch (t) {
            case FUNDRAISER: typeSpecificLabel.setText("Target amount (EGP)"); break;
            case WORKSHOP:   typeSpecificLabel.setText("Workshop topic"); break;
            case OUTREACH:   typeSpecificLabel.setText("Target area"); break;
            default:         typeSpecificLabel.setText("Details");
        }
    }

    private static <T> void reloadCombo(JComboBox<T> box, List<? extends T> items) {
        Object previous = box.getSelectedItem();
        DefaultComboBoxModel<T> model = new DefaultComboBoxModel<>();
        for (T item : items) model.addElement(item);
        box.setModel(model);
        if (previous != null) {
            for (int i = 0; i < model.getSize(); i++) {
                if (model.getElementAt(i).toString().equals(previous.toString())) {
                    box.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private static Integer selectedId(JTable table) {
        int row = table.getSelectedRow();
        return row < 0 ? null : (Integer) table.getModel().getValueAt(table.convertRowIndexToModel(row), 0);
    }

    private static void reselect(JTable table, Integer id) {
        if (id == null) return;
        for (int i = 0; i < table.getRowCount(); i++) {
            if (id.equals(table.getValueAt(i, 0))) {
                table.setRowSelectionInterval(i, i);
                return;
            }
        }
    }

    private static String join(List<?> items) {
        return items.stream().map(Object::toString).collect(Collectors.joining(", "));
    }

    private static DefaultTableModel table(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private static JScrollPane scroll(JTable table, String title) {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(22);
        JScrollPane pane = new JScrollPane(table);
        pane.setBorder(BorderFactory.createTitledBorder(title));
        pane.setPreferredSize(new Dimension(900, 220));
        return pane;
    }

    private static JPanel titled(String title, java.awt.LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setBorder(BorderFactory.createTitledBorder(title));
        return p;
    }

    private static JPanel row(Component... components) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 3));
        for (Component c : components) p.add(c);
        return p;
    }

    private static JButton button(String text, java.awt.event.ActionListener action) {
        JButton b = new JButton(text);
        b.addActionListener(action);
        return b;
    }
}
