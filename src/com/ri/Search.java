package com.ri;

import org.apache.lucene.document.Document;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.StringContent;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Nikolai on 19/12/2014.
 */
public class Search extends JFrame {
    private JPanel rootPanel;
    private JMenuItem about;
    private JMenuItem exitMenuItem;
    private JMenuItem indexToolMenuItem;
    private JMenuItem loadIndexMenuItem;
    private JTextField dateField;
    private JTextField apertureField;
    private JTextField ssField;
    private JTextField flField;
    private JTextField isoField;
    private JTextField tagsField;
    private JRadioButton flashYesRadioButton;
    private JRadioButton flashNoRadioButton;
    private JRadioButton horizontalRadioButton;
    private JRadioButton verticalRadioButton;
    private JButton searchButton;
    private JList resultList;
    private JTextPane mdPane;
    private JButton launchViewerButton;
    private JComboBox apertureCB;
    private JComboBox ssCb;
    private JComboBox flCb;
    private JComboBox flashCb;
    private JComboBox isoCb;
    private JComboBox orientationCb;
    private JComboBox tagsCb;
    private DefaultListModel listModel;
    private Map<String, String> currentResultsMd = new HashMap<String, String>();
    private Map<String, String> currentResultsPhoto = new HashMap<String, String>();

    private QueryEngine qEngine;
    private String currentPhoto, viewerPath;

    public Search() {
        super("Buscador de fotografías");
        initGui();
        resultList.addMouseListener(new MouseAdapter() {
        });
    }

    private void initGui() {
        setContentPane(rootPanel);
        initMenu();
        initRadioButtons();
        initListeners();
        pack();
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private void initRadioButtons() {
        flashNoRadioButton.setSelected(true);
        horizontalRadioButton.setSelected(true);

        ButtonGroup flash = new ButtonGroup();
        flash.add(flashNoRadioButton);
        flash.add(flashYesRadioButton);

        ButtonGroup orientation = new ButtonGroup();
        orientation.add(horizontalRadioButton);
        orientation.add(verticalRadioButton);
    }

    private void initMenu() {
        JMenuBar menubar  = new JMenuBar();

        JMenu file = new JMenu("Archivo");
        about = new JMenuItem("Acerca de...");
        file.setMnemonic(KeyEvent.VK_F);
        about.setMnemonic(KeyEvent.VK_A);

        exitMenuItem = new JMenuItem("Salir");
        indexToolMenuItem = new JMenuItem("Herramienta del índice");
        loadIndexMenuItem = new JMenuItem("Cargar el índice");

        exitMenuItem.setMnemonic(KeyEvent.VK_E);
        indexToolMenuItem.setMnemonic(KeyEvent.VK_T);
        loadIndexMenuItem.setMnemonic(KeyEvent.VK_L);

        exitMenuItem.setToolTipText("Salir del programa");
        indexToolMenuItem.setToolTipText("Cargar las fotos DNG originales. Cargar y guardad metadatos en XML. Crear el índice Lucene");
        loadIndexMenuItem.setToolTipText("Cargar el índice Lucene");


        file.add(indexToolMenuItem);
        file.add(loadIndexMenuItem);
        file.add(exitMenuItem);
        menubar.add(file);
        menubar.add(about);
        setJMenuBar(menubar);
    }

    private void initListeners() {
        exitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                dispose();
            }
        });

        about.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                JOptionPane.showMessageDialog(Search.this, "Nikolai Arsentiev\nAntonio Romero",
                        "RI: Lucene", JOptionPane.PLAIN_MESSAGE);
            }
        });

        indexToolMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                MainWindow mainWindow = new MainWindow();
                mainWindow.setVisible(true);
            }
        });

        loadIndexMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                loadIndex();
            }
        });

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                try {
                    List<Document> results = qEngine.runQuery(buildQuery());
                    listModel.clear();

                    if(results != null && !results.isEmpty()) {
                        for (Document document : results) {
                            currentResultsMd.put(document.get("name"), document.get("mdPath"));
                            currentResultsPhoto.put(document.get("name"), document.get("path"));
                            listModel.addElement(document.get("name"));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        resultList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                currentPhoto = resultList.getSelectedValue().toString();
                String key = resultList.getSelectedValue().toString();
                showPhotoMd(currentResultsMd.get(key));
            }
        });

        launchViewerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                String path = currentResultsPhoto.get(currentPhoto);
                launchPhotoViewer(path);
            }
        });
    }

    private void showPhotoMd(String path) {
        Photo photo = new Photo();
        photo.readFromXml(path);
        String info = "Fecha de la creación: " + photo.creationDate + "\nVelocidad de obturación: " +
                photo.shutterSpeed + "\nApertura: " + photo.aperture + "\nLongitud focal: " + photo.focalLength +
                "\nISO: " + photo.iso + "\nFlash: " + photo.flashFired + "\nOrientación: " + photo.orientation +
                "\nPalabras clave: " + photo.tags;
        mdPane.setText(info);
    }

    private void launchPhotoViewer(String path) {
        if (viewerPath == null)
            viewerPath = openFile().toString();
        Runtime runTime = Runtime.getRuntime();
        try {
            Process process = runTime.exec(viewerPath + " " + path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadIndex() {
        try {
            qEngine = new QueryEngine(openDirectory());
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private File openFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("Elegir el visor de archivos DNG");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            return chooser.getSelectedFile();
        else return null;
    }

    private void createUIComponents() {
        listModel = new DefaultListModel();
        listModel.addElement("");
        resultList = new JList(listModel);
    }

    private Query buildQuery() {
        Query query = new Query();
        query.aperture = apertureField.getText();
        query.shutSpeed = ssField.getText();
        query.focalLength = Double.parseDouble(flField.getText());
        query.date = dateField.getText();
        query.iso = Integer.parseInt(isoField.getText());
        query.flash = flashNoRadioButton.isSelected() ? "false" : "true";
        query.orientation = horizontalRadioButton.isSelected() ? "horizontal" : "vertical";
        query.tags = tagsField.getText();

        query.apertureOp = apertureCB.getSelectedItem().toString();
        query.ssOp = ssCb.getSelectedItem().toString();
        query.flOp = flCb.getSelectedItem().toString();
        query.isoOp = isoCb.getSelectedItem().toString();
        query.flashOp = flashCb.getSelectedItem().toString();
        query.orientOp = orientationCb.getSelectedItem().toString();
        query.tagOp = tagsCb.getSelectedItem().toString();

        return query;
    }
}
