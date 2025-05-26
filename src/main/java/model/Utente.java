package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.awt.Image; // Per il metodo creaToDo

public class Utente {
    private String username;
    private String password; // Considerare hashing in un'applicazione reale
    private List<Bacheca> bacheche;
    // Lista di ToDo creati direttamente dall'utente, prima di essere assegnati a una bacheca.
    // Oppure, tutti i ToDo sono sempre su una bacheca. L'UML non è chiarissimo su ToDo "orfani".
    // Assumiamo che i ToDo creati siano subito da associare a una bacheca.
    // La relazione "condivisione" 1..* Utente -- 0..* ToDo potrebbe implicare che un utente
    // "possiede" o è "autore" di ToDo, e questi ToDo possono essere condivisi.

    public Utente(String username, String password) {
        this.username = Objects.requireNonNull(username, "Username non può essere nullo.");
        this.password = Objects.requireNonNull(password, "Password non può essere nulla.");
        this.bacheche = new ArrayList<>(3); // Max 3 bacheche
    }

    public String getUsername() { return username; }
    // public void setUsername(String username) { this.username = username; } // Username di solito è immutabile

    public String getPassword() { return password; } // Solo per UtenteManager per verifica
    public void setPassword(String password) { this.password = password; } // Es. per cambio password

    public List<Bacheca> getBacheche() {
        return Collections.unmodifiableList(bacheche);
    }

    // --- Metodi da UML ---

    // login() e logout() sono più azioni a livello di sistema/sessione, gestite esternamente (es. MainFrame, UtenteManager)
    // che impostano l'utente corrente. Non sono metodi interni di un'istanza Utente.

    public boolean aggiungiBacheca(TitoloBacheca titolo, String descrizione) {
        Objects.requireNonNull(titolo, "Il titolo per la nuova bacheca non può essere nullo.");
        if (bacheche.size() >= 3) {
            System.out.println(username + ": Limite massimo di 3 bacheche raggiunto.");
            return false;
        }
        if (bacheche.stream().anyMatch(b -> b.getTitoloEnum() == titolo)) {
            System.out.println(username + ": Una bacheca con titolo '" + titolo.getDisplayName() + "' esiste già.");
            return false;
        }
        Bacheca nuovaBacheca = new Bacheca(titolo, descrizione);
        bacheche.add(nuovaBacheca);
        System.out.println(username + ": Bacheca '" + titolo.getDisplayName() + "' aggiunta.");
        return true;
    }

    public boolean modificaBacheca(TitoloBacheca titoloDaModificare, String nuovaDescrizione) {
        Optional<Bacheca> bachecaOpt = getBachecaByTitolo(titoloDaModificare);
        if (bachecaOpt.isPresent()) {
            bachecaOpt.get().setDescrizione(nuovaDescrizione);
            System.out.println(username + ": Descrizione bacheca '" + titoloDaModificare.getDisplayName() + "' modificata.");
            return true;
        }
        System.out.println(username + ": Bacheca '" + titoloDaModificare.getDisplayName() + "' non trovata per la modifica.");
        return false;
    }

    public boolean eliminaBacheca(TitoloBacheca titoloDaEliminare) {
        boolean removed = bacheche.removeIf(b -> b.getTitoloEnum() == titoloDaEliminare);
        if (removed) {
            System.out.println(username + ": Bacheca '" + titoloDaEliminare.getDisplayName() + "' eliminata.");
        } else {
            System.out.println(username + ": Bacheca '" + titoloDaEliminare.getDisplayName() + "' non trovata per l'eliminazione.");
        }
        return removed;
    }

    public Optional<Bacheca> getBachecaByTitolo(TitoloBacheca titolo) {
        return bacheche.stream().filter(b -> b.getTitoloEnum() == titolo).findFirst();
    }
    public Optional<Bacheca> getBachecaByDisplayName(String displayName) {
        try {
            TitoloBacheca titoloEnum = TitoloBacheca.fromDisplayName(displayName);
            return getBachecaByTitolo(titoloEnum);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }


    /**
     * Crea un nuovo ToDo e lo aggiunge alla bacheca specificata.
     * @return Il ToDo creato, o Optional.empty() se la bacheca non esiste.
     */
    public Optional<ToDo> creaToDo(TitoloBacheca bachecaDestinazione, String titolo, String descrizione, LocalDate scadenza, String colore, String url, Image immagine) {
        Optional<Bacheca> bachecaOpt = getBachecaByTitolo(bachecaDestinazione);
        if (bachecaOpt.isPresent()) {
            ToDo nuovoToDo = new ToDo(titolo, descrizione, this, scadenza, colore);
            if (url != null) nuovoToDo.setUrl(url);
            if (immagine != null) nuovoToDo.setImmagine(immagine);
            // La posizione iniziale potrebbe essere gestita qui o in Bacheca.aggiungiToDo
            // nuovoToDo.setPosizione(bachecaOpt.get().getTodos().size());

            bachecaOpt.get().aggiungiToDo(nuovoToDo);
            System.out.println(username + ": ToDo '" + titolo + "' creato e aggiunto a '" + bachecaDestinazione.getDisplayName() + "'.");
            return Optional.of(nuovoToDo);
        }
        System.out.println(username + ": Bacheca '" + bachecaDestinazione.getDisplayName() + "' non trovata per creare ToDo.");
        return Optional.empty();
    }

    /**
     * Modifica un ToDo esistente. L'identificazione del ToDo da modificare
     * deve essere gestita dal chiamante (es. passando l'oggetto ToDo).
     */
    public boolean modificaToDo(ToDo todoDaModificare, String nuovoTitolo, String nuovaDescrizione, LocalDate nuovaScadenza, StatoToDo nuovoStato, String nuovoColore, String nuovoUrl, Image nuovaImmagine) {
        if (todoDaModificare == null) return false;
        // Verifica che il ToDo sia gestito da questo utente (o che l'utente abbia i permessi)
        // Per ora, assumiamo che il ToDo passato sia valido e modificabile.
        // In un sistema più complesso, si verificherebbe se todoDaModificare.getAutore() == this
        // o se è condiviso con this.

        boolean modificato = false;
        if (nuovoTitolo != null) { todoDaModificare.setTitolo(nuovoTitolo); modificato = true; }
        if (nuovaDescrizione != null) { todoDaModificare.setDescrizione(nuovaDescrizione); modificato = true; }
        if (nuovaScadenza != null) { todoDaModificare.setScadenza(nuovaScadenza); modificato = true; }
        if (nuovoStato != null) { todoDaModificare.setStato(nuovoStato); modificato = true; }
        if (nuovoColore != null) { todoDaModificare.setColore(nuovoColore); modificato = true; }
        if (nuovoUrl != null) { todoDaModificare.setUrl(nuovoUrl); modificato = true; }
        if (nuovaImmagine != null) { todoDaModificare.setImmagine(nuovaImmagine); modificato = true; }

        if (modificato) System.out.println(username + ": ToDo '" + todoDaModificare.getTitolo() + "' modificato.");
        return modificato;
    }

    /**
     * Elimina un ToDo da tutte le bacheche di questo utente in cui appare.
     * Se il ToDo è stato creato da questo utente, lo elimina "logicamente" (potrebbe essere rimosso
     * anche dalle condivisioni, o contrassegnato come eliminato).
     * L'UML non specifica la politica di eliminazione per ToDo condivisi.
     * Semplificazione: lo rimuove dalle bacheche di questo utente.
     */
    public boolean eliminaToDo(ToDo todoDaEliminare) {
        if (todoDaEliminare == null) return false;
        boolean rimossoAlmenoUnaVolta = false;
        for (Bacheca b : bacheche) {
            if (b.eliminaToDo(todoDaEliminare)) {
                rimossoAlmenoUnaVolta = true;
            }
        }
        if (rimossoAlmenoUnaVolta) {
            System.out.println(username + ": ToDo '" + todoDaEliminare.getTitolo() + "' eliminato dalle proprie bacheche.");
            // Se questo utente è l'autore, considerare la revoca delle condivisioni.
            if (todoDaEliminare.getAutore().equals(this)) {
                // Logica per notificare/rimuovere da utenti condivisi, se necessario
                // todoDaEliminare.getUtentiConAccessoCondiviso().clear(); // Esempio drastico
            }
        }
        return rimossoAlmenoUnaVolta;
    }

    /**
     * Sposta un ToDo da una bacheca di origine a una di destinazione (entrambe dell'utente corrente).
     */
    public boolean spostaToDo(ToDo todoDaSpostare, TitoloBacheca origine, TitoloBacheca destinazione) {
        if (todoDaSpostare == null || origine == destinazione) return false;

        Optional<Bacheca> bachecaOrigineOpt = getBachecaByTitolo(origine);
        Optional<Bacheca> bachecaDestinazioneOpt = getBachecaByTitolo(destinazione);

        if (bachecaOrigineOpt.isPresent() && bachecaDestinazioneOpt.isPresent()) {
            Bacheca bOrigine = bachecaOrigineOpt.get();
            Bacheca bDestinazione = bachecaDestinazioneOpt.get();

            if (bOrigine.getTodos().contains(todoDaSpostare)) {
                bOrigine.eliminaToDo(todoDaSpostare);
                bDestinazione.aggiungiToDo(todoDaSpostare);
                System.out.println(username + ": ToDo '" + todoDaSpostare.getTitolo() + "' spostato da '" + origine.getDisplayName() + "' a '" + destinazione.getDisplayName() + "'.");
                return true;
            } else {
                System.out.println(username + ": ToDo '" + todoDaSpostare.getTitolo() + "' non trovato in '" + origine.getDisplayName() + "'.");
            }
        } else {
            System.out.println(username + ": Una o entrambe le bacheche (origine/destinazione) non trovate.");
        }
        return false;
    }

    // cambiaBachecaToDo è semanticamente simile a spostaToDo. Potrebbe essere un alias o avere sfumature diverse.
    public boolean cambiaBachecaToDo(ToDo todo, TitoloBacheca nuovaBacheca) {
        // Trova la bacheca corrente del ToDo per questo utente
        TitoloBacheca bachecaCorrente = null;
        for(Bacheca b : bacheche) {
            if (b.getTodos().contains(todo)) {
                bachecaCorrente = b.getTitoloEnum();
                break;
            }
        }
        if (bachecaCorrente == null) {
            System.out.println(username + ": Impossibile trovare la bacheca corrente per il ToDo '" + todo.getTitolo() + "'.");
            return false; // ToDo non trovato in nessuna bacheca di questo utente
        }
        return spostaToDo(todo, bachecaCorrente, nuovaBacheca);
    }


    /**
     * Condivide un ToDo (di cui l'utente corrente è l'autore) con un altro utente,
     * posizionandolo sulla bacheca specificata dell'utente target.
     * @param toDoDaCondividere Il ToDo da condividere (deve essere stato creato da this Utente).
     * @param utenteTarget L'utente con cui condividere.
     * @param bachecaTargetTitolo Il titolo della bacheca dell'utente target su cui posizionare il ToDo.
     * @return true se la condivisione ha avuto successo.
     */
    public boolean condividiToDo(ToDo toDoDaCondividere, Utente utenteTarget, TitoloBacheca bachecaTargetTitolo) {
        if (toDoDaCondividere == null || utenteTarget == null || bachecaTargetTitolo == null) return false;
        if (!toDoDaCondividere.getAutore().equals(this)) {
            System.out.println(username + ": Puoi condividere solo i ToDo di cui sei l'autore.");
            return false;
        }
        if (utenteTarget.equals(this)) {
            System.out.println(username + ": Non puoi condividere un ToDo con te stesso.");
            return false;
        }

        Optional<Bacheca> bachecaTargetOpt = utenteTarget.getBachecaByTitolo(bachecaTargetTitolo);
        Bacheca bachecaTargetUtente;

        if (bachecaTargetOpt.isPresent()) {
            bachecaTargetUtente = bachecaTargetOpt.get();
        } else {
            // Se l'utente target non ha la bacheca, prova a crearla (rispettando i suoi limiti)
            if (utenteTarget.aggiungiBacheca(bachecaTargetTitolo, "Bacheca per ToDo condivisi")) {
                bachecaTargetUtente = utenteTarget.getBachecaByTitolo(bachecaTargetTitolo).orElse(null); // Ricarica
                if (bachecaTargetUtente == null) {
                    System.out.println(username + ": Errore imprevisto nella creazione della bacheca target per '" + utenteTarget.getUsername() + "'.");
                    return false; // Fallimento creazione bacheca target
                }
            } else {
                System.out.println(username + ": Impossibile creare la bacheca '" + bachecaTargetTitolo.getDisplayName() + "' per l'utente target '" + utenteTarget.getUsername() + "' (limite raggiunto o esiste già con altro nome).");
                return false; // Fallimento creazione bacheca target
            }
        }

        // Aggiungi il ToDo alla bacheca dell'utente target
        // e registra che l'utente target ha accesso condiviso
        if (bachecaTargetUtente.aggiungiToDo(toDoDaCondividere)) {
            toDoDaCondividere.aggiungiUtenteCondiviso(utenteTarget);
            System.out.println(username + ": ToDo '" + toDoDaCondividere.getTitolo() + "' condiviso con '" + utenteTarget.getUsername() + "' su bacheca '" + bachecaTargetTitolo.getDisplayName() + "'.");
            return true;
        } else {
            System.out.println(username + ": ToDo '" + toDoDaCondividere.getTitolo() + "' era già presente o non è stato possibile aggiungerlo alla bacheca '" + bachecaTargetTitolo.getDisplayName() + "' di '" + utenteTarget.getUsername() + "'.");
            return false;
        }
    }

    /**
     * Revoca la condivisione di un ToDo per un utente specifico.
     * Il ToDo viene rimosso dalla bacheca dell'utente target.
     */
    public boolean revocaCondivisione(ToDo toDoCondiviso, Utente utenteTarget, TitoloBacheca bachecaTargetTitolo) {
        if (toDoCondiviso == null || utenteTarget == null || bachecaTargetTitolo == null) return false;
        if (!toDoCondiviso.getAutore().equals(this)) {
            System.out.println(username + ": Puoi revocare la condivisione solo per i ToDo di cui sei l'autore.");
            return false;
        }

        Optional<Bacheca> bachecaTargetOpt = utenteTarget.getBachecaByTitolo(bachecaTargetTitolo);
        if (bachecaTargetOpt.isPresent()) {
            if (bachecaTargetOpt.get().eliminaToDo(toDoCondiviso)) {
                toDoCondiviso.rimuoviUtenteCondiviso(utenteTarget);
                System.out.println(username + ": Condivisione del ToDo '" + toDoCondiviso.getTitolo() + "' revocata per '" + utenteTarget.getUsername() + "' dalla bacheca '" + bachecaTargetTitolo.getDisplayName() + "'.");
                return true;
            }
        }
        System.out.println(username + ": Impossibile revocare condivisione. ToDo o bacheca non trovati per utente target.");
        return false;
    }

    public List<ToDo> ricercaToDo(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) return getAllToDos();
        String term = searchTerm.toLowerCase();
        return getAllToDos().stream()
                .filter(todo -> todo.getTitolo().toLowerCase().contains(term) ||
                        todo.getDescrizione().toLowerCase().contains(term))
                .collect(Collectors.toList());
    }

    public List<ToDo> toDoInScadenza(LocalDate finoA) {
        if (finoA == null) finoA = LocalDate.now().plusDays(7); // Default: entro una settimana
        LocalDate finalFinoA = finoA;
        return getAllToDos().stream()
                .filter(todo -> todo.getScadenza() != null &&
                        !todo.getScadenza().isBefore(LocalDate.now()) && // Non scaduti nel passato
                        !todo.getScadenza().isAfter(finalFinoA)) // Entro la data specificata
                .filter(todo -> todo.getStato() == StatoToDo.NON_COMPLETATO) // Solo quelli non completati
                .sorted((t1, t2) -> t1.getScadenza().compareTo(t2.getScadenza()))
                .collect(Collectors.toList());
    }

    /**
     * Metodo helper per ottenere tutti i ToDo gestiti o accessibili da questo utente.
     * Include i ToDo su tutte le sue bacheche.
     * Se un ToDo è condiviso e appare su più bacheche dello stesso utente, apparirà una sola volta.
     */
    public List<ToDo> getAllToDos() {
        return bacheche.stream()
                .flatMap(bacheca -> bacheca.getTodos().stream())
                .distinct() // Se lo stesso ToDo (condiviso) fosse per errore su più bacheche dello stesso utente
                .collect(Collectors.toList());
    }

    /**
     * Modifica l'ordine dei ToDo in una specifica bacheca.
     */
    public boolean modificaOrdineToDoInBacheca(TitoloBacheca titoloBacheca, ToDo todo, int nuovaPosizione) {
        Optional<Bacheca> bachecaOpt = getBachecaByTitolo(titoloBacheca);
        if (bachecaOpt.isPresent()) {
            return bachecaOpt.get().modificaOrdineToDo(todo, nuovaPosizione);
        }
        return false;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Utente utente = (Utente) o;
        return username.equals(utente.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    @Override
    public String toString() {
        return username;
    }
}