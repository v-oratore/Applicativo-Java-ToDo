// File: src/main/java/gui/DashboardPanel.java
package gui;

import controller.Controller;
import model.Bacheca;
import model.ToDo;
import model.TitoloBacheca;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import model.Utente;

/**
 * Pannello principale della dashboard dell'applicazione Task.
 * Gestisce la visualizzazione e le azioni sulle bacheche e i Task dell'utente.
 */
public class DashboardPanel extends JPanel {

    private final transient Controller controller;
    private final JPanel boardsDisplayPanel;
    private final List<BoardPanel> boardPanels = new ArrayList<>();
    private final MainFrame mainFrame;

    private JMenuItem creaBachecaMenuItem;
    private JMenuItem modificaDescMenuItem;
    private JMenuItem eliminaBachecaMenuItem;
    private JMenuItem completaTuttiMenuItem;
    private JMenuItem spostaToDoMenuItem;

    private static final String SPOSATA_TODO = "Sposta ToDo";
    private static final String MODIFICA = "MODIFICA";
    private static final String ELIMINA = "ELIMINA";
    private static final String ERRORE = "Errore";
    private static final String COMPLETA = "COMPLETA";
    private static final String INFORMAZIONI = "Informazioni";
    private static final String ELIMINA_BUTTON = "Elimina";
    private static final String ANNULLA = "Annulla";
    private static final String COMPLETA_BUTTON = "Completa";
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DashboardPanel.class);

    /**
     * Costruttore. Inizializza la dashboard e carica le bacheche dell'utente.
     * @param mainFrame Frame principale dell'applicazione
     */
    public DashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.controller = mainFrame.getController();

        if (!controller.isUserLoggedIn()) {
            JOptionPane.showMessageDialog(this, "Errore: Utente non loggato.", "Errore Dashboard", JOptionPane.ERROR_MESSAGE);
            this.mainFrame.showLogin();
            this.boardsDisplayPanel = new JPanel();
            return;
        }

        setLayout(new BorderLayout(0, 15));
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

        JButton bachecaMenuButton = createMenuButton("Bacheca");
        JPopupMenu bachecaMenu = createBachecaMenu();
        bachecaMenuButton.addActionListener(_ -> {
            boolean hasBoards = !controller.getBachecheUtenteCorrente().isEmpty();
            boolean canCreateBoard = !controller.getAvailableBoardTitlesForCurrentUser().isEmpty();

            creaBachecaMenuItem.setEnabled(canCreateBoard);
            modificaDescMenuItem.setEnabled(hasBoards);
            eliminaBachecaMenuItem.setEnabled(hasBoards);

            boolean hasUncompletedTodos = controller.getAllToDosUtenteCorrente().stream()
                    .anyMatch(todo -> todo.getStato() != model.StatoToDo.COMPLETATO);
            completaTuttiMenuItem.setEnabled(hasUncompletedTodos);

            // Abilita Sposta Task solo se esiste almeno un Task NON completato
            boolean canMoveTodos = controller.getAllToDosUtenteCorrente().stream()
                    .anyMatch(todo -> todo.getStato() != model.StatoToDo.COMPLETATO)
                && controller.getBachecheUtenteCorrente().size() >= 2;
            spostaToDoMenuItem.setEnabled(canMoveTodos);

            int x = (int) (bachecaMenuButton.getWidth() - bachecaMenu.getPreferredSize().getWidth());
            bachecaMenu.show(bachecaMenuButton, x, bachecaMenuButton.getHeight() + 5);
        });

        JButton infoMenuButton = createMenuButton(INFORMAZIONI);
        infoMenuButton.addActionListener(_ -> showInfoDialog());

        JPopupMenu accountMenu = createAccountMenu();
        JButton accountButton = createMenuButton("Account");
        accountButton.addActionListener(_ -> {
            int x = (int) (accountButton.getWidth() - accountMenu.getPreferredSize().getWidth());
            accountMenu.show(accountButton, x, accountButton.getHeight() + 5);
        });

        navToolbar.add(bachecaMenuButton);
        navToolbar.addSeparator(new Dimension(15,0));
        navToolbar.add(infoMenuButton);
        navToolbar.addSeparator(new Dimension(15,0));
        navToolbar.add(accountButton);

        add(navToolbar, BorderLayout.NORTH);

        JPanel mainContentPanel = new JPanel(new BorderLayout(0, 10));
        mainContentPanel.setBackground(Color.WHITE);
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        boardsDisplayPanel = new JPanel(new GridLayout(0, 3, 20, 10));
        boardsDisplayPanel.setBackground(Color.WHITE);
        boardsDisplayPanel.setBorder(null);
        JScrollPane scrollableBoards = new JScrollPane(boardsDisplayPanel);
        scrollableBoards.setBorder(null);
        scrollableBoards.getVerticalScrollBar().setUnitIncrement(16);
        scrollableBoards.getHorizontalScrollBar().setUnitIncrement(16);
        scrollableBoards.getViewport().setBackground(Color.WHITE);

        mainContentPanel.add(scrollableBoards, BorderLayout.CENTER);
        add(mainContentPanel, BorderLayout.CENTER);

        loadUserBoards();
    }

    /**
     * Crea il menu popup per la gestione delle bacheche.
     * @return JPopupMenu configurato
     */
    private JPopupMenu createBachecaMenu() {
        JPopupMenu menu = new JPopupMenu();

        creaBachecaMenuItem = new JMenuItem("Crea");
        modificaDescMenuItem = new JMenuItem("Modifica");
        eliminaBachecaMenuItem = new JMenuItem(ELIMINA_BUTTON);
        completaTuttiMenuItem = new JMenuItem(COMPLETA_BUTTON);
        spostaToDoMenuItem = new JMenuItem(SPOSATA_TODO);

        creaBachecaMenuItem.setFont(FontManager.getRegular(14f));
        creaBachecaMenuItem.addActionListener(_ -> showBoardActionDialog("CREA"));

        modificaDescMenuItem.setFont(FontManager.getRegular(14f));
        modificaDescMenuItem.addActionListener(_ -> showBoardActionDialog(MODIFICA));

        eliminaBachecaMenuItem.setFont(FontManager.getRegular(14f));
        eliminaBachecaMenuItem.addActionListener(_ -> showBoardActionDialog(ELIMINA));

        completaTuttiMenuItem.setFont(FontManager.getRegular(14f));
        completaTuttiMenuItem.addActionListener(_ -> handleCompletaTuttiIToDoInBacheca());

        spostaToDoMenuItem.setFont(FontManager.getRegular(14f));
        spostaToDoMenuItem.addActionListener(_ -> handleSpostaToDo());

        menu.add(creaBachecaMenuItem);
        menu.addSeparator();
        menu.add(modificaDescMenuItem);
        menu.addSeparator();
        menu.add(eliminaBachecaMenuItem);
        menu.addSeparator();
        menu.add(completaTuttiMenuItem);
        menu.addSeparator();
        menu.add(spostaToDoMenuItem);

        return menu;
    }

    /**
     * Crea il menu popup per la gestione dell'account.
     * @return JPopupMenu configurato
     */
    private JPopupMenu createAccountMenu() {
        JPopupMenu accountMenu = new JPopupMenu();
        JMenuItem infoMenuItem = new JMenuItem(INFORMAZIONI);
        infoMenuItem.setFont(FontManager.getRegular(14f));
        infoMenuItem.addActionListener(_ -> showAccountInfoDialog());
        accountMenu.add(infoMenuItem);
        accountMenu.addSeparator();
        JMenuItem logoutMenuItem = new JMenuItem("Esci");
        logoutMenuItem.setFont(FontManager.getRegular(14f));
        accountMenu.add(logoutMenuItem);
        logoutMenuItem.addActionListener(_ -> {
            controller.logout();
            mainFrame.showLogin();
        });
        return accountMenu;
    }

    /**
     * Mostra la finestra di dialogo per la creazione/modifica/eliminazione/completamento bacheca.
     * @param actionType Tipo di azione (CREA, MODIFICA, ELIMINA, COMPLETA)
     */
    private void showBoardActionDialog(String actionType) {
        JDialog actionDialog = createBoardActionDialog(actionType);
        Dimension dialogSize = calculateBoardDialogSize();
        actionDialog.setSize(dialogSize);
        actionDialog.setLocationRelativeTo(boardsDisplayPanel);
        actionDialog.setVisible(true);
    }

    /**
     * Crea la finestra di dialogo per la gestione delle bacheche.
     * @param panelType Tipo di pannello (CREA, MODIFICA, ELIMINA, COMPLETA)
     * @return JDialog configurato
     */
    private JDialog createBoardActionDialog(String panelType) {
        final JDialog dialog = new JDialog(mainFrame, "Gestione Bacheca", true);
        dialog.setUndecorated(true);
        ((JComponent)dialog.getContentPane()).setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 0, 15));

        JPanel formContentWrapperPanel = new JPanel();
        formContentWrapperPanel.setLayout(new BoxLayout(formContentWrapperPanel, BoxLayout.Y_AXIS));
        formContentWrapperPanel.setOpaque(false);
        formContentWrapperPanel.add(Box.createVerticalGlue());

        FormComponents formComponents = createFormComponents(panelType);
        formContentWrapperPanel.add(formComponents.formPanel, BorderLayout.CENTER);
        formContentWrapperPanel.add(Box.createVerticalGlue());
        contentPanel.add(formContentWrapperPanel, BorderLayout.CENTER);

        JPanel buttonPanel = createButtonPanel(dialog, panelType, formComponents);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(contentPanel);
        return dialog;
    }

    /**
     * Crea i componenti del form per la gestione delle bacheche.
     * @param panelType Tipo di pannello
     * @return Oggetto FormComponents con i riferimenti ai componenti
     */
    private static class FormComponents {
        JPanel formPanel;
        JComboBox<String> boardComboBox;
        JTextField descriptionField;
    }

    /**
     * Crea i componenti del form per la gestione delle bacheche.
     * @param panelType Tipo di pannello
     * @return Oggetto FormComponents con i riferimenti ai componenti
     */
    private FormComponents createFormComponents(String panelType) {
        FormComponents components = new FormComponents();
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.insets = new Insets(0, 0, 5, 0);

        JLabel nomeLabel = new JLabel("Nome: *");
        nomeLabel.setFont(FontManager.getRegular(13f));
        formPanel.add(nomeLabel, gbc);

        List<String> boardItems;
        if ("CREA".equals(panelType)) {
            boardItems = controller.getAvailableBoardTitlesForCurrentUser();
        } else {
            boardItems = controller.getBachecheUtenteCorrente().stream()
                    .map(Bacheca::getTitoloDisplayName)
                    .toList();
        }
        JComboBox<String> boardComboBox = new JComboBox<>(boardItems.toArray(new String[0]));
        boardComboBox.setFont(FontManager.getRegular(14f));
        boardComboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1),
                BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));
        boardComboBox.setBackground(Color.WHITE);
        boardComboBox.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton button = new JButton() {
                    @Override
                    public void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2d = (Graphics2D) g.create();
                        g2d.setColor(Color.BLACK);
                        int x = (getWidth() - 8) / 2;
                        int y = (getHeight() - 5) / 2;
                        int[] xPoints = {x, x + 8, x + 4};
                        int[] yPoints = {y, y, y + 5};
                        g2d.fillPolygon(xPoints, yPoints, 3);
                        g2d.dispose();
                    }
                };
                button.setOpaque(false);
                button.setFocusPainted(false);
                button.setBorder(BorderFactory.createEmptyBorder());
                button.setBackground(Color.WHITE);
                button.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent ignored) {
                        button.setBackground(Color.LIGHT_GRAY);
                    }
                    @Override
                    public void mouseExited(MouseEvent ignored) {
                        button.setBackground(Color.WHITE);
                    }
                });
                return button;
            }
        });
        formPanel.add(boardComboBox, gbc);

        JTextField descriptionField = new JTextField();
        descriptionField.setFont(FontManager.getRegular(14f));
        descriptionField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1),
                BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));
        descriptionField.setBackground(Color.WHITE);
        if (!ELIMINA.equals(panelType) && !COMPLETA.equals(panelType)) {
            gbc.insets = new Insets(10, 0, 5, 0);
            JLabel descLabel = new JLabel("Descrizione:");
            descLabel.setFont(FontManager.getRegular(13f));
            formPanel.add(descLabel, gbc);
            gbc.insets = new Insets(0, 0, 5, 0);
            formPanel.add(descriptionField, gbc);
            if (MODIFICA.equals(panelType) && boardComboBox.getItemCount() > 0) {
                boardComboBox.addActionListener(_ -> {
                    String selectedBoardName = (String) boardComboBox.getSelectedItem();
                    controller.getBachecaByDisplayNameDaUtenteCorrente(selectedBoardName)
                            .ifPresent(b -> descriptionField.setText(b.getDescrizione()));
                });
                boardComboBox.setSelectedIndex(0);
                boardComboBox.getActionListeners()[0].actionPerformed(new ActionEvent(boardComboBox, ActionEvent.ACTION_PERFORMED, null));
            }
        }
        components.formPanel = formPanel;
        components.boardComboBox = boardComboBox;
        components.descriptionField = descriptionField;
        return components;
    }

    /**
     * Crea il pannello dei bottoni per le finestre di dialogo bacheca.
     * @param dialog Dialogo di riferimento
     * @param panelType Tipo di pannello
     * @param formComponents Componenti del form
     * @return JPanel con i bottoni
     */
    private JPanel createButtonPanel(JDialog dialog, String panelType, FormComponents formComponents) {
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        JButton cancelButton = new JButton(ANNULLA);
        styleDialogButton(cancelButton);
        JButton primaryButton = new JButton();
        styleDialogButton(primaryButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(primaryButton);
        cancelButton.addActionListener(_ -> dialog.dispose());
        switch (panelType) {
            case MODIFICA:
                primaryButton.setText("Salva");
                primaryButton.addActionListener(_ -> handleModifica(dialog, formComponents));
                break;
            case ELIMINA: {
                primaryButton.setText(ELIMINA_BUTTON);
                final boolean[] confirming = {false};
                primaryButton.addActionListener(_ -> {
                    if (!confirming[0]) {
                        primaryButton.setText("Sei sicuro?");
                        confirming[0] = true;
                    } else {
                        handleElimina(dialog, formComponents);
                    }
                });
                break;
            }
            case COMPLETA:
                primaryButton.setText(COMPLETA_BUTTON);
                primaryButton.addActionListener(_ -> handleCompleta(dialog, formComponents));
                break;
            default:
                primaryButton.setText("Crea");
                primaryButton.addActionListener(_ -> handleCrea(dialog, formComponents));
                break;
        }
        return buttonPanel;
    }

    /**
     * Gestisce la modifica della descrizione di una bacheca.
     * @param dialog Dialogo di riferimento
     * @param formComponents Componenti del form
     */
    private void handleModifica(JDialog dialog, FormComponents formComponents) {
        String chosenDisplayName = (String) formComponents.boardComboBox.getSelectedItem();
        if (chosenDisplayName == null) return;
        try {
            TitoloBacheca titoloEnum = TitoloBacheca.fromDisplayName(chosenDisplayName);
            if (controller.modificaDescrizioneBacheca(titoloEnum, formComponents.descriptionField.getText().trim())) {
                dialog.dispose();
                loadUserBoards();
            } else {
                JOptionPane.showMessageDialog(dialog, "Errore durante l'aggiornamento.", ERRORE, JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException _) {
            JOptionPane.showMessageDialog(dialog, "Titolo bacheca non valido.", ERRORE, JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Gestisce l'eliminazione di una bacheca.
     * @param dialog Dialogo di riferimento
     * @param formComponents Componenti del form
     */
    private void handleElimina(JDialog dialog, FormComponents formComponents) {
        String chosenDisplayName = (String) formComponents.boardComboBox.getSelectedItem();
        if (chosenDisplayName == null) return;
        try {
            TitoloBacheca titoloEnum = TitoloBacheca.fromDisplayName(chosenDisplayName);
            if (controller.eliminaBacheca(titoloEnum)) {
                dialog.dispose();
                loadUserBoards();
            } else {
                JOptionPane.showMessageDialog(dialog, "Impossibile eliminare la bacheca.", ERRORE, JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException _) {
            JOptionPane.showMessageDialog(dialog, "Nome bacheca non valido.", ERRORE, JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Gestisce la creazione di una nuova bacheca.
     * @param dialog Dialogo di riferimento
     * @param formComponents Componenti del form
     */
    private void handleCrea(JDialog dialog, FormComponents formComponents) {
        String chosenDisplayName = (String) formComponents.boardComboBox.getSelectedItem();
        if (chosenDisplayName == null) return;
        try {
            TitoloBacheca titoloEnum = TitoloBacheca.fromDisplayName(chosenDisplayName);
            if (controller.aggiungiBacheca(titoloEnum, formComponents.descriptionField.getText().trim())) {
                dialog.dispose();
                loadUserBoards();
            } else {
                JOptionPane.showMessageDialog(dialog, "Impossibile aggiungere la bacheca.", ERRORE, JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException _) {
            JOptionPane.showMessageDialog(dialog, "Titolo bacheca non valido.", ERRORE, JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Gestisce il completamento di tutti i Task in una bacheca.
     * @param dialog Dialogo di riferimento
     * @param formComponents Componenti del form
     */
    private void handleCompleta(JDialog dialog, FormComponents formComponents) {
        String chosenDisplayName = (String) formComponents.boardComboBox.getSelectedItem();
        if (chosenDisplayName == null) return;
        BoardPanel targetPanel = boardPanels.stream()
                .filter(bp -> bp.getBoardDisplayName().equals(chosenDisplayName))
                .findFirst().orElse(null);
        if (targetPanel != null) {
            targetPanel.completaTuttiITasks();
            dialog.dispose();
        }
    }

    /**
     * Gestisce il completamento di tutti i Task in una bacheca selezionata dall'utente.
     */
    private void handleCompletaTuttiIToDoInBacheca() {
        if (!controller.isUserLoggedIn()) return;
        if (boardPanels.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nessuna bacheca disponibile per completare Task.", "Completa Task", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        // Dialog custom identica a EliminaBacheca
        JDialog dialog = new JDialog(mainFrame, "Completa tutti i Task", true);
        dialog.setUndecorated(true);
        ((JComponent)dialog.getContentPane()).setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 0, 15));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.insets = new Insets(0, 0, 5, 0);

        JLabel nomeLabel = new JLabel("Nome bacheca: *");
        nomeLabel.setFont(FontManager.getRegular(13f));
        formPanel.add(nomeLabel, gbc);

        // Filtra solo le bacheche con almeno un Task non completato
        List<BoardPanel> eligibleBoards = boardPanels.stream()
                .filter(bp -> bp.getTodos().stream().anyMatch(todo -> todo.getStato() != model.StatoToDo.COMPLETATO))
                .toList();
        if (eligibleBoards.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nessuna bacheca con Task da completare.", "Completa Task", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String[] boardNames = eligibleBoards.stream().map(BoardPanel::getBoardDisplayName).toArray(String[]::new);
        JComboBox<String> boardComboBox = new JComboBox<>(boardNames);
        boardComboBox.setFont(FontManager.getRegular(14f));
        boardComboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1),
                BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));
        boardComboBox.setBackground(Color.WHITE);
        boardComboBox.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton button = new JButton() {
                    @Override
                    public void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2d = (Graphics2D) g.create();
                        g2d.setColor(Color.BLACK);
                        int x = (getWidth() - 8) / 2;
                        int y = (getHeight() - 5) / 2;
                        int[] xPoints = {x, x + 8, x + 4};
                        int[] yPoints = {y, y, y + 5};
                        g2d.fillPolygon(xPoints, yPoints, 3);
                        g2d.dispose();
                    }
                };
                button.setOpaque(false);
                button.setFocusPainted(false);
                button.setBorder(BorderFactory.createEmptyBorder());
                button.setBackground(Color.WHITE);
                button.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent ignored) {
                        button.setBackground(Color.LIGHT_GRAY);
                    }
                    @Override
                    public void mouseExited(java.awt.event.MouseEvent ignored) {
                        button.setBackground(Color.WHITE);
                    }
                });
                return button;
            }
        });
        formPanel.add(boardComboBox, gbc);

        contentPanel.add(formPanel, BorderLayout.CENTER);

        // Bottoni con doppio click
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        JButton cancelButton = new JButton(ANNULLA);
        styleDialogButton(cancelButton);
        JButton completeButton = new JButton(COMPLETA_BUTTON);
        styleDialogButton(completeButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(completeButton);
        cancelButton.addActionListener(_ -> dialog.dispose());
        completeButton.addActionListener(_ -> {
            String selectedBoard = (String) boardComboBox.getSelectedItem();
            if (selectedBoard != null) {
                BoardPanel targetPanel = eligibleBoards.stream()
                        .filter(bp -> bp.getBoardDisplayName().equals(selectedBoard))
                        .findFirst().orElse(null);
                if (targetPanel != null) {
                    targetPanel.completaTuttiITasks();
                }
            }
            dialog.dispose();
        });
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        dialog.add(contentPanel);
        Dimension dialogSize = calculateBoardDialogSize();
        dialog.setSize(dialogSize);
        dialog.setLocationRelativeTo(boardsDisplayPanel);
        dialog.setVisible(true);
    }

    /**
     * Gestisce lo spostamento di un Task tra bacheche.
     */
    private void handleSpostaToDo() {
        if (!validateSpostaToDoPrerequisites()) {
            return;
        }
        
        JDialog dialog = createSpostaToDoDialog();
        JPanel contentPanel = createSpostaToDoContentPanel();
        JPanel formPanel = createSpostaToDoFormPanel();
        
        JComboBox<ToDoComboItem> todoComboBox = createTodoComboBox();
        JComboBox<Bacheca> destinazioneComboBox = createDestinazioneComboBox();
        
        setupSpostaToDoForm(formPanel, todoComboBox, destinazioneComboBox);
        contentPanel.add(formPanel, BorderLayout.CENTER);
        setupSpostaToDoButtonPanel(dialog, contentPanel, todoComboBox, destinazioneComboBox);
        
        dialog.add(contentPanel);
        showSpostaToDoDialog(dialog);
    }

    /**
     * Valida i prerequisiti per lo spostamento di un Task.
     * @return true se i prerequisiti sono soddisfatti, false altrimenti
     */
    private boolean validateSpostaToDoPrerequisites() {
        if (!controller.isUserLoggedIn() || controller.getAllToDosUtenteCorrente().isEmpty()) {
            showInfo("Nessun Task disponibile da spostare o utente non loggato.");
            return false;
        }
        List<Bacheca> bacheche = controller.getBachecheUtenteCorrente();
        if (bacheche.size() < 2) {
            showInfo("Non ci sono abbastanza bacheche per spostare un ToDo.");
            return false;
        }
        return true;
    }

    /**
     * Crea il dialogo per lo spostamento di un Task.
     * @return JDialog configurato
     */
    private JDialog createSpostaToDoDialog() {
        JDialog dialog = new JDialog(mainFrame, SPOSATA_TODO, true);
        dialog.setUndecorated(true);
        ((JComponent)dialog.getContentPane()).setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        return dialog;
    }

    /**
     * Crea il pannello contenuto per il dialogo di spostamento.
     * @return JPanel configurato
     */
    private JPanel createSpostaToDoContentPanel() {
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 0, 15));
        return contentPanel;
    }

    /**
     * Crea il pannello del form per lo spostamento.
     * @return JPanel configurato
     */
    private JPanel createSpostaToDoFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        return formPanel;
    }

    /**
     * Crea la combo box per la selezione del Task.
     * @return JComboBox configurata
     */
    private JComboBox<ToDoComboItem> createTodoComboBox() {
        JComboBox<ToDoComboItem> todoComboBox = new JComboBox<>();
        setupComboBoxStyle(todoComboBox);
        populateTodoComboBox(todoComboBox);
        return todoComboBox;
    }

    /**
     * Crea la combo box per la selezione della destinazione.
     * @return JComboBox configurata
     */
    private JComboBox<Bacheca> createDestinazioneComboBox() {
        JComboBox<Bacheca> destinazioneComboBox = new JComboBox<>();
        setupComboBoxStyle(destinazioneComboBox);
        setupDestinazioneComboBoxRenderer(destinazioneComboBox);
        return destinazioneComboBox;
    }

    /**
     * Configura lo stile di una combo box.
     * @param comboBox Combo box da configurare
     */
    private void setupComboBoxStyle(JComboBox<?> comboBox) {
        comboBox.setFont(FontManager.getRegular(14f));
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1),
                BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));
        comboBox.setBackground(Color.WHITE);
        comboBox.setUI(createCustomComboBoxUI());
    }

    /**
     * Crea un UI personalizzato per le combo box.
     * @return BasicComboBoxUI configurato
     */
    private BasicComboBoxUI createCustomComboBoxUI() {
        return new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton button = new JButton() {
                    @Override
                    public void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2d = (Graphics2D) g.create();
                        g2d.setColor(Color.BLACK);
                        int x = (getWidth() - 8) / 2;
                        int y = (getHeight() - 5) / 2;
                        int[] xPoints = {x, x + 8, x + 4};
                        int[] yPoints = {y, y, y + 5};
                        g2d.fillPolygon(xPoints, yPoints, 3);
                        g2d.dispose();
                    }
                };
                button.setOpaque(false);
                button.setFocusPainted(false);
                button.setBorder(BorderFactory.createEmptyBorder());
                button.setBackground(Color.WHITE);
                button.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent ignored) {
                        button.setBackground(Color.LIGHT_GRAY);
                    }
                    @Override
                    public void mouseExited(java.awt.event.MouseEvent ignored) {
                        button.setBackground(Color.WHITE);
                    }
                });
                return button;
            }
        };
    }

    /**
     * Popola la combo box dei Task con i Task disponibili.
     * @param todoComboBox Combo box da popolare
     */
    private void populateTodoComboBox(JComboBox<ToDoComboItem> todoComboBox) {
        List<Bacheca> bachecheUtente = controller.getBachecheUtenteCorrente();
        List<ToDo> allToDos = bachecheUtente.stream()
                .flatMap(b -> b.getTodos().stream())
                .filter(t -> t.getStato() != model.StatoToDo.COMPLETATO)
                .toList();
        for (ToDo t : allToDos) {
            String scadenza = t.getScadenza() != null
                    ? t.getScadenza().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    : "-";
            String label = t.getTitolo() + " [Scadenza il: " + scadenza + "]";
            todoComboBox.addItem(new ToDoComboItem(t, label));
        }
    }

    /**
     * Configura il renderer per la combo box destinazione.
     * @param destinazioneComboBox Combo box da configurare
     */
    private void setupDestinazioneComboBoxRenderer(JComboBox<Bacheca> destinazioneComboBox) {
        destinazioneComboBox.setRenderer(new javax.swing.plaf.basic.BasicComboBoxRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Bacheca b) {
                    setText(b.getTitoloDisplayName());
                }
                return this;
            }
        });
    }

    /**
     * Configura il form per lo spostamento del Task.
     * @param formPanel Pannello del form
     * @param todoComboBox Combo box ToDo
     * @param destinazioneComboBox Combo box destinazione
     */
    private void setupSpostaToDoForm(JPanel formPanel, JComboBox<ToDoComboItem> todoComboBox, JComboBox<Bacheca> destinazioneComboBox) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.insets = new Insets(0, 0, 5, 0);

        JLabel todoLabel = new JLabel("ToDo da spostare: *");
        todoLabel.setFont(FontManager.getRegular(13f));
        formPanel.add(todoLabel, gbc);
        formPanel.add(todoComboBox, gbc);

        JLabel destinazioneLabel = new JLabel("Bacheca di destinazione:");
        destinazioneLabel.setFont(FontManager.getRegular(13f));
        formPanel.add(destinazioneLabel, gbc);
        formPanel.add(destinazioneComboBox, gbc);

        setupTodoComboBoxListener(todoComboBox, destinazioneComboBox);
        if (todoComboBox.getItemCount() > 0) {
            todoComboBox.setSelectedIndex(0);
        }
    }

    /**
     * Configura il listener per la combo box Task.
     * @param todoComboBox Combo box ToDo
     * @param destinazioneComboBox Combo box destinazione
     */
    private void setupTodoComboBoxListener(JComboBox<ToDoComboItem> todoComboBox, JComboBox<Bacheca> destinazioneComboBox) {
        List<Bacheca> bacheche = controller.getBachecheUtenteCorrente();
        todoComboBox.addActionListener(_ -> {
            destinazioneComboBox.removeAllItems();
            ToDoComboItem selectedItem = (ToDoComboItem) todoComboBox.getSelectedItem();
            if (selectedItem != null) {
                for (Bacheca b : bacheche) {
                    if (selectedItem.todo.getBachecaId() == null || b.getId() != selectedItem.todo.getBachecaId()) {
                        destinazioneComboBox.addItem(b);
                    }
                }
            }
        });
    }

    /**
     * Configura il pannello dei pulsanti per lo spostamento.
     * @param dialog Dialogo di riferimento
     * @param contentPanel Pannello contenuto
     * @param todoComboBox Combo box ToDo
     * @param destinazioneComboBox Combo box destinazione
     */
    private void setupSpostaToDoButtonPanel(JDialog dialog, JPanel contentPanel, JComboBox<ToDoComboItem> todoComboBox, JComboBox<Bacheca> destinazioneComboBox) {
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        JButton cancelButton = new JButton(ANNULLA);
        styleDialogButton(cancelButton);
        JButton moveButton = new JButton("Sposta");
        styleDialogButton(moveButton);
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(moveButton);
        
        cancelButton.addActionListener(_ -> dialog.dispose());
        moveButton.addActionListener(_ -> {
            ToDoComboItem selectedItem = (ToDoComboItem) todoComboBox.getSelectedItem();
            Bacheca destBacheca = (Bacheca) destinazioneComboBox.getSelectedItem();
            if (selectedItem != null && destBacheca != null) {
                moveToDoToBoard(selectedItem.todo, destBacheca.getTitoloDisplayName());
            }
            dialog.dispose();
        });
        
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Mostra il dialogo di spostamento Task.
     * @param dialog Dialogo da mostrare
     */
    private void showSpostaToDoDialog(JDialog dialog) {
        Dimension dialogSize = calculateBoardDialogSize();
        dialog.setSize(dialogSize);
        dialog.setLocationRelativeTo(boardsDisplayPanel);
        dialog.setVisible(true);
    }

    // Helper per mostrare label custom nella combo dei Task
    private static class ToDoComboItem {
        public final ToDo todo;
        public final String label;
        public ToDoComboItem(ToDo todo, String label) {
            this.todo = todo;
            this.label = label;
        }
        @Override
        public String toString() { return label; }
    }

    /**
     * Mostra un messaggio informativo all'utente.
     * @param message Messaggio da mostrare
     */
    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, SPOSATA_TODO, JOptionPane.INFORMATION_MESSAGE);
    }



    /**
     * Esegue lo spostamento del Task nella bacheca di destinazione.
     * @param selectedToDo ToDo da spostare
     * @param selectedDestinationBoardName Nome della bacheca di destinazione
     */
    private void moveToDoToBoard(ToDo selectedToDo, String selectedDestinationBoardName) {
        try {
            boolean success = controller.cambiaBachecaToDo(selectedToDo, selectedDestinationBoardName);
            if (success) {
                loadUserBoards();
            } else {
                JOptionPane.showMessageDialog(this, "Impossibile spostare il Task. Controlla il log o assicurati che il Task sia nella bacheca di partenza corretta.", "Errore Spostamento", JOptionPane.ERROR_MESSAGE);
            }

        } catch (IllegalArgumentException _) {
            JOptionPane.showMessageDialog(this, "Nome bacheca non valido: " + selectedDestinationBoardName, ERRORE, JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Ricarica e visualizza tutte le bacheche dell'utente corrente.
     */
    public void loadUserBoards() {
        if (!controller.isUserLoggedIn()) {
            boardsDisplayPanel.removeAll(); boardPanels.clear();
            boardsDisplayPanel.revalidate(); boardsDisplayPanel.repaint();
            return;
        }
        boardsDisplayPanel.removeAll();
        boardPanels.clear();

        List<Bacheca> bachecheUtente = controller.getBachecheUtenteCorrente();

        if (bachecheUtente.isEmpty()) {
            boardsDisplayPanel.setLayout(new BorderLayout());
            JLabel noBoardsLabel = new JLabel("Nessuna bacheca disponibile. Clicca su 'Bacheca' > 'Crea' per iniziare!");
            noBoardsLabel.setFont(FontManager.getRegular(14f));
            noBoardsLabel.setHorizontalAlignment(SwingConstants.CENTER);
            boardsDisplayPanel.add(noBoardsLabel, BorderLayout.CENTER);
        } else {
            boardsDisplayPanel.setLayout(new GridLayout(0, 3, 20, 10));
            for (Bacheca bacheca : bachecheUtente) {
                BoardPanel bachecaPanel = new BoardPanel(bacheca.getTitoloDisplayName(), bacheca.getDescrizione(), controller, controller.getUtenteCorrente().getUsername(), bacheca.getTodos());
                boardsDisplayPanel.add(bachecaPanel);
                boardPanels.add(bachecaPanel);
            }
        }
        boardsDisplayPanel.revalidate();
        boardsDisplayPanel.repaint();
    }

    /**
     * Aggiorna il pannello di una bacheca selezionata.
     * @param boardDisplayName Nome visualizzato della bacheca da aggiornare
     */
    public void refreshSelectedBoardPanel(String boardDisplayName) {
        if (!controller.isUserLoggedIn()) return;
        Optional<BoardPanel> panelToRefresh = boardPanels.stream()
                .filter(bp -> bp.getBoardDisplayName().equalsIgnoreCase(boardDisplayName))
                .findFirst();
        panelToRefresh.ifPresent(BoardPanel::refreshToDoList);
    }

    /**
     * Crea un bottone di menu con stile personalizzato.
     * @param text Testo del bottone
     * @return JButton configurato
     */
    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FontManager.getRegular(15f));
        button.setForeground(new Color(200, 200, 200));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.setPreferredSize(new Dimension(
                (int) button.getPreferredSize().getWidth(),
                40
        ));
        button.setMinimumSize(button.getPreferredSize());
        button.setMaximumSize(button.getPreferredSize());

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent ignored) {
                button.setForeground(Color.WHITE);
            }
            @Override
            public void mouseExited(MouseEvent ignored) {
                button.setForeground(new Color(200, 200, 200));
            }
        });
        return button;
    }

    /**
     * Applica lo stile ai bottoni delle finestre di dialogo.
     * @param button Bottone da stilizzare
     */
    private void styleDialogButton(JButton button) {
        button.setFont(FontManager.getRegular(12f));
        button.setBackground(Color.BLACK);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(null);
        button.setMaximumSize(null);
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent ignored) {
                button.setBackground(Color.DARK_GRAY);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent ignored) {
                button.setBackground(Color.BLACK);
            }
        });
    }

    /**
     * Restituisce la lista dei pannelli bacheca attualmente visualizzati.
     * @return Lista non modificabile di BoardPanel
     */
    public List<BoardPanel> getBoardPanels() {
        return Collections.unmodifiableList(boardPanels);
    }

    /**
     * Mostra la finestra di dialogo con le informazioni sull'applicazione.
     */
    private void showInfoDialog() {
        JDialog infoDialog = new JDialog(SwingUtilities.getWindowAncestor(this), INFORMAZIONI, Dialog.ModalityType.APPLICATION_MODAL);
        infoDialog.setUndecorated(true);
        ((JComponent) infoDialog.getContentPane()).setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        infoDialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        infoDialog.setBackground(Color.WHITE);

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 0, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel iconLabel = createInfoIconLabel();
        contentPanel.add(iconLabel, gbc);

        JLabel infoLabel = createInfoTextLabel();
        gbc.insets = new Insets(10, 0, 5, 0);
        contentPanel.add(infoLabel, gbc);

        JButton closeButton = new JButton("Chiudi");
        styleDialogButton(closeButton);

        JPanel southButtonPanel = new JPanel(new BorderLayout());
        southButtonPanel.setOpaque(false);
        southButtonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        southButtonPanel.add(closeButton, BorderLayout.CENTER);

        closeButton.addActionListener(_ -> infoDialog.dispose());

        infoDialog.getContentPane().setLayout(new BorderLayout());
        infoDialog.getContentPane().setBackground(Color.WHITE);
        infoDialog.getContentPane().add(contentPanel, BorderLayout.CENTER);
        infoDialog.getContentPane().add(southButtonPanel, BorderLayout.SOUTH);

        boardsDisplayPanel.validate();
        Dimension dialogSize = calculateInfoDialogSize();
        Point dialogLocation = calculateInfoDialogLocation(dialogSize);
        infoDialog.setSize(dialogSize);
        infoDialog.setLocation(dialogLocation);
        infoDialog.setVisible(true);
    }

    /**
     * Crea la label dell'icona informazioni.
     * @return JLabel con icona o testo
     */
    private JLabel createInfoIconLabel() {
        JLabel iconLabel = new JLabel();
        try {
            URL iconURL = getClass().getResource("/Favicon.png");
            if (iconURL != null) {
                ImageIcon originalIcon = new ImageIcon(iconURL);
                Image image = originalIcon.getImage();
                Image scaledImage = image.getScaledInstance(60, 60, Image.SCALE_SMOOTH);
                iconLabel.setIcon(new ImageIcon(scaledImage));
                iconLabel.setText("");
            } else {
                logger.error("Icona dell'applicazione non trovata: /Favicon.png");
                iconLabel.setText("ⓘ");
                iconLabel.setFont(FontManager.getBold(60f));
            }
        } catch (Exception _) {
            logger.error("Errore durante il caricamento dell'icona informazioni");
            iconLabel.setText("ⓘ");
            iconLabel.setFont(FontManager.getBold(60f));
        }
        iconLabel.setForeground(Color.BLACK);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        return iconLabel;
    }

    /**
     * Crea la label con il testo informativo sull'applicazione.
     * @return JLabel con testo informativo
     */
    private JLabel createInfoTextLabel() {
        String infoText = "<html>"
                + "<div style='text-align:center;'>"
                + "<p style='text-align:left; margin:0; padding:0;'>"
                + "<b>Applicazione:</b> ToDoApp<br>"
                + "<b>Versione attuale:</b> 3.0<br>"
                + "<b>Sviluppata in:</b> Java+Swing<br>"
                + "<b>Sviluppatori:</b><br>"
                + "Vincenzo Oratore N86005092<br>"
                + "Mattia Pentagallo N86005235"
                + "</p></div></html>";
        JLabel infoLabel = new JLabel(infoText);
        infoLabel.setFont(FontManager.getRegular(16f));
        infoLabel.setForeground(Color.BLACK);
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        infoLabel.setVerticalAlignment(SwingConstants.TOP);
        return infoLabel;
    }

    /**
     * Calcola la dimensione della finestra di dialogo informazioni.
     * @return Dimensione preferita
     */
    private Dimension calculateInfoDialogSize() {
        int boardWidth = 0;
        int boardHeight = 0;
        if (!boardPanels.isEmpty()) {
            BoardPanel targetBoard = null;
            if (boardsDisplayPanel.getComponentCount() >= 2) {
                targetBoard = boardPanels.get(1);
            } else {
                targetBoard = boardPanels.get(0);
            }
            if (targetBoard != null) {
                targetBoard.validate();
                boardWidth = targetBoard.getWidth();
                boardHeight = targetBoard.getHeight();
            }
        }
        if (boardWidth == 0 || boardHeight == 0) {
            int hGap = 20;
            int numColumns = 3;
            boardWidth = (boardsDisplayPanel.getWidth() - (hGap * (numColumns - 1))) / numColumns;
            boardHeight = (int) (mainFrame.getHeight() * 0.6);
            if (boardWidth <= 0) boardWidth = 350;
            if (boardHeight <= 0) boardHeight = 500;
        }
        return new Dimension(boardWidth, boardHeight);
    }

    /**
     * Calcola la posizione della finestra di dialogo informazioni.
     * @param dialogSize Dimensione della finestra
     * @return Punto di posizionamento
     */
    private Point calculateInfoDialogLocation(Dimension dialogSize) {
        if (boardPanels.size() >= 2) {
            BoardPanel targetBoard = boardPanels.get(1);
            Point boardLocation = targetBoard.getLocationOnScreen();
            int x = boardLocation.x + (targetBoard.getWidth() - dialogSize.width) / 2;
            int y = boardLocation.y + (targetBoard.getHeight() - dialogSize.height) / 2;
            return new Point(x, y);
        } else {
            int centerX = mainFrame.getLocationOnScreen().x + (mainFrame.getWidth() - dialogSize.width) / 2;
            int centerY = mainFrame.getLocationOnScreen().y + (mainFrame.getHeight() - dialogSize.height) / 2;
            return new Point(centerX, centerY);
        }
    }

    /**
     * Calcola la dimensione preferita delle finestre di dialogo bacheca.
     * @return Dimensione preferita
     */
    private Dimension calculateBoardDialogSize() {
        int boardWidth = (boardsDisplayPanel.getWidth() - (20 * 2)) / 3;
        int boardHeight = 0;
        if (!boardPanels.isEmpty()) {
            BoardPanel referenceBoard = boardPanels.get(0);
            referenceBoard.validate();
            boardHeight = referenceBoard.getHeight();
        }
        if (boardWidth <= 0) boardWidth = 350;
        if (boardHeight <= 0) boardHeight = 400;
        return new Dimension(boardWidth, boardHeight);
    }

    /**
     * Mostra una dialog con le informazioni dell'utente loggato.
     */
    private void showAccountInfoDialog() {
        Utente utente = controller.getUtenteCorrente();
        JDialog dialog = new JDialog(mainFrame, "Informazioni Account", true);
        dialog.setUndecorated(true);
        ((JComponent)dialog.getContentPane()).setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 0, 15));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.insets = new Insets(0, 0, 5, 0);

        JLabel idLabel = new JLabel("ID:");
        idLabel.setFont(FontManager.getRegular(13f));
        formPanel.add(idLabel, gbc);
        JTextField idField = new JTextField(String.valueOf(utente.getId()));
        idField.setFont(FontManager.getRegular(14f));
        idField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1),
                BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));
        idField.setBackground(Color.WHITE);
        idField.setEditable(false);
        formPanel.add(idField, gbc);

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(FontManager.getRegular(13f));
        formPanel.add(usernameLabel, gbc);
        JTextField usernameField = new JTextField(utente.getUsername());
        usernameField.setFont(FontManager.getRegular(14f));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1),
                BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));
        usernameField.setBackground(Color.WHITE);
        usernameField.setEditable(false);
        formPanel.add(usernameField, gbc);

        contentPanel.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 5, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        // Nella dialog Informazioni Account:
        JButton cancelButton = new JButton(ANNULLA);
        styleDialogButton(cancelButton);
        JButton deleteButton = new JButton(ELIMINA_BUTTON);
        styleDialogButton(deleteButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(deleteButton);
        cancelButton.addActionListener(_ -> dialog.dispose());
        final boolean[] confirmingDelete = {false};
        deleteButton.addActionListener(_ -> {
            if (!confirmingDelete[0]) {
                deleteButton.setText("Sei sicuro?");
                confirmingDelete[0] = true;
            } else {
                boolean success = controller.eliminaUtenteCorrente();
                if (success) {
                    showAccountDeletedDialog();
                    mainFrame.dispose(); // Chiude l'applicazione
                } else {
                    showAccountDeletionErrorDialog();
                }
                dialog.dispose();
            }
        });
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        dialog.add(contentPanel);
        Dimension dialogSize = calculateBoardDialogSize();
        dialog.setSize(dialogSize);
        dialog.setLocationRelativeTo(boardsDisplayPanel);
        dialog.setVisible(true);
    }

    /**
     * Mostra la dialog di conferma eliminazione account riuscita.
     */
    private void showAccountDeletedDialog() {
        JDialog dialog = new JDialog(mainFrame, "Account eliminato", true);
        dialog.setUndecorated(true);
        ((JComponent)dialog.getContentPane()).setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 0, 15));

        JPanel formPanel = new JPanel(new BorderLayout());
        formPanel.setOpaque(false);

        JPanel textPanel = new JPanel(new GridBagLayout());
        textPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel messageLabel = new JLabel("Account eliminato con successo.");
        messageLabel.setFont(FontManager.getRegular(14f));
        textPanel.add(messageLabel, gbc);
        gbc.gridy++;
        
        JLabel closeLabel = new JLabel("L'applicazione verrà chiusa.");
        closeLabel.setFont(FontManager.getRegular(14f));
        textPanel.add(closeLabel, gbc);

        formPanel.add(textPanel, BorderLayout.CENTER);

        contentPanel.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        JButton closeButton = new JButton("Chiudi");
        closeButton.setFont(FontManager.getRegular(12f));
        closeButton.setBackground(Color.BLACK);
        closeButton.setForeground(Color.WHITE);
        closeButton.setOpaque(true);
        closeButton.setFocusPainted(false);
        closeButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(_ -> dialog.dispose());
        closeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent ignored) {
                closeButton.setBackground(Color.DARK_GRAY);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent ignored) {
                closeButton.setBackground(Color.BLACK);
            }
        });
        buttonPanel.add(closeButton, BorderLayout.CENTER);
        
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        dialog.add(contentPanel);
        
        Dimension dialogSize = calculateBoardDialogSize();
        dialog.setSize(dialogSize);
        dialog.setLocationRelativeTo(boardsDisplayPanel);
        dialog.setVisible(true);
    }

    /**
     * Mostra la dialog di errore per l'eliminazione account.
     */
    private void showAccountDeletionErrorDialog() {
        JDialog dialog = new JDialog(mainFrame, "Errore eliminazione", true);
        dialog.setUndecorated(true);
        ((JComponent)dialog.getContentPane()).setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 0, 15));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.insets = new Insets(0, 0, 5, 0);

        JLabel messageLabel = new JLabel("Errore durante l'eliminazione dell'account.");
        messageLabel.setFont(FontManager.getRegular(14f));
        formPanel.add(messageLabel, gbc);
        
        JLabel retryLabel = new JLabel("Riprova più tardi.");
        retryLabel.setFont(FontManager.getRegular(14f));
        formPanel.add(retryLabel, gbc);

        contentPanel.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        JButton okButton = new JButton("OK");
        styleDialogButton(okButton);
        okButton.addActionListener(_ -> dialog.dispose());
        buttonPanel.add(okButton);
        
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        dialog.add(contentPanel);
        
        Dimension dialogSize = calculateBoardDialogSize();
        dialog.setSize(dialogSize);
        dialog.setLocationRelativeTo(boardsDisplayPanel);
        dialog.setVisible(true);
    }
}