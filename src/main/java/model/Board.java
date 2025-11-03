package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Modello semplice che rappresenta una board di Task.
 * Contiene un titolo e una lista di Task associati.
 */
public class Board {
    private String titolo;
    private final List<ToDo> todos;

    /**
     * Costruttore. Crea una board con il titolo specificato.
     * @param titolo Titolo della board
     */
    public Board(String titolo) {
        this.titolo = titolo;
        this.todos = new ArrayList<>();
    }

    /**
     * Restituisce il titolo della board.
     * @return Titolo
     */
    public String getTitolo() {
        return titolo;
    }

    /**
     * Restituisce la lista dei Task associati alla board.
     * @return Lista di Task
     */
    public List<ToDo> getTodos() {
        return todos;
    }

}