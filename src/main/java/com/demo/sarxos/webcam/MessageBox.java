package com.demo.sarxos.webcam;

import javax.swing.*;

public class MessageBox {
    private static void info(String message) {
        info(message, "");
    }

    private static void info(String message, String title) {
        JOptionPane.showMessageDialog(null, message, "[Default title]",
                JOptionPane.INFORMATION_MESSAGE);
    }

    static void show(String message) {
        info(message);
    }

}
