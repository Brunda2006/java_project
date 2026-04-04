import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class FacultyDashboard extends JFrame {

    JComboBox<String> branchBox, courseBox;
    JComboBox<Integer> yearBox;
    JLabel countLabel;

    Color sidebarColor = new Color(25, 25, 35);
    Color hoverColor = new Color(0, 120, 215);
    Color mainBg = new Color(240, 242, 245);

    public FacultyDashboard(int facultyId, String username) {

        setTitle("Faculty Dashboard");
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ===== SIDEBAR =====
        JPanel sidebar = new JPanel();
        sidebar.setBackground(sidebarColor);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(220, getHeight()));

        sidebar.add(Box.createVerticalStrut(20));

        sidebar.add(createBtn("Add Branch", e -> addBranch()));
        sidebar.add(createBtn("Update Branch", e -> updateBranch()));
        sidebar.add(createBtn("Delete Branch", e -> deleteBranch()));

        sidebar.add(Box.createVerticalStrut(15));

        sidebar.add(createBtn("Add Course", e -> addCourse()));
        sidebar.add(createBtn("Update Course", e -> updateCourse()));
        sidebar.add(createBtn("Delete Course", e -> deleteCourse()));
        sidebar.add(createBtn("Update Deadline", e -> updateDeadline()));

        sidebar.add(Box.createVerticalStrut(15));

        sidebar.add(createBtn("Load Courses", e -> loadCourses()));
        sidebar.add(createBtn("Check Count", e -> checkCount()));

        sidebar.add(Box.createVerticalStrut(15));

        sidebar.add(createBtn("View Students", e -> viewStudents()));

        sidebar.add(Box.createVerticalGlue());

        sidebar.add(createBtn("Logout", e -> {
            dispose();
            new RoleSelectionFrame();
        }));

        add(sidebar, BorderLayout.WEST);

        // ===== MAIN PANEL =====
        JPanel main = new JPanel(new GridBagLayout());
        main.setBackground(mainBg);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);

        JLabel title = new JLabel("Faculty Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));

        branchBox = new JComboBox<>();
        yearBox = new JComboBox<>(new Integer[]{1, 2, 3, 4});
        courseBox = new JComboBox<>();

        styleCombo(branchBox);
        styleCombo(yearBox);
        styleCombo(courseBox);

        countLabel = new JLabel("Students: 0");
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        main.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        main.add(new JLabel("Branch:"), gbc);
        gbc.gridx = 1;
        main.add(branchBox, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        main.add(new JLabel("Year:"), gbc);
        gbc.gridx = 1;
        main.add(yearBox, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        main.add(new JLabel("Course:"), gbc);
        gbc.gridx = 1;
        main.add(courseBox, gbc);

        gbc.gridx = 1; gbc.gridy = 4;
        main.add(countLabel, gbc);

        add(main, BorderLayout.CENTER);

        branchBox.addActionListener(e -> reset());
        yearBox.addActionListener(e -> reset());

        loadBranches();
        setVisible(true);
    }

    // ===== BUTTON STYLE =====
    private JButton createBtn(String text, java.awt.event.ActionListener e) {
        JButton b = new JButton(text);
        b.setMaximumSize(new Dimension(200, 45));
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setBackground(sidebarColor);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addActionListener(e);

        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                b.setBackground(hoverColor);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                b.setBackground(sidebarColor);
            }
        });

        return b;
    }

    private void styleCombo(JComboBox<?> c) {
        c.setPreferredSize(new Dimension(200, 30));
    }

    private void reset() {
        courseBox.removeAllItems();
        countLabel.setText("Students: 0");
    }

    // ===== LOAD BRANCHES =====
    private void loadBranches() {
        branchBox.removeAllItems();
        try (Connection con = DBConnection.getConnection()) {
            ResultSet rs = con.createStatement()
                    .executeQuery("SELECT branch_name FROM branches");
            while (rs.next()) branchBox.addItem(rs.getString(1));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    // ===== LOAD COURSES =====
    private void loadCourses() {
        if (branchBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Select branch!");
            return;
        }

        courseBox.removeAllItems();

        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement ps = con.prepareStatement(
                    "SELECT course_name FROM courses WHERE branch_id=(" +
                    "SELECT branch_id FROM branches WHERE branch_name=?) AND year=?"
            );

            ps.setString(1, branchBox.getSelectedItem().toString());
            ps.setInt(2, (Integer) yearBox.getSelectedItem());

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                courseBox.addItem(rs.getString(1));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    // ===== FIXED COUNT (NO ERROR) =====
    private void checkCount() {
        if (courseBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Select course!");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement ps = con.prepareStatement(
                    "SELECT COUNT(*) FROM registrations r " +
                    "JOIN courses c ON r.course_id=c.course_id " +
                    "WHERE c.course_name=?"
            );

            ps.setString(1, courseBox.getSelectedItem().toString());

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                countLabel.setText("Students: " + rs.getInt(1));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    // ===== BRANCH CRUD =====
    private void addBranch() {
        String name = JOptionPane.showInputDialog("Branch Name:");
        if (name == null || name.trim().isEmpty()) return;

        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO branches VALUES((SELECT NVL(MAX(branch_id),0)+1 FROM branches),?)"
            );

            ps.setString(1, name);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Branch Added!");
            loadBranches();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void updateBranch() {
        if (branchBox.getSelectedItem() == null) return;

        String old = branchBox.getSelectedItem().toString();
        String newName = JOptionPane.showInputDialog("New Name:", old);

        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement ps = con.prepareStatement(
                    "UPDATE branches SET branch_name=? WHERE branch_name=?"
            );

            ps.setString(1, newName);
            ps.setString(2, old);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Updated!");
            loadBranches();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void deleteBranch() {
        if (branchBox.getSelectedItem() == null) return;

        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM branches WHERE branch_name=?"
            );

            ps.setString(1, branchBox.getSelectedItem().toString());
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Deleted!");
            loadBranches();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    // ===== COURSE CRUD =====
    private void addCourse() {
        try (Connection con = DBConnection.getConnection()) {

            String name = JOptionPane.showInputDialog("Course Name:");
            int credits = Integer.parseInt(JOptionPane.showInputDialog("Credits:"));
            int year = (Integer) yearBox.getSelectedItem();
            java.sql.Date deadline = java.sql.Date.valueOf(
                    JOptionPane.showInputDialog("YYYY-MM-DD")
            );

            PreparedStatement ps1 = con.prepareStatement(
                    "SELECT branch_id FROM branches WHERE branch_name=?"
            );
            ps1.setString(1, branchBox.getSelectedItem().toString());
            ResultSet rs = ps1.executeQuery();

            if (!rs.next()) return;

            int branchId = rs.getInt(1);

            PreparedStatement ps2 = con.prepareStatement(
                    "INSERT INTO courses VALUES((SELECT NVL(MAX(course_id),0)+1 FROM courses),?,?,?,?,?)"
            );

            ps2.setString(1, name);
            ps2.setInt(2, branchId);
            ps2.setInt(3, credits);
            ps2.setInt(4, year);
            ps2.setDate(5, deadline);

            ps2.executeUpdate();

            JOptionPane.showMessageDialog(this, "Course Added!");
            loadCourses();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void updateCourse() {
        if (courseBox.getSelectedItem() == null) return;

        String old = courseBox.getSelectedItem().toString();
        String newName = JOptionPane.showInputDialog("New Name:", old);

        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement ps = con.prepareStatement(
                    "UPDATE courses SET course_name=? WHERE course_name=?"
            );

            ps.setString(1, newName);
            ps.setString(2, old);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Updated!");
            loadCourses();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void deleteCourse() {
        if (courseBox.getSelectedItem() == null) return;

        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM courses WHERE course_name=?"
            );

            ps.setString(1, courseBox.getSelectedItem().toString());
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Deleted!");
            loadCourses();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    // ===== UPDATE DEADLINE =====
  private void updateDeadline() {

    if (courseBox.getSelectedItem() == null) {
        JOptionPane.showMessageDialog(this, "Please select a course first!");
        return;
    }

    try (Connection con = DBConnection.getConnection()) {

        String course = courseBox.getSelectedItem().toString();

        // Ask date
        String input = JOptionPane.showInputDialog(
                this,
                "Enter New Deadline (YYYY-MM-DD):"
        );

        // If user pressed cancel
        if (input == null) return;

        if (input.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Date cannot be empty!");
            return;
        }

        java.sql.Date newDate;

        try {
            newDate = java.sql.Date.valueOf(input);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid date format! Use YYYY-MM-DD");
            return;
        }

        PreparedStatement ps = con.prepareStatement(
                "UPDATE courses SET deadline=? WHERE course_name=?"
        );

        ps.setDate(1, newDate);
        ps.setString(2, course);

        int rows = ps.executeUpdate();

        if (rows > 0) {
            JOptionPane.showMessageDialog(this, "✅ Deadline updated successfully!");
        } else {
            JOptionPane.showMessageDialog(this, "⚠ Course not found!");
        }

    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error updating deadline!");
    }
}

    // ===== VIEW STUDENTS =====
    private void viewStudents() {
        if (courseBox.getSelectedItem() == null) return;
        new ViewStudentsFrame(courseBox.getSelectedItem().toString());
    }
}