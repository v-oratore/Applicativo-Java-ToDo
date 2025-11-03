// File: src/main/java/controller/Controller.java
package controller;

import model.Bacheca;
import model.StatoToDo;
import model.ToDo;
import model.TitoloBacheca;
import model.Utente;
import dao.UtenteDAO;
import dao.BachecaDAO;
import dao.ToDoDAO;
import dao.implementazione_postgres_dao.UtentePostgresDAOImpl;
import dao.implementazione_postgres_dao.BachecaPostgresDAOImpl;
import dao.implementazione_postgres_dao.ToDoPostgresDAOImpl;

import java.awt.Image;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller principale dell'applicazione Task.
 * Gestisce la logica di business tra GUI e DAO, inclusa la gestione di utenti, bacheche e Task.
 */
public class Controller {
    /** Logger per la classe Controller. */
    private static final Logger logger = LoggerFactory.getLogger(Controller.class);
    private Utente utenteCorrente;
    private final UtenteDAO utenteDAO;
    private final BachecaDAO bachecaDAO;
    private final ToDoDAO toDoDAO;

    /**
     * Costruttore del Controller. Inizializza i DAO e crea l'utente admin di default se non esiste.
     */
    public Controller() {
        this.utenteDAO = new UtentePostgresDAOImpl();
        this.bachecaDAO = new BachecaPostgresDAOImpl();
        this.toDoDAO = new ToDoPostgresDAOImpl();
        this.utenteCorrente = null;

        Optional<Utente> adminOpt = utenteDAO.findByUsername("admin");
        if (adminOpt.isEmpty()) {
            Utente admin = new Utente("admin", "admin123");
            if (utenteDAO.save(admin)) {
                logger.info("Controller: Utente admin di default creato e salvato nel DB con ID: {}", admin.getId());
            } else {
                logger.error("Controller: Fallimento creazione utente admin nel DB.");
            }
        } else {
            logger.info("Controller: Utente admin già presente nel DB con ID: {}", adminOpt.get().getId());
        }
    }

    /**
     * Registra un nuovo utente nel sistema.
     * @param username Nome utente da registrare
     * @param password Password dell'utente
     * @return true se la registrazione ha successo, false altrimenti
     */
    public boolean registraUtente(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            logger.error("Controller: Username e password non possono essere vuoti per la registrazione.");
            return false;
        }
        Utente nuovoUtente = new Utente(username, password);
        boolean success = utenteDAO.save(nuovoUtente);
        if (success) {
            logger.info("Controller: Utente '{}' registrato con successo nel DB con ID: {}", username, nuovoUtente.getId());

            // Crea automaticamente la board "Università" per il nuovo utente
            Bacheca nuovaBacheca = new Bacheca(TitoloBacheca.UNIVERSITA, "Bacheca per le attività universitarie");
            boolean boardCreated = bachecaDAO.save(nuovaBacheca, nuovoUtente.getId());
            if (boardCreated) {
                logger.info("Controller: Board 'Università' creata automaticamente per l'utente '{}'", username);
            } else {
                logger.warn("Controller: Fallimento creazione board 'Università' per l'utente '{}'", username);
            }
        } else {
            logger.info("Controller: Fallimento registrazione utente '{}' nel DB (potrebbe già esistere).", username);
        }
        return success;
    }

    /**
     * Effettua il login di un utente.
     * @param username Nome utente
     * @param password Password
     * @return true se il login ha successo, false altrimenti
     */
    public boolean login(String username, String password) {
        Optional<Utente> utenteOpt = utenteDAO.findByUsername(username);
        if (utenteOpt.isPresent()) {
            Utente utente = utenteOpt.get();
            if (utente.checkPassword(password)) {
                this.utenteCorrente = utente;
                loadFullUtenteCorrente();
                logger.info("Controller: Utente '{}' loggato con successo. ID: {}", username, utente.getId());
                return true;
            } else {
                logger.info("Controller: Login fallito - Password errata per l'utente '{}'.", username);
            }
        } else {
            logger.info("Controller: Login fallito - Utente '{}' non trovato.", username);
        }
        this.utenteCorrente = null;
        return false;
    }

    /**
     * Carica tutte le informazioni complete dell'utente corrente (bacheche e Task).
     */
    public void loadFullUtenteCorrente() { // <--- DEVE ESSERE PUBLIC
        if (utenteCorrente == null || utenteCorrente.getId() == 0) return;

        List<Bacheca> bachecheDB = bachecaDAO.findByUtenteId(utenteCorrente.getId());
        bachecheDB.forEach(this::caricaToDoPerBacheca);
        gestisciToDoCondivisi(bachecheDB);
        utenteCorrente.setBacheche(bachecheDB);
    }

    private void caricaToDoPerBacheca(Bacheca bacheca) {
        List<ToDo> todosInBacheca = toDoDAO.findAllByBachecaId(bacheca.getId());
        for (ToDo t : todosInBacheca) {
            utenteDAO.findById(t.getAutoreId()).ifPresent(t::setAutore);
            t.setUtentiConAccessoCondiviso(new HashSet<>(toDoDAO.getUtentiCondivisione(t)));
        }
        bacheca.setTodos(todosInBacheca);
    }

    private void gestisciToDoCondivisi(List<Bacheca> bachecheDB) {
        List<ToDo> sharedWithUser = toDoDAO.findAllSharedWithUserAndDestination(utenteCorrente);
        for (ToDo sharedToDo : sharedWithUser) {
            gestisciSingoloToDoCondiviso(sharedToDo, bachecheDB);
        }
    }

    private void gestisciSingoloToDoCondiviso(ToDo sharedToDo, List<Bacheca> bachecheDB) {
        if (sharedToDo.getAutoreId() == utenteCorrente.getId()) {
            return;
        }

        Optional<Bacheca> targetBachecaOpt = trovaBachecaDestinazione(sharedToDo, bachecheDB);
        if (targetBachecaOpt.isPresent()) {
            aggiungiToDoCondivisoAllaBacheca(sharedToDo, targetBachecaOpt.get());
        }
    }

    /**
     * Trova la bacheca di destinazione per un Task condiviso.
     * @param sharedToDo Task condiviso
     * @param bachecheDB Lista delle bacheche dell'utente corrente
     * @return Optional contenente la bacheca di destinazione se trovata
     */
    private Optional<Bacheca> trovaBachecaDestinazione(ToDo sharedToDo, List<Bacheca> bachecheDB) {
        if (sharedToDo.getBachecaDestinazioneId() != null) {
            return trovaBachecaPerId(sharedToDo.getBachecaDestinazioneId(), bachecheDB);
        } else {
            return trovaBachecaPerTitolo(sharedToDo, bachecheDB);
        }
    }

    /**
     * Trova una bacheca per ID nella lista delle bacheche.
     * @param bachecaId ID della bacheca da cercare
     * @param bachecheDB Lista delle bacheche
     * @return Optional contenente la bacheca se trovata
     */
    private Optional<Bacheca> trovaBachecaPerId(Integer bachecaId, List<Bacheca> bachecheDB) {
        return bachecheDB.stream()
                .filter(b -> b.getId() == bachecaId)
                .findFirst();
    }

    /**
     * Trova una bacheca per titolo basandosi sulla bacheca originale del Task.
     * @param sharedToDo Task condiviso
     * @param bachecheDB Lista delle bacheche dell'utente corrente
     * @return Optional contenente la bacheca se trovata
     */
    private Optional<Bacheca> trovaBachecaPerTitolo(ToDo sharedToDo, List<Bacheca> bachecheDB) {
        if (sharedToDo.getBachecaId() == null) {
            return Optional.empty();
        }

        Optional<Bacheca> originalBachecaOpt = bachecaDAO.findById(sharedToDo.getBachecaId());
        if (originalBachecaOpt.isEmpty()) {
            return Optional.empty();
        }

        TitoloBacheca titoloOriginale = originalBachecaOpt.get().getTitoloEnum();
        return bachecheDB.stream()
                .filter(b -> b.getTitoloEnum() == titoloOriginale)
                .findFirst();
    }

    /**
     * Aggiunge un Task condiviso alla bacheca di destinazione se non è già presente.
     * @param sharedToDo Task condiviso da aggiungere
     * @param targetBacheca Bacheca di destinazione
     */
    private void aggiungiToDoCondivisoAllaBacheca(ToDo sharedToDo, Bacheca targetBacheca) {
        if (targetBacheca.getTodos().contains(sharedToDo)) {
            return;
        }

        utenteDAO.findById(sharedToDo.getAutoreId()).ifPresent(sharedToDo::setAutore);
        sharedToDo.setUtentiConAccessoCondiviso(new HashSet<>(toDoDAO.getUtentiCondivisione(sharedToDo)));
        targetBacheca.aggiungiToDo(sharedToDo);
        logger.info("Controller: ToDo condiviso '{}' aggiunto alla bacheca locale '{}' per '{}'.",
                sharedToDo.getTitolo(), targetBacheca.getTitoloDisplayName(), utenteCorrente.getUsername());
    }


    /**
     * Effettua il logout dell'utente corrente.
     */
    public void logout() {
        if (this.utenteCorrente != null) {
            logger.info("Controller: Utente '{}' sloggato.", this.utenteCorrente.getUsername());
        }
        this.utenteCorrente = null;
    }

    /**
     * Restituisce l'utente attualmente loggato.
     * @return Utente corrente, oppure null se nessun utente è loggato
     */
    public Utente getUtenteCorrente() {
        return utenteCorrente;
    }

    /**
     * Verifica se un utente è attualmente loggato.
     * @return true se un utente è loggato, false altrimenti
     */
    public boolean isUserLoggedIn() {
        return utenteCorrente != null;
    }

    /**
     * Restituisce un utente dato il suo username.
     * @param username Username da cercare
     * @return Optional contenente l'utente se trovato, altrimenti Optional vuoto
     */
    public Optional<Utente> getUtenteByUsername(String username) {
        return utenteDAO.findByUsername(username);
    }

    /**
     * Aggiunge una nuova bacheca per l'utente corrente.
     * @param titolo Titolo della bacheca
     * @param descrizione Descrizione della bacheca
     * @return true se l'aggiunta ha successo, false altrimenti
     */
    public boolean aggiungiBacheca(TitoloBacheca titolo, String descrizione) {
        if (!isUserLoggedIn()) {
            logger.error("Controller: Nessun utente loggato. Impossibile aggiungere bacheca.");
            return false;
        }

        if (utenteCorrente.getBacheche().size() >= getMaxBoards()) {
            logger.info("{}: Limite massimo di {} bacheche raggiunto.", utenteCorrente.getUsername(), getMaxBoards());
            return false;
        }

        Bacheca nuovaBacheca = new Bacheca(titolo, descrizione);
        boolean successDB = bachecaDAO.save(nuovaBacheca, utenteCorrente.getId());

        if (successDB) {
            // Usa il nuovo metodo aggiungiBachecaCaricata per aggiungere la bacheca con ID all'utente corrente
            nuovaBacheca.setUtenteId(utenteCorrente.getId()); // Assicura che utenteId sia impostato nell'oggetto locale
            utenteCorrente.aggiungiBachecaCaricata(nuovaBacheca); // <--- MODIFICA QUI
            logger.info("Controller: Bacheca '{}' aggiunta con successo. ID: {}", titolo.getDisplayName(), nuovaBacheca.getId());
            return true;
        } else {
            logger.error("Controller: Fallimento aggiunta bacheca '{}' nel DB (potrebbe già esistere).", titolo.getDisplayName());
            return false;
        }
    }


    /**
     * Modifica la descrizione di una bacheca esistente.
     * @param titolo Titolo della bacheca
     * @param nuovaDescrizione Nuova descrizione da impostare
     * @return true se la modifica ha successo, false altrimenti
     */
    public boolean modificaDescrizioneBacheca(TitoloBacheca titolo, String nuovaDescrizione) {
        if (!isUserLoggedIn()) return false;
        Optional<Bacheca> bachecaOpt = utenteCorrente.getBachecaByTitolo(titolo);
        if (bachecaOpt.isPresent()) {
            Bacheca bachecaDaModificare = bachecaOpt.get();
            if (!nuovaDescrizione.equals(bachecaDaModificare.getDescrizione())) {
                bachecaDaModificare.setDescrizione(nuovaDescrizione);
                boolean successDB = bachecaDAO.update(bachecaDaModificare);
                if (successDB) {
                    logger.info("Controller: Descrizione bacheca '{}' modificata.", titolo.getDisplayName());
                } else {
                    logger.error("Controller: Fallimento modifica descrizione bacheca '{}' nel DB.", titolo.getDisplayName());
                }
                return successDB;
            } else {
                logger.info("Controller: Descrizione bacheca '{}' invariata.", titolo.getDisplayName());
                return true;
            }
        }
        return false;
    }

    /**
     * Elimina una bacheca dell'utente corrente.
     * @param titolo Titolo della bacheca da eliminare
     * @return true se l'eliminazione ha successo, false altrimenti
     */
    public boolean eliminaBacheca(TitoloBacheca titolo) {
        if (!isUserLoggedIn()) return false;
        Optional<Bacheca> bachecaOpt = utenteCorrente.getBachecaByTitolo(titolo);
        if (bachecaOpt.isPresent()) {
            Bacheca bachecaDaEliminare = bachecaOpt.get();
            boolean successDB = bachecaDAO.delete(bachecaDaEliminare.getId());
            if (successDB) {
                utenteCorrente.eliminaBacheca(titolo);
                logger.info("Controller: Bacheca '{}' eliminata.", titolo.getDisplayName());
            } else {
                logger.error("Controller: Fallimento eliminazione bacheca '{}' dal DB.", titolo.getDisplayName());
            }
            return successDB;
        }
        return false;
    }

    /**
     * Restituisce la lista delle bacheche dell'utente corrente.
     * @return Lista di bacheche, oppure lista vuota se nessun utente è loggato
     */
    public List<Bacheca> getBachecheUtenteCorrente() {
        if (!isUserLoggedIn()) {
            return Collections.emptyList();
        }
        return utenteCorrente.getBacheche();
    }

    /**
     * Restituisce una bacheca dell'utente corrente dato il display name.
     * @param displayName Nome visualizzato della bacheca
     * @return Optional contenente la bacheca se trovata, altrimenti Optional vuoto
     */
    public Optional<Bacheca> getBachecaByDisplayNameDaUtenteCorrente(String displayName) {
        if (!isUserLoggedIn()) return Optional.empty();
        try {
            TitoloBacheca titoloEnum = TitoloBacheca.fromDisplayName(displayName);
            return utenteCorrente.getBachecaByTitolo(titoloEnum);
        } catch (IllegalArgumentException _) {
            logger.error("Controller: Nome bacheca non valido per ricerca: {}", displayName);
            return Optional.empty();
        }
    }


    /**
     * Restituisce il numero massimo di bacheche consentite per utente.
     * @return Numero massimo di bacheche
     */
    public int getMaxBoards() { return 3; }

    /**
     * Restituisce i titoli disponibili per nuove bacheche dell'utente corrente.
     * @return Lista di nomi visualizzati disponibili
     */
    public List<String> getAvailableBoardTitlesForCurrentUser() {
        if (!isUserLoggedIn()) return Collections.emptyList();
        List<String> currentBoardDisplayNames = utenteCorrente.getBacheche().stream()
                .map(Bacheca::getTitoloDisplayName)
                .toList();
        if (currentBoardDisplayNames.size() >= getMaxBoards()) {
            return Collections.emptyList();
        }
        return java.util.Arrays.stream(TitoloBacheca.values())
                .map(TitoloBacheca::getDisplayName)
                .filter(displayName -> !currentBoardDisplayNames.contains(displayName))
                .toList();
    }

    /**
     * Crea un nuovo Task nella bacheca specificata.
     * @param nomeBachecaDestinazione Nome visualizzato della bacheca di destinazione
     * @param titolo Titolo del Task
     * @param descrizione Descrizione del Task
     * @param scadenza Data di scadenza
     * @param colore Colore associato
     * @param url URL associato (opzionale)
     * @param immagine Immagine associata (opzionale)
     * @return Optional contenente il Task creato, oppure Optional vuoto se fallisce
     */
    public Optional<ToDo> creaToDo(String nomeBachecaDestinazione, String titolo, String descrizione,
                                   LocalDate scadenza, String colore, String url, Image immagine) {
        if (!isUserLoggedIn()) return Optional.empty();

        try {
            TitoloBacheca titoloBachecaEnum = TitoloBacheca.fromDisplayName(nomeBachecaDestinazione);
            Optional<Bacheca> bachecaOpt = utenteCorrente.getBachecaByTitolo(titoloBachecaEnum);

            if (bachecaOpt.isPresent()) {
                Bacheca bachecaDest = bachecaOpt.get();
                if (bachecaDest.getId() == 0) {
                    logger.error("Controller: La bacheca destinazione '{}' non è stata salvata nel DB (ID=0).", nomeBachecaDestinazione);
                    return Optional.empty();
                }

                ToDo nuovoToDo = new ToDo(titolo, descrizione, utenteCorrente, scadenza, colore);
                nuovoToDo.setAutoreId(utenteCorrente.getId());
                nuovoToDo.setBachecaId(bachecaDest.getId());
                if (url != null) nuovoToDo.setUrl(url);
                if (immagine != null) nuovoToDo.setImmagine(immagine);

                int nuovaPosizione = bachecaDest.getTodos().size();
                nuovoToDo.setPosizione(nuovaPosizione);


                if (toDoDAO.save(nuovoToDo)) {
                    bachecaDest.aggiungiToDo(nuovoToDo);
                    logger.info("Controller: ToDo '{}' creato e salvato nel DB. ID: {}", titolo, nuovoToDo.getId());
                    return Optional.of(nuovoToDo);
                } else {
                    logger.error("Controller: Fallimento salvataggio ToDo '{}' nel DB.", titolo);
                }
            } else {
                logger.info("{}: Bacheca '{}' non trovata per creare ToDo.", utenteCorrente.getUsername(), nomeBachecaDestinazione);
            }
        } catch (IllegalArgumentException ignored) {
            logger.error("Controller: Nome bacheca non valido '{}' per creazione ToDo. {}", nomeBachecaDestinazione, ignored.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Parametri di aggiornamento per la modifica di un Task.
     */
    public static class ToDoUpdateParams {
        private String nuovoTitolo;
        private String nuovaDescrizione;
        private LocalDate nuovaScadenza;
        private StatoToDo nuovoStato;
        private String nuovoColore;
        private String nuovoUrl;
        private Image nuovaImmagine;

        /**
         * Costruttore per i parametri di aggiornamento di un ToDo.
         * @param nuovoTitolo Il nuovo titolo
         * @param nuovaDescrizione La nuova descrizione
         * @param nuovaScadenza La nuova data di scadenza
         * @param nuovoStato Il nuovo stato
         * @param nuovoColore Il nuovo colore
         * @param nuovoUrl Il nuovo URL
         * @param nuovaImmagine La nuova immagine
         */
        public ToDoUpdateParams(String nuovoTitolo, String nuovaDescrizione, LocalDate nuovaScadenza, StatoToDo nuovoStato, String nuovoColore, String nuovoUrl, Image nuovaImmagine) {
            this.nuovoTitolo = nuovoTitolo;
            this.nuovaDescrizione = nuovaDescrizione;
            this.nuovaScadenza = nuovaScadenza;
            this.nuovoStato = nuovoStato;
            this.nuovoColore = nuovoColore;
            this.nuovoUrl = nuovoUrl;
            this.nuovaImmagine = nuovaImmagine;
        }

        /**
         * Restituisce il nuovo titolo.
         * @return Il nuovo titolo
         */
        public String getNuovoTitolo() { return nuovoTitolo; }
        /**
         * Imposta il nuovo titolo.
         * @param nuovoTitolo Il nuovo titolo
         */
        public void setNuovoTitolo(String nuovoTitolo) { this.nuovoTitolo = nuovoTitolo; }

        /**
         * Restituisce la nuova descrizione.
         * @return La nuova descrizione
         */
        public String getNuovaDescrizione() { return nuovaDescrizione; }
        /**
         * Imposta la nuova descrizione.
         * @param nuovaDescrizione La nuova descrizione
         */
        public void setNuovaDescrizione(String nuovaDescrizione) { this.nuovaDescrizione = nuovaDescrizione; }

        /**
         * Restituisce la nuova data di scadenza.
         * @return La nuova data di scadenza
         */
        public LocalDate getNuovaScadenza() { return nuovaScadenza; }
        /**
         * Imposta la nuova data di scadenza.
         * @param nuovaScadenza La nuova data di scadenza
         */
        public void setNuovaScadenza(LocalDate nuovaScadenza) { this.nuovaScadenza = nuovaScadenza; }

        /**
         * Restituisce il nuovo stato.
         * @return Il nuovo stato
         */
        public StatoToDo getNuovoStato() { return nuovoStato; }
        /**
         * Imposta il nuovo stato.
         * @param nuovoStato Il nuovo stato
         */
        public void setNuovoStato(StatoToDo nuovoStato) { this.nuovoStato = nuovoStato; }

        /**
         * Restituisce il nuovo colore.
         * @return Il nuovo colore
         */
        public String getNuovoColore() { return nuovoColore; }
        /**
         * Imposta il nuovo colore.
         * @param nuovoColore Il nuovo colore
         */
        public void setNuovoColore(String nuovoColore) { this.nuovoColore = nuovoColore; }

        /**
         * Restituisce il nuovo URL.
         * @return Il nuovo URL
         */
        public String getNuovoUrl() { return nuovoUrl; }
        /**
         * Imposta il nuovo URL.
         * @param nuovoUrl Il nuovo URL
         */
        public void setNuovoUrl(String nuovoUrl) { this.nuovoUrl = nuovoUrl; }

        /**
         * Restituisce la nuova immagine.
         * @return La nuova immagine
         */
        public Image getNuovaImmagine() { return nuovaImmagine; }
        /**
         * Imposta la nuova immagine.
         * @param nuovaImmagine La nuova immagine
         */
        public void setNuovaImmagine(Image nuovaImmagine) { this.nuovaImmagine = nuovaImmagine; }
    }

    /**
     * Modifica un Task esistente con i parametri specificati.
     * @param todoDaModificare Task da modificare
     * @param params Parametri di aggiornamento
     * @return true se la modifica ha successo, false altrimenti
     */
    public boolean modificaToDo(ToDo todoDaModificare, ToDoUpdateParams params) {
        if (!isUserLoggedIn() || todoDaModificare == null || todoDaModificare.getId() == 0) {
            logger.error("Controller: Impossibile modificare ToDo. Utente non loggato o ToDo non valido/non persistito.");
            return false;
        }
        Optional<ToDo> currentToDoOpt = toDoDAO.findById(todoDaModificare.getId());
        if (currentToDoOpt.isEmpty()) {
            logger.error("Controller: ToDo con ID {} non trovato nel DB per la modifica.", todoDaModificare.getId());
            return false;
        }
        ToDo originalToDoInDB = currentToDoOpt.get();
        boolean isAutore = originalToDoInDB.getAutoreId() == utenteCorrente.getId();
        boolean changed = false;
        changed |= aggiornaTitolo(originalToDoInDB, params.getNuovoTitolo(), isAutore);
        changed |= aggiornaDescrizione(originalToDoInDB, params.getNuovaDescrizione(), isAutore);
        changed |= aggiornaScadenza(originalToDoInDB, params.getNuovaScadenza(), isAutore);
        changed |= aggiornaColore(originalToDoInDB, params.getNuovoColore(), isAutore);
        changed |= aggiornaUrl(originalToDoInDB, params.getNuovoUrl(), isAutore);
        changed |= aggiornaImmagine(originalToDoInDB, params.getNuovaImmagine(), isAutore);
        changed |= aggiornaStato(originalToDoInDB, params.getNuovoStato());
        if (!changed) {
            logger.info("Controller: Nessuna modifica effettiva al ToDo '{}'.", originalToDoInDB.getTitolo());
            return true;
        }
        boolean successDB = toDoDAO.update(originalToDoInDB);
        if (successDB) {
            todoDaModificare.setTitolo(originalToDoInDB.getTitolo());
            todoDaModificare.setDescrizione(originalToDoInDB.getDescrizione());
            todoDaModificare.setScadenza(originalToDoInDB.getScadenza());
            todoDaModificare.setStato(originalToDoInDB.getStato());
            todoDaModificare.setColore(originalToDoInDB.getColore());
            todoDaModificare.setUrl(originalToDoInDB.getUrl());
            todoDaModificare.setImmagine(originalToDoInDB.getImmagine());
            logger.info("Controller: ToDo '{}' modificato.", originalToDoInDB.getTitolo());
        } else {
            logger.error("Controller: Fallimento modifica ToDo '{}'", originalToDoInDB.getTitolo());
        }
        return successDB;
    }

    private boolean aggiornaTitolo(ToDo todo, String nuovoTitolo, boolean isAutore) {
        if (nuovoTitolo != null && !nuovoTitolo.equals(todo.getTitolo())) {
            if (!isAutore) { logger.error("Controller: Permesso negato - solo l'autore può cambiare il titolo."); return false; }
            todo.setTitolo(nuovoTitolo); return true;
        }
        return false;
    }
    private boolean aggiornaDescrizione(ToDo todo, String nuovaDescrizione, boolean isAutore) {
        if (nuovaDescrizione != null && !nuovaDescrizione.equals(todo.getDescrizione())) {
            if (!isAutore) { logger.error("Controller: Permesso negato - solo l'autore può cambiare la descrizione."); return false; }
            todo.setDescrizione(nuovaDescrizione); return true;
        }
        return false;
    }
    private boolean aggiornaScadenza(ToDo todo, LocalDate nuovaScadenza, boolean isAutore) {
        if (nuovaScadenza != null && !nuovaScadenza.equals(todo.getScadenza())) {
            if (!isAutore) { logger.error("Controller: Permesso negato - solo l'autore può cambiare la scadenza."); return false; }
            todo.setScadenza(nuovaScadenza); return true;
        }
        return false;
    }
    private boolean aggiornaColore(ToDo todo, String nuovoColore, boolean isAutore) {
        if (nuovoColore != null && !nuovoColore.equals(todo.getColore())) {
            if (!isAutore) { logger.error("Controller: Permesso negato - solo l'autore può cambiare il colore."); return false; }
            todo.setColore(nuovoColore); return true;
        }
        return false;
    }
    private boolean aggiornaUrl(ToDo todo, String nuovoUrl, boolean isAutore) {
        if (nuovoUrl != null && !nuovoUrl.equals(todo.getUrl())) {
            if (!isAutore) { logger.error("Controller: Permesso negato - solo l'autore può cambiare l'URL."); return false; }
            todo.setUrl(nuovoUrl); return true;
        }
        return false;
    }
    private boolean aggiornaImmagine(ToDo todo, Image nuovaImmagine, boolean isAutore) {
        if (nuovaImmagine != null && !Objects.equals(nuovaImmagine, todo.getImmagine())) {
            if (!isAutore) { logger.error("Controller: Permesso negato - solo l'autore può cambiare l'immagine."); return false; }
            todo.setImmagine(nuovaImmagine); return true;
        } else if (nuovaImmagine == null && todo.getImmagine() != null) {
            if (!isAutore) { logger.error("Controller: Permesso negato - solo l'autore può rimuovere l'immagine."); return false; }
            todo.setImmagine(null); return true;
        }
        return false;
    }
    private boolean aggiornaStato(ToDo todo, StatoToDo nuovoStato) {
        if (nuovoStato != null && nuovoStato != todo.getStato()) {
            todo.setStato(nuovoStato); return true;
        }
        return false;
    }


    /**
     * Elimina un Task (come autore o revoca la condivisione se non autore).
     * @param todoDaEliminare Task da eliminare
     * @return true se l'eliminazione ha successo, false altrimenti
     */
    public boolean eliminaToDo(ToDo todoDaEliminare) {
        if (!isUserLoggedIn() || todoDaEliminare == null || todoDaEliminare.getId() == 0) {
            logger.error("Controller: Impossibile eliminare ToDo. Utente non loggato o ToDo non valido/non persistito.");
            return false;
        }
        boolean isAutore = todoDaEliminare.getAutoreId() == utenteCorrente.getId();
        if (isAutore) {
            return eliminaToDoComeAutore(todoDaEliminare);
        } else {
            return revocaCondivisioneToDo(todoDaEliminare);
        }
    }

    private boolean eliminaToDoComeAutore(ToDo todoDaEliminare) {
        boolean successDB = toDoDAO.delete(todoDaEliminare.getId());
        if (successDB) {
            for (Bacheca b : utenteCorrente.getBacheche()) {
                b.eliminaToDo(todoDaEliminare);
            }
            logger.info("Controller: ToDo '{}' (ID: {}) eliminato dal DB dall'autore '{}'.", todoDaEliminare.getTitolo(), todoDaEliminare.getId(), utenteCorrente.getUsername());
            return true;
        } else {
            logger.error("Controller: Fallimento eliminazione ToDo '{}' dal DB.", todoDaEliminare.getTitolo());
            return false;
        }
    }

    private boolean revocaCondivisioneToDo(ToDo todoDaEliminare) {
        boolean successRevoca = toDoDAO.removeCondivisione(todoDaEliminare, utenteCorrente);
        if (successRevoca) {
            for (Bacheca b : utenteCorrente.getBacheche()) {
                b.eliminaToDo(todoDaEliminare);
            }
            todoDaEliminare.rimuoviUtenteCondiviso(utenteCorrente);
            logger.info("Controller: Condivisione del ToDo '{}' revocata per l'utente '{}'.", todoDaEliminare.getTitolo(), utenteCorrente.getUsername());
            return true;
        } else {
            logger.error("Controller: Fallimento revoca condivisione per ToDo '{}' e utente '{}'.", todoDaEliminare.getTitolo(), utenteCorrente.getUsername());
            return false;
        }
    }


    /**
     * Restituisce la lista dei Task per una bacheca specifica.
     * @param nomeBachecaDisplayName Nome visualizzato della bacheca
     * @return Lista di Task della bacheca, oppure lista vuota se non trovata
     */
    public List<ToDo> getToDosPerBacheca(String nomeBachecaDisplayName) {
        if (!isUserLoggedIn()) return Collections.emptyList();
        try {
            TitoloBacheca titoloEnum = TitoloBacheca.fromDisplayName(nomeBachecaDisplayName);
            Optional<Bacheca> bachecaOpt = utenteCorrente.getBachecaByTitolo(titoloEnum);
            if (bachecaOpt.isPresent()) {
                Bacheca bacheca = bachecaOpt.get();
                List<ToDo> todosFromDb = toDoDAO.findAllByBachecaId(bacheca.getId());
                List<ToDo> sharedToDosForThisBoard = toDoDAO.findAllSharedWithUser(utenteCorrente).stream()
                        .filter(sharedToDo -> {
                            Optional<Bacheca> originalBachecaOpt = (sharedToDo.getBachecaId() != null) ? bachecaDAO.findById(sharedToDo.getBachecaId()) : Optional.empty();
                            return originalBachecaOpt.map(originalBacheca -> originalBacheca.getTitoloEnum() == titoloEnum).orElse(false);
                        })
                        .toList();

                Set<ToDo> combinedToDos = new HashSet<>(todosFromDb);
                for(ToDo sharedToDo : sharedToDosForThisBoard) {
                    if (sharedToDo.getAutoreId() != utenteCorrente.getId()) {
                        combinedToDos.add(sharedToDo);
                    }
                }

                List<ToDo> finalToDos = new ArrayList<>(combinedToDos);
                finalToDos.sort((t1, t2) -> Integer.compare(t1.getPosizione(), t2.getPosizione()));

                bacheca.setTodos(finalToDos);

                return finalToDos;
            }
        } catch (IllegalArgumentException ignored) {
            logger.error("Controller: Nome bacheca non valido '{}' per ricerca ToDo. {}", nomeBachecaDisplayName, ignored.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * Restituisce tutti i Task dell'utente corrente.
     * @return Lista di tutti i Task
     */
    public List<ToDo> getAllToDosUtenteCorrente() {
        if (!isUserLoggedIn()) return Collections.emptyList();
        return utenteCorrente.getAllToDos();
    }

    /**
     * Sposta un Task da una bacheca a un'altra.
     * @param todoDaSpostare Task da spostare
     * @param nomeBachecaOrigineDisplay Nome visualizzato della bacheca di origine
     * @param nomeBachecaDestinazioneDisplay Nome visualizzato della bacheca di destinazione
     * @return true se lo spostamento ha successo, false altrimenti
     */
    public boolean spostaToDo(ToDo todoDaSpostare, String nomeBachecaOrigineDisplay, String nomeBachecaDestinazioneDisplay) {
        if (!isUserLoggedIn() || todoDaSpostare == null || todoDaSpostare.getId() == 0) {
            logger.error("Controller: Impossibile spostare ToDo. Utente non loggato o ToDo non valido/non persistito.");
            return false;
        }
        try {
            TitoloBacheca origineEnum = TitoloBacheca.fromDisplayName(nomeBachecaOrigineDisplay);
            TitoloBacheca destinazioneEnum = TitoloBacheca.fromDisplayName(nomeBachecaDestinazioneDisplay);
            if (origineEnum == destinazioneEnum) {
                logger.info("Controller: Origine e destinazione sono la stessa bacheca, nessun spostamento necessario.");
                return true;
            }
            return eseguiSpostamentoToDo(todoDaSpostare, origineEnum, destinazioneEnum);
        } catch (IllegalArgumentException ignored) {
            logger.error("Controller: Nomi bacheca non validi per lo spostamento. {}", ignored.getMessage());
            return false;
        }
    }

    private boolean eseguiSpostamentoToDo(ToDo todoDaSpostare, TitoloBacheca origineEnum, TitoloBacheca destinazioneEnum) {
        Optional<Bacheca> bachecaOrigineOpt = utenteCorrente.getBachecaByTitolo(origineEnum);
        Optional<Bacheca> bachecaDestinazioneOpt = utenteCorrente.getBachecaByTitolo(destinazioneEnum);
        if (bachecaOrigineOpt.isPresent() && bachecaDestinazioneOpt.isPresent()) {
            Bacheca bOrigine = bachecaOrigineOpt.get();
            Bacheca bDestinazione = bachecaDestinazioneOpt.get();
            if (bOrigine.getId() == 0 || bDestinazione.getId() == 0) {
                logger.error("Controller: Bacheche di origine o destinazione non persistite (ID=0).");
                return false;
            }
            if (bOrigine.getTodos().contains(todoDaSpostare)) {
                return aggiornaSpostamentoToDo(todoDaSpostare, bOrigine, bDestinazione, origineEnum, destinazioneEnum);
            } else {
                logger.info("Controller: ToDo '{}' non trovato nella bacheca di origine '{}'.", todoDaSpostare.getTitolo(), origineEnum.getDisplayName());
                return false;
            }
        } else {
            logger.info("Controller: Una o entrambe le bacheche (origine/destinazione) non trovate per l'utente corrente.");
            return false;
        }
    }

    private boolean aggiornaSpostamentoToDo(ToDo todoDaSpostare, Bacheca bOrigine, Bacheca bDestinazione, TitoloBacheca origineEnum, TitoloBacheca destinazioneEnum) {
        todoDaSpostare.setBachecaId(bDestinazione.getId());
        todoDaSpostare.setPosizione(bDestinazione.getTodos().size());
        boolean successDB = toDoDAO.update(todoDaSpostare);
        if (successDB) {
            bOrigine.eliminaToDo(todoDaSpostare);
            bDestinazione.aggiungiToDo(todoDaSpostare);
            aggiornaPosizioniToDoInBacheca(bOrigine);
            logger.info("Controller: ToDo '{}' spostato da '{}' a '{}'.", todoDaSpostare.getTitolo(), origineEnum.getDisplayName(), destinazioneEnum.getDisplayName());
            return true;
        } else {
            logger.error("Controller: Fallimento spostamento ToDo nel DB. Nessuna modifica locale.");
            return false;
        }
    }

    private void aggiornaPosizioniToDoInBacheca(Bacheca bacheca) {
        for (int i = 0; i < bacheca.getTodos().size(); i++) {
            ToDo t = bacheca.getTodos().get(i);
            if (t.getPosizione() != i) {
                t.setPosizione(i);
                if (!toDoDAO.update(t)) {
                    logger.error("Controller: Fallito aggiornamento posizione per ToDo ID {}. Ripristino manuale necessario.", t.getId());
                }
            }
        }
    }


    /**
     * Cambia la bacheca di un Task.
     * @param todo Task da spostare
     * @param nomeNuovaBachecaDisplay Nome visualizzato della nuova bacheca
     * @return true se il cambio ha successo, false altrimenti
     */
    public boolean cambiaBachecaToDo(ToDo todo, String nomeNuovaBachecaDisplay) {
        if (!isUserLoggedIn() || todo == null) {
            logger.error("Controller: Impossibile cambiare bacheca ToDo. Utente non loggato o ToDo non valido.");
            return false;
        }
        try {
            String bachecaCorrenteDisplay = null;
            for(Bacheca b : utenteCorrente.getBacheche()) {
                if (b.getTodos().contains(todo)) {
                    bachecaCorrenteDisplay = b.getTitoloDisplayName();
                    break;
                }
            }

            if (bachecaCorrenteDisplay == null) {
                logger.info("{}: Impossibile trovare la bacheca corrente per il ToDo '{}'.", utenteCorrente.getUsername(), todo.getTitolo());
                return false;
            }
            return spostaToDo(todo, bachecaCorrenteDisplay, nomeNuovaBachecaDisplay);

        } catch (IllegalArgumentException ignored) {
            logger.error("Controller: Nome nuova bacheca non valido per cambio bacheca. {}", ignored.getMessage());
            return false;
        }
    }


    /**
     * Modifica l'ordine di un Task in una bacheca.
     * @param nomeBachecaDisplay Nome visualizzato della bacheca
     * @param todo Task da spostare
     * @param nuovaPosizione Nuova posizione desiderata
     * @return true se la modifica ha successo, false altrimenti
     */
    public boolean modificaOrdineToDoInBacheca(String nomeBachecaDisplay, ToDo todo, int nuovaPosizione) {
        if (!isUserLoggedIn() || todo == null || todo.getId() == 0) {
            logger.error("Controller: Impossibile modificare ordine ToDo. Utente non loggato o ToDo non valido/non persistito.");
            return false;
        }
        try {
            TitoloBacheca bachecaEnum = TitoloBacheca.fromDisplayName(nomeBachecaDisplay);
            Optional<Bacheca> bachecaOpt = utenteCorrente.getBachecaByTitolo(bachecaEnum);
            if (bachecaOpt.isPresent()) {
                Bacheca bacheca = bachecaOpt.get();
                if (!bacheca.getTodos().contains(todo)) {
                    logger.error("Controller: Il ToDo specificato non si trova nella bacheca '{}'.", nomeBachecaDisplay);
                    return false;
                }
                return aggiornaOrdineToDoInBacheca(bacheca, todo, nuovaPosizione);
            }
        } catch (IllegalArgumentException ignored) {
            logger.error("Controller: Nome bacheca non valido per modifica ordine. {}", ignored.getMessage());
        }
        return false;
    }

    private boolean aggiornaOrdineToDoInBacheca(Bacheca bacheca, ToDo todo, int nuovaPosizione) {
        if (bacheca.modificaOrdineToDo(todo, nuovaPosizione)) {
            List<ToDo> todosOrdinati = bacheca.getTodos();
            boolean allUpdated = true;
            for (int i = 0; i < todosOrdinati.size(); i++) {
                ToDo t = todosOrdinati.get(i);
                if (t.getPosizione() != i) {
                    t.setPosizione(i);
                    if (!toDoDAO.update(t)) {
                        allUpdated = false;
                        logger.error("Controller: Fallito aggiornamento posizione per ToDo ID {}. Ripristino manuale necessario.", t.getId());
                    }
                }
            }
            if(allUpdated) {
                logger.info("Controller: Ordine ToDo aggiornato per bacheca {}", bacheca.getTitoloDisplayName());
            }
            return allUpdated;
        }
        return false;
    }

    /**
     * Condivide un Task con un altro utente su una bacheca specifica.
     * @param toDoDaCondividere Task da condividere
     * @param usernameDestinatario Username del destinatario
     * @param nomeBachecaDestinazioneDisplay Nome visualizzato della bacheca di destinazione
     * @return true se la condivisione ha successo, false altrimenti
     */
    public boolean condividiToDo(ToDo toDoDaCondividere, String usernameDestinatario, String nomeBachecaDestinazioneDisplay) {
        if (!isUserLoggedIn() || toDoDaCondividere == null || toDoDaCondividere.getId() == 0) {
            logger.error("Controller: Impossibile condividere ToDo. Utente non loggato o ToDo non valido/non persistito.");
            return false;
        }
        if (toDoDaCondividere.getAutoreId() != utenteCorrente.getId()) {
            logger.error("Controller: Solo l'autore può condividere questo ToDo.");
            return false;
        }

        Optional<Utente> utenteTargetOpt = utenteDAO.findByUsername(usernameDestinatario);
        if (utenteTargetOpt.isEmpty()) {
            logger.error("Controller: Utente destinatario '{}' non trovato.", usernameDestinatario);
            return false;
        }
        Utente utenteTarget = utenteTargetOpt.get();
        if (utenteTarget.getId() == utenteCorrente.getId()) {
            logger.info("Controller: Tentativo di condivisione con se stesso per l'utente '{}'.", utenteTarget.getUsername());
            return false;
        }

        try {
            TitoloBacheca bachecaTargetEnum = TitoloBacheca.fromDisplayName(nomeBachecaDestinazioneDisplay);

            // Verifica se la bacheca di destinazione esiste per l'utente target
            List<Bacheca> bachecheTarget = bachecaDAO.findByUtenteId(utenteTarget.getId());
            boolean bachecaEsiste = bachecheTarget.stream()
                    .anyMatch(b -> b.getTitoloEnum() == bachecaTargetEnum);

            if (!bachecaEsiste) {
                logger.error("Controller: Bacheca '{}' non esiste per l'utente '{}'. Impossibile condividere il ToDo.",
                        bachecaTargetEnum.getDisplayName(), utenteTarget.getUsername());
                return false;
            }

            if (toDoDAO.getUtentiCondivisione(toDoDaCondividere).contains(utenteTarget)) {
                logger.info("Controller: ToDo '{}' è già condiviso con '{}'.", toDoDaCondividere.getTitolo(), utenteTarget.getUsername());
                return true;
            }

            // Trova la bacheca di destinazione per l'utente target
            Optional<Bacheca> bachecaDestinazioneOpt = bachecheTarget.stream()
                    .filter(b -> b.getTitoloEnum() == bachecaTargetEnum)
                    .findFirst();

            if (bachecaDestinazioneOpt.isEmpty()) {
                logger.error("Controller: Bacheca di destinazione non trovata per l'utente '{}'.", utenteTarget.getUsername());
                return false;
            }

            Integer bachecaDestinazioneId = bachecaDestinazioneOpt.get().getId();
            boolean condivisioneSuccess = toDoDAO.addCondivisione(toDoDaCondividere, utenteTarget, bachecaDestinazioneId);
            if (!condivisioneSuccess) {
                logger.error("Controller: Fallimento aggiunta relazione di condivisione nel DB.");
                return false;
            }

            toDoDaCondividere.aggiungiUtenteCondiviso(utenteTarget);

            logger.info("Controller: ToDo '{}' condiviso con successo con '{}' sulla bacheca corrispondente '{}'.", toDoDaCondividere.getTitolo(), utenteTarget.getUsername(), bachecaTargetEnum.getDisplayName());
            return true;

        } catch (IllegalArgumentException ignored) {
            logger.error("Controller: Nome bacheca destinazione non valido per condivisione: '{}' {}", nomeBachecaDestinazioneDisplay, ignored.getMessage());
            return false;
        }
    }


    /**
     * Revoca la condivisione di un Task per un utente specifico.
     * @param toDoCondiviso Task condiviso
     * @param usernameTarget Username dell'utente a cui revocare la condivisione
     * @return true se la revoca ha successo, false altrimenti
     */
    public boolean revocaCondivisione(ToDo toDoCondiviso, String usernameTarget) {
        if (!isUserLoggedIn() || toDoCondiviso == null || toDoCondiviso.getId() == 0) {
            logger.error("Controller: Impossibile revocare condivisione. Utente non loggato o ToDo non valido/non persistito.");
            return false;
        }
        if (toDoCondiviso.getAutoreId() != utenteCorrente.getId()) {
            logger.error("Controller: Solo l'autore può revocare la condivisione di questo ToDo.");
            return false;
        }

        Optional<Utente> utenteTargetOpt = utenteDAO.findByUsername(usernameTarget);
        if (utenteTargetOpt.isEmpty()) {
            logger.error("Controller: Utente target '{}' non trovato per revoca.", usernameTarget);
            return false;
        }
        Utente utenteTarget = utenteTargetOpt.get();

        boolean successRevoca = toDoDAO.removeCondivisione(toDoCondiviso, utenteTarget);
        if (successRevoca) {
            toDoCondiviso.rimuoviUtenteCondiviso(utenteTarget);

            logger.info("Controller: Condivisione del ToDo '{}' revocata per l'utente '{}'.", toDoCondiviso.getTitolo(), utenteTarget.getUsername());
            return true;
        } else {
            logger.error("Controller: Fallimento revoca condivisione dal DB. Il ToDo potrebbe non essere stato condiviso con questo utente.");
            return false;
        }
    }


    /**
     * Ricerca Task dell'utente corrente in base a una stringa di ricerca.
     * @param searchTerm Termine di ricerca
     * @return Lista di Task che corrispondono alla ricerca
     */
    public List<ToDo> ricercaToDo(String searchTerm) {
        if (!isUserLoggedIn()) return Collections.emptyList();
        return utenteCorrente.ricercaToDo(searchTerm);
    }

    /**
     * Restituisce i Task in scadenza fino a una certa data.
     * @param finoA Data limite
     * @return Lista di Task in scadenza
     */
    public List<ToDo> toDoInScadenza(LocalDate finoA) {
        if (!isUserLoggedIn()) return Collections.emptyList();
        return utenteCorrente.toDoInScadenza(finoA);
    }

    /**
     * Ottiene le bacheche di un utente specifico tramite username.
     * @param username Username dell'utente
     * @return Lista delle bacheche dell'utente, o lista vuota se l'utente non esiste
     */
    public List<String> getBachecheUtenteByUsername(String username) {
        Optional<Utente> utenteOpt = utenteDAO.findByUsername(username);
        if (utenteOpt.isEmpty()) {
            return Collections.emptyList();
        }

        List<Bacheca> bacheche = bachecaDAO.findByUtenteId(utenteOpt.get().getId());
        return bacheche.stream()
                .map(Bacheca::getTitoloDisplayName)
                .toList();
    }

    /**
     * Elimina completamente l'utente corrente e tutti i suoi dati correlati dal database.
     * @return true se l'eliminazione ha successo, false altrimenti
     */
    public boolean eliminaUtenteCorrente() {
        if (!isUserLoggedIn()) {
            logger.error("Controller: Impossibile eliminare utente - nessun utente loggato.");
            return false;
        }

        try {
            // 1. Elimina tutti i Task dell'utente (sia come autore che come condivisi)
            List<ToDo> todosUtente = getAllToDosUtenteCorrente();
            for (ToDo todo : todosUtente) {
                toDoDAO.delete(todo.getId());
            }
            logger.info("Controller: Eliminati {} ToDo dell'utente '{}'", todosUtente.size(), utenteCorrente.getUsername());

            // 2. Elimina tutte le bacheche dell'utente
            List<Bacheca> bachecheUtente = getBachecheUtenteCorrente();
            for (Bacheca bacheca : bachecheUtente) {
                bachecaDAO.delete(bacheca.getId());
            }
            logger.info("Controller: Eliminate {} bacheche dell'utente '{}'", bachecheUtente.size(), utenteCorrente.getUsername());

            // 3. Elimina l'utente stesso
            boolean success = utenteDAO.delete(utenteCorrente.getId());
            if (success) {
                logger.info("Controller: Utente '{}' eliminato con successo dal database", utenteCorrente.getUsername());
                utenteCorrente = null; // Reset dell'utente corrente
                return true;
            } else {
                logger.error("Controller: Fallimento eliminazione utente '{}' dal database", utenteCorrente.getUsername());
                return false;
            }
        } catch (Exception e) {
            logger.error("Controller: Errore durante l'eliminazione dell'utente '{}': {}", utenteCorrente.getUsername(), e.getMessage());
            return false;
        }
    }
}