package gui;

import model.StatoToDo;
import model.ToDo;
import model.TitoloBacheca; // Assicura che l'import sia presente
import controller.Controller;
import model.Utente;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat; // Non più usato per LocalDate, ma può rimanere se serve altrove
import java.time.LocalDate;
import java.time.ZoneId; // Non usato direttamente qui, ma ToDo lo usa
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date; // Usato nel costruttore di ToDo da addNewToDo, ma ToDo usa LocalDate
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BoardPanel extends JPanel {
    private String boardDisplayName;
    private String boardDescriptionValue; // Campo per memorizzare la descrizione
    private Controller controller;
    private JPanel todoListPanel;
    private String currentUsername;
    private List<ToDo> todosCache;
    private Consumer<String> refreshDashboardCallback;
    private JLabel titleLabelComponent;
    private JLabel descriptionLabelComponent;

    public BoardPanel(String boardDisplayName, String boardDescription, Controller controller, String username, List<ToDo> initialToDos, Consumer<String> refreshCallback) {
        this.boardDisplayName = boardDisplayName;
        this.boardDescriptionValue = boardDescription; // Salva la descrizione
        this.controller = controller;
        this.currentUsername = username;
        this.todosCache = new ArrayList<>(initialToDos);
        this.refreshDashboardCallback = refreshCallback;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        setBackground(Color.WHITE);

        titleLabelComponent = new JLabel(this.boardDisplayName);
        titleLabelComponent.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabelComponent.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));

        descriptionLabelComponent = new JLabel();
        updateDescriptionLabelText(); // Imposta testo iniziale descrizione
        descriptionLabelComponent.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        descriptionLabelComponent.setForeground(Color.DARK_GRAY);
        descriptionLabelComponent.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));

        JPanel titleAndDescPanel = new JPanel();
        titleAndDescPanel.setLayout(new BoxLayout(titleAndDescPanel, BoxLayout.Y_AXIS));
        titleAndDescPanel.setOpaque(false);
        titleAndDescPanel.add(titleLabelComponent);
        titleAndDescPanel.add(descriptionLabelComponent);

        todoListPanel = new JPanel();
        todoListPanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(todoListPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(Color.WHITE);

        JButton shareToDoButton = createStyledButton("Condividi ToDo");
        shareToDoButton.addActionListener(e -> showShareToDoDialog()); // Chiamata al metodo

        JButton addToDoButton = createStyledButton("+ Nuovo ToDo");
        addToDoButton.addActionListener(e -> addNewToDo());

        JPanel topControlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5,0));
        topControlsPanel.setOpaque(false);
        topControlsPanel.add(shareToDoButton);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.add(titleAndDescPanel, BorderLayout.WEST);
        headerPanel.add(topControlsPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(addToDoButton, BorderLayout.SOUTH);

        refreshToDoListDisplay();
    }

    private void updateDescriptionLabelText() {
        if (this.boardDescriptionValue != null && !this.boardDescriptionValue.trim().isEmpty()) {
            descriptionLabelComponent.setText("<html><body style='width: 200px;'>" + escapeHtml(this.boardDescriptionValue) + "</body></html>");
        } else {
            descriptionLabelComponent.setText("<html><i style='color:gray;'>Nessuna descrizione per questa bacheca.</i></html>");
        }
    }

    private void addNewToDo() {
        if (!controller.isUserLoggedIn()) return;

        JDialog newToDoDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Nuovo ToDo per " + boardDisplayName, true);
        newToDoDialog.setLayout(new BorderLayout(10,10));
        newToDoDialog.setSize(450, 450);
        newToDoDialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        JTextField titoloField = new JTextField(25);
        JTextArea descrizioneArea = new JTextArea(4, 25);
        JScrollPane descrizioneScrollPane = new JScrollPane(descrizioneArea);
        descrizioneArea.setLineWrap(true);
        descrizioneArea.setWrapStyleWord(true);
        JTextField scadenzaField = new JTextField(10);
        scadenzaField.setText(LocalDate.now().plusDays(1).toString());
        JTextField coloreField = new JTextField(7);
        coloreField.setText("#FFFFFF");
        JTextField urlField = new JTextField(25);
        Image immagineInput = null;

        int y = 0;
        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Titolo [Obbligatorio]:"), gbc);
        gbc.gridx = 1; gbc.gridy = y++; gbc.weightx = 1.0; formPanel.add(titoloField, gbc);
        gbc.weightx = 0;

        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Descrizione [Obbligatoria]:"), gbc);
        gbc.gridx = 1; gbc.gridy = y++; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0;
        formPanel.add(descrizioneScrollPane, gbc);
        gbc.weightx = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weighty = 0;

        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Scadenza [YYYY-MM-DD]:"), gbc);
        gbc.gridx = 1; gbc.gridy = y++; formPanel.add(scadenzaField, gbc);

        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Colore Sfondo [#RRGGBB]:"), gbc);
        gbc.gridx = 1; gbc.gridy = y++; formPanel.add(coloreField, gbc);

        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("URL [Opzionale]:"), gbc);
        gbc.gridx = 1; gbc.gridy = y++; formPanel.add(urlField, gbc);

        JButton saveButton = new JButton("Salva");
        JButton cancelButton = new JButton("Annulla");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        newToDoDialog.add(formPanel, BorderLayout.CENTER);
        newToDoDialog.add(buttonPanel, BorderLayout.SOUTH);

        saveButton.addActionListener(ae -> {
            String titolo = titoloField.getText().trim();
            String descrizione = descrizioneArea.getText().trim();
            String scadenzaStr = scadenzaField.getText().trim();
            String coloreInput = coloreField.getText().trim();
            String urlInput = urlField.getText().trim();

            if (titolo.isEmpty()) { JOptionPane.showMessageDialog(newToDoDialog, "Il titolo è obbligatorio.", "Errore", JOptionPane.ERROR_MESSAGE); return; }
            if (descrizione.isEmpty()) { JOptionPane.showMessageDialog(newToDoDialog, "La descrizione è obbligatoria.", "Errore", JOptionPane.ERROR_MESSAGE); return; }

            LocalDate scadenzaDate;
            try {
                scadenzaDate = LocalDate.parse(scadenzaStr);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(newToDoDialog, "Formato data scadenza non valido. Usare YYYY-MM-DD.", "Errore", JOptionPane.ERROR_MESSAGE); return;
            }

            String coloreValidato = "#FFFFFF";
            if (coloreInput.matches("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")) {
                coloreValidato = coloreInput;
            } else if (!coloreInput.isEmpty() && !coloreInput.equals("#")){
                JOptionPane.showMessageDialog(newToDoDialog, "Formato colore non valido. Usare #RRGGBB. Verrà usato il bianco.", "Avviso Colore", JOptionPane.WARNING_MESSAGE);
            }

            Optional<ToDo> toDoCreatoOpt = controller.creaToDo(this.boardDisplayName, titolo, descrizione, scadenzaDate, coloreValidato, urlInput, immagineInput);

            if (toDoCreatoOpt.isPresent()) {
                refreshToDoList();
                newToDoDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(newToDoDialog, "Errore durante la creazione del ToDo.", "Errore Creazione", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(ae -> newToDoDialog.dispose());
        newToDoDialog.setVisible(true);
    }

    // --- DEFINIZIONE DEL METODO showShareToDoDialog ---
    private void showShareToDoDialog() {
        if (!controller.isUserLoggedIn() || todosCache.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nessun ToDo in questa bacheca da condividere o utente non loggato.", "Condividi ToDo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        List<ToDo> shareableToDos = todosCache.stream()
                .filter(t -> t.getAutore() != null && t.getAutore().getUsername().equalsIgnoreCase(currentUsername))
                .collect(Collectors.toList());

        if(shareableToDos.isEmpty()){
            JOptionPane.showMessageDialog(this, "Non ci sono ToDo creati da te in questa bacheca che puoi condividere.", "Condividi ToDo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        ToDo[] todoArray = shareableToDos.toArray(new ToDo[0]);
        ToDo selectedToDo = (ToDo) JOptionPane.showInputDialog(
                this, "Scegli un ToDo (creato da te) da condividere:", "Condividi ToDo",
                JOptionPane.PLAIN_MESSAGE, null, todoArray, todoArray[0]);

        if (selectedToDo == null) return;

        String destinatarioUsername = JOptionPane.showInputDialog(this, "Username dell'utente con cui condividere:");
        if (destinatarioUsername == null || destinatarioUsername.trim().isEmpty()) return;

        destinatarioUsername = destinatarioUsername.trim();

        if (destinatarioUsername.equalsIgnoreCase(currentUsername)) {
            JOptionPane.showMessageDialog(this, "Non puoi condividere un ToDo con te stesso.", "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String[] nomiBachecheStandard = java.util.Arrays.stream(TitoloBacheca.values())
                .map(TitoloBacheca::getDisplayName)
                .toArray(String[]::new);
        String bachecaDestinazioneDisplayName = (String) JOptionPane.showInputDialog(this,
                "Scegli la bacheca del destinatario su cui condividere il ToDo:",
                "Bacheca Destinazione", JOptionPane.PLAIN_MESSAGE, null,
                nomiBachecheStandard, nomiBachecheStandard[0]);

        if (bachecaDestinazioneDisplayName == null || bachecaDestinazioneDisplayName.trim().isEmpty()) {
            return;
        }

        boolean shareSuccess = controller.condividiToDo(selectedToDo, destinatarioUsername, bachecaDestinazioneDisplayName);
        if (shareSuccess) {
            JOptionPane.showMessageDialog(this, "ToDo '" + selectedToDo.getTitolo() + "' condiviso con successo con '" + destinatarioUsername + "' sulla sua bacheca '" + bachecaDestinazioneDisplayName + "'.",
                    "Condivisione Riuscita", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Impossibile condividere il ToDo.\nVerifica che l'utente destinatario esista e che le condizioni per la condivisione siano soddisfatte (es. limiti bacheche destinatario).",
                    "Errore Condivisione", JOptionPane.ERROR_MESSAGE);
        }
    }
    // --- FINE DEFINIZIONE ---


    public void aggiornaNomeVisualizzato(String nuovoNomeVisualizzato) {
        titleLabelComponent.setText(nuovoNomeVisualizzato);
    }

    public void aggiornaDescrizioneVisualizzata(String nuovaDescrizione) {
        this.boardDescriptionValue = nuovaDescrizione;
        updateDescriptionLabelText();
    }

    public String getBoardCanonicalName() {
        return boardDisplayName;
    }

    public String getBoardDisplayName() {
        return titleLabelComponent.getText();
    }

    public void refreshToDoList() {
        if (!controller.isUserLoggedIn()) return;
        this.todosCache = controller.getToDosPerBacheca(this.boardDisplayName);
        refreshToDoListDisplay();
    }

    private void refreshToDoListDisplay() {
        todoListPanel.removeAll();

        if (todosCache.isEmpty()) {
            todoListPanel.setLayout(new GridBagLayout());
            JLabel emptyLabel = new JLabel("Nessun ToDo in questa bacheca.");
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            emptyLabel.setForeground(Color.GRAY);
            todoListPanel.add(emptyLabel);
        } else {
            todoListPanel.setLayout(new BoxLayout(todoListPanel, BoxLayout.Y_AXIS));
            for (ToDo todo : todosCache) {
                JPanel todoItemPanel = new JPanel(new BorderLayout(5, 2));
                todoItemPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(200, 200, 200)),
                        BorderFactory.createEmptyBorder(8, 8, 8, 8)
                ));

                Color backgroundColor = Color.WHITE;
                try {
                    String hexColor = todo.getColore();
                    if (hexColor != null && !hexColor.trim().isEmpty()) {
                        if (!hexColor.startsWith("#")) hexColor = "#" + hexColor;
                        if (hexColor.matches("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")) {
                            backgroundColor = Color.decode(hexColor);
                        }
                    }
                } catch (NumberFormatException ex) { /* Usa default */ }
                todoItemPanel.setBackground(backgroundColor);

                String titleText = todo.getTitolo();
                if (todo.getStato() == StatoToDo.COMPLETATO) {
                    titleText = "<html><strike>" + escapeHtml(todo.getTitolo()) + "</strike></html>";
                } else {
                    titleText = escapeHtml(todo.getTitolo());
                }
                JLabel titleLabel = new JLabel(titleText);
                titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

                String sharedInfo = "";
                Utente autoreToDo = todo.getAutore();
                if (autoreToDo != null && !currentUsername.equalsIgnoreCase(autoreToDo.getUsername())) {
                    sharedInfo = " <i style='color:gray;font-size:smaller;'>(Condiviso da: " + escapeHtml(autoreToDo.getUsername()) + ")</i>";
                }

                if (!sharedInfo.isEmpty()) {
                    if (titleLabel.getText().startsWith("<html>")) {
                        titleLabel.setText(titleLabel.getText().replace("</html>", sharedInfo + "</html>"));
                    } else {
                        titleLabel.setText("<html>" + titleLabel.getText() + sharedInfo + "</html>");
                    }
                }

                JTextArea descAreaToDo = new JTextArea(todo.getDescrizione());
                descAreaToDo.setEditable(false); descAreaToDo.setLineWrap(true); descAreaToDo.setWrapStyleWord(true);
                descAreaToDo.setFont(new Font("Segoe UI", Font.PLAIN, 12)); descAreaToDo.setOpaque(false);

                JLabel scadenzaLabel = new JLabel("Scade: " + (todo.getScadenza() != null ? todo.getScadenza().toString() : "N/D"));
                scadenzaLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));

                JLabel creazioneLabel = new JLabel("Creato il: " + todo.getCreazione().toString());
                creazioneLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                creazioneLabel.setForeground(Color.DARK_GRAY);

                JLabel statoLabel = new JLabel("Stato: " + todo.getStato().getDisplayName());
                statoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));

                JPanel detailsPanel = new JPanel(new GridLayout(0,1,0,2));
                detailsPanel.setOpaque(false);
                detailsPanel.add(scadenzaLabel);
                detailsPanel.add(statoLabel);
                detailsPanel.add(creazioneLabel);
                if (todo.getUrl() != null && !todo.getUrl().isEmpty()) {
                    JLabel urlLabel = new JLabel("<html>URL: <a href='" + escapeHtml(todo.getUrl()) + "'>" + escapeHtml(todo.getUrl()) + "</a></html>");
                    urlLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                    urlLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    urlLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                            try {
                                Desktop.getDesktop().browse(new java.net.URI(todo.getUrl()));
                            } catch (Exception ex) {
                                System.err.println("Impossibile aprire URL: " + todo.getUrl() + " - " + ex.getMessage());
                            }
                        }
                    });
                    detailsPanel.add(urlLabel);
                }

                JCheckBox completeCheckbox = new JCheckBox("Completato");
                completeCheckbox.setOpaque(false);
                completeCheckbox.setSelected(todo.getStato() == StatoToDo.COMPLETATO);
                final ToDo currentTodo = todo;
                completeCheckbox.addActionListener(e -> {
                    StatoToDo nuovoStato = completeCheckbox.isSelected() ? StatoToDo.COMPLETATO : StatoToDo.NON_COMPLETATO;
                    controller.modificaToDo(currentTodo, currentTodo.getTitolo(), currentTodo.getDescrizione(),
                            currentTodo.getScadenza(), nuovoStato, currentTodo.getColore(),
                            currentTodo.getUrl(), currentTodo.getImmagine());
                    refreshDashboardCallback.accept(this.boardDisplayName);
                });

                JButton deleteButton = new JButton("X");
                deleteButton.setToolTipText("Elimina ToDo");
                deleteButton.setMargin(new Insets(1, 4, 1, 4));
                deleteButton.addActionListener(e -> {
                    int confirm = JOptionPane.showConfirmDialog(this, "Eliminare ToDo '" + currentTodo.getTitolo() + "'?", "Conferma Eliminazione", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        if (controller.eliminaToDo(currentTodo)) {
                            refreshToDoList();
                        } else {
                            JOptionPane.showMessageDialog(this, "Impossibile eliminare il ToDo.", "Errore", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });

                JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 0));
                actionPanel.setOpaque(false);
                actionPanel.add(completeCheckbox);
                if(todo.getAutore() != null && todo.getAutore().getUsername().equalsIgnoreCase(currentUsername)){
                    actionPanel.add(deleteButton);
                }

                JPanel titleActionPanel = new JPanel(new BorderLayout());
                titleActionPanel.setOpaque(false);
                titleActionPanel.add(titleLabel, BorderLayout.CENTER);
                titleActionPanel.add(actionPanel, BorderLayout.EAST);

                todoItemPanel.add(titleActionPanel, BorderLayout.NORTH);
                todoItemPanel.add(descAreaToDo, BorderLayout.CENTER);
                todoItemPanel.add(detailsPanel, BorderLayout.SOUTH);

                todoListPanel.add(todoItemPanel);
                todoListPanel.add(Box.createVerticalStrut(8));
            }
        }
        todoListPanel.revalidate();
        todoListPanel.repaint();
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    public void completaTuttiITasks() {
        if (!controller.isUserLoggedIn()) return;
        for (ToDo todo : this.todosCache) {
            if (todo.getStato() != StatoToDo.COMPLETATO) {
                controller.modificaToDo(todo, todo.getTitolo(), todo.getDescrizione(),
                        todo.getScadenza(), StatoToDo.COMPLETATO, todo.getColore(),
                        todo.getUrl(), todo.getImmagine());
            }
        }
        refreshToDoList();
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setFocusPainted(false);
        button.setBackground(new Color(240, 245, 255));
        button.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        button.setOpaque(true);
        return button;
    }
}