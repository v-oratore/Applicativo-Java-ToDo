package gui;

import controller.Controller; // Import Controller

import javax.swing.*;
import java.awt.*;

public class RegistrationPanel extends JPanel {
    protected JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton registerButton;
    private JButton backToLoginButton;
    private JLabel messageLabel;
    private MainFrame mainFrame;
    private Controller controller; // Riferimento al controller principale

    public RegistrationPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.controller = mainFrame.getController(); // Ottiene il controller

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Registrati:", SwingConstants.CENTER);
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

        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, usernameField.getPreferredSize().height + 8));
        fieldsPanel.add(usernameField);
        fieldsPanel.add(Box.createVerticalStrut(10));

        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        fieldsPanel.add(passLabel);

        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, passwordField.getPreferredSize().height + 8));
        fieldsPanel.add(passwordField);
        fieldsPanel.add(Box.createVerticalStrut(10));

        JLabel confirmLabel = new JLabel("Conferma Password:");
        confirmLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        fieldsPanel.add(confirmLabel);

        confirmPasswordField = new JPasswordField(20);
        confirmPasswordField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        confirmPasswordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, confirmPasswordField.getPreferredSize().height + 8));
        fieldsPanel.add(confirmPasswordField);
        // fieldsPanel.add(Box.createVerticalStrut(10)); // Spostato messageLabel in alto
        // fieldsPanel.add(messageLabel);

        add(fieldsPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        buttonPanel.setBackground(Color.WHITE);

        backToLoginButton = createStyledButton("Accedi");
        registerButton = createStyledButton("Registrati");

        buttonPanel.add(backToLoginButton);
        buttonPanel.add(registerButton);

        // Imposta bottone di default
        SwingUtilities.invokeLater(() -> {
            JRootPane rootPane = SwingUtilities.getRootPane(registerButton);
            if (rootPane != null) {
                rootPane.setDefaultButton(registerButton);
            }
        });

        add(buttonPanel, BorderLayout.SOUTH);

        registerButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                messageLabel.setForeground(Color.RED);
                messageLabel.setText("Tutti i campi sono obbligatori.");
                return;
            }

            if (!password.equals(confirmPassword)) {
                messageLabel.setForeground(Color.RED);
                messageLabel.setText("Le password non corrispondono.");
                return;
            }

            if (controller.registraUtente(username, password)) { // Usa il controller
                messageLabel.setForeground(new Color(0, 128, 0));
                // messageLabel.setText("Registrazione avvenuta con successo!"); // Mostrato da JOptionPane
                JOptionPane.showMessageDialog(this, "Registrazione avvenuta con successo! Puoi ora effettuare il login.");
                mainFrame.showLogin();
                clearFields();
            } else {
                messageLabel.setForeground(Color.RED);
                // Il controller ora stampa i dettagli dell'errore sulla console,
                // qui possiamo dare un messaggio generico o basarci su un eventuale codice di errore.
                messageLabel.setText("Registrazione fallita (username già esistente o dati non validi).");
            }
        });

        backToLoginButton.addActionListener(e -> {
            clearFields();
            mainFrame.showLogin();
        });
    }

    @Override
    public void addNotify() {
        super.addNotify();
        JRootPane rootPane = SwingUtilities.getRootPane(this);
        if (rootPane != null) {
            rootPane.setDefaultButton(registerButton);
            if (usernameField != null) { // Assicura che il campo sia inizializzato
                usernameField.requestFocusInWindow();
            }
        }
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(150, 40));
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setFocusPainted(false);
        button.setBackground(new Color(230, 240, 255));
        button.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        button.setOpaque(true);
        return button;
    }

    public void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
        messageLabel.setText("");
    }
}