import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginFrame extends JFrame {

    private JTextField userField;
    private JPasswordField passField;
    private String role;

    public LoginFrame(String role) {

        this.role = role;

        setTitle(role + " Login");
        setSize(450, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(45, 52, 54));
        mainPanel.setLayout(new BorderLayout());
        add(mainPanel);

        JLabel titleLabel = new JLabel(role + " LOGIN", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 26));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridBagLayout());
        centerPanel.setBackground(new Color(45, 52, 54));
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new GridBagLayout());
        loginPanel.setPreferredSize(new Dimension(300, 180));
        loginPanel.setBackground(Color.WHITE);
        loginPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ID Label changes depending on role
        String idLabel = role.equalsIgnoreCase("STUDENT") ? "Student ID:" : "Username:";

        gbc.gridx = 0;
        gbc.gridy = 0;
        loginPanel.add(new JLabel(idLabel), gbc);

        gbc.gridx = 1;
        userField = new JTextField(15);
        loginPanel.add(userField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        loginPanel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        passField = new JPasswordField(15);
        loginPanel.add(passField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;

        JCheckBox showPass = new JCheckBox("Show Password");
        showPass.setBackground(Color.WHITE);
        loginPanel.add(showPass, gbc);

        showPass.addActionListener(e -> {
            if(showPass.isSelected()){
                passField.setEchoChar((char)0);
            } else {
                passField.setEchoChar('*');
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;

        JButton loginBtn = new JButton("Login");
        loginBtn.setBackground(new Color(9,132,227));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setFont(new Font("Arial", Font.BOLD, 14));

        loginPanel.add(loginBtn, gbc);
        centerPanel.add(loginPanel);

        loginBtn.addActionListener(e -> loginUser());

        setVisible(true);
    }

    private void loginUser() {

        String id = userField.getText().trim();
        String password = String.valueOf(passField.getPassword()).trim();

        if(id.isEmpty() || password.isEmpty()){
            JOptionPane.showMessageDialog(this,"Please enter ID and password");
            return;
        }

        try {

            Connection con = DBConnection.getConnection();

            // STUDENT LOGIN
            if(role.equalsIgnoreCase("STUDENT")){

                PreparedStatement ps = con.prepareStatement(
                        "SELECT student_id FROM students WHERE student_id=? AND password=?");

                ps.setString(1, id);
                ps.setString(2, password);

                ResultSet rs = ps.executeQuery();

                if(rs.next()){

                    String studentId = rs.getString("student_id");

                    JOptionPane.showMessageDialog(this,"Login Successful!");

                    new StudentDashboard(studentId);

                    dispose();

                } else {
                    JOptionPane.showMessageDialog(this,"Invalid Student Login");
                }

                rs.close();
                ps.close();
            }

            // FACULTY LOGIN
            else if(role.equalsIgnoreCase("FACULTY")){

                PreparedStatement ps = con.prepareStatement(
                        "SELECT faculty_id FROM faculty WHERE username=? AND password=?");

                ps.setString(1, id);
                ps.setString(2, password);

                ResultSet rs = ps.executeQuery();

                if(rs.next()){

                    int facultyId = rs.getInt("faculty_id");

                    JOptionPane.showMessageDialog(this,"Login Successful!");

                    new FacultyDashboard(facultyId,id);

                    dispose();

                } else {
                    JOptionPane.showMessageDialog(this,"Invalid Faculty Login");
                }

                rs.close();
                ps.close();
            }

            con.close();

        } catch(Exception e){

            e.printStackTrace();
            JOptionPane.showMessageDialog(this,"Database Error: "+e.getMessage());
        }
    }
}