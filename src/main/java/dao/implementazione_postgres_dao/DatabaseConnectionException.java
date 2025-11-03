package dao.implementazione_postgres_dao;

/**
 * Eccezione personalizzata per errori di connessione al database.
 * Viene lanciata quando si verifica un problema durante la connessione o l'accesso al database.
 */
public class DatabaseConnectionException extends RuntimeException {
    /**
     * Costruttore dell'eccezione.
     * @param message Messaggio descrittivo dell'errore
     * @param cause Eccezione causa originale
     */
    public DatabaseConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
} 