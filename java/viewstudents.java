import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ViewStudentsFrame extends JFrame {

    private JTable studentTable;
    private String courseName;

    public ViewStudentsFrame(String courseName) {

        this.courseName = courseName;

        setTitle("Registered Students - " + courseName);
        setSize(600, 400);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        studentTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(studentTable);

        add(scrollPane, BorderLayout.CENTER);

        loadStudents();

        setVisible(true);
    }

    private void loadStudents() {

        try (Connection con = DBConnection.getConnection()) {

            String query =
                    "SELECT s.student_id, s.student_name " +
                    "FROM students s " +
                    "JOIN registrations r ON s.student_id = r.student_id " +
                    "JOIN courses c ON r.course_id = c.course_id " +
                    "WHERE c.course_name=? " +
                    "ORDER BY s.student_name";

            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, courseName);

            ResultSet rs = ps.executeQuery();

            String[] columns = {"Student ID", "Student Name"};
            DefaultTableModel model = new DefaultTableModel(columns, 0);

            while (rs.next()) {
                Object[] row = {
                        rs.getInt("student_id"),
                        rs.getString("student_name")
                };
                model.addRow(row);
            }

            studentTable.setModel(model);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading students!");
            e.printStackTrace();
        }
    }
}