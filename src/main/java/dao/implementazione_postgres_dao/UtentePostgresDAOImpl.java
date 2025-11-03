package dao.implementazione_postgres_dao;

import dao.UtenteDAO;
import database.ConnessioneDatabase;
import model.Utente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Implementazione Postgres del DAO per la gestione degli utenti.
 * Fornisce metodi per CRUD e ricerca degli utenti nel database PostgreSQL.
 */
@SuppressWarnings("java:S2139")
public class UtentePostgresDAOImpl implements UtenteDAO {

    private Connection conn;
    private static final Logger LOGGER = Logger.getLogger(UtentePostgresDAOImpl.class.getName());
    private static final String USERNAME = "username";
    private static final String PASSWORD_HASH = "password_hash";
    private static final String SELECT_ID = "SELECT id, ";

    /**
     * Costruttore. Inizializza la connessione al database tramite il singleton ConnessioneDatabase.
     * @throws DatabaseConnectionException se la connessione fallisce
     */
    public UtentePostgresDAOImpl() {
        try {
            conn = ConnessioneDatabase.getInstance().getConnection();
        } catch (SQLException e) {
            LOGGER.severe("Errore nel costruttore di UtentePostgresDAOImpl: " + e.getMessage());
            throw new DatabaseConnectionException("Impossibile stabilire la connessione al database per UtenteDAO", e);
        }
    }

    /**
     * Cerca un utente tramite ID.
     * @param id ID dell'utente
     * @return Optional contenente l'utente se trovato, altrimenti Optional vuoto
     */
    @Override
    public Optional<Utente> findById(int id) {
        if (conn == null) {
            LOGGER.severe("findById Utente fallito: connessione DB non disponibile.");
            return Optional.empty();
        }
        String query = SELECT_ID + USERNAME + ", " + PASSWORD_HASH + " FROM utenti WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(new Utente(
                        rs.getInt("id"),
                        rs.getString(USERNAME),
                        rs.getString(PASSWORD_HASH)
                ));
            }
        } catch (SQLException e) {
            LOGGER.severe("Errore findById Utente: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Cerca un utente tramite username.
     * @param username Username da cercare
     * @return Optional contenente l'utente se trovato, altrimenti Optional vuoto
     */
    @Override
    public Optional<Utente> findByUsername(String username) {
        if (conn == null) {
            LOGGER.severe("findByUsername Utente fallito: connessione DB non disponibile.");
            return Optional.empty();
        }
        String query = SELECT_ID + USERNAME + ", " + PASSWORD_HASH + " FROM utenti WHERE username = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(new Utente(
                        rs.getInt("id"),
                        rs.getString(USERNAME),
                        rs.getString(PASSWORD_HASH)
                ));
            }
        } catch (SQLException e) {
            LOGGER.severe("Errore findByUsername Utente: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Restituisce la lista di tutti gli utenti presenti nel database.
     * @return Lista di utenti
     */
    @Override
    public List<Utente> findAll() {
        List<Utente> utenti = new ArrayList<>();
        if (conn == null) {
            LOGGER.severe("findAll Utenti fallito: connessione DB non disponibile.");
            return utenti;
        }
        String query = SELECT_ID + USERNAME + ", " + PASSWORD_HASH + " FROM utenti ORDER BY username";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                utenti.add(new Utente(
                        rs.getInt("id"),
                        rs.getString(USERNAME),
                        rs.getString(PASSWORD_HASH)
                ));
            }
        } catch (SQLException e) {
            LOGGER.severe("Errore findAll Utenti: " + e.getMessage());
        }
        return utenti;
    }

    /**
     * Salva un nuovo utente nel database o aggiorna se già esistente.
     * @param utente Oggetto Utente da salvare
     * @return true se il salvataggio o aggiornamento ha successo, false altrimenti
     */
    @Override
    public boolean save(Utente utente) {
        if (conn == null) {
            LOGGER.severe("save Utente fallito: connessione DB non disponibile.");
            return false;
        }
        if (utente.getId() != 0) {
            return update(utente); // Se l'ID è già impostato, esegui un update
        }

        String query = "INSERT INTO utenti (" + USERNAME + ", " + PASSWORD_HASH + ") VALUES (?, ?) RETURNING id";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, utente.getUsername());
            ps.setString(2, utente.getPasswordHash());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                utente.setId(rs.getInt(1));
                return true;
            }
            return false;
        } catch (SQLException e) {
            LOGGER.severe("Errore save Utente: " + e.getMessage());
            if ("23505".equals(e.getSQLState())) {
                LOGGER.warning("Tentativo di inserire un username duplicato: " + utente.getUsername());
            }
            return false;
        }
    }

    /**
     * Aggiorna un utente esistente nel database.
     * @param utente Oggetto Utente da aggiornare
     * @return true se l'aggiornamento ha successo, false altrimenti
     */
    @Override
    public boolean update(Utente utente) {
        if (conn == null) {
            LOGGER.severe("update Utente fallito: connessione DB non disponibile.");
            return false;
        }
        if (utente.getId() == 0) {
            LOGGER.severe("Impossibile aggiornare utente senza ID.");
            return false;
        }
        String query = "UPDATE utenti SET " + USERNAME + " = ?, " + PASSWORD_HASH + " = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, utente.getUsername());
            ps.setString(2, utente.getPasswordHash());
            ps.setInt(3, utente.getId());
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.severe("Errore update Utente: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina un utente tramite ID.
     * @param id ID dell'utente da eliminare
     * @return true se l'eliminazione ha successo, false altrimenti
     */
    @Override
    public boolean delete(int id) {
        if (conn == null) {
            LOGGER.severe("delete Utente fallito: connessione DB non disponibile.");
            return false;
        }
        String query = "DELETE FROM utenti WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.severe("Errore delete Utente by ID: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina un utente tramite username.
     * @param username Username dell'utente da eliminare
     * @return true se l'eliminazione ha successo, false altrimenti
     */
    @Override
    public boolean deleteByUsername(String username) {
        if (conn == null) {
            LOGGER.severe("delete Utente by Username fallito: connessione DB non disponibile.");
            return false;
        }
        String query = "DELETE FROM utenti WHERE username = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, username);
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.severe("Errore delete Utente by Username: " + e.getMessage());
            return false;
        }
    }
}