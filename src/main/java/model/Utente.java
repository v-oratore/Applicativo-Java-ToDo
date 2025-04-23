package model;
import java.util.ArrayList;
public class Utente {
    private  String login;
    private String password;
    private ArrayList<ToDo> ToDo = new ArrayList<>();
    private ArrayList<Bacheca> Bacheche = new ArrayList<>();

    public Utente(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public void setLogin(String login) {
        this.login = login;
    }

    public ArrayList<Bacheca> getBacheche() {
        return Bacheche;
    }

    //prototipi
    public boolean login(){ return true;};
    public void logout() {};
    public void aggiungiBacheca() {};
    public void modificaBacheca() {};
    public void eliminaBacheca() {};
    public void modificaOrdine() {};
    public void creaTodo() {};
    public void modificaTodo() {};
    public void eliminaTodo() {};
    public void spostaTodo() {};
    public void cambiaBacheca() {};
    public void condividiTodo() {};
    public void revocaCondivisione() {};
    public void ricercaTodo() {};
    public void todoInScadenza() {};

}
