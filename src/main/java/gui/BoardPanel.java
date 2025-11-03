// File: src/main/java/gui/BoardPanel.java
package gui;

import controller.Controller;
import model.Bacheca;
import model.StatoToDo;
import model.ToDo;
import model.TitoloBacheca;
import model.Utente;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pannello grafico Swing che rappresenta una singola bacheca di Task.
 * Gestisce la visualizzazione, la modifica e le azioni sui Task di una bacheca.
 */
public class BoardPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(BoardPanel.class);
    private String boardDisplayName;
    private TitoloBacheca boardTitleEnum;
    private String boardDescriptionValue;
    private transient Controller controller;
    private JPanel todoListPanel;
    private String currentUsername;
    private transient List<ToDo> todosCache;
    private JLabel titleLabelComponent;
    private JLabel descriptionLabelComponent;
    private JScrollPane scrollPane;

    private transient Image immagineSelezionataGlobal = null;

    private static final int TODO_IMAGE_SQUARE_SIZE = 140;

    private static final DateTimeFormatter ITALIAN_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private static final Border BORDER_NORMAL = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8));
    private static final Border BORDER_ERROR = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.RED, 2),
            BorderFactory.createEmptyBorder(4, 7, 4, 7));

    private static final String BIANCO = "Bianco";
    private static final Map<String, String> COLOR_PALETTE = new LinkedHashMap<>();
    static {
        COLOR_PALETTE.put(BIANCO, "#FFFFFF");
        COLOR_PALETTE.put("Grigio Chiaro", "#F0F0F0");
        COLOR_PALETTE.put("Azzurro", "#ADD8E6");
        COLOR_PALETTE.put("Verde Chiaro", "#90EE90");
        COLOR_PALETTE.put("Rosa", "#FFC0CB");
        COLOR_PALETTE.put("Arancione", "#FFA500");
        COLOR_PALETTE.put("Rosso", "#FF0000");
    }
    private static final String ANNULLA = "Annulla";
    private static final String TITOLO_FIELD = "titoloField";
    private static final String DESCRIZIONE_AREA = "descrizioneArea";
    private static final String SCADENZA_FIELD = "scadenzaField";
    private static final String COLORE_COMBOBOX = "coloreComboBox";
    private static final String URL_FIELD = "urlField";
    private static final String HTML_CLOSE = "</html>";
    private static final String ACTIONS = "actions";
    private static final String IMMAGINE = "Immagine:";
    private static final String ERRORE = "Errore";
    private static final String CONFIRMING = "confirming";
    private static final String ELIMINA = "Elimina";
    private static final String CONDIVIDI = "Condividi";

    private boolean showCompleted = false;

    // Inner classes (SquareWrapperPanel, ImagePreviewLabel, LargeColorIcon, FormFields)
    /**
     * Pannello quadrato per il layout delle immagini Task.
     */
    private static class SquareWrapperPanel extends JPanel {
        public SquareWrapperPanel(LayoutManager layout) {
            super(layout);
        }

        @Override
        public void setBounds(int x, int y, int width, int height) {
            super.setBounds(x, y, width, width);
        }
    }

    /**
     * Label per l'anteprima delle immagini dei Task.
     */
    private static class ImagePreviewLabel extends JLabel {
        private transient Image image;

        public ImagePreviewLabel(String text) {
            super(text);
        }

        public void setImage(Image image) {
            this.image = image;
            this.repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());

            if (image != null) {
                int panelWidth = getWidth();
                int panelHeight = getHeight();
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                int imgWidth = image.getWidth(null);
                int imgHeight = image.getHeight(null);

                if (imgWidth <= 0 || imgHeight <= 0) {
                    g2d.dispose();
                    super.paintComponent(g);
                    return;
                }

                double imgAspect = (double) imgWidth / imgHeight;
                double panelAspect = (double) panelWidth / panelHeight;
                int drawX;
                int drawY;
                int drawW;
                int drawH;

                if (imgAspect > panelAspect) {
                    drawW = panelWidth;
                    drawH = (int) (panelWidth / imgAspect);
                    drawX = 0;
                    drawY = (panelHeight - drawH) / 2;
                } else {
                    drawH = panelHeight;
                    drawW = (int) (panelHeight * imgAspect);
                    drawY = 0;
                    drawX = (panelWidth - drawW) / 2;
                }
                g2d.drawImage(image, drawX, drawY, drawW, drawH, null);
                g2d.dispose();
            } else {
                super.paintComponent(g);
            }
        }
    }

    /**
     * Icona colorata di grandi dimensioni per la selezione del colore.
     */
    private static class LargeColorIcon implements Icon {
        private final Color color;
        private static final int SIZE = 22;

        public LargeColorIcon(Color color) {
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            int yCentered = y + (c.getHeight() - SIZE) / 2;
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setColor(color);
            g2d.fillRect(x, yCentered, SIZE, SIZE);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(x, yCentered, SIZE - 1, SIZE - 1);
            g2d.dispose();
        }

        @Override
        public int getIconWidth() {
            return SIZE;
        }

        @Override
        public int getIconHeight() {
            return getIconWidth();
        }
    }

    /**
     * Contenitore per i campi del form Task.
     */
    private static class FormFields {
        JTextField titoloField;
        JTextField descrizioneField;
        JTextField scadenzaField;
        JComboBox<String> coloreComboBox;
        JTextField urlField;
        ImagePreviewLabel imagePreviewLabel;
        JButton deleteImageButton;

        FormFields(JTextField titoloField, JTextField descrizioneField, JTextField scadenzaField,
                   JComboBox<String> coloreComboBox, JTextField urlField, ImagePreviewLabel imagePreviewLabel,
                   JButton deleteImageButton) {
            this.titoloField = titoloField;
            this.descrizioneField = descrizioneField;
            this.scadenzaField = scadenzaField;
            this.coloreComboBox = coloreComboBox;
            this.urlField = urlField;
            this.imagePreviewLabel = imagePreviewLabel;
            this.deleteImageButton = deleteImageButton;
        }
    }


    /**
     * Crea un nuovo pannello per una bacheca.
     * @param boardDisplayName Nome visualizzato della bacheca
     * @param boardDescription Descrizione della bacheca
     * @param controller Controller dell'applicazione
     * @param username Username dell'utente corrente
     * @param initialToDos Lista iniziale di ToDo da visualizzare
     */
    public BoardPanel(String boardDisplayName, String boardDescription, Controller controller, String username, List<ToDo> initialToDos) {
        this.boardDisplayName = boardDisplayName;
        try {
            this.boardTitleEnum = TitoloBacheca.fromDisplayName(boardDisplayName);
        } catch (IllegalArgumentException _) {
            logger.error("BoardPanel: Titolo bacheca non valido al caricamento: {}", boardDisplayName);
            this.boardTitleEnum = null;
        }
        this.boardDescriptionValue = boardDescription;
        this.controller = controller;
        this.currentUsername = username;
        this.todosCache = new ArrayList<>(initialToDos != null ? initialToDos : List.of());

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        setOpaque(true);
        setBackground(Color.WHITE);

        titleLabelComponent = new JLabel(this.boardDisplayName);
        titleLabelComponent.setFont(FontManager.getRegular(16f));

        descriptionLabelComponent = new JLabel();
        updateDescriptionLabelText();
        descriptionLabelComponent.setFont(FontManager.getRegular(12f));
        descriptionLabelComponent.setForeground(Color.BLACK);

        JPanel titleAndDescPanel = new JPanel();
        titleAndDescPanel.setLayout(new BoxLayout(titleAndDescPanel, BoxLayout.Y_AXIS));
        titleAndDescPanel.setOpaque(false);
        titleAndDescPanel.add(titleLabelComponent);
        titleAndDescPanel.add(descriptionLabelComponent);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        headerPanel.setOpaque(false);
        headerPanel.add(titleAndDescPanel, BorderLayout.CENTER);

        JPanel southButtonPanel = new JPanel(new BorderLayout());
        southButtonPanel.setOpaque(true);
        southButtonPanel.setBackground(Color.WHITE);
        southButtonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JButton addToDoButton = new JButton("+ ToDo");
        styleActionButton(addToDoButton);
        addToDoButton.addActionListener(_ -> addNewToDo());
        southButtonPanel.add(addToDoButton, BorderLayout.CENTER);

        todoListPanel = new JPanel();
        todoListPanel.setBackground(Color.WHITE);
        this.scrollPane = new JScrollPane(todoListPanel);
        this.scrollPane.setBorder(null);
        this.scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        this.scrollPane.setPreferredSize(new Dimension(0, 400));
        this.scrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        this.scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));

        add(headerPanel, BorderLayout.NORTH);
        add(this.scrollPane, BorderLayout.CENTER);
        add(southButtonPanel, BorderLayout.SOUTH);

        refreshToDoList();
    }

    /**
     * Apre la finestra di dialogo per aggiungere un nuovo Task alla bacheca.
     */
    public void addNewToDo() {
        if (!controller.isUserLoggedIn()) return;
        immagineSelezionataGlobal = null;

        JDialog newToDoDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), true);
        newToDoDialog.setUndecorated(true);
        ((JComponent) newToDoDialog.getContentPane()).setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        newToDoDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel formPanel = createToDoFormPanel(null);

        JButton saveButton = new JButton("Crea");
        styleActionButton(saveButton);

        JButton cancelButton = new JButton(ANNULLA);
        styleActionButton(cancelButton);

        saveButton.addActionListener(_ -> handleSaveNewToDo(formPanel, newToDoDialog));
        cancelButton.addActionListener(_ -> newToDoDialog.dispose());

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        newToDoDialog.getContentPane().setLayout(new BorderLayout());
        newToDoDialog.getContentPane().setBackground(Color.WHITE);
        newToDoDialog.add(formPanel, BorderLayout.CENTER);
        newToDoDialog.add(buttonPanel, BorderLayout.SOUTH);

        newToDoDialog.setBounds(this.getLocationOnScreen().x, this.getLocationOnScreen().y, this.getWidth(), this.getHeight());
        newToDoDialog.setVisible(true);
    }

    private void handleSaveNewToDo(JPanel formPanel, JDialog dialog) {
        JTextField titoloField = (JTextField) findComponentByName(formPanel, TITOLO_FIELD);
        JTextField descrizioneField = (JTextField) findComponentByName(formPanel, DESCRIZIONE_AREA);
        JTextField scadenzaField = (JTextField) findComponentByName(formPanel, SCADENZA_FIELD);
        JComboBox<?> coloreComboBox = (JComboBox<?>) findComponentByName(formPanel, COLORE_COMBOBOX);
        JTextField urlField = (JTextField) findComponentByName(formPanel, URL_FIELD);

        if (titoloField == null || descrizioneField == null || scadenzaField == null || coloreComboBox == null || urlField == null) {
            logger.error("BoardPanel: Errore interno, uno o più campi del form non trovati (handleSaveNewToDo)");
            JOptionPane.showMessageDialog(dialog, ERRORE + ": Errore interno nel form.", ERRORE, JOptionPane.ERROR_MESSAGE);
            return;
        }
        titoloField.setBorder(BORDER_NORMAL);
        descrizioneField.setBorder(BORDER_NORMAL);
        scadenzaField.setBorder(BORDER_NORMAL);

        if (!validateToDoFields(titoloField, descrizioneField)) return;

        try {
            LocalDate scadenzaDate = parseScadenzaDate(scadenzaField, dialog);
            if (scadenzaDate == null && !scadenzaField.getText().trim().isEmpty()) return;

            String titolo = titoloField.getText().trim();
            String descrizione = descrizioneField.getText().trim();
            String coloreInput = (String) coloreComboBox.getSelectedItem();
            String urlInput = urlField.getText().trim();
            String coloreHex = COLOR_PALETTE.get(coloreInput);

            Optional<ToDo> toDoCreatoOpt = controller.creaToDo(this.boardDisplayName, titolo, descrizione, scadenzaDate, coloreHex, urlInput, immagineSelezionataGlobal);
            if (toDoCreatoOpt.isPresent()) {
                refreshToDoList();
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Errore durante la creazione del ToDo. Controlla il log per dettagli.", ERRORE, JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception _) {
            JOptionPane.showMessageDialog(dialog, "Errore generico nel form.", ERRORE, JOptionPane.ERROR_MESSAGE);
            logger.error("BoardPanel: Errore generico nel form.");
        }
    }

    /**
     * Valida i campi obbligatori del form Task.
     * Evidenzia i campi vuoti con un bordo rosso.
     * @param titoloField Campo titolo
     * @param descrizioneField Campo descrizione
     * @return true se i campi sono validi, false altrimenti
     */
    private boolean validateToDoFields(JTextField titoloField, JTextField descrizioneField) {
        boolean isValid = true;
        if (titoloField.getText().trim().isEmpty()) {
            titoloField.setBorder(BORDER_ERROR);
            isValid = false;
        }
        if (descrizioneField.getText().trim().isEmpty()) {
            descrizioneField.setBorder(BORDER_ERROR);
            isValid = false;
        }
        return isValid;
    }

    /**
     * Effettua il parsing della data di scadenza dal campo testo.
     * Mostra un messaggio di errore se la data non è valida.
     * @param scadenzaField Campo data scadenza
     * @param dialog Dialogo di riferimento per i messaggi
     * @return LocalDate se valida, null altrimenti
     */
    private LocalDate parseScadenzaDate(JTextField scadenzaField, JDialog dialog) {
        String scadenzaStr = scadenzaField.getText().trim();
        if (scadenzaStr.isEmpty()) return null;
        try {
            LocalDate scadenzaDate = LocalDate.parse(scadenzaStr, ITALIAN_DATE_FORMATTER);
            if (scadenzaDate.isBefore(LocalDate.now())) {
                JOptionPane.showMessageDialog(dialog, "La data di scadenza non può essere nel passato.", ERRORE, JOptionPane.ERROR_MESSAGE);
                scadenzaField.setBorder(BORDER_ERROR);
                return null;
            }
            return scadenzaDate;
        } catch (DateTimeParseException _) {
            JOptionPane.showMessageDialog(dialog, "Formato data scadenza non valido. Usare GG-MM-AAAA.", ERRORE, JOptionPane.ERROR_MESSAGE);
            scadenzaField.setBorder(BORDER_ERROR);
            return null;
        }
    }

    /**
     * Aggiorna la visualizzazione della lista Task nel pannello.
     * Mantiene la posizione dello scroll.
     */
    private void refreshToDoListDisplay() {
        int scrollPosition = this.scrollPane.getVerticalScrollBar().getValue();
        todoListPanel.removeAll();

        if (todosCache == null || todosCache.isEmpty()) {
            setupEmptyToDoListPanel();
        } else {
            setupFilledToDoListPanel();
        }
        todoListPanel.revalidate();
        todoListPanel.repaint();
        this.scrollPane.getVerticalScrollBar().setValue(scrollPosition);
    }

    /**
     * Imposta la visualizzazione per una lista Task vuota.
     */
    private void setupEmptyToDoListPanel() {
        todoListPanel.setLayout(new GridBagLayout());
        JLabel emptyLabel = new JLabel("Nessun ToDo in questa bacheca.");
        emptyLabel.setFont(FontManager.getRegular(13f));
        emptyLabel.setForeground(Color.BLACK);
        todoListPanel.add(emptyLabel);
    }

    /**
     * Imposta la visualizzazione per una lista Task piena.
     * Gestisce la suddivisione tra completati e non completati.
     */
    private void setupFilledToDoListPanel() {
        todoListPanel.setLayout(new BoxLayout(todoListPanel, BoxLayout.Y_AXIS));
        todoListPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        final LocalDate oggi = LocalDate.now();
        
        List<ToDo> nonCompletati = new ArrayList<>();
        List<ToDo> completati = new ArrayList<>();
        separateCompletedAndNonCompletedTasks(nonCompletati, completati);
        
        addNonCompletedTasks(nonCompletati, oggi);
        addCompletedTasksSection(completati, oggi);
    }

    /**
     * Separa i Task in completati e non completati.
     * @param nonCompletati Lista per i Task non completati
     * @param completati Lista per i Task completati
     */
    private void separateCompletedAndNonCompletedTasks(List<ToDo> nonCompletati, List<ToDo> completati) {
        for (ToDo t : todosCache) {
            if (t.getStato() == StatoToDo.COMPLETATO) {
                completati.add(t);
            } else {
                nonCompletati.add(t);
            }
        }
    }

    /**
     * Aggiunge i Task non completati al pannello.
     * @param nonCompletati Lista dei Task non completati
     * @param oggi Data odierna per il calcolo priorità
     */
    private void addNonCompletedTasks(List<ToDo> nonCompletati, LocalDate oggi) {
        int pos = 1;
        for (int i = 0; i < nonCompletati.size(); i++) {
            ToDo t = nonCompletati.get(i);
            JPanel todoItemPanel = createToDoItemPanel(t, pos++, oggi);
            todoListPanel.add(todoItemPanel);
            if (i < nonCompletati.size() - 1) {
                todoListPanel.add(Box.createVerticalStrut(8));
        }
    }
    }

    /**
     * Aggiunge la sezione dei Task completati al pannello.
     * @param completati Lista dei Task completati
     * @param oggi Data odierna per il calcolo priorità
     */
    private void addCompletedTasksSection(List<ToDo> completati, LocalDate oggi) {
        if (completati.isEmpty()) {
            return;
        }
        
        JButton toggleCompletedButton = createToggleCompletedButton();
        todoListPanel.add(Box.createVerticalStrut(10));
        todoListPanel.add(toggleCompletedButton);
        
        if (showCompleted) {
            addCompletedTasksList(completati, oggi);
        }
    }

    /**
     * Crea il pulsante per mostrare/nascondere i Task completati.
     * @return JButton configurato
     */
    private JButton createToggleCompletedButton() {
        JButton toggleCompletedButton = new JButton(showCompleted ? "Nascondi completati" : "Mostra completati");
        styleActionButton(toggleCompletedButton);
        toggleCompletedButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        toggleCompletedButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, toggleCompletedButton.getPreferredSize().height));
        toggleCompletedButton.addActionListener(_ -> {
            showCompleted = !showCompleted;
            refreshToDoListDisplay();
        });
        return toggleCompletedButton;
    }

    /**
     * Aggiunge la lista dei Task completati al pannello.
     * @param completati Lista dei Task completati
     * @param oggi Data odierna per il calcolo priorità
     */
    private void addCompletedTasksList(List<ToDo> completati, LocalDate oggi) {
        if (!completati.isEmpty()) {
            todoListPanel.add(Box.createVerticalStrut(8));
        }
        int completedPos = 1;
        for (int i = 0; i < completati.size(); i++) {
            ToDo t = completati.get(i);
            JPanel todoItemPanel = createToDoItemPanel(t, completedPos++, oggi);
            todoListPanel.add(todoItemPanel);
            if (i < completati.size() - 1) {
                todoListPanel.add(Box.createVerticalStrut(8));
            }
        }
    }

    /**
     * Crea il pannello grafico per un singolo Task.
     * @param currentTodo Task da visualizzare
     * @param position Posizione nella lista
     * @param oggi Data odierna per il calcolo priorità
     * @return JPanel rappresentante il Task
     */
    private JPanel createToDoItemPanel(ToDo currentTodo, int position, LocalDate oggi) {
        final JPanel todoItemPanel = new JPanel(new BorderLayout(0, 10)) {
            @Override
            public Dimension getMaximumSize() {
                Dimension pref = getPreferredSize();
                return new Dimension(Integer.MAX_VALUE, pref.height);
            }
        };
        todoItemPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 1, 0, Color.BLACK),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        todoItemPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        setToDoPanelBackground(todoItemPanel, currentTodo);

        JPanel topContentPanel = new JPanel(new BorderLayout(10, 0));
        topContentPanel.setOpaque(false);
        JPanel imagePanel = createImagePanel(currentTodo);
        topContentPanel.add(imagePanel, BorderLayout.WEST);
        JPanel detailsTextPanel = createDetailsTextPanel(currentTodo, position, oggi);
        topContentPanel.add(detailsTextPanel, BorderLayout.CENTER);
        todoItemPanel.add(topContentPanel, BorderLayout.NORTH);

        // Mostra i pulsanti per Task creati dall'utente corrente o per Task condivisi
        boolean isOwnedByCurrentUser = currentTodo.getAutore() != null 
                && currentTodo.getAutore().getUsername().equalsIgnoreCase(currentUsername);
        boolean isSharedWithCurrentUser = currentTodo.getBachecaDestinazioneId() != null;
        
        if (isOwnedByCurrentUser || isSharedWithCurrentUser) {
            JPanel bottomPanel = new JPanel(new BorderLayout());
            bottomPanel.setOpaque(false);
            
            // Aggiungi la voce "Task condiviso da" se è un Task condiviso
            if (isSharedWithCurrentUser && !isOwnedByCurrentUser) {
                Utente autoreToDo = currentTodo.getAutore();
                String autoreDisplayName = autoreToDo != null ? escapeHtml(autoreToDo.getUsername()) : "N/D";
                JLabel sharedLabel = new JLabel("<html><b>ToDo condiviso dall'utente:</b><br>" + "@" + autoreDisplayName + HTML_CLOSE);
                sharedLabel.setFont(FontManager.getRegular(12f));
                sharedLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
                bottomPanel.add(sharedLabel, BorderLayout.NORTH);
            }
            
            JPanel dynamicButtonsWrapper = new JPanel(new CardLayout());
            dynamicButtonsWrapper.setOpaque(false);
            dynamicButtonsWrapper.setPreferredSize(new Dimension(Integer.MAX_VALUE, 85));
            dynamicButtonsWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));
            JPanel actionButtonsPanel = createActionButtonsPanel(currentTodo);
            dynamicButtonsWrapper.add(actionButtonsPanel, ACTIONS);
            bottomPanel.add(dynamicButtonsWrapper, BorderLayout.CENTER);
            
            todoItemPanel.add(bottomPanel, BorderLayout.SOUTH);
        }
        return todoItemPanel;
    }

    /**
     * Imposta il colore di sfondo del pannello Task in base al colore associato.
     * @param panel Pannello da colorare
     * @param task Task di riferimento
     */
    private void setToDoPanelBackground(JPanel panel, ToDo todo) {
        try {
            String hexColor = todo.getColore();
            if (hexColor != null && !hexColor.trim().isEmpty()
                    && hexColor.matches("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")) {
                panel.setBackground(Color.decode(hexColor.startsWith("#") ? hexColor : "#" + hexColor));
            } else {
                panel.setBackground(Color.WHITE);
            }
        } catch (NumberFormatException _) {
            panel.setBackground(Color.WHITE);
        }
    }

    /**
     * Crea il pannello immagine per il Task.
     * @param currentTodo Task di riferimento
     * @return JPanel con l'immagine o placeholder
     */
    private JPanel createImagePanel(ToDo currentTodo) {
        JPanel imagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Image todoImage = currentTodo.getImmagine();
                int panelWidth = getWidth();
                int panelHeight = getHeight();
                if (todoImage != null) {
                    Graphics2D g2dPanel = (Graphics2D) g.create();
                    g2dPanel.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    int imgWidth = todoImage.getWidth(this);
                    int imgHeight = todoImage.getHeight(this);
                    if (imgWidth <= 0 || imgHeight <= 0) {
                        g2dPanel.dispose();
                        return;
                    }
                    double imgAspect = (double) imgWidth / imgHeight;
                    double panelAspect = (double) panelWidth / panelHeight;
                    int drawX;
                    int drawY;
                    int drawW;
                    int drawH;
                    if (imgAspect > panelAspect) {
                        drawW = panelWidth;
                        drawH = (int) (panelWidth / imgAspect);
                        drawX = 0;
                        drawY = (panelHeight - drawH) / 2;
                    } else {
                        drawH = panelHeight;
                        drawW = (int) (panelHeight * imgAspect);
                        drawY = 0;
                        drawX = (panelWidth - drawW) / 2;
                    }
                    g2dPanel.drawImage(todoImage, drawX, drawY, drawW, drawH, this);
                    g2dPanel.dispose();
                } else {
                    Graphics2D g2dPanel = (Graphics2D) g.create();
                    g2dPanel.setColor(new Color(0, 0, 0, 0));
                    g2dPanel.fillRect(0, 0, panelWidth, panelHeight);
                    g2dPanel.setColor(Color.DARK_GRAY);
                    g2dPanel.setFont(FontManager.getBold(28f));
                    FontMetrics fm = g2dPanel.getFontMetrics();
                    String placeholderText = "N/D";
                    int stringWidth = fm.stringWidth(placeholderText);
                    int xText = (panelWidth - stringWidth) / 2;
                    int yText = (panelHeight - fm.getHeight()) / 2 + fm.getAscent();
                    g2dPanel.drawString(placeholderText, xText, yText);
                    g2dPanel.dispose();
                }
            }
        };
        imagePanel.setPreferredSize(new Dimension(TODO_IMAGE_SQUARE_SIZE, TODO_IMAGE_SQUARE_SIZE));
        imagePanel.setMinimumSize(new Dimension(TODO_IMAGE_SQUARE_SIZE, TODO_IMAGE_SQUARE_SIZE));
        imagePanel.setMaximumSize(new Dimension(TODO_IMAGE_SQUARE_SIZE, TODO_IMAGE_SQUARE_SIZE));
        imagePanel.setOpaque(false);
        imagePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        return imagePanel;
    }

    /**
     * Crea il pannello con i dettagli testuali del Task.
     * @param currentTodo Task di riferimento
     * @param position Posizione nella lista
     * @param oggi Data odierna per priorità
     * @return JPanel con i dettagli
     */
    private JPanel createDetailsTextPanel(ToDo currentTodo, int position, LocalDate oggi) {
        JPanel detailsTextPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        detailsTextPanel.setOpaque(false);

        String titleValue = escapeHtml(currentTodo.getTitolo());
        String titleText;
        if (currentTodo.getStato() == StatoToDo.COMPLETATO) {
            titleText = "<strike>" + titleValue + "</strike>";
        } else {
            titleText = titleValue;
        }
        JLabel titleLabel = new JLabel("<html><b>Titolo:</b> " + titleText + HTML_CLOSE);
        titleLabel.setFont(FontManager.getRegular(13f));
        detailsTextPanel.add(titleLabel, gbc);

        String descText = escapeHtml(currentTodo.getDescrizione());
        JLabel descLabel = new JLabel("<html><b>Descrizione:</b> " + descText + HTML_CLOSE);
        descLabel.setFont(FontManager.getRegular(12f));
        detailsTextPanel.add(descLabel, gbc);

        Utente autoreToDo = currentTodo.getAutore();
        String autoreDisplayName;
        
        if (autoreToDo != null) {
            if (currentUsername.equalsIgnoreCase(autoreToDo.getUsername())) {
                autoreDisplayName = "Tu";
            } else {
                autoreDisplayName = escapeHtml(autoreToDo.getUsername());
            }
        } else {
            autoreDisplayName = "N/D";
        }
        
        JLabel autoreLabel = new JLabel("<html><b>Autore:</b> " + autoreDisplayName + HTML_CLOSE);
        autoreLabel.setFont(FontManager.getRegular(12f));
        detailsTextPanel.add(autoreLabel, gbc);

        String urlText = currentTodo.getUrl() != null && !currentTodo.getUrl().isEmpty()
                ? escapeHtml(currentTodo.getUrl())
                : "N/D";
        JLabel urlLabel = new JLabel("<html><b>Url:</b> " + urlText + HTML_CLOSE);
        urlLabel.setFont(FontManager.getRegular(12f));
        detailsTextPanel.add(urlLabel, gbc);

        JLabel posLabel = new JLabel("<html><b>Posizione:</b>&nbsp;" + position + HTML_CLOSE);
        posLabel.setFont(FontManager.getRegular(12f));
        detailsTextPanel.add(posLabel, gbc);

        JLabel statoLabel = new JLabel("<html><b>Stato:</b> " + currentTodo.getStato().getDisplayName() + HTML_CLOSE);
        statoLabel.setFont(FontManager.getRegular(12f));
        detailsTextPanel.add(statoLabel, gbc);

        String colorName = getColorName(currentTodo.getColore());
        JLabel coloreLabel = new JLabel("<html><b>Colore:</b> " + colorName + HTML_CLOSE);
        coloreLabel.setFont(FontManager.getRegular(12f));
        detailsTextPanel.add(coloreLabel, gbc);

        JLabel prioritaLabel = new JLabel(
                "<html><b>Priorità:</b> " + getPrioritaText(currentTodo.getScadenza(), oggi) + HTML_CLOSE);
        prioritaLabel.setFont(FontManager.getRegular(12f));
        detailsTextPanel.add(prioritaLabel, gbc);

        String scadenzaText = (currentTodo.getScadenza() != null)
                ? currentTodo.getScadenza().format(ITALIAN_DATE_FORMATTER)
                : "N/D";
        JLabel scadenzaLabel = new JLabel("<html><b>Scadenza:</b>&nbsp;" + scadenzaText + HTML_CLOSE);
        scadenzaLabel.setFont(FontManager.getRegular(12f));
        detailsTextPanel.add(scadenzaLabel, gbc);

        return detailsTextPanel;
    }

    /**
     * Restituisce il nome del colore a partire dal codice esadecimale.
     * @param hexColor Codice colore esadecimale
     * @return Nome del colore o "N/D"
     */
    private String getColorName(String hexColor) {
        if (hexColor != null && !hexColor.isEmpty()) {
            return COLOR_PALETTE.entrySet().stream()
                    .filter(entry -> entry.getValue().equalsIgnoreCase(hexColor))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(hexColor);
        }
        return "N/D";
    }

    /**
     * Restituisce la priorità testuale in base alla scadenza.
     * @param scadenza Data di scadenza
     * @param oggi Data odierna
     * @return Stringa descrittiva della priorità
     */
    private String getPrioritaText(LocalDate scadenza, LocalDate oggi) {
        if (scadenza != null) {
            if (scadenza.isBefore(oggi)) {
                return "Nessuna - Scaduto";
            } else if (scadenza.isEqual(oggi)) {
                return "Alta - Entro fine giornata";
            } else if (scadenza.isAfter(oggi) && scadenza.isBefore(oggi.plusDays(8))) {
                return "Media - Entro 7 giorni";
            } else {
                return "Bassa";
            }
        } else {
            return "N/D";
        }
    }

    /**
     * Crea il pannello dei pulsanti azione per un Task.
     * Mostra sempre tutti e 4 i pulsanti: "Completa/Ripristina", "Elimina", "Modifica", "Condividi".
     * @param currentTodo Task di riferimento
     * @return JPanel con i pulsanti azione
     */
    private JPanel createActionButtonsPanel(ToDo currentTodo) {
        JPanel actionButtonsPanel = createBaseActionButtonsPanel();
        
        if (currentTodo.getStato() == StatoToDo.COMPLETATO) {
            addCompletedTaskButtons(actionButtonsPanel, currentTodo);
        } else {
            addNonCompletedTaskButtons(actionButtonsPanel, currentTodo);
        }
        
        return actionButtonsPanel;
    }

    /**
     * Crea il pannello base per i pulsanti azione.
     * @return JPanel configurato
     */
    private JPanel createBaseActionButtonsPanel() {
        JPanel actionButtonsPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        actionButtonsPanel.setOpaque(false);
        actionButtonsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return actionButtonsPanel;
    }

    /**
     * Aggiunge i pulsanti per un Task completato.
     * @param actionButtonsPanel Pannello dei pulsanti
     * @param currentTodo Task di riferimento
     */
    private void addCompletedTaskButtons(JPanel actionButtonsPanel, ToDo currentTodo) {
        boolean isOwnedByCurrentUser = currentTodo.getAutore() != null 
                && currentTodo.getAutore().getUsername().equalsIgnoreCase(currentUsername);
        boolean isSharedWithCurrentUser = currentTodo.getBachecaDestinazioneId() != null;
        
        JButton completeButton = createRestoreButton(currentTodo);
        JButton deleteButton = createDeleteButton(currentTodo);
        
        // Solo l'autore può modificare e condividere
        if (isOwnedByCurrentUser) {
            actionButtonsPanel.add(completeButton);
            actionButtonsPanel.add(deleteButton);
            JButton editButton = createEditButton(currentTodo);
            JButton shareButton = createShareButton(currentTodo);
            actionButtonsPanel.add(editButton);
            actionButtonsPanel.add(shareButton);
        } else if (isSharedWithCurrentUser) {
            // Per Task condivisi, usa un layout con 2 pulsanti (uno sopra e uno sotto)
            actionButtonsPanel.setLayout(new GridLayout(2, 1, 5, 5));
            actionButtonsPanel.add(completeButton);
            actionButtonsPanel.add(deleteButton);
        }
    }

    /**
     * Aggiunge i pulsanti per un Task non completato.
     * @param actionButtonsPanel Pannello dei pulsanti
     * @param currentTodo Task di riferimento
     */
    private void addNonCompletedTaskButtons(JPanel actionButtonsPanel, ToDo currentTodo) {
        boolean isOwnedByCurrentUser = currentTodo.getAutore() != null 
                && currentTodo.getAutore().getUsername().equalsIgnoreCase(currentUsername);
        boolean isSharedWithCurrentUser = currentTodo.getBachecaDestinazioneId() != null;
        
        JButton completeButton = createCompleteButton(currentTodo);
        JButton deleteButton = createDeleteButton(currentTodo);
        
        // Solo l'autore può modificare e condividere
        if (isOwnedByCurrentUser) {
            actionButtonsPanel.add(completeButton);
            actionButtonsPanel.add(deleteButton);
            JButton editButton = createEditButton(currentTodo);
            JButton shareButton = createShareButton(currentTodo);
            actionButtonsPanel.add(editButton);
            actionButtonsPanel.add(shareButton);
        } else if (isSharedWithCurrentUser) {
            // Per Task condivisi, usa un layout con 2 pulsanti (uno sopra e uno sotto)
            actionButtonsPanel.setLayout(new GridLayout(2, 1, 5, 5));
            actionButtonsPanel.add(completeButton);
            actionButtonsPanel.add(deleteButton);
        }
    }

    /**
     * Crea il pulsante "Ripristina" per Task completati.
     * @param currentTodo Task di riferimento
     * @return JButton configurato
     */
    private JButton createRestoreButton(ToDo currentTodo) {
        JButton completeButton = new JButton("Ripristina");
        styleActionButton(completeButton);
            completeButton.setToolTipText("Segna come non completato");
        completeButton.addActionListener(_ -> {
            StatoToDo nuovoStato = StatoToDo.NON_COMPLETATO;
            controller.modificaToDo(currentTodo, new Controller.ToDoUpdateParams(null, null, null, nuovoStato, null, null, null));
            refreshToDoList();
        });
        return completeButton;
    }

    /**
     * Crea il pulsante "Completa" per Task non completati.
     * @param currentTodo Task di riferimento
     * @return JButton configurato
     */
    private JButton createCompleteButton(ToDo currentTodo) {
        JButton completeButton = new JButton("Completa");
        styleActionButton(completeButton);
            completeButton.setToolTipText("Segna come completato");
        completeButton.addActionListener(_ -> {
            StatoToDo nuovoStato = StatoToDo.COMPLETATO;
            controller.modificaToDo(currentTodo, new Controller.ToDoUpdateParams(null, null, null, nuovoStato, null, null, null));
            showCompleted = false;
            refreshToDoList();
        });
        return completeButton;
    }

    /**
     * Crea il pulsante "Elimina" con conferma.
     * @param currentTodo Task di riferimento
     * @return JButton configurato
     */
    private JButton createDeleteButton(ToDo currentTodo) {
        JButton deleteButton = new JButton(ELIMINA);
        styleActionButton(deleteButton);
        deleteButton.setToolTipText("Elimina ToDo");
        deleteButton.addActionListener(_ -> {
            if (Boolean.TRUE.equals(deleteButton.getClientProperty(CONFIRMING))) {
                if (controller.eliminaToDo(currentTodo)) {
                    refreshToDoList();
                } else {
                    JOptionPane.showMessageDialog(this, "Impossibile eliminare il ToDo. Controlla i permessi o il log.", ERRORE, JOptionPane.ERROR_MESSAGE);
                }
            } else {
                deleteButton.putClientProperty(CONFIRMING, true);
                deleteButton.setText("Sei sicuro?");
                Timer resetTimer = new Timer(3000, _ -> {
                    deleteButton.setText(ELIMINA);
                    deleteButton.putClientProperty(CONFIRMING, false);
                });
                resetTimer.setRepeats(false);
                resetTimer.start();
            }
        });
        return deleteButton;
    }

    /**
     * Crea il pulsante "Modifica".
     * @param currentTodo Task di riferimento
     * @return JButton configurato
     */
    private JButton createEditButton(ToDo currentTodo) {
        JButton editButton = new JButton("Modifica");
        editButton.setToolTipText("Modifica ToDo");
        styleActionButton(editButton);
        editButton.addActionListener(_ -> showEditToDoDialog(currentTodo));
        return editButton;
    }

    /**
     * Crea il pulsante "Condividi".
     * @param currentTodo Task di riferimento
     * @return JButton configurato
     */
    private JButton createShareButton(ToDo currentTodo) {
        JButton shareButton = new JButton(CONDIVIDI);
        shareButton.setToolTipText("Condividi ToDo con altro utente");
        styleActionButton(shareButton);
        shareButton.addActionListener(_ -> showShareToDoDialog(currentTodo));
        return shareButton;
    }

    /**
     * Mostra la finestra di dialogo per condividere un Task.
     * @param toDoToShare Task da condividere
     */
    private void showShareToDoDialog(ToDo toDoToShare) {
        if (!validateSharePermissions(toDoToShare)) {
            return;
        }

        JDialog shareDialog = createShareDialog();
        JPanel formPanel = createShareFormPanel();
        JPanel buttonPanel = createShareButtonPanel(shareDialog, formPanel, toDoToShare);

        shareDialog.add(formPanel, BorderLayout.CENTER);
        shareDialog.add(buttonPanel, BorderLayout.SOUTH);
        shareDialog.setBounds(this.getLocationOnScreen().x, this.getLocationOnScreen().y, this.getWidth(), this.getHeight());
        shareDialog.setVisible(true);
    }

    /**
     * Valida i permessi per la condivisione del Task.
     * @param toDoToShare Task da condividere
     * @return true se i permessi sono validi, false altrimenti
     */
    private boolean validateSharePermissions(ToDo toDoToShare) {
        if (!controller.isUserLoggedIn() || toDoToShare == null) {
            JOptionPane.showMessageDialog(this, "Nessun ToDo selezionato o utente non loggato.", "Condividi ToDo", JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        if (toDoToShare.getAutore() == null || !toDoToShare.getAutore().getUsername().equalsIgnoreCase(currentUsername)) {
            JOptionPane.showMessageDialog(this, "Solo l'autore può condividere questo ToDo.", "Condividi ToDo", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    /**
     * Crea il dialogo principale per la condivisione.
     * @return JDialog configurato
     */
    private JDialog createShareDialog() {
        JDialog shareDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), true);
        shareDialog.setUndecorated(true);
        ((JComponent) shareDialog.getContentPane()).setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        shareDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        shareDialog.getContentPane().setLayout(new BorderLayout());
        shareDialog.getContentPane().setBackground(Color.WHITE);
        return shareDialog;
    }

    /**
     * Crea il pannello del form per la condivisione.
     * @return JPanel del form
     */
    private JPanel createShareFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = createShareFormConstraints();

        JTextField userField = createUserField();
        JComboBox<String> boardCombo = createBoardComboBox(userField);
        
        addUserFieldToForm(formPanel, gbc, userField);
        addBoardFieldToForm(formPanel, gbc, boardCombo);
        
        return formPanel;
    }

    /**
     * Crea i vincoli per il layout del form di condivisione.
     * @return GridBagConstraints configurati
     */
    private GridBagConstraints createShareFormConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        return gbc;
    }

    /**
     * Crea il campo username destinatario.
     * @return JTextField configurato
     */
    private JTextField createUserField() {
        JTextField userField = new JTextField();
        userField.setName("userField");
        userField.setFont(FontManager.getRegular(14f));
        userField.setBorder(BORDER_NORMAL);
        userField.setColumns(15);
        userField.setPreferredSize(new Dimension(220, 28));
        return userField;
    }

    /**
     * Crea la combo box per la selezione della bacheca.
     * @param userField Campo username per il listener
     * @return JComboBox configurata
     */
    private JComboBox<String> createBoardComboBox(JTextField userField) {
        JComboBox<String> boardCombo = new JComboBox<>();
        boardCombo.setName("boardCombo");
        boardCombo.addItem("Seleziona un utente prima");
        
        userField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateBachecheCombo(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateBachecheCombo(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateBachecheCombo(); }
            
            private void updateBachecheCombo() {
                updateBoardComboItems(userField, boardCombo);
            }
        });
        
        setupBoardComboBoxStyle(boardCombo);
        return boardCombo;
    }

    /**
     * Aggiorna gli elementi della combo box bacheche.
     * @param userField Campo username
     * @param boardCombo Combo box da aggiornare
     */
    private void updateBoardComboItems(JTextField userField, JComboBox<String> boardCombo) {
        String username = userField.getText().trim();
        boardCombo.removeAllItems();
        
        if (username.isEmpty()) {
            boardCombo.addItem("Seleziona un utente prima");
            return;
        }
        
        if (username.equalsIgnoreCase(currentUsername)) {
            boardCombo.addItem("Non puoi condividere un ToDo con te stesso");
            return;
        }
        
        List<String> bachecheUtente = controller.getBachecheUtenteByUsername(username);
        if (bachecheUtente.isEmpty()) {
            boardCombo.addItem("Utente non trovato o senza bacheche");
        } else {
            for (String bacheca : bachecheUtente) {
                boardCombo.addItem(bacheca);
            }
        }
    }

    /**
     * Configura lo stile della combo box bacheche.
     * @param boardCombo Combo box da configurare
     */
    private void setupBoardComboBoxStyle(JComboBox<String> boardCombo) {
        boardCombo.setFont(FontManager.getRegular(14f));
        boardCombo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 1),
            BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));
        boardCombo.setBackground(Color.WHITE);
        boardCombo.setPreferredSize(new Dimension(220, 28));
        boardCombo.setUI(createCustomComboBoxUI());
    }

    /**
     * Crea un UI personalizzato per la combo box.
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
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        button.setBackground(Color.LIGHT_GRAY);
                    }
                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        button.setBackground(Color.WHITE);
                    }
                });
                return button;
            }
        };
    }

    /**
     * Aggiunge il campo username al form.
     * @param formPanel Pannello del form
     * @param gbc Vincoli del layout
     * @param userField Campo username
     */
    private void addUserFieldToForm(JPanel formPanel, GridBagConstraints gbc, JTextField userField) {
        JLabel userLabel = new JLabel("Username destinatario:");
        userLabel.setFont(FontManager.getRegular(13f));
        formPanel.add(userLabel, gbc);
        gbc.gridy++;
        formPanel.add(userField, gbc);
        gbc.gridy++;
    }

    /**
     * Aggiunge il campo bacheca al form.
     * @param formPanel Pannello del form
     * @param gbc Vincoli del layout
     * @param boardCombo Combo box bacheche
     */
    private void addBoardFieldToForm(JPanel formPanel, GridBagConstraints gbc, JComboBox<String> boardCombo) {
        JLabel boardLabel = new JLabel("Bacheca destinazione:");
        boardLabel.setFont(FontManager.getRegular(13f));
        formPanel.add(boardLabel, gbc);
        gbc.gridy++;
        formPanel.add(boardCombo, gbc);
        gbc.gridy++;
    }

    /**
     * Crea il pannello dei pulsanti per la condivisione.
     * @param shareDialog Dialogo di riferimento
     * @param formPanel Pannello del form
     * @param toDoToShare Task da condividere
     * @return JPanel dei pulsanti
     */
    private JPanel createShareButtonPanel(JDialog shareDialog, JPanel formPanel, ToDo toDoToShare) {
        JButton shareButton = new JButton(CONDIVIDI);
        styleActionButton(shareButton);
        JButton cancelButton = new JButton(ANNULLA);
        styleActionButton(cancelButton);

        shareButton.addActionListener(_ -> handleShareAction(shareDialog, formPanel, toDoToShare));
        cancelButton.addActionListener(_ -> shareDialog.dispose());
        
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        buttonPanel.add(cancelButton);
        buttonPanel.add(shareButton);
        
        return buttonPanel;
    }

    /**
     * Gestisce l'azione di condivisione.
     * @param shareDialog Dialogo di riferimento
     * @param formPanel Pannello del form
     * @param toDoToShare Task da condividere
     */
    private void handleShareAction(JDialog shareDialog, JPanel formPanel, ToDo toDoToShare) {
        Component userFieldComponent = findComponentByName(formPanel, "userField");
        Component boardComboComponent = findComponentByName(formPanel, "boardCombo");
        
        if (!(userFieldComponent instanceof JTextField) || !(boardComboComponent instanceof JComboBox)) {
            logger.error("BoardPanel: Campi del form non trovati o di tipo errato per la condivisione");
            return;
        }
        
        JTextField userField = (JTextField) userFieldComponent;
        @SuppressWarnings("unchecked")
        JComboBox<String> boardCombo = (JComboBox<String>) boardComboComponent;
        
        String destinatarioUsername = userField.getText().trim();
        String bachecaDestinazioneDisplayName = (String) boardCombo.getSelectedItem();
        
        if (!validateShareInput(destinatarioUsername, bachecaDestinazioneDisplayName, shareDialog)) {
            return;
        }
        
        if (destinatarioUsername.equalsIgnoreCase(currentUsername)) {
            return;
        }

        boolean shareSuccess = controller.condividiToDo(toDoToShare, destinatarioUsername, bachecaDestinazioneDisplayName);
        if (shareSuccess) {
            showShareSuccessDialog();
            shareDialog.dispose();
            refreshToDoList();
        } else {
            showShareErrorDialog();
        }
    }

    /**
     * Valida l'input per la condivisione.
     * @param destinatarioUsername Username destinatario
     * @param bachecaDestinazioneDisplayName Nome bacheca destinazione
     * @param shareDialog Dialogo di riferimento
     * @return true se l'input è valido, false altrimenti
     */
    private boolean validateShareInput(String destinatarioUsername, String bachecaDestinazioneDisplayName, JDialog shareDialog) {
        if (destinatarioUsername.isEmpty()) {
            JOptionPane.showMessageDialog(shareDialog, "Inserisci l'username del destinatario.", ERRORE, JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (bachecaDestinazioneDisplayName == null || bachecaDestinazioneDisplayName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(shareDialog, "Seleziona una bacheca di destinazione.", ERRORE, JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }

    /**
     * Mostra la dialog di successo per la condivisione.
     */
    private void showShareSuccessDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Condivisione Riuscita", true);
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

        String message = "ToDo condiviso con successo.";
        JLabel messageLabel = new JLabel("<html><div style='text-align: center; width: 300px;'>" + message + "</div></html>");
        messageLabel.setFont(FontManager.getRegular(14f));
        textPanel.add(messageLabel, gbc);

        formPanel.add(textPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        JButton okButton = new JButton("OK");
        okButton.setFont(FontManager.getRegular(12f));
        okButton.setBackground(Color.BLACK);
        okButton.setForeground(Color.WHITE);
        okButton.setOpaque(true);
        okButton.setFocusPainted(false);
        okButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        okButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        okButton.addActionListener(_ -> dialog.dispose());
        okButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                okButton.setBackground(Color.DARK_GRAY);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                okButton.setBackground(Color.BLACK);
            }
        });
        buttonPanel.add(okButton, BorderLayout.CENTER);
        
        contentPanel.add(formPanel, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        dialog.add(contentPanel);
        
        dialog.setSize(400, 150);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /**
     * Mostra la dialog di errore per la condivisione.
     */
    private void showShareErrorDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Errore Condivisione", true);
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

        String message = "Impossibile condividere il ToDo.<br>Verifica che l'utente destinatario esista e che le condizioni per la condivisione siano soddisfatte.";
        JLabel messageLabel = new JLabel("<html><div style='text-align: center; width: 300px;'>" + message + "</div></html>");
        messageLabel.setFont(FontManager.getRegular(14f));
        textPanel.add(messageLabel, gbc);

        formPanel.add(textPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        JButton okButton = new JButton("OK");
        okButton.setFont(FontManager.getRegular(12f));
        okButton.setBackground(Color.BLACK);
        okButton.setForeground(Color.WHITE);
        okButton.setOpaque(true);
        okButton.setFocusPainted(false);
        okButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        okButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        okButton.addActionListener(_ -> dialog.dispose());
        okButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                okButton.setBackground(Color.DARK_GRAY);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                okButton.setBackground(Color.BLACK);
        }
        });
        buttonPanel.add(okButton, BorderLayout.CENTER);
        
        contentPanel.add(formPanel, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        dialog.add(contentPanel);
        
        dialog.setSize(400, 150);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /**
     * Mostra la finestra di dialogo per modificare un Task.
     * @param todoToEdit Task da modificare
     */
    private void showEditToDoDialog(ToDo todoToEdit) {
        if (!controller.isUserLoggedIn() || todoToEdit == null) return;
        immagineSelezionataGlobal = todoToEdit.getImmagine();
        JDialog editToDoDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), true);
        editToDoDialog.setUndecorated(true);
        editToDoDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel formPanel = createToDoFormPanel(todoToEdit);
        JButton saveButton = new JButton("Salva");
        styleActionButton(saveButton);
        JButton cancelButton = new JButton(ANNULLA);
        styleActionButton(cancelButton);
        
        saveButton.addActionListener(_ -> {
            try {
                String nuovoTitolo = ((JTextField)findComponentByName(formPanel, TITOLO_FIELD)).getText().trim();
                String nuovaDescrizione = ((JTextField)findComponentByName(formPanel, DESCRIZIONE_AREA)).getText().trim();
                String nuovaScadenzaStr = ((JTextField)findComponentByName(formPanel, SCADENZA_FIELD)).getText().trim();
                String nuovoColore = (String) ((JComboBox<?>)findComponentByName(formPanel, COLORE_COMBOBOX)).getSelectedItem();
                String nuovoUrl = ((JTextField)findComponentByName(formPanel, URL_FIELD)).getText().trim();

                if (nuovoTitolo.isEmpty()) { JOptionPane.showMessageDialog(editToDoDialog, "Il titolo è obbligatorio.", ERRORE, JOptionPane.ERROR_MESSAGE); return; }
                if (nuovaDescrizione.isEmpty()) { JOptionPane.showMessageDialog(editToDoDialog, "La descrizione è obbligatoria.", ERRORE, JOptionPane.ERROR_MESSAGE); return; }

                LocalDate nuovaScadenza = parseNuovaScadenza(nuovaScadenzaStr, editToDoDialog, todoToEdit);
                if (nuovaScadenza == null) return;

                String coloreHex = COLOR_PALETTE.get(nuovoColore);
                boolean success = controller.modificaToDo(todoToEdit, new Controller.ToDoUpdateParams(nuovoTitolo, nuovaDescrizione, nuovaScadenza,
                        todoToEdit.getStato(), coloreHex, nuovoUrl,
                        immagineSelezionataGlobal));
                if (success) {
                    refreshToDoList();
                    editToDoDialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(editToDoDialog, "Errore durante la modifica del ToDo. Controlla i permessi o il log.", ERRORE + " Modifica", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ignored) {
                JOptionPane.showMessageDialog(editToDoDialog, "Errore generico nel form: " + ignored.getMessage(), ERRORE + " Form", JOptionPane.ERROR_MESSAGE);
                ignored.printStackTrace();
            }
        });
        cancelButton.addActionListener(_ -> editToDoDialog.dispose());
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        editToDoDialog.getContentPane().setLayout(new BorderLayout());
        editToDoDialog.getContentPane().setBackground(Color.WHITE);
        editToDoDialog.add(formPanel, BorderLayout.CENTER);
        editToDoDialog.add(buttonPanel, BorderLayout.SOUTH);

        editToDoDialog.setBounds(this.getLocationOnScreen().x, this.getLocationOnScreen().y, this.getWidth(), this.getHeight());
        editToDoDialog.setVisible(true);
    }

    /**
     * Effettua il parsing della nuova data di scadenza dal form di modifica.
     * @param nuovaScadenzaStr Stringa data scadenza
     * @param dialog Dialogo di riferimento
     * @param todoToEdit Task in modifica
     * @return LocalDate se valida, null altrimenti
     */
    private LocalDate parseNuovaScadenza(String nuovaScadenzaStr, JDialog dialog, ToDo todoToEdit) {
        if (nuovaScadenzaStr.isEmpty()) return null;
        try {
            LocalDate nuovaScadenza = LocalDate.parse(nuovaScadenzaStr, ITALIAN_DATE_FORMATTER);
            if (nuovaScadenza.isBefore(LocalDate.now()) && (todoToEdit.getScadenza() == null || !nuovaScadenza.isEqual(todoToEdit.getScadenza()))) {
                JOptionPane.showMessageDialog(dialog, "La nuova data di scadenza non può essere nel passato.", ERRORE, JOptionPane.ERROR_MESSAGE);
                return null;
            }
            return nuovaScadenza;
        } catch (DateTimeParseException _) {
            JOptionPane.showMessageDialog(dialog, "Formato data scadenza non valido. Usare GG-MM-AAAA.", ERRORE, JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    /**
     * Crea il pannello del form per la creazione/modifica Task.
     * @param task Task da modificare (null per creazione)
     * @return JPanel del form
     */
    private JPanel createToDoFormPanel(ToDo todo) {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        final ImagePreviewLabel imagePreviewLabel = new ImagePreviewLabel(IMMAGINE);
        Dimension inputSize = new Dimension(Integer.MAX_VALUE, new JTextField().getPreferredSize().height + 10);

        JLabel titoloLabel = new JLabel("Titolo: *");
        JTextField titoloField = new JTextField();
        titoloField.setName(TITOLO_FIELD);

        JLabel descrizioneLabel = new JLabel("Descrizione: *");
        JTextField descrizioneField = new JTextField();
        descrizioneField.setName(DESCRIZIONE_AREA);

        JLabel scadenzaLabel = new JLabel("Scadenza: *");
        JTextField scadenzaField = new JTextField();
        scadenzaField.setName(SCADENZA_FIELD);

        JLabel coloreLabel = new JLabel("Colore sfondo:");
        JComboBox<String> coloreComboBox = new JComboBox<>(COLOR_PALETTE.keySet().toArray(new String[0]));
        coloreComboBox.setName(COLORE_COMBOBOX);

        JLabel urlLabel = new JLabel("Url:");
        JTextField urlField = new JTextField();
        urlField.setName(URL_FIELD);

        setupFormFields(new JComponent[] { titoloField, descrizioneField, scadenzaField, urlField, coloreComboBox },
                inputSize);
        setupFormLabels(new JLabel[] { titoloLabel, descrizioneLabel, scadenzaLabel, coloreLabel, urlLabel });
        scadenzaField.setToolTipText("Formato: GG-MM-AAAA");
        setupColoreComboBox(coloreComboBox);

        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        int yPos = 0;

        final JLayeredPane layeredPane = new JLayeredPane();
        final JPanel previewContainer = new SquareWrapperPanel(new BorderLayout());
        previewContainer.setOpaque(false);
        previewContainer.add(layeredPane, BorderLayout.CENTER);

        setupImagePreviewLabel(imagePreviewLabel);
        final JButton deleteImageButton = new JButton("X");
        setupDeleteImageButton(imagePreviewLabel, layeredPane, deleteImageButton);

        layeredPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                imagePreviewLabel.setBounds(0, 0, layeredPane.getWidth(), layeredPane.getHeight());
                deleteImageButton.setBounds(layeredPane.getWidth() - 25, 0, 25, 25);
            }
        });

        gbc.gridy = yPos++;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridheight = 4;
        formPanel.add(previewContainer, gbc);

        yPos += 4;
        gbc.gridy = yPos;
        gbc.gridheight = 1;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;
        gbc.gridx = 0;

        JPanel fieldsGridPanel = new JPanel(new GridLayout(0, 2, 10, 0));
        fieldsGridPanel.setOpaque(false);

        fieldsGridPanel.add(titoloLabel);
        fieldsGridPanel.add(descrizioneLabel);
        fieldsGridPanel.add(titoloField);
        fieldsGridPanel.add(descrizioneField);
        fieldsGridPanel.add(scadenzaLabel);
        fieldsGridPanel.add(coloreLabel);
        fieldsGridPanel.add(scadenzaField);
        fieldsGridPanel.add(coloreComboBox);

        formPanel.add(fieldsGridPanel, gbc);

        yPos++;
        gbc.gridy = yPos;
        formPanel.add(urlLabel, gbc);

        yPos++;
        gbc.gridy = yPos;
        formPanel.add(urlField, gbc);

        FormFields formFields = new FormFields(titoloField, descrizioneField, scadenzaField, coloreComboBox, urlField,
                imagePreviewLabel, deleteImageButton);
        setupInitialFormValues(todo, formFields);
        setupImageDeleteButton(formFields);
        setupImageChooser(formFields);

        return formPanel;
    }

    /**
     * Imposta i valori iniziali dei campi del form Task.
     * @param task Task di riferimento (null per creazione)
     * @param fields Contenitore dei campi
     */
    private void setupInitialFormValues(ToDo todo, FormFields fields) {
        if (todo != null) {
            fields.titoloField.setText(todo.getTitolo());
            fields.descrizioneField.setText(todo.getDescrizione());
            if (todo.getScadenza() != null) {
                fields.scadenzaField.setText(todo.getScadenza().format(ITALIAN_DATE_FORMATTER));
            }
            String currentHexColor = todo.getColore() != null ? todo.getColore() : "#FFFFFF";
            String currentPaletteName = COLOR_PALETTE.entrySet().stream()
                    .filter(entry -> entry.getValue().equalsIgnoreCase(currentHexColor))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(BIANCO);
            fields.coloreComboBox.setSelectedItem(currentPaletteName);

            fields.urlField.setText(todo.getUrl() != null ? todo.getUrl() : "");
            if (todo.getImmagine() != null) {
                immagineSelezionataGlobal = todo.getImmagine();
                fields.imagePreviewLabel.setText("");
                fields.imagePreviewLabel.setImage(immagineSelezionataGlobal);
                fields.deleteImageButton.setVisible(true);
            } else {
                fields.imagePreviewLabel.setText(IMMAGINE);
                fields.imagePreviewLabel.setImage(null);
                fields.deleteImageButton.setVisible(false);
            }
        } else {
            fields.scadenzaField.setText(LocalDate.now().plusDays(1).format(ITALIAN_DATE_FORMATTER));
            fields.coloreComboBox.setSelectedItem(BIANCO);
            fields.imagePreviewLabel.setText(IMMAGINE);
            fields.imagePreviewLabel.setImage(null);
            fields.deleteImageButton.setVisible(false);
        }
    }

    /**
     * Imposta il comportamento del pulsante elimina immagine.
     * @param fields Contenitore dei campi
     */
    private void setupImageDeleteButton(FormFields fields) {
        fields.deleteImageButton.addActionListener(_ -> {
            immagineSelezionataGlobal = null;
            fields.imagePreviewLabel.setImage(null);
            fields.imagePreviewLabel.setText(IMMAGINE);
            fields.deleteImageButton.setVisible(false);
        });
    }

    /**
     * Imposta il comportamento per la selezione immagine dal file system.
     * @param fields Contenitore dei campi
     */
    private void setupImageChooser(FormFields fields) {
        fields.imagePreviewLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setDialogTitle("Scegli un'immagine per il Task");
                    fileChooser.setAcceptAllFileFilterUsed(false);
                    FileNameExtensionFilter filter = new FileNameExtensionFilter("Immagini (JPG, PNG, GIF)", "jpg",
                            "jpeg", "png", "gif");
                    fileChooser.addChoosableFileFilter(filter);
                    int returnValue = fileChooser
                            .showOpenDialog(SwingUtilities.getWindowAncestor(fields.imagePreviewLabel));
                    if (returnValue == JFileChooser.APPROVE_OPTION) {
                        File selectedFile = fileChooser.getSelectedFile();
                        try {
                            immagineSelezionataGlobal = ImageIO.read(selectedFile);
                            if (immagineSelezionataGlobal != null) {
                                fields.imagePreviewLabel.setText("");
                                fields.imagePreviewLabel.setImage(immagineSelezionataGlobal);
                                fields.deleteImageButton.setVisible(true);
                            } else {
                                JOptionPane.showMessageDialog(
                                        SwingUtilities.getWindowAncestor(fields.imagePreviewLabel),
                                        "Impossibile caricare l'immagine dal file selezionato.", ERRORE + " Immagine",
                                        JOptionPane.ERROR_MESSAGE);
                                fields.imagePreviewLabel.setText(IMMAGINE);
                                fields.imagePreviewLabel.setImage(null);
                                fields.deleteImageButton.setVisible(false);
                            }
                        } catch (IOException ignored) {
                            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(fields.imagePreviewLabel),
                                    "Errore nel caricamento dell'immagine: " + ignored.getMessage(), ERRORE + " Immagine",
                                    JOptionPane.ERROR_MESSAGE);
                            fields.imagePreviewLabel.setText(IMMAGINE);
                            fields.imagePreviewLabel.setImage(null);
                            fields.deleteImageButton.setVisible(false);
                        }
                    }
                }
            }
        });
    }

    /**
     * Imposta le proprietà grafiche dei campi del form.
     * @param fields Array di componenti
     * @param inputSize Dimensione massima
     */
    private void setupFormFields(JComponent[] fields, Dimension inputSize) {
        for (JComponent field : fields) {
            field.setFont(FontManager.getRegular(14f));
            field.setBorder(BORDER_NORMAL);
            field.setMaximumSize(inputSize);
        }
    }

    /**
     * Imposta le proprietà grafiche delle label del form.
     * @param labels Array di label
     */
    private void setupFormLabels(JLabel[] labels) {
        for (JLabel label : labels) {
            label.setFont(FontManager.getRegular(13f));
        }
    }

    /**
     * Imposta il renderer e lo stile della combo box dei colori.
     * @param coloreComboBox Combo box da configurare
     */
    private void setupColoreComboBox(JComboBox<String> coloreComboBox) {
        coloreComboBox.setBackground(Color.WHITE);
        coloreComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                                                          boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
                        cellHasFocus);
                if (value != null) {
                    String colorName = (String) value;
                    String hexColor = COLOR_PALETTE.get(colorName);
                    try {
                        label.setIcon(new LargeColorIcon(Color.decode(hexColor)));
                    } catch (Exception _) {
                        label.setIcon(new LargeColorIcon(Color.WHITE));
                    }
                }
                label.setIconTextGap(10);
                return label;
            }
        });
        coloreComboBox.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton button = super.createArrowButton();
                button.setBackground(Color.WHITE);
                button.setBorder(BorderFactory.createEmptyBorder());
                return button;
            }
        });
    }

    /**
     * Imposta le proprietà grafiche della label anteprima immagine.
     * @param imagePreviewLabel Label da configurare
     */
    private void setupImagePreviewLabel(ImagePreviewLabel imagePreviewLabel) {
        imagePreviewLabel.setName("imagePreviewLabel");
        imagePreviewLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        imagePreviewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imagePreviewLabel.setVerticalAlignment(SwingConstants.CENTER);
        imagePreviewLabel.setFont(FontManager.getRegular(16f));
        imagePreviewLabel.setOpaque(true);
        imagePreviewLabel.setBackground(Color.WHITE);
        imagePreviewLabel.setForeground(Color.BLACK);
        imagePreviewLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /**
     * Imposta la posizione e lo stile del pulsante elimina immagine nel layered pane.
     * @param imagePreviewLabel Label anteprima
     * @param layeredPane Layered pane contenitore
     * @param deleteImageButton Pulsante elimina
     */
    private void setupDeleteImageButton(ImagePreviewLabel imagePreviewLabel, JLayeredPane layeredPane, JButton deleteImageButton) {
        deleteImageButton.setFont(FontManager.getBold(12f));
        deleteImageButton.setForeground(Color.WHITE);
        deleteImageButton.setBackground(Color.BLACK);
        deleteImageButton.setOpaque(true);
        deleteImageButton.setFocusPainted(false);
        deleteImageButton.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
        deleteImageButton.setMargin(new Insets(0, 0, 0, 0));
        deleteImageButton.setVisible(false);

        layeredPane.add(imagePreviewLabel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(deleteImageButton, JLayeredPane.PALETTE_LAYER);
    }

    /**
     * Cerca ricorsivamente un componente per nome all'interno di un container.
     * @param container Container di partenza
     * @param name Nome del componente
     * @return Componente trovato o null
     */
    private Component findComponentByName(Container container, String name) {
        for (Component comp : container.getComponents()) {
            if (name.equals(comp.getName())) {
                return comp;
            }
            if (comp instanceof Container childContainer) {
                Component found = findComponentByName(childContainer, name);
                if (found != null)
                    return found;
            }
        }
        return null;
    }

    /**
     * Aggiorna il testo della label descrizione della bacheca.
     */
    private void updateDescriptionLabelText() {
        if (this.boardDescriptionValue != null && !this.boardDescriptionValue.trim().isEmpty()) {
            descriptionLabelComponent.setText(
                    "<html><body style='width: 200px;'>" + escapeHtml(this.boardDescriptionValue) + "</body></html>");
        } else {
            descriptionLabelComponent
                    .setText("<html><span style='color:black;'>Nessuna descrizione per questa bacheca.</span></html>");
        }
    }

    /**
     * Aggiorna il nome visualizzato della bacheca nel pannello.
     * @param nuovoNomeVisualizzato Nuovo nome da mostrare
     */
    public void aggiornaNomeVisualizzato(String nuovoNomeVisualizzato) {
        titleLabelComponent.setText(nuovoNomeVisualizzato);
    }

    /**
     * Aggiorna la descrizione visualizzata della bacheca nel pannello.
     * @param nuovaDescrizione Nuova descrizione da mostrare
     */
    public void aggiornaDescrizioneVisualizzata(String nuovaDescrizione) {
        this.boardDescriptionValue = nuovaDescrizione;
        updateDescriptionLabelText();
    }

    /**
     * Restituisce il nome canonico (enum) della bacheca.
     * @return Nome canonico della bacheca
     */
    public String getBoardCanonicalName() {
        try {
            return TitoloBacheca.fromDisplayName(boardDisplayName).name();
        } catch (IllegalArgumentException _) {
            return boardDisplayName;
        }
    }

    /**
     * Restituisce il nome visualizzato della bacheca.
     * @return Nome visualizzato della bacheca
     */
    public String getBoardDisplayName() {
        return boardDisplayName;
    }

    /**
     * Segna tutti i Task della bacheca come completati.
     * Mostra un messaggio di conferma all'utente.
     */
    public void completaTuttiITasks() {
        if (!controller.isUserLoggedIn()) return;
        if (todosCache == null) return;
        boolean changed = false;
        for (ToDo todo : this.todosCache) {
            if (todo.getStato() != StatoToDo.COMPLETATO && controller.modificaToDo(todo,
                    new Controller.ToDoUpdateParams(null, null, null, StatoToDo.COMPLETATO, null, null, null))) {
                changed = true;
            }
        }
        if (changed) {
            refreshToDoList();
        } else {
            JOptionPane.showMessageDialog(this, "Nessun ToDo da completare in questa bacheca.", ERRORE,
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Restituisce la lista dei Task della bacheca.
     * @return Lista dei Task della bacheca
     */
    public List<ToDo> getTodos() {
        return todosCache == null ? List.of() : new ArrayList<>(todosCache);
    }

    private void styleActionButton(JButton button) {
        button.setFont(FontManager.getRegular(12f));
        button.setBackground(Color.BLACK);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.DARK_GRAY);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.BLACK);
            }
        });
    }

    /**
     * Effettua l'escape dei caratteri HTML in una stringa.
     * @param text Testo da processare
     * @return Stringa con escape HTML
     */
    private String escapeHtml(String text) {
        if (text == null)
            return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'",
                "&#39;");
    }

    /**
     * Aggiorna la lista dei Task visualizzati nel pannello, sincronizzandola con il modello.
     */
    public void refreshToDoList() {
        if (!controller.isUserLoggedIn() || boardTitleEnum == null) {
            return;
        }
        Optional<Bacheca> bachecaAggiornataOpt = controller.getUtenteCorrente().getBachecaByTitolo(this.boardTitleEnum);
        if (bachecaAggiornataOpt.isPresent()) {
            this.todosCache = new ArrayList<>(bachecaAggiornataOpt.get().getTodos());
            sortTodosCacheByPriorityAndDate();
            refreshToDoListDisplay();
        } else {
            logger.error(
                    "BoardPanel: Impossibile trovare la bacheca '{}' nel modello del controller durante il refresh.",
                    this.boardDisplayName);
            this.todosCache = Collections.emptyList();
            refreshToDoListDisplay();
        }
    }

    /**
     * Ordina la lista Task in base a priorità e data.
     */
    private void sortTodosCacheByPriorityAndDate() {
        final LocalDate oggi = LocalDate.now();
        this.todosCache.sort((t1, t2) -> compareToDoForSorting(t1, t2, oggi));
    }

    /**
     * Confronta due Task per l'ordinamento personalizzato.
     * @param t1 Primo Task
     * @param t2 Secondo Task
     * @param oggi Data odierna
     * @return Valore di confronto
     */
    private int compareToDoForSorting(ToDo t1, ToDo t2, LocalDate oggi) {
        int statoCompare = compareStato(t1, t2);
        if (statoCompare != 0)
            return statoCompare;

        int dateCompare = compareDateNulls(t1.getScadenza(), t2.getScadenza());
        if (dateCompare != 0)
            return dateCompare;

        int groupCompare = compareDateGroups(t1.getScadenza(), t2.getScadenza(), oggi);
        if (groupCompare != 0)
            return groupCompare;

        return t1.getScadenza().compareTo(t2.getScadenza());
    }

    /**
     * Confronta lo stato di due Task (completato/non completato).
     * @param t1 Primo Task
     * @param t2 Secondo Task
     * @return Valore di confronto
     */
    private int compareStato(ToDo t1, ToDo t2) {
        if (t1.getStato() == StatoToDo.COMPLETATO && t2.getStato() != StatoToDo.COMPLETATO)
            return 1;
        if (t1.getStato() != StatoToDo.COMPLETATO && t2.getStato() == StatoToDo.COMPLETATO)
            return -1;
        return 0;
    }

    /**
     * Confronta la presenza di date (null/non null) tra due Task.
     * @param d1 Prima data
     * @param d2 Seconda data
     * @return Valore di confronto
     */
    private int compareDateNulls(LocalDate d1, LocalDate d2) {
        if (d1 == null && d2 == null)
            return 0;
        if (d1 == null)
            return 1;
        if (d2 == null)
            return -1;
        return 0;
    }

    /**
     * Confronta i gruppi di date (scaduto, oggi, entro 7 giorni, altro).
     * @param d1 Prima data
     * @param d2 Seconda data
     * @param oggi Data odierna
     * @return Valore di confronto
     */
    private int compareDateGroups(LocalDate d1, LocalDate d2, LocalDate oggi) {
        int group1 = getDateGroup(d1, oggi);
        int group2 = getDateGroup(d2, oggi);
        return Integer.compare(group1, group2);
    }

    /**
     * Restituisce il gruppo di una data per l'ordinamento (scaduto, oggi, entro 7 giorni, altro).
     * @param d Data da valutare
     * @param oggi Data odierna
     * @return Intero rappresentante il gruppo
     */
    private int getDateGroup(LocalDate d, LocalDate oggi) {
        if (d == null)
            return Integer.MAX_VALUE;
        if (d.isBefore(oggi))
            return 0;
        if (d.isEqual(oggi))
            return 1;
        if (d.isBefore(oggi.plusDays(8)))
            return 2;
        return 3;
    }
}