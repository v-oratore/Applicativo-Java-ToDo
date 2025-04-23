package model;
import java.time.LocalDate;
import java.util.Date;
import java.util.ArrayList;
public class ToDo {
    private ArrayList<Utente> Utenti = new ArrayList<>();
    private String Titolo;
    private String Url;
    private String Descrizione;
    private LocalDate Scadenza;
    private String Immagine;
    private int Posizione;
    private StatoToDo Stato;
    private LocalDate Creazione;
    private String Colore;
    private Utente Autore;
    public ToDo(String titolo, String descrizione, LocalDate scadenza, String colore) {
        this.Titolo = titolo;
        this.Descrizione = descrizione;
        this.Scadenza = scadenza;
        this.Colore = colore;
        this.Stato = StatoToDo.NON_COMPLETATO;
        this.Creazione = LocalDate.now();
    }
    public String getColore() {
        return Colore;
    }
    public void setColore(String colore) {
        this.Colore = colore;
    }
    public StatoToDo getStato() {
        return Stato;
    }
    public void setStato(StatoToDo stato) {
        this.Stato = stato;
    }
    public LocalDate getScadenza() {
        return Scadenza;
    }
    public void setScadenza(LocalDate scadenza) {
        this.Scadenza = scadenza;
    }
    public String getImmagine() {
        return Immagine;
    }
    public void setImmagine(String immagine) {
        this.Immagine = immagine;
    }
    public int getPosizione() {
        return Posizione;
    }
    public void setPosizione(int posizione) {
        Posizione = posizione;
    }
    public LocalDate getCreazione() {
        return Creazione;
    }
    public void setCreazione(LocalDate creazione) {
        Creazione = creazione;
    }
    public String getTitolo() {
        return Titolo;
    }
    public void setTitolo(String titolo) {
        Titolo = titolo;
    }
    public String getUrl() {
        return Url;
    }
    public void setUrl(String url) {
        Url = url;
    }
    public String getDescrizione() {
        return Descrizione;
    }
    public void setDescrizione(String descrizione) {
        Descrizione = descrizione;
    }
    public Utente getAutore() {
        return Autore;
    }
    public void setAutore(Utente autore) {
        Autore = autore;
    }
    public ArrayList<Utente> getUtenti() {
        return Utenti;
    }
}
