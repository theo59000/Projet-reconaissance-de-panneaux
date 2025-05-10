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
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.Base64;
import org.json.JSONObject;

public class Interface_image1 extends JFrame {

    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SECONDARY_COLOR = new Color(52, 152, 219);
    private static final Color BACKGROUND_COLOR = new Color(236, 240, 241);
    private static final Color TEXT_COLOR = new Color(44, 62, 80);

    JLabel imageLabel;
    JLabel imageLabel2;
    JPanel mainPanel;
    JPanel controlPanel;
    JPanel optionsPanel;
    JTabbedPane tabbedPane;

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

    public Interface_image1() {
        super("Détection Image - Reconnaissance de Panneaux");
        setupFrame();
        createMainPanel();
        createTabbedPane();
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
    }

    private void createTabbedPane() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setBounds(900, 50, 250, 400);
        tabbedPane.setBackground(new Color(255, 255, 255, 200));
        mainPanel.add(tabbedPane);

        // Onglet Contrôles
        controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBackground(new Color(255, 255, 255, 200));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        createControlPanel();
        tabbedPane.addTab("Contrôles", new ImageIcon(), controlPanel);

        // Onglet Options
        optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setBackground(new Color(255, 255, 255, 200));
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        createOptionsPanel();
        tabbedPane.addTab("Options", new ImageIcon(), optionsPanel);
    }

    private void createControlPanel() {
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
        controlPanel.add(Box.createVerticalStrut(15));

        // Bouton pour ouvrir la fenêtre de détection vidéo
        JButton videoButton = createStyledButton("Détection Vidéo");
        videoButton.addActionListener(e -> {
            new VideoDetectionWindow();
        });
        controlPanel.add(videoButton);
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

    private void createOptionsPanel() {
        // Titre
        JLabel titleLabel = new JLabel("Paramètres");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        optionsPanel.add(titleLabel);
        optionsPanel.add(Box.createVerticalStrut(20));

        // Échelle de l'image principale
        JPanel scalePanel = createOptionPanel("Échelle de l'image principale");
        JSlider imageScaleSlider = new JSlider(JSlider.HORIZONTAL, 25, 100, 50);
        imageScaleSlider.setMajorTickSpacing(25);
        imageScaleSlider.setMinorTickSpacing(5);
        imageScaleSlider.setPaintTicks(true);
        imageScaleSlider.setPaintLabels(true);
        imageScaleSlider.addChangeListener(e -> {
            if (!imageScaleSlider.getValueIsAdjusting()) {
                imageScaleFactor = imageScaleSlider.getValue() / 100.0;
                if (originalImage != null) {
                    refreshMainImage();
                }
            }
        });
        scalePanel.add(imageScaleSlider);
        optionsPanel.add(scalePanel);
        optionsPanel.add(Box.createVerticalStrut(15));

        // Échelle de l'image de détection
        JPanel detectionScalePanel = createOptionPanel("Échelle de détection");
        JSlider detectionScaleSlider = new JSlider(JSlider.HORIZONTAL, 25, 100, 70);
        detectionScaleSlider.setMajorTickSpacing(25);
        detectionScaleSlider.setMinorTickSpacing(5);
        detectionScaleSlider.setPaintTicks(true);
        detectionScaleSlider.setPaintLabels(true);
        detectionScaleSlider.addChangeListener(e -> {
            detectionScaleFactor = detectionScaleSlider.getValue() / 100.0;
            if (imageLabel2.getIcon() != null) {
                refreshDetectionImage();
            }
        });
        detectionScalePanel.add(detectionScaleSlider);
        optionsPanel.add(detectionScalePanel);
        optionsPanel.add(Box.createVerticalStrut(15));

        // Afficher les bordures
        JPanel borderPanel = createOptionPanel("Afficher les bordures");
        JCheckBox borderCheckBox = new JCheckBox();
        borderCheckBox.setSelected(showBorders);
        borderCheckBox.addActionListener(e -> {
            showBorders = borderCheckBox.isSelected();
            updateBorders();
        });
        borderPanel.add(borderCheckBox);
        optionsPanel.add(borderPanel);
        optionsPanel.add(Box.createVerticalStrut(15));

        // Détection automatique
        JPanel autoDetectPanel = createOptionPanel("Détection automatique");
        JCheckBox autoDetectCheckBox = new JCheckBox();
        autoDetectCheckBox.setSelected(autoDetect);
        autoDetectCheckBox.addActionListener(e -> {
            autoDetect = autoDetectCheckBox.isSelected();
        });
        autoDetectPanel.add(autoDetectCheckBox);
        optionsPanel.add(autoDetectPanel);
    }

    private JPanel createOptionPanel(String label) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(new Color(255, 255, 255, 200));
        JLabel optionLabel = new JLabel(label);
        optionLabel.setForeground(TEXT_COLOR);
        panel.add(optionLabel);
        return panel;
    }

    private void updateBorders() {
        if (showBorders) {
            imageLabel.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 2));
            imageLabel2.setBorder(BorderFactory.createLineBorder(SECONDARY_COLOR, 2));
        } else {
            imageLabel.setBorder(null);
            imageLabel2.setBorder(null);
        }
    }

    private void refreshMainImage() {
        if (originalImage != null) {
            l1 = (int)(originalImage.getWidth() * imageScaleFactor);
            h1 = (int)(originalImage.getHeight() * imageScaleFactor);
            Image imgReduite = originalImage.getScaledInstance(l1, h1, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(imgReduite));
            imageLabel.setBounds(x1, y1, l1, h1);
            updateDetectionImagePosition();
        }
    }

    private void refreshDetectionImage() {
        if (imageLabel2.getIcon() != null) {
            ImageIcon icon = (ImageIcon) imageLabel2.getIcon();
            Image img = icon.getImage();
            l2 = (int)(img.getWidth(null) * detectionScaleFactor);
            h2 = (int)(img.getHeight(null) * detectionScaleFactor);
            Image imgReduite = img.getScaledInstance(l2, h2, Image.SCALE_SMOOTH);
            imageLabel2.setIcon(new ImageIcon(imgReduite));
            updateDetectionImagePosition();
        }
    }

    private void updateDetectionImagePosition() {
        x2 = x1 + (l1 - l2) / 2;
        y2 = y1 + h1 + 20;
        imageLabel2.setBounds(x2, y2, l2, h2);
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
                originalImage = ImageIO.read(fichier);
                l1 = (int)(originalImage.getWidth() * imageScaleFactor);
                h1 = (int)(originalImage.getHeight() * imageScaleFactor);
                Image imgReduite = originalImage.getScaledInstance(l1, h1, Image.SCALE_SMOOTH);
                imageLabel.setBounds(x1, y1, l1, h1);
                imageLabel.setIcon(new ImageIcon(imgReduite));
                updateDetectionImagePosition();

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
            File tempFile = File.createTempFile("temp_image", ".jpg");
            ImageIO.write(originalImage, "jpg", tempFile);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:5000/predict"))
                .header("Content-Type", "image/jpeg")
                .POST(HttpRequest.BodyPublishers.ofFile(tempFile.toPath()))
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject jsonResponse = new JSONObject(response.body());
                String detectedClass = jsonResponse.getString("class");
                String base64Image = jsonResponse.getString("image");

                byte[] imageBytes = Base64.getDecoder().decode(base64Image);
                ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
                BufferedImage detectedImage = ImageIO.read(bis);

                l2 = (int)(detectedImage.getWidth() * detectionScaleFactor);
                h2 = (int)(detectedImage.getHeight() * detectionScaleFactor);
                Image imgReduite2 = detectedImage.getScaledInstance(l2, h2, Image.SCALE_SMOOTH);

                updateDetectionImagePosition();
                imageLabel2.setIcon(new ImageIcon(imgReduite2));

                JOptionPane.showMessageDialog(this,
                    "Panneau détecté : " + detectedClass,
                    "Résultat de la détection",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                throw new IOException("Erreur lors de la détection : " + response.statusCode());
            }

            tempFile.delete();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors de la détection du panneau : " + ex.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
        }
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
