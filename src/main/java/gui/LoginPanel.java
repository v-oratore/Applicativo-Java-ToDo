// File: src/main/java/gui/LoginPanel.java
package gui;

import controller.Controller;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * Pannello Swing per la gestione della schermata di login dell'applicazione Task.
 * Permette l'inserimento delle credenziali e l'accesso all'applicazione.
 */
public class LoginPanel extends JPanel {
    protected JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel messageLabel;

    private final transient Controller controller;

    private static final String USERNAME_PLACEHOLDER = "Username";
    private static final String PASSWORD_PLACEHOLDER = "Password";

    /**
     * Costruttore. Inizializza il pannello di login e i suoi componenti.
     * @param mainFrame Frame principale dell'applicazione
     */
    public LoginPanel(MainFrame mainFrame) {
        this.controller = mainFrame.getController();
        initComponents(mainFrame);
    }

    /**
     * Inizializza e configura tutti i componenti grafici del pannello di login.
     * @param mainFrame Frame principale dell'applicazione
     */
    private void initComponents(MainFrame mainFrame) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JToolBar navToolbar = new JToolBar();
        navToolbar.setFloatable(false);
        navToolbar.setRollover(true);
        navToolbar.setBackground(Color.BLACK);
        navToolbar.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        JLabel logoTextLabel = new JLabel("ToDoApp");
        logoTextLabel.setFont(FontManager.getBold(22f));
        logoTextLabel.setForeground(Color.WHITE);

        navToolbar.add(logoTextLabel);
        navToolbar.add(Box.createHorizontalGlue());

        JButton registratiButton = new JButton("Registrati");
        registratiButton.setFont(FontManager.getRegular(14f));
        registratiButton.setForeground(Color.WHITE);
        registratiButton.setBackground(Color.BLACK);
        registratiButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        registratiButton.setFocusPainted(false);
        registratiButton.setOpaque(true);
        registratiButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        registratiButton.setPreferredSize(new Dimension(
                (int) registratiButton.getPreferredSize().getWidth() + 20,
                40
        ));
        registratiButton.setMinimumSize(registratiButton.getPreferredSize());
        registratiButton.setMaximumSize(registratiButton.getPreferredSize());

        registratiButton.addActionListener(_ -> mainFrame.showRegistrationPanel());

        navToolbar.add(registratiButton);

        add(navToolbar, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(50, 250, 50, 250));
        centerPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Accedi", SwingConstants.CENTER);
        titleLabel.setFont(FontManager.getRegular(24f));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        centerPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        fieldsPanel.setBackground(Color.WHITE);
        fieldsPanel.setMaximumSize(new Dimension(350, Integer.MAX_VALUE));
        fieldsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        Border inputBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        );

        usernameField = new JTextField(USERNAME_PLACEHOLDER, 20);
        usernameField.setFont(FontManager.getRegular(15f));
        usernameField.setForeground(Color.GRAY);
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, usernameField.getPreferredSize().height + 10));
        usernameField.setBorder(inputBorder);
        usernameField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (usernameField.getText().equals(USERNAME_PLACEHOLDER)) {
                    usernameField.setText("");
                    usernameField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (usernameField.getText().isEmpty()) {
                    usernameField.setText(USERNAME_PLACEHOLDER);
                    usernameField.setForeground(Color.GRAY);
                }
            }
        });
        fieldsPanel.add(usernameField);
        fieldsPanel.add(Box.createVerticalStrut(15));

        passwordField = new JPasswordField(PASSWORD_PLACEHOLDER, 20);
        passwordField.setFont(FontManager.getRegular(15f));
        passwordField.setForeground(Color.GRAY);
        passwordField.setEchoChar((char)0);
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, passwordField.getPreferredSize().height + 10));
        passwordField.setBorder(inputBorder);
        passwordField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (new String(passwordField.getPassword()).equals(PASSWORD_PLACEHOLDER)) {
                    passwordField.setText("");
                    passwordField.setEchoChar('•');
                    passwordField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (new String(passwordField.getPassword()).isEmpty()) {
                    passwordField.setText(PASSWORD_PLACEHOLDER);
                    passwordField.setEchoChar((char)0);
                    passwordField.setForeground(Color.GRAY);
                }
            }
        });
        fieldsPanel.add(passwordField);
        fieldsPanel.add(Box.createVerticalStrut(15));

        messageLabel = new JLabel(" ");
        messageLabel.setFont(FontManager.getRegular(14f));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        fieldsPanel.add(messageLabel);

        centerPanel.add(fieldsPanel, BorderLayout.CENTER);

        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
        southPanel.setOpaque(false);

        loginButton = new JButton("Entra");
        loginButton.setFont(FontManager.getRegular(15f));
        loginButton.setBackground(Color.BLACK);
        loginButton.setForeground(Color.WHITE);
        loginButton.setOpaque(true);
        loginButton.setFocusPainted(false);
        loginButton.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(200, 40));
        loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                loginButton.setBackground(Color.DARK_GRAY);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                loginButton.setBackground(Color.BLACK);
            }
        });

        southPanel.add(Box.createVerticalStrut(10));
        southPanel.add(loginButton);

        centerPanel.add(southPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);

        usernameField.addActionListener(_ -> {
            if (usernameField.getText().equals(USERNAME_PLACEHOLDER) || usernameField.getText().isEmpty()) {
                passwordField.requestFocusInWindow();
            } else {
                loginButton.doClick();
            }
        });
        passwordField.addActionListener(_ -> loginButton.doClick());

        loginButton.addActionListener(_ -> handleLogin(mainFrame));
    }

    /**
     * Gestisce la logica di autenticazione dell'utente.
     * Mostra messaggi di errore o successo e reindirizza alla dashboard se il login ha successo.
     * @param mainFrame Frame principale dell'applicazione
     */
    private void handleLogin(MainFrame mainFrame) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (username.equals(USERNAME_PLACEHOLDER)) {
                username = "";
            }
            if (password.equals(PASSWORD_PLACEHOLDER)) {
                password = "";
            }

            if (username.isEmpty() || password.isEmpty()) {
                messageLabel.setForeground(Color.RED);
                messageLabel.setText("Username e password sono obbligatori.");
                return;
            }

            if (controller.login(username, password)) {
                messageLabel.setForeground(new Color(0, 128, 0));
                messageLabel.setText("Accesso effettuato con successo! Accesso in corso...");

            Timer timer = new Timer(2000, _ -> mainFrame.showDashboard());
                timer.setRepeats(false);
                timer.start();
            } else {
                messageLabel.setForeground(Color.RED);
                messageLabel.setText("Credenziali errate o utente non trovato.");
            }
    }

    /**
     * Imposta il bottone di login come default quando il pannello viene aggiunto al contenitore.
     */
    @Override
    public void addNotify() {
        super.addNotify();
        JRootPane rootPane = SwingUtilities.getRootPane(this);
        if (rootPane != null) {
            rootPane.setDefaultButton(loginButton);
        }
    }

    /**
     * Pulisce i campi di input e i messaggi del pannello di login.
     */
    public void clearFields() {
        usernameField.setText(USERNAME_PLACEHOLDER);
        usernameField.setForeground(Color.GRAY);
        passwordField.setText(PASSWORD_PLACEHOLDER);
        passwordField.setForeground(Color.GRAY);
        passwordField.setEchoChar((char)0);
        messageLabel.setText(" ");
    }
}