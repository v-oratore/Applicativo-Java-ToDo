package model;

import java.awt.Image;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Modello che rappresenta un utente dell'applicazione Task.
 * Gestisce username, password hash, bacheche e operazioni locali su Task e bacheche.
 */
public class Utente {
    private static final Logger LOGGER = Logger.getLogger(Utente.class.getName());
    // FIX: Simplified the constant to remove special formatting characters.
    private static final String TODO_PREFIX = ": Task";
    private int id;
    private String username;
    private String passwordHash;
    private List<Bacheca> bacheche;

    /**
     * Costruttore principale. Crea un utente con username e password.
     * @param username Username dell'utente
     * @param password Password in chiaro (verrà hashata)
     */
    public Utente(String username, String password) {
        this.username = Objects.requireNonNull(username, "Username non può essere nullo.");
        setPassword(password);
        this.bacheche = new ArrayList<>(3);
        this.id = 0;
    }

    /**
     * Costruttore con ID, username e password hash (per DAO/persistenza).
     * @param id ID utente
     * @param username Username
     * @param passwordHash Hash della password
     */
    public Utente(int id, String username, String passwordHash) {
        this.id = id;
        this.username = Objects.requireNonNull(username, "Username non può essere nullo.");
        this.passwordHash = Objects.requireNonNull(passwordHash, "Password hash non può essere nulla.");
        this.bacheche = new ArrayList<>(3);
    }

    /** * Restituisce l'ID dell'utente.
     * @return ID utente
     */
    public int getId() { return id; }
    /** * Imposta l'ID dell'utente.
     * @param id Nuovo ID
     */
    public void setId(int id) { this.id = id; }
    /** * Restituisce lo username.
     * @return Username
     */
    public String getUsername() { return username; }
    /** * Restituisce l'hash della password.
     * @return Hash password
     */
    public String getPasswordHash() { return passwordHash; }
    /** * Imposta la password (viene hashata).
     * @param password Password in chiaro
     */
    public void setPassword(String password) { this.passwordHash = hashPassword(password); }

    /**
     * Esegue l'hash della password in chiaro usando SHA-256.
     * @param password Password in chiaro
     * @return Hash base64 della password
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.log(Level.SEVERE, "Errore nell''algoritmo di hashing: {0}", e.getMessage());
            return null;
        }
    }

    /**
     * Verifica se la password fornita corrisponde all'hash memorizzato.
     * @param password Password in chiaro
     * @return true se la password è corretta, false altrimenti
     */
    public boolean checkPassword(String password) {
        return passwordHash.equals(hashPassword(password));
    }

    /**
     * Restituisce la lista delle bacheche dell'utente (non modificabile).
     * @return Lista di bacheche
     */
    public List<Bacheca> getBacheche() {
        return Collections.unmodifiableList(bacheche);
    }

    /**
     * Imposta la lista delle bacheche dell'utente.
     * @param bacheche Lista di bacheche
     */
    public void setBacheche(List<Bacheca> bacheche) {
        this.bacheche = new ArrayList<>(bacheche);
    }

    /**
     * Aggiunge una bacheca caricata all'utente se non già presente.
     * @param bacheca Bacheca da aggiungere
     * @return true se aggiunta, false se già presente
     */
    public boolean aggiungiBachecaCaricata(Bacheca bacheca) {
        Objects.requireNonNull(bacheca, "La bacheca da aggiungere non può essere nulla.");
        if (this.bacheche.stream().anyMatch(b -> b.equals(bacheca))) {
            LOGGER.log(Level.INFO, "{0}: Bacheca ''{1}'' (ID: {2}) è già presente in memoria.",
                    new Object[]{username, bacheca.getTitoloDisplayName(), bacheca.getId()});
            return false;
        }
        this.bacheche.add(bacheca);
        LOGGER.log(Level.INFO, "{0}: Bacheca ''{1}'' (ID: {2}) aggiunta (localmente).",
                new Object[]{username, bacheca.getTitoloDisplayName(), bacheca.getId()});
        return true;
    }

    /**
     * Modifica la descrizione di una bacheca esistente.
     * @param titoloDaModificare Titolo della bacheca da modificare
     * @param nuovaDescrizione Nuova descrizione
     * @return true se modificata, false altrimenti
     */
    public boolean modificaBacheca(TitoloBacheca titoloDaModificare, String nuovaDescrizione) {
        Optional<Bacheca> bachecaOpt = getBachecaByTitolo(titoloDaModificare);
        if (bachecaOpt.isPresent()) {
            bachecaOpt.get().setDescrizione(nuovaDescrizione);
            LOGGER.log(Level.INFO, "{0}: Descrizione bacheca ''{1}'' modificata (localmente).",
                    new Object[]{username, titoloDaModificare.getDisplayName()});
            return true;
        }
        LOGGER.log(Level.INFO, "{0}: Bacheca ''{1}'' non trovata per la modifica.",
                new Object[]{username, titoloDaModificare.getDisplayName()});
        return false;
    }

    /**
     * Elimina una bacheca dell'utente.
     * @param titoloDaEliminare Titolo della bacheca da eliminare
     * @return true se eliminata, false altrimenti
     */
    public boolean eliminaBacheca(TitoloBacheca titoloDaEliminare) {
        boolean removed = bacheche.removeIf(b -> b.getTitoloEnum() == titoloDaEliminare);
        if (removed) {
            LOGGER.log(Level.INFO, "{0}: Bacheca ''{1}'' eliminata (localmente).",
                    new Object[]{username, titoloDaEliminare.getDisplayName()});
        } else {
            LOGGER.log(Level.INFO, "{0}: Bacheca ''{1}'' non trovata per l''eliminazione.",
                    new Object[]{username, titoloDaEliminare.getDisplayName()});
        }
        return removed;
    }

    /**
     * Restituisce una bacheca dato il titolo (enum).
     * @param titolo Titolo della bacheca
     * @return Optional con la bacheca se trovata
     */
    public Optional<Bacheca> getBachecaByTitolo(TitoloBacheca titolo) {
        return bacheche.stream().filter(b -> b.getTitoloEnum() == titolo).findFirst();
    }

    /**
     * Restituisce una bacheca dato il nome visualizzato.
     * @param displayName Nome visualizzato
     * @return Optional con la bacheca se trovata
     */
    public Optional<Bacheca> getBachecaByDisplayName(String displayName) {
        try {
            TitoloBacheca titoloEnum = TitoloBacheca.fromDisplayName(displayName);
            return getBachecaByTitolo(titoloEnum);
        } catch (IllegalArgumentException _) {
            return Optional.empty();
        }
    }

    /**
     * Crea e aggiunge un nuovo Task a una bacheca.
     * @param bachecaDestinazione Titolo bacheca destinazione
     * @param titolo Titolo Task
     * @param descrizione Descrizione Task
     * @param scadenza Data scadenza
     * @param colore Colore
     * @param url URL associato
     * @param immagine Immagine
     * @return Optional con il Task creato
     */
    public Optional<ToDo> creaToDo(TitoloBacheca bachecaDestinazione, String titolo, String descrizione,
                                   LocalDate scadenza, String colore, String url, Image immagine) {
        Optional<Bacheca> bachecaOpt = getBachecaByTitolo(bachecaDestinazione);
        if (bachecaOpt.isPresent()) {
            ToDo nuovoToDo = new ToDo(titolo, descrizione, this, scadenza, colore);
            if (url != null && !url.isEmpty())
                nuovoToDo.setUrl(url);
            if (immagine != null)
                nuovoToDo.setImmagine(immagine);

            bachecaOpt.get().aggiungiToDo(nuovoToDo);
            LOGGER.log(Level.INFO, "{0}{1} ''{2}'' creato e aggiunto a ''{3}'' (localmente).",
                    new Object[]{username, TODO_PREFIX, titolo, bachecaDestinazione.getDisplayName()});
            return Optional.of(nuovoToDo);
        }
        LOGGER.log(Level.INFO, "{0}: Bacheca ''{1}'' non trovata per creare Task.",
                new Object[]{username, bachecaDestinazione.getDisplayName()});
        return Optional.empty();
    }

    /**
     * Oggetto di supporto per la modifica di un Task (parameter object).
     */
    public static class ToDoUpdate {
        private String nuovoTitolo;
        private String nuovaDescrizione;
        private LocalDate nuovaScadenza;
        private StatoToDo nuovoStato;
        private String nuovoColore;
        private String nuovoUrl;
        private Image nuovaImmagine;

        /**
         * Costruttore di default per ToDoUpdate.
         */
        public ToDoUpdate() {
            // Costruttore vuoto
        }

        /** * Restituisce il nuovo titolo.
         * @return Il nuovo titolo
         */
        public String getNuovoTitolo() { return nuovoTitolo; }
        /** * Imposta il nuovo titolo.
         * @param nuovoTitolo Il nuovo titolo
         */
        public void setNuovoTitolo(String nuovoTitolo) { this.nuovoTitolo = nuovoTitolo; }

        /** * Restituisce la nuova descrizione.
         * @return La nuova descrizione
         */
        public String getNuovaDescrizione() { return nuovaDescrizione; }
        /** * Imposta la nuova descrizione.
         * @param nuovaDescrizione La nuova descrizione
         */
        public void setNuovaDescrizione(String nuovaDescrizione) { this.nuovaDescrizione = nuovaDescrizione; }

        /** * Restituisce la nuova data di scadenza.
         * @return La nuova data di scadenza
         */
        public LocalDate getNuovaScadenza() { return nuovaScadenza; }
        /** * Imposta la nuova data di scadenza.
         * @param nuovaScadenza La nuova data di scadenza
         */
        public void setNuovaScadenza(LocalDate nuovaScadenza) { this.nuovaScadenza = nuovaScadenza; }

        /** * Restituisce il nuovo stato.
         * @return Il nuovo stato
         */
        public StatoToDo getNuovoStato() { return nuovoStato; }
        /** * Imposta il nuovo stato.
         * @param nuovoStato Il nuovo stato
         */
        public void setNuovoStato(StatoToDo nuovoStato) { this.nuovoStato = nuovoStato; }

        /** * Restituisce il nuovo colore.
         * @return Il nuovo colore
         */
        public String getNuovoColore() { return nuovoColore; }
        /** * Imposta il nuovo colore.
         * @param nuovoColore Il nuovo colore
         */
        public void setNuovoColore(String nuovoColore) { this.nuovoColore = nuovoColore; }

        /** * Restituisce il nuovo URL.
         * @return Il nuovo URL
         */
        public String getNuovoUrl() { return nuovoUrl; }
        /** * Imposta il nuovo URL.
         * @param nuovoUrl Il nuovo URL
         */
        public void setNuovoUrl(String nuovoUrl) { this.nuovoUrl = nuovoUrl; }

        /** * Restituisce la nuova immagine.
         * @return La nuova immagine
         */
        public Image getNuovaImmagine() { return nuovaImmagine; }
        /** * Imposta la nuova immagine.
         * @param nuovaImmagine La nuova immagine
         */
        public void setNuovaImmagine(Image nuovaImmagine) { this.nuovaImmagine = nuovaImmagine; }
    }

    /**
     * Modifica un Task esistente con i parametri specificati.
     * @param todoDaModificare Task da modificare
     * @param update Parametri di aggiornamento
     * @return true se modificato, false altrimenti
     */
    public boolean modificaToDo(ToDo todoDaModificare, ToDoUpdate update) {
        if (todoDaModificare == null || update == null)
            return false;

        boolean modificato = false;
        modificato |= updateField(() -> update.getNuovoTitolo() != null && !update.getNuovoTitolo().equals(todoDaModificare.getTitolo()), () -> todoDaModificare.setTitolo(update.getNuovoTitolo()));
        modificato |= updateField(() -> update.getNuovaDescrizione() != null && !update.getNuovaDescrizione().equals(todoDaModificare.getDescrizione()), () -> todoDaModificare.setDescrizione(update.getNuovaDescrizione()));
        modificato |= updateField(() -> update.getNuovaScadenza() != null && !update.getNuovaScadenza().equals(todoDaModificare.getScadenza()), () -> todoDaModificare.setScadenza(update.getNuovaScadenza()));
        modificato |= updateField(() -> update.getNuovoStato() != null && update.getNuovoStato() != todoDaModificare.getStato(), () -> todoDaModificare.setStato(update.getNuovoStato()));
        modificato |= updateField(() -> update.getNuovoColore() != null && !update.getNuovoColore().equals(todoDaModificare.getColore()), () -> todoDaModificare.setColore(update.getNuovoColore()));
        modificato |= updateField(() -> update.getNuovoUrl() != null && !update.getNuovoUrl().equals(todoDaModificare.getUrl()), () -> todoDaModificare.setUrl(update.getNuovoUrl()));
        modificato |= updateField(() -> update.getNuovaImmagine() != null && !update.getNuovaImmagine().equals(todoDaModificare.getImmagine()), () -> todoDaModificare.setImmagine(update.getNuovaImmagine()));
        modificato |= updateField(() -> update.getNuovaImmagine() == null && todoDaModificare.getImmagine() != null, () -> todoDaModificare.setImmagine(null));

        if (modificato) {
            LOGGER.log(Level.INFO, "{0}{1} ''{2}'' modificato (localmente).",
                    new Object[]{username, TODO_PREFIX, todoDaModificare.getTitolo()});
        }
        return modificato;
    }

    private boolean updateField(java.util.function.BooleanSupplier condition, Runnable updater) {
        if (condition.getAsBoolean()) {
            updater.run();
            return true;
        }
        return false;
    }

    /**
     * Elimina un Task da tutte le bacheche dell'utente.
     * @param todoDaEliminare Task da eliminare
     * @return true se eliminato almeno una volta, false altrimenti
     */
    public boolean eliminaToDo(ToDo todoDaEliminare) {
        if (todoDaEliminare == null)
            return false;
        boolean rimossoAlmenoUnaVolta = false;
        for (Bacheca b : bacheche) {
            if (b.eliminaToDo(todoDaEliminare)) {
                rimossoAlmenoUnaVolta = true;
            }
        }
        if (rimossoAlmenoUnaVolta) {
            LOGGER.log(Level.INFO, "{0}{1} ''{2}'' eliminato dalle proprie bacheche (localmente).",
                    new Object[]{username, TODO_PREFIX, todoDaEliminare.getTitolo()});
        }
        return rimossoAlmenoUnaVolta;
    }

    /**
     * Sposta un Task da una bacheca a un'altra.
     * @param todoDaSpostare Task da spostare
     * @param origine Titolo bacheca origine
     * @param destinazione Titolo bacheca destinazione
     * @return true se spostato, false altrimenti
     */
    public boolean spostaToDo(ToDo todoDaSpostare, TitoloBacheca origine, TitoloBacheca destinazione) {
        if (todoDaSpostare == null || origine == destinazione)
            return false;

        Optional<Bacheca> bachecaOrigineOpt = getBachecaByTitolo(origine);
        Optional<Bacheca> bachecaDestinazioneOpt = getBachecaByTitolo(destinazione);

        if (bachecaOrigineOpt.isPresent() && bachecaDestinazioneOpt.isPresent()) {
            Bacheca bOrigine = bachecaOrigineOpt.get();
            Bacheca bDestinazione = bachecaDestinazioneOpt.get();
            if (bOrigine.getTodos().contains(todoDaSpostare)) {
                bOrigine.eliminaToDo(todoDaSpostare);
                bDestinazione.aggiungiToDo(todoDaSpostare);
                LOGGER.log(Level.INFO, "{0}{1} ''{2}'' spostato da ''{3}'' a ''{4}'' (localmente).",
                        new Object[]{username, TODO_PREFIX, todoDaSpostare.getTitolo(), origine.getDisplayName(), destinazione.getDisplayName()});
                return true;
            }
            LOGGER.log(Level.INFO, "{0}{1} ''{2}'' non trovato in ''{3}''.",
                    new Object[]{username, TODO_PREFIX, todoDaSpostare.getTitolo(), origine.getDisplayName()});
        } else {
            LOGGER.log(Level.INFO, "{0}: Una o entrambe le bacheche (origine/destinazione) non trovate.", username);
        }
        return false;
    }

    /**
     * Cambia la bacheca di un Task.
     * @param todo Task da spostare
     * @param nuovaBacheca Titolo nuova bacheca
     * @return true se spostato, false altrimenti
     */
    public boolean cambiaBachecaToDo(ToDo todo, TitoloBacheca nuovaBacheca) {
        TitoloBacheca bachecaCorrente = null;
        for (Bacheca b : bacheche) {
            if (b.getTodos().contains(todo)) {
                bachecaCorrente = b.getTitoloEnum();
                break;
            }
        }
        if (bachecaCorrente == null) {
            LOGGER.log(Level.INFO, "{0}: Impossibile trovare la bacheca corrente per il Task ''{1}''.",
                    new Object[]{username, todo.getTitolo()});
            return false;
        }
        return spostaToDo(todo, bachecaCorrente, nuovaBacheca);
    }

    /**
     * Condivide un Task con un altro utente su una bacheca specifica.
     * @param toDoDaCondividere Task da condividere
     * @param utenteTarget Utente destinatario
     * @param bachecaTargetTitolo Titolo bacheca destinataria
     * @return true se condiviso, false altrimenti
     */
    public boolean condividiToDo(ToDo toDoDaCondividere, Utente utenteTarget, TitoloBacheca bachecaTargetTitolo) {
        if (toDoDaCondividere == null || utenteTarget == null || bachecaTargetTitolo == null)
            return false;
        if (!toDoDaCondividere.getAutore().equals(this)) {
            LOGGER.log(Level.INFO, "{0}: Puoi condividere solo i Task di cui sei l''autore.", username);
            return false;
        }
        if (utenteTarget.equals(this)) {
            LOGGER.log(Level.INFO, "{0}: Non puoi condividere un Task con te stesso.", username);
            return false;
        }

        toDoDaCondividere.aggiungiUtenteCondiviso(utenteTarget);
        LOGGER.log(Level.INFO,
                "{0}: Tentativo di condivisione di Task ''{1}'' con ''{2}'' (logica DB gestita dal Controller).",
                new Object[]{username, toDoDaCondividere.getTitolo(), utenteTarget.getUsername()});
        return true;
    }

    /**
     * Revoca la condivisione di un Task per un utente specifico.
     * @param toDoCondiviso Task condiviso
     * @param utenteTarget Utente destinatario
     * @param bachecaTargetTitolo Titolo bacheca destinataria
     * @return true se revocata, false altrimenti
     */
    public boolean revocaCondivisione(ToDo toDoCondiviso, Utente utenteTarget, TitoloBacheca bachecaTargetTitolo) {
        if (toDoCondiviso == null || utenteTarget == null || bachecaTargetTitolo == null)
            return false;
        if (!toDoCondiviso.getAutore().equals(this)) {
            LOGGER.log(Level.INFO, "{0}: Puoi revocare la condivisione solo per i Task di cui sei l''autore.", username);
            return false;
        }
        toDoCondiviso.rimuoviUtenteCondiviso(utenteTarget);
        LOGGER.log(Level.INFO,
                "{0}: Tentativo di revoca condivisione Task ''{1}'' per ''{2}'' (logica DB gestita dal Controller).",
                new Object[]{username, toDoCondiviso.getTitolo(), utenteTarget.getUsername()});
        return true;
    }

    /**
     * Ricerca i Task dell'utente in base a una stringa di ricerca.
     * @param searchTerm Termine di ricerca
     * @return Lista di Task che corrispondono alla ricerca
     */
    public List<ToDo> ricercaToDo(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty())
            return getAllToDos();
        String term = searchTerm.toLowerCase();
        return getAllToDos().stream()
                .filter(todo -> (todo.getTitolo() != null && todo.getTitolo().toLowerCase().contains(term)) ||
                        (todo.getDescrizione() != null && todo.getDescrizione().toLowerCase().contains(term)))
                .toList();
    }

    /**
     * Restituisce i Task in scadenza fino a una certa data.
     * @param finoA Data limite
     * @return Lista di Task in scadenza
     */
    public List<ToDo> toDoInScadenza(LocalDate finoA) {
        if (finoA == null)
            finoA = LocalDate.now().plusDays(7);
        final LocalDate finalFinoA = finoA;
        return getAllToDos().stream()
                .filter(todo -> todo.getScadenza() != null &&
                        !todo.getScadenza().isBefore(LocalDate.now()) &&
                        !todo.getScadenza().isAfter(finalFinoA))
                .filter(todo -> todo.getStato() == StatoToDo.NON_COMPLETATO)
                .sorted((t1, t2) -> t1.getScadenza().compareTo(t2.getScadenza()))
                .toList();
    }

    /**
     * Restituisce tutti i Task dell'utente (da tutte le bacheche).
     * @return Lista di tutti i Task
     */
    public List<ToDo> getAllToDos() {
        return bacheche.stream()
                .flatMap(bacheca -> bacheca.getTodos().stream())
                .distinct()
                .toList();
    }

    /**
     * Modifica l'ordine di un Task in una bacheca.
     * @param titoloBacheca Titolo della bacheca
     * @param todo Task da spostare
     * @param nuovaPosizione Nuova posizione
     * @return true se modificato, false altrimenti
     */
    public boolean modificaOrdineToDoInBacheca(TitoloBacheca titoloBacheca, ToDo todo, int nuovaPosizione) {
        Optional<Bacheca> bachecaOpt = getBachecaByTitolo(titoloBacheca);
        if (bachecaOpt.isPresent()) {
            return bachecaOpt.get().modificaOrdineToDo(todo, nuovaPosizione);
        }
        return false;
    }

    /**
     * Confronta due utenti per uguaglianza logica (ID o username).
     * @param o Oggetto da confrontare
     * @return true se uguali, false altrimenti
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Utente utente = (Utente) o;
        if (id != 0 && utente.id != 0) {
            return id == utente.id;
        }
        return username.equals(utente.username);
    }

    /**
     * Calcola l'hashCode dell'utente (ID se presente, altrimenti username).
     * @return hashCode
     */
    @Override
    public int hashCode() {
        if (id != 0) {
            return Objects.hash(id);
        }
        return Objects.hash(username);
    }

    /**
     * Restituisce una rappresentazione testuale dell'utente.
     * @return Stringa descrittiva
     */
    @Override
    public String toString() {
        return username + (id != 0 ? " (ID:" + id + ")" : "");
    }
}