package com.ri;

import org.apache.lucene.document.Document;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.LabelAndValue;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Nikolai on 19/12/2014.
 */
public class SearchWindow extends JFrame {
    private JPanel rootPanel;
    private JMenuItem about;
    private JMenuItem exitMenuItem;
    private JMenuItem indexToolMenuItem;
    private JMenuItem loadIndexMenuItem;
    private JTextField dateField;
    private JTextField apertureField;
    private JTextField ssField;
    private JTextField isoField;
    private JTextField tagsField;
    private JRadioButton flashYesRadioButton;
    private JRadioButton flashNoRadioButton;
    private JButton searchButton;
    private JList resultList;
    private JTextPane mdPane;
    private JButton launchViewerButton;
    private JComboBox apertureCB;
    private JComboBox ssCb;
    private JComboBox flCb;
    private JComboBox flashCb;
    private JComboBox isoCb;
    private JComboBox tagsCb;
    private JLabel imageLabel;
    private JComboBox dateCB;
    private ButtonGroup flash;
    private JTextField maxResults;
    private JCheckBox pickCheckBox;
    private JComboBox orientFacetCb;
    private JComboBox apfacetCb;
    private JComboBox ssFacetCb;
    private JComboBox flFacetCb;
    private JComboBox isoFacetCb;
    private JComboBox flashFacetCb;
    private JTextField flStartField;
    private JTextField flEndField;
    private JButton clearFlashButton;
    private DefaultListModel listModel;
    private Map<String, String> currentResultsMd = new HashMap<String, String>();
    private Map<String, String> currentResultsPhoto = new HashMap<String, String>();

    private QueryEngine qEngine;
    private String currentPhoto, viewerPath;
    private boolean searchPerformed = false;

    //Dynamic CheckBox listeners
    private ActionListener apertureFacetListener;
    private ActionListener shutterSpeedFacetListener;
    private ActionListener focalLengthFacetListener;
    private ActionListener flashFacetListener;
    private ActionListener isoFacetListener;
    private ActionListener orientationFacetListener;

    public SearchWindow() {
        super("Buscador de fotografías");
        initGui();
    }

    private void initGui() {
        setContentPane(rootPanel);
        initMenu();
        initRadioButtons();
        initListeners();
        pack();
        setSize(1024, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private void initRadioButtons() {
        flash = new ButtonGroup();
        flash.add(flashNoRadioButton);
        flash.add(flashYesRadioButton);
    }

    private void initMenu() {
        JMenuBar menubar = new JMenuBar();

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
        initFacetListeners();

        exitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                dispose();
            }
        });

        about.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                JOptionPane.showMessageDialog(SearchWindow.this, "Nikolai Arsentiev\nAntonio Romero",
                        "RI Práctica 3: Lucene", JOptionPane.PLAIN_MESSAGE);
            }
        });

        indexToolMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                IndexTool mainWindow = new IndexTool();
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
                runSearchQuery();
            }
        });


        resultList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (resultList.getSelectedValue() != null) {
                    currentPhoto = resultList.getSelectedValue().toString();
                    String key = resultList.getSelectedValue().toString();
                    showPhotoMd(currentResultsMd.get(key));
                    displaySidecarImage(currentResultsMd.get(key));
                }
            }
        });

        launchViewerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                String path = currentResultsPhoto.get(currentPhoto);
                launchPhotoViewer(path);
            }
        });

        clearFlashButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                flash.clearSelection();
            }
        });
    }

    private void initFacetListeners() {
        apertureFacetListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (apfacetCb.getSelectedItem() != null) {
                    try {
                        String selectedItem = apfacetCb.getSelectedItem().toString();
                        fillInfo(qEngine.drillDown("apertureCat", selectedItem));
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        };

        shutterSpeedFacetListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ssFacetCb.getSelectedItem() != null) {
                    try {
                        String selectedItem = ssFacetCb.getSelectedItem().toString();
                        fillInfo(qEngine.drillDown("shutSpeedCat", selectedItem));
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        };

        focalLengthFacetListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (flFacetCb.getSelectedItem() != null) {
                    try {
                        String selectedItem = flFacetCb.getSelectedItem().toString();
                        fillInfo(qEngine.drillDown("focalLengthCat", selectedItem));
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        };

        isoFacetListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isoFacetCb.getSelectedItem() != null) {
                    try {
                        String selectedItem = isoFacetCb.getSelectedItem().toString();
                        fillInfo(qEngine.drillDown("isoCat", selectedItem));
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        };

        flashFacetListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (flashFacetCb.getSelectedItem() != null) {
                    try {
                        String selectedItem = flashFacetCb.getSelectedItem().toString();
                        fillInfo(qEngine.drillDown("flashCat", selectedItem));
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        };

        orientationFacetListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (orientFacetCb.getSelectedItem() != null) {
                    try {
                        String selectedItem = orientFacetCb.getSelectedItem().toString();
                        fillInfo(qEngine.drillDown("orientationCat", selectedItem));
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        };

        apfacetCb.addActionListener(apertureFacetListener);
        ssFacetCb.addActionListener(shutterSpeedFacetListener);
        flFacetCb.addActionListener(focalLengthFacetListener);
        isoFacetCb.addActionListener(isoFacetListener);
        flashFacetCb.addActionListener(flashFacetListener);
        orientFacetCb.addActionListener(orientationFacetListener);
    }

    private void runSearchQuery() {
        try {
            if (qEngine != null)
                fillInfo(qEngine.runQuery(buildQuery()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fillInfo(QueryWithFacets results) {
        resultList.clearSelection();
        listModel.clear();
        clearFacetCbs();

        System.out.println(results.getDocuments().size());
        if (results != null && results.getDocuments() != null && !results.getDocuments().isEmpty()) {
            for (Document document : results.getDocuments()) {
                currentResultsMd.put(document.get("name"), document.get("mdPath"));
                currentResultsPhoto.put(document.get("name"), document.get("path"));
                listModel.addElement(document.get("name"));
                System.out.println(document.get("name"));
            }
            updateFacetCbs(results.getFacetResults());
        }
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
        qEngine = new QueryEngine(openDirectory());
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

    private void displaySidecarImage(String path) {
        String thumbPath = org.apache.commons.io.FilenameUtils.removeExtension(path);
        thumbPath += ".jpg";
        imageLabel.setIcon(new ImageIcon(thumbPath));
    }

    private Query buildQuery() {
        Query query = new Query();
        query.aperture = apertureField.getText();
        query.shutSpeed = ssField.getText();
        query.focalLengthStart = Double.parseDouble(flStartField.getText());
        query.focalLengthEnd = Double.parseDouble(flEndField.getText());
        query.date = dateField.getText();
        query.iso = Integer.parseInt(isoField.getText());
        query.tags = tagsField.getText();

        if (flashNoRadioButton.isSelected())
            query.flash = "false";
        else if (flashYesRadioButton.isSelected())
            query.flash = "true";

        query.dateOp = dateCB.getSelectedItem().toString();
        query.apertureOp = apertureCB.getSelectedItem().toString();
        query.ssOp = ssCb.getSelectedItem().toString();
        query.flOp = flCb.getSelectedItem().toString();
        query.isoOp = isoCb.getSelectedItem().toString();
        query.flashOp = flashCb.getSelectedItem().toString();
        query.tagOp = tagsCb.getSelectedItem().toString();

        query.maxResults = Integer.parseInt(maxResults.getText());
        query.pick = pickCheckBox.isSelected();

        return query;
    }

    private void updateFacetCbs(List<FacetResult> result) {
        apfacetCb.removeActionListener(apertureFacetListener);
        ssFacetCb.removeActionListener(shutterSpeedFacetListener);
        flFacetCb.removeActionListener(focalLengthFacetListener);
        isoFacetCb.removeActionListener(isoFacetListener);
        flashFacetCb.removeActionListener(flashFacetListener);
        orientFacetCb.removeActionListener(orientationFacetListener);

        clearFacetCbs();

        System.out.println(result.size());
        for (FacetResult fr : result) {
            if (fr != null) {
                System.out.println(fr.dim);

                if (fr.dim.equals("apertureCat")) {
                    for (LabelAndValue lav : fr.labelValues)
                        apfacetCb.addItem(lav.label);
                } else if (fr.dim.equals("shutSpeedCat")) {
                    for (LabelAndValue lav : fr.labelValues)
                        ssFacetCb.addItem(lav.label);
                } else if (fr.dim.equals("focalLengthCat")) {
                    for (LabelAndValue lav : fr.labelValues)
                        flFacetCb.addItem(lav.label);
                } else if (fr.dim.equals("isoCat")) {
                    for (LabelAndValue lav : fr.labelValues)
                        isoFacetCb.addItem(lav.label);
                } else if (fr.dim.equals("flashCat")) {
                    for (LabelAndValue lav : fr.labelValues)
                        flashFacetCb.addItem(lav.label);
                } else if (fr.dim.equals("orientationCat")) {
                    for (LabelAndValue lav : fr.labelValues)
                        orientFacetCb.addItem(lav.label);
                }
            }
        }

        apfacetCb.addActionListener(apertureFacetListener);
        ssFacetCb.addActionListener(shutterSpeedFacetListener);
        flFacetCb.addActionListener(focalLengthFacetListener);
        isoFacetCb.addActionListener(isoFacetListener);
        flashFacetCb.addActionListener(flashFacetListener);
        orientFacetCb.addActionListener(orientationFacetListener);
    }

    private void clearFacetCbs() {
        apfacetCb.removeAllItems();
        ssFacetCb.removeAllItems();
        flFacetCb.removeAllItems();
        isoFacetCb.removeAllItems();
        flashFacetCb.removeAllItems();
        orientFacetCb.removeAllItems();
    }
}
