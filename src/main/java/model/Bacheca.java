package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Modello che rappresenta una bacheca di Task per un utente.
 * Contiene titolo, descrizione, lista di Task e informazioni di persistenza.
 */
public class Bacheca {
    private static final Logger LOGGER = Logger.getLogger(Bacheca.class.getName());
    private static final String TODO_PREFIX = "Task '";
    private int id; // NUOVO CAMPO
    private int utenteId; // NUOVO CAMPO (FK a Utente)
    private TitoloBacheca titolo;
    private String descrizione;
    private List<ToDo> todos;

    /**
     * Costruttore base. Crea una bacheca con titolo e descrizione.
     * @param titolo Titolo della bacheca (enum)
     * @param descrizione Descrizione della bacheca
     */
    public Bacheca(TitoloBacheca titolo, String descrizione) {
        this.titolo = Objects.requireNonNull(titolo, "Il titolo della bacheca non può essere nullo.");
        this.descrizione = descrizione;
        this.todos = new ArrayList<>();
        this.id = 0; // Inizializza a 0 per indicare non persistito
        this.utenteId = 0; // Inizializza a 0
    }

    /**
     * Costruttore con ID e utenteId (utile per DAO/persistenza).
     * @param id ID della bacheca
     * @param utenteId ID dell'utente proprietario
     * @param titolo Titolo della bacheca
     * @param descrizione Descrizione della bacheca
     */
    public Bacheca(int id, int utenteId, TitoloBacheca titolo, String descrizione) {
        this(titolo, descrizione);
        this.id = id;
        this.utenteId = utenteId;
    }

    /**
     * Restituisce l'ID della bacheca.
     * @return ID bacheca
     */
    public int getId() { return id; }
    /**
     * Imposta l'ID della bacheca.
     * @param id Nuovo ID
     */
    public void setId(int id) { this.id = id; }

    /**
     * Restituisce l'ID dell'utente proprietario.
     * @return ID utente
     */
    public int getUtenteId() { return utenteId; }
    /**
     * Imposta l'ID dell'utente proprietario.
     * @param utenteId Nuovo ID utente
     */
    public void setUtenteId(int utenteId) { this.utenteId = utenteId; }

    /**
     * Restituisce il titolo (enum) della bacheca.
     * @return TitoloBacheca
     */
    public TitoloBacheca getTitoloEnum() { return titolo; }
    /**
     * Restituisce il nome visualizzato del titolo della bacheca.
     * @return Nome visualizzato
     */
    public String getTitoloDisplayName() { return titolo.getDisplayName(); }

    /**
     * Restituisce la descrizione della bacheca.
     * @return Descrizione
     */
    public String getDescrizione() { return descrizione; }
    /**
     * Imposta la descrizione della bacheca.
     * @param descrizione Nuova descrizione
     */
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }

    /**
     * Restituisce la lista dei Task (non modificabile).
     * @return Lista di Task
     */
    public List<ToDo> getTodos() {
        return Collections.unmodifiableList(todos);
    }

    /**
     * Imposta la lista dei Task (sovrascrive quella esistente).
     * @param todos Nuova lista di Task
     */
    public void setTodos(List<ToDo> todos) {
        this.todos = new ArrayList<>(todos);
    }

    /**
     * Aggiunge un Task alla bacheca se non già presente.
     * @param todo Task da aggiungere
     * @return true se aggiunto, false se già presente
     */
    public boolean aggiungiToDo(ToDo todo) {
        Objects.requireNonNull(todo, "Il Task da aggiungere non può essere nullo.");
        if (this.todos.stream().anyMatch(t -> t.equals(todo))) {
            LOGGER.info(TODO_PREFIX + todo.getTitolo() + "' è già presente nella bacheca '" + getTitoloDisplayName() + "'.");
            return false;
        }
        this.todos.add(todo);
        LOGGER.info(TODO_PREFIX + todo.getTitolo() + "' aggiunto alla bacheca '" + getTitoloDisplayName() + "' (localmente).");
        return true;
    }

    /**
     * Rimuove un Task dalla bacheca.
     * @param todo Task da rimuovere
     * @return true se rimosso, false se non presente
     */
    public boolean eliminaToDo(ToDo todo) {
        Objects.requireNonNull(todo, "Il Task da eliminare non può essere nullo.");
        boolean removed = this.todos.remove(todo);
        if (removed) {
            LOGGER.info(TODO_PREFIX + todo.getTitolo() + "' rimosso dalla bacheca '" + getTitoloDisplayName() + "' (localmente).");
        }
        return removed;
    }

    /**
     * Modifica l'ordine di un Task nella lista.
     * @param todo Task da spostare
     * @param nuovaPosizione Nuova posizione (indice)
     * @return true se lo spostamento ha successo, false altrimenti
     */
    public boolean modificaOrdineToDo(ToDo todo, int nuovaPosizione) {
        if (todo == null || !todos.contains(todo) || nuovaPosizione < 0 || nuovaPosizione >= todos.size()) {
            return false;
        }
        todos.remove(todo);
        todos.add(nuovaPosizione, todo);
        // Aggiorna le posizioni locali, ma l'aggiornamento nel DB avverrà tramite il Controller/DAO
        for (int i = 0; i < todos.size(); i++) {
            todos.get(i).setPosizione(i);
        }
        return true;
    }

    /**
     * Confronta due bacheche per uguaglianza logica (ID o combinazione utenteId+titolo).
     * @param o Oggetto da confrontare
     * @return true se uguali, false altrimenti
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bacheca bacheca = (Bacheca) o;
        // Se l'ID è stato assegnato (non 0), confronta solo per ID.
        // Questo è il modo più robusto per oggetti persistiti.
        if (id != 0 && bacheca.id != 0) {
            return id == bacheca.id;
        }
        // Se almeno uno degli ID è 0 (non persistito o nuovo), confronta per titolo e utenteId
        // (o altri campi che definiscono l'unicità prima della persistenza).
        // Questo può essere problematico se TitoloBacheca non è sufficientemente unico per un utente.
        // La combinazione utente_id + titolo_bacheca è UNIQUE nel DB.
        return titolo == bacheca.titolo && utenteId == bacheca.utenteId;
    }

    /**
     * Calcola l'hashCode della bacheca (ID se presente, altrimenti utenteId+titolo).
     * @return hashCode
     */
    @Override
    public int hashCode() {
        // Se l'ID è stato assegnato (non 0), usa solo l'ID per l'hash.
        if (id != 0) {
            return Objects.hash(id);
        }
        // Altrimenti, usa i campi che definiscono l'unicità prima della persistenza.
        // Essendo l'accoppiata (utenteId, titolo) un unique constraint nel DB,
        // ha senso usarla qui per l'hash di oggetti non ancora persistiti o con ID 0.
        return Objects.hash(titolo, utenteId);
    }

    /**
     * Restituisce una rappresentazione testuale della bacheca.
     * @return Stringa descrittiva
     */
    @Override
    public String toString() {
        return "Bacheca: " + titolo.getDisplayName() + (descrizione != null && !descrizione.isEmpty() ? " (" + descrizione + ")" : "") + (id != 0 ? " [ID:" + id + "]" : "");
    }
}