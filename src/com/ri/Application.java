package com.ri;

import javax.swing.*;

/**
 * Punto de entrada a la aplicación
 */
public class Application {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                SearchWindow search = new SearchWindow();
                search.setVisible(true);
            }
        });
    }
}
