package gui;

import controller.Controller;
import javax.swing.*;
import java.awt.*;
import java.net.URL; // Importa URL
// RIMOSSO: import java.util.Objects; // Importa Objects

/**
 * Finestra principale dell'applicazione Task.
 * Gestisce la visualizzazione delle schermate di login, registrazione e dashboard tramite CardLayout.
 */
public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private LoginPanel loginPanel;
    private RegistrationPanel registrationPanel;

    private final transient Controller controller;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MainFrame.class);

    /**
     * Costruttore. Inizializza la finestra principale, i pannelli e l'icona dell'applicazione.
     * @param controller Controller principale dell'applicazione
     */
    public MainFrame(Controller controller) {
        this.controller = controller;
        setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
        setTitle("ToDoApp");

        setSize(1280, 800);
        setMinimumSize(new Dimension(1024, 768));
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setLayout(new BorderLayout());

        // Carica e imposta l'icona dell'applicazione
        try {
            URL iconURL = getClass().getResource("/Favicon.png");
            if (iconURL != null) {
                ImageIcon appIcon = new ImageIcon(iconURL);
                setIconImage(appIcon.getImage());
            } else {
                logger.error("Icona dell'applicazione non trovata: /Favicon.png");
            }
        } catch (Exception e) {
            logger.error("Errore durante il caricamento dell'icona dell'applicazione: {}", e.getMessage(), e);
        }
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        loginPanel = new LoginPanel(this);
        registrationPanel = new RegistrationPanel(this);

        cardPanel.add(loginPanel, "login");
        cardPanel.add(registrationPanel, "register");

        showLogin();
    }

    /**
     * Rimuove tutti i componenti dal content pane e aggiorna la finestra.
     */
    private void clearContentPane() {
        getContentPane().removeAll();
        revalidate();
        repaint();
    }

    /**
     * Mostra la schermata di login e pulisce i campi.
     */
    public void showLogin() {
        clearContentPane();
        getContentPane().add(cardPanel, BorderLayout.CENTER);
        cardLayout.show(cardPanel, "login");
        if (loginPanel != null) {
            loginPanel.clearFields();
        }
        revalidate();
        repaint();
    }

    /**
     * Mostra la dashboard principale se l'utente è loggato, altrimenti torna al login.
     */
    public void showDashboard() {
        if (!controller.isUserLoggedIn()) {
            showLogin();
            return;
        }
        clearContentPane();
        DashboardPanel dashboardPanel = new DashboardPanel(this);
        getContentPane().add(dashboardPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    /**
     * Mostra la schermata di registrazione e pulisce i campi.
     */
    public void showRegistrationPanel() {
        clearContentPane();
        getContentPane().add(cardPanel, BorderLayout.CENTER);
        cardLayout.show(cardPanel, "register");
        if (registrationPanel != null) {
            registrationPanel.clearFields();
        }
        revalidate();
        repaint();
    }

    /**
     * Restituisce il controller principale dell'applicazione.
     * @return Controller
     */
    public Controller getController() {
        return controller;
    }
}