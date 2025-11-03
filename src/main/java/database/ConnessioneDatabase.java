package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton per la gestione della connessione al database PostgreSQL.
 * Garantisce una sola connessione condivisa per tutta l'applicazione.
 */
@SuppressWarnings("java:S6548")
public class ConnessioneDatabase {
    private static final Logger logger = LoggerFactory.getLogger(ConnessioneDatabase.class);

    private static final String ENV_DB_URL = "jdbc:postgresql://localhost:5432/todo_app_db";
    private static final String ENV_DB_USER = "postgres";
    private static final String ENV_DB_PASSWORD = "30112003V@Mac";

    // Le credenziali sono ora lette da variabili d'ambiente per sicurezza
    private static final String DB_URL = System.getenv(ENV_DB_URL) != null && !System.getenv(ENV_DB_URL).isBlank() ? System.getenv(ENV_DB_URL) : ENV_DB_URL;
    private static final String DB_USER = System.getenv(ENV_DB_USER) != null && !System.getenv(ENV_DB_USER).isBlank() ? System.getenv(ENV_DB_USER) : ENV_DB_USER;
    private static final String DB_PASSWORD = System.getenv(ENV_DB_PASSWORD) != null && !System.getenv(ENV_DB_PASSWORD).isBlank() ? System.getenv(ENV_DB_PASSWORD) : ENV_DB_PASSWORD;

    // Singleton pattern: garantisce una sola connessione condivisa per tutta
    // l'applicazione
    private static ConnessioneDatabase instance;
    private Connection connection;

    /**
     * Costruttore privato. Inizializza la connessione al database.
     * @throws SQLException se la connessione fallisce o le variabili d'ambiente non sono valorizzate
     */
    private ConnessioneDatabase() throws SQLException {
        try {
            // Controllo che le variabili d'ambiente siano valorizzate
            if (DB_URL == null || DB_URL.isBlank() || DB_USER == null || DB_USER.isBlank() || DB_PASSWORD == null || DB_PASSWORD.isBlank()) {
                String msg = "Le variabili d'ambiente DB_URL, DB_USER e DB_PASSWORD devono essere valorizzate. " +
                        "Valori attuali: DB_URL=" + DB_URL + ", DB_USER=" + DB_USER + ", DB_PASSWORD=" + (DB_PASSWORD != null ? "***" : null);
                logger.error(msg);
                throw new SQLException(msg);
            }
            // Class.forName("org.postgresql.Driver"); // NON SERVE più con JDBC 4.0+
            this.connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            logger.info("Connessione al database PostgreSQL stabilita con successo.");
        } catch (SQLException e) {
            String msg = String.format(
                    "Errore durante la connessione al database: %s [SQLState: %s, ErrorCode: %d]",
                    e.getMessage(), e.getSQLState(), e.getErrorCode());
            throw new SQLException(msg, e);
        }
    }

    /**
     * Restituisce la connessione JDBC attiva.
     * @return Oggetto Connection attivo
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Restituisce l'istanza singleton di ConnessioneDatabase.
     * Se la connessione è chiusa o non esiste, ne crea una nuova.
     * @return Istanza singleton di ConnessioneDatabase
     * @throws SQLException se la connessione fallisce
     */
    public static ConnessioneDatabase getInstance() throws SQLException {
        // Verifica se l'istanza è null O se la connessione è chiusa
        if (instance == null || instance.getConnection().isClosed()) {
            synchronized (ConnessioneDatabase.class) {
                // Double-check per la sicurezza del thread
                if (instance == null || instance.getConnection().isClosed()) {
                    instance = new ConnessioneDatabase();
                }
            }
        }
        return instance;
    }

    /**
     * Chiude la connessione al database, se attiva.
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                logger.info("Connessione al database chiusa.");
            } catch (SQLException e) {
                logger.error("Errore durante la chiusura della connessione", e);
            }
        }
    }
}