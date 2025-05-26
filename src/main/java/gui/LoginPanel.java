package gui;

import controller.Controller;
import model.Utente;

import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {
    protected JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JLabel messageLabel;
    private Controller controller; // Riferimento al controller principale
    private MainFrame mainFrame;

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.controller = mainFrame.getController(); // Ottiene il controller da MainFrame

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Accedi:", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0,0,15,0));
        // add(titleLabel, BorderLayout.NORTH); // Spostato per fare spazio al messageLabel globale

        JPanel topMessagePanel = new JPanel(new BorderLayout());
        topMessagePanel.setOpaque(false);
        topMessagePanel.add(titleLabel, BorderLayout.NORTH);
        messageLabel = new JLabel("", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        topMessagePanel.add(messageLabel, BorderLayout.SOUTH); // Message label sotto il titolo
        add(topMessagePanel, BorderLayout.NORTH);


        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        fieldsPanel.setBackground(Color.WHITE);
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        fieldsPanel.add(userLabel);

        usernameField = new JTextField(20); // Dimensione preferita
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, usernameField.getPreferredSize().height + 8));
        fieldsPanel.add(usernameField);
        fieldsPanel.add(Box.createVerticalStrut(10));

        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        fieldsPanel.add(passLabel);

        passwordField = new JPasswordField(20); // Dimensione preferita
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, passwordField.getPreferredSize().height + 8));
        fieldsPanel.add(passwordField);
        // fieldsPanel.add(Box.createVerticalStrut(10)); // Rimosso per avvicinare i bottoni
        // fieldsPanel.add(messageLabel); // Spostato in alto

        passwordField.addActionListener(e -> loginButton.doClick());
        usernameField.addActionListener(e -> loginButton.doClick());

        add(fieldsPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        buttonPanel.setBackground(Color.WHITE);

        loginButton = new JButton("Accedi");
        styleButton(loginButton);

        registerButton = new JButton("Registrati");
        styleButton(registerButton);

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        add(buttonPanel, BorderLayout.SOUTH);

        loginButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                messageLabel.setForeground(Color.RED);
                messageLabel.setText("Username e password sono obbligatori.");
                return;
            }

            if (controller.login(username, password)) { // Usa il nuovo metodo login del controller
                // mainFrame.setUtenteLoggato(controller.getUtenteCorrente()); // Controller gestisce utenteCorrente
                messageLabel.setForeground(new Color(0, 128, 0));
                messageLabel.setText("Accesso effettuato con successo!");
                // clearFields(); // Non pulire subito, l'utente vede il messaggio poi cambia schermata
                Timer timer = new Timer(1000, event -> mainFrame.showDashboard()); // Delay per mostrare messaggio
                timer.setRepeats(false);
                timer.start();
            } else {
                messageLabel.setForeground(Color.RED);
                messageLabel.setText("Credenziali errate o utente non trovato.");
            }
        });

        registerButton.addActionListener(e -> {
            clearFields();
            mainFrame.showRegistration();
        });
    }

    @Override
    public void addNotify() {
        super.addNotify();
        JRootPane rootPane = SwingUtilities.getRootPane(this);
        if (rootPane != null) {
            rootPane.setDefaultButton(loginButton);
            usernameField.requestFocusInWindow();
        }
    }

    public void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
        messageLabel.setText(""); // Pulisce anche il messaggio
    }

    private void styleButton(JButton button) {
        button.setPreferredSize(new Dimension(150, 40));
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setFocusPainted(false);
        button.setBackground(new Color(230, 240, 255));
        button.setBorder(BorderFactory.createLineBorder(new Color(180, 200, 220)));
        button.setOpaque(true);
    }
}