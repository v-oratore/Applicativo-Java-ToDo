package model;

/**
 * Enum che rappresenta i titoli possibili per una bacheca (Università, Lavoro, Tempo libero).
 * Ogni titolo ha un nome visualizzato associato.
 */
public enum TitoloBacheca {
    /** Titolo per bacheca universitaria. */
    UNIVERSITA("Università"),
    /** Titolo per bacheca lavorativa. */
    LAVORO("Lavoro"),
    /** Titolo per bacheca del tempo libero. */
    TEMPOLIBERO("Tempo libero");

    private final String displayName;

    /**
     * Costruttore dell'enum.
     * @param displayName Nome visualizzato del titolo
     */
    TitoloBacheca(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Restituisce il nome visualizzato del titolo.
     * @return Nome visualizzato
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Restituisce il nome visualizzato del titolo (override di toString).
     * @return Nome visualizzato
     */
    @Override
    public String toString() {
        return displayName;
    }

    /**
     * Restituisce il TitoloBacheca corrispondente al displayName o nome enum fornito.
     * @param displayName Nome visualizzato o nome enum
     * @return TitoloBacheca corrispondente
     * @throws IllegalArgumentException se non esiste corrispondenza
     */
    public static TitoloBacheca fromDisplayName(String displayName) {
        if (displayName == null) {
            throw new IllegalArgumentException("Il displayName non può essere nullo.");
        }
        for (TitoloBacheca b : TitoloBacheca.values()) {
            if (b.getDisplayName().equalsIgnoreCase(displayName)) {
                return b;
            }
        }
        // Fallback attempt for direct enum name match if display name fails (e.g. "UNIVERSITA")
        try {
            return TitoloBacheca.valueOf(displayName.toUpperCase());
        } catch (IllegalArgumentException _) {
            throw new IllegalArgumentException("Nessun TitoloBacheca corrisponde al displayName o nome: " + displayName);
        }
    }

    /**
     * Verifica se il nome fornito è un displayName valido o un nome enum valido.
     * @param name Nome da verificare
     * @return true se valido, false altrimenti
     */
    public static boolean isValidDisplayName(String name) {
        if (name == null) return false;
        for (TitoloBacheca b : TitoloBacheca.values()) {
            if (b.getDisplayName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        // Check also for direct enum name match
        try {
            TitoloBacheca.valueOf(name.toUpperCase());
            return true;
        } catch (IllegalArgumentException _) {
            return false;
        }
    }
}