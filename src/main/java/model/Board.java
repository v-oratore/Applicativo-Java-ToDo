package model;

import java.util.ArrayList;
import java.util.List;

public class Board {
    private String titolo;
    private final List<ToDo> todos;

    public Board(String titolo) {
        this.titolo = titolo;
        this.todos = new ArrayList<>();
    }

    public String getTitolo() {
        return titolo;
    }

    public List<ToDo> getTodos() {
        return todos;
    }

}