package model;
import java.util.ArrayList;
public abstract class Bacheca {
    private TitoloBacheca Titolo;
    private String Descrizione;
    private ArrayList<ToDo> ToDo = new ArrayList<>();
    public Bacheca(TitoloBacheca titolo, String descrizione) {
        this.Titolo = titolo;
        this.Descrizione = descrizione;
    }
    public TitoloBacheca getTitolo() {
        return Titolo;
    }
    public String getDescrizione() {
        return Descrizione;
    }
    public void setDescrizione(String descrizione) {
        Descrizione = descrizione;
    }
    public void setTitolo(TitoloBacheca titolo) {
        Titolo = titolo;
    }
    public ArrayList<ToDo> getTodo(){
        return ToDo;
    }

    //
    public abstract void aggiungiTodo();
}
