package fr.synapse;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatLightLaf;

public class YTConverter {

    public static void main(String[] args) {
        try {
            //UIManager.setLookAndFeel(new FlatLightLaf());
            UIManager.setLookAndFeel(new FlatDarculaLaf());
            UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 14));
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(YTConverter::createGUI);
    }

    private static void createGUI() {
        JFrame frame = new JFrame("Lolo (le meilleur des chiens) Youtube Extracteur By Pipou Software");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //frame.setSize(880, 320);
        frame.setSize(1000, 320);
        frame.setLocationRelativeTo(null);

        // Chargement des icônes
        ImageIcon icon = new ImageIcon(YTConverter.class.getResource("/lolo_intro_b.png"));
        ImageIcon iconSuccess = new ImageIcon(YTConverter.class.getResource("/lolo_succes_b.png"));
        ImageIcon iconFailed = new ImageIcon(YTConverter.class.getResource("/lolo_failed_b.png"));
        ImageIcon folderIcon = new ImageIcon(YTConverter.class.getResource("/icons/folder.png"));
        ImageIcon convertIcon = new ImageIcon(YTConverter.class.getResource("/icons/convert.png"));
        ImageIcon infoIcon = new ImageIcon(YTConverter.class.getResource("/icons/info.png"));

        // Menu
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Fichier");
        JMenuItem exitItem = new JMenuItem("Quitter");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        JMenu helpMenu = new JMenu("Aide");
        JMenuItem aboutItem = new JMenuItem("À propos");

//        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(frame,
//                "Lolo Youtube Extracteur v1.0\nDéveloppé avec lolo chien ninja codeur.\n\nUtilise yt-dlp + ffmpeg.",
//                "À propos", JOptionPane.INFORMATION_MESSAGE));

        aboutItem.addActionListener(e -> {
            ImageIcon icon_info = new ImageIcon(YTConverter.class.getResource("/lolo_about.png"));
            JLabel imageLabel = new JLabel(icon_info);
            imageLabel.setPreferredSize(new Dimension(100, 133));

            JLabel textLabel = new JLabel("<html>" +
                    "<p>Pipou Software présente :" +
                    "<h3>Lolo Youtube Extracteur v1.0</h3>" +
                    "<p>Développé avec lolo 🐶 ninja.</p>" +
                    "<p>Petit assistant codeur.</p>" +
                    "<p>Avec beaucoup de Bonies !!" +
                    "</html>");

            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.add(imageLabel, BorderLayout.WEST);
            panel.add(textLabel, BorderLayout.CENTER);
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JOptionPane.showMessageDialog(frame, panel, "À propos", JOptionPane.INFORMATION_MESSAGE);
        });

        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        frame.setJMenuBar(menuBar);

        // Champs et composants
        JTextField urlField = new JTextField();
        JButton convertButton = new JButton("Convertir en MP3", convertIcon);
        JButton convertButtonMp4 = new JButton("Convertir en MP4", convertIcon);
        JLabel statusLabel = new JLabel("Entrez l'URL ou l'ID d'une vidéo YouTube.", infoIcon, JLabel.LEFT);

        JTextField outputDirField = new JTextField(System.getProperty("user.dir") + File.separator + "downloads");
        outputDirField.setEditable(false);
        JButton browseButton = new JButton("Parcourir...", folderIcon);

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setVisible(false);

        JLabel imageLabel = new JLabel(icon);
        //imageLabel.setPreferredSize(new Dimension(120, 120));
        imageLabel.setPreferredSize(new Dimension(240, 240));

        browseButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = chooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedDir = chooser.getSelectedFile();
                outputDirField.setText(selectedDir.getAbsolutePath());
            }
        });

        convertButton.addActionListener((ActionEvent e) -> {
            imageLabel.setIcon(icon);
            String url;
            try {
                url = normalizeYoutubeUrl(urlField.getText().trim());
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage(), "Erreur URL", JOptionPane.ERROR_MESSAGE);
                convertButton.setEnabled(true);
                return;
            }

            String outputDir = outputDirField.getText();

            if (url.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Veuillez entrer une URL ou un ID valide.");
                return;
            }

            File dir = new File(outputDir);
            if (!dir.exists() || !dir.isDirectory()) {
                JOptionPane.showMessageDialog(frame, "Le dossier de destination est invalide.");
                return;
            }

            convertButton.setEnabled(false);
            statusLabel.setText("Téléchargement en cours...");
            progressBar.setValue(0);
            progressBar.setStringPainted(false);
            progressBar.setIndeterminate(true);
            progressBar.setVisible(true);

            new Thread(() -> {
                try {
                    File ytDlp = extractResourceToAppDir("/native-bin/yt-dlp.exe", "yt-dlp.exe");
                    File ffmpeg = extractResourceToAppDir("/native-bin/ffmpeg.exe", "ffmpeg.exe");
                    extractResourceToAppDir("/native-bin/avcodec-62.dll", "avcodec-62.dll");
                    extractResourceToAppDir("/native-bin/avdevice-62.dll", "avdevice-62.dll");
                    extractResourceToAppDir("/native-bin/avfilter-11.dll", "avfilter-11.dll");
                    extractResourceToAppDir("/native-bin/avformat-62.dll", "avformat-62.dll");
                    extractResourceToAppDir("/native-bin/avutil-60.dll", "avutil-60.dll");
                    extractResourceToAppDir("/native-bin/ffplay.exe", "ffplay.exe");
                    extractResourceToAppDir("/native-bin/ffprobe.exe", "ffprobe.exe");
                    extractResourceToAppDir("/native-bin/postproc-59.dll", "postproc-59.dll");
                    extractResourceToAppDir("/native-bin/swresample-6.dll", "swresample-6.dll");
                    extractResourceToAppDir("/native-bin/swscale-9.dll", "swscale-9.dll");

                    String outputPath = outputDir + File.separator + "%(title)s.%(ext)s";

//                    ProcessBuilder pb = new ProcessBuilder(
//                            ytDlp.getAbsolutePath(),
//                            //"--proxy", "http://55.225.55.6:3128",
//                            "--ffmpeg-location", ffmpeg.getAbsolutePath(),
//                            "-f", "bestaudio",
//                            "--extract-audio",
//                            "--audio-format", "mp3",
//                            "-o", outputPath,
//                            url
//                    );
                    ProcessBuilder pb = new ProcessBuilder(
                            ytDlp.getAbsolutePath(),
                            //"--proxy", "http://55.225.55.6:3128",
                            //"--proxy", "http://55.227.148.201:3128",
                            "--ffmpeg-location", ffmpeg.getAbsolutePath(),
                            "-f", "bestaudio",
                            "--extract-audio",
                            "--audio-format", "mp3",
                            "--audio-quality", "0", // Qualité maximale (~320 kbps)
                            "-o", outputPath,
                            url
                    );

                    pb.redirectErrorStream(true);
                    Process process = pb.start();

                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                        if (line.contains("[download]")) {
                            int percentIndex = line.indexOf('%');
                            if (percentIndex > 10) {
                                String sub = line.substring(percentIndex - 6, percentIndex).trim();
                                try {
                                    int progress = (int) Float.parseFloat(sub);
                                    SwingUtilities.invokeLater(() -> {
                                        progressBar.setIndeterminate(false);
                                        progressBar.setValue(progress);
                                        progressBar.setStringPainted(true);
                                    });
                                } catch (NumberFormatException ignored) {}
                            }
                        }
                    }

                    int exitCode = process.waitFor();
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText(exitCode == 0 ? "Conversion réussie !" : "Erreur lors de la conversion.");
                        imageLabel.setIcon(exitCode == 0 ? iconSuccess : iconFailed);
                        convertButton.setEnabled(true);
                        progressBar.setValue(0);
                        progressBar.setVisible(false);
                        progressBar.setStringPainted(false);
                    });

                } catch (Exception ex) {
                    ex.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Erreur : " + ex.getMessage());
                        convertButton.setEnabled(true);
                        progressBar.setValue(0);
                        progressBar.setVisible(false);
                        progressBar.setStringPainted(false);
                    });
                }
            }).start();
        });
        convertButtonMp4.addActionListener((ActionEvent e) -> {
            imageLabel.setIcon(icon);
            String url;
            try {
                url = normalizeYoutubeUrl(urlField.getText().trim());
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage(), "Erreur URL", JOptionPane.ERROR_MESSAGE);
                convertButtonMp4.setEnabled(true);
                return;
            }

            String outputDir = outputDirField.getText();

            if (url.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Veuillez entrer une URL ou un ID valide.");
                return;
            }

            File dir = new File(outputDir);
            if (!dir.exists() || !dir.isDirectory()) {
                JOptionPane.showMessageDialog(frame, "Le dossier de destination est invalide.");
                return;
            }

            convertButtonMp4.setEnabled(false);
            statusLabel.setText("Téléchargement en cours...");
            progressBar.setValue(0);
            progressBar.setStringPainted(false);
            progressBar.setIndeterminate(true);
            progressBar.setVisible(true);

            new Thread(() -> {
                try {
                    File ytDlp = extractResourceToAppDir("/native-bin/yt-dlp.exe", "yt-dlp.exe");
                    File ffmpeg = extractResourceToAppDir("/native-bin/ffmpeg.exe", "ffmpeg.exe");
                    extractResourceToAppDir("/native-bin/avcodec-62.dll", "avcodec-62.dll");
                    extractResourceToAppDir("/native-bin/avdevice-62.dll", "avdevice-62.dll");
                    extractResourceToAppDir("/native-bin/avfilter-11.dll", "avfilter-11.dll");
                    extractResourceToAppDir("/native-bin/avformat-62.dll", "avformat-62.dll");
                    extractResourceToAppDir("/native-bin/avutil-60.dll", "avutil-60.dll");
                    extractResourceToAppDir("/native-bin/ffplay.exe", "ffplay.exe");
                    extractResourceToAppDir("/native-bin/ffprobe.exe", "ffprobe.exe");
                    extractResourceToAppDir("/native-bin/postproc-59.dll", "postproc-59.dll");
                    extractResourceToAppDir("/native-bin/swresample-6.dll", "swresample-6.dll");
                    extractResourceToAppDir("/native-bin/swscale-9.dll", "swscale-9.dll");

                    String outputPath = outputDir + File.separator + "%(title)s.%(ext)s";

                    ProcessBuilder pb = new ProcessBuilder(
                            ytDlp.getAbsolutePath(),
                            //"--proxy", "http://55.225.55.6:3128",
                            //"--proxy", "http://55.227.148.201:3128",
                            "--ffmpeg-location", ffmpeg.getAbsolutePath(),
                            "-f", "bestvideo[ext=mp4]+bestaudio[ext=m4a]/mp4",
                            "-o", outputPath,
                            url
                    );

                    pb.redirectErrorStream(true);
                    Process process = pb.start();

                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                        if (line.contains("[download]")) {
                            int percentIndex = line.indexOf('%');
                            if (percentIndex > 10) {
                                String sub = line.substring(percentIndex - 6, percentIndex).trim();
                                try {
                                    int progress = (int) Float.parseFloat(sub);
                                    SwingUtilities.invokeLater(() -> {
                                        progressBar.setIndeterminate(false);
                                        progressBar.setValue(progress);
                                        progressBar.setStringPainted(true);
                                    });
                                } catch (NumberFormatException ignored) {}
                            }
                        }
                    }

                    int exitCode = process.waitFor();
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText(exitCode == 0 ? "Conversion réussie !" : "Erreur lors de la conversion.");
                        imageLabel.setIcon(exitCode == 0 ? iconSuccess : iconFailed);
                        convertButtonMp4.setEnabled(true);
                        progressBar.setValue(0);
                        progressBar.setVisible(false);
                        progressBar.setStringPainted(false);
                    });

                } catch (Exception ex) {
                    ex.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Erreur : " + ex.getMessage());
                        convertButtonMp4.setEnabled(true);
                        progressBar.setValue(0);
                        progressBar.setVisible(false);
                        progressBar.setStringPainted(false);
                    });
                }
            }).start();
        });

        frame.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridheight = 6;
        frame.add(imageLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 2; gbc.gridheight = 1;
        frame.add(new JLabel("URL / ID de la vidéo :"), gbc);

        gbc.gridy = 1;
        frame.add(urlField, gbc);

        gbc.gridy = 2; gbc.gridwidth = 1;
        frame.add(new JLabel("Dossier de sortie :"), gbc);

        gbc.gridx = 2;
        frame.add(outputDirField, gbc);

        gbc.gridx = 3;
        frame.add(browseButton, gbc);

// === Ajout d’un panel pour les deux boutons ===
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0)); // boutons centrés avec un petit écart
        buttonPanel.add(convertButton);
        buttonPanel.add(convertButtonMp4);

        gbc.gridx = 1; gbc.gridy = 3; gbc.gridwidth = 3;
        frame.add(buttonPanel, gbc);

        gbc.gridy = 4;
        frame.add(statusLabel, gbc);

        gbc.gridy = 5;
        frame.add(progressBar, gbc);

        frame.setVisible(true);
    }

    private static File extractResourceToAppDir(String resourcePath, String outputName) throws IOException {
        InputStream in = YTConverter.class.getResourceAsStream(resourcePath);
        if (in == null) throw new FileNotFoundException("Ressource introuvable : " + resourcePath);

        Path binDir = Paths.get(System.getProperty("user.dir"), "bin");
        Files.createDirectories(binDir);
        Path outputPath = binDir.resolve(outputName);

        Files.copy(in, outputPath, StandardCopyOption.REPLACE_EXISTING);
        outputPath.toFile().setExecutable(true);

        return outputPath.toFile();
    }

    private static String normalizeYoutubeUrl(String inputUrl) {
        try {
            String cleanInput = inputUrl.trim();
            if (cleanInput.matches("^[a-zA-Z0-9_-]{11}$")) {
                return "https://www.youtube.com/watch?v=" + cleanInput;
            }

            String cleanUrl = cleanInput.split("\\?")[0];
            String path = cleanUrl.replaceFirst("^(https?://)?(www\\.)?[^/]+/", "");
            String videoId = null;

            if (cleanInput.contains("watch?v=")) {
                return cleanInput;
            } else if (path.startsWith("shorts/")) {
                videoId = path.substring("shorts/".length());
            } else if (path.startsWith("watch/")) {
                videoId = path.substring("watch/".length());
            } else if (path.matches("^[a-zA-Z0-9_-]{11}$")) {
                videoId = path;
            } else {
                String[] parts = path.split("/");
                String last = parts[parts.length - 1];
                if (last.matches("^[a-zA-Z0-9_-]{11}$")) {
                    videoId = last;
                }
            }

            if (videoId != null && videoId.matches("^[a-zA-Z0-9_-]{11}$")) {
                return "https://www.youtube.com/watch?v=" + videoId;
            } else {
                throw new IllegalArgumentException("Impossible d'extraire l'ID de la vidéo.");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("URL ou ID YouTube invalide : " + inputUrl);
        }
    }
}
