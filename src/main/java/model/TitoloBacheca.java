package model;

public enum TitoloBacheca {
    UNIVERSITA("Università"),
    LAVORO("Lavoro"),
    TEMPOLIBERO("TempoLibero");

    private final String displayName;

    TitoloBacheca(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

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
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Nessun TitoloBacheca corrisponde al displayName o nome: " + displayName);
        }
    }

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
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}