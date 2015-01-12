package com.ri;

import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Herramienta de gestión del índice
 */
public class IndexTool extends JFrame {
    private List<File> files = new ArrayList<File>();
    private List<Photo> photos = new ArrayList<Photo>();

    private JMenuItem loadFromDngMenuItem;
    private JMenuItem saveToXmlMenuItem;
    private JMenuItem loadFromXmlMenuItem;
    private JMenuItem closeCollectionMenuItem;
    private JMenuItem exitMenuItem;
    private JPanel rootPanel;
    private JButton indexButton;
    private JLabel infoLabel;
    private JProgressBar loadingBar;

    public IndexTool() {
        super("Herramienta del índice");
        initGui();
    }

    private void initGui() {
        setContentPane(rootPanel);
        initMenu();
        initListeners();
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    private void initMenu() {
        JMenuBar menubar = new JMenuBar();
        JMenu collection = new JMenu("Colección");

        collection.setMnemonic(KeyEvent.VK_C);

        loadFromDngMenuItem = new JMenuItem("Importar DNG...");
        saveToXmlMenuItem = new JMenuItem("Guardar en XML...");
        loadFromXmlMenuItem = new JMenuItem("Cargar desde XML...");
        closeCollectionMenuItem = new JMenuItem("Cerrar Colección");
        exitMenuItem = new JMenuItem("Salir");

        loadFromDngMenuItem.setMnemonic(KeyEvent.VK_I);
        saveToXmlMenuItem.setMnemonic(KeyEvent.VK_S);
        loadFromXmlMenuItem.setMnemonic(KeyEvent.VK_L);
        closeCollectionMenuItem.setMnemonic(KeyEvent.VK_X);
        exitMenuItem.setMnemonic(KeyEvent.VK_E);

        loadFromDngMenuItem.setToolTipText("Importar un árbol de directorios con fotos en formato DNG");
        saveToXmlMenuItem.setToolTipText("Guardar la colección cargada en archivos XML");
        loadFromXmlMenuItem.setToolTipText("Cargar la colección a partir de un directorio con archivos XML");
        closeCollectionMenuItem.setToolTipText("Cerrar la colección abierta liberando memoria");
        exitMenuItem.setToolTipText("Salir del programa");

        collection.add(loadFromDngMenuItem);
        collection.add(saveToXmlMenuItem);
        collection.add(loadFromXmlMenuItem);
        collection.add(closeCollectionMenuItem);
        collection.addSeparator();
        collection.add(exitMenuItem);
        menubar.add(collection);
        setJMenuBar(menubar);
    }

    private void initListeners() {
        loadFromDngMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                importPhotos();
            }
        });

        loadFromXmlMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                loadFromXml();
            }
        });

        saveToXmlMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                saveToXml();
            }
        });

        closeCollectionMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                photos.clear();
                files.clear();
            }
        });

        exitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                dispose();
            }
        });

        indexButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                indexPhotos();
            }
        });
    }

    private void indexPhotos() {
        try {
            indexButton.setEnabled(false);
            Index index = new Index(openDirectory().getPath());
            index.indexDocs(photos);
            indexButton.setEnabled(true);
            infoLabel.setText("Índice creado.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveToXml() {
        indexButton.setEnabled(false);
        infoLabel.setText("");
        infoLabel.setEnabled(true);
        infoLabel.setForeground(Color.decode("#006699"));
        loadingBar.setValue(0);

        XmlSaver xs = new XmlSaver(openDirectory().getPath(), infoLabel);
        xs.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String name = evt.getPropertyName();
                if (name.equals("progress")) {
                    int progress = (Integer) evt.getNewValue();
                    loadingBar.setValue(progress);
                } else if (name.equals("state")) {
                    SwingWorker.StateValue state = (SwingWorker.StateValue) evt.getNewValue();
                    switch (state) {
                        case DONE:
                            indexButton.setEnabled(true);
                            infoLabel.setText(files.size() + " archivos XML guardados.");
                            break;
                    }
                }
            }
        });
        xs.execute();
    }

    private void loadFromXml() {
        indexButton.setEnabled(false);
        infoLabel.setText("");
        infoLabel.setEnabled(true);
        infoLabel.setForeground(Color.decode("#006699"));
        loadingBar.setValue(0);

        XmlLoader xl = new XmlLoader(openDirectory(), infoLabel);
        xl.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String name = evt.getPropertyName();
                if (name.equals("progress")) {
                    int progress = (Integer) evt.getNewValue();
                    loadingBar.setValue(progress);
                } else if (name.equals("state")) {
                    SwingWorker.StateValue state = (SwingWorker.StateValue) evt.getNewValue();
                    switch (state) {
                        case DONE:
                            indexButton.setEnabled(true);
                            infoLabel.setText(files.size() + " Fotografías cargadas.");
                            break;
                    }
                }
            }
        });
        xl.execute();
    }

    private void importPhotos() {
        indexButton.setEnabled(false);
        infoLabel.setText("");
        infoLabel.setEnabled(true);
        infoLabel.setForeground(Color.decode("#006699"));
        loadingBar.setValue(0);

        ProgressWorker pw = new ProgressWorker(openDirectory(), infoLabel);
        pw.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String name = evt.getPropertyName();
                if (name.equals("progress")) {
                    int progress = (Integer) evt.getNewValue();
                    loadingBar.setValue(progress);
                } else if (name.equals("state")) {
                    SwingWorker.StateValue state = (SwingWorker.StateValue) evt.getNewValue();
                    switch (state) {
                        case DONE:
                            indexButton.setEnabled(true);
                            infoLabel.setText(files.size() + " Fotografías cargadas.");
                            break;
                    }
                }
            }
        });
        pw.execute();
    }

    private File openDirectory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("Importar directorio DNG");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            return chooser.getSelectedFile();
        else return null;
    }

    public class XmlSaver extends SwingWorker<Void, String> {
        String path;
        JLabel infoLabel;

        public XmlSaver(String path, JLabel infoLabel) {
            this.path = path;
            this.infoLabel = infoLabel;
        }

        @Override
        protected Void doInBackground() throws Exception {
            saveToXml(path);
            return null;
        }

        @Override
        protected void process(List<String> chunks) {
            for (String chunk : chunks)
                infoLabel.setText(chunk);
        }

        @Override
        protected void done() {
            indexButton.setEnabled(true);
            loadingBar.setValue(100);
        }

        private void saveToXml(String path) {
            if (photos != null) {
                for (Photo photo : photos) {
                    publish("Guardando " + photo.fileName);
                    photo.saveToXml(path);
                }
            }
        }
    }

    public class XmlLoader extends SwingWorker<Void, String> {
        File directory;
        JLabel infoLabel;

        public XmlLoader(File directory, JLabel infoLabel) {
            this.directory = directory;
            this.infoLabel = infoLabel;
        }

        @Override
        protected Void doInBackground() throws Exception {
            loadFromXml(directory);
            return null;
        }

        @Override
        protected void process(List<String> chunks) {
            for (String chunk : chunks)
                infoLabel.setText(chunk);
        }

        @Override
        protected void done() {
            indexButton.setEnabled(true);
            loadingBar.setValue(100);
        }

        private void loadFromXml(File directory) {
            files.clear();
            photos.clear();

            if (directory != null) {
                addXmlFiles(directory);

                float count = 1;
                for (File file : files) {
                    Photo photo = new Photo();
                    photo.readFromXml(file.getPath());
                    photos.add(photo);

                    publish("Procesando " + file.getName());
                    setProgress((int) ((count / files.size()) * 100));
                    count++;
                }
            }
        }

        @SuppressWarnings("ConstantConditions")
        private void addXmlFiles(File directory) {
            if (directory != null) {
                for (File fileEntry : directory.listFiles()) {
                    if (fileEntry.isDirectory())
                        addXmlFiles(fileEntry);
                    else if (FilenameUtils.getExtension(fileEntry.getName()).matches("(?i)xml"))
                        files.add(fileEntry);
                }
            } else infoLabel.setText("Error al abrir el directorio.");
        }
    }

    public class ProgressWorker extends SwingWorker<Void, String> {
        File directory;
        JLabel label;

        public ProgressWorker(File rootDir, JLabel infoLabel) {
            directory = rootDir;
            label = infoLabel;
        }

        @Override
        protected void process(List<String> chunks) {
            for (String chunk : chunks)
                label.setText(chunk);
        }

        @Override
        protected void done() {
            indexButton.setEnabled(true);
            loadingBar.setValue(100);
        }

        @Override
        protected Void doInBackground() throws Exception {
            importFromDng();
            return null;
        }

        private void importFromDng() {
            files.clear();
            photos.clear();

            if (directory != null) {
                addDngFiles(directory);

                float count = 1;
                for (File file : files) {
                    photos.add(new Photo(file));

                    publish("Procesando " + file.getName());
                    setProgress((int) ((count / files.size()) * 100));
                    count++;
                }
            }
        }

        @SuppressWarnings("ConstantConditions")
        private void addDngFiles(File directory) {
            if (directory != null) {
                for (File fileEntry : directory.listFiles()) {
                    if (fileEntry.isDirectory())
                        addDngFiles(fileEntry);
                    else if (FilenameUtils.getExtension(fileEntry.getName()).matches("(?i)dng"))
                        files.add(fileEntry);
                }
            } else infoLabel.setText("Error al abrir el directorio.");
        }
    }
}
