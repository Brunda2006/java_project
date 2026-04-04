import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.*;

public class StudentDashboard extends JFrame {

    private JComboBox<String> courseBox;
    private JTable table;
    private DefaultTableModel model;
    private JLabel totalCreditsLabel;

    private String studentId, studentBranch, studentName, studentSection;
    private int studentYear;

    private DefaultListModel<String> notificationModel;
    private JList<String> notifyList;

    private Set<String> shownNotifications = new HashSet<>();
     private Set<String> seenCourses = new HashSet<>();
    private Map<String, String> courseDetails = new HashMap<>();
   

    public StudentDashboard(String studentId) {

        this.studentId = studentId;

        setTitle("Student Dashboard");
        setSize(1100, 650);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        loadStudentDetails();

        // ===== SIDEBAR =====
        JPanel sideBar = new JPanel(new BorderLayout());
        sideBar.setBackground(new Color(20, 25, 30));
        sideBar.setPreferredSize(new Dimension(240, getHeight()));

        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(new Color(20, 25, 30));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        JButton dashBtn = createBtn("Dashboard");
        JButton regBtn = createBtn("Register Course");
        JButton courseBtn = createBtn("My Courses");
        JButton logoutBtn = createBtn("Logout");

        menuPanel.add(dashBtn);
        menuPanel.add(Box.createVerticalStrut(15));
        menuPanel.add(regBtn);
        menuPanel.add(Box.createVerticalStrut(15));
        menuPanel.add(courseBtn);
        menuPanel.add(Box.createVerticalStrut(15));
        menuPanel.add(logoutBtn);

        // ===== NOTIFICATIONS =====
        notificationModel = new DefaultListModel<>();
        notifyList = new JList<>(notificationModel);

        notifyList.setBackground(new Color(52, 58, 64));
        notifyList.setForeground(Color.WHITE);

        JScrollPane notifyScroll = new JScrollPane(notifyList);

        JButton deleteBtn = new JButton("Delete");
        deleteBtn.addActionListener(e -> deleteNotification());

        JPanel notifPanel = new JPanel(new BorderLayout());
        notifPanel.setBackground(new Color(40, 44, 52));

        JLabel notifLabel = new JLabel("🔔 Notifications");
        notifLabel.setForeground(new Color(255, 193, 7));

        notifPanel.add(notifLabel, BorderLayout.NORTH);
        notifPanel.add(notifyScroll, BorderLayout.CENTER);
        notifPanel.add(deleteBtn, BorderLayout.SOUTH);

        sideBar.add(menuPanel, BorderLayout.NORTH);
        sideBar.add(notifPanel, BorderLayout.CENTER);

        add(sideBar, BorderLayout.WEST);

        // ===== MAIN =====
        JPanel main = new JPanel(new CardLayout());

        // ===== DASHBOARD =====
        JPanel dash = new JPanel(new GridBagLayout());
        dash.setBackground(new Color(235, 240, 245));

        JPanel card = new JPanel(new GridLayout(6,1,10,10));
        card.setPreferredSize(new Dimension(420,300));
        card.setBackground(Color.WHITE);

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200,200,200)),
                BorderFactory.createEmptyBorder(20,25,20,25)
        ));

        card.add(createLabel("Welcome " + studentName, true));
        card.add(createLabel("ID: " + studentId, false));
        card.add(createLabel("Branch: " + studentBranch, false));
        card.add(createLabel("Year: " + studentYear, false));
        card.add(createLabel("Section: " + studentSection, false));
        card.add(createLabel("Max Credits: 24", false));

        dash.add(card);

        // ===== REGISTER =====
        JPanel reg = new JPanel();
        courseBox = new JComboBox<>();
        JButton registerBtn = new JButton("Register");

        reg.add(courseBox);
        reg.add(registerBtn);

        // ===== COURSES =====
        JPanel coursePanel = new JPanel(new BorderLayout());
        model = new DefaultTableModel(new String[]{"Course","Credits","Deadline"},0);
        table = new JTable(model);
        totalCreditsLabel = new JLabel("Total Credits: 0");

        coursePanel.add(new JScrollPane(table), BorderLayout.CENTER);
        coursePanel.add(totalCreditsLabel, BorderLayout.SOUTH);

        main.add(dash,"DASH");
        main.add(reg,"REG");
        main.add(coursePanel,"COURSE");

        add(main,BorderLayout.CENTER);

        CardLayout cl = (CardLayout) main.getLayout();

        dashBtn.addActionListener(e -> cl.show(main,"DASH"));

        regBtn.addActionListener(e -> {
            loadCourses();
            cl.show(main,"REG");
        });

        courseBtn.addActionListener(e -> {
            loadRegisteredCourses();
            cl.show(main,"COURSE");
        });

        logoutBtn.addActionListener(e -> {
            dispose();
            new RoleSelectionFrame();
        });

        registerBtn.addActionListener(e -> registerCourse());

        checkDeadlines();
        new javax.swing.Timer(60000, e -> checkDeadlines()).start();

        setVisible(true);
    }

    private JLabel createLabel(String text, boolean title) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", title ? Font.BOLD : Font.PLAIN, title ? 18 : 14));
        return lbl;
    }

    private void loadStudentDetails() {
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                "SELECT full_name,branch_name,year,section FROM students WHERE student_id=?");
            ps.setString(1, studentId);
            ResultSet rs = ps.executeQuery();

            if(rs.next()) {
                studentName = rs.getString(1);
                studentBranch = rs.getString(2);
                studentYear = rs.getInt(3);
                studentSection = rs.getString(4);
            }
        } catch(Exception e){ e.printStackTrace(); }
    }

    // ===== LOAD COURSES (NO EXPIRED) =====
    private void loadCourses() {

        courseBox.removeAllItems();
        courseDetails.clear();

        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement ps = con.prepareStatement(
                "SELECT DISTINCT course_name,credits,deadline FROM courses c " +
                "JOIN branches b ON c.branch_id=b.branch_id " +
                "WHERE b.branch_name=? AND c.year=?"
            );

            ps.setString(1, studentBranch);
            ps.setInt(2, studentYear);

            ResultSet rs = ps.executeQuery();

            java.sql.Date today = new java.sql.Date(System.currentTimeMillis());

            while(rs.next()) {

                String name = rs.getString("course_name");
                int credits = rs.getInt("credits");
                java.sql.Date d = rs.getDate("deadline");

                if(d != null && d.before(today)) continue; // ❌ hide expired

                String display = name + " | " + credits + " Credits | " + d;

                courseBox.addItem(display);
                courseDetails.put(display, name);
            }

        } catch(Exception e){ e.printStackTrace(); }
    }

    // ===== CREDIT CALC =====
    private int getCurrentCredits() {

        int total = 0;

        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement ps = con.prepareStatement(
                "SELECT SUM(c.credits) FROM registrations r " +
                "JOIN courses c ON r.course_id=c.course_id WHERE r.student_id=?"
            );

            ps.setString(1, studentId);
            ResultSet rs = ps.executeQuery();

            if(rs.next()) total = rs.getInt(1);

        } catch(Exception e){ e.printStackTrace(); }

        return total;
    }

    // ===== REGISTER =====
    private void registerCourse() {

        String selected = (String) courseBox.getSelectedItem();
        if(selected == null) return;

        String course = courseDetails.get(selected);

        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement ps1 = con.prepareStatement(
                "SELECT credits, deadline FROM courses WHERE course_name=?"
            );

            ps1.setString(1, course);
            ResultSet rs = ps1.executeQuery();

            if(rs.next()) {

                int credits = rs.getInt("credits");
                java.sql.Date deadline = rs.getDate("deadline");
                java.sql.Date today = new java.sql.Date(System.currentTimeMillis());

                if(deadline != null && deadline.before(today)) {
                    JOptionPane.showMessageDialog(this,"Deadline over!");
                    return;
                }

                if(getCurrentCredits() + credits > 24) {
                    JOptionPane.showMessageDialog(this,"Credit limit exceeded!");
                    return;
                }

                PreparedStatement ps2 = con.prepareStatement(
                    "INSERT INTO registrations(student_id,course_id) VALUES(?,(SELECT MIN(course_id) FROM courses WHERE course_name=?))"
                );

                ps2.setString(1, studentId);
                ps2.setString(2, course);
                ps2.executeUpdate();

                JOptionPane.showMessageDialog(this,"Registered!");

            }

        } catch(Exception e){
            JOptionPane.showMessageDialog(this,"Already registered!");
        }
    }

    private void loadRegisteredCourses() {

        model.setRowCount(0);

        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement ps = con.prepareStatement(
                "SELECT c.course_name,c.credits,c.deadline FROM registrations r " +
                "JOIN courses c ON r.course_id=c.course_id WHERE r.student_id=?"
            );

            ps.setString(1, studentId);
            ResultSet rs = ps.executeQuery();

            int total = 0;

            while(rs.next()) {
                model.addRow(new Object[]{
                    rs.getString(1),
                    rs.getInt(2),
                    rs.getDate(3)
                });
                total += rs.getInt(2);
            }

            totalCreditsLabel.setText("Total Credits: " + total);

        } catch(Exception e){ e.printStackTrace(); }
    }

private void checkDeadlines() {

    try (Connection con = DBConnection.getConnection()) {

        PreparedStatement ps = con.prepareStatement(
            "SELECT DISTINCT course_name, deadline FROM courses c " +
            "JOIN branches b ON c.branch_id=b.branch_id " +
            "WHERE b.branch_name=? AND c.year=?"
        );

        ps.setString(1, studentBranch);
        ps.setInt(2, studentYear);

        ResultSet rs = ps.executeQuery();

        java.sql.Date today = new java.sql.Date(System.currentTimeMillis());

        while(rs.next()) {

            String course = rs.getString("course_name");
            java.sql.Date deadline = rs.getDate("deadline");

            // =========================
            // 🆕 NEW COURSE DETECTION
            // =========================
            if (!seenCourses.contains(course)) {
                String msg = "📢 New course added: " + course;

                notificationModel.addElement(msg);
                shownNotifications.add(msg);
                seenCourses.add(course);
            }

            if(deadline == null) continue;

            long diff = (deadline.getTime() - today.getTime()) / (1000*60*60*24);

            String msg = null;

            if(diff <= 3 && diff >= 0)
                msg = "⏰ Deadline soon: " + course;

            else if(diff < 0)
                msg = "❌ Deadline passed: " + course;

            if(msg != null && !shownNotifications.contains(msg)) {
                notificationModel.addElement(msg);
                shownNotifications.add(msg);
            }
        }

    } catch(Exception e){ e.printStackTrace(); }
}

    private void deleteNotification() {
        int i = notifyList.getSelectedIndex();
        if(i != -1) {
            shownNotifications.remove(notificationModel.get(i));
            notificationModel.remove(i);
        }
    }

    private JButton createBtn(String text) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE,45));
        btn.setBackground(new Color(33,37,41));
        btn.setForeground(Color.WHITE);

        btn.addMouseListener(new java.awt.event.MouseAdapter(){
            public void mouseEntered(java.awt.event.MouseEvent e){
                btn.setBackground(new Color(0,123,255));
            }
            public void mouseExited(java.awt.event.MouseEvent e){
                btn.setBackground(new Color(33,37,41));
            }
        });

        return btn;
    }

  
}