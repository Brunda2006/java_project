import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class StudentDashboard extends JFrame {

    private JComboBox<BranchItem> branchBox;
    private JComboBox<String> courseBox;
    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;
    private JLabel totalCreditsLabel;
    private int studentId;

    // ================= BRANCH OBJECT =================
    class BranchItem {
        int id;
        String name;

        BranchItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public String toString() {
            return name;
        }
    }

    // ================= CONSTRUCTOR =================
    public StudentDashboard(int studentId) {

        this.studentId = studentId;

        setTitle("Student Dashboard");
        setSize(950, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Modern Font
        UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 14));
        UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("ComboBox.font", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("TextField.font", new Font("Segoe UI", Font.PLAIN, 14));

        // ================= TOP PANEL =================
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);

        branchBox = new JComboBox<>();
        courseBox = new JComboBox<>();
        searchField = new JTextField(15);

        JButton registerBtn = new JButton("Register");
        JButton logoutBtn = new JButton("Logout");

        logoutBtn.setBackground(new Color(220,53,69));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFocusPainted(false);

        // Row 1
        gbc.gridx = 0; gbc.gridy = 0;
        topPanel.add(new JLabel("Select Branch:"), gbc);

        gbc.gridx = 1;
        topPanel.add(branchBox, gbc);

        gbc.gridx = 2;
        topPanel.add(logoutBtn, gbc);

        // Row 2
        gbc.gridx = 0; gbc.gridy = 1;
        topPanel.add(new JLabel("Select Course:"), gbc);

        gbc.gridx = 1;
        topPanel.add(courseBox, gbc);

        gbc.gridx = 2;
        topPanel.add(registerBtn, gbc);

        // Row 3
        gbc.gridx = 0; gbc.gridy = 2;
        topPanel.add(new JLabel("Search:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        topPanel.add(searchField, gbc);

        add(topPanel, BorderLayout.NORTH);

        // ================= TABLE =================
        model = new DefaultTableModel();
        model.setColumnIdentifiers(new String[]{
                "Reg ID", "Course", "Branch", "Credits"
        });

        table = new JTable(model);
        table.setRowHeight(25);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // ================= BOTTOM PANEL =================
        JPanel bottomPanel = new JPanel(new BorderLayout());

        JButton unregisterBtn = new JButton("Unregister Selected Course");

        totalCreditsLabel = new JLabel("Total Credits: 0");
        totalCreditsLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

        bottomPanel.add(unregisterBtn, BorderLayout.WEST);
        bottomPanel.add(totalCreditsLabel, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);

        // ================= LOAD DATA =================
        loadBranches();
        loadRegisteredCourses();

        // ================= EVENTS =================
        branchBox.addActionListener(e -> loadCourses());
        registerBtn.addActionListener(e -> registerCourse(registerBtn));
        unregisterBtn.addActionListener(e -> unregisterCourse());
        logoutBtn.addActionListener(e -> logout());

        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                filterCourses();
            }
        });

        setVisible(true);
    }

    // ================= LOAD BRANCHES =================
    private void loadBranches() {

        branchBox.removeAllItems();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT branch_id, branch_name FROM branches ORDER BY branch_name");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                branchBox.addItem(new BranchItem(
                        rs.getInt("branch_id"),
                        rs.getString("branch_name")));
            }

            if (branchBox.getItemCount() > 0) {
                branchBox.setSelectedIndex(0);
                loadCourses();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= LOAD COURSES =================
    private void loadCourses() {

        courseBox.removeAllItems();

        BranchItem selected = (BranchItem) branchBox.getSelectedItem();
        if (selected == null) return;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT course_name FROM courses WHERE branch_id = ? ORDER BY course_name")) {

            ps.setInt(1, selected.id);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                courseBox.addItem(rs.getString("course_name"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= SEARCH FILTER =================
    private void filterCourses() {

        String text = searchField.getText().toLowerCase();

        for (int i = 0; i < courseBox.getItemCount(); i++) {
            String item = courseBox.getItemAt(i).toLowerCase();
            if (item.contains(text)) {
                courseBox.setSelectedIndex(i);
                break;
            }
        }
    }

    // ================= REGISTER =================
    private void registerCourse(JButton btn) {

        btn.setEnabled(false);

        Object selectedCourse = courseBox.getSelectedItem();
        if (selectedCourse == null) {
            JOptionPane.showMessageDialog(this, "Select Course");
            btn.setEnabled(true);
            return;
        }

        String courseName = selectedCourse.toString();

        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement ps1 = con.prepareStatement(
                    "SELECT course_id FROM courses WHERE course_name = ?");
            ps1.setString(1, courseName);
            ResultSet rs = ps1.executeQuery();

            if (rs.next()) {

                int courseId = rs.getInt("course_id");

                PreparedStatement insert = con.prepareStatement(
                        "INSERT INTO registrations(student_id, course_id) VALUES (?, ?)");
                insert.setInt(1, studentId);
                insert.setInt(2, courseId);
                insert.executeUpdate();

                JOptionPane.showMessageDialog(this, "Registered Successfully!");
                loadRegisteredCourses();
            }

        } catch (SQLException ex) {

            if (ex.getErrorCode() == 1) {
                JOptionPane.showMessageDialog(this,
                        "You already registered this course!",
                        "Duplicate Registration",
                        JOptionPane.WARNING_MESSAGE);
            } else {
                ex.printStackTrace();
            }
        }

        btn.setEnabled(true);
    }

    // ================= LOAD REGISTERED COURSES =================
    private void loadRegisteredCourses() {

        model.setRowCount(0);
        int totalCredits = 0;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT r.reg_id, c.course_name, b.branch_name, c.credits " +
                             "FROM registrations r " +
                             "JOIN courses c ON r.course_id = c.course_id " +
                             "JOIN branches b ON c.branch_id = b.branch_id " +
                             "WHERE r.student_id = ?")) {

            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                int credits = rs.getInt("credits");
                totalCredits += credits;

                model.addRow(new Object[]{
                        rs.getInt("reg_id"),
                        rs.getString("course_name"),
                        rs.getString("branch_name"),
                        credits
                });
            }

            totalCreditsLabel.setText("Total Credits: " + totalCredits);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= UNREGISTER =================
    private void unregisterCourse() {

        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a course");
            return;
        }

        int regId = (int) model.getValueAt(selectedRow, 0);

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "DELETE FROM registrations WHERE reg_id=?")) {

            ps.setInt(1, regId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Unregistered Successfully!");
            loadRegisteredCourses();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= LOGOUT =================
    private void logout() {

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Logout Confirmation",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose();
            new RoleSelectionFrame();
        }
    }
}