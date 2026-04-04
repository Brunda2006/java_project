import javax.swing.*;
import java.awt.*;

public class RoleSelectionFrame extends JFrame {

    public RoleSelectionFrame() {

        setTitle("Course Registration System");
        setSize(600, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // ===== MAIN PANEL =====
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(45, 52, 54));
        add(mainPanel);

        // ===== TITLE =====
        JLabel title = new JLabel("COURSE REGISTRATION SYSTEM", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(30, 10, 20, 10));
        mainPanel.add(title, BorderLayout.NORTH);

        // ===== CENTER =====
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(new Color(45, 52, 54));
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        JPanel cardContainer = new JPanel();
        cardContainer.setBackground(new Color(45, 52, 54));
        cardContainer.setLayout(new GridLayout(1, 2, 25, 0));

        // ===== CARDS =====
        JPanel studentCard = createCard("Student", "🎓", new Color(9, 132, 227));
        JPanel facultyCard = createCard("Faculty", "👨‍🏫", new Color(0, 184, 148));

        cardContainer.add(studentCard);
        cardContainer.add(facultyCard);

        centerPanel.add(cardContainer);

        // ===== ACTIONS =====
        addCardAction(studentCard, "STUDENT");
        addCardAction(facultyCard, "FACULTY");

        setVisible(true);
    }

    // ===== CREATE CARD =====
    private JPanel createCard(String text, String icon, Color color) {

        JPanel card = new JPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(180, 180));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 50));

        JLabel textLabel = new JLabel(text, SwingConstants.CENTER);
        textLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));

        card.add(iconLabel, BorderLayout.CENTER);
        card.add(textLabel, BorderLayout.SOUTH);

        // ===== HOVER EFFECT =====
        card.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseEntered(java.awt.event.MouseEvent evt) {
                card.setBackground(new Color(240, 248, 255));
                card.setBorder(BorderFactory.createLineBorder(color, 2));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                card.setBackground(Color.WHITE);
                card.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
            }

            public void mousePressed(java.awt.event.MouseEvent evt) {
                card.setBackground(new Color(220, 235, 255)); // click feel
            }
        });

        return card;
    }

    // ===== CARD ACTION =====
    private void addCardAction(JPanel card, String role) {

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {

                // Smooth transition effect
                new Thread(() -> {
                    try { Thread.sleep(120); } catch (Exception ignored) {}

                    SwingUtilities.invokeLater(() -> {
                        new LoginFrame(role);
                        dispose();
                    });
                }).start();
            }
        });
    }

    public static void main(String[] args) {
        new RoleSelectionFrame();
    }
}