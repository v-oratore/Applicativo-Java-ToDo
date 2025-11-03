package model;

/**
 * Enum che rappresenta lo stato di un Task (completato o non completato).
 * Ogni stato ha un nome visualizzato associato.
 */
public enum StatoToDo {
    /** Stato che indica che il Task è stato completato. */
    COMPLETATO("Completato"),
    /** Stato che indica che il Task non è ancora stato completato. */
    NON_COMPLETATO("Non completato");

    private final String displayName;

    /**
     * Costruttore dell'enum.
     * @param displayName Nome visualizzato dello stato
     */
    StatoToDo(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Restituisce il nome visualizzato dello stato.
     * @return Nome visualizzato
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Restituisce il nome visualizzato dello stato (override di toString).
     * @return Nome visualizzato
     */
    @Override
    public String toString() {
        return displayName;
    }
}