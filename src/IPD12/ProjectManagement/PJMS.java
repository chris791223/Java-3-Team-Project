/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package IPD12.ProjectManagement;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.util.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

public class PJMS extends javax.swing.JFrame {
   
    private final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$");
    private DefaultTableModel projectTableModel = null;
    private DefaultTableModel taskTableModel = null;
    private final int REMINDER_DAY = 2;
    private final int REMINDER_COLOR_RED = 1;    
    private final int REMINDER_COLOR_YELLOW = 3;
    private final int OPEN_REGISTER_DLG=1;
    private final int OPEN_SHOWUSER_DLG=2;
    private final int OPEN_USEREDIT_DLG=3;
    private int[] colorFlag;   
    public static Database db;
    public static User currentUser = null;
    private boolean editFlag = false;

    public void showMainDlg(){
        mainDlg.setVisible(true);
    }
    static int count = 0;
    class ReminderRenderer implements TableCellRenderer {

        public final DefaultTableCellRenderer DEFAULT_RENDERER = new DefaultTableCellRenderer();

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component renderer
                    = DEFAULT_RENDERER.getTableCellRendererComponent(table, value,
                            isSelected, hasFocus, row, column);
            Color background = null;
            Color foreground = null;
            if (column == 5) {
                if (colorFlag[row] == REMINDER_COLOR_RED) {
                    background = Color.RED;
                } else if (colorFlag[row] == REMINDER_COLOR_YELLOW) {
                    background = Color.YELLOW;
                }
            }
            foreground = Color.BLUE;
            renderer.setBackground(background);
            renderer.setForeground(foreground);
            //System.out.println(count++);
            return renderer;
        }
    }

    public static boolean isInteger(String str) {
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    private BigDecimal countTwoDate(Date d1, Date d2) {
        return new BigDecimal(Math.ceil((d1.getTime() - d2.getTime()) / (1000 * 3600 * 24.0)));
    }

    private void showTasksList(ArrayList<Task> pList,String pCompleted) {
        colorFlag = new int[pList.size()];
        int rowIndex = 0;
        String alertString = "";
        BigDecimal diff = null;
        if (pCompleted.compareToIgnoreCase("no") == 0) {
            for (Task l : pList) {
                boolean isCompleted = l.getIsCompleted();
                if (!isCompleted) {
                    Date now = new Date();
                    Date sdp = l.getStartDatePlanned();
                    Date sda = l.getStartDateActual();
                    Date edp = l.getEndDatePlanned();
                    Date eda = l.getEndDateActual();
                    if (sda == null && sdp != null) {
                        diff = countTwoDate(sdp, now);
                        if ((diff.compareTo(BigDecimal.ZERO) == 0 || diff.compareTo(BigDecimal.ZERO) == 1) && (diff.compareTo(new BigDecimal(REMINDER_DAY)) == -1 || diff.compareTo(new BigDecimal(REMINDER_DAY)) == 0)) {
                            alertString = diff.abs() + " Days due to start Date";
                            colorFlag[rowIndex] = REMINDER_COLOR_YELLOW;
                        } else if (diff.compareTo(BigDecimal.ZERO) == -1) {
                            alertString = diff.abs() + " Days over start Date ";
                            colorFlag[rowIndex] = REMINDER_COLOR_RED;
                        }
                    } else if (eda == null && edp != null) {
                        diff = countTwoDate(edp, now);
                        if ((diff.compareTo(BigDecimal.ZERO) == 0 || diff.compareTo(BigDecimal.ZERO) == 1) && (diff.compareTo(new BigDecimal(REMINDER_DAY)) == -1 || diff.compareTo(new BigDecimal(REMINDER_DAY)) == 0)) {
                            alertString = diff.abs() + " Days due to end Date";
                            colorFlag[rowIndex] = REMINDER_COLOR_YELLOW;
                        } else if (diff.compareTo(BigDecimal.ZERO) == -1) {
                            alertString = diff.abs() + " Days over end Date ";
                            colorFlag[rowIndex] = REMINDER_COLOR_RED;
                        }
                    }
                }
                rowIndex++;
                String completedStr = "";
                if (isCompleted) {
                    completedStr = "Yes";
                } else {
                    completedStr = "No";
                }                
                taskTableModel.addRow(new Object[]{l.getId(), l.getName(), l.getDescription(), l.getPersonInChargeName(), completedStr, alertString, String.valueOf(l.getStartDatePlanned()), String.valueOf(l.getEndDatePlanned()), String.valueOf(l.getStartDateActual()), String.valueOf(l.getEndDateActual())});
                alertString = "";
            }
        } else {
            for (Task l : pList) {
                boolean isCompleted = l.getIsCompleted();
                String completedStr = "";
                if (isCompleted) {
                    completedStr = "Yes";
                } else {
                    completedStr = "No";
                }
                taskTableModel.addRow(new Object[]{l.getId(), l.getName(), l.getDescription(), l.getPersonInChargeName(), completedStr, alertString, String.valueOf(l.getStartDatePlanned()), String.valueOf(l.getEndDatePlanned()), String.valueOf(l.getStartDateActual()), String.valueOf(l.getEndDateActual())});                
            }
        }
    }

    public void loadTasksById(long id,String isCompleted) {
        for (int i = taskTableModel.getRowCount() - 1; i >= 0; i--) {
            taskTableModel.removeRow(i);
        }
        try {
            ArrayList<Task> pList = db.getTasksById(id);
            showTasksList(pList,isCompleted);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Load projects information failure!\n" + ex.getMessage(),
                    "Database error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadAllTasks() {
        for (int i = taskTableModel.getRowCount() - 1; i >= 0; i--) {
            taskTableModel.removeRow(i);
        }
        try {
            ArrayList<Task> pList = db.getAllTasks();
            showTasksList(pList,"no");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Load tasks information failure!\n" + ex.getMessage(),
                    "Database error",
                    JOptionPane.ERROR_MESSAGE);
        }
        TableColumnModel TCModel = mainDlg_tbRemenderTask.getColumnModel();
        TCModel.getColumn(0).setPreferredWidth(23);
        TCModel.getColumn(1).setPreferredWidth(86);
        TCModel.getColumn(2).setPreferredWidth(131);
        TCModel.getColumn(3).setPreferredWidth(64);
        TCModel.getColumn(4).setPreferredWidth(77);
        TCModel.getColumn(5).setPreferredWidth(269);
        TCModel.getColumn(6).setPreferredWidth(102);
        TCModel.getColumn(7).setPreferredWidth(107);
        TCModel.getColumn(8).setPreferredWidth(88);
        TCModel.getColumn(9).setPreferredWidth(88);
        //mainDlg_tbRemenderTask.setDefaultRenderer(Object.class, new ReminderRenderer());
    }
    public void loadAllProjects(int controlCode) {
        for (int i = projectTableModel.getRowCount() - 1; i >= 0; i--) {
            projectTableModel.removeRow(i);
        }
        try {
            ArrayList<Project> pList = db.getAllProjects(controlCode);
            for (Project l : pList) {
                String completedStr = "";
                if (l.getIsCompleted()) {
                    completedStr = "Yes";
                } else {
                    completedStr = "No";
                }
                 projectTableModel.addRow(new Object[]{l.getId(), l.getName(), l.getDescription(), l.getProjectManager(), l.getPMName(), l.getTasknums(),completedStr,l.getStartDatePlanned(), l.getEndDatePlanned(), l.getStartDateActual(), l.getEndDateActual()});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Load projects information failure!\n" + ex.getMessage(),
                    "Database error",
                    JOptionPane.ERROR_MESSAGE);
        }
        //set every cell's font align center
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);
        int listSize = mainDlg_tbProjects.getColumnCount();
        TableColumnModel TCModel = mainDlg_tbProjects.getColumnModel();
        for (int i = 0; i < listSize; i++) {
            TCModel.getColumn(i).setCellRenderer(leftRenderer);
        }
        TCModel.getColumn(0).setPreferredWidth(23);
        TCModel.getColumn(1).setPreferredWidth(75);
        TCModel.getColumn(2).setPreferredWidth(231);
        TCModel.getColumn(3).setPreferredWidth(45);
        TCModel.getColumn(4).setPreferredWidth(66);
        TCModel.getColumn(5).setPreferredWidth(65);
        TCModel.getColumn(6).setPreferredWidth(62);
        TCModel.getColumn(7).setPreferredWidth(63);
        TCModel.getColumn(8).setPreferredWidth(58);
        TCModel.getColumn(9).setPreferredWidth(69);
        TCModel.getColumn(10).setPreferredWidth(65);
        //set every coloum's width
    }
    public void loadAllProjects() {
        for (int i = projectTableModel.getRowCount() - 1; i >= 0; i--) {
            projectTableModel.removeRow(i);
        }
        try {
            ArrayList<Project> pList = db.getAllProjects(Database.GETALLPROJECTS_ORDERBYID_ASC);
            for (Project l : pList) {
                String completedStr = "";
                if (l.getIsCompleted()) {
                    completedStr = "Yes";
                } else {
                    completedStr = "No";
                }
                projectTableModel.addRow(new Object[]{l.getId(), l.getName(), l.getDescription(), l.getProjectManager(), l.getPMName(), l.getTasknums(),completedStr,l.getStartDatePlanned(), l.getEndDatePlanned(), l.getStartDateActual(), l.getEndDateActual()});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Load projects information failure!\n" + ex.getMessage(),
                    "Database error",
                    JOptionPane.ERROR_MESSAGE);
        }
        //set every cell's font align center
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);
        int listSize = mainDlg_tbProjects.getColumnCount();
        TableColumnModel TCModel = mainDlg_tbProjects.getColumnModel();
        for (int i = 0; i < listSize; i++) {
            TCModel.getColumn(i).setCellRenderer(leftRenderer);
        }
        TCModel.getColumn(0).setPreferredWidth(23);
        TCModel.getColumn(1).setPreferredWidth(75);
        TCModel.getColumn(2).setPreferredWidth(231);
        TCModel.getColumn(3).setPreferredWidth(45);
        TCModel.getColumn(4).setPreferredWidth(66);
        TCModel.getColumn(5).setPreferredWidth(65);
        TCModel.getColumn(6).setPreferredWidth(62);
        TCModel.getColumn(7).setPreferredWidth(63);
        TCModel.getColumn(8).setPreferredWidth(58);
        TCModel.getColumn(9).setPreferredWidth(69);
        TCModel.getColumn(10).setPreferredWidth(65);
        //set every coloum's width
    }

    /**
     * Creates new form Login
     */
    public PJMS() {
        try {
            db = new Database();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Open MySQL Database failure!\n" + ex.getMessage(),
                    "Database error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        initComponents();
        projectTableModel = (DefaultTableModel) mainDlg_tbProjects.getModel();
        taskTableModel = (DefaultTableModel) mainDlg_tbRemenderTask.getModel();
        loadAllProjects();
        loadAllTasks();
        mainDlg_tbRemenderTask.setDefaultRenderer(Object.class, new ReminderRenderer());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainDlg_pmProjects = new javax.swing.JPopupMenu();
        mainDlg_pmShowDetail = new javax.swing.JMenuItem();
        mainDlg = new javax.swing.JDialog();
        jLabel30 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jLabel29 = new javax.swing.JLabel();
        jScrollPane11 = new javax.swing.JScrollPane();
        mainDlg_tbRemenderTask = new javax.swing.JTable();
        jLabel34 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel35 = new javax.swing.JLabel();
        mainDlg_btnAddProject = new javax.swing.JButton();
        jButton14 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        mainDlg_tbProjects = new javax.swing.JTable();
        jSeparator6 = new javax.swing.JSeparator();
        mainDlg_miAccount = new javax.swing.JMenuBar();
        jMenu7 = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();
        mainDlg_menuAccount = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        userDlg = new javax.swing.JDialog();
        userDlg_lbHi = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        userDlg_tfID = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        userDlg_tfEmail = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        userDlg_tfAbility = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        userDlg_btnEdit = new javax.swing.JButton();
        userDlg_btnCancel = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        userDlg_tfPass = new javax.swing.JPasswordField();
        jLabel9 = new javax.swing.JLabel();
        userDlg_tfPassconfirm = new javax.swing.JPasswordField();
        jSeparator3 = new javax.swing.JSeparator();
        registerDlg = new javax.swing.JDialog();
        userDlg_lbHi1 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        registerDlg_tfEmail = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        registerDlg_tfAbility = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        registerDlg_btnSave = new javax.swing.JButton();
        registerDlg_btnCancel = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JSeparator();
        registerDlg_tfPass = new javax.swing.JPasswordField();
        jLabel15 = new javax.swing.JLabel();
        registerDlg_tfPassconfirm = new javax.swing.JPasswordField();
        jSeparator5 = new javax.swing.JSeparator();
        jLabel16 = new javax.swing.JLabel();
        registerDlg_tfName = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        loginDlg_tfUserID = new javax.swing.JTextField();
        loginDlg_btnLogin = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        jButton6 = new javax.swing.JButton();
        loginDlg_pwtfPassword = new javax.swing.JPasswordField();
        jSeparator1 = new javax.swing.JSeparator();
        loginDlg_btnRegister = new javax.swing.JButton();

        mainDlg_pmShowDetail.setText("Report");
        mainDlg_pmShowDetail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mainDlg_pmShowDetailActionPerformed(evt);
            }
        });
        mainDlg_pmProjects.add(mainDlg_pmShowDetail);

        mainDlg.setTitle("Main Dialog");

        jLabel30.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N
        jLabel30.setText("Projects List:");

        jPanel9.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel29.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N
        jLabel29.setText("Task Reminder:");

        mainDlg_tbRemenderTask.setAutoCreateRowSorter(true);
        mainDlg_tbRemenderTask.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Name", "Description", "InChargePerson", "IsCompleted", "ReminderInfo", "PlanStartDate", "PlanEndDate", "ActualStartDate", "ActualEndDate"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Long.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane11.setViewportView(mainDlg_tbRemenderTask);

        jLabel34.setText("Near time");

        jButton1.setBackground(new java.awt.Color(255, 0, 0));

        jButton2.setBackground(new java.awt.Color(255, 255, 0));

        jLabel35.setText("Over time");

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(jLabel29)
                        .addGap(373, 373, 373)
                        .addComponent(jLabel34)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel35)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 412, Short.MAX_VALUE))
                    .addComponent(jScrollPane11, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel29, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel34)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel35))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        mainDlg_btnAddProject.setText("Add New project");
        mainDlg_btnAddProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mainDlg_btnAddProjectActionPerformed(evt);
            }
        });

        jButton14.setText("Project Detail");
        jButton14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton14ActionPerformed(evt);
            }
        });

        mainDlg_tbProjects.setAutoCreateRowSorter(true);
        mainDlg_tbProjects.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Name", "Description", "ManagerID", "ManagerName", "TaskNumbers", "IsCompleted", "PlanStartDate", "PlanEndDate", "ActualStartDate", "ActualEndDate"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Long.class, java.lang.String.class, java.lang.String.class, java.lang.Long.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        mainDlg_tbProjects.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        mainDlg_tbProjects.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                mainDlg_tbProjectsMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                mainDlg_tbProjectsMouseReleased(evt);
            }
        });
        jScrollPane1.setViewportView(mainDlg_tbProjects);

        jMenu7.setText("File");

        jMenuItem3.setText("Export to Report");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu7.add(jMenuItem3);

        mainDlg_miAccount.add(jMenu7);

        mainDlg_menuAccount.setText("Hi,...");

        jMenuItem2.setText("Your Account");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        mainDlg_menuAccount.add(jMenuItem2);

        jMenuItem1.setText("Logout");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        mainDlg_menuAccount.add(jMenuItem1);

        mainDlg_miAccount.add(mainDlg_menuAccount);

        mainDlg.setJMenuBar(mainDlg_miAccount);

        javax.swing.GroupLayout mainDlgLayout = new javax.swing.GroupLayout(mainDlg.getContentPane());
        mainDlg.getContentPane().setLayout(mainDlgLayout);
        mainDlgLayout.setHorizontalGroup(
            mainDlgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainDlgLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainDlgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel9, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(mainDlgLayout.createSequentialGroup()
                        .addComponent(jLabel30)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(mainDlg_btnAddProject)
                        .addGap(18, 18, 18)
                        .addComponent(jButton14)
                        .addGap(28, 28, 28))
                    .addComponent(jScrollPane1)
                    .addComponent(jSeparator6))
                .addContainerGap())
        );
        mainDlgLayout.setVerticalGroup(
            mainDlgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainDlgLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainDlgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainDlgLayout.createSequentialGroup()
                        .addComponent(jLabel30)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(mainDlgLayout.createSequentialGroup()
                        .addGap(0, 2, Short.MAX_VALUE)
                        .addGroup(mainDlgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(mainDlg_btnAddProject)
                            .addComponent(jButton14))
                        .addGap(18, 18, 18)))
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator6, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        userDlg.setTitle("Your account");
        userDlg.setModal(true);

        userDlg_lbHi.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N
        userDlg_lbHi.setText("Hi,****");

        jLabel4.setText("ID:");

        userDlg_tfID.setEnabled(false);

        jLabel5.setText("Email (*):");

        userDlg_tfEmail.setEnabled(false);
        userDlg_tfEmail.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                userDlg_tfEmailFocusGained(evt);
            }
        });

        jLabel6.setText("Ability:");

        userDlg_tfAbility.setEnabled(false);
        userDlg_tfAbility.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                userDlg_tfAbilityFocusGained(evt);
            }
        });

        jLabel8.setText("Password(*):");

        userDlg_btnEdit.setText("Edit");
        userDlg_btnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                userDlg_btnEditActionPerformed(evt);
            }
        });

        userDlg_btnCancel.setText("Cancel");
        userDlg_btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                userDlg_btnCancelActionPerformed(evt);
            }
        });

        userDlg_tfPass.setText("jPasswordField1");
        userDlg_tfPass.setEnabled(false);
        userDlg_tfPass.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                userDlg_tfPassFocusGained(evt);
            }
        });

        jLabel9.setText("Confirm:");

        userDlg_tfPassconfirm.setText("jPasswordField1");
        userDlg_tfPassconfirm.setEnabled(false);
        userDlg_tfPassconfirm.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                userDlg_tfPassconfirmFocusGained(evt);
            }
        });

        javax.swing.GroupLayout userDlgLayout = new javax.swing.GroupLayout(userDlg.getContentPane());
        userDlg.getContentPane().setLayout(userDlgLayout);
        userDlgLayout.setHorizontalGroup(
            userDlgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator2)
            .addComponent(jSeparator3)
            .addGroup(userDlgLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(userDlgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, userDlgLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(userDlg_btnCancel)
                        .addGap(18, 18, 18)
                        .addComponent(userDlg_btnEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(userDlgLayout.createSequentialGroup()
                        .addGroup(userDlgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(userDlg_lbHi, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(userDlgLayout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(userDlg_tfID, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(userDlgLayout.createSequentialGroup()
                        .addGroup(userDlgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6)
                            .addComponent(jLabel8)
                            .addComponent(jLabel9))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(userDlgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(userDlg_tfPassconfirm)
                            .addComponent(userDlg_tfEmail, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(userDlg_tfAbility, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(userDlg_tfPass, javax.swing.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE))))
                .addContainerGap())
        );
        userDlgLayout.setVerticalGroup(
            userDlgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(userDlgLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(userDlg_lbHi, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(userDlgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(userDlg_tfID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 25, Short.MAX_VALUE)
                .addGroup(userDlgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(userDlg_tfEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(24, 24, 24)
                .addGroup(userDlgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(userDlg_tfAbility, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(28, 28, 28)
                .addGroup(userDlgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(userDlg_tfPass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(userDlgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(userDlg_tfPassconfirm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(userDlgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(userDlg_btnCancel)
                    .addComponent(userDlg_btnEdit))
                .addGap(34, 34, 34))
        );

        registerDlg.setTitle("Register account");
        registerDlg.setModal(true);

        userDlg_lbHi1.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N
        userDlg_lbHi1.setText("Hi!");

        jLabel12.setText("Email (*):");

        registerDlg_tfEmail.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                registerDlg_tfEmailFocusGained(evt);
            }
        });

        jLabel13.setText("Ability:");

        registerDlg_tfAbility.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                registerDlg_tfAbilityFocusGained(evt);
            }
        });

        jLabel14.setText("Password(*):");

        registerDlg_btnSave.setText("Save");
        registerDlg_btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                registerDlg_btnSaveActionPerformed(evt);
            }
        });

        registerDlg_btnCancel.setText("Cancel");
        registerDlg_btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                registerDlg_btnCancelActionPerformed(evt);
            }
        });

        registerDlg_tfPass.setText("jPasswordField1");
        registerDlg_tfPass.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                registerDlg_tfPassFocusGained(evt);
            }
        });

        jLabel15.setText("Confirm:");

        registerDlg_tfPassconfirm.setText("jPasswordField1");
        registerDlg_tfPassconfirm.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                registerDlg_tfPassconfirmFocusGained(evt);
            }
        });

        jLabel16.setText("Name(*):");

        registerDlg_tfName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                registerDlg_tfNameFocusGained(evt);
            }
        });

        javax.swing.GroupLayout registerDlgLayout = new javax.swing.GroupLayout(registerDlg.getContentPane());
        registerDlg.getContentPane().setLayout(registerDlgLayout);
        registerDlgLayout.setHorizontalGroup(
            registerDlgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator4)
            .addComponent(jSeparator5)
            .addGroup(registerDlgLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(registerDlgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(registerDlgLayout.createSequentialGroup()
                        .addGroup(registerDlgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel12)
                            .addComponent(jLabel13)
                            .addComponent(jLabel14)
                            .addComponent(jLabel15)
                            .addComponent(jLabel16))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(registerDlgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(registerDlg_tfName)
                            .addComponent(registerDlg_tfPassconfirm)
                            .addComponent(registerDlg_tfEmail, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(registerDlg_tfAbility, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(registerDlg_tfPass, javax.swing.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE)))
                    .addGroup(registerDlgLayout.createSequentialGroup()
                        .addComponent(userDlg_lbHi1, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, registerDlgLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(registerDlg_btnCancel)
                        .addGap(12, 12, 12)
                        .addComponent(registerDlg_btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        registerDlgLayout.setVerticalGroup(
            registerDlgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(registerDlgLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(userDlg_lbHi1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(registerDlgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(registerDlg_tfName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(registerDlgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(registerDlg_tfEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(24, 24, 24)
                .addGroup(registerDlgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(registerDlg_tfAbility, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(28, 28, 28)
                .addGroup(registerDlgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(registerDlg_tfPass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(registerDlgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(registerDlg_tfPassconfirm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15)
                .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(registerDlgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(registerDlg_btnCancel)
                    .addComponent(registerDlg_btnSave))
                .addGap(29, 29, 29))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Login");
        setName("frmLogin"); // NOI18N
        addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                formFocusGained(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N
        jLabel1.setText("Password:");

        jLabel2.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N
        jLabel2.setText("UserID:");

        loginDlg_tfUserID.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N
        loginDlg_tfUserID.setText("Email or Employee ID");
        loginDlg_tfUserID.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                loginDlg_tfUserIDFocusGained(evt);
            }
        });
        loginDlg_tfUserID.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                loginDlg_tfUserIDKeyPressed(evt);
            }
        });

        loginDlg_btnLogin.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N
        loginDlg_btnLogin.setText("Login");
        loginDlg_btnLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loginDlg_btnLoginActionPerformed(evt);
            }
        });
        loginDlg_btnLogin.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                loginDlg_btnLoginKeyPressed(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Dialog", 0, 24)); // NOI18N
        jLabel7.setText("Project Management System V1.0");

        jButton6.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N
        jButton6.setText("Cancel");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        loginDlg_pwtfPassword.setText("jPasswordField1");
        loginDlg_pwtfPassword.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                loginDlg_pwtfPasswordFocusGained(evt);
            }
        });
        loginDlg_pwtfPassword.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                loginDlg_pwtfPasswordKeyPressed(evt);
            }
        });

        loginDlg_btnRegister.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N
        loginDlg_btnRegister.setText("Register");
        loginDlg_btnRegister.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loginDlg_btnRegisterActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator1)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(90, 90, 90)
                        .addComponent(loginDlg_btnRegister)
                        .addGap(14, 14, 14)
                        .addComponent(jButton6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 15, Short.MAX_VALUE)
                        .addComponent(loginDlg_btnLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(22, 22, 22)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(loginDlg_tfUserID)
                            .addComponent(loginDlg_pwtfPassword))))
                .addGap(31, 31, 31))
            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 9, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(loginDlg_tfUserID, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(loginDlg_pwtfPassword, javax.swing.GroupLayout.DEFAULT_SIZE, 38, Short.MAX_VALUE))
                .addGap(41, 41, 41)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(loginDlg_btnLogin)
                    .addComponent(jButton6)
                    .addComponent(loginDlg_btnRegister))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private boolean isPasswordCorrect(char[] passwordInput, char[] passwordDBChar) {
        boolean isCorrect = false;
        if (passwordInput.length != passwordDBChar.length) {
            isCorrect = false;
        } else {
            isCorrect = Arrays.equals(passwordInput, passwordDBChar);
        }
        return isCorrect;
    }

    private boolean isInputEmail(String str) {
        Matcher matcher = EMAIL_PATTERN.matcher(str);
        return matcher.matches();
    }

    private boolean isMatchUserAccount() {
        String userID = loginDlg_tfUserID.getText();
        char[] passwordInput = loginDlg_pwtfPassword.getPassword();
        String passwordDB = "";
        char[] passwordDBChar = null;
        long userIdentify = 0;
        if (isInputEmail(userID)) {
            try {
                passwordDB = db.getPasswordByEmail(userID);
                userIdentify = db.getUserIdByEmail(userID);
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Select password from users by Email failure!\n" + ex.getMessage(),
                        "Database error",
                        JOptionPane.ERROR_MESSAGE);
                loginDlg_tfUserID.requestFocusInWindow();
                return false;
            }
            if (passwordDB.isEmpty()) {
                JOptionPane.showMessageDialog(null,
                        "You are enter a wrong email!\n",
                        "Email wrong!",
                        JOptionPane.INFORMATION_MESSAGE);
                loginDlg_tfUserID.requestFocusInWindow();
                return false;
            }
        } else {
            if (isInteger(userID)) {
                try {
                    passwordDB = db.getPasswordByEmployeeID(userID);
                    userIdentify = Integer.parseInt(userID);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null,
                            "Select password from users by ID failure!\n" + ex.getMessage(),
                            "Database error",
                            JOptionPane.ERROR_MESSAGE);
                    loginDlg_tfUserID.requestFocusInWindow();
                    return false;
                }
                if (passwordDB.isEmpty()) {
                    JOptionPane.showMessageDialog(null,
                            "You are enter a wrong Employee ID!\n",
                            "Employee ID wrong!",
                            JOptionPane.INFORMATION_MESSAGE);
                    loginDlg_tfUserID.requestFocusInWindow();
                    return false;
                }
            } else {
                JOptionPane.showMessageDialog(null,
                        "You entered a ID that may contain characters!\n",
                        "Enter error",
                        JOptionPane.ERROR_MESSAGE);
                loginDlg_tfUserID.requestFocusInWindow();
                return false;
            }
        }
        passwordDBChar = passwordDB.toCharArray();
        if (!isPasswordCorrect(passwordInput, passwordDBChar)) {
            JOptionPane.showMessageDialog(null,
                    "You are enter a wrong password!\n",
                    "Password wrong!",
                    JOptionPane.INFORMATION_MESSAGE);
            loginDlg_pwtfPassword.requestFocusInWindow();
            return false;
        }

        try {
            currentUser = db.getUserById(userIdentify);
            mainDlg_menuAccount.setText("  |  Hi! " + currentUser.getName());
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "There is no your ID in database!\n" + ex.getMessage(),
                    "Database error",
                    JOptionPane.ERROR_MESSAGE);
        }
        return true;
    }

    private void loginDlg_btnLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loginDlg_btnLoginActionPerformed
        String userID = loginDlg_tfUserID.getText();
        if(userID.isEmpty()){
            JOptionPane.showMessageDialog(null,
                    "You still didn't enter a userID!\n",
                    "Enter wrong!",
                    JOptionPane.INFORMATION_MESSAGE);
            loginDlg_tfUserID.setText("Email or Employee ID");
            loginDlg_tfUserID.requestFocusInWindow();
            return;
        }else if(isMatchUserAccount()) {
            this.setVisible(false);
            mainDlg.pack();
            mainDlg.setLocationRelativeTo(this);
            mainDlg.setVisible(true);
        }
    }//GEN-LAST:event_loginDlg_btnLoginActionPerformed


    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jButton6ActionPerformed

    private void loginDlg_tfUserIDFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_loginDlg_tfUserIDFocusGained
        loginDlg_tfUserID.selectAll();
    }//GEN-LAST:event_loginDlg_tfUserIDFocusGained

    private void loginDlg_pwtfPasswordFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_loginDlg_pwtfPasswordFocusGained
        loginDlg_pwtfPassword.selectAll();
    }//GEN-LAST:event_loginDlg_pwtfPasswordFocusGained

    private void loginDlg_btnLoginKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_loginDlg_btnLoginKeyPressed
        if (evt.getKeyChar() == KeyEvent.VK_ENTER) {
            String userID = loginDlg_tfUserID.getText();
            if (userID.isEmpty()) {
                JOptionPane.showMessageDialog(null,
                        "You still didn't enter a userID!\n",
                        "Enter wrong!",
                        JOptionPane.INFORMATION_MESSAGE);
                loginDlg_tfUserID.setText("Email or Employee ID");
                loginDlg_tfUserID.requestFocusInWindow();
                return;
            } else if (isMatchUserAccount()) {
                this.setVisible(false);
                mainDlg.pack();
                mainDlg.setLocationRelativeTo(this);
                mainDlg.setVisible(true);
            }
        }
    }//GEN-LAST:event_loginDlg_btnLoginKeyPressed
    private void showProjectEditDlg() {        
        int rowindex = mainDlg_tbProjects.getSelectedRow();        
        if (rowindex != -1) {
            Object ido = mainDlg_tbProjects.getValueAt(rowindex, 0);
            int id = 0;
            if (ido != null) {
                id = Integer.parseInt(ido.toString());
            }
            new ProjectDetails(this, id).setVisible(true);
            mainDlg.setVisible(false);
        } else {
            JOptionPane.showMessageDialog(null,
                    "Please select a project!\n",
                    "Control tutorial!",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
    private void jButton14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton14ActionPerformed
        showProjectEditDlg();
    }//GEN-LAST:event_jButton14ActionPerformed

    private void mainDlg_btnAddProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mainDlg_btnAddProjectActionPerformed
        new ProjectDetails(this, 0).setVisible(true);
        mainDlg.setVisible(false);
    }//GEN-LAST:event_mainDlg_btnAddProjectActionPerformed

    private void mainDlg_tbProjectsMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mainDlg_tbProjectsMousePressed
        if (evt.getClickCount() == 2) {
            showProjectEditDlg();
        } else{
            JTable table = (JTable) evt.getSource(); 
            Point point = evt.getPoint();
            int index = table.rowAtPoint(point);
            String idString = mainDlg_tbProjects.getValueAt(index, 0).toString();
            String isCompleted = mainDlg_tbProjects.getValueAt(index, 6).toString();
            //String idString = projectTableModel.getValueAt(index, 0).toString();
            if (isInteger(idString)) {
                int id = Integer.parseInt(idString);
                loadTasksById(id,isCompleted);
            }
        }
    }//GEN-LAST:event_mainDlg_tbProjectsMousePressed

    private void mainDlg_pmShowDetailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mainDlg_pmShowDetailActionPerformed
        //showProjectEditDlg();
        int rowIndex = mainDlg_tbProjects.getSelectedRow();
        String projectName = mainDlg_tbProjects.getValueAt(rowIndex, 1).toString();
        String title = projectName + "'s Report";            
        ProjectTasksTimeSeriesReport report = new ProjectTasksTimeSeriesReport(title);
    }//GEN-LAST:event_mainDlg_pmShowDetailActionPerformed

    private void mainDlg_tbProjectsMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mainDlg_tbProjectsMouseReleased
        if (evt.isPopupTrigger()) {
            JTable table = (JTable) evt.getSource();
            Point point = evt.getPoint();
            int row = table.rowAtPoint(point);
            mainDlg_tbProjects.setRowSelectionInterval(row, row);
            mainDlg_pmProjects.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_mainDlg_tbProjectsMouseReleased

    private void loginDlg_tfUserIDKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_loginDlg_tfUserIDKeyPressed
        if ((evt.getKeyChar() == KeyEvent.VK_ENTER)) {
            loginDlg_pwtfPassword.requestFocusInWindow();
        }
    }//GEN-LAST:event_loginDlg_tfUserIDKeyPressed

    private void loginDlg_pwtfPasswordKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_loginDlg_pwtfPasswordKeyPressed
        if ((evt.getKeyChar() == KeyEvent.VK_ENTER)) {
            loginDlg_btnLogin.requestFocusInWindow();
        }
    }//GEN-LAST:event_loginDlg_pwtfPasswordKeyPressed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        mainDlg.dispose();
        this.setVisible(true);
        this.requestFocusInWindow(true);
    }//GEN-LAST:event_jMenuItem1ActionPerformed
    private void disableUserDlgTextField() {
        userDlg_tfID.setEnabled(false);
        userDlg_tfEmail.setEnabled(false);
        userDlg_tfAbility.setEnabled(false);
        userDlg_tfPass.setEnabled(false);
        userDlg_tfPassconfirm.setEnabled(false);
        userDlg_btnEdit.setText("Edit");
    }

    private void enableUserDlgTextField() {
        userDlg_tfEmail.setEnabled(true);
        userDlg_tfAbility.setEnabled(true);
        userDlg_tfPass.setEnabled(true);
        userDlg_tfPassconfirm.setEnabled(true);
        userDlg_btnEdit.setText("Save");
    }

    private void showCurrentUserInfo() {
        try {
            User user = db.getUserById(currentUser.getId());
            userDlg_lbHi.setText("Hi, " + currentUser.getName());
            userDlg_tfID.setText("" + currentUser.getId());
            userDlg_tfEmail.setText(user.getEmail());
            userDlg_tfAbility.setText(user.getAbility());
            userDlg_tfPass.setText(user.getPassword());
            userDlg_tfPassconfirm.setText(user.getPassword());
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Maybe there is some wrong in database!\n" + ex.getMessage(),
                    "Database error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        userDlg.pack();
        disableUserDlgTextField();
        if (currentUser != null) {
            showCurrentUserInfo();
        }
        userDlg.setLocationRelativeTo(this);
        userDlg.setVisible(true);
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void userDlg_btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_userDlg_btnCancelActionPerformed
        editFlag = false;
        userDlg.dispose();
    }//GEN-LAST:event_userDlg_btnCancelActionPerformed

    private void userDlg_btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_userDlg_btnEditActionPerformed
        if (!editFlag) {
            editFlag = true;
            enableUserDlgTextField();
            userDlg_tfEmail.requestFocusInWindow();
        } else if (validateUserInputInfo()) {            
            String email = userDlg_tfEmail.getText();
            String ability = userDlg_tfAbility.getText();
            char[] password = userDlg_tfPass.getPassword();
            User user = new User(currentUser.getId(), currentUser.getName(), email, ability, String.valueOf(password));
            try {
                db.updateUser(user);
                JOptionPane.showMessageDialog(null,
                        "Your account has updated successfully!\n",
                        "Successfully!",
                        JOptionPane.INFORMATION_MESSAGE);
                editFlag = false;
                disableUserDlgTextField();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null,
                    "There is some error when update your information!\n" + ex.getMessage(),
                    "Database error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_userDlg_btnEditActionPerformed

    private void userDlg_tfEmailFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_userDlg_tfEmailFocusGained
        userDlg_tfEmail.selectAll();
    }//GEN-LAST:event_userDlg_tfEmailFocusGained

    private void userDlg_tfAbilityFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_userDlg_tfAbilityFocusGained
        userDlg_tfAbility.selectAll();
    }//GEN-LAST:event_userDlg_tfAbilityFocusGained

    private void userDlg_tfPassFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_userDlg_tfPassFocusGained
        userDlg_tfPass.selectAll();
    }//GEN-LAST:event_userDlg_tfPassFocusGained

    private void userDlg_tfPassconfirmFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_userDlg_tfPassconfirmFocusGained
        userDlg_tfPassconfirm.selectAll();
    }//GEN-LAST:event_userDlg_tfPassconfirmFocusGained

    private void loginDlg_btnRegisterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loginDlg_btnRegisterActionPerformed
        registerDlg.pack();
        registerDlg.setLocationRelativeTo(this);
        registerDlg.setVisible(true);
    }//GEN-LAST:event_loginDlg_btnRegisterActionPerformed

    private void registerDlg_tfEmailFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_registerDlg_tfEmailFocusGained
        registerDlg_tfEmail.selectAll();
    }//GEN-LAST:event_registerDlg_tfEmailFocusGained

    private void registerDlg_tfAbilityFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_registerDlg_tfAbilityFocusGained
        registerDlg_tfAbility.selectAll();
    }//GEN-LAST:event_registerDlg_tfAbilityFocusGained
    /*
    private void disableRegisterDlgTextField(){
        registerDlg_tfName.setEnabled(false);
        registerDlg_tfEmail.setEnabled(false);
        registerDlg_tfAbility.setEnabled(false);
        registerDlg_tfPass.setEnabled(false);
        registerDlg_tfPassconfirm.setEnabled(false);        
    }
    private void enableRegisterDlgTextField(){
        registerDlg_tfName.setEnabled(true);
        registerDlg_tfEmail.setEnabled(true);
        registerDlg_tfAbility.setEnabled(true);
        registerDlg_tfPass.setEnabled(true);
        registerDlg_tfPassconfirm.setEnabled(true);
    }
    */
    private void registerDlg_btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_registerDlg_btnSaveActionPerformed
        if (validateRegisterInfo()) {  
            String name = registerDlg_tfName.getText();
            String email = registerDlg_tfEmail.getText();
            String ability = registerDlg_tfAbility.getText();
            char[] password = registerDlg_tfPass.getPassword();
            User user = new User(1, name, email, ability, String.valueOf(password));
            try {
                db.AddUser(user);
                long id = db.getUserIdByEmail(email);
                registerDlg.dispose();
                JOptionPane.showMessageDialog(null,
                        "Your new account has been saved successfully!\n"+"Please remember your EmployeeID is "+id
                        +".\nYou can use your ID or Email to login our system",
                        "Successfully!",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "You enter a wrong information!\n" + ex.getMessage(),
                    "Database error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_registerDlg_btnSaveActionPerformed

    private void registerDlg_btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_registerDlg_btnCancelActionPerformed
        registerDlg.dispose();
        this.requestFocusInWindow(true);
    }//GEN-LAST:event_registerDlg_btnCancelActionPerformed

    private void registerDlg_tfPassFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_registerDlg_tfPassFocusGained
         registerDlg_tfPass.selectAll();
    }//GEN-LAST:event_registerDlg_tfPassFocusGained

    private void registerDlg_tfPassconfirmFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_registerDlg_tfPassconfirmFocusGained
        registerDlg_tfPassconfirm.selectAll();        
    }//GEN-LAST:event_registerDlg_tfPassconfirmFocusGained

    private void registerDlg_tfNameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_registerDlg_tfNameFocusGained
       registerDlg_tfName.selectAll();
    }//GEN-LAST:event_registerDlg_tfNameFocusGained

    private void formFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusGained
        loginDlg_tfUserID.requestFocusInWindow();
    }//GEN-LAST:event_formFocusGained

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        int rowIndex = mainDlg_tbProjects.getSelectedRow();
        if(rowIndex==-1){
            JOptionPane.showMessageDialog(null,
                    "Please select a project before export to report!\n",
                    "User tutorial information",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            String projectName = projectTableModel.getValueAt(rowIndex, 1).toString();
            String title = projectName + "'s Report";
            ProjectTasksTimeSeriesReport report = new ProjectTasksTimeSeriesReport(title);
        }     
    }//GEN-LAST:event_jMenuItem3ActionPerformed
    private boolean validateRegisterInfo(){
        String name = registerDlg_tfName.getText();
        String email = registerDlg_tfEmail.getText();
        char[] password = registerDlg_tfPass.getPassword();
        char[] password2 = registerDlg_tfPassconfirm.getPassword();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Name cann't be empty!\n",
                    "Enter wrong!",
                    JOptionPane.ERROR_MESSAGE);
            registerDlg_tfName.requestFocusInWindow();
            return false;
        }
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Email cann't be empty!\n",
                    "Enter wrong!",
                    JOptionPane.ERROR_MESSAGE);
            registerDlg_tfEmail.requestFocusInWindow();
            return false;
        } else if (!isInputEmail(email)) {
            JOptionPane.showMessageDialog(null,
                    "You enter a wrong email,please enter again!\n",
                    "Enter wrong!",
                    JOptionPane.ERROR_MESSAGE);
            registerDlg_tfEmail.requestFocusInWindow();
            return false;
        }
        if (password.length == 0) {
            JOptionPane.showMessageDialog(null,
                    "Password cann't be empty!\n",
                    "Enter wrong!",
                    JOptionPane.ERROR_MESSAGE);
            registerDlg_tfPass.requestFocusInWindow();
            return false;
        }
        if (password2.length == 0) {
            JOptionPane.showMessageDialog(null,
                    "Password cann't be empty!\n",
                    "Enter wrong!",
                    JOptionPane.ERROR_MESSAGE);
            registerDlg_tfPassconfirm.requestFocusInWindow();
            return false;
        } else if (!isPasswordCorrect(password, password2)) {
            JOptionPane.showMessageDialog(null,
                    "Passwords are not match,please enter again!\n",
                    "Enter wrong!",
                    JOptionPane.ERROR_MESSAGE);
            registerDlg_tfPass.requestFocusInWindow();
            registerDlg_tfPassconfirm.setText("");
            return false;
        }
        return true;
    }
    private boolean validateUserInputInfo() {
        String email = userDlg_tfEmail.getText();
        char[] password = userDlg_tfPass.getPassword();
        char[] password2 = userDlg_tfPassconfirm.getPassword();
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Email cann't be empty!\n",
                    "Enter wrong!",
                    JOptionPane.ERROR_MESSAGE);
            userDlg_tfEmail.requestFocusInWindow();
            return false;
        } else if (!isInputEmail(email)) {
            JOptionPane.showMessageDialog(null,
                    "You enter a wrong email,please enter again!\n",
                    "Enter wrong!",
                    JOptionPane.ERROR_MESSAGE);
            userDlg_tfEmail.requestFocusInWindow();
            return false;
        }
        if (password.length == 0) {
            JOptionPane.showMessageDialog(null,
                    "Password cann't be empty!\n",
                    "Enter wrong!",
                    JOptionPane.ERROR_MESSAGE);
            userDlg_tfPass.requestFocusInWindow();
            return false;
        }
        if (password2.length == 0) {
            JOptionPane.showMessageDialog(null,
                    "Password cann't be empty!\n",
                    "Enter wrong!",
                    JOptionPane.ERROR_MESSAGE);
            userDlg_tfPassconfirm.requestFocusInWindow();
            return false;
        } else if (!isPasswordCorrect(password, password2)) {
            JOptionPane.showMessageDialog(null,
                    "Passwords are not match,please enter again!\n",
                    "Enter wrong!",
                    JOptionPane.ERROR_MESSAGE);
            userDlg_tfPass.requestFocusInWindow();
            userDlg_tfPassconfirm.setText("");
            return false;
        }
        return true;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PJMS.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new PJMS().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton6;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu7;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JButton loginDlg_btnLogin;
    private javax.swing.JButton loginDlg_btnRegister;
    private javax.swing.JPasswordField loginDlg_pwtfPassword;
    private javax.swing.JTextField loginDlg_tfUserID;
    private javax.swing.JDialog mainDlg;
    private javax.swing.JButton mainDlg_btnAddProject;
    private javax.swing.JMenu mainDlg_menuAccount;
    private javax.swing.JMenuBar mainDlg_miAccount;
    private javax.swing.JPopupMenu mainDlg_pmProjects;
    private javax.swing.JMenuItem mainDlg_pmShowDetail;
    private javax.swing.JTable mainDlg_tbProjects;
    private javax.swing.JTable mainDlg_tbRemenderTask;
    private javax.swing.JDialog registerDlg;
    private javax.swing.JButton registerDlg_btnCancel;
    private javax.swing.JButton registerDlg_btnSave;
    private javax.swing.JTextField registerDlg_tfAbility;
    private javax.swing.JTextField registerDlg_tfEmail;
    private javax.swing.JTextField registerDlg_tfName;
    private javax.swing.JPasswordField registerDlg_tfPass;
    private javax.swing.JPasswordField registerDlg_tfPassconfirm;
    private javax.swing.JDialog userDlg;
    private javax.swing.JButton userDlg_btnCancel;
    private javax.swing.JButton userDlg_btnEdit;
    private javax.swing.JLabel userDlg_lbHi;
    private javax.swing.JLabel userDlg_lbHi1;
    private javax.swing.JTextField userDlg_tfAbility;
    private javax.swing.JTextField userDlg_tfEmail;
    private javax.swing.JTextField userDlg_tfID;
    private javax.swing.JPasswordField userDlg_tfPass;
    private javax.swing.JPasswordField userDlg_tfPassconfirm;
    // End of variables declaration//GEN-END:variables
}
