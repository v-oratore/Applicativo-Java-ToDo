import model.Utente;
import model.Bacheca;
import model.ToDo;

public class Main {
    public static void main(String[] args){
        Utente u = new Utente("", "");
        System.out.println(u.getLogin() + " " + u.getPassword());
    }
}