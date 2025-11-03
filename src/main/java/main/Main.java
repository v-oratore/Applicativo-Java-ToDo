package main;

import gui.MainFrame;
import controller.Controller;
import javax.swing.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe di avvio dell'applicazione Task.
 * Inizializza il controller, la finestra principale e gestisce eventuali errori critici all'avvio.
 */
public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    /**
     * Metodo main. Punto di ingresso dell'applicazione.
     * Avvia l'interfaccia grafica Swing e gestisce le eccezioni critiche.
     * @param args Argomenti da linea di comando (non usati)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                Controller controller = new Controller(); // La connessione al DB avviene qui
                MainFrame mainFrame = new MainFrame(controller);
                mainFrame.setVisible(true);
            } catch (Exception e) {
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.log(Level.SEVERE, String.format("Errore fatale all'avvio dell'applicazione: %s", e.getMessage()), e);
                }
                JOptionPane.showMessageDialog(null, "Errore all'avvio dell'applicazione. Controlla la connessione al database.", "Errore Fatale", JOptionPane.ERROR_MESSAGE);
                System.exit(1); // Termina l'applicazione in caso di errore critico all'avvio
            }
        });
    }
}