import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Interface_image extends JFrame {

    JLabel imageLabel;
    JLabel imageLabel2;

    private int l1 = 0; // Longueur de la photo
    private int h1 = 0; // Hauteur de la photo

    private int l2 = 0; // Longueur de la photo
    private int h2 = 0; // Hauteur de la photo    
    
    // Position (x,y) de la photo
    private int x1 = 50;
    private int y1 = 50;

    // Position (x,y) de la détection
    private int x2 = 0;
    private int y2 = 0;

    public Interface_image() {
        // Configuration de la fenêtre principale
        super("Projet Twizy");
        int l_interface = 1000;
        int h_interface = 700;
        setSize(l_interface, h_interface);
        setLayout(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Bouton pour choisir une image
        JButton btn = new JButton("Choisir une image");
        int l_btn = 200;
        int h_btn = 30;
        btn.setBounds(l_interface-l_btn-30, x1, l_btn, h_btn);
        add(btn);

        // Action du bouton
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                choix_image();
            }
        });

        // Bouton de détection du panneau de signalisation
        JButton bouton = new JButton("Détection panneaux");
        int l_bouton = 200;
        int h_bouton = 30;
        bouton.setBounds(l_interface-l_bouton-30, 2*x1, l_bouton, h_bouton);
        add(bouton);

        // Détecter le bon panneau
        bouton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                detecter_panneau();
            }
        });

        // Label pour afficher l'image sélectionnée
        imageLabel = new JLabel();
        imageLabel.setBounds(x1, y1, l1, h1); // Taille initiale
        add(imageLabel);

        // Label pour afficher la détection
        imageLabel2 = new JLabel();
        imageLabel2.setBounds(x2, y2, l2, h2);; // Taille initiale
        add(imageLabel2);

        ImageIcon icon = new ImageIcon("Images_panneaux\\4fant.jpg");
        Image image = icon.getImage().getScaledInstance(l_interface, h_interface, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(image);
        JLabel label = new JLabel(scaledIcon);
        label.setBounds(0, 0, l_interface, h_interface);
        add(label);

        setVisible(true);
    }
    
    private void choix_image() {
        JFileChooser fileChooser = new JFileChooser();
        int retour = fileChooser.showOpenDialog(this);

        if (retour == JFileChooser.APPROVE_OPTION) {
            File fichier = fileChooser.getSelectedFile();

            try {
                BufferedImage img = ImageIO.read(fichier);

                double facteur = 0.5;
                l1 = (int)(img.getWidth() * facteur);   // Longueur de l'image 1 modifiée
                h1 = (int)(img.getHeight() * facteur);  // Hauteur de l'image 1 modifiée

                Image imgReduite = img.getScaledInstance(l1, h1, Image.SCALE_SMOOTH);

                // Définir la position de l'image (x, y)
                imageLabel.setBounds(x1, y1, l1, h1);
                imageLabel.setIcon(new ImageIcon(imgReduite));

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Erreur de lecture de l'image.");
            }
        }
    }

    private void detecter_panneau() {
        try {
            BufferedImage img2 = ImageIO.read(new File("Images_panneaux\\panneau_30.jpg"));

            double facteur = 0.7;
            l2 = (int)(img2.getWidth() * facteur);   // Longueur de l'image 2 modifiée
            h2 = (int)(img2.getHeight() * facteur);  // Hauteur de l'image 2 modifiée

            Image imgReduite2 = img2.getScaledInstance(l2, h2, Image.SCALE_SMOOTH);

            // Définir la position de l'image (x, y)
            x2 = l1/2-x1;
            y2 = y1+h1+30;;
            imageLabel2.setBounds(x2, y2, l2, h2);
            imageLabel2.setIcon(new ImageIcon(imgReduite2));


        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Erreur de lecture de l'image.");
        }
    }

    public static void main(String[] args) {
        new Interface_image();
    }
}
