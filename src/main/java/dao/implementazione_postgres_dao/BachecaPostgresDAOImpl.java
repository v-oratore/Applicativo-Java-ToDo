package dao.implementazione_postgres_dao;

import dao.BachecaDAO;
import database.ConnessioneDatabase;
import model.Bacheca;
import model.TitoloBacheca;
import model.Utente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Implementazione Postgres del DAO per la gestione delle bacheche.
 * Fornisce metodi per CRUD e ricerca delle bacheche nel database PostgreSQL.
 */
@SuppressWarnings("java:S2139")
public class BachecaPostgresDAOImpl implements BachecaDAO {

    private Connection conn;
    private static final Logger LOGGER = Logger.getLogger(BachecaPostgresDAOImpl.class.getName());

    /**
     * Costruttore. Inizializza la connessione al database tramite il singleton ConnessioneDatabase.
     * @throws DatabaseConnectionException se la connessione fallisce
     */
    public BachecaPostgresDAOImpl() {
        try {
            conn = ConnessioneDatabase.getInstance().getConnection();
        } catch (SQLException e) {
            LOGGER.severe("Errore nel costruttore di BachecaPostgresDAOImpl: " + e.getMessage());
            throw new DatabaseConnectionException(
                    "Impossibile connettersi al database nel costruttore di BachecaPostgresDAOImpl", e);
        }
    }

    /**
     * Cerca una bacheca tramite ID.
     * @param id ID della bacheca
     * @return Optional contenente la bacheca se trovata, altrimenti Optional vuoto
     * @throws DatabaseConnectionException in caso di errore SQL
     */
    @Override
    public Optional<Bacheca> findById(int id) {
        if (conn == null) {
            LOGGER.severe("findById Bacheca fallito: connessione DB non disponibile.");
            return Optional.empty();
        }
        String query = "SELECT id, utente_id, titolo_bacheca, descrizione FROM bacheche WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToBacheca(rs));
            }
        } catch (SQLException e) {
            LOGGER.severe("Errore findById Bacheca: " + e.getMessage());
            throw new DatabaseConnectionException("Errore findById Bacheca", e);
        }
        return Optional.empty();
    }

    /**
     * Restituisce tutte le bacheche associate a un utente.
     * @param utente Utente di cui recuperare le bacheche
     * @return Lista di bacheche dell'utente
     */
    @Override
    public List<Bacheca> findByUtente(Utente utente) {
        if (utente == null || utente.getId() == 0) {
            LOGGER.severe("findByUtente Bacheca fallito: utente non valido.");
            return new ArrayList<>();
        }
        return findByUtenteId(utente.getId());
    }

    /**
     * Restituisce tutte le bacheche associate a un utente tramite ID.
     * @param utenteId ID dell'utente
     * @return Lista di bacheche dell'utente
     * @throws DatabaseConnectionException in caso di errore SQL
     */
    @Override
    public List<Bacheca> findByUtenteId(int utenteId) {
        List<Bacheca> bacheche = new ArrayList<>();
        if (conn == null) {
            LOGGER.severe("findByUtenteId Bacheche fallito: connessione DB non disponibile.");
            return bacheche;
        }
        String query = "SELECT id, utente_id, titolo_bacheca, descrizione FROM bacheche WHERE utente_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, utenteId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                bacheche.add(mapRowToBacheca(rs));
            }
        } catch (SQLException e) {
            LOGGER.severe("Errore findByUtenteId Bacheche: " + e.getMessage());
            throw new DatabaseConnectionException("Errore findByUtenteId Bacheche", e);
        }
        return bacheche;
    }

    /**
     * Cerca una bacheca tramite utente e titolo.
     * @param utente Utente proprietario
     * @param titolo Titolo della bacheca
     * @return Optional contenente la bacheca se trovata, altrimenti Optional vuoto
     */
    @Override
    public Optional<Bacheca> findByUtenteAndTitolo(Utente utente, TitoloBacheca titolo) {
        if (utente == null || utente.getId() == 0) {
            LOGGER.severe("findByUtenteAndTitolo Bacheca fallito: utente non valido.");
            return Optional.empty();
        }
        return findByUtenteIdAndTitolo(utente.getId(), titolo);
    }

    /**
     * Cerca una bacheca tramite ID utente e titolo.
     * @param utenteId ID dell'utente
     * @param titolo Titolo della bacheca
     * @return Optional contenente la bacheca se trovata, altrimenti Optional vuoto
     * @throws DatabaseConnectionException in caso di errore SQL
     */
    @Override
    public Optional<Bacheca> findByUtenteIdAndTitolo(int utenteId, TitoloBacheca titolo) {
        if (conn == null) {
            LOGGER.severe("findByUtenteIdAndTitolo Bacheca fallito: connessione DB non disponibile.");
            return Optional.empty();
        }
        String query = "SELECT id, utente_id, titolo_bacheca, descrizione FROM bacheche WHERE utente_id = ? AND titolo_bacheca = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, utenteId);
            ps.setString(2, titolo.name());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToBacheca(rs));
            }
        } catch (SQLException e) {
            LOGGER.severe("Errore findByUtenteAndTitolo Bacheca: " + e.getMessage());
            throw new DatabaseConnectionException("Errore findByUtenteAndTitolo Bacheca", e);
        }
        return Optional.empty();
    }

    /**
     * Salva una nuova bacheca nel database.
     * @param bacheca Oggetto Bacheca da salvare
     * @param utenteId ID dell'utente proprietario
     * @return true se il salvataggio ha successo, false altrimenti
     * @throws DatabaseConnectionException in caso di errore SQL
     */
    @Override
    public boolean save(Bacheca bacheca, int utenteId) {
        if (conn == null) {
            LOGGER.severe("save Bacheca fallito: connessione DB non disponibile.");
            return false;
        }
        String query = "INSERT INTO bacheche (utente_id, titolo_bacheca, descrizione) VALUES (?, ?, ?) RETURNING id";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, utenteId);
            ps.setString(2, bacheca.getTitoloEnum().name());
            ps.setString(3, bacheca.getDescrizione());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                bacheca.setId(rs.getInt(1));
                bacheca.setUtenteId(utenteId); // Imposta l'utenteId anche sull'oggetto Bacheca
                return true;
            }
            return false;
        } catch (SQLException e) {
            LOGGER.severe("Errore save Bacheca: " + e.getMessage());
            if ("23505".equals(e.getSQLState())) {
                LOGGER.warning("Tentativo di inserire bacheca duplicata per utente " + utenteId + " e titolo "
                        + bacheca.getTitoloEnum().name());
            }
            throw new DatabaseConnectionException("Errore save Bacheca", e);
        }
    }

    /**
     * Aggiorna una bacheca esistente nel database.
     * @param bacheca Oggetto Bacheca da aggiornare
     * @return true se l'aggiornamento ha successo, false altrimenti
     * @throws DatabaseConnectionException in caso di errore SQL
     */
    @Override
    public boolean update(Bacheca bacheca) {
        if (conn == null) {
            LOGGER.severe("update Bacheca fallito: connessione DB non disponibile.");
            return false;
        }
        if (bacheca.getId() == 0) {
            LOGGER.severe("Impossibile aggiornare bacheca senza ID.");
            return false;
        }
        String query = "UPDATE bacheche SET titolo_bacheca = ?, descrizione = ? WHERE id = ? AND utente_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, bacheca.getTitoloEnum().name());
            ps.setString(2, bacheca.getDescrizione());
            ps.setInt(3, bacheca.getId());
            ps.setInt(4, bacheca.getUtenteId());

            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.severe("Errore update Bacheca: " + e.getMessage());
            throw new DatabaseConnectionException("Errore update Bacheca", e);
        }
    }

    /**
     * Elimina una bacheca tramite ID.
     * @param id ID della bacheca da eliminare
     * @return true se l'eliminazione ha successo, false altrimenti
     * @throws DatabaseConnectionException in caso di errore SQL
     */
    @Override
    public boolean delete(int id) {
        if (conn == null) {
            LOGGER.severe("delete Bacheca fallito: connessione DB non disponibile.");
            return false;
        }
        String query = "DELETE FROM bacheche WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.severe("Errore delete Bacheca by ID: " + e.getMessage());
            throw new DatabaseConnectionException("Errore delete Bacheca by ID", e);
        }
    }

    /**
     * Elimina una bacheca tramite utente e titolo.
     * @param utente Utente proprietario
     * @param titolo Titolo della bacheca
     * @return true se l'eliminazione ha successo, false altrimenti
     */
    @Override
    public boolean deleteByUtenteAndTitolo(Utente utente, TitoloBacheca titolo) {
        if (utente == null || titolo == null) {
            LOGGER.severe("deleteByUtenteAndTitolo Bacheca fallito: utente o titolo non validi.");
            return false;
        }
        Optional<Bacheca> bachecaOpt = findByUtenteAndTitolo(utente, titolo);
        if (bachecaOpt.isPresent()) {
            return delete(bachecaOpt.get().getId());
        }
        return false;
    }

    /**
     * Mappa una riga del ResultSet a un oggetto Bacheca.
     * @param rs ResultSet posizionato sulla riga da mappare
     * @return Oggetto Bacheca corrispondente
     * @throws SQLException in caso di errore di accesso ai dati
     */
    private Bacheca mapRowToBacheca(ResultSet rs) throws SQLException {
        return new Bacheca(
                rs.getInt("id"),
                rs.getInt("utente_id"), // Recupera utente_id dal ResultSet
                TitoloBacheca.valueOf(rs.getString("titolo_bacheca")),
                rs.getString("descrizione"));
    }
}