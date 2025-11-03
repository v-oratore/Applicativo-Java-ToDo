package gui;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * Utility per la gestione e il caricamento dei font personalizzati dell'applicazione.
 * Fornisce metodi statici per ottenere font regular e bold Poppins.
 */
public class FontManager {

    private static final Logger LOGGER = Logger.getLogger(FontManager.class.getName());
    private static Font poppinsRegular;
    private static Font poppinsBold;

    /**
     * Costruttore privato per evitare l'istanziazione della classe utility.
     */
    private FontManager() {
        // Prevent instantiation
    }

    /**
     * Blocco statico di inizializzazione: carica e registra i font Poppins.
     * Se il caricamento fallisce, usa un font di default.
     */
    static {
        try {
            InputStream regularStream = FontManager.class.getResourceAsStream("/fonts/Poppins-Regular.ttf");
            InputStream boldStream = FontManager.class.getResourceAsStream("/fonts/Poppins-Bold.ttf");

            if (regularStream == null || boldStream == null) {
                throw new IOException("File del font Poppins non trovati in /resources/fonts/");
            }

            Font poppinsRegularBase = Font.createFont(Font.TRUETYPE_FONT, regularStream);
            Font poppinsBoldBase = Font.createFont(Font.TRUETYPE_FONT, boldStream);

            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(poppinsRegularBase);
            ge.registerFont(poppinsBoldBase);

            poppinsRegular = poppinsRegularBase;
            poppinsBold = poppinsBoldBase;

        } catch (IOException | FontFormatException e) {
            LOGGER.severe("Errore durante il caricamento dei font personalizzati. Verrà usato un font di default.");
            e.printStackTrace();
            poppinsRegular = new Font("SansSerif", Font.PLAIN, 14);
            poppinsBold = new Font("SansSerif", Font.BOLD, 14);
        }
    }

    /**
     * Restituisce il font Poppins Regular della dimensione desiderata.
     * @param size Dimensione del font
     * @return Font regular Poppins
     */
    public static Font getRegular(float size) {
        return poppinsRegular.deriveFont(size);
    }

    /**
     * Restituisce il font Poppins Bold della dimensione desiderata.
     * @param size Dimensione del font
     * @return Font bold Poppins
     */
    public static Font getBold(float size) {
        return poppinsBold.deriveFont(Font.BOLD, size);
    }
}