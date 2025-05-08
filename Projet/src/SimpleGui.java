import javax.swing.*; // Importe les classes Swing pour l'interface graphique
import java.awt.*; // Importe des classes AWT (utilisées par Swing)
import java.awt.event.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import javax.imageio.ImageIO;

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
        String imagePath = "s90_20.jpg";
        ImageIcon icon = new ImageIcon(imagePath);

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
            try {
                File imageFile = new File(imagePath);
        
                // Connexion à l'API
                URL url = new URL("http://127.0.0.1:5000/predict");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setDoOutput(true);
                con.setRequestProperty("Content-Type", "application/octet-stream");

                // Lire l'image et envoyer les octets
                try (OutputStream os = con.getOutputStream();
                     FileInputStream fis = new FileInputStream(imageFile)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                }

                // Lire la réponse complète
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    sb.append(line);
                }
                in.close();

                String response = sb.toString();
                System.out.println("Réponse brute de l'API : " + response);

                String labelValue = "Inconnu";
                String base64Image = null;

                try {
                    // Cherche la valeur du champ "class"
                    int classIndex = response.indexOf("\"class\"");
                    if (classIndex != -1) {
                        int colonIndex = response.indexOf(":", classIndex);
                        int quoteStart = response.indexOf("\"", colonIndex);
                        int quoteEnd = response.indexOf("\"", quoteStart + 1);
                        labelValue = response.substring(quoteStart + 1, quoteEnd);
                    }

                    // Cherche la valeur du champ "image"
                    int imageIndex = response.indexOf("\"image\"");
                    if (imageIndex != -1) {
                        int colonIndex = response.indexOf(":", imageIndex);
                        int quoteStart = response.indexOf("\"", colonIndex);
                        int quoteEnd = response.indexOf("\"", quoteStart + 1);
                        base64Image = response.substring(quoteStart + 1, quoteEnd);
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                //Affichage dans l'interface
                JLabel resultLabel = new JLabel("Résultat : " + labelValue);
                resultLabel.setBounds(x + 200, y + 200, 300, 30);
                frame.add(resultLabel);

                if (base64Image != null) {
                    byte[] imageBytes = Base64.getDecoder().decode(base64Image);
                    ImageIcon annotatedIcon = new ImageIcon(imageBytes);
                    JLabel annotatedLabel = new JLabel(annotatedIcon);
                    annotatedLabel.setBounds(x + 400, y + 50,
                            annotatedIcon.getIconWidth(), annotatedIcon.getIconHeight());
                    frame.add(annotatedLabel);
                }
                frame.repaint();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

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