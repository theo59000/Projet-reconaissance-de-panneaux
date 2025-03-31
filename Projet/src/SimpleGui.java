import javax.swing.*; // Importe les classes Swing pour l'interface graphique
import java.awt.*; // Importe des classes AWT (utilisées par Swing)

/**
 * Une classe simple pour démontrer une interface graphique basique en Java Swing.
 */
public class SimpleGui {

    /**
     * La méthode principale (point d'entrée de l'application).
     * @param args Arguments de la ligne de commande (non utilisés ici).
     */
    public static void main(String[] args) {
        // Il est recommandé de créer et de manipuler les composants Swing
        // sur le 'Event Dispatch Thread' (EDT) pour éviter les problèmes de concurrence.
        // SwingUtilities.invokeLater exécute le code fourni sur l'EDT.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI(); // Appelle notre méthode pour créer l'interface
            }
        });
    }

    /**
     * Crée et affiche l'interface graphique.
     */
    private static void createAndShowGUI() {
        // 1. Créer la fenêtre principale (le cadre)
        // JFrame est la fenêtre de niveau supérieur.
        JFrame frame = new JFrame("Ma Première Interface Graphique"); // Définit le titre de la fenêtre

        // 2. Définir l'opération de fermeture par défaut
        // EXIT_ON_CLOSE termine l'application lorsque l'utilisateur ferme la fenêtre.
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 3. Définir la taille de la fenêtre (en pixels)
        frame.setSize(400, 300); // Largeur = 400, Hauteur = 300

        // 4. Centrer la fenêtre sur l'écran (optionnel mais pratique)
        frame.setLocationRelativeTo(null);

        // 5. Créer un panneau (JPanel)
        // JPanel est un conteneur léger pour organiser les composants.
        JPanel panel = new JPanel();

        // 6. Créer un composant (par exemple, une étiquette de texte)
        // JLabel affiche une ligne de texte (ou une image).
        JLabel label = new JLabel("Bonjour le monde de Swing !");

        // 7. Ajouter le composant au panneau
        panel.add(label);

        // 8. Ajouter le panneau au "content pane" de la fenêtre
        // Le content pane est la zone principale où les composants sont placés.
        frame.getContentPane().add(panel);

        // 9. Rendre la fenêtre visible
        // Important : sans cela, la fenêtre existe mais n'est pas affichée.
        frame.setVisible(true);
    }
}
