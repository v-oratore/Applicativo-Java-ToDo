package model;
import java.util.ArrayList;
public abstract class Utente {
    private  String login;
    private String password;
    private ArrayList<ToDo> ToDo = new ArrayList<>();


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

    //prototipi
    public abstract boolean login();
    public abstract void logout();
    public abstract void aggiungiBacheca();
    public abstract void modificaBacheca();
    public abstract void eliminaBacheca();
    public abstract void modificaOrdine();
    public abstract void creaTodo();
    public abstract void modificaTodo();
    public abstract void eliminaTodo();
    public abstract void spostaTodo();
    public abstract void cambiaBacheca();
    public abstract void condividiTodo();
    public abstract void revocaCondivisione();
    public abstract void ricercaTodo();
    public abstract void todoInScadenza();

}
