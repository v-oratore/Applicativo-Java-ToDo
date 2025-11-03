package dao.implementazione_postgres_dao;

import dao.ToDoDAO;
import dao.UtenteDAO;
import database.ConnessioneDatabase;
import model.ToDo;
import model.Bacheca;
import model.Utente;
import model.StatoToDo;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Implementazione Postgres del DAO per la gestione dei Task.
 * Fornisce metodi per CRUD, ricerca e condivisione dei Task nel database PostgreSQL.
 */
@SuppressWarnings("java:S2139")
public class ToDoPostgresDAOImpl implements ToDoDAO {

    private Connection conn;
    private UtenteDAO utenteDAO; // Per risolvere l'autore e gli utenti condivisi
    private static final Logger LOGGER = Logger.getLogger(ToDoPostgresDAOImpl.class.getName());

    /** Costante per il nome della colonna bacheca_destinazione_id. */
    private static final String COLONNA_BACHECA_DESTINAZIONE_ID = "bacheca_destinazione_id";

    /**
     * Costruttore. Inizializza la connessione al database e l'istanza di UtenteDAO.
     * @throws DatabaseConnectionException se la connessione fallisce
     */
    public ToDoPostgresDAOImpl() {
        try {
            conn = ConnessioneDatabase.getInstance().getConnection();
            this.utenteDAO = new UtentePostgresDAOImpl(); // Crea un'istanza di UtenteDAO
        } catch (SQLException e) {
            LOGGER.severe("Errore nel costruttore di ToDoPostgresDAOImpl: " + e.getMessage());
            throw new DatabaseConnectionException("Impossibile connettersi al database nel costruttore di ToDoPostgresDAOImpl", e);
        }
    }

    /**
     * Converte un oggetto Image in un array di byte PNG.
     * @param image Immagine da convertire
     * @return Array di byte rappresentante l'immagine
     * @throws IOException in caso di errore di conversione
     */
    private byte[] imageToBytes(Image image) throws IOException {
        if (image == null) return new byte[0];
        if (image instanceof BufferedImage bufferedImage) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos); // Salva come PNG
            return baos.toByteArray();
        } else {
            BufferedImage temp = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics2D bGr = temp.createGraphics();
            bGr.drawImage(image, 0, 0, null);
            bGr.dispose();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(temp, "png", baos); // Salva come PNG
            return baos.toByteArray();
        }
    }

    /**
     * Converte un array di byte in un oggetto Image.
     * @param bytes Array di byte dell'immagine
     * @return Oggetto Image, oppure null se vuoto
     * @throws IOException in caso di errore di conversione
     */
    private Image bytesToImage(byte[] bytes) throws IOException {
        if (bytes == null || bytes.length == 0) return null;
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        return ImageIO.read(bais);
    }

    /**
     * Cerca un Task tramite ID.
     * @param id ID del Task
     * @return Optional contenente il Task se trovato, altrimenti Optional vuoto
     * @throws DatabaseConnectionException in caso di errore SQL
     */
    @Override
    public Optional<ToDo> findById(int id) {
        if (conn == null) {
            LOGGER.severe("findById Task fallito: connessione DB non disponibile.");
            return Optional.empty();
        }
        String query = "SELECT id, bacheca_id, autore_id, titolo, descrizione, url, scadenza, immagine, posizione, stato, data_creazione, colore FROM todos WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ToDo todo = mapRowToToDo(rs);
                utenteDAO.findById(todo.getAutoreId()).ifPresent(todo::setAutore);
                todo.setUtentiConAccessoCondiviso(new HashSet<>(getUtentiCondivisione(todo)));
                return Optional.of(todo);
            }
        } catch (SQLException | IOException e) {
            LOGGER.severe("Errore findById Task: " + e.getMessage());
            throw new DatabaseConnectionException("Errore findById Task", e);
        }
        return Optional.empty();
    }

    /**
     * Restituisce tutti i Task associati a una bacheca.
     * @param bacheca Oggetto Bacheca
     * @return Lista di Task della bacheca
     */
    @Override
    public List<ToDo> findAllByBacheca(Bacheca bacheca) {
        if (bacheca == null || bacheca.getId() == 0) {
            return new ArrayList<>();
        }
        return findAllByBachecaId(bacheca.getId());
    }

    /**
     * Restituisce tutti i Task associati a una bacheca tramite ID.
     * @param bachecaId ID della bacheca
     * @return Lista di Task della bacheca
     * @throws DatabaseConnectionException in caso di errore SQL
     */
    @Override
    public List<ToDo> findAllByBachecaId(int bachecaId) {
        List<ToDo> todos = new ArrayList<>();
        if (conn == null) {
            LOGGER.severe("findAllByBachecaId Tasks fallito: connessione DB non disponibile.");
            return todos;
        }
        String query = "SELECT id, bacheca_id, autore_id, titolo, descrizione, url, scadenza, immagine, posizione, stato, data_creazione, colore FROM todos WHERE bacheca_id = ? ORDER BY posizione ASC, data_creazione DESC";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, bachecaId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ToDo todo = mapRowToToDo(rs);
                utenteDAO.findById(todo.getAutoreId()).ifPresent(todo::setAutore);
                todos.add(todo);
            }
        } catch (SQLException | IOException e) {
            LOGGER.severe("Errore findAllByBachecaId Tasks: " + e.getMessage());
            throw new DatabaseConnectionException("Errore findAllByBachecaId Tasks", e);
        }
        return todos;
    }

    /**
     * Restituisce tutti i Task creati da un autore.
     * @param autore Oggetto Utente autore
     * @return Lista di Task creati dall'autore
     */
    @Override
    public List<ToDo> findAllByAutore(Utente autore) {
        if (autore == null || autore.getId() == 0) {
            return new ArrayList<>();
        }
        return findAllByAutoreId(autore.getId());
    }

    /**
     * Restituisce tutti i Task creati da un autore tramite ID.
     * @param autoreId ID dell'autore
     * @return Lista di Task creati dall'autore
     * @throws DatabaseConnectionException in caso di errore SQL
     */
    @Override
    public List<ToDo> findAllByAutoreId(int autoreId) {
        List<ToDo> todos = new ArrayList<>();
        if (conn == null) {
            LOGGER.severe("findAllByAutoreId Tasks fallito: connessione DB non disponibile.");
            return todos;
        }
        String query = "SELECT id, bacheca_id, autore_id, titolo, descrizione, url, scadenza, immagine, posizione, stato, data_creazione, colore FROM todos WHERE autore_id = ? ORDER BY data_creazione DESC";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, autoreId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ToDo todo = mapRowToToDo(rs);
                utenteDAO.findById(todo.getAutoreId()).ifPresent(todo::setAutore);
                todo.setUtentiConAccessoCondiviso(new HashSet<>(getUtentiCondivisione(todo)));
                todos.add(todo);
            }
        } catch (SQLException | IOException e) {
            LOGGER.severe("Errore findAllByAutoreId Tasks: " + e.getMessage());
            throw new DatabaseConnectionException("Errore findAllByAutoreId Tasks", e);
        }
        return todos;
    }

    /**
     * Restituisce tutti i Task condivisi con un utente.
     * @param utente Utente destinatario della condivisione
     * @return Lista di Task condivisi con l'utente
     * @throws DatabaseConnectionException in caso di errore SQL
     */
    @Override
    public List<ToDo> findAllSharedWithUser(Utente utente) {
        List<ToDo> todosCondivisi = new ArrayList<>();
        if (conn == null || utente == null || utente.getId() == 0) {
            LOGGER.severe("findAllSharedWithUser Tasks fallito: connessione DB non disponibile o utente non valido.");
            return todosCondivisi;
        }
        String query = "SELECT t.id, t.bacheca_id, t.autore_id, t.titolo, t.descrizione, t.url, t.scadenza, t.immagine, t.posizione, t.stato, t.data_creazione, t.colore FROM todos t " +
                "JOIN todo_condivisioni tc ON t.id = tc.todo_id " +
                "WHERE tc.utente_id = ? " +
                "ORDER BY t.data_creazione DESC";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, utente.getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ToDo todo = mapRowToToDo(rs);
                utenteDAO.findById(todo.getAutoreId()).ifPresent(todo::setAutore);
                todosCondivisi.add(todo);
            }
        } catch (SQLException | IOException e) {
            LOGGER.severe("Errore findAllSharedWithUser Tasks: " + e.getMessage());
            throw new DatabaseConnectionException("Errore findAllSharedWithUser Tasks", e);
        }
        return todosCondivisi;
    }

    @Override
    public List<ToDo> findAllSharedWithUserAndDestination(Utente utente) {
        List<ToDo> todosCondivisi = new ArrayList<>();
        if (conn == null || utente == null || utente.getId() == 0) {
            LOGGER.severe("findAllSharedWithUserAndDestination Tasks fallito: connessione DB non disponibile o utente non valido.");
            return todosCondivisi;
        }

        // Prova prima con la nuova colonna bacheca_destinazione_id
        String query = "SELECT t.id, t.bacheca_id, t.autore_id, t.titolo, t.descrizione, t.url, t.scadenza, t.immagine, t.posizione, t.stato, t.data_creazione, t.colore, tc." + COLONNA_BACHECA_DESTINAZIONE_ID + " FROM todos t " +
                "JOIN todo_condivisioni tc ON t.id = tc.todo_id " +
                "WHERE tc.utente_id = ? " +
                "ORDER BY t.data_creazione DESC";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, utente.getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ToDo todo = mapRowToToDo(rs);
                Integer bachecaDestinazioneId = rs.getObject(COLONNA_BACHECA_DESTINAZIONE_ID, Integer.class);
                todo.setBachecaDestinazioneId(bachecaDestinazioneId);
                utenteDAO.findById(todo.getAutoreId()).ifPresent(todo::setAutore);
                todosCondivisi.add(todo);
            }
        } catch (SQLException e) {
            // Se la colonna bacheca_destinazione_id non esiste, usa la query vecchia
            if (e.getMessage().contains(COLONNA_BACHECA_DESTINAZIONE_ID)) {
                LOGGER.warning("Colonna " + COLONNA_BACHECA_DESTINAZIONE_ID + " non trovata, uso query legacy per findAllSharedWithUserAndDestination");
                return findAllSharedWithUser(utente); // Usa il metodo esistente
            } else {
                LOGGER.severe("Errore findAllSharedWithUserAndDestination Tasks: " + e.getMessage());
                throw new DatabaseConnectionException("Errore findAllSharedWithUserAndDestination Tasks", e);
            }
        } catch (IOException e) {
            LOGGER.severe("Errore findAllSharedWithUserAndDestination Tasks: " + e.getMessage());
            throw new DatabaseConnectionException("Errore findAllSharedWithUserAndDestination Tasks", e);
        }
        return todosCondivisi;
    }

    /**
     * Salva un nuovo Task nel database.
     * @param todo Oggetto Task da salvare
     * @return true se il salvataggio ha successo, false altrimenti
     * @throws DatabaseConnectionException in caso di errore SQL
     */
    @Override
    public boolean save(ToDo todo) {
        if (conn == null) {
            LOGGER.severe("save Task fallito: connessione DB non disponibile.");
            return false;
        }
        String query = "INSERT INTO todos (bacheca_id, autore_id, titolo, descrizione, url, scadenza, immagine, posizione, stato, data_creazione, colore) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            if (todo.getBachecaId() != null) {
                ps.setInt(1, todo.getBachecaId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setInt(2, todo.getAutoreId());
            ps.setString(3, todo.getTitolo());
            ps.setString(4, todo.getDescrizione());
            ps.setString(5, todo.getUrl());
            ps.setDate(6, todo.getScadenza() != null ? Date.valueOf(todo.getScadenza()) : null);

            byte[] imageBytes = imageToBytes(todo.getImmagine());
            if (imageBytes != null) {
                ps.setBytes(7, imageBytes);
            } else {
                ps.setNull(7, Types.BINARY);
            }

            ps.setInt(8, todo.getPosizione());
            ps.setString(9, todo.getStato().name());
            ps.setDate(10, Date.valueOf(todo.getCreazione()));
            ps.setString(11, todo.getColore());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                todo.setId(rs.getInt(1));
                return true;
            }
            return false;
        } catch (SQLException | IOException e) {
            LOGGER.severe("Errore save Task: " + e.getMessage());
            throw new DatabaseConnectionException("Errore save Task", e);
        }
    }

    /**
     * Aggiorna un Task esistente nel database.
     * @param todo Oggetto Task da aggiornare
     * @return true se l'aggiornamento ha successo, false altrimenti
     * @throws DatabaseConnectionException in caso di errore SQL
     */
    @Override
    public boolean update(ToDo todo) {
        if (conn == null) {
            LOGGER.severe("update Task fallito: connessione DB non disponibile.");
            return false;
        }
        if (todo.getId() == 0) {
            LOGGER.severe("Impossibile aggiornare Task senza ID.");
            return false;
        }
        String query = "UPDATE todos SET bacheca_id = ?, titolo = ?, descrizione = ?, url = ?, scadenza = ?, immagine = ?, " +
                "posizione = ?, stato = ?, colore = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            if (todo.getBachecaId() != null) {
                ps.setInt(1, todo.getBachecaId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setString(2, todo.getTitolo());
            ps.setString(3, todo.getDescrizione());
            ps.setString(4, todo.getUrl());
            ps.setDate(5, todo.getScadenza() != null ? Date.valueOf(todo.getScadenza()) : null);

            byte[] imageBytes = imageToBytes(todo.getImmagine());
            if (imageBytes != null) {
                ps.setBytes(6, imageBytes);
            } else {
                ps.setNull(6, Types.BINARY);
            }

            ps.setInt(7, todo.getPosizione());
            ps.setString(8, todo.getStato().name());
            ps.setString(9, todo.getColore());
            ps.setInt(10, todo.getId());

            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException | IOException e) {
            LOGGER.severe("Errore update Task: " + e.getMessage());
            throw new DatabaseConnectionException("Errore update Task", e);
        }
    }

    /**
     * Elimina un Task tramite ID.
     * @param id ID del Task da eliminare
     * @return true se l'eliminazione ha successo, false altrimenti
     * @throws DatabaseConnectionException in caso di errore SQL
     */
    @Override
    public boolean delete(int id) {
        if (conn == null) {
            LOGGER.severe("delete Task fallito: connessione DB non disponibile.");
            return false;
        }
        String query = "DELETE FROM todos WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.severe("Errore delete Task by ID: " + e.getMessage());
            throw new DatabaseConnectionException("Errore delete Task by ID", e);
        }
    }

    /**
     * Aggiunge una condivisione di un Task con un altro utente.
     * @param todo Task da condividere
     * @param utenteConCuiCondividere Utente destinatario
     * @param bachecaDestinazioneId ID della bacheca di destinazione
     * @return true se la condivisione ha successo, false altrimenti
     * @throws DatabaseConnectionException in caso di errore SQL
     */
    @Override
    public boolean addCondivisione(ToDo todo, Utente utenteConCuiCondividere, Integer bachecaDestinazioneId) {
        if (conn == null) {
            LOGGER.severe("addCondivisione Task fallito: connessione DB non disponibile.");
            return false;
        }
        if (todo.getId() == 0 || utenteConCuiCondividere.getId() == 0) {
            LOGGER.severe("addCondivisione Task fallito: Task o Utente non persistiti.");
            return false;
        }

        // Prova prima con la nuova colonna bacheca_destinazione_id
        String query = "INSERT INTO todo_condivisioni (todo_id, utente_id, bacheca_destinazione_id) VALUES (?, ?, ?) ON CONFLICT (todo_id, utente_id) DO UPDATE SET bacheca_destinazione_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, todo.getId());
            ps.setInt(2, utenteConCuiCondividere.getId());
            if (bachecaDestinazioneId != null) {
                ps.setInt(3, bachecaDestinazioneId);
                ps.setInt(4, bachecaDestinazioneId);
            } else {
                ps.setNull(3, Types.INTEGER);
                ps.setNull(4, Types.INTEGER);
            }
            int affectedRows = ps.executeUpdate();
            return affectedRows >= 0;
        } catch (SQLException e) {
            // Se la colonna bacheca_destinazione_id non esiste, usa la query vecchia
            if (e.getMessage().contains(COLONNA_BACHECA_DESTINAZIONE_ID)) {
                LOGGER.warning("Colonna " + COLONNA_BACHECA_DESTINAZIONE_ID + " non trovata, uso query legacy per addCondivisione");
                String legacyQuery = "INSERT INTO todo_condivisioni (todo_id, utente_id) VALUES (?, ?) ON CONFLICT (todo_id, utente_id) DO NOTHING";
                try (PreparedStatement ps = conn.prepareStatement(legacyQuery)) {
                    ps.setInt(1, todo.getId());
                    ps.setInt(2, utenteConCuiCondividere.getId());
                    int affectedRows = ps.executeUpdate();
                    return affectedRows >= 0;
                } catch (SQLException e2) {
                    LOGGER.severe("Errore addCondivisione Task (legacy): " + e2.getMessage());
                    throw new DatabaseConnectionException("Errore addCondivisione Task (legacy)", e2);
                }
            } else {
                LOGGER.severe("Errore addCondivisione Task: " + e.getMessage());
                throw new DatabaseConnectionException("Errore addCondivisione Task", e);
            }
        }
    }

    /**
     * Rimuove la condivisione di un Task per un utente.
     * @param todo Task condiviso
     * @param utenteDaRimuovere Utente da rimuovere dalla condivisione
     * @return true se la rimozione ha successo, false altrimenti
     * @throws DatabaseConnectionException in caso di errore SQL
     */
    @Override
    public boolean removeCondivisione(ToDo todo, Utente utenteDaRimuovere) {
        if (conn == null) {
            LOGGER.severe("removeCondivisione Task fallito: connessione DB non disponibile.");
            return false;
        }
        if (todo.getId() == 0 || utenteDaRimuovere.getId() == 0) {
            LOGGER.severe("removeCondivisione Task fallito: Task o Utente non persistiti.");
            return false;
        }
        String query = "DELETE FROM todo_condivisioni WHERE todo_id = ? AND utente_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, todo.getId());
            ps.setInt(2, utenteDaRimuovere.getId());
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.severe("Errore removeCondivisione Task: " + e.getMessage());
            throw new DatabaseConnectionException("Errore removeCondivisione Task", e);
        }
    }

    /**
     * Restituisce la lista degli utenti con cui un Task è condiviso.
     * @param todo Task di interesse
     * @return Lista di utenti con cui il Task è condiviso
     * @throws DatabaseConnectionException in caso di errore SQL
     */
    @Override
    public List<Utente> getUtentiCondivisione(ToDo todo) {
        List<Utente> utenti = new ArrayList<>();
        if (conn == null || todo.getId() == 0) {
            LOGGER.severe("getUtentiCondivisione fallito: connessione DB non disponibile o Task non valido.");
            return utenti;
        }
        String query = "SELECT u.id, u.username, u.password_hash FROM utenti u " +
                "JOIN todo_condivisioni tc ON u.id = tc.utente_id " +
                "WHERE tc.todo_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, todo.getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                utenti.add(new Utente(rs.getInt("id"), rs.getString("username"), rs.getString("password_hash")));
            }
        } catch (SQLException e) {
            LOGGER.severe("Errore getUtentiCondivisione: " + e.getMessage());
            throw new DatabaseConnectionException("Errore getUtentiCondivisione", e);
        }
        return utenti;
    }

    /**
     * Mappa una riga del ResultSet a un oggetto Task.
     * @param rs ResultSet posizionato sulla riga da mappare
     * @return Oggetto Task corrispondente
     * @throws SQLException in caso di errore di accesso ai dati
     * @throws IOException in caso di errore di conversione immagine
     */
    private ToDo mapRowToToDo(ResultSet rs) throws SQLException, IOException {
        int id = rs.getInt("id");
        Integer bachecaId = rs.getObject("bacheca_id", Integer.class);
        int autoreId = rs.getInt("autore_id");
        String titolo = rs.getString("titolo");
        String descrizione = rs.getString("descrizione");
        String url = rs.getString("url");
        Date scadenzaSql = rs.getDate("scadenza");
        LocalDate scadenza = (scadenzaSql != null) ? scadenzaSql.toLocalDate() : null;

        byte[] imageBytes = rs.getBytes("immagine");
        Image immagine = bytesToImage(imageBytes);

        int posizione = rs.getInt("posizione");
        StatoToDo stato = StatoToDo.valueOf(rs.getString("stato"));
        LocalDate creazione = rs.getDate("data_creazione").toLocalDate();
        String colore = rs.getString("colore");

        return new ToDo(id, bachecaId, autoreId, titolo, descrizione, url, scadenza, immagine, posizione, stato, creazione, colore);
    }
}