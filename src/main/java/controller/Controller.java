package controller;

import model.Bacheca;
import model.StatoToDo;
import model.ToDo;
import model.TitoloBacheca;
import model.Utente;
// Non c'è più import di UtenteManager

import java.awt.Image;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Controller {
    private final Map<String, Utente> utentiRegistrati; // Sostituisce UtenteManager per la memorizzazione
    private Utente utenteCorrente;

    public Controller() {
        this.utentiRegistrati = new HashMap<>();
        this.utenteCorrente = null;
        // Eventuale creazione di un utente admin di default, se necessario
        // Esempio: registraUtente("admin", "admin123"); // Chiamata al metodo interno
        // Per coerenza con il precedente UtenteManager, aggiungiamo l'admin
        Utente admin = new Utente("admin", "admin123");
        this.utentiRegistrati.put("admin", admin);
        System.out.println("Controller: Utente admin di default creato.");
    }

    // --- Gestione Utenti e Sessione (Logica prima in UtenteManager) ---

    /**
     * Registra un nuovo utente nel sistema.
     *
     * @param username L'username desiderato.
     * @param password La password per il nuovo utente.
     * @return true se la registrazione ha successo, false se l'username esiste già.
     */
    public boolean registraUtente(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            System.err.println("Controller: Username e password non possono essere vuoti per la registrazione.");
            return false;
        }
        if (utentiRegistrati.containsKey(username)) {
            System.out.println("Controller: Username '" + username + "' già esistente.");
            return false; // Username già in uso
        }
        Utente nuovoUtente = new Utente(username, password);
        utentiRegistrati.put(username, nuovoUtente);
        System.out.println("Controller: Utente '" + username + "' registrato con successo.");
        return true;
    }

    /**
     * Esegue il login di un utente.
     *
     * @param username L'username dell'utente.
     * @param password La password dell'utente.
     * @return true se il login ha successo, false altrimenti.
     */
    public boolean login(String username, String password) {
        Utente utente = utentiRegistrati.get(username);
        if (utente != null && utente.getPassword().equals(password)) { // Confronto password diretto (non sicuro per produzione)
            this.utenteCorrente = utente;
            System.out.println("Controller: Utente '" + username + "' loggato con successo.");
            return true;
        }
        this.utenteCorrente = null;
        if (utente == null) {
            System.out.println("Controller: Login fallito - Utente '" + username + "' non trovato.");
        } else {
            System.out.println("Controller: Login fallito - Password errata per l'utente '" + username + "'.");
        }
        return false;
    }

    public void logout() {
        if (this.utenteCorrente != null) {
            System.out.println("Controller: Utente '" + this.utenteCorrente.getUsername() + "' sloggato.");
        }
        this.utenteCorrente = null;
    }

    public Utente getUtenteCorrente() {
        return utenteCorrente;
    }

    public boolean isUserLoggedIn() {
        return utenteCorrente != null;
    }

    /**
     * Recupera un'istanza Utente dato il suo username.
     * Usato internamente o per funzionalità come la condivisione.
     *
     * @param username L'username da cercare.
     * @return Optional contenente l'Utente se trovato, altrimenti Optional.empty().
     */
    public Optional<Utente> getUtenteByUsername(String username) {
        return Optional.ofNullable(utentiRegistrati.get(username));
    }

    // --- Operazioni su Bacheche (delegate all'Utente corrente) ---

    public boolean aggiungiBacheca(TitoloBacheca titolo, String descrizione) {
        if (!isUserLoggedIn()) {
            System.err.println("Controller: Nessun utente loggato. Impossibile aggiungere bacheca.");
            return false;
        }
        return utenteCorrente.aggiungiBacheca(titolo, descrizione);
    }

    public boolean modificaDescrizioneBacheca(TitoloBacheca titolo, String nuovaDescrizione) {
        if (!isUserLoggedIn()) {
            System.err.println("Controller: Nessun utente loggato. Impossibile modificare bacheca.");
            return false;
        }
        return utenteCorrente.modificaBacheca(titolo, nuovaDescrizione);
    }

    public boolean eliminaBacheca(TitoloBacheca titolo) {
        if (!isUserLoggedIn()) {
            System.err.println("Controller: Nessun utente loggato. Impossibile eliminare bacheca.");
            return false;
        }
        return utenteCorrente.eliminaBacheca(titolo);
    }

    public List<Bacheca> getBachecheUtenteCorrente() {
        if (!isUserLoggedIn()) {
            return Collections.emptyList();
        }
        return utenteCorrente.getBacheche();
    }

    public Optional<Bacheca> getBachecaByDisplayNameDaUtenteCorrente(String displayName) {
        if (!isUserLoggedIn()) {
            return Optional.empty();
        }
        return utenteCorrente.getBachecaByDisplayName(displayName);
    }

    public int getMaxBoards() {
        return 3;
    }

    public List<String> getAvailableBoardTitlesForCurrentUser() {
        if (!isUserLoggedIn()) {
            return Collections.emptyList();
        }
        List<String> currentBoardDisplayNames = utenteCorrente.getBacheche().stream()
                .map(Bacheca::getTitoloDisplayName)
                .collect(Collectors.toList());
        if (currentBoardDisplayNames.size() >= getMaxBoards()) {
            return Collections.emptyList();
        }

        return java.util.Arrays.stream(TitoloBacheca.values())
                .map(TitoloBacheca::getDisplayName)
                .filter(displayName -> !currentBoardDisplayNames.contains(displayName)) // Case sensitive, ok se i nomi sono canonici
                .collect(Collectors.toList());
    }

    // --- Operazioni su ToDo (delegate all'Utente corrente) ---

    public Optional<ToDo> creaToDo(String nomeBachecaDestinazione, String titolo, String descrizione,
                                   LocalDate scadenza, String colore, String url, Image immagine) {
        if (!isUserLoggedIn()) {
            System.err.println("Controller: Nessun utente loggato. Impossibile creare ToDo.");
            return Optional.empty();
        }
        try {
            TitoloBacheca titoloEnum = TitoloBacheca.fromDisplayName(nomeBachecaDestinazione);
            return utenteCorrente.creaToDo(titoloEnum, titolo, descrizione, scadenza, colore, url, immagine);
        } catch (IllegalArgumentException e) {
            System.err.println("Controller: Nome bacheca non valido '" + nomeBachecaDestinazione + "' per creazione ToDo. " + e.getMessage());
            return Optional.empty();
        }
    }

    public boolean modificaToDo(ToDo todoDaModificare, String nuovoTitolo, String nuovaDescrizione,
                                LocalDate nuovaScadenza, StatoToDo nuovoStato, String nuovoColore,
                                String nuovoUrl, Image nuovaImmagine) {
        if (!isUserLoggedIn() || todoDaModificare == null) {
            System.err.println("Controller: Nessun utente loggato o ToDo nullo. Impossibile modificare ToDo.");
            return false;
        }
        // Assumiamo che utenteCorrente.modificaToDo gestisca i permessi (es. solo autore)
        return utenteCorrente.modificaToDo(todoDaModificare, nuovoTitolo, nuovaDescrizione, nuovaScadenza, nuovoStato, nuovoColore, nuovoUrl, nuovaImmagine);
    }

    public boolean eliminaToDo(ToDo todoDaEliminare) {
        if (!isUserLoggedIn() || todoDaEliminare == null) {
            System.err.println("Controller: Nessun utente loggato o ToDo nullo. Impossibile eliminare ToDo.");
            return false;
        }
        return utenteCorrente.eliminaToDo(todoDaEliminare);
    }

    public List<ToDo> getToDosPerBacheca(String nomeBacheca) {
        if (!isUserLoggedIn()) {
            return Collections.emptyList();
        }
        Optional<Bacheca> bachecaOpt = utenteCorrente.getBachecaByDisplayName(nomeBacheca);
        return bachecaOpt.map(Bacheca::getTodos).orElseGet(ArrayList::new); // Restituisce lista modificabile se necessario? No, Bacheca.getTodos() è unmodifiable.
    }

    public List<ToDo> getAllToDosUtenteCorrente() {
        if (!isUserLoggedIn()) {
            return Collections.emptyList();
        }
        return utenteCorrente.getAllToDos();
    }

    public boolean spostaToDo(ToDo todoDaSpostare, String nomeBachecaOrigine, String nomeBachecaDestinazione) {
        if (!isUserLoggedIn() || todoDaSpostare == null) return false;
        try {
            TitoloBacheca origineEnum = TitoloBacheca.fromDisplayName(nomeBachecaOrigine);
            TitoloBacheca destinazioneEnum = TitoloBacheca.fromDisplayName(nomeBachecaDestinazione);
            return utenteCorrente.spostaToDo(todoDaSpostare, origineEnum, destinazioneEnum);
        } catch (IllegalArgumentException e) {
            System.err.println("Controller: Nomi bacheca non validi per lo spostamento. " + e.getMessage());
            return false;
        }
    }

    public boolean cambiaBachecaToDo(ToDo todo, String nomeNuovaBacheca) {
        if (!isUserLoggedIn() || todo == null) return false;
        try {
            TitoloBacheca nuovaBachecaEnum = TitoloBacheca.fromDisplayName(nomeNuovaBacheca);
            return utenteCorrente.cambiaBachecaToDo(todo, nuovaBachecaEnum);
        } catch (IllegalArgumentException e) {
            System.err.println("Controller: Nome nuova bacheca non valido. " + e.getMessage());
            return false;
        }
    }

    public boolean modificaOrdineToDoInBacheca(String nomeBacheca, ToDo todo, int nuovaPosizione) {
        if (!isUserLoggedIn() || todo == null) return false;
        try {
            TitoloBacheca bachecaEnum = TitoloBacheca.fromDisplayName(nomeBacheca);
            return utenteCorrente.modificaOrdineToDoInBacheca(bachecaEnum, todo, nuovaPosizione);
        } catch (IllegalArgumentException e) {
            System.err.println("Controller: Nome bacheca non valido per modifica ordine. " + e.getMessage());
            return false;
        }
    }

    // --- Condivisione ToDo ---
    public boolean condividiToDo(ToDo toDoDaCondividere, String usernameDestinatario, String nomeBachecaDestinazioneDisplay) {
        if (!isUserLoggedIn() || toDoDaCondividere == null) {
            System.err.println("Controller: Utente non loggato o ToDo nullo per condivisione.");
            return false;
        }
        if (!toDoDaCondividere.getAutore().equals(utenteCorrente)) {
            System.err.println("Controller: Solo l'autore ('" + toDoDaCondividere.getAutore().getUsername() + "') può condividere questo ToDo. Utente corrente: '" + utenteCorrente.getUsername() + "'.");
            return false;
        }

        Optional<Utente> utenteTargetOpt = getUtenteByUsername(usernameDestinatario); // Usa il metodo interno
        if (utenteTargetOpt.isEmpty()) {
            System.err.println("Controller: Utente destinatario '" + usernameDestinatario + "' non trovato.");
            return false;
        }

        try {
            TitoloBacheca bachecaTargetEnum = TitoloBacheca.fromDisplayName(nomeBachecaDestinazioneDisplay);
            // Il metodo utenteCorrente.condividiToDo si aspetta l'Utente target e il TitoloBacheca target
            return utenteCorrente.condividiToDo(toDoDaCondividere, utenteTargetOpt.get(), bachecaTargetEnum);
        } catch (IllegalArgumentException e) {
            System.err.println("Controller: Nome bacheca destinazione non valido per condivisione: '" + nomeBachecaDestinazioneDisplay + "'. " + e.getMessage());
            return false;
        }
    }

    public boolean revocaCondivisione(ToDo toDoCondiviso, String usernameTarget, String nomeBachecaTargetDisplay) {
        if (!isUserLoggedIn() || toDoCondiviso == null) return false;

        Optional<Utente> utenteTargetOpt = getUtenteByUsername(usernameTarget);
        if (utenteTargetOpt.isEmpty()) {
            System.err.println("Controller: Utente target '" + usernameTarget + "' non trovato per revoca condivisione.");
            return false;
        }
        try {
            TitoloBacheca bachecaTargetEnum = TitoloBacheca.fromDisplayName(nomeBachecaTargetDisplay);
            return utenteCorrente.revocaCondivisione(toDoCondiviso, utenteTargetOpt.get(), bachecaTargetEnum);
        } catch (IllegalArgumentException e) {
            System.err.println("Controller: Nome bacheca target non valido per revoca: '" + nomeBachecaTargetDisplay + "'. " + e.getMessage());
            return false;
        }
    }

    // --- Ricerca e Filtri ---
    public List<ToDo> ricercaToDo(String searchTerm) {
        if (!isUserLoggedIn()) return Collections.emptyList();
        return utenteCorrente.ricercaToDo(searchTerm);
    }

    public List<ToDo> toDoInScadenza(LocalDate finoA) {
        if (!isUserLoggedIn()) return Collections.emptyList();
        return utenteCorrente.toDoInScadenza(finoA);
    }

    // --- Salvataggio Dati (Simbolico per In-Memory) ---

    /**
     * In un contesto puramente in-memory, questo metodo è simbolico.
     * Non c'è persistenza su file. Lo stato è "salvato" finché l'applicazione è in esecuzione.
     * Potrebbe essere usato per loggare uno snapshot o per future estensioni.
     */
    public boolean salvaStatoApplicazione() {
        if (isUserLoggedIn()) {
            System.out.println("Controller: 'salvaStatoApplicazione' chiamato per utente '" + utenteCorrente.getUsername() + "'. Tutti i dati sono gestiti in memoria.");
        } else {
            System.out.println("Controller: 'salvaStatoApplicazione' chiamato. Nessun utente loggato. Tutti i dati sono gestiti in memoria.");
        }
        // Non c'è un'azione di salvataggio reale da compiere qui per il modello in-memory.
        return true;
    }
}