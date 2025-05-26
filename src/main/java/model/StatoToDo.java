package model;

public enum StatoToDo {
    COMPLETATO("Completato"),
    NON_COMPLETATO("Non Completato");

    private final String displayName;

    StatoToDo(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}