package model;

import java.awt.Image;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * Modello che rappresenta un singolo Task.
 * Contiene titolo, descrizione, autore, scadenza, stato, colore, immagine, posizione e informazioni di condivisione.
 */
public class ToDo {
    private int id;
    private Integer bachecaId;
    private String titolo;
    private String url;
    private String descrizione;
    private LocalDate scadenza;
    private Image immagine;
    private int posizione;
    private StatoToDo stato;
    private LocalDate creazione;
    private String colore;
    private Utente autore; // L'oggetto Utente completo
    private int autoreId; // L'ID dell'autore, per la persistenza
    private Integer bachecaDestinazioneId; // ID della bacheca di destinazione per Task condivisi

    private Set<Utente> utentiConAccessoCondiviso = new HashSet<>(); // Utenti con cui è stato condiviso

    /**
     * Costruttore principale. Crea un nuovo Task con i dati essenziali.
     * @param titolo Titolo del Task
     * @param descrizione Descrizione del Task
     * @param autore Utente autore
     * @param scadenza Data di scadenza
     * @param colore Colore associato
     */
    public ToDo(String titolo, String descrizione, Utente autore, LocalDate scadenza, String colore) {
        this.titolo = Objects.requireNonNull(titolo, "Il titolo non può essere nullo");
        this.descrizione = Objects.requireNonNull(descrizione, "La descrizione non può essere nulla");
        this.autore = Objects.requireNonNull(autore, "L'autore non può essere nullo");
        this.autoreId = autore.getId(); // Prende l'ID dell'autore
        this.scadenza = scadenza;
        this.colore = colore;
        this.creazione = LocalDate.now();
        this.stato = StatoToDo.NON_COMPLETATO;
        this.url = "";
        this.immagine = null;
        this.posizione = 0; // Posizione iniziale
        this.id = 0; // Inizializza a 0 per indicare non persistito
        this.bachecaId = null; // Inizializza a null
    }

    /**
     * Costruttore completo per DAO/persistenza (caricamento da DB).
     * @param id ID Task
     * @param bachecaId ID bacheca
     * @param autoreId ID autore
     * @param titolo Titolo
     * @param descrizione Descrizione
     * @param url URL associato
     * @param scadenza Data di scadenza
     * @param immagine Immagine
     * @param posizione Posizione nella lista
     * @param stato Stato del Task
     * @param creazione Data di creazione
     * @param colore Colore associato
     */
    @SuppressWarnings("java:S107")
    public ToDo(int id, Integer bachecaId, int autoreId, String titolo, String descrizione, String url,
                LocalDate scadenza, Image immagine, int posizione, StatoToDo stato, LocalDate creazione, String colore) {
        this.id = id;
        this.bachecaId = bachecaId;
        this.autoreId = autoreId;
        this.titolo = Objects.requireNonNull(titolo);
        this.descrizione = descrizione;
        this.url = url;
        this.scadenza = scadenza;
        this.immagine = immagine;
        this.posizione = posizione;
        this.stato = Objects.requireNonNull(stato);
        this.creazione = Objects.requireNonNull(creazione);
        this.colore = colore;
        this.utentiConAccessoCondiviso = new HashSet<>(); // Inizializzato vuoto, verrà popolato a parte dal DAO
        this.autore = null; // Autore sarà impostato dopo con utenteDAO.findById(autoreId)
    }

    /**
     * Builder per la creazione flessibile di oggetti Task.
     */
    public static class Builder {
        private int id = 0;
        private Integer bachecaId = null;
        private int autoreId;
        private String titolo;
        private String descrizione;
        private String url = "";
        private LocalDate scadenza;
        private Image immagine;
        private int posizione = 0;
        private Integer bachecaDestinazioneId = null;
        private StatoToDo stato = StatoToDo.NON_COMPLETATO;
        private LocalDate creazione = LocalDate.now();
        private String colore;
        private Utente autore;
        private Set<Utente> utentiConAccessoCondiviso = new HashSet<>();

        /**
         * Costruttore di default per il Builder.
         */
        public Builder() {
            // Costruttore vuoto per inizializzazione
        }

        /** * Imposta l'ID del Task.
         * @param id ID del Task.
         * @return this
         */
        public Builder id(int id) { this.id = id; return this; }

        /** * Imposta l'ID della bacheca.
         * @param bachecaId ID bacheca.
         * @return this
         */
        public Builder bachecaId(Integer bachecaId) { this.bachecaId = bachecaId; return this; }

        /** * Imposta l'ID dell'autore.
         * @param autoreId ID autore.
         * @return this
         */
        public Builder autoreId(int autoreId) { this.autoreId = autoreId; return this; }

        /** * Imposta il titolo.
         * @param titolo Titolo.
         * @return this
         */
        public Builder titolo(String titolo) { this.titolo = titolo; return this; }

        /** * Imposta la descrizione.
         * @param descrizione Descrizione.
         * @return this
         */
        public Builder descrizione(String descrizione) { this.descrizione = descrizione; return this; }

        /** * Imposta l'URL.
         * @param url URL.
         * @return this
         */
        public Builder url(String url) { this.url = url; return this; }

        /** * Imposta la data di scadenza.
         * @param scadenza Data scadenza.
         * @return this
         */
        public Builder scadenza(LocalDate scadenza) { this.scadenza = scadenza; return this; }

        /** * Imposta l'immagine.
         * @param immagine Immagine.
         * @return this
         */
        public Builder immagine(Image immagine) { this.immagine = immagine; return this; }

        /** * Imposta la posizione.
         * @param posizione Posizione.
         * @return this
         */
        public Builder posizione(int posizione) { this.posizione = posizione; return this; }

        /** * Imposta lo stato.
         * @param stato Stato.
         * @return this
         */
        public Builder stato(StatoToDo stato) { this.stato = stato; return this; }

        /** * Imposta la data di creazione.
         * @param creazione Data creazione.
         * @return this
         */
        public Builder creazione(LocalDate creazione) { this.creazione = creazione; return this; }

        /** * Imposta il colore.
         * @param colore Colore.
         * @return this
         */
        public Builder colore(String colore) { this.colore = colore; return this; }

        /** * Imposta l'ID della bacheca di destinazione per la condivisione.
         * @param bachecaDestinazioneId ID bacheca destinazione condivisione.
         * @return this
         */
        public Builder bachecaDestinazioneId(Integer bachecaDestinazioneId) { this.bachecaDestinazioneId = bachecaDestinazioneId; return this; }

        /** * Imposta l'oggetto autore.
         * @param autore Oggetto autore.
         * @return this
         */
        public Builder autore(Utente autore) { this.autore = autore; return this; }

        /** * Imposta gli utenti con accesso condiviso.
         * @param utenti Set utenti condivisione.
         * @return this
         */
        public Builder utentiConAccessoCondiviso(Set<Utente> utenti) { this.utentiConAccessoCondiviso = utenti; return this; }

        /**
         * Costruisce e restituisce un nuovo oggetto Task.
         * @return Nuovo Task
         */
        public ToDo build() {
            return new ToDo(this);
        }
    }

    /**
     * Costruttore privato usato dal Builder.
     * @param builder Oggetto Builder
     */
    private ToDo(Builder builder) {
        this.id = builder.id;
        this.bachecaId = builder.bachecaId;
        this.autoreId = builder.autoreId;
        this.titolo = builder.titolo;
        this.descrizione = builder.descrizione;
        this.url = builder.url;
        this.scadenza = builder.scadenza;
        this.immagine = builder.immagine;
        this.posizione = builder.posizione;
        this.stato = builder.stato;
        this.creazione = builder.creazione;
        this.colore = builder.colore;
        this.bachecaDestinazioneId = builder.bachecaDestinazioneId;
        this.autore = builder.autore;
        this.utentiConAccessoCondiviso = builder.utentiConAccessoCondiviso;
    }

    // Getters e Setters
    /** * Restituisce l'ID del Task.
     * @return ID Task
     */
    public int getId() { return id; }
    /** * Imposta l'ID del Task.
     * @param id Nuovo ID
     */
    public void setId(int id) { this.id = id; }
    /** * Restituisce l'ID della bacheca.
     * @return ID bacheca
     */
    public Integer getBachecaId() { return bachecaId; }
    /** * Imposta l'ID della bacheca.
     * @param bachecaId Nuovo ID bacheca
     */
    public void setBachecaId(Integer bachecaId) { this.bachecaId = bachecaId; }
    /** * Restituisce l'ID dell'autore.
     * @return ID autore
     */
    public int getAutoreId() { return autoreId; }
    /** * Imposta l'ID dell'autore.
     * @param autoreId Nuovo ID autore
     */
    public void setAutoreId(int autoreId) { this.autoreId = autoreId; }
    /** * Restituisce il titolo del Task.
     * @return Titolo
     */
    public String getTitolo() { return titolo; }
    /** * Imposta il titolo del Task.
     * @param titolo Nuovo titolo
     */
    public void setTitolo(String titolo) { this.titolo = titolo; }
    /** * Restituisce l'URL associato.
     * @return URL
     */
    public String getUrl() { return url; }
    /** * Imposta l'URL associato.
     * @param url Nuovo URL
     */
    public void setUrl(String url) { this.url = url; }
    /** * Restituisce la descrizione del Task.
     * @return Descrizione
     */
    public String getDescrizione() { return descrizione; }
    /** * Imposta la descrizione del Task.
     * @param descrizione Nuova descrizione
     */
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }
    /** * Restituisce la data di scadenza.
     * @return Data di scadenza
     */
    public LocalDate getScadenza() { return scadenza; }
    /** * Imposta la data di scadenza.
     * @param scadenza Nuova data di scadenza
     */
    public void setScadenza(LocalDate scadenza) { this.scadenza = scadenza; }
    /** * Restituisce l'immagine associata.
     * @return Immagine
     */
    public Image getImmagine() { return immagine; }
    /** * Imposta l'immagine associata.
     * @param immagine Nuova immagine
     */
    public void setImmagine(Image immagine) { this.immagine = immagine; }
    /** * Restituisce la posizione del Task nella lista.
     * @return Posizione
     */
    public int getPosizione() { return posizione; }
    /** * Imposta la posizione del Task nella lista.
     * @param posizione Nuova posizione
     */
    public void setPosizione(int posizione) { this.posizione = posizione; }
    /** * Restituisce lo stato del Task.
     * @return Stato
     */
    public StatoToDo getStato() { return stato; }
    /** * Imposta lo stato del Task.
     * @param stato Nuovo stato
     */
    public void setStato(StatoToDo stato) { this.stato = stato; }
    /** * Restituisce la data di creazione.
     * @return Data creazione
     */
    public LocalDate getCreazione() { return creazione; }
    /** * Imposta la data di creazione.
     * @param creazione Nuova data creazione
     */
    public void setCreazione(LocalDate creazione) { this.creazione = creazione; }
    /** * Restituisce il colore associato.
     * @return Colore
     */
    public String getColore() { return colore; }
    /** * Imposta il colore associato.
     * @param colore Nuovo colore
     */
    public void setColore(String colore) { this.colore = colore; }

    /**
     * Restituisce l'ID della bacheca di destinazione per la condivisione.
     * @return ID bacheca destinazione
     */
    public Integer getBachecaDestinazioneId() { return bachecaDestinazioneId; }

    /**
     * Imposta l'ID della bacheca di destinazione per la condivisione.
     * @param bachecaDestinazioneId ID bacheca destinazione
     */
    public void setBachecaDestinazioneId(Integer bachecaDestinazioneId) { this.bachecaDestinazioneId = bachecaDestinazioneId; }

    /** * Restituisce l'autore (oggetto Utente).
     * @return Autore
     */
    public Utente getAutore() { return autore; }
    /** * Imposta l'autore (oggetto Utente) e aggiorna autoreId.
     * @param autore Nuovo autore
     */
    public void setAutore(Utente autore) {
        this.autore = autore;
        if (autore != null) {
            this.autoreId = autore.getId(); // Assicura che autoreId sia coerente
        }
    }

    /**
     * Restituisce l'insieme degli utenti con cui il Task è condiviso.
     * @return Set di utenti
     */
    public Set<Utente> getUtentiConAccessoCondiviso() {
        return utentiConAccessoCondiviso;
    }

    /**
     * Imposta l'insieme degli utenti con cui il Task è condiviso.
     * @param utenti Set di utenti
     */
    public void setUtentiConAccessoCondiviso(Set<Utente> utenti) {
        this.utentiConAccessoCondiviso = utenti != null ? new HashSet<>(utenti) : new HashSet<>();
    }

    /**
     * Aggiunge un utente all'insieme di condivisione.
     * @param utente Utente da aggiungere
     */
    public void aggiungiUtenteCondiviso(Utente utente) {
        this.utentiConAccessoCondiviso.add(utente);
    }

    /**
     * Rimuove un utente dall'insieme di condivisione.
     * @param utente Utente da rimuovere
     */
    public void rimuoviUtenteCondiviso(Utente utente) {
        this.utentiConAccessoCondiviso.remove(utente);
    }

    /**
     * Restituisce una rappresentazione testuale del Task.
     * @return Stringa descrittiva
     */
    @Override
    public String toString() {
        return titolo + (scadenza != null ? " (Scade: " + scadenza.toString() + ")" : "") + (id != 0 ? " [ID:" + id + "]" : "");
    }

    /**
     * Confronta due Task per uguaglianza logica (ID o campi principali).
     * @param o Oggetto da confrontare
     * @return true se uguali, false altrimenti
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ToDo toDo = (ToDo) o;
        // Se entrambi hanno ID (non 0), confronta per ID. Questo è l'identificatore unico nel DB.
        if (id != 0 && toDo.id != 0) {
            return id == toDo.id;
        }
        return titolo.equals(toDo.titolo) &&
                creazione.equals(toDo.creazione) && // La creazione è un buon discriminante
                Objects.equals(autore, toDo.autore) && // Confronta gli oggetti Utente (che hanno ID)
                Objects.equals(descrizione, toDo.descrizione);
    }

    /**
     * Calcola l'hashCode del Task (ID se presente, altrimenti campi principali).
     * @return hashCode
     */
    @Override
    public int hashCode() {
        // Se l'ID è stato assegnato (non 0), usa solo l'ID per l'hash.
        if (id != 0) {
            return Objects.hash(id);
        }
        // Altrimenti, usa i campi che definiscono l'unicità prima della persistenza.
        return Objects.hash(titolo, creazione, autore, descrizione);
    }
}