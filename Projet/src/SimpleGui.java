import javax.swing.*; // Importe les classes Swing pour l'interface graphique
import java.awt.*; // Importe des classes AWT (utilisées par Swing)
import java.awt.event.*;


public class SimpleGui {


    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI(); // Appelle notre méthode pour créer l'interface
            }
        });
    }

    // Création et affichage l'interface graphique.
     
    private static void createAndShowGUI() {
        // Création de la fenêtre principale
        JFrame frame = new JFrame("Groupe : Les 4 Fantastiques");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 600);
        frame.setLayout(null); 

        // Chargement de l'image
        ImageIcon icon = new ImageIcon("Projet\\Images_panneaux\\30.jpg");

        // Modification de la taille de l'image proportionnellement par rapport à sa taille d'origine
        double facteur = 0.4; // Facteur de proportionnalité
        int l = set_dimension(icon.getIconWidth(),facteur);
        int L = set_dimension(icon.getIconHeight(), facteur);
        Image image = icon.getImage().getScaledInstance(l, L, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(image);
        JLabel label = new JLabel(scaledIcon);
        

        // Positionnement manuel (x, y, largeur, hauteur)
        int x = 60;
        int y = 30;
        int set_x = x-set_dimension(icon.getIconWidth(), 1-facteur)/2;
        int set_y = y-set_dimension(icon.getIconHeight(), 1-facteur)/2;
        label.setBounds(set_x, set_y, icon.getIconWidth(), icon.getIconHeight());
        frame.add(label);

        // Crée un bouton
        JButton bouton = new JButton("Détecter les panneaux de signalisation");


        // Ajoute une action au clic
        bouton.addActionListener(e -> {
            // Charger l'image
            ImageIcon resultat = new ImageIcon("Projet\\Images_panneaux\\panneau_30.jpg");
            // Dimensionner l'image
            int l_res = set_dimension(resultat.getIconWidth(), 0.8);
            int L_res = set_dimension(resultat.getIconHeight(), 0.8);
            Image image_res = resultat.getImage().getScaledInstance(l_res, L_res, Image.SCALE_SMOOTH);
            ImageIcon scaledIcon_res = new ImageIcon(image_res);
            JLabel label_res = new JLabel(scaledIcon_res);
            label_res.setBounds(x+set_dimension(icon.getIconWidth(), facteur),y+set_dimension(icon.getIconHeight(), facteur)/2-resultat.getIconHeight()/2,resultat.getIconWidth(),resultat.getIconHeight());
            // Ajouter l'image
            frame.add(label_res);
        });
        // Note : cliquer sur le bouton "Détecter les panneaux de signalisation" puis agrandir la
        //        fenêtre pour afficher le panneau 
        

        int width_bouton = 300;
        int height_bouton = 40;
        // Positionner le bouton en-dessous de l'image centré
        int x_bouton = x+set_dimension(icon.getIconWidth(), facteur)/2-width_bouton/2;
        int y_bouton = y+set_dimension(icon.getIconHeight(), facteur)+10;
        bouton.setBounds(x_bouton,y_bouton,width_bouton,height_bouton);
        frame.add(bouton);

        frame.setVisible(true);
    
    }

    public static int set_dimension(int mesure, double facteur){
        double me = mesure*facteur;
        int m = (int) Math.round(me);
        return m;
    }
}
