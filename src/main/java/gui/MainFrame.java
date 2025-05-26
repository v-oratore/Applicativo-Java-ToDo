package gui;

import controller.Controller;
import model.Utente; // Per il tipo Utente

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private LoginPanel loginPanel;
    private DashboardPanel dashboardPanel;
    private RegistrationPanel registrationPanel;
    private Utente utenteLoggato; // Questo è l'oggetto Utente dal modello
    private JPanel bottomPanelLogin;
    private JPanel bottomPanelDashboard;
    private JButton logoutButton;
    private JButton exitButton;

    private final Controller controller;

    public MainFrame(Controller controller) {
        this.controller = controller;
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setTitle("Gestore ToDo");
        setSize(1000, 650); // Leggermente più alto per contenuti
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.WHITE);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(Color.WHITE);

        loginPanel = new LoginPanel(this);
        registrationPanel = new RegistrationPanel(this);
        // DashboardPanel viene creato quando si mostra

        cardPanel.add(loginPanel, "login");
        cardPanel.add(registrationPanel, "register");

        add(cardPanel, BorderLayout.CENTER);

        exitButton = createStyledButton("Chiudi");
        exitButton.addActionListener(e -> {
            // Chiamata simbolica al salvataggio prima di uscire
            if (controller.isUserLoggedIn()) {
                controller.salvaStatoApplicazione();
            }
            System.exit(0);
        });

        logoutButton = createStyledButton("Esci");
        logoutButton.addActionListener(e -> {
            if (controller.isUserLoggedIn()) {
                controller.salvaStatoApplicazione(); // Chiamata simbolica
                controller.logout(); // Esegue il logout nel controller
            }
            setUtenteLoggato(null); // Cancella l'utente loggato in MainFrame
            showLogin();
        });

        bottomPanelLogin = new JPanel(new BorderLayout());
        bottomPanelLogin.setBackground(Color.WHITE);
        bottomPanelLogin.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        bottomPanelLogin.add(exitButton, BorderLayout.CENTER);

        bottomPanelDashboard = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottomPanelDashboard.setBackground(Color.WHITE);
        bottomPanelDashboard.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        bottomPanelDashboard.add(logoutButton);
        // Potremmo voler avere il pulsante Chiudi anche qui, o solo logout
        // bottomPanelDashboard.add(exitButton); // Opzionale

        add(bottomPanelLogin, BorderLayout.SOUTH);
        showLogin();
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(180, 40)); // Leggermente più larghi
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setFocusPainted(false);
        button.setBackground(new Color(230, 240, 255));
        button.setBorder(BorderFactory.createLineBorder(new Color(180, 200, 220)));
        button.setOpaque(true);
        return button;
    }

    public void showLogin() {
        remove(bottomPanelDashboard); // Rimuovi se presente
        add(bottomPanelLogin, BorderLayout.SOUTH); // Aggiungi quello del login
        cardLayout.show(cardPanel, "login");
        if (loginPanel != null) { // loginPanel dovrebbe essere sempre inizializzato
            loginPanel.clearFields();
            loginPanel.usernameField.requestFocusInWindow();
        }
        revalidate();
        repaint();
    }

    public void showDashboard() {
        if (!controller.isUserLoggedIn()) {
            // Sicurezza: non mostrare la dashboard se nessun utente è loggato
            showLogin();
            return;
        }
        setUtenteLoggato(controller.getUtenteCorrente()); // Assicura che utenteLoggato sia aggiornato

        remove(bottomPanelLogin); // Rimuovi se presente
        add(bottomPanelDashboard, BorderLayout.SOUTH); // Aggiungi quello della dashboard

        if (dashboardPanel != null) {
            cardPanel.remove(dashboardPanel); // Rimuovi la vecchia istanza se esiste
        }
        dashboardPanel = new DashboardPanel(this); // Crea una nuova istanza fresca
        cardPanel.add(dashboardPanel, "dashboard");
        cardLayout.show(cardPanel, "dashboard");
        revalidate();
        repaint();
    }

    public void showRegistration() {
        remove(bottomPanelDashboard);
        add(bottomPanelLogin, BorderLayout.SOUTH);
        cardLayout.show(cardPanel, "register");
        if (registrationPanel != null) {
            registrationPanel.clearFields();
            registrationPanel.usernameField.requestFocusInWindow();
        }
        revalidate();
        repaint();
    }

    // Questo metodo è usato da DashboardPanel per ottenere l'username
    // e da LoginPanel per impostare l'utente dopo il login (anche se ora login imposta utenteCorrente nel controller)
    public void setUtenteLoggato(Utente utente) {
        this.utenteLoggato = utente;
    }

    public Utente getUtenteLoggato() {
        // Preferibilmente, la GUI dovrebbe chiedere al controller chi è l'utente corrente
        // Ma per mantenere compatibilità con DashboardPanel che usa mainFrame.getUtenteLoggato().getUsername()
        // lo manteniamo, assicurandoci che sia sincronizzato con controller.getUtenteCorrente().
        if (controller.isUserLoggedIn()) {
            return controller.getUtenteCorrente();
        }
        return null; // o this.utenteLoggato che viene impostato da showDashboard
    }

    public Controller getController() {
        return controller;
    }
}