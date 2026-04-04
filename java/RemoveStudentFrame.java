import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class RemoveStudentFrame extends JFrame {

    private JTable table;
    private DefaultTableModel model;
    private String courseName;

    public RemoveStudentFrame(String courseName) {

        this.courseName = courseName;

        setTitle("Remove Students - " + courseName);
        setSize(700, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ===== TITLE =====
        JLabel title = new JLabel("Manage Students - " + courseName, JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        add(title, BorderLayout.NORTH);

        // ===== TABLE =====
        model = new DefaultTableModel(new String[]{"Student ID", "Name"}, 0);
        table = new JTable(model);

        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(52, 152, 219));
        table.getTableHeader().setForeground(Color.WHITE);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // ===== BUTTON =====
        JButton removeBtn = new JButton("Remove Selected Student");
        removeBtn.setBackground(new Color(220, 53, 69));
        removeBtn.setForeground(Color.WHITE);
        removeBtn.setFocusPainted(false);

        add(removeBtn, BorderLayout.SOUTH);

        // LOAD DATA
        loadStudents();

        // ACTION
        removeBtn.addActionListener(e -> removeSelectedStudent());

        setVisible(true);
    }

    // ===== LOAD STUDENTS =====
    private void loadStudents() {

        model.setRowCount(0);

        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement ps = con.prepareStatement(
                "SELECT s.student_id, s.full_name FROM registrations r " +
                "JOIN students s ON r.student_id = s.student_id " +
                "JOIN courses c ON r.course_id = c.course_id " +
                "WHERE c.course_name=?"
            );

            ps.setString(1, courseName);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString(1),
                        rs.getString(2)
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===== REMOVE STUDENT =====
    private void removeSelectedStudent() {

        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a student first!");
            return;
        }

        String studentId = model.getValueAt(row, 0).toString();

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Remove this student?",
                "Confirm",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection con = DBConnection.getConnection()) {

            // Get course_id
            PreparedStatement ps1 = con.prepareStatement(
                    "SELECT course_id FROM courses WHERE course_name=?");
            ps1.setString(1, courseName);

            ResultSet rs = ps1.executeQuery();
            rs.next();
            int courseId = rs.getInt(1);

            // Delete
            PreparedStatement ps2 = con.prepareStatement(
                    "DELETE FROM registrations WHERE student_id=? AND course_id=?");

            ps2.setString(1, studentId);
            ps2.setInt(2, courseId);

            ps2.executeUpdate();

            JOptionPane.showMessageDialog(this, "Student Removed!");

            loadStudents(); // refresh table

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}