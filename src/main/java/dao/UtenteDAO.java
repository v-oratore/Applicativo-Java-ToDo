package dao;

import model.Utente;
import java.util.List;
import java.util.Optional;

/**
 * Interfaccia DAO per la gestione degli utenti.
 * Definisce le operazioni CRUD e di ricerca per gli utenti nel database.
 */
public interface UtenteDAO {
    /**
     * Cerca un utente tramite ID.
     * @param id ID dell'utente
     * @return Optional contenente l'utente se trovato, altrimenti Optional vuoto
     */
    Optional<Utente> findById(int id);

    /**
     * Cerca un utente tramite username.
     * @param username Username da cercare
     * @return Optional contenente l'utente se trovato, altrimenti Optional vuoto
     */
    Optional<Utente> findByUsername(String username);

    /**
     * Restituisce la lista di tutti gli utenti presenti nel database.
     * @return Lista di utenti
     */
    List<Utente> findAll();

    /**
     * Salva un nuovo utente nel database (insert).
     * @param utente Oggetto Utente da salvare
     * @return true se il salvataggio ha successo, false altrimenti
     */
    boolean save(Utente utente);

    /**
     * Aggiorna un utente esistente nel database.
     * @param utente Oggetto Utente da aggiornare
     * @return true se l'aggiornamento ha successo, false altrimenti
     */
    boolean update(Utente utente);

    /**
     * Elimina un utente tramite ID.
     * @param id ID dell'utente da eliminare
     * @return true se l'eliminazione ha successo, false altrimenti
     */
    boolean delete(int id);

    /**
     * Elimina un utente tramite username.
     * @param username Username dell'utente da eliminare
     * @return true se l'eliminazione ha successo, false altrimenti
     */
    boolean deleteByUsername(String username);
}