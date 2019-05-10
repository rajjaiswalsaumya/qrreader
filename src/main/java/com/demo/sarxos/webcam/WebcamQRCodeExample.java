package com.demo.sarxos.webcam;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Dimension;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.prefs.Preferences;

public class WebcamQRCodeExample extends JFrame implements Runnable, ThreadFactory {
    private static final String LAST_USED_FOLDER = System.getProperty("user.dir");
    private Preferences prefs = null;
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Executor executor = Executors.newSingleThreadExecutor(this);
    private Webcam webcam = null;
    private WebcamPanel webcamPanel = null;
    private JTextArea textarea = null;
    private JFileChooser jFileChooser = null;
    private JButton fileUpload = null;

    public WebcamQRCodeExample() {
        super();

        setLayout(new FlowLayout());
        setTitle("Read QR / Bar Code With Webcam");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Dimension size = WebcamResolution.VGA.getSize();

        webcam = Webcam.getWebcams().get(0);
        webcam.setViewSize(size);

        webcamPanel = new WebcamPanel(webcam);
        webcamPanel.setPreferredSize(size);

        prefs = Preferences.userRoot().node(getClass().getName());

        textarea = new JTextArea();
        textarea.setEditable(false);
        textarea.setPreferredSize(size);

        jFileChooser = new JFileChooser(prefs.get(LAST_USED_FOLDER,
                new File(".").getAbsolutePath()));
        jFileChooser.setDialogTitle("Choose a QR Code File: ");
        jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        fileUpload = new JButton("Upload");
        fileUpload.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int returnValue = jFileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File file = jFileChooser.getSelectedFile();
                    logger.info("File selected ", file.getName());
                    BufferedImage image = null;
                    try {
                        // do something
                        prefs.put(LAST_USED_FOLDER, jFileChooser.getSelectedFile().getParent());
                        image = ImageIO.read(file);
                        LuminanceSource source = new BufferedImageLuminanceSource(image);
                        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                        Result result = null;
                        try {
                            result = new MultiFormatReader().decode(bitmap);
                            if (result != null) {
                                textarea.setText(result.getText());
                            }
                        } catch (NotFoundException ee) {
                            // fall thru, it means there is no QR code in image
                        }

                    } catch (IOException ex) {
                        logger.error("Unable to read  file ", ex);
                    }
                }
            }
        });

        FileFilter imageFilter = new FileNameExtensionFilter(
                "Image files", ImageIO.getReaderFileSuffixes());
        jFileChooser.setFileFilter(imageFilter);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(webcamPanel, BorderLayout.NORTH);
        leftPanel.add(fileUpload, BorderLayout.SOUTH);
        add(leftPanel);
        add(textarea);

        pack();
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);

        executor.execute(this);
    }

    public static void main(String[] args) {
        new WebcamQRCodeExample();
    }

    @Override
    public void run() {

        int i = 0;

        do {
            Result result = null;
            BufferedImage image = null;

            if (webcam.isOpen()) {

                if ((image = webcam.getImage()) == null) {
                    continue;
                }

                LuminanceSource source = new BufferedImageLuminanceSource(image);
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

                try {
                    result = new QRCodeReader().decode(bitmap);
                } catch (NotFoundException | ChecksumException | FormatException e) {
                    // fall thru, it means there is no QR code in image
                }
            }

            if (result != null) {
                textarea.setText(result.getText());
                try {
                    MessageBox.show("Scan completed.");
                    Thread.sleep(5000);
                    textarea.setText("");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        } while (true);
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r, "example-runner");
        t.setDaemon(true);
        return t;
    }
}