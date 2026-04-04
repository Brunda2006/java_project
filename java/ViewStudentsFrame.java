import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class ViewStudentsFrame extends JFrame {

    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;
    private JComboBox<Integer> yearBox;
    private JComboBox<String> sectionBox;
    private JLabel countLabel;

    private String courseName;

    public ViewStudentsFrame(String courseName) {

        this.courseName = courseName;

        setTitle("Manage Students - " + courseName);
        setSize(1050, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ===== TOP PANEL =====
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,10,10));

        searchField = new JTextField(10);
        yearBox = new JComboBox<>(new Integer[]{1,2,3,4});
        sectionBox = new JComboBox<>(new String[]{"A","B","C","D"});

        JButton searchBtn = new JButton("🔍 Search");
        JButton filterBtn = new JButton("🎯 Filter");
        JButton refreshBtn = new JButton("🔄 Refresh");
        JButton addBtn = new JButton("➕ Add");
        JButton removeBtn = new JButton("❌ Remove");

        countLabel = new JLabel("Students: 0");

        // Add components in proper order
        topPanel.add(new JLabel("Roll No:"));
        topPanel.add(searchField);

        topPanel.add(new JLabel("Year:"));
        topPanel.add(yearBox);

        topPanel.add(new JLabel("Section:"));
        topPanel.add(sectionBox);

        topPanel.add(searchBtn);
        topPanel.add(filterBtn);
        topPanel.add(refreshBtn);
        topPanel.add(addBtn);
        topPanel.add(removeBtn);
        topPanel.add(countLabel);

        add(topPanel, BorderLayout.NORTH);

        // ===== TABLE =====
        model = new DefaultTableModel(
                new String[]{"ID", "Name", "Year", "Section", "Email"}, 0
        );

        table = new JTable(model);
        styleTable(table);

        add(new JScrollPane(table), BorderLayout.CENTER);

        loadStudents();

        // ===== ACTIONS =====
        searchBtn.addActionListener(e -> searchByRoll());
        filterBtn.addActionListener(e -> filterStudents());

        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            loadStudents();
        });

        addBtn.addActionListener(e -> addStudent());
        removeBtn.addActionListener(e -> removeStudent());

        setVisible(true);
    }

    // ===== STYLE =====
    private void styleTable(JTable table) {

        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(0,120,215));
        table.setSelectionForeground(Color.WHITE);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 15));
        header.setBackground(new Color(30,30,45));
        header.setForeground(Color.WHITE);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
            public Component getTableCellRendererComponent(JTable t,Object v,
                    boolean isSel, boolean f,int r,int c){

                Component comp = super.getTableCellRendererComponent(t,v,isSel,f,r,c);

                if(!isSel){
                    comp.setBackground(r%2==0?Color.WHITE:new Color(245,245,245));
                }

                return comp;
            }
        });
    }

    // ===== GET COURSE ID =====
    private int getCourseId(Connection con) throws Exception {

        PreparedStatement ps = con.prepareStatement(
                "SELECT course_id FROM courses WHERE course_name=?"
        );

        ps.setString(1, courseName);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) return rs.getInt(1);
        else throw new Exception("Course not found!");
    }

    // ===== LOAD =====
    private void loadStudents() {

        model.setRowCount(0);

        try (Connection con = DBConnection.getConnection()) {

            int courseId = getCourseId(con);

            PreparedStatement ps = con.prepareStatement(
                    "SELECT s.student_id, s.full_name, s.year, s.section, s.email " +
                    "FROM registrations r JOIN students s ON r.student_id=s.student_id " +
                    "WHERE r.course_id=? ORDER BY s.student_id"
            );

            ps.setInt(1, courseId);

            ResultSet rs = ps.executeQuery();

            int count = 0;

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString(1),
                        rs.getString(2),
                        rs.getInt(3),
                        rs.getString(4),
                        rs.getString(5)
                });
                count++;
            }

            countLabel.setText("Students: " + count);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    // ===== SEARCH =====
    private void searchByRoll() {

        String roll = searchField.getText().trim();

        if (roll.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter Roll Number!");
            return;
        }

        model.setRowCount(0);

        try (Connection con = DBConnection.getConnection()) {

            int courseId = getCourseId(con);

            PreparedStatement ps = con.prepareStatement(
                    "SELECT s.student_id, s.full_name, s.year, s.section, s.email " +
                    "FROM registrations r JOIN students s ON r.student_id=s.student_id " +
                    "WHERE r.course_id=? AND s.student_id=?"
            );

            ps.setInt(1, courseId);
            ps.setString(2, roll);

            ResultSet rs = ps.executeQuery();

            int count = 0;

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString(1),
                        rs.getString(2),
                        rs.getInt(3),
                        rs.getString(4),
                        rs.getString(5)
                });
                count++;
            }

            countLabel.setText("Students: " + count);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    // ===== FILTER =====
    private void filterStudents() {

        model.setRowCount(0);

        try (Connection con = DBConnection.getConnection()) {

            int courseId = getCourseId(con);

            PreparedStatement ps = con.prepareStatement(
                    "SELECT s.student_id, s.full_name, s.year, s.section, s.email " +
                    "FROM registrations r JOIN students s ON r.student_id=s.student_id " +
                    "WHERE r.course_id=? AND s.year=? AND s.section=?"
            );

            ps.setInt(1, courseId);
            ps.setInt(2, (Integer)yearBox.getSelectedItem());
            ps.setString(3, sectionBox.getSelectedItem().toString());

            ResultSet rs = ps.executeQuery();

            int count = 0;

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString(1),
                        rs.getString(2),
                        rs.getInt(3),
                        rs.getString(4),
                        rs.getString(5)
                });
                count++;
            }

            countLabel.setText("Students: " + count);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    // ===== ADD =====
    private void addStudent() {

        String id = JOptionPane.showInputDialog(this, "Enter Student ID:");

        if (id == null || id.trim().isEmpty()) return;

        try (Connection con = DBConnection.getConnection()) {

            int courseId = getCourseId(con);

            // Check duplicate
            PreparedStatement check = con.prepareStatement(
                    "SELECT * FROM registrations WHERE student_id=? AND course_id=?"
            );
            check.setString(1, id);
            check.setInt(2, courseId);

            if (check.executeQuery().next()) {
                JOptionPane.showMessageDialog(this, "Already Registered!");
                return;
            }

            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO registrations(student_id, course_id) VALUES(?,?)"
            );

            ps.setString(1, id);
            ps.setInt(2, courseId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Student Added!");
            loadStudents();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid Student ID!");
        }
    }

    // ===== REMOVE =====
    private void removeStudent() {

        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a student!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this, "Remove student?", "Confirm", JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        String id = model.getValueAt(row, 0).toString();

        try (Connection con = DBConnection.getConnection()) {

            int courseId = getCourseId(con);

            PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM registrations WHERE student_id=? AND course_id=?"
            );

            ps.setString(1, id);
            ps.setInt(2, courseId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Student Removed!");
            loadStudents();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}