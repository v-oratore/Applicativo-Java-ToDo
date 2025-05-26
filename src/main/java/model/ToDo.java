package model;

import java.awt.Image; // Per l'attributo immagine come da UML
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

public class ToDo {
    private String titolo;
    private String url;
    private String descrizione;
    private LocalDate scadenza;
    private transient Image immagine; // transient se pensassimo alla serializzazione, per in-memory non è strettamente necessario
    private int posizione;
    private StatoToDo stato;
    private LocalDate creazione;
    private String colore;
    private Utente autore; // Come da UML

    // Per la funzionalità di condivisione, non esplicitata come attributo diretto nell'UML del ToDo
    // ma implicata dalla relazione "condivisione" e metodi in Utente.
    private Set<Utente> utentiConAccessoCondiviso = new HashSet<>();


    public ToDo(String titolo, String descrizione, Utente autore, LocalDate scadenza, String colore) {
        this.titolo = Objects.requireNonNull(titolo, "Il titolo non può essere nullo");
        this.descrizione = Objects.requireNonNull(descrizione, "La descrizione non può essere nulla");
        this.autore = Objects.requireNonNull(autore, "L'autore non può essere nullo");
        this.scadenza = scadenza; // Può essere nullo se non specificato
        this.colore = colore;
        this.creazione = LocalDate.now();
        this.stato = StatoToDo.NON_COMPLETATO;
        this.url = "";
        this.immagine = null; // Default a null
        this.posizione = 0; // Default
    }

    // Getters e Setters (omessi per brevità, ma necessari per tutti i campi)

    public String getTitolo() { return titolo; }
    public void setTitolo(String titolo) { this.titolo = titolo; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }

    public LocalDate getScadenza() { return scadenza; }
    public void setScadenza(LocalDate scadenza) { this.scadenza = scadenza; }

    public Image getImmagine() { return immagine; }
    public void setImmagine(Image immagine) { this.immagine = immagine; }

    public int getPosizione() { return posizione; }
    public void setPosizione(int posizione) { this.posizione = posizione; }

    public StatoToDo getStato() { return stato; }
    public void setStato(StatoToDo stato) { this.stato = stato; }

    public LocalDate getCreazione() { return creazione; }
    public void setCreazione(LocalDate creazione) { this.creazione = creazione; } // Generalmente non si cambia

    public String getColore() { return colore; }
    public void setColore(String colore) { this.colore = colore; }

    public Utente getAutore() { return autore; }
    public void setAutore(Utente autore) { this.autore = autore; } // L'autore di solito non cambia dopo la creazione

    public Set<Utente> getUtentiConAccessoCondiviso() {
        return utentiConAccessoCondiviso;
    }

    public void aggiungiUtenteCondiviso(Utente utente) {
        this.utentiConAccessoCondiviso.add(utente);
    }

    public void rimuoviUtenteCondiviso(Utente utente) {
        this.utentiConAccessoCondiviso.remove(utente);
    }

    // Per facilitare la visualizzazione negli JComboBox, ad esempio
    @Override
    public String toString() {
        return titolo + (scadenza != null ? " (Scade: " + scadenza.toString() + ")" : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ToDo toDo = (ToDo) o;
        // Un ToDo è univoco per titolo, data creazione e autore per semplicità.
        // O usare un ID univoco se disponibile.
        return titolo.equals(toDo.titolo) &&
                creazione.equals(toDo.creazione) &&
                autore.equals(toDo.autore) &&
                Objects.equals(url, toDo.url) &&
                Objects.equals(descrizione, toDo.descrizione);
    }

    @Override
    public int hashCode() {
        return Objects.hash(titolo, creazione, autore, url, descrizione);
    }
}