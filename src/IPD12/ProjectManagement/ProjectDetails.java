/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package IPD12.ProjectManagement;

import java.awt.GraphicsConfiguration;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import jdk.nashorn.internal.objects.Global;

/**
 *
 * @author 1796111
 */
public class ProjectDetails extends javax.swing.JFrame {

    Database db;
    DefaultListModel<Team> modelResourceList = new DefaultListModel<>();
    DefaultListModel<Team> modelMemberList = new DefaultListModel<>();
    private final String PLEASE_CHOOSE = "Please choose ...";
    private final String PROJECT_EDITOR = "Project Editor:　";
    private final String CREATE_NEW_PROJECT = "Create New Project: ";
    private final String TASK_EDITOR = "Task Editor:　";
    private final String CREATE_NEW_TASK = "Create New Task: ";
    JDialog parentDlg = null;
    long currentProjectId;
    Project currentProject;
    long currentTaskId;

    /**
     * Creates new form ProjectList
     */
    public ProjectDetails(long projectId) {

        this.currentProjectId = projectId;

        try {
            // connect to db
            db = new Database();
        }
        catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error connecting to database: " + e.getMessage(),
                    "Database error",
                    JOptionPane.ERROR_MESSAGE);
            dispose(); // can't continue if database connection failed
        }

        if (currentProjectId != 0) {
            try {
                // get current project object
                this.currentProject = db.getProjectById(this.currentProjectId);

            }
            catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Error fetching project information from database: " + e.getMessage(),
                        "Database error",
                        JOptionPane.ERROR_MESSAGE);
                dispose(); // can't continue if database connection failed
            }
        }

        initComponents();
        loadProjectInfo();

    }

    public ProjectDetails(JDialog parentDlg, long projectId) {
        this(projectId);
        this.parentDlg = parentDlg;
    }

    public void loadProjectInfo() {
        // load project details
        loadProjectSummary();
        // load task list
        loadTaskList();
        // load team member
        loadTeamMember();
    }

    public void loadProjectSummary() {

        // load project information 
        if (currentProjectId != 0) {

            pjd_lblTitle.setText(PROJECT_EDITOR + currentProject.getName());
            pjd_lblProjectId.setText(currentProject.getId() + "");
            pjd_tfName.setText(currentProject.getName());
            pjd_taDescription.setText(currentProject.getDescription());
            
            //setDate();
            pjd_tfStartDatePlanned.setText(GlobalProcess.formatOutputDate(currentProject.getStartDatePlanned()));
            pjd_tfEndDatePlanned.setText(GlobalProcess.formatOutputDate(currentProject.getEndDatePlanned()));
            pjd_tfStartDateActual.setText(GlobalProcess.formatOutputDate(currentProject.getStartDateActual()));
            pjd_tfEndDateActual.setText(GlobalProcess.formatOutputDate(currentProject.getEndDateActual()));

            pjd_chkbIsCompleted.setSelected(currentProject.getIsCompleted());

            // initial value list for project manager combo box
            loadAllTeamMemberForComboBox();
        }
        // add new project
        else {
            pjd_lblTitle.setText(CREATE_NEW_PROJECT);
            pjd_lblProjectId.setText("");
            pjd_tfName.setText("");
            pjd_taDescription.setText("");
            pjd_tfStartDatePlanned.setText(GlobalProcess.DATE_PATTERN);
            pjd_tfEndDatePlanned.setText(GlobalProcess.DATE_PATTERN);
            pjd_tfStartDateActual.setText(GlobalProcess.DATE_PATTERN);
            pjd_tfEndDateActual.setText(GlobalProcess.DATE_PATTERN);
            pjd_chkbIsCompleted.setSelected(false);

            // initial value list for project manager combo box
            loadAvailabeResourceForComboBox();

        }
    }

    public void loadTaskList() {

        DefaultTableModel tbModel = (DefaultTableModel) pjd_tbTaskList.getModel();
        // FIXED: initialization
        tbModel.getDataVector().removeAllElements();

        if (currentProjectId != 0) {

            try {
                // get task list
                ArrayList<Task> taskList = db.getAllTasksByProjectId(currentProjectId);

                for (Task task : taskList) {

                    String id = task.getId() + "";
                    String name = task.getName();
                    String description = task.getDescription();
                    String startDatePlanned = GlobalProcess.formatOutputDate2(task.getStartDatePlanned());
                    String endDatePlanned = GlobalProcess.formatOutputDate2(task.getEndDatePlanned());
                    String startDateActual = GlobalProcess.formatOutputDate2(task.getStartDateActual());
                    String endDateActual = GlobalProcess.formatOutputDate2(task.getEndDateActual());
                    String isCompleted;
                    String inCharegePerson = "";

                    isCompleted = task.getIsCompleted() ? "YES" : "NO";

                    long inChargePersonId = task.getPersonInCharge();
                    if (inChargePersonId != 0) {
                        Team teamMember = db.getTeamMemberById(inChargePersonId);
                        if (teamMember != null) {
                            inCharegePerson = teamMember.getIdName();
                        }
                    }

                    tbModel.addRow(new Object[]{id, name, description, startDatePlanned, endDatePlanned,
                        startDateActual, endDateActual, inCharegePerson, isCompleted});
                }

            }
            catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Error fetching data: " + ex.getMessage(),
                        "Database error",
                        JOptionPane.ERROR_MESSAGE);
                this.dispose();
            }
        }

    }

    public void loadTeamMember() {
        // initialization
        modelResourceList.removeAllElements();
        modelMemberList.removeAllElements();
        try {
            ArrayList<Team> allAvailableResourceList = db.getAllTeamAvailabeResouces();
            for (Team resource : allAvailableResourceList) {
                modelResourceList.addElement(resource);
            }
            if (currentProjectId != 0) {
                ArrayList<Team> allTeamMemberList = db.getAllTeamMembers(currentProjectId);
                for (Team member : allTeamMemberList) {
                    modelMemberList.addElement(member);
                }
            }
        }
        catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error fetching data: " + ex.getMessage(),
                    "Database error",
                    JOptionPane.ERROR_MESSAGE);
            this.dispose();
        }

    }

    private void loadAvailabeResourceForComboBox() {
        // initialization
        DefaultComboBoxModel modelPM = (DefaultComboBoxModel) pjd_cbProjectManager.getModel();
        modelPM.removeAllElements();
        
        try {
            // get all availabe resourses
            ArrayList<Team> resourceList = db.getAllTeamAvailabeResouces();
            modelPM.removeAllElements();
            modelPM.addElement(PLEASE_CHOOSE);

            for (Team rsc : resourceList) {
                modelPM.addElement(rsc.getIdName());
            }

        }
        catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error fetching data: " + ex.getMessage(),
                    "Database error",
                    JOptionPane.ERROR_MESSAGE);
            this.dispose();
        }
    }

    private void loadAllTeamMemberForComboBox() {
        // initialization
        DefaultComboBoxModel modelPM = (DefaultComboBoxModel) pjd_cbProjectManager.getModel();
        modelPM.removeAllElements();

        try {
            // get all team members
            ArrayList<Team> teamList = db.getAllTeamMembers(currentProject.getId());
            modelPM.removeAllElements();
            modelPM.addElement(PLEASE_CHOOSE);

            for (Team tm : teamList) {
                modelPM.addElement(tm.getIdName());
                if (tm.getId() == currentProject.getProjectManager()) {
                    modelPM.setSelectedItem(tm.getIdName());
                }
            }

        }
        catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error fetching data: " + ex.getMessage(),
                    "Database error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        dlgTaskEditor = new javax.swing.JDialog();
        tsk_lblTitle = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel25 = new javax.swing.JLabel();
        tsk_cbInChargePerson = new javax.swing.JComboBox<>();
        tsk_tfStartDateActual = new javax.swing.JTextField();
        tsk_tfTaskName = new javax.swing.JTextField();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        tsk_tfEndDatePlanned = new javax.swing.JTextField();
        jLabel28 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        tsk_chkbIsCompleted = new javax.swing.JCheckBox();
        jLabel18 = new javax.swing.JLabel();
        tsk_tfStartDatePlanned = new javax.swing.JTextField();
        tsk_btSave = new javax.swing.JButton();
        jLabel30 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        tsk_tfEndDateActual = new javax.swing.JTextField();
        tsk_btCancel = new javax.swing.JButton();
        jLabel32 = new javax.swing.JLabel();
        tsk_lblTaskId = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        tsk_taTaskDescription = new javax.swing.JTextArea();
        jLabel33 = new javax.swing.JLabel();
        tsk_lblProjectId = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        tsk_lblProjectName = new javax.swing.JLabel();
        popMenuTaskEdit = new javax.swing.JPopupMenu();
        popMiEdit = new javax.swing.JMenuItem();
        popMiDelete = new javax.swing.JMenuItem();
        pjd_lblTitle = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        pjd_taDescription = new javax.swing.JTextArea();
        pjd_tfName = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        pjd_tfStartDateActual = new javax.swing.JTextField();
        pjd_tfEndDatePlanned = new javax.swing.JTextField();
        pjd_tfStartDatePlanned = new javax.swing.JTextField();
        pjd_tfEndDateActual = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        pjd_cbProjectManager = new javax.swing.JComboBox<>();
        jLabel14 = new javax.swing.JLabel();
        pjd_chkbIsCompleted = new javax.swing.JCheckBox();
        pjd_btDetailSave = new javax.swing.JButton();
        pjd_btDetailCancel = new javax.swing.JButton();
        pjd_lblProjectId = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        pjd_tbTaskList = new javax.swing.JTable();
        pjd_btDeleteTask = new javax.swing.JButton();
        pjd_btUpdateTask = new javax.swing.JButton();
        pjd_btAddTask = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        pjd_lstAllResourse = new javax.swing.JList<>();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        pjd_lstCurTeamMember = new javax.swing.JList<>();
        pjd_btMoveToTeam = new javax.swing.JButton();
        pjd_btMoveBackFromTeam = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        pjd_btTeamSave = new javax.swing.JButton();
        pjd_btTeamCancel = new javax.swing.JButton();
        pjd_btGoBackToPjList = new javax.swing.JButton();

        dlgTaskEditor.setTitle("Task Editor");
        dlgTaskEditor.setModal(true);

        tsk_lblTitle.setFont(new java.awt.Font("Dialog", 0, 24)); // NOI18N
        tsk_lblTitle.setText("Task Editor: Task 1 - Current Business Analysis");

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel25.setText("In Charge Person:");

        tsk_tfStartDateActual.setText("10-20-2019");
        tsk_tfStartDateActual.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tsk_tfStartDateActualFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                tsk_tfStartDateActualFocusLost(evt);
            }
        });

        tsk_tfTaskName.setText("Task 1 - Current Business Analysis");
        tsk_tfTaskName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tsk_tfTaskNameActionPerformed(evt);
            }
        });

        jLabel26.setText("Task ID:");

        jLabel27.setText("Is Completed:");

        tsk_tfEndDatePlanned.setText("10-20-2019");
        tsk_tfEndDatePlanned.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tsk_tfEndDatePlannedFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                tsk_tfEndDatePlannedFocusLost(evt);
            }
        });

        jLabel28.setText("Planned Start Date:");

        jLabel29.setText("Task Name:");

        tsk_chkbIsCompleted.setSelected(true);

        jLabel18.setText("Planned End Date:");

        tsk_tfStartDatePlanned.setText("10-20-2019");
        tsk_tfStartDatePlanned.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tsk_tfStartDatePlannedFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                tsk_tfStartDatePlannedFocusLost(evt);
            }
        });

        tsk_btSave.setText("Save");
        tsk_btSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tsk_btSaveActionPerformed(evt);
            }
        });

        jLabel30.setText("Description:");

        jLabel31.setText("Actual Start Date:");

        tsk_tfEndDateActual.setText("10-20-2019");
        tsk_tfEndDateActual.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tsk_tfEndDateActualFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                tsk_tfEndDateActualFocusLost(evt);
            }
        });

        tsk_btCancel.setText("Cancel");
        tsk_btCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tsk_btCancelActionPerformed(evt);
            }
        });

        jLabel32.setText("Actual End Date:");

        tsk_lblTaskId.setText(":::");

        tsk_taTaskDescription.setColumns(20);
        tsk_taTaskDescription.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        tsk_taTaskDescription.setRows(5);
        tsk_taTaskDescription.setText("task description");
        jScrollPane5.setViewportView(tsk_taTaskDescription);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel25)
                        .addGap(18, 18, 18)
                        .addComponent(tsk_cbInChargePerson, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel31)
                            .addComponent(jLabel28)
                            .addComponent(jLabel26))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(tsk_tfStartDatePlanned)
                            .addComponent(tsk_tfStartDateActual)
                            .addComponent(tsk_lblTaskId, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(72, 72, 72)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel32)
                            .addComponent(jLabel27)
                            .addComponent(jLabel18)
                            .addComponent(jLabel29))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tsk_chkbIsCompleted)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(tsk_tfTaskName, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(27, 27, 27)
                                .addComponent(jLabel30))
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(tsk_tfEndDateActual, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(tsk_tfEndDatePlanned, javax.swing.GroupLayout.Alignment.LEADING)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tsk_btCancel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tsk_btSave, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel26)
                                    .addComponent(jLabel29)
                                    .addComponent(tsk_tfTaskName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel30)
                                    .addComponent(tsk_lblTaskId))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel28)
                                    .addComponent(tsk_tfStartDatePlanned, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel18)
                                    .addComponent(tsk_tfEndDatePlanned, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(tsk_btCancel, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGap(7, 7, 7)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel31)
                                    .addComponent(tsk_tfStartDateActual, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel32)
                                    .addComponent(tsk_tfEndDateActual, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel25)
                                        .addComponent(tsk_cbInChargePerson, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel27))
                                    .addComponent(tsk_chkbIsCompleted)))
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(tsk_btSave)))))
                .addGap(38, 38, 38))
        );

        jLabel33.setText("Project ID:");

        tsk_lblProjectId.setText(":::");

        jLabel34.setText("Project Name:");

        tsk_lblProjectName.setText("ABC Inc. Core-System Re-Build Project");

        javax.swing.GroupLayout dlgTaskEditorLayout = new javax.swing.GroupLayout(dlgTaskEditor.getContentPane());
        dlgTaskEditor.getContentPane().setLayout(dlgTaskEditorLayout);
        dlgTaskEditorLayout.setHorizontalGroup(
            dlgTaskEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dlgTaskEditorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dlgTaskEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(dlgTaskEditorLayout.createSequentialGroup()
                        .addGroup(dlgTaskEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tsk_lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 788, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(dlgTaskEditorLayout.createSequentialGroup()
                                .addGap(8, 8, 8)
                                .addComponent(jLabel33)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(tsk_lblProjectId, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(132, 132, 132)
                                .addComponent(jLabel34)
                                .addGap(26, 26, 26)
                                .addComponent(tsk_lblProjectName)))
                        .addGap(0, 544, Short.MAX_VALUE)))
                .addContainerGap())
        );
        dlgTaskEditorLayout.setVerticalGroup(
            dlgTaskEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dlgTaskEditorLayout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(tsk_lblTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
                .addGroup(dlgTaskEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel33)
                    .addComponent(jLabel34)
                    .addComponent(tsk_lblProjectId)
                    .addComponent(tsk_lblProjectName))
                .addGap(18, 18, 18)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        popMiEdit.setText("Edit");
        popMiEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popMiEditActionPerformed(evt);
            }
        });
        popMenuTaskEdit.add(popMiEdit);

        popMiDelete.setText("Delete");
        popMenuTaskEdit.add(popMiDelete);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Project Information Maintenance");
        setName("frmProjectDetails"); // NOI18N
        setResizable(false);

        pjd_lblTitle.setFont(new java.awt.Font("Dialog", 0, 24)); // NOI18N
        pjd_lblTitle.setText("Project Editor");

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel6.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel6.setText("Project Summary:");

        jLabel7.setText("Project Id:");

        jLabel8.setText("Name:");

        jLabel9.setText("Description:");

        pjd_taDescription.setColumns(20);
        pjd_taDescription.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        pjd_taDescription.setRows(5);
        pjd_taDescription.setText("Project description......");
        jScrollPane4.setViewportView(pjd_taDescription);

        pjd_tfName.setText("ABC Inc. Core-System Re-Build Project");
        pjd_tfName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pjd_tfNameActionPerformed(evt);
            }
        });

        jLabel4.setText("Planned Start Date:");

        jLabel10.setText("Planned End Date:");

        jLabel11.setText("Actual Start Date:");

        jLabel13.setText("Actual End Date:");

        pjd_tfStartDateActual.setText("10-20-2019");
        pjd_tfStartDateActual.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                pjd_tfStartDateActualFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                pjd_tfStartDateActualFocusLost(evt);
            }
        });

        pjd_tfEndDatePlanned.setText("10-20-2019");
        pjd_tfEndDatePlanned.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                pjd_tfEndDatePlannedFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                pjd_tfEndDatePlannedFocusLost(evt);
            }
        });

        pjd_tfStartDatePlanned.setText("10-20-2019");
        pjd_tfStartDatePlanned.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                pjd_tfStartDatePlannedFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                pjd_tfStartDatePlannedFocusLost(evt);
            }
        });

        pjd_tfEndDateActual.setText("10-20-2019");
        pjd_tfEndDateActual.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                pjd_tfEndDateActualFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                pjd_tfEndDateActualFocusLost(evt);
            }
        });

        jLabel12.setText("Project Manager:");

        jLabel14.setText("Is Completed:");

        pjd_chkbIsCompleted.setSelected(true);

        pjd_btDetailSave.setText("Save");
        pjd_btDetailSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pjd_btDetailSaveActionPerformed(evt);
            }
        });

        pjd_btDetailCancel.setText("Cancel");
        pjd_btDetailCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pjd_btDetailCancelActionPerformed(evt);
            }
        });

        pjd_lblProjectId.setText(":::");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(pjd_cbProjectManager, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel6)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel11)
                            .addComponent(jLabel4)
                            .addComponent(jLabel7))
                        .addGap(27, 27, 27)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(pjd_lblProjectId, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(pjd_tfStartDatePlanned)
                            .addComponent(pjd_tfStartDateActual))
                        .addGap(63, 63, 63)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel13)
                            .addComponent(jLabel14)
                            .addComponent(jLabel10)
                            .addComponent(jLabel8))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(pjd_tfName, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 63, Short.MAX_VALUE)
                                .addComponent(jLabel9))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(pjd_chkbIsCompleted)
                                    .addComponent(pjd_tfEndDatePlanned)
                                    .addComponent(pjd_tfEndDateActual))
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 27, Short.MAX_VALUE)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 287, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pjd_btDetailCancel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pjd_btDetailSave, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel6))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(62, 62, 62)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel7)
                                    .addComponent(jLabel8)
                                    .addComponent(pjd_tfName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel9)
                                    .addComponent(pjd_lblProjectId))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel4)
                                    .addComponent(pjd_tfStartDatePlanned, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel10)
                                    .addComponent(pjd_tfEndDatePlanned, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(pjd_btDetailCancel)))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(7, 7, 7)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel11)
                                    .addComponent(pjd_tfStartDateActual, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel13)
                                    .addComponent(pjd_tfEndDateActual, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel12)
                                        .addComponent(pjd_cbProjectManager, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel14))
                                    .addComponent(pjd_chkbIsCompleted)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(pjd_btDetailSave))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(62, 62, 62)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(21, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        pjd_tbTaskList.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Task ID", "Task Name", "Description", "Planned Start Date", "Planned End Date", "Actual Start Date", "Actual End Date", "Person in Charge", "Is Completed"
            }
        ));
        pjd_tbTaskList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        pjd_tbTaskList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                pjd_tbTaskListMouseReleased(evt);
            }
        });
        jScrollPane1.setViewportView(pjd_tbTaskList);

        pjd_btDeleteTask.setText("Delete Task");
        pjd_btDeleteTask.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pjd_btDeleteTaskActionPerformed(evt);
            }
        });

        pjd_btUpdateTask.setText("Update Task");
        pjd_btUpdateTask.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pjd_btUpdateTaskActionPerformed(evt);
            }
        });

        pjd_btAddTask.setText("Add Task");
        pjd_btAddTask.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pjd_btAddTaskActionPerformed(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel5.setText("Task List:");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jScrollPane1)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(pjd_btDeleteTask, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(pjd_btUpdateTask, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 112, Short.MAX_VALUE)
                            .addComponent(pjd_btAddTask, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );

        jPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {pjd_btDeleteTask, pjd_btUpdateTask});

        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(66, 66, 66)
                        .addComponent(pjd_btAddTask)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(pjd_btUpdateTask)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(pjd_btDeleteTask)
                        .addGap(0, 98, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        pjd_lstAllResourse.setModel(modelResourceList);
        jScrollPane2.setViewportView(pjd_lstAllResourse);

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel2.setText("All availabe resource:");

        pjd_lstCurTeamMember.setModel(modelMemberList);
        jScrollPane3.setViewportView(pjd_lstCurTeamMember);

        pjd_btMoveToTeam.setText(">>");
        pjd_btMoveToTeam.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pjd_btMoveToTeamActionPerformed(evt);
            }
        });

        pjd_btMoveBackFromTeam.setText("<<");
        pjd_btMoveBackFromTeam.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pjd_btMoveBackFromTeamActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel3.setText("Current team members:");

        pjd_btTeamSave.setText("Save");
        pjd_btTeamSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pjd_btTeamSaveActionPerformed(evt);
            }
        });

        pjd_btTeamCancel.setText("Cancel");
        pjd_btTeamCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pjd_btTeamCancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 481, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(pjd_btMoveBackFromTeam, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(pjd_btMoveToTeam, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jScrollPane3)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(pjd_btTeamCancel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(pjd_btTeamSave, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jPanel3Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {pjd_btMoveBackFromTeam, pjd_btMoveToTeam});

        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(109, 109, 109)
                        .addComponent(pjd_btMoveToTeam)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(pjd_btMoveBackFromTeam))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 235, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 235, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addGap(76, 76, 76)
                                        .addComponent(pjd_btTeamCancel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(pjd_btTeamSave)))))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pjd_btGoBackToPjList.setText("<< Back to Project List");
        pjd_btGoBackToPjList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pjd_btGoBackToPjListActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(pjd_lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 788, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(pjd_btGoBackToPjList, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(38, 38, 38))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pjd_lblTitle)
                    .addComponent(pjd_btGoBackToPjList))
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void pjd_tfNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pjd_tfNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_pjd_tfNameActionPerformed

    private void pjd_btDetailCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pjd_btDetailCancelActionPerformed
        // reload project summary
        loadProjectSummary();
    }//GEN-LAST:event_pjd_btDetailCancelActionPerformed

    private void pjd_btDetailSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pjd_btDetailSaveActionPerformed

        String name = pjd_tfName.getText();
        String description = pjd_taDescription.getText();
        Date startDatePlanned = null, endDatePlanned = null, startDateActual = null, endDateActual = null;
        boolean isCompleted = pjd_chkbIsCompleted.isSelected();
        String projectManagerStr = (String) pjd_cbProjectManager.getSelectedItem();
        long projectManager;
        String tempDate;

        if (name.trim().compareTo("") == 0) {
            // Show message box to the user
            JOptionPane.showMessageDialog(this, "Error: Please enter the project name.", "Input error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        tempDate = pjd_tfStartDatePlanned.getText();
        if (tempDate.trim().compareTo("") != 0 && tempDate.trim().compareToIgnoreCase(GlobalProcess.DATE_PATTERN) != 0) {
            startDatePlanned = GlobalProcess.checkDateFormat(tempDate);
            if (startDatePlanned == null) {
                // Show message box to the user
                JOptionPane.showMessageDialog(this, "Error: Planned Start Date format error (Format \"YYYY-MM-DD\").", "Input error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        
        tempDate = pjd_tfEndDatePlanned.getText();
        if (tempDate.trim().compareTo("") != 0 && tempDate.trim().compareToIgnoreCase(GlobalProcess.DATE_PATTERN) != 0) {
            endDatePlanned = GlobalProcess.checkDateFormat(tempDate);
            if (endDatePlanned == null) {
                // Show message box to the user
                JOptionPane.showMessageDialog(this, "Error: Planned end Date format error (Format \"YYYY-MM-DD\").", "Input error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        
        tempDate = pjd_tfStartDateActual.getText();
        if (tempDate.trim().compareTo("") != 0 && tempDate.trim().compareToIgnoreCase(GlobalProcess.DATE_PATTERN) != 0) {
            startDateActual = GlobalProcess.checkDateFormat(tempDate);
            if (startDateActual == null) {
                // Show message box to the user
                JOptionPane.showMessageDialog(this, "Error: Actual Start Date format error ((Format \"YYYY-MM-DD\")." , "Input error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        tempDate = pjd_tfEndDateActual.getText();
        if (tempDate.trim().compareTo("") != 0 && tempDate.trim().compareToIgnoreCase(GlobalProcess.DATE_PATTERN) != 0) {
            endDateActual = GlobalProcess.checkDateFormat(tempDate);
            if (endDateActual == null) {
                // Show message box to the user
                JOptionPane.showMessageDialog(this, "Error: Actual end Date format error (Format \"YYYY-MM-DD\")." , "Input error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        
        if (projectManagerStr.compareTo(PLEASE_CHOOSE) != 0) {
            projectManager = Long.parseLong(projectManagerStr.substring(0, projectManagerStr.indexOf(" ")));
        }
        else {
            projectManager = 0;
        }

        // add new project
        if (currentProjectId == 0) {
            try {
                Project project = new Project(0, name, description, startDatePlanned, endDatePlanned, startDateActual, endDateActual, projectManager, isCompleted);

                db.setAutoCommit(false);
                // insert to db and return new generated project id
                Long id = db.addProject(project);

                // update project to the team
                if (projectManager != 0) {
                    Team member = new Team(id, projectManager, false);
                    db.addTeamMember(member);
                    User user = new User(projectManager, false);
                    db.updateUserStatus(user);
                }
                db.commitUpdate();
                // when add successuflly, update current project id and project object
                currentProjectId = id;
                currentProject = project;
                currentProject.setId(currentProjectId);

                // reload project summary
                loadProjectSummary();
                // reload team list
                loadTeamMember();

            }
            catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: add project error !", "Database error", JOptionPane.ERROR_MESSAGE);
                try {
                    db.rollbackUpdate();
                }
                catch (SQLException ex1) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error: database rollback error !", "Database error", JOptionPane.ERROR_MESSAGE);
                }
            }
            finally {
                try {
                    db.setAutoCommit(true);
                }
                catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error: database setting error !", "Database error", JOptionPane.ERROR_MESSAGE);
                }
            }

        } // update project
        else {
            try {
                Project project = new Project(currentProjectId, name, description, startDatePlanned, endDatePlanned, startDateActual, endDateActual, projectManager, isCompleted);
                db.updateProject(project);

                // when update successfully
                currentProject = project;

                // reload project summary
                loadProjectSummary();
                // reload team list
                loadTeamMember();
            }
            catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: project update error !", "Database error", JOptionPane.ERROR_MESSAGE);
            }
        }


    }//GEN-LAST:event_pjd_btDetailSaveActionPerformed

    private void pjd_btMoveToTeamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pjd_btMoveToTeamActionPerformed

        moveItemBetween2Lists(pjd_lstAllResourse, modelResourceList, pjd_lstCurTeamMember, modelMemberList);

    }//GEN-LAST:event_pjd_btMoveToTeamActionPerformed

    private void pjd_btMoveBackFromTeamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pjd_btMoveBackFromTeamActionPerformed

        moveItemBetween2Lists(pjd_lstCurTeamMember, modelMemberList, pjd_lstAllResourse, modelResourceList);

    }//GEN-LAST:event_pjd_btMoveBackFromTeamActionPerformed

    private void pjd_btTeamCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pjd_btTeamCancelActionPerformed
        if (currentProjectId != 0) {
            loadTeamMember();
        }


    }//GEN-LAST:event_pjd_btTeamCancelActionPerformed

    private void pjd_btTeamSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pjd_btTeamSaveActionPerformed

        if (currentProjectId != 0) {

            try {
                db.setAutoCommit(false);

                // process Resource List
                int resourceSize = modelResourceList.getSize();
                for (int i = 0; i < resourceSize; i++) {
                    Team resource = modelResourceList.getElementAt(i);
                    resource.setProjectId(currentProjectId);

                    // check if left from a team 
                    Team memberWithStatus = db.checkIfMemberInTeam(resource);
                    if (memberWithStatus != null) {
                        resource.setIsLeft(true);
                        // update isLeft = true
                        db.updateTeamMemberStatus(resource);

                        User user = new User(resource.getId(), true);
                        // update resource to available status
                        db.updateUserStatus(user);
                    }
                }

                // process Team List
                int teamSize = modelMemberList.getSize();

                for (int i = 0; i < teamSize; i++) {
                    Team member = modelMemberList.getElementAt(i);
                    member.setProjectId(currentProjectId);

                    // check if new member who joins the team 
                    Team memberWithStatus = db.checkIfMemberInTeam(member);

                    // new member
                    if (memberWithStatus == null) {
                        member.setIsLeft(false);
                        User user = new User(member.getId(), false);
                        // add new member
                        db.addTeamMember(member);
                        // update resource to unavailable status
                        db.updateUserStatus(user);
                    } // member who left team and join team again
                    else if (memberWithStatus.getIsLeft()) {
                        member.setIsLeft(false);
                        User user = new User(member.getId(), false);
                        // update isLeft = true
                        db.updateTeamMemberStatus(member);
                        // update resource to unavailable status
                        db.updateUserStatus(user);
                    }

                }

                db.commitUpdate();
                // reload team member
                // Project project = new Project(projectId);
                loadTeamMember();

                // reload team member for pm or in-charge-person in project summary 
                loadAllTeamMemberForComboBox();
            }
            catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: team update error !", "Database error", JOptionPane.ERROR_MESSAGE);
                try {
                    db.rollbackUpdate();
                }
                catch (SQLException ex1) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error: database rollback error !", "Database error", JOptionPane.ERROR_MESSAGE);
                }
            }
            finally {
                try {
                    db.setAutoCommit(true);
                }
                catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error: database setting error !", "Database error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }


    }//GEN-LAST:event_pjd_btTeamSaveActionPerformed

    private void pjd_btGoBackToPjListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pjd_btGoBackToPjListActionPerformed
        parentDlg.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_pjd_btGoBackToPjListActionPerformed

    private void tsk_btCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tsk_btCancelActionPerformed
        dlgTaskEditor.setVisible(false);
    }//GEN-LAST:event_tsk_btCancelActionPerformed

    private void tsk_btSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tsk_btSaveActionPerformed
        String taskName = tsk_tfTaskName.getText();
        String description = tsk_taTaskDescription.getText();
        Date startDatePlanned = null, endDatePlanned = null, startDateActual = null, endDateActual = null;
        boolean isCompleted = tsk_chkbIsCompleted.isSelected();
        String inChargePersonStr = (String) tsk_cbInChargePerson.getSelectedItem();
        long inChargePerson;
        String tempDate;

        if (taskName.trim().compareTo("") == 0) {
            // Show message box to the user
            JOptionPane.showMessageDialog(this, "Error: Please enter the task name.", "Input error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        tempDate = tsk_tfStartDatePlanned.getText();
        if (tempDate.trim().compareTo("") != 0 && tempDate.trim().compareToIgnoreCase(GlobalProcess.DATE_PATTERN) != 0) {
            startDatePlanned = GlobalProcess.checkDateFormat(tempDate);
            if (startDatePlanned == null) {
                // Show message box to the user
                JOptionPane.showMessageDialog(this, "Error: Planned Start Date format error (Format \"YYYY-MM-DD\").", "Input error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        
        tempDate = tsk_tfEndDatePlanned.getText();
        if (tempDate.trim().compareTo("") != 0 && tempDate.trim().compareToIgnoreCase(GlobalProcess.DATE_PATTERN) != 0) {
            endDatePlanned = GlobalProcess.checkDateFormat(tempDate);
            if (endDatePlanned == null) {
                // Show message box to the user
                JOptionPane.showMessageDialog(this, "Error: Planned end Date format error (Format \"YYYY-MM-DD\").", "Input error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }


        tempDate = tsk_tfStartDateActual.getText();
        if (tempDate.trim().compareTo("") != 0 && tempDate.trim().compareToIgnoreCase(GlobalProcess.DATE_PATTERN) != 0) {
            startDateActual = GlobalProcess.checkDateFormat(tempDate);
            if (startDateActual == null) {
                // Show message box to the user
                JOptionPane.showMessageDialog(this, "Error: Actual start Date format error (Format \"YYYY-MM-DD\").", "Input error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        
        tempDate = tsk_tfEndDateActual.getText();
        if (tempDate.trim().compareTo("") != 0 && tempDate.trim().compareToIgnoreCase(GlobalProcess.DATE_PATTERN) != 0) {
            endDateActual = GlobalProcess.checkDateFormat(tempDate);
            if (endDateActual == null) {
                // Show message box to the user
                JOptionPane.showMessageDialog(this, "Error: Actual end Date format error (Format \"YYYY-MM-DD\").", "Input error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        if (inChargePersonStr.compareTo(PLEASE_CHOOSE) != 0) {
            inChargePerson = Long.parseLong(inChargePersonStr.substring(0, inChargePersonStr.indexOf(" ")));
        }
        else {
            inChargePerson = 0;
        }

        // create a new task
        if (currentTaskId == 0) {
            try {
                Task task = new Task(0, currentProjectId, taskName, description, startDatePlanned, endDatePlanned, startDateActual, endDateActual, inChargePerson, isCompleted);
                db.addTask(task);

                loadTaskList();
                dlgTaskEditor.setVisible(false);
            }
            catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: project update error !", "Database error", JOptionPane.ERROR_MESSAGE);
            }
        } // update old task
        else {
            try {
                Task task = new Task(currentTaskId, currentProjectId, taskName, description, startDatePlanned, endDatePlanned, startDateActual, endDateActual, inChargePerson, isCompleted);
                db.updateTask(task);

                loadTaskList();
                dlgTaskEditor.setVisible(false);
            }
            catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: project update error !", "Database error", JOptionPane.ERROR_MESSAGE);
            }

        }

    }//GEN-LAST:event_tsk_btSaveActionPerformed

    private void tsk_tfTaskNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tsk_tfTaskNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tsk_tfTaskNameActionPerformed

    private void pjd_btAddTaskActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pjd_btAddTaskActionPerformed

        if (currentProjectId == 0) {
            JOptionPane.showMessageDialog(this,
                    "Error: Please create a project before creating any task.",
                    "No Project",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        else {
            // initialize current task id
            currentTaskId = 0;
            
            tsk_lblTitle.setText(CREATE_NEW_TASK);
            tsk_lblProjectId.setText(currentProjectId + "");
            tsk_lblProjectName.setText(currentProject.getName());
            tsk_lblTaskId.setText("");
            tsk_tfTaskName.setText("");
            tsk_taTaskDescription.setText("");
            tsk_tfStartDatePlanned.setText(GlobalProcess.DATE_PATTERN);
            tsk_tfEndDatePlanned.setText(GlobalProcess.DATE_PATTERN);
            tsk_tfStartDateActual.setText(GlobalProcess.DATE_PATTERN);
            tsk_tfEndDateActual.setText(GlobalProcess.DATE_PATTERN);
            tsk_chkbIsCompleted.setSelected(false);

            try {
                // initial value list for in-charege-person combo box
                // get all team members
                ArrayList<Team> team = db.getAllTeamMembers(currentProjectId);

                DefaultComboBoxModel modelInCharePerson = (DefaultComboBoxModel) tsk_cbInChargePerson.getModel();
                modelInCharePerson.removeAllElements();
                modelInCharePerson.addElement(PLEASE_CHOOSE);

                for (Team member : team) {
                    modelInCharePerson.addElement(member.getIdName());
                }

            }
            catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Error fetching data: " + ex.getMessage(),
                        "Database error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            dlgTaskEditor.pack();
            dlgTaskEditor.setLocationRelativeTo(this);
            dlgTaskEditor.setVisible(true);
        }

    }//GEN-LAST:event_pjd_btAddTaskActionPerformed

    private void pjd_btUpdateTaskActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pjd_btUpdateTaskActionPerformed

        System.out.println("update " + pjd_tbTaskList.getSelectedRow());
        if (pjd_tbTaskList.getSelectedRow() == -1) {
            if (pjd_tbTaskList.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this,
                        "Error: There is no task in the list.",
                        "Task choose error",
                        JOptionPane.ERROR_MESSAGE);
            }
            else {
                JOptionPane.showMessageDialog(this,
                        "Error: Please choose one task for editing.",
                        "Task choose error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        else {

            tsk_lblProjectId.setText(currentProjectId + "");

            tsk_lblProjectName.setText(currentProject.getName());

            // initilize current task id
            currentTaskId = Long.parseLong((String) pjd_tbTaskList.getValueAt(pjd_tbTaskList.getSelectedRow(), 0));
            tsk_lblTaskId.setText(currentTaskId + "");

            try {
                // get task details from Database
                Task task = db.getTaskById(currentTaskId);
                
                tsk_lblTitle.setText(TASK_EDITOR + task.getName());
                tsk_tfTaskName.setText(task.getName());
                tsk_taTaskDescription.setText(task.getDescription());
                
                tsk_tfStartDatePlanned.setText(GlobalProcess.formatOutputDate(task.getStartDatePlanned()));
                tsk_tfEndDatePlanned.setText(GlobalProcess.formatOutputDate(task.getEndDatePlanned()));
                tsk_tfStartDateActual.setText(GlobalProcess.formatOutputDate(task.getStartDateActual()));
                tsk_tfEndDateActual.setText(GlobalProcess.formatOutputDate(task.getEndDateActual()));

                tsk_chkbIsCompleted.setSelected(task.getIsCompleted());

                // initial value list for in-charege-person combo box
                // get all team members
                ArrayList<Team> team = db.getAllTeamMembers(currentProjectId);

                DefaultComboBoxModel modelInCharePerson = (DefaultComboBoxModel) tsk_cbInChargePerson.getModel();
                modelInCharePerson.removeAllElements();
                modelInCharePerson.addElement(PLEASE_CHOOSE);

                for (Team member : team) {
                    modelInCharePerson.addElement(member.getIdName());
                    if (member.getId() == task.getPersonInCharge()) {
                        modelInCharePerson.setSelectedItem(member.getIdName());
                    }
                }

            }
            catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Error fetching data: " + ex.getMessage(),
                        "Database error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            dlgTaskEditor.pack();
            dlgTaskEditor.setLocationRelativeTo(this);
            dlgTaskEditor.setVisible(true);

        }


    }//GEN-LAST:event_pjd_btUpdateTaskActionPerformed

    private void pjd_btDeleteTaskActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pjd_btDeleteTaskActionPerformed

        System.out.println("delete " + pjd_tbTaskList.getSelectedRow());
        if (pjd_tbTaskList.getSelectedRow() == -1) {
            if (pjd_tbTaskList.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this,
                        "Error: There is no task in the list.",
                        "Task choose error",
                        JOptionPane.ERROR_MESSAGE);
            }
            else {
                JOptionPane.showMessageDialog(this,
                        "Error: Please choose one task for deleting.",
                        "Task choose error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        else {
            currentTaskId = Long.parseLong((String) pjd_tbTaskList.getValueAt(pjd_tbTaskList.getSelectedRow(), 0));

            Object[] options = {"Cancel", "Delete"};
            int decision = JOptionPane.showOptionDialog(this,
                    "Are you sure you want to delete the task: ID# " + currentTaskId,
                    "Confirm delete",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null, //do not use a custom Icon
                    options, //the titles of buttons
                    options[0]); //default button title

            if (decision == 1) {
                // change delete flag status to true
                try {
                    db.changeDeleteFlagStatus(currentTaskId, true);
                    loadTaskList();
                }
                catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error: task deletion error !", "Database error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }//GEN-LAST:event_pjd_btDeleteTaskActionPerformed

    private void popMiEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_popMiEditActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_popMiEditActionPerformed

    private void pjd_tbTaskListMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pjd_tbTaskListMouseReleased
        if (evt.isPopupTrigger()) {

            int row = pjd_tbTaskList.rowAtPoint(evt.getPoint());
            pjd_tbTaskList.setRowSelectionInterval(row, row);

            popMenuTaskEdit.show(evt.getComponent(), evt.getX(), evt.getY());

        }
    }//GEN-LAST:event_pjd_tbTaskListMouseReleased

    private void pjd_tfStartDatePlannedFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_pjd_tfStartDatePlannedFocusGained
        if (pjd_tfStartDatePlanned.getText().compareToIgnoreCase(GlobalProcess.DATE_PATTERN) == 0) {
            pjd_tfStartDatePlanned.setText("");
        }
    }//GEN-LAST:event_pjd_tfStartDatePlannedFocusGained

    private void pjd_tfEndDatePlannedFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_pjd_tfEndDatePlannedFocusGained
        if (pjd_tfEndDatePlanned.getText().compareToIgnoreCase(GlobalProcess.DATE_PATTERN) == 0) {
            pjd_tfEndDatePlanned.setText("");
        }
    }//GEN-LAST:event_pjd_tfEndDatePlannedFocusGained

    private void pjd_tfStartDateActualFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_pjd_tfStartDateActualFocusGained
        if (pjd_tfStartDateActual.getText().compareToIgnoreCase(GlobalProcess.DATE_PATTERN) == 0) {
            pjd_tfStartDateActual.setText("");
        }
    }//GEN-LAST:event_pjd_tfStartDateActualFocusGained

    private void pjd_tfEndDateActualFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_pjd_tfEndDateActualFocusGained
        if (pjd_tfEndDateActual.getText().compareToIgnoreCase(GlobalProcess.DATE_PATTERN) == 0) {
            pjd_tfEndDateActual.setText("");
        }
    }//GEN-LAST:event_pjd_tfEndDateActualFocusGained

    private void tsk_tfStartDatePlannedFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tsk_tfStartDatePlannedFocusGained
        if (tsk_tfStartDatePlanned.getText().compareToIgnoreCase(GlobalProcess.DATE_PATTERN) == 0) {
            tsk_tfStartDatePlanned.setText("");
        }
    }//GEN-LAST:event_tsk_tfStartDatePlannedFocusGained

    private void tsk_tfEndDatePlannedFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tsk_tfEndDatePlannedFocusGained
        if (tsk_tfEndDatePlanned.getText().compareToIgnoreCase(GlobalProcess.DATE_PATTERN) == 0) {
            tsk_tfEndDatePlanned.setText("");
        }
    }//GEN-LAST:event_tsk_tfEndDatePlannedFocusGained

    private void tsk_tfStartDateActualFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tsk_tfStartDateActualFocusGained
        if (tsk_tfStartDateActual.getText().compareToIgnoreCase(GlobalProcess.DATE_PATTERN) == 0) {
            tsk_tfStartDateActual.setText("");
        }
    }//GEN-LAST:event_tsk_tfStartDateActualFocusGained

    private void tsk_tfEndDateActualFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tsk_tfEndDateActualFocusGained
        if (tsk_tfEndDateActual.getText().compareToIgnoreCase(GlobalProcess.DATE_PATTERN) == 0) {
            tsk_tfEndDateActual.setText("");
        }
    }//GEN-LAST:event_tsk_tfEndDateActualFocusGained

    private void tsk_tfStartDatePlannedFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tsk_tfStartDatePlannedFocusLost
        if (tsk_tfStartDatePlanned.getText().trim().compareTo("") == 0) {
            tsk_tfStartDatePlanned.setText(GlobalProcess.DATE_PATTERN);
        }
    }//GEN-LAST:event_tsk_tfStartDatePlannedFocusLost

    private void tsk_tfEndDatePlannedFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tsk_tfEndDatePlannedFocusLost
        if (tsk_tfEndDatePlanned.getText().trim().compareTo("") == 0) {
            tsk_tfEndDatePlanned.setText(GlobalProcess.DATE_PATTERN);
        }
    }//GEN-LAST:event_tsk_tfEndDatePlannedFocusLost

    private void tsk_tfStartDateActualFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tsk_tfStartDateActualFocusLost
        if (tsk_tfStartDateActual.getText().trim().compareTo("") == 0) {
            tsk_tfStartDateActual.setText(GlobalProcess.DATE_PATTERN);
        }
    }//GEN-LAST:event_tsk_tfStartDateActualFocusLost

    private void tsk_tfEndDateActualFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tsk_tfEndDateActualFocusLost
        if (tsk_tfEndDateActual.getText().trim().compareTo("") == 0) {
            tsk_tfEndDateActual.setText(GlobalProcess.DATE_PATTERN);
        }
    }//GEN-LAST:event_tsk_tfEndDateActualFocusLost

    private void pjd_tfEndDatePlannedFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_pjd_tfEndDatePlannedFocusLost
        if (pjd_tfEndDatePlanned.getText().trim().compareTo("") == 0) {
            pjd_tfEndDatePlanned.setText(GlobalProcess.DATE_PATTERN);
        }
    }//GEN-LAST:event_pjd_tfEndDatePlannedFocusLost

    private void pjd_tfStartDateActualFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_pjd_tfStartDateActualFocusLost
        if (pjd_tfStartDateActual.getText().trim().compareTo("") == 0) {
            pjd_tfStartDateActual.setText(GlobalProcess.DATE_PATTERN);
        }
    }//GEN-LAST:event_pjd_tfStartDateActualFocusLost

    private void pjd_tfEndDateActualFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_pjd_tfEndDateActualFocusLost
        if (pjd_tfEndDateActual.getText().trim().compareTo("") == 0) {
            pjd_tfEndDateActual.setText(GlobalProcess.DATE_PATTERN);
        }
    }//GEN-LAST:event_pjd_tfEndDateActualFocusLost

    private void pjd_tfStartDatePlannedFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_pjd_tfStartDatePlannedFocusLost
        if (pjd_tfStartDatePlanned.getText().trim().compareTo("") == 0) {
            pjd_tfStartDatePlanned.setText(GlobalProcess.DATE_PATTERN);
        }
    }//GEN-LAST:event_pjd_tfStartDatePlannedFocusLost

    private void moveItemBetween2Lists(JList listFrom, DefaultListModel modelFrom, JList listTo, DefaultListModel modelTo) {
        // when use choose 1 or more rows
        if (!listFrom.isSelectionEmpty()) {
            ArrayList<Team> listSelected = (ArrayList<Team>) listFrom.getSelectedValuesList();
            int[] rscIdxList = listFrom.getSelectedIndices();

            int rowsForMoving = rscIdxList.length;
            // move out from resource
            for (int i = rowsForMoving - 1; i >= 0; i--) {
                modelFrom.removeElementAt(rscIdxList[i]);
            }

            // move into team
            int sizeBeforeMoving = modelTo.getSize();
            for (Team member : listSelected) {
                modelTo.addElement(member);
            }
            // set selected items for
            int[] idxSelected = new int[rowsForMoving];
            for (int i = 0; i < rowsForMoving; i++) {
                idxSelected[i] = sizeBeforeMoving + i;
            }
            listTo.setSelectedIndices(idxSelected);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JDialog dlgTaskEditor;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JButton pjd_btAddTask;
    private javax.swing.JButton pjd_btDeleteTask;
    private javax.swing.JButton pjd_btDetailCancel;
    private javax.swing.JButton pjd_btDetailSave;
    private javax.swing.JButton pjd_btGoBackToPjList;
    private javax.swing.JButton pjd_btMoveBackFromTeam;
    private javax.swing.JButton pjd_btMoveToTeam;
    private javax.swing.JButton pjd_btTeamCancel;
    private javax.swing.JButton pjd_btTeamSave;
    private javax.swing.JButton pjd_btUpdateTask;
    private javax.swing.JComboBox<String> pjd_cbProjectManager;
    private javax.swing.JCheckBox pjd_chkbIsCompleted;
    private javax.swing.JLabel pjd_lblProjectId;
    private javax.swing.JLabel pjd_lblTitle;
    private javax.swing.JList<Team> pjd_lstAllResourse;
    private javax.swing.JList<Team> pjd_lstCurTeamMember;
    private javax.swing.JTextArea pjd_taDescription;
    private javax.swing.JTable pjd_tbTaskList;
    private javax.swing.JTextField pjd_tfEndDateActual;
    private javax.swing.JTextField pjd_tfEndDatePlanned;
    private javax.swing.JTextField pjd_tfName;
    private javax.swing.JTextField pjd_tfStartDateActual;
    private javax.swing.JTextField pjd_tfStartDatePlanned;
    private javax.swing.JPopupMenu popMenuTaskEdit;
    private javax.swing.JMenuItem popMiDelete;
    private javax.swing.JMenuItem popMiEdit;
    private javax.swing.JButton tsk_btCancel;
    private javax.swing.JButton tsk_btSave;
    private javax.swing.JComboBox<String> tsk_cbInChargePerson;
    private javax.swing.JCheckBox tsk_chkbIsCompleted;
    private javax.swing.JLabel tsk_lblProjectId;
    private javax.swing.JLabel tsk_lblProjectName;
    private javax.swing.JLabel tsk_lblTaskId;
    private javax.swing.JLabel tsk_lblTitle;
    private javax.swing.JTextArea tsk_taTaskDescription;
    private javax.swing.JTextField tsk_tfEndDateActual;
    private javax.swing.JTextField tsk_tfEndDatePlanned;
    private javax.swing.JTextField tsk_tfStartDateActual;
    private javax.swing.JTextField tsk_tfStartDatePlanned;
    private javax.swing.JTextField tsk_tfTaskName;
    // End of variables declaration//GEN-END:variables

}
