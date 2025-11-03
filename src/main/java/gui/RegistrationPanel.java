// File: src/main/java/gui/RegistrationPanel.java
package gui;

import controller.Controller;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * Pannello Swing per la gestione della schermata di registrazione dell'applicazione Task.
 * Permette la creazione di un nuovo account utente e l'accesso automatico.
 */
public class RegistrationPanel extends JPanel {
    protected JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton registerButton;
    private JLabel messageLabel;

    private final transient MainFrame mainFrame;
    private final transient Controller controller;

    private static final String USERNAME_PLACEHOLDER = "Username";
    private static final String PASSWORD_PLACEHOLDER = "Password";
    private static final String CONFIRM_PASSWORD_PLACEHOLDER = "Conferma Password";

    /**
     * Costruttore. Inizializza il pannello di registrazione e i suoi componenti.
     * @param mainFrame Frame principale dell'applicazione
     */
    public RegistrationPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.controller = mainFrame.getController();

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JToolBar navToolbar = createNavToolbar(mainFrame);
        add(navToolbar, BorderLayout.NORTH);

        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);
    }

    /**
     * Crea la toolbar di navigazione con il logo e il bottone "Accedi".
     * @param mainFrame Frame principale dell'applicazione
     * @return JToolBar configurata
     */
    private JToolBar createNavToolbar(MainFrame mainFrame) {
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

        JButton accediButton = new JButton("Accedi");
        accediButton.setFont(FontManager.getRegular(14f));
        accediButton.setForeground(Color.WHITE);
        accediButton.setBackground(Color.BLACK);
        accediButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        accediButton.setFocusPainted(false);
        accediButton.setOpaque(true);
        accediButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        accediButton.setPreferredSize(new Dimension((int) accediButton.getPreferredSize().getWidth() + 20, 40));
        accediButton.setMinimumSize(accediButton.getPreferredSize());
        accediButton.setMaximumSize(accediButton.getPreferredSize());
        accediButton.addActionListener(_ -> mainFrame.showLogin());
        navToolbar.add(accediButton);
        return navToolbar;
    }

    /**
     * Crea il pannello centrale con titolo, campi e bottoni.
     * @return JPanel centrale
     */
    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(50, 250, 50, 250));
        centerPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Registrati", SwingConstants.CENTER);
        titleLabel.setFont(FontManager.getRegular(24f));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        centerPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel fieldsPanel = createFieldsPanel();
        centerPanel.add(fieldsPanel, BorderLayout.CENTER);

        JPanel southPanel = createSouthPanel();
        centerPanel.add(southPanel, BorderLayout.SOUTH);
        return centerPanel;
    }

    /**
     * Crea il pannello dei campi di input per la registrazione.
     * @return JPanel con i campi
     */
    private JPanel createFieldsPanel() {
        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        fieldsPanel.setBackground(Color.WHITE);
        fieldsPanel.setMaximumSize(new Dimension(350, Integer.MAX_VALUE));
        fieldsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        Border inputBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        );

        usernameField = createTextField(USERNAME_PLACEHOLDER, inputBorder);
        addFocusListenerToUsername();
        fieldsPanel.add(usernameField);
        fieldsPanel.add(Box.createVerticalStrut(15));

        passwordField = createPasswordField(PASSWORD_PLACEHOLDER, inputBorder);
        addFocusListenerToPassword(passwordField, PASSWORD_PLACEHOLDER);
        fieldsPanel.add(passwordField);
        fieldsPanel.add(Box.createVerticalStrut(15));

        confirmPasswordField = createPasswordField(CONFIRM_PASSWORD_PLACEHOLDER, inputBorder);
        addFocusListenerToPassword(confirmPasswordField, CONFIRM_PASSWORD_PLACEHOLDER);
        fieldsPanel.add(confirmPasswordField);
        fieldsPanel.add(Box.createVerticalStrut(15));

        messageLabel = new JLabel(" ");
        messageLabel.setFont(FontManager.getRegular(14f));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        fieldsPanel.add(messageLabel);
        return fieldsPanel;
    }

    /**
     * Crea un campo di testo con placeholder e bordo personalizzato.
     * @param placeholder Testo placeholder
     * @param border Bordo da applicare
     * @return JTextField configurato
     */
    private JTextField createTextField(String placeholder, Border border) {
        JTextField field = new JTextField(placeholder, 20);
        field.setFont(FontManager.getRegular(15f));
        field.setForeground(Color.GRAY);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, field.getPreferredSize().height + 10));
        field.setBorder(border);
        return field;
    }

    /**
     * Crea un campo password con placeholder e bordo personalizzato.
     * @param placeholder Testo placeholder
     * @param border Bordo da applicare
     * @return JPasswordField configurato
     */
    private JPasswordField createPasswordField(String placeholder, Border border) {
        JPasswordField field = new JPasswordField(placeholder, 20);
        field.setFont(FontManager.getRegular(15f));
        field.setForeground(Color.GRAY);
        field.setEchoChar((char)0);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, field.getPreferredSize().height + 10));
        field.setBorder(border);
        return field;
    }

    /**
     * Aggiunge il focus listener al campo username per gestire placeholder e colore.
     */
    private void addFocusListenerToUsername() {
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
    }

    /**
     * Aggiunge il focus listener a un campo password per gestire placeholder, colore ed echo char.
     * @param field Campo password
     * @param placeholder Testo placeholder
     */
    private void addFocusListenerToPassword(JPasswordField field, String placeholder) {
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (new String(field.getPassword()).equals(placeholder)) {
                    field.setText("");
                    field.setEchoChar('•');
                    field.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (new String(field.getPassword()).isEmpty()) {
                    field.setText(placeholder);
                    field.setEchoChar((char)0);
                    field.setForeground(Color.GRAY);
                }
            }
        });
    }

    /**
     * Crea il pannello sud con il bottone di registrazione.
     * @return JPanel sud
     */
    private JPanel createSouthPanel() {
        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
        southPanel.setOpaque(false);

        registerButton = new JButton("Entra");
        registerButton.setFont(FontManager.getRegular(15f));
        registerButton.setBackground(Color.BLACK);
        registerButton.setForeground(Color.WHITE);
        registerButton.setOpaque(true);
        registerButton.setFocusPainted(false);
        registerButton.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerButton.setMaximumSize(new Dimension(200, 40));
        registerButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                registerButton.setBackground(Color.DARK_GRAY);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                registerButton.setBackground(Color.BLACK);
            }
        });

        southPanel.add(Box.createVerticalStrut(10));
        southPanel.add(registerButton);

        registerButton.addActionListener(_ -> handleRegisterAction());
        return southPanel;
    }

    /**
     * Gestisce la logica di registrazione dell'utente.
     * Mostra messaggi di errore o successo e reindirizza alla dashboard se la registrazione ha successo.
     */
    private void handleRegisterAction() {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            if (username.equals(USERNAME_PLACEHOLDER)) {
                username = "";
            }
            if (password.equals(PASSWORD_PLACEHOLDER)) {
                password = "";
            }
            if (confirmPassword.equals(CONFIRM_PASSWORD_PLACEHOLDER)) {
                confirmPassword = "";
            }

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

            try {
                if (controller.registraUtente(username, password)) {
                    if (controller.login(username, password)) {
                        messageLabel.setForeground(new Color(0, 128, 0));
                        messageLabel.setText("Registrazione effettuata con successo! Accesso in corso...");

                        Timer timer = new Timer(2000, _ -> {
                            mainFrame.showDashboard();
                            clearFields();
                        });
                        timer.setRepeats(false);
                        timer.start();
                    } else {
                        messageLabel.setForeground(Color.RED);
                        messageLabel.setText("Registrazione riuscita, ma accesso automatico fallito. Riprova il login.");
                    }
                } else {
                    messageLabel.setForeground(Color.RED);
                    messageLabel.setText("Registrazione fallita (username già esistente o dati non validi).");
                }
            } catch (IllegalArgumentException _) {
                messageLabel.setForeground(Color.RED);
                messageLabel.setText("Errore di registrazione: username già esistente o dati non validi.");
            } catch (Exception _) {
                messageLabel.setForeground(Color.RED);
                messageLabel.setText("Errore sconosciuto durante la registrazione.");
            }
    }

    /**
     * Imposta il bottone di registrazione come default quando il pannello viene aggiunto al contenitore.
     */
    @Override
    public void addNotify() {
        super.addNotify();
        JRootPane rootPane = SwingUtilities.getRootPane(this);
        if (rootPane != null) {
            rootPane.setDefaultButton(registerButton);
        }
    }

    /**
     * Pulisce i campi di input e i messaggi del pannello di registrazione.
     */
    public void clearFields() {
        usernameField.setText(USERNAME_PLACEHOLDER);
        usernameField.setForeground(Color.GRAY);
        passwordField.setText(PASSWORD_PLACEHOLDER);
        passwordField.setForeground(Color.GRAY);
        passwordField.setEchoChar((char)0);
        confirmPasswordField.setText(CONFIRM_PASSWORD_PLACEHOLDER);
        confirmPasswordField.setForeground(Color.GRAY);
        confirmPasswordField.setEchoChar((char)0);
        messageLabel.setText(" ");
    }
}