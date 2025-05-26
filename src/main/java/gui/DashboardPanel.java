package gui;

import controller.Controller;
import model.Bacheca;
import model.ToDo;
import model.TitoloBacheca;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DashboardPanel extends JPanel {
    private Controller controller;
    private JPanel boardsDisplayPanel;
    private List<BoardPanel> boardPanels = new ArrayList<>();
    private MainFrame mainFrame;

    public DashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.controller = mainFrame.getController();

        if (!controller.isUserLoggedIn()) {
            JOptionPane.showMessageDialog(this, "Errore: Utente non loggato.", "Errore Dashboard", JOptionPane.ERROR_MESSAGE);
            this.mainFrame.showLogin();
            return;
        }

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel welcomeLabel = new JLabel("Benvenuto " + controller.getUtenteCorrente().getUsername() + "!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel header = new JLabel("Le tue bacheche:");
        header.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.setAlignmentX(Component.CENTER_ALIGNMENT);

        topPanel.add(welcomeLabel);
        topPanel.add(Box.createVerticalStrut(10));
        topPanel.add(header);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        actionPanel.setBackground(Color.WHITE);

        JButton newBoardButton = createStyledButton("Crea");
        JButton saveStateButton = createStyledButton("Salva");
        JButton completeAllTasksButton = createStyledButton("Completa");
        JButton editBoardDescButton = createStyledButton("Modifica"); // Rinominato per chiarezza
        JButton deleteBoardButton = createStyledButton("Elimina");

        actionPanel.add(newBoardButton);
        actionPanel.add(saveStateButton);
        actionPanel.add(completeAllTasksButton);
        actionPanel.add(editBoardDescButton); // Bottone aggiornato
        actionPanel.add(deleteBoardButton);

        topPanel.add(Box.createVerticalStrut(10));
        topPanel.add(actionPanel);

        add(topPanel, BorderLayout.NORTH);

        boardsDisplayPanel = new JPanel(new GridLayout(0, 3, 20, 10));
        boardsDisplayPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        boardsDisplayPanel.setBackground(Color.WHITE);
        JScrollPane scrollableBoards = new JScrollPane(boardsDisplayPanel);
        scrollableBoards.setBorder(null);
        scrollableBoards.getVerticalScrollBar().setUnitIncrement(16);
        scrollableBoards.getHorizontalScrollBar().setUnitIncrement(16);
        scrollableBoards.getViewport().setBackground(Color.WHITE);

        add(scrollableBoards, BorderLayout.CENTER);

        // ActionListeners (newBoardButton, saveStateButton, deleteBoardButton, completeAllTasksButton)
        // ... (come nella versione precedente, assicurati che il newBoardButton gestisca correttamente l'annullamento della descrizione)
        newBoardButton.addActionListener(e -> {
            if (!controller.isUserLoggedIn()) return;
            List<String> availableTitles = controller.getAvailableBoardTitlesForCurrentUser();
            if (availableTitles.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Hai raggiunto il limite massimo di " + controller.getMaxBoards() + " bacheche, oppure non ci sono titoli standard disponibili.", "Limite Bacheche / Nessuna Disponibile", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            String chosenDisplayName = (String) JOptionPane.showInputDialog(this, "Scegli una bacheca da aggiungere:", "Nuova Bacheca", JOptionPane.PLAIN_MESSAGE, null, availableTitles.toArray(), availableTitles.get(0));
            if (chosenDisplayName != null && !chosenDisplayName.trim().isEmpty()) {
                try {
                    TitoloBacheca titoloEnum = TitoloBacheca.fromDisplayName(chosenDisplayName);
                    String descrizione = JOptionPane.showInputDialog(this, "Inserisci una descrizione per la bacheca '" + chosenDisplayName + "' [Opzionale]:");
                    if (descrizione == null) {
                        System.out.println("DashboardPanel: Aggiunta bacheca '" + chosenDisplayName + "' annullata durante inserimento descrizione.");
                        return;
                    }
                    if (controller.aggiungiBacheca(titoloEnum, descrizione.trim())) {
                        loadUserBoards();
                        JOptionPane.showMessageDialog(this, "Bacheca '" + chosenDisplayName + "' aggiunta con successo.");
                    } else {
                        JOptionPane.showMessageDialog(this, "Impossibile aggiungere la bacheca '" + chosenDisplayName + "'.\nPotrebbe esistere già o si è verificato un errore (limite raggiunto).", "Errore Creazione Bacheca", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(this, "Titolo bacheca selezionato non valido: " + chosenDisplayName, "Errore", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        saveStateButton.addActionListener(e -> {
            if (!controller.isUserLoggedIn()) return;
            controller.salvaStatoApplicazione();
            JOptionPane.showMessageDialog(this, "Stato dell'applicazione 'salvato' in memoria (simbolico).");
        });

        deleteBoardButton.addActionListener(e -> {
            if (!controller.isUserLoggedIn() || controller.getBachecheUtenteCorrente().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nessuna bacheca da eliminare o utente non loggato.");
                return;
            }
            String[] nomiBachecheAttuali = controller.getBachecheUtenteCorrente().stream()
                    .map(Bacheca::getTitoloDisplayName)
                    .toArray(String[]::new);
            String selezionataDisplayName = (String) JOptionPane.showInputDialog(this, "Scegli la bacheca da eliminare:", "Elimina Bacheca", JOptionPane.QUESTION_MESSAGE, null, nomiBachecheAttuali, nomiBachecheAttuali[0]);
            if (selezionataDisplayName != null) {
                int confirm = JOptionPane.showConfirmDialog(this, "Sei sicuro di voler eliminare la bacheca '" + selezionataDisplayName + "' e tutti i suoi ToDo?", "Conferma Eliminazione", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        TitoloBacheca titoloEnumDaEliminare = TitoloBacheca.fromDisplayName(selezionataDisplayName);
                        if (controller.eliminaBacheca(titoloEnumDaEliminare)) {
                            loadUserBoards();
                            JOptionPane.showMessageDialog(this, "Bacheca '" + selezionataDisplayName + "' eliminata.");
                        } else {
                            JOptionPane.showMessageDialog(this, "Impossibile eliminare la bacheca '" + selezionataDisplayName + "'.", "Errore", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (IllegalArgumentException ex) {
                        JOptionPane.showMessageDialog(this, "Nome bacheca selezionato non valido per eliminazione: " + selezionataDisplayName, "Errore", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        completeAllTasksButton.addActionListener(e -> {
            if (!controller.isUserLoggedIn() || boardPanels.isEmpty()) return;
            String[] nomi = boardPanels.stream().map(BoardPanel::getBoardDisplayName).toArray(String[]::new); // Usa il nome visualizzato dal pannello
            if (nomi.length == 0) {
                JOptionPane.showMessageDialog(this, "Nessuna bacheca su cui operare."); return;
            }
            String selezionata = (String) JOptionPane.showInputDialog(this, "Scegli la bacheca i cui ToDo vuoi completare:", "Completa Tasks Bacheca", JOptionPane.PLAIN_MESSAGE, null, nomi, nomi[0]);
            if (selezionata != null) {
                boardPanels.stream()
                        .filter(bp -> bp.getBoardDisplayName().equals(selezionata))
                        .findFirst()
                        .ifPresent(BoardPanel::completaTuttiITasks);
            }
        });

        editBoardDescButton.addActionListener(e -> { // Azione per editBoardDescButton
            if (!controller.isUserLoggedIn() || controller.getBachecheUtenteCorrente().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nessuna bacheca da editare o utente non loggato."); return;
            }
            String[] nomiBachecheAttuali = controller.getBachecheUtenteCorrente().stream()
                    .map(Bacheca::getTitoloDisplayName)
                    .toArray(String[]::new);
            String selezionataDisplayName = (String) JOptionPane.showInputDialog(this, "Scegli la bacheca di cui modificare la descrizione:",
                    "Modifica Descrizione Bacheca", JOptionPane.PLAIN_MESSAGE, null, nomiBachecheAttuali, nomiBachecheAttuali[0]);

            if (selezionataDisplayName != null) {
                Optional<Bacheca> bachecaOpt = controller.getBachecaByDisplayNameDaUtenteCorrente(selezionataDisplayName);
                if (bachecaOpt.isPresent()) {
                    String descrizioneCorrente = bachecaOpt.get().getDescrizione() != null ? bachecaOpt.get().getDescrizione() : "";
                    String nuovaDescrizione = JOptionPane.showInputDialog(this, "Nuova descrizione per '" + selezionataDisplayName + "':", descrizioneCorrente);

                    if (nuovaDescrizione != null) {
                        try {
                            TitoloBacheca titoloEnum = TitoloBacheca.fromDisplayName(selezionataDisplayName);
                            if (controller.modificaDescrizioneBacheca(titoloEnum, nuovaDescrizione.trim())) {
                                JOptionPane.showMessageDialog(this, "Descrizione aggiornata per la bacheca '" + selezionataDisplayName + "'.");
                                // Aggiorna la visualizzazione nel BoardPanel corrispondente
                                boardPanels.stream()
                                        .filter(bp -> bp.getBoardCanonicalName().equalsIgnoreCase(selezionataDisplayName))
                                        .findFirst()
                                        .ifPresent(bp -> bp.aggiornaDescrizioneVisualizzata(nuovaDescrizione.trim()));
                            } else {
                                JOptionPane.showMessageDialog(this, "Errore durante l'aggiornamento della descrizione.", "Errore", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (IllegalArgumentException ex) {
                            JOptionPane.showMessageDialog(this, "Nome bacheca per modifica non valido: " + selezionataDisplayName, "Errore", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Bacheca '" + selezionataDisplayName + "' non trovata nel modello.", "Errore Interno", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        loadUserBoards();
    }

    private void loadUserBoards() {
        if (!controller.isUserLoggedIn()) {
            boardsDisplayPanel.removeAll();
            boardPanels.clear();
            boardsDisplayPanel.revalidate();
            boardsDisplayPanel.repaint();
            return;
        }
        boardsDisplayPanel.removeAll();
        boardPanels.clear();

        List<Bacheca> bachecheUtente = controller.getBachecheUtenteCorrente();

        if (bachecheUtente.isEmpty()) {
            System.out.println("DashboardPanel: Nessuna bacheca per l'utente " + controller.getUtenteCorrente().getUsername() + ". Utilizzare 'Crea Bacheca'.");
            boardsDisplayPanel.setLayout(new BorderLayout());
            JLabel noBoardsLabel = new JLabel("Nessuna bacheca disponibile. Clicca su 'Crea'.");
            noBoardsLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            noBoardsLabel.setHorizontalAlignment(SwingConstants.CENTER);
            boardsDisplayPanel.add(noBoardsLabel, BorderLayout.CENTER);

        } else {
            boardsDisplayPanel.setLayout(new GridLayout(0, 3, 20, 10));
            for (Bacheca bacheca : bachecheUtente) {
                String titoloDisplayName = bacheca.getTitoloDisplayName();
                String descrizioneBacheca = bacheca.getDescrizione(); // OTTIENI LA DESCRIZIONE
                List<ToDo> todos = bacheca.getTodos();
                // PASSA LA DESCRIZIONE AL COSTRUTTORE DI BoardPanel
                BoardPanel bachecaPanel = new BoardPanel(titoloDisplayName, descrizioneBacheca, controller, controller.getUtenteCorrente().getUsername(), todos, this::refreshSelectedBoardPanel);
                boardsDisplayPanel.add(bachecaPanel);
                boardPanels.add(bachecaPanel);
            }
        }
        boardsDisplayPanel.revalidate();
        boardsDisplayPanel.repaint();
    }

    public void refreshSelectedBoardPanel(String boardDisplayName) {
        if (!controller.isUserLoggedIn()) return;
        boardPanels.stream()
                .filter(bp -> bp.getBoardCanonicalName().equalsIgnoreCase(boardDisplayName))
                .findFirst()
                .ifPresent(BoardPanel::refreshToDoList);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setPreferredSize(new Dimension(200, 35));
        button.setFocusPainted(false);
        button.setBackground(new Color(240, 245, 255));
        button.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        button.setOpaque(true);
        return button;
    }
}