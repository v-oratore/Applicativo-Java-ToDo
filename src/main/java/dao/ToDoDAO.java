package dao;

import model.ToDo;
import model.Bacheca;
import model.Utente;
import java.util.List;
import java.util.Optional;

/**
 * Interfaccia DAO per la gestione dei Task.
 * Definisce le operazioni CRUD, di ricerca e di condivisione per i Task nel database.
 */
public interface ToDoDAO {
    /**
     * Cerca un Task tramite ID.
     * @param id ID del Task
     * @return Optional contenente il Task se trovato, altrimenti Optional vuoto
     */
    Optional<ToDo> findById(int id);

    /**
     * Restituisce tutti i Task associati a una bacheca.
     * @param bacheca Oggetto Bacheca
     * @return Lista di Task della bacheca
     */
    List<ToDo> findAllByBacheca(Bacheca bacheca);

    /**
     * Restituisce tutti i Task associati a una bacheca tramite ID.
     * @param bachecaId ID della bacheca
     * @return Lista di Task della bacheca
     */
    List<ToDo> findAllByBachecaId(int bachecaId);

    /**
     * Restituisce tutti i Task creati da un autore.
     * @param autore Oggetto Utente autore
     * @return Lista di Task creati dall'autore
     */
    List<ToDo> findAllByAutore(Utente autore);

    /**
     * Restituisce tutti i Task creati da un autore tramite ID.
     * @param autoreId ID dell'autore
     * @return Lista di Task creati dall'autore
     */
    List<ToDo> findAllByAutoreId(int autoreId);

    /**
     * Restituisce tutti i Task condivisi con un utente.
     * @param utente Utente destinatario della condivisione
     * @return Lista di Task condivisi con l'utente
     */
    List<ToDo> findAllSharedWithUser(Utente utente);

    /**
     * Restituisce tutti i Task condivisi con un utente, includendo l'ID della bacheca di destinazione.
     * @param utente Utente destinatario della condivisione
     * @return Lista di Task condivisi con l'utente, con bachecaDestinazioneId impostato
     */
    List<ToDo> findAllSharedWithUserAndDestination(Utente utente);

    /**
     * Salva un nuovo Task nel database.
     * @param todo Oggetto Task da salvare
     * @return true se il salvataggio ha successo, false altrimenti
     */
    boolean save(ToDo todo);

    /**
     * Aggiorna un Task esistente nel database.
     * @param todo Oggetto Task da aggiornare
     * @return true se l'aggiornamento ha successo, false altrimenti
     */
    boolean update(ToDo todo);

    /**
     * Elimina un Task tramite ID.
     * @param id ID del Task da eliminare
     * @return true se l'eliminazione ha successo, false altrimenti
     */
    boolean delete(int id);

    /**
     * Aggiunge una condivisione di un Task con un altro utente.
     * @param todo Task da condividere
     * @param utenteConCuiCondividere Utente destinatario
     * @param bachecaDestinazioneId ID della bacheca di destinazione dell'utente destinatario
     * @return true se la condivisione ha successo, false altrimenti
     */
    boolean addCondivisione(ToDo todo, Utente utenteConCuiCondividere, Integer bachecaDestinazioneId);

    /**
     * Rimuove la condivisione di un Task per un utente.
     * @param todo Task condiviso
     * @param utenteDaRimuovere Utente da rimuovere dalla condivisione
     * @return true se la rimozione ha successo, false altrimenti
     */
    boolean removeCondivisione(ToDo todo, Utente utenteDaRimuovere);

    /**
     * Restituisce la lista degli utenti con cui un Task è condiviso.
     * @param todo Task di interesse
     * @return Lista di utenti con cui il Task è condiviso
     */
    List<Utente> getUtentiCondivisione(ToDo todo);
}