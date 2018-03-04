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

    //private final Pattern EMAIL_PATTERN = Pattern.compile(".+@.+");
    private final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$");
    private DefaultTableModel projectTableModel = null;
    private DefaultTableModel taskTableModel = null;
    private final int REMINDER_DAY = 2;
    private final int REMINDER_COLOR_RED = 1;
    //private final int REMINDER_COLOR_BLUE = 2;
    private final int REMINDER_COLOR_YELLOW = 3;
    private int[] colorFlag;
    //private int loginInputFlag=1;
    private Database db;
    public static User currentUser = null;
    private boolean editFlag = false;

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

    private void showTasksList(ArrayList<Task> pList) {
        colorFlag = new int[pList.size()];
        int rowIndex = 0;
        String alertString = "";
        BigDecimal diff = null;
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
                    if ((diff.compareTo(BigDecimal.ZERO) == 0 || diff.compareTo(BigDecimal.ZERO) == 1) &&(diff.compareTo(new BigDecimal(REMINDER_DAY)) == -1||diff.compareTo(new BigDecimal(REMINDER_DAY)) == 0)) {
                        alertString = diff.abs() + " Days due to start Date";
                        colorFlag[rowIndex] = REMINDER_COLOR_YELLOW;
                    } else if (diff.compareTo(BigDecimal.ZERO) == -1) {                      
                        alertString = diff.abs() + " Days over start Date ";
                        colorFlag[rowIndex] = REMINDER_COLOR_RED;
                    }
                } else if (eda == null && edp != null) {
                    diff = countTwoDate(edp, now);
                    if ((diff.compareTo(BigDecimal.ZERO) == 0 || diff.compareTo(BigDecimal.ZERO) == 1) && (diff.compareTo(new BigDecimal(REMINDER_DAY)) == -1||diff.compareTo(new BigDecimal(REMINDER_DAY)) == 0)) {
                        alertString = diff.abs() + " Days due to end Date";
                        colorFlag[rowIndex] = REMINDER_COLOR_YELLOW;
                    } else if (diff.compareTo(BigDecimal.ZERO) == -1) {
                        alertString = diff.abs() + " Days over end Date ";
                        colorFlag[rowIndex] = REMINDER_COLOR_RED;
                    }
                }
            }
            rowIndex++;
            taskTableModel.addRow(new Object[]{l.getId(), l.getName(), l.getDescription(), l.getPersonInChargeName(), l.getIsCompleted(), alertString, l.getStartDatePlanned(), l.getEndDatePlanned(), l.getStartDateActual(), l.getEndDateActual()});
            alertString = "";
        }
    }

    private void loadTasksById(int id) {
        for (int i = taskTableModel.getRowCount() - 1; i >= 0; i--) {
            taskTableModel.removeRow(i);
        }
        try {
            ArrayList<Task> pList = db.getTasksById(id);
            showTasksList(pList);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Load projects information failure!\n" + ex.getMessage(),
                    "Database error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadAllTasks() {
        for (int i = taskTableModel.getRowCount() - 1; i >= 0; i--) {
            taskTableModel.removeRow(i);
        }
        try {
            ArrayList<Task> pList = db.getAllTasks();
            showTasksList(pList);
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

        mainDlg_tbRemenderTask.setDefaultRenderer(Object.class, new ReminderRenderer());
    }

    private void loadAllProjects() {
        for (int i = projectTableModel.getRowCount() - 1; i >= 0; i--) {
            projectTableModel.removeRow(i);
        }

        try {
            ArrayList<Project> pList = db.getAllProjects();
            for (Project l : pList) {
                projectTableModel.addRow(new Object[]{l.getId(), l.getName(), l.getDescription(), l.getProjectManager(), l.getPMName(), l.getTasknums(), l.getIsCompleted(), l.getStartDatePlanned(), l.getEndDatePlanned(), l.getStartDateActual(), l.getEndDateActual()});
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
        mainDlg_miAccount = new javax.swing.JMenuBar();
        jMenu7 = new javax.swing.JMenu();
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
        jButton4 = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        userDlg_tfPass = new javax.swing.JPasswordField();
        jLabel9 = new javax.swing.JLabel();
        userDlg_tfPassconfirm = new javax.swing.JPasswordField();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        loginDlg_tfUserID = new javax.swing.JTextField();
        loginDlg_btnLogin = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        jButton6 = new javax.swing.JButton();
        loginDlg_pwtfPassword = new javax.swing.JPasswordField();
        jSeparator1 = new javax.swing.JSeparator();

        mainDlg_pmShowDetail.setText("Show Detail");
        mainDlg_pmShowDetail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mainDlg_pmShowDetailActionPerformed(evt);
            }
        });
        mainDlg_pmProjects.add(mainDlg_pmShowDetail);

        mainDlg.setTitle("Main Dialog");
        mainDlg.addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
                mainDlgWindowGainedFocus(evt);
            }
            public void windowLostFocus(java.awt.event.WindowEvent evt) {
            }
        });

        jLabel30.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N
        jLabel30.setText("Projects List:");

        jPanel9.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel29.setText("Task Reminder:");

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
                        .addGap(0, 451, Short.MAX_VALUE))
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
                java.lang.Long.class, java.lang.String.class, java.lang.String.class, java.lang.Long.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
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
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainDlgLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainDlgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, mainDlgLayout.createSequentialGroup()
                        .addComponent(jLabel30)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(mainDlg_btnAddProject)
                        .addGap(18, 18, 18)
                        .addComponent(jButton14)
                        .addGap(28, 28, 28))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING))
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
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainDlgLayout.createSequentialGroup()
                        .addGap(0, 2, Short.MAX_VALUE)
                        .addGroup(mainDlgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(mainDlg_btnAddProject)
                            .addComponent(jButton14))
                        .addGap(18, 18, 18)))
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 196, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(42, 42, 42)
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

        jButton4.setText("Cancel");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
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
            .addGroup(userDlgLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(userDlgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, userDlgLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton4)
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
                        .addGroup(userDlgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(userDlg_tfEmail)
                            .addComponent(userDlg_tfPassconfirm, javax.swing.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE)
                            .addComponent(userDlg_tfAbility)
                            .addComponent(userDlg_tfPass, javax.swing.GroupLayout.Alignment.LEADING))))
                .addContainerGap())
            .addComponent(jSeparator3)
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
                .addGap(28, 28, 28)
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 16, Short.MAX_VALUE)
                .addGroup(userDlgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(userDlg_tfPassconfirm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(userDlgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton4)
                    .addComponent(userDlg_btnEdit))
                .addContainerGap())
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Login");
        setName("frmLogin"); // NOI18N

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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator1)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(205, 205, 205)
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
                    .addComponent(jButton6))
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
            Logger.getLogger(PJMS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    private void loginDlg_btnLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loginDlg_btnLoginActionPerformed
        if (isMatchUserAccount()) {
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
            if (isMatchUserAccount()) {
                this.setVisible(false);
                mainDlg.pack();
                mainDlg.setLocationRelativeTo(this);
                mainDlg.setVisible(true);
            }
        }
    }//GEN-LAST:event_loginDlg_btnLoginKeyPressed
    private void showProjectEditDlg() {
        /*
        int rowindex = mainDlg_tbProjects.getSelectedRow();
        if (rowindex != -1) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
            Project p = null;
            try {
                Object ido = projectTableModel.getValueAt(rowindex, 0);
                Object nameo = projectTableModel.getValueAt(rowindex, 1);
                Object descriptiono = projectTableModel.getValueAt(rowindex, 2);
                Object personInChargeo = projectTableModel.getValueAt(rowindex, 3);
                Object isCompletedo = projectTableModel.getValueAt(rowindex, 6);

                int id = 0;
                String name = "";
                String description = "";
                int personInCharge = 0;
                boolean isCompleted = true;
                if (ido != null) {
                    id = Integer.parseInt(ido.toString());
                }
                if (nameo != null) {
                    name = nameo.toString();
                }
                if (descriptiono != null) {
                    description = descriptiono.toString();
                }
                if (personInChargeo != null) {
                    personInCharge = Integer.parseInt(personInChargeo.toString());
                }
                if (isCompletedo != null) {
                    isCompleted = Boolean.parseBoolean(isCompletedo.toString());
                }
                Object sdpo = projectTableModel.getValueAt(rowindex, 7);
                Object edpo = projectTableModel.getValueAt(rowindex, 8);
                Object sdao = projectTableModel.getValueAt(rowindex, 9);
                Object edao = projectTableModel.getValueAt(rowindex, 10);
                Date sdp = null;
                Date sda = null;
                Date edp = null;
                Date eda = null;
                if (sdpo != null) {
                    sdp = sdf.parse(sdpo.toString());
                }
                if (edpo != null) {
                    edp = sdf.parse(edpo.toString());
                }
                if (sdao != null) {
                    sda = sdf.parse(sdao.toString());
                }
                if (edao != null) {
                    eda = sdf.parse(edao.toString());
                }
                p = new Project(id, name, description, sdp, edp, sda, eda, personInCharge, isCompleted);
                new ProjectDetails(mainDlg, id).setVisible(true);
                mainDlg.setVisible(false);
            } catch (ParseException ex) {
                Logger.getLogger(PJMS.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            JOptionPane.showMessageDialog(null,
                    "Please select a project!\n",
                    "Control tutorial!",
                    JOptionPane.INFORMATION_MESSAGE);
        }
        */
        int rowindex = mainDlg_tbProjects.getSelectedRow();
        if (rowindex != -1) {
            Object ido = projectTableModel.getValueAt(rowindex, 0);
            int id = 0;
            if (ido != null) {
                id = Integer.parseInt(ido.toString());
            }
            new ProjectDetails(mainDlg, id).setVisible(true);
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
        new ProjectDetails(mainDlg, 0).setVisible(true);
        mainDlg.setVisible(false);
    }//GEN-LAST:event_mainDlg_btnAddProjectActionPerformed

    private void mainDlgWindowGainedFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_mainDlgWindowGainedFocus
        loadAllProjects();
        loadAllTasks();
    }//GEN-LAST:event_mainDlgWindowGainedFocus

    private void mainDlg_tbProjectsMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mainDlg_tbProjectsMousePressed
        if (evt.getClickCount() == 2) {
            showProjectEditDlg();
        } else {
            int index = mainDlg_tbProjects.getSelectedRow();
            String idString = projectTableModel.getValueAt(index, 0).toString();
            if (isInteger(idString)) {
                int id = Integer.parseInt(idString);
                loadTasksById(id);
            }
        }
    }//GEN-LAST:event_mainDlg_tbProjectsMousePressed

    private void mainDlg_pmShowDetailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mainDlg_pmShowDetailActionPerformed
        showProjectEditDlg();
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
            Logger.getLogger(PJMS.class.getName()).log(Level.SEVERE, null, ex);
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

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        editFlag = false;
        userDlg.dispose();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void userDlg_btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_userDlg_btnEditActionPerformed
        if (!editFlag) {
            editFlag = true;
            enableUserDlgTextField();
            userDlg_tfEmail.requestFocusInWindow();
        } else if (validateUserInputInfo()) {
            editFlag = false;
            disableUserDlgTextField();
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
            } catch (SQLException ex) {
                Logger.getLogger(PJMS.class.getName()).log(Level.SEVERE, null, ex);
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
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton6;
    private javax.swing.JLabel jLabel1;
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
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JButton loginDlg_btnLogin;
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
    private javax.swing.JDialog userDlg;
    private javax.swing.JButton userDlg_btnEdit;
    private javax.swing.JLabel userDlg_lbHi;
    private javax.swing.JTextField userDlg_tfAbility;
    private javax.swing.JTextField userDlg_tfEmail;
    private javax.swing.JTextField userDlg_tfID;
    private javax.swing.JPasswordField userDlg_tfPass;
    private javax.swing.JPasswordField userDlg_tfPassconfirm;
    // End of variables declaration//GEN-END:variables
}
