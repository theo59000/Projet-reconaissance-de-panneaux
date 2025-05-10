import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.io.*;
import java.awt.image.BufferedImage;
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

public class VideoDetectionWindow extends JFrame {
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SECONDARY_COLOR = new Color(52, 152, 219);
    private static final Color BACKGROUND_COLOR = new Color(236, 240, 241);
    private static final Color TEXT_COLOR = new Color(44, 62, 80);

    private JPanel mainPanel;
    private JPanel videoSurface;
    private JPanel controlPanel;
    private EmbeddedMediaPlayerComponent mediaPlayerComponent;
    private JSlider progressSlider;
    private Timer progressTimer;
    private Timer detectionTimer;
    private boolean isPlaying = false;
    private Rectangle roiRect;
    private boolean isDrawingROI = false;
    private Point startPoint;
    private JPanel roiPanel;
    private JLabel detectionLabel;
    private JPanel detectionPanel;

    public VideoDetectionWindow() {
        super("Détection Vidéo - Reconnaissance de Panneaux");
        setupFrame();
        createMainPanel();
        initializeVLC();
        initializeROI();
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
        mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.setBounds(0, 0, 1000, 800);
        mainPanel.setBackground(BACKGROUND_COLOR);
        add(mainPanel);

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

        // Timer pour mettre à jour la progression
        progressTimer = new Timer(1000, e -> updateProgress());
    }

    private void createDetectionPanel() {
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

    private void initializeROI() {
        roiPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (roiRect != null) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setColor(new Color(255, 0, 0, 100));
                    g2d.fill(roiRect);
                    g2d.setColor(Color.RED);
                    g2d.draw(roiRect);
                }
            }
        };
        roiPanel.setOpaque(false);
        roiPanel.setBounds(50, 50, 800, 500);
        mainPanel.add(roiPanel);

        roiPanel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                startPoint = e.getPoint();
                isDrawingROI = true;
                roiRect = null;
                roiPanel.repaint();
            }

            public void mouseReleased(MouseEvent e) {
                isDrawingROI = false;
                if (roiRect != null && roiRect.width > 10 && roiRect.height > 10) {
                    startDetectionOnVideo();
                }
            }
        });

        roiPanel.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (isDrawingROI) {
                    int x = Math.min(startPoint.x, e.getX());
                    int y = Math.min(startPoint.y, e.getY());
                    int width = Math.abs(e.getX() - startPoint.x);
                    int height = Math.abs(e.getY() - startPoint.y);
                    roiRect = new Rectangle(x, y, width, height);
                    roiPanel.repaint();
                }
            }
        });
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
            isPlaying = false;
        } else {
            mediaPlayerComponent.mediaPlayer().controls().play();
            progressTimer.start();
            if (roiRect != null) {
                startDetectionOnVideo();
            }
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
            isPlaying = false;
            progressSlider.setValue(0);
            progressSlider.setEnabled(false);
            roiRect = null;
            roiPanel.repaint();
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

    private void startDetectionOnVideo() {
        if (detectionTimer != null) {
            detectionTimer.stop();
        }

        detectionTimer = new Timer(1000, e -> {
            if (mediaPlayerComponent != null && isPlaying && roiRect != null) {
                detectInVideoFrame();
            }
        });
        detectionTimer.start();
    }

    private void detectInVideoFrame() {
        try {
            BufferedImage frame = mediaPlayerComponent.mediaPlayer().snapshots().get();
            if (frame != null && roiRect != null) {
                BufferedImage roi = frame.getSubimage(
                    roiRect.x, roiRect.y, roiRect.width, roiRect.height);

                File tempFile = File.createTempFile("temp_roi", ".jpg");
                ImageIO.write(roi, "jpg", tempFile);

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
                    
                    SwingUtilities.invokeLater(() -> {
                        detectionLabel.setText("Panneau détecté : " + detectedClass);
                        detectionLabel.setForeground(PRIMARY_COLOR);
                        detectionPanel.repaint();
                    });
                }

                tempFile.delete();
            }
        } catch (Exception ex) {
            System.err.println("Erreur lors de la détection sur la vidéo: " + ex.getMessage());
            SwingUtilities.invokeLater(() -> {
                detectionLabel.setText("Erreur de détection : " + ex.getMessage());
                detectionLabel.setForeground(Color.RED);
                detectionPanel.repaint();
            });
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