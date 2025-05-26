import gui.MainFrame;
import controller.Controller;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Controller controller = new Controller(); // Crea direttamente il Controller
            MainFrame mainFrame = new MainFrame(controller); // Passa solo il controller
            mainFrame.setVisible(true);
        });
    }
}