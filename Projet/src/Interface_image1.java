import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Desktop;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

public class Interface_image1 extends JFrame {

    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SECONDARY_COLOR = new Color(52, 152, 219);
    private static final Color BACKGROUND_COLOR = new Color(236, 240, 241);
    private static final Color TEXT_COLOR = new Color(44, 62, 80);

    JLabel imageLabel;
    JLabel imageLabel2;
    JPanel mainPanel;
    JPanel controlPanel;
    JPanel videoSurface;

    // Paramètres configurables
    private double imageScaleFactor = 0.5;
    private double detectionScaleFactor = 0.7;
    private boolean showBorders = true;
    private boolean autoDetect = false;

    private int l1 = 0;
    private int h1 = 0;
    private int l2 = 0;
    private int h2 = 0;    
    private int x1 = 50;
    private int y1 = 50;
    private int x2 = 0;
    private int y2 = 0;

    private BufferedImage originalImage = null;

    // Video player components
    private EmbeddedMediaPlayerComponent mediaPlayerComponent;
    private JSlider progressSlider;
    private Timer progressTimer;
    private boolean isPlaying = false;

    public Interface_image1() {
        super("Projet Twizy - Reconnaissance de Panneaux");
        setupFrame();
        createMainPanel();
        setupBackground();
        setVisible(true);
    }

    private void setupFrame() {
        setSize(1200, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND_COLOR);
    }

    private void createMainPanel() {
        mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.setBounds(0, 0, 1200, 800);
        mainPanel.setBackground(BACKGROUND_COLOR);
        add(mainPanel);

        // Label pour l'image principale
        imageLabel = new JLabel();
        imageLabel.setBounds(x1, y1, l1, h1);
        imageLabel.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 2));
        mainPanel.add(imageLabel);

        // Label pour l'image de détection
        imageLabel2 = new JLabel();
        imageLabel2.setBounds(x2, y2, l2, h2);
        imageLabel2.setBorder(BorderFactory.createLineBorder(SECONDARY_COLOR, 2));
        mainPanel.add(imageLabel2);

        // Surface de lecture vidéo
        videoSurface = new JPanel();
        videoSurface.setBounds(50, 50, 800, 500);
        videoSurface.setBackground(Color.BLACK);
        mainPanel.add(videoSurface);

        // Panneau de contrôle vidéo
        JPanel videoControls = new JPanel(new BorderLayout());
        videoControls.setBounds(50, 560, 800, 100);
        videoControls.setBackground(new Color(255, 255, 255, 200));
        mainPanel.add(videoControls);

        // Slider de progression
        progressSlider = new JSlider(0, 100, 0);
        progressSlider.setEnabled(false);
        progressSlider.addChangeListener(e -> {
            if (!progressSlider.getValueIsAdjusting() && mediaPlayerComponent != null) {
                mediaPlayerComponent.mediaPlayer().controls().setPosition(progressSlider.getValue() / 100.0f);
            }
        });
        videoControls.add(progressSlider, BorderLayout.NORTH);

        // Boutons de contrôle
        JPanel controlButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        controlButtons.setBackground(new Color(255, 255, 255, 200));

        JButton openButton = createStyledButton("Ouvrir Vidéo");
        openButton.addActionListener(e -> openVideo());
        controlButtons.add(openButton);

        JButton playButton = createStyledButton("Lecture");
        playButton.addActionListener(e -> togglePlay());
        controlButtons.add(playButton);

        JButton stopButton = createStyledButton("Stop");
        stopButton.addActionListener(e -> stopVideo());
        controlButtons.add(stopButton);

        videoControls.add(controlButtons, BorderLayout.CENTER);

        // Panneau de contrôle pour les images
        controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBounds(900, 50, 250, 200);
        controlPanel.setBackground(new Color(255, 255, 255, 200));
        controlPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        mainPanel.add(controlPanel);

        // Titre du panneau de contrôle
        JLabel titleLabel = new JLabel("Contrôles");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        controlPanel.add(titleLabel);
        controlPanel.add(Box.createVerticalStrut(20));

        // Bouton pour choisir une image
        JButton btn = createStyledButton("Choisir une image");
        btn.addActionListener(e -> choix_image());
        controlPanel.add(btn);
        controlPanel.add(Box.createVerticalStrut(15));

        // Bouton de détection
        JButton bouton = createStyledButton("Détection panneaux");
        bouton.addActionListener(e -> detecter_panneau());
        controlPanel.add(bouton);

        // Timer pour mettre à jour la progression
        progressTimer = new Timer(1000, e -> updateProgress());

        // Initialiser VLC
        initializeVLC();
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY_COLOR);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setMaximumSize(new Dimension(200, 40));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Effet de survol
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(SECONDARY_COLOR);
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(PRIMARY_COLOR);
            }
        });
        
        return button;
    }

    private void setupBackground() {
        try {
            ImageIcon icon = new ImageIcon("Images_panneaux\\4fant.jpg");
            Image image = icon.getImage().getScaledInstance(1200, 800, Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(image);
            JLabel backgroundLabel = new JLabel(scaledIcon);
            backgroundLabel.setBounds(0, 0, 1200, 800);
            mainPanel.add(backgroundLabel);
            mainPanel.setComponentZOrder(backgroundLabel, mainPanel.getComponentCount() - 1);
        } catch (Exception e) {
            System.out.println("Erreur lors du chargement de l'image de fond");
        }
    }

    private void choix_image() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                if (f.isDirectory()) return true;
                String name = f.getName().toLowerCase();
                return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png");
            }
            public String getDescription() {
                return "Images (*.jpg, *.jpeg, *.png)";
            }
        });

        int retour = fileChooser.showOpenDialog(this);

        if (retour == JFileChooser.APPROVE_OPTION) {
            File fichier = fileChooser.getSelectedFile();

            try {
                // Sauvegarder l'image originale
                originalImage = ImageIO.read(fichier);
                
                // Appliquer l'échelle initiale
                l1 = (int)(originalImage.getWidth() * imageScaleFactor);
                h1 = (int)(originalImage.getHeight() * imageScaleFactor);

                Image imgReduite = originalImage.getScaledInstance(l1, h1, Image.SCALE_SMOOTH);
                imageLabel.setBounds(x1, y1, l1, h1);
                imageLabel.setIcon(new ImageIcon(imgReduite));

                // Mettre à jour la position de l'image de détection
                updateDetectionImagePosition();

                // Détection automatique si activée
                if (autoDetect) {
                    detecter_panneau();
                }

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Erreur de lecture de l'image.",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void detecter_panneau() {
        if (imageLabel.getIcon() == null) {
            JOptionPane.showMessageDialog(this,
                "Veuillez d'abord sélectionner une image.",
                "Information",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            BufferedImage img2 = ImageIO.read(new File("Images_panneaux\\panneau_30.jpg"));

            // Utiliser le facteur d'échelle configuré
            l2 = (int)(img2.getWidth() * detectionScaleFactor);
            h2 = (int)(img2.getHeight() * detectionScaleFactor);

            Image imgReduite2 = img2.getScaledInstance(l2, h2, Image.SCALE_SMOOTH);
            updateDetectionImagePosition();
            imageLabel2.setIcon(new ImageIcon(imgReduite2));

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors de la détection du panneau.",
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateDetectionImagePosition() {
        // Positionner l'image de détection en dessous de l'image principale
        x2 = x1 + (l1 - l2) / 2;
        y2 = y1 + h1 + 20;
        imageLabel2.setBounds(x2, y2, l2, h2);
    }

    private void initializeVLC() {
        boolean found = new NativeDiscovery().discover();
        if (!found) {
            JOptionPane.showMessageDialog(this,
                "VLC n'a pas été trouvé sur votre système. Veuillez l'installer pour utiliser le lecteur vidéo.",
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create and configure the media player component
        mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
        mediaPlayerComponent.setPreferredSize(new Dimension(800, 500));
        
        // Configure the video surface
        videoSurface.removeAll();
        videoSurface.setLayout(new BorderLayout());
        videoSurface.add(mediaPlayerComponent, BorderLayout.CENTER);
        videoSurface.revalidate();
        videoSurface.repaint();
    }

    private void openVideo() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter(
            "Vidéos (*.mp4, *.avi, *.mkv)", "mp4", "avi", "mkv"));

        int retour = fileChooser.showOpenDialog(this);
        if (retour == JFileChooser.APPROVE_OPTION) {
            File videoFile = fileChooser.getSelectedFile();
            try {
                // Arrêter la lecture en cours si nécessaire
                stopVideo();

                // Charger la nouvelle vidéo
                mediaPlayerComponent.mediaPlayer().media().play(videoFile.getAbsolutePath());
                
                // Activer le slider
                progressSlider.setEnabled(true);
                progressSlider.setValue(0);

                // Démarrer la lecture
                togglePlay();

                // Force refresh of the video surface
                videoSurface.revalidate();
                videoSurface.repaint();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Erreur lors de l'ouverture de la vidéo: " + ex.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void togglePlay() {
        if (mediaPlayerComponent == null) return;

        if (isPlaying) {
            mediaPlayerComponent.mediaPlayer().controls().pause();
            progressTimer.stop();
            isPlaying = false;
        } else {
            mediaPlayerComponent.mediaPlayer().controls().play();
            progressTimer.start();
            isPlaying = true;
        }
    }

    private void stopVideo() {
        if (mediaPlayerComponent != null) {
            mediaPlayerComponent.mediaPlayer().controls().stop();
            progressTimer.stop();
            isPlaying = false;
            progressSlider.setValue(0);
            progressSlider.setEnabled(false);
        }
    }

    private void updateProgress() {
        if (mediaPlayerComponent != null && isPlaying) {
            float position = mediaPlayerComponent.mediaPlayer().status().position();
            progressSlider.setValue((int)(position * 100));
        }
    }

    @Override
    public void dispose() {
        stopVideo();
        if (mediaPlayerComponent != null) {
            mediaPlayerComponent.release();
        }
        super.dispose();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new Interface_image1());
    }
}
