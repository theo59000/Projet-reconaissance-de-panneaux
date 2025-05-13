import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.io.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import javax.imageio.ImageIO;
import javax.swing.filechooser.FileNameExtensionFilter;
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
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.highgui.Highgui;
import java.util.ArrayList;
import java.util.List;

public class VideoDetectionWindow extends JFrame {
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SECONDARY_COLOR = new Color(52, 152, 219);
    private static final Color BACKGROUND_COLOR = new Color(236, 240, 241);
    private static final Color TEXT_COLOR = new Color(44, 62, 80);

    private JPanel mainPanel;
    private JPanel videoSurface;
    private JPanel detectedVideoSurface;
    private JPanel controlPanel;
    private EmbeddedMediaPlayerComponent mediaPlayerComponent;
    private JSlider progressSlider;
    private Timer progressTimer;
    private Timer detectionTimer;
    private Timer apiRequestTimer;
    private boolean isPlaying = false;
    private JLabel detectionLabel;
    private JPanel detectionPanel;
    private static final int FPS = 10; // 10 FPS pour la détection
    private static final int API_REQUEST_INTERVAL = 500; // Intervalle de 1 seconde pour les requêtes API

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public VideoDetectionWindow() {
        super("Détection Vidéo - Reconnaissance de Panneaux");
        setupFrame();
        createMainPanel();
        initializeVLC();
        createDetectionPanel();
        setVisible(true);
    }

    private void setupFrame() {
        setSize(1000, 800);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND_COLOR);
    }

    private void createMainPanel() {
        // Créer un JScrollPane comme conteneur principal
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(0, 0, 1000, 800);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane);

        // Créer le panneau principal qui contiendra tous les éléments
        mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.setPreferredSize(new Dimension(1000, 1200)); // Hauteur augmentée pour la vidéo détectée
        mainPanel.setBackground(BACKGROUND_COLOR);
        scrollPane.setViewportView(mainPanel);

        // Surface de lecture vidéo originale
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

        // Timer pour mettre à jour la progression
        progressTimer = new Timer(1000, e -> updateProgress());
    }

    private void createDetectionPanel() {
        // Panel des résultats de détection
        detectionPanel = new JPanel();
        detectionPanel.setLayout(new BoxLayout(detectionPanel, BoxLayout.Y_AXIS));
        detectionPanel.setBounds(50, 670, 800, 100);
        detectionPanel.setBackground(new Color(255, 255, 255, 200));
        detectionPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR),
            "Résultats de détection",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14),
            PRIMARY_COLOR
        ));
        mainPanel.add(detectionPanel);

        detectionLabel = new JLabel("Aucune détection");
        detectionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        detectionLabel.setForeground(TEXT_COLOR);
        detectionPanel.add(detectionLabel);

        // Panel pour la vidéo détectée
        JPanel detectedVideoPanel = new JPanel();
        detectedVideoPanel.setLayout(new BorderLayout());
        detectedVideoPanel.setBounds(50, 780, 800, 500);
        detectedVideoPanel.setBackground(new Color(255, 255, 255, 200));
        detectedVideoPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR),
            "Vidéo avec détections",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14),
            PRIMARY_COLOR
        ));
        mainPanel.add(detectedVideoPanel);

        // Surface pour afficher la vidéo détectée
        detectedVideoSurface = new JPanel();
        detectedVideoSurface.setBackground(Color.BLACK);
        detectedVideoPanel.add(detectedVideoSurface, BorderLayout.CENTER);
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

    private void initializeVLC() {
        boolean found = new NativeDiscovery().discover();
        if (!found) {
            JOptionPane.showMessageDialog(this,
                "VLC n'a pas été trouvé sur votre système. Veuillez l'installer pour utiliser le lecteur vidéo.",
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
        mediaPlayerComponent.setPreferredSize(new Dimension(800, 500));
        
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
                stopVideo();
                mediaPlayerComponent.mediaPlayer().media().play(videoFile.getAbsolutePath());
                progressSlider.setEnabled(true);
                progressSlider.setValue(0);
                togglePlay();
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
            if (detectionTimer != null) {
                detectionTimer.stop();
            }
            if (apiRequestTimer != null) {
                apiRequestTimer.stop();
            }
            isPlaying = false;
        } else {
            mediaPlayerComponent.mediaPlayer().controls().play();
            progressTimer.start();
            startDetectionOnVideo();
            isPlaying = true;
        }
    }

    private void stopVideo() {
        if (mediaPlayerComponent != null) {
            mediaPlayerComponent.mediaPlayer().controls().stop();
            progressTimer.stop();
            if (detectionTimer != null) {
                detectionTimer.stop();
            }
            if (apiRequestTimer != null) {
                apiRequestTimer.stop();
            }
            isPlaying = false;
            progressSlider.setValue(0);
            progressSlider.setEnabled(false);
            detectionLabel.setText("Aucune détection");
            detectionLabel.setForeground(TEXT_COLOR);
            detectionPanel.repaint();
        }
    }

    private void updateProgress() {
        if (mediaPlayerComponent != null && isPlaying) {
            float position = mediaPlayerComponent.mediaPlayer().status().position();
            progressSlider.setValue((int)(position * 100));
        }
    }

    private void detectInVideoFrame() {
        try {
            BufferedImage frame = mediaPlayerComponent.mediaPlayer().snapshots().get();
            if (frame != null) {
                // Convertir BufferedImage en Mat
                Mat mat = new Mat(frame.getHeight(), frame.getWidth(), CvType.CV_8UC3);
                byte[] data = ((DataBufferByte) frame.getRaster().getDataBuffer()).getData();
                mat.put(0, 0, data);

                // Convertir en HSV
                Mat hsv = new Mat();
                Imgproc.cvtColor(mat, hsv, Imgproc.COLOR_BGR2HSV);

                // Créer les masques pour les couleurs des panneaux
                Mat mask1 = new Mat();
                Mat mask2 = new Mat();
                Mat mask = new Mat();

                // Rouge (0-10 et 160-180)
                Core.inRange(hsv, new Scalar(0, 100, 100), new Scalar(10, 255, 255), mask1);
                Core.inRange(hsv, new Scalar(160, 100, 100), new Scalar(180, 255, 255), mask2);
                Core.add(mask1, mask2, mask);

                // Appliquer un flou gaussien
                Imgproc.GaussianBlur(mask, mask, new Size(9, 9), 2, 2);

                // Trouver les contours
                List<MatOfPoint> contours = new ArrayList<>();
                Mat hierarchy = new Mat();
                Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

                // Créer une copie de l'image pour dessiner les rectangles
                BufferedImage frameWithDetection = new BufferedImage(
                    frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = frameWithDetection.createGraphics();
                g2d.drawImage(frame, 0, 0, null);

                // Dessiner les rectangles HSV
                g2d.setColor(Color.GREEN);
                g2d.setStroke(new BasicStroke(3));

                boolean detectionFound = false;
                for (MatOfPoint contour : contours) {
                    Rect rect = Imgproc.boundingRect(contour);
                    // Filtrer les rectangles trop petits
                    if (rect.width > 50 && rect.height > 50) {
                        detectionFound = true;
                        g2d.drawRect(rect.x, rect.y, rect.width, rect.height);
                    }
                }

                g2d.dispose();

                // Mettre à jour l'interface avec l'image originale
                final BufferedImage finalFrame = frame;
                SwingUtilities.invokeLater(() -> {
                    JLabel videoLabel = new JLabel(new ImageIcon(finalFrame));
                    videoLabel.setBounds(0, 0, videoSurface.getWidth(), videoSurface.getHeight());
                    videoSurface.removeAll();
                    videoSurface.add(videoLabel);
                    videoSurface.revalidate();
                    videoSurface.repaint();
                });

                // Libérer les ressources OpenCV
                mat.release();
                hsv.release();
                mask1.release();
                mask2.release();
                mask.release();
                hierarchy.release();
            }
        } catch (Exception ex) {
            System.err.println("Erreur lors de la détection sur la vidéo: " + ex.getMessage());
        }
    }

    private void startDetectionOnVideo() {
        if (detectionTimer != null) {
            detectionTimer.stop();
        }
        if (apiRequestTimer != null) {
            apiRequestTimer.stop();
        }

        // Timer pour la détection HSV (10 FPS)
        detectionTimer = new Timer(100, e -> {
            if (mediaPlayerComponent != null && isPlaying) {
                detectInVideoFrame();
            }
        });
        detectionTimer.start();

        // Timer pour les requêtes API (1 requête par seconde)
        apiRequestTimer = new Timer(API_REQUEST_INTERVAL, e -> {
            if (mediaPlayerComponent != null && isPlaying) {
                sendFrameToAPI();
            }
        });
        apiRequestTimer.start();
    }

    private void sendFrameToAPI() {
        try {
            BufferedImage frame = mediaPlayerComponent.mediaPlayer().snapshots().get();
            if (frame != null) {
                // Convertir BufferedImage en Mat
                Mat mat = new Mat(frame.getHeight(), frame.getWidth(), CvType.CV_8UC3);
                byte[] data = ((DataBufferByte) frame.getRaster().getDataBuffer()).getData();
                mat.put(0, 0, data);

                // Convertir en HSV
                Mat hsv = new Mat();
                Imgproc.cvtColor(mat, hsv, Imgproc.COLOR_BGR2HSV);

                // Créer les masques pour les couleurs des panneaux
                Mat mask1 = new Mat();
                Mat mask2 = new Mat();
                Mat mask = new Mat();

                // Rouge (0-10 et 160-180)
                Core.inRange(hsv, new Scalar(0, 100, 100), new Scalar(10, 255, 255), mask1);
                Core.inRange(hsv, new Scalar(160, 100, 100), new Scalar(180, 255, 255), mask2);
                Core.add(mask1, mask2, mask);

                // Appliquer un flou gaussien
                Imgproc.GaussianBlur(mask, mask, new Size(9, 9), 2, 2);

                // Trouver les contours
                List<MatOfPoint> contours = new ArrayList<>();
                Mat hierarchy = new Mat();
                Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

                for (MatOfPoint contour : contours) {
                    Rect rect = Imgproc.boundingRect(contour);
                    // Filtrer les rectangles trop petits
                    if (rect.width > 25 && rect.height > 25) {
                        // Créer une copie de l'image pour dessiner les rectangles
                        BufferedImage frameWithDetection = new BufferedImage(
                            frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_RGB);
                        Graphics2D g2d = frameWithDetection.createGraphics();
                        g2d.drawImage(frame, 0, 0, null);

                        // Dessiner le rectangle HSV
                        g2d.setColor(Color.GREEN);
                        g2d.setStroke(new BasicStroke(3));
                        g2d.drawRect(rect.x, rect.y, rect.width, rect.height);

                        // Extraire la région du panneau pour l'API
                        BufferedImage roi = frame.getSubimage(
                            rect.x, rect.y, rect.width, rect.height);

                        // Envoyer à l'API
                        File tempFile = File.createTempFile("temp_roi", ".jpg");
                        ImageIO.write(roi, "jpg", tempFile);

                        try {
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

                                // Afficher le type de panneau détecté
                                g2d.setColor(Color.WHITE);
                                g2d.setFont(new Font("Arial", Font.BOLD, 14));
                                g2d.drawString(detectedClass, rect.x, rect.y - 5);

                                // Mettre à jour le label de détection
                                final String finalDetectedClass = detectedClass;
                                SwingUtilities.invokeLater(() -> {
                                    detectionLabel.setText("Panneau détecté : " + finalDetectedClass);
                                    detectionLabel.setForeground(Color.GREEN);
                                    detectionPanel.repaint();
                                });

                                // Mettre à jour l'interface avec l'image détectée
                                final BufferedImage finalFrame = frameWithDetection;
                                SwingUtilities.invokeLater(() -> {
                                    JLabel detectionLabel = new JLabel(new ImageIcon(finalFrame));
                                    detectionLabel.setBounds(0, 0, detectedVideoSurface.getWidth(), detectedVideoSurface.getHeight());
                                    detectedVideoSurface.removeAll();
                                    detectedVideoSurface.add(detectionLabel);
                                    detectedVideoSurface.revalidate();
                                    detectedVideoSurface.repaint();
                                });
                            }
                        } catch (Exception ex) {
                            System.err.println("Erreur lors de l'envoi à l'API: " + ex.getMessage());
                        } finally {
                            tempFile.delete();
                        }

                        g2d.dispose();
                        break; // Sortir après la première détection
                    }
                }

                // Libérer les ressources OpenCV
                mat.release();
                hsv.release();
                mask1.release();
                mask2.release();
                mask.release();
                hierarchy.release();
            }
        } catch (Exception ex) {
            System.err.println("Erreur lors de la capture de l'image: " + ex.getMessage());
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
} 