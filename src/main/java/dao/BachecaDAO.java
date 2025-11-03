package dao;

import model.Bacheca;
import model.TitoloBacheca;
import model.Utente;
import java.util.List;
import java.util.Optional;

/**
 * Interfaccia DAO per la gestione delle bacheche.
 * Definisce le operazioni CRUD e di ricerca per le bacheche nel database.
 */
public interface BachecaDAO {
    /**
     * Cerca una bacheca tramite ID.
     * @param id ID della bacheca
     * @return Optional contenente la bacheca se trovata, altrimenti Optional vuoto
     */
    Optional<Bacheca> findById(int id);

    /**
     * Restituisce tutte le bacheche associate a un utente.
     * @param utente Utente di cui recuperare le bacheche
     * @return Lista di bacheche dell'utente
     */
    List<Bacheca> findByUtente(Utente utente);

    /**
     * Restituisce tutte le bacheche associate a un utente tramite ID.
     * @param utenteId ID dell'utente
     * @return Lista di bacheche dell'utente
     */
    List<Bacheca> findByUtenteId(int utenteId);

    /**
     * Cerca una bacheca tramite utente e titolo.
     * @param utente Utente proprietario
     * @param titolo Titolo della bacheca
     * @return Optional contenente la bacheca se trovata, altrimenti Optional vuoto
     */
    Optional<Bacheca> findByUtenteAndTitolo(Utente utente, TitoloBacheca titolo);

    /**
     * Cerca una bacheca tramite ID utente e titolo.
     * @param utenteId ID dell'utente
     * @param titolo Titolo della bacheca
     * @return Optional contenente la bacheca se trovata, altrimenti Optional vuoto
     */
    Optional<Bacheca> findByUtenteIdAndTitolo(int utenteId, TitoloBacheca titolo);

    /**
     * Salva una nuova bacheca per un utente.
     * @param bacheca Oggetto Bacheca da salvare
     * @param utenteId ID dell'utente proprietario
     * @return true se il salvataggio ha successo, false altrimenti
     */
    boolean save(Bacheca bacheca, int utenteId);

    /**
     * Aggiorna una bacheca esistente.
     * @param bacheca Oggetto Bacheca da aggiornare
     * @return true se l'aggiornamento ha successo, false altrimenti
     */
    boolean update(Bacheca bacheca);

    /**
     * Elimina una bacheca tramite ID.
     * @param id ID della bacheca da eliminare
     * @return true se l'eliminazione ha successo, false altrimenti
     */
    boolean delete(int id);

    /**
     * Elimina una bacheca tramite utente e titolo.
     * @param utente Utente proprietario
     * @param titolo Titolo della bacheca
     * @return true se l'eliminazione ha successo, false altrimenti
     */
    boolean deleteByUtenteAndTitolo(Utente utente, TitoloBacheca titolo);
}