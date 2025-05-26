package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Bacheca {
    private TitoloBacheca titolo; // Tipo Enum come da UML
    private String descrizione;
    private List<ToDo> todos;

    public Bacheca(TitoloBacheca titolo, String descrizione) {
        this.titolo = Objects.requireNonNull(titolo, "Il titolo della bacheca non può essere nullo.");
        this.descrizione = descrizione; // Può essere nulla o vuota
        this.todos = new ArrayList<>();
    }

    public TitoloBacheca getTitoloEnum() { // Rinominato per chiarezza
        return titolo;
    }

    public String getTitoloDisplayName() {
        return titolo.getDisplayName();
    }

    // Non permettiamo di cambiare il TitoloBacheca una volta creato, è la sua identità.
    // public void setTitoloEnum(TitoloBacheca titolo) { this.titolo = titolo; }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public List<ToDo> getTodos() {
        return Collections.unmodifiableList(todos); // Restituisce una vista non modificabile
    }

    /**
     * Aggiunge un ToDo a questa bacheca.
     * @param todo Il ToDo da aggiungere.
     * @return true se il ToDo è stato aggiunto, false se era già presente.
     */
    public boolean aggiungiToDo(ToDo todo) {
        Objects.requireNonNull(todo, "Il ToDo da aggiungere non può essere nullo.");
        if (!this.todos.contains(todo)) {
            this.todos.add(todo);
            // Qui si potrebbe impostare la posizione del ToDo se necessario
            // todo.setPosizione(this.todos.size());
            System.out.println("ToDo '" + todo.getTitolo() + "' aggiunto alla bacheca '" + getTitoloDisplayName() + "'.");
            return true;
        }
        System.out.println("ToDo '" + todo.getTitolo() + "' è già presente nella bacheca '" + getTitoloDisplayName() + "'.");
        return false;
    }

    /**
     * Rimuove un ToDo da questa bacheca.
     * @param todo Il ToDo da rimuovere.
     * @return true se il ToDo è stato rimosso, false altrimenti.
     */
    public boolean eliminaToDo(ToDo todo) {
        Objects.requireNonNull(todo, "Il ToDo da eliminare non può essere nullo.");
        boolean removed = this.todos.remove(todo);
        if (removed) {
            System.out.println("ToDo '" + todo.getTitolo() + "' rimosso dalla bacheca '" + getTitoloDisplayName() + "'.");
        }
        return removed;
    }

    /**
     * Modifica l'ordine dei ToDo. Implementazione di esempio, sposta un ToDo a una nuova posizione.
     * @param todo Lo ToDo da spostare.
     * @param nuovaPosizione La nuova posizione (0-indexed).
     * @return true se lo spostamento ha avuto successo.
     */
    public boolean modificaOrdineToDo(ToDo todo, int nuovaPosizione) {
        if (todo == null || !todos.contains(todo) || nuovaPosizione < 0 || nuovaPosizione >= todos.size()) {
            return false;
        }
        todos.remove(todo);
        todos.add(nuovaPosizione, todo);
        // Aggiorna le posizioni di tutti i ToDo per consistenza
        for (int i = 0; i < todos.size(); i++) {
            todos.get(i).setPosizione(i);
        }
        return true;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bacheca bacheca = (Bacheca) o;
        return titolo == bacheca.titolo; // Le bacheche sono uniche per titolo (enum) per un dato utente
    }

    @Override
    public int hashCode() {
        return Objects.hash(titolo);
    }

    @Override
    public String toString() {
        return "Bacheca: " + titolo.getDisplayName() + (descrizione != null && !descrizione.isEmpty() ? " (" + descrizione + ")" : "");
    }
}