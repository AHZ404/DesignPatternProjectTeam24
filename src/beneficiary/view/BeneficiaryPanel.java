package beneficiary.view;

import beneficiary.controller.BeneficiaryController;
import beneficiary.model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

/** MVC View. All actions call the controller, never the repository. */
public final class BeneficiaryPanel extends JPanel {
    private final BeneficiaryController controller;
    private final JTable beneficiaries=new JTable(), needs=new JTable(), requests=new JTable();
    private final JTable resources=new JTable(), distributions=new JTable();
    private final JTable services=new JTable(), impacts=new JTable();
    private final JTextField search=new JTextField(23);

    public BeneficiaryPanel(BeneficiaryController controller) {
        super(new BorderLayout(8,8));
        this.controller=Objects.requireNonNull(controller);
        setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        JLabel title=new JLabel("Beneficiary Management  |  Singleton Inventory Manager");
        title.setFont(title.getFont().deriveFont(Font.BOLD,18f));
        add(title,BorderLayout.NORTH);
        JTabbedPane tabs=new JTabbedPane();
        tabs.addTab("Beneficiaries",beneficiariesTab());
        tabs.addTab("Needs & Requests",needsTab());
        tabs.addTab("Inventory & Aid",inventoryTab());
        tabs.addTab("Services & Impact",servicesTab());
        tabs.addTab("CRUD Matrix",crudMatrixTab());
        add(tabs,BorderLayout.CENTER);
        refresh();
    }
    private static DefaultTableModel model(String... columns) {
        return new DefaultTableModel(columns,0) {
            @Override public boolean isCellEditable(int row,int column) { return false; }
        };
    }
    private static void setup(JTable table,DefaultTableModel model) {
        table.setModel(model); table.setRowHeight(25);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setReorderingAllowed(false);
    }
    private static JScrollPane pane(JTable table) { return new JScrollPane(table); }
    private static JPanel buttons(String label,JButton... buttons) {
        JPanel panel=new JPanel(new FlowLayout(FlowLayout.LEFT,7,7));
        if (label!=null && !label.isEmpty()) panel.add(new JLabel(label));
        for (JButton button:buttons) panel.add(button);
        return panel;
    }
    private static JButton button(String name,Runnable task) {
        JButton b=new JButton(name); b.addActionListener(e -> task.run()); return b;
    }
    private void action(Runnable runnable) {
        try {
            runnable.run(); refresh();
        } catch (IllegalArgumentException | IllegalStateException | ArithmeticException ex) {
            JOptionPane.showMessageDialog(this,ex.getMessage(),"Input error",JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this,ex.getMessage(),"Operation failed",JOptionPane.ERROR_MESSAGE);
        }
    }
    private int selectedId(JTable table) {
        int viewRow=table.getSelectedRow();
        if (viewRow<0) throw new IllegalArgumentException("Select a row first");
        return (Integer)table.getModel().getValueAt(table.convertRowIndexToModel(viewRow),0);
    }
    private int number(String value,String label) {
        try { return Integer.parseInt(value.trim()); }
        catch (NumberFormatException ex) { throw new IllegalArgumentException(label + " must be an integer"); }
    }
    private String[] prompt(String title,String[] labels,String... defaults) {
        JPanel form=new JPanel(new GridLayout(labels.length,2,12,12));
        JTextField[] fields=new JTextField[labels.length];
        for (int i=0;i<labels.length;i++) {
            form.add(new JLabel(labels[i]));
            fields[i]=new JTextField(i<defaults.length ? defaults[i] : "",24);
            form.add(fields[i]);
        }
        int answer=JOptionPane.showConfirmDialog(this,form,title,JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);
        if (answer!=JOptionPane.OK_OPTION) return null;
        String[] values=new String[fields.length];
        for (int i=0;i<fields.length;i++) values[i]=fields[i].getText().trim();
        return values;
    }
    private Integer chooseBeneficiary() {
        List<Beneficiary> items=controller.listBeneficiaries("");
        if (items.isEmpty()) throw new IllegalArgumentException("Register a beneficiary first");
        Object selected=JOptionPane.showInputDialog(this,"Choose beneficiary","Beneficiary",
                JOptionPane.QUESTION_MESSAGE,null,items.toArray(),items.get(0));
        return selected==null ? null : ((Beneficiary)selected).getId();
    }
    private Integer chooseResource() {
        List<Resource> items=controller.listResources();
        if (items.isEmpty()) throw new IllegalArgumentException("Add a resource first");
        Object selected=JOptionPane.showInputDialog(this,"Choose resource","Resource",
                JOptionPane.QUESTION_MESSAGE,null,items.toArray(),items.get(0));
        return selected==null ? null : ((Resource)selected).getId();
    }
    private static JPanel withToolbar(JComponent content,JPanel toolbar) {
        JPanel panel=new JPanel(new BorderLayout(0,5));
        panel.add(toolbar,BorderLayout.NORTH); panel.add(content,BorderLayout.CENTER); return panel;
    }
    private JPanel beneficiariesTab() {
        setup(beneficiaries,model("ID","Name","Contact","Address","Needs status"));
        JButton find=button("Search",()->action(this::refreshBeneficiaries));
        JButton clear=button("Clear",()->{search.setText("");refreshBeneficiaries();});
        JButton add=button("+ Register",()->action(()->{
            String[] x=prompt("Register Beneficiary",new String[]{"Name","Contact","Address (optional)"});
            if (x!=null) controller.register(x[0],x[1],x[2]);
        }));
        JButton edit=button("Edit Selected",()->action(()->{
            int id=selectedId(beneficiaries);
            Beneficiary existing=null;
            for (Beneficiary b:controller.listBeneficiaries("")) if (b.getId()==id) existing=b;
            if (existing==null) throw new IllegalArgumentException("Beneficiary not found");
            String[] x=prompt("Update Beneficiary",new String[]{"Name","Contact","Address"},
                    existing.getName(),existing.getContact(),existing.getAddress());
            if (x!=null) controller.update(id,x[0],x[1],x[2]);
        }));
        JButton status=button("Set Needs Status",()->action(()->{
            int id=selectedId(beneficiaries);
            Beneficiary.Status choice=(Beneficiary.Status)JOptionPane.showInputDialog(this,"New needs status",
                    "Change status",JOptionPane.QUESTION_MESSAGE,null,Beneficiary.Status.values(),Beneficiary.Status.PENDING);
            if (choice!=null) controller.setStatus(id,choice);
        }));
        JButton delete=button("Delete Selected",()->action(()->{
            int id=selectedId(beneficiaries);
            if (JOptionPane.showConfirmDialog(this,"Delete beneficiary #"+id+"?","Confirm delete",
                    JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE)==JOptionPane.YES_OPTION)
                controller.deleteBeneficiary(id);
        }));
        JButton history=button("View History",()->action(()->{
            int id=selectedId(beneficiaries);
            JTextArea text=new JTextArea(controller.history(id),22,70);
            text.setEditable(false);text.setFont(new Font(Font.MONOSPACED,Font.PLAIN,12));
            JOptionPane.showMessageDialog(this,new JScrollPane(text),"Assistance history #"+id,JOptionPane.INFORMATION_MESSAGE);
        }));
        JPanel top=new JPanel(new BorderLayout());
        JPanel query=new JPanel(new FlowLayout(FlowLayout.LEFT));
        query.add(new JLabel("Search by name, ID, or contact:"));
        query.add(search); query.add(find); query.add(clear);
        top.add(query,BorderLayout.NORTH);
        top.add(buttons("",add,edit,delete,status,history),BorderLayout.SOUTH);
        return withToolbar(pane(beneficiaries),top);
    }
    private JPanel needsTab() {
        setup(needs,model("ID","Beneficiary","Resource needs","Service needs","Notes","Date"));
        setup(requests,model("ID","Beneficiary","Type","Details","Resource ID","Requested","Fulfilled","Status"));
        JButton assess=button("+ Record Needs Assessment",()->action(()->{
            Integer id=chooseBeneficiary(); if (id==null) return;
            String[] x=prompt("Needs assessment for beneficiary #"+id,
                    new String[]{"Resource needs","Service needs","Notes"});
            if (x!=null) controller.addNeeds(id,x[0],x[1],x[2]);
        }));
        JButton resource=button("+ Resource Request",()->action(()->{
            Integer b=chooseBeneficiary();if(b==null)return;
            Integer r=chooseResource();if(r==null)return;
            String[] x=prompt("Request a resource",new String[]{"Quantity","Reason / description"},"1","");
            if(x!=null)controller.requestResource(b,r,number(x[0],"Quantity"),x[1]);
        }));
        JButton service=button("+ Service Request",()->action(()->{
            Integer id=chooseBeneficiary();if(id==null)return;
            String[] x=prompt("Request a service",new String[]{"Requested service and details"});
            if(x!=null)controller.requestService(id,x[0]);
        }));
        JButton cancel=button("Cancel Selected Request",()->action(()->{
            int requestId=selectedId(requests);
            if(JOptionPane.showConfirmDialog(this,"Cancel request #"+requestId+"?","Confirm",
                    JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)controller.cancelRequest(requestId);
        }));
        JSplitPane split=new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                withToolbar(pane(needs),buttons("Needs assessments:",assess)),
                withToolbar(pane(requests),buttons("Assistance requests:",resource,service,cancel)));
        split.setResizeWeight(0.43); return withToolbar(split,buttons("Tip: Select requests below to cancel them; IDs can be used for linked distributions."));
    }
    private JComponent inventoryTab() {
        setup(resources,model("ID","Resource","Category","Available stock"));
        setup(distributions,model("ID","Beneficiary","Resource","Quantity","Request ID","Date"));
        JButton add=button("+ Add Resource",()->action(()->{
            String[] x=prompt("Add resource",new String[]{"Name","Category","Initial stock"},"","","0");
            if (x!=null) controller.addResource(x[0],x[1],number(x[2],"Stock"));
        }));
        JButton restock=button("Restock Selected",()->action(()->{
            int resourceId=selectedId(resources);
            String[] x=prompt("Restock #"+resourceId,new String[]{"Quantity to add"},"1");
            if (x!=null) controller.restock(resourceId,number(x[0],"Quantity"));
        }));
        JButton removeResource=button("Delete Selected",()->action(()->{
            int resourceId=selectedId(resources);
            if (JOptionPane.showConfirmDialog(this,"Delete resource #"+resourceId+"?","Confirm delete",
                    JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE)==JOptionPane.YES_OPTION)
                controller.deleteResource(resourceId);
        }));
        JButton distribute=button("+ Distribute Aid",()->action(()->{
            Integer b=chooseBeneficiary();if(b==null)return;
            Integer r=chooseResource();if(r==null)return;
            String[] x=prompt("Record resource distribution",new String[]{"Quantity","Linked resource request ID (0 for none)","Notes"},"1","0","");
            if (x!=null) controller.distribute(b,r,number(x[0],"Quantity"),number(x[1],"Request ID"),x[2]);
        }));
        JSplitPane split=new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                withToolbar(pane(resources),buttons("Resource inventory:",add,restock,removeResource)),
                withToolbar(pane(distributions),buttons("Distribution history:",distribute)));
        split.setResizeWeight(.47);
        return split;
    }
    private JComponent servicesTab() {
        setup(services,model("ID","Beneficiary","Service","Request ID","Notes","Date"));
        setup(impacts,model("ID","Beneficiary","Outcome","Notes","Date"));
        JButton add=button("+ Provide Service",()->action(()->{
            Integer id=chooseBeneficiary();if(id==null)return;
            String[] x=prompt("Record service",new String[]{"Service name","Linked service request ID (0 for none)","Notes"},"","0","");
            if(x!=null)controller.recordService(id,x[0],number(x[1],"Request ID"),x[2]);
        }));
        JButton impact=button("+ Assess Impact",()->action(()->{
            Integer id=chooseBeneficiary();if(id==null)return;
            String[] x=prompt("Record program impact",new String[]{"Outcome / progress","Notes"});
            if(x!=null)controller.recordImpact(id,x[0],x[1]);
        }));
        JButton report=button("View Impact Report",()->action(()->{
            Integer id=chooseBeneficiary(); if(id==null)return;
            JTextArea text=new JTextArea(controller.impactSummary(id),15,60);
            text.setEditable(false); text.setFont(new Font(Font.MONOSPACED,Font.PLAIN,13));
            JOptionPane.showMessageDialog(this,new JScrollPane(text),"Program Impact Report",JOptionPane.INFORMATION_MESSAGE);
        }));
        JSplitPane split=new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                withToolbar(pane(services),buttons("Delivered services:",add)),
                withToolbar(pane(impacts),buttons("Program outcomes:",impact,report)));
        split.setResizeWeight(.5);return split;
    }
    /** Read-only CRUD matrix: requirements (rows) x entities (columns). */
    private JComponent crudMatrixTab() {
        String[] columns=new String[CrudMatrix.ENTITIES.length+2];
        columns[0]="ID"; columns[1]="Requirement";
        System.arraycopy(CrudMatrix.ENTITIES,0,columns,2,CrudMatrix.ENTITIES.length);
        DefaultTableModel m=model(columns);
        for (CrudMatrix.Row r:controller.crudMatrix()) {
            Object[] row=new Object[columns.length];
            row[0]=r.getId(); row[1]=r.getRequirement();
            for (int i=0;i<CrudMatrix.ENTITIES.length;i++) row[i+2]=r.get(CrudMatrix.ENTITIES[i]);
            m.addRow(row);
        }
        JTable table=new JTable(m);
        table.setRowHeight(26);
        table.getTableHeader().setReorderingAllowed(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        javax.swing.table.DefaultTableCellRenderer center=new javax.swing.table.DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(300);
        for (int i=2;i<columns.length;i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(center);
            // width follows the entity name so every header stays readable
            table.getColumnModel().getColumn(i).setPreferredWidth(Math.max(80,columns[i].length()*8+10));
        }
        JLabel title=new JLabel("CRUD Matrix — Beneficiary Management Module");
        title.setFont(title.getFont().deriveFont(Font.BOLD,15f));
        JTextArea notes=new JTextArea(CrudMatrix.NOTES,2,80);
        notes.setEditable(false); notes.setLineWrap(true); notes.setWrapStyleWord(true); notes.setOpaque(false);
        JPanel panel=new JPanel(new BorderLayout(0,8));
        panel.setBorder(BorderFactory.createEmptyBorder(8,4,4,4));
        panel.add(title,BorderLayout.NORTH);
        panel.add(new JScrollPane(table),BorderLayout.CENTER);
        panel.add(notes,BorderLayout.SOUTH);
        return panel;
    }
    public void refresh() {
        refreshBeneficiaries();
        DefaultTableModel n=(DefaultTableModel)needs.getModel();n.setRowCount(0);
        for (NeedAssessment a:controller.listNeeds())n.addRow(new Object[]{a.getId(),a.getBeneficiaryId(),a.getResourceNeeds(),a.getServiceNeeds(),a.getNotes(),a.getCreatedAt()});
        DefaultTableModel q=(DefaultTableModel)requests.getModel();q.setRowCount(0);
        for (AssistanceRequest r:controller.listRequests())q.addRow(new Object[]{r.getId(),r.getBeneficiaryId(),r.getType(),r.getDescription(),
                r.getResourceId()==0?"—":r.getResourceId(),r.getQuantity(),r.getFulfilledQuantity(),r.getStatus()});
        DefaultTableModel stock=(DefaultTableModel)resources.getModel();stock.setRowCount(0);
        for(Resource r:controller.listResources())stock.addRow(new Object[]{r.getId(),r.getName(),r.getCategory(),r.getStock()});
        DefaultTableModel aid=(DefaultTableModel)distributions.getModel();aid.setRowCount(0);
        for(ResourceDistribution d:controller.listDistributions())aid.addRow(new Object[]{d.getId(),d.getBeneficiaryId(),d.getResourceId(),d.getQuantity(),d.getRequestId()==0?"—":d.getRequestId(),d.getCreatedAt()});
        DefaultTableModel s=(DefaultTableModel)services.getModel();s.setRowCount(0);
        for(ServiceRecord r:controller.listServices())s.addRow(new Object[]{r.getId(),r.getBeneficiaryId(),r.getServiceName(),r.getRequestId()==0?"—":r.getRequestId(),r.getNotes(),r.getCreatedAt()});
        DefaultTableModel i=(DefaultTableModel)impacts.getModel();i.setRowCount(0);
        for(ImpactRecord r:controller.listImpacts())i.addRow(new Object[]{r.getId(),r.getBeneficiaryId(),r.getOutcome(),r.getNotes(),r.getCreatedAt()});
    }
    private void refreshBeneficiaries() {
        DefaultTableModel m=(DefaultTableModel)beneficiaries.getModel();m.setRowCount(0);
        for (Beneficiary b:controller.listBeneficiaries(search.getText()))
            m.addRow(new Object[]{b.getId(),b.getName(),b.getContact(),b.getAddress(),b.getStatus()});
    }
}