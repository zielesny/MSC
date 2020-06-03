/*
 * MSC - Molecule Set Comparator
 * Copyright (C) 2020 Jan-Mathis Hein
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.whs.ibci.msc.control;

import de.whs.ibci.msc.model.ComparisonUtility;
import de.whs.ibci.msc.model.ComparisonFeature;
import de.whs.ibci.msc.model.ComparisonResult;
import de.whs.ibci.msc.utility.GeneralUtilities;
import de.whs.ibci.msc.utility.GuiUtilities;
import de.whs.ibci.msc.utility.MSCConstants;
import de.whs.ibci.msc.view.MoleculePairDisplayerStage;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Cursor;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Labeled;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Window;
import javax.imageio.ImageIO;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.openscience.cdk.depict.DepictionGenerator;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

/**
 * Controller for a MoleculePairDisplayerStage that handles related events. This 
 * is used to display a list of molecule pairs
 *
 * @author Jan-Mathis Hein
 */
public class MoleculePairDisplayerController {
    
    //<editor-fold defaultstate="collapsed" desc="Private class variables">
    /**
     * If true the MoleculePairDisplayerStage will be maximized, 
     * if false it will not
     */
    private static boolean isMaximized = false;
    
    /**
     * The MoleculePairDisplayerStage's initial width
     */
    private static double stageWidth = Double.NaN;
    
    /**
     * The MoleculePairDisplayerStage's initial height
     */
    private static double stageHeight = Double.NaN;
    
    /**
     * The MoleculePairDisplayerStage's initial X position
     */
    private static double stageXPosition = Double.NaN;
    
    /**
     * The MoleculePairDisplayerStage's initial Y position
     */
    private static double stageYPosition = Double.NaN;

    /**
     * Directory where the last molecule list file of any 
     * MoleculePairDisplayerController was saved
     */
    private static File lastMoleculeListFileDirectory;

    /**
     * Directory where the last molecule image file of any 
     * MoleculePairDisplayerController was saved
     */
    private static File lastMoleculeImageFileDirectory;
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Private instance variables">
    /**
     * True if the last move of the comparisonResultListIterator was forward, 
     * false otherwise
     */
    private boolean wasLastIteratorStepForward;
    
    /**
     * The ComparisonFeature which was used for the comparison of the molecule 
     * pairs
     */
    private ComparisonFeature comparisonFeature;
    
    /**
     * The ComparisonResult that is currently displayed
     */
    private ComparisonResult currentComparisonResult;
    
    /**
     * Formats numerical input to fit the given pattern
     */
    private final DecimalFormat formatter = new DecimalFormat("###.##");
    
    /**
     * The lower border of the bin from which the molecule pairs will be 
     * displayed
     */
    private final double lowerBorder;
    
    /**
     * The upper border of the bin from which the molecule pairs will be 
     * displayed
     */
    private final double upperBorder;
    
    /**
     * The frequency-value of the bin from which the molecule pairs will be 
     * displayed
     */
    private final double frequencyValue;
    
    /**
     * Depicts molecules contained in IAtomContainers as BufferedImages
     */
    private DepictionGenerator depictionGenerator;
    
    /**
     * FileChooser to choose a location where images and lists of molecules will
     * be saved
     */
    private FileChooser fileChooser;
    
    /**
     * List of ComparisonResults which contain the molecule pairs
     * that will be displayed
     */
    private final List<ComparisonResult> comparisonResultList;
    
    /**
     * Iterator for iterating through the comparisonResultList
     */
    private ListIterator<ComparisonResult> comparisonResultListIterator;
    
    /**
     * MoleculePairDisplayerStage that is being controlled
     */
    private MoleculePairDisplayerStage moleculePairDisplayerStage;
    
    /**
     * Object that takes a SMILES string as input and parses it to the 
     * corresponding IAtomContainer
     */
    private final SmilesParser smilesParser = new SmilesParser(SilentChemObjectBuilder.getInstance());
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Initialize instance variables, add event handlers, set up and show the 
     * MoleculePairDisplayerStage
     *
     * @param tmpMoleculePairList list of ComparisonResults which contain
     * molecule pairs
     * @param tmpOwner specifies the owner window of the 
     * MoleculePairDisplayerStage
     * @param tmpFileChooser a FileChooser to choose a file where the molecule 
     * pair list will be saved
     * @param tmpBinNumber specifies the number of the bin from which the 
     * molecule pairs will be displayed
     * @param tmpComparisonFeature the ComparisonFeature which was used for the 
     * comparison of the molecule pairs
     * @param tmpFrequencyValue the frequency-value of the bin from which the 
     * molecule pairs will be displayed
     * @param tmpLowerBorder the lower border of the bin from which the 
     * molecule pairs will be displayed
     * @param tmpUpperBorder the upper border of the bin from which the 
     * molecule pairs will be displayed
     * @throws IllegalArgumentException if the tmpMoleculePairList is empty or 
     * null
     */
    public MoleculePairDisplayerController(
        List<ComparisonResult> tmpMoleculePairList, Window tmpOwner, 
        FileChooser tmpFileChooser, int tmpBinNumber, ComparisonFeature tmpComparisonFeature, double tmpFrequencyValue,
        double tmpLowerBorder, double tmpUpperBorder
    ) throws IllegalArgumentException {
        //<editor-fold defaultstate="collapsed" desc="Checks">
        if (tmpMoleculePairList == null || tmpMoleculePairList.isEmpty()) {
            throw new IllegalArgumentException("Invalid moleculePairList");
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Initialize instance variables">
        this.comparisonResultList = tmpMoleculePairList;
        this.comparisonResultListIterator = this.comparisonResultList.listIterator();
        this.currentComparisonResult = this.comparisonResultListIterator.next();
        this.wasLastIteratorStepForward = true;
        this.moleculePairDisplayerStage = new MoleculePairDisplayerStage();
        this.moleculePairDisplayerStage.setTitle(String.format(GeneralUtilities.getUIText("MoleculePairDisplayerStage.title.text"), tmpBinNumber));
        this.moleculePairDisplayerStage.initOwner(tmpOwner);
        this.moleculePairDisplayerStage.initModality(Modality.APPLICATION_MODAL);
        this.fileChooser = tmpFileChooser;
        this.comparisonFeature = tmpComparisonFeature;
        this.frequencyValue = tmpFrequencyValue;
        this.lowerBorder = tmpLowerBorder;
        this.upperBorder = tmpUpperBorder;
        this.depictionGenerator = new DepictionGenerator().withAtomColors().withSize(200, 200).withFillToFit().withBackgroundColor(Color.WHITE).withMargin(10);
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Set up the first pair of images">
        this.updateDisplay();
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Event handling">
        this.moleculePairDisplayerStage.getNextButton().setOnAction((ActionEvent) -> {
            if (!this.wasLastIteratorStepForward) {
                // Iterator has to move one step further because of change of direction
                this.comparisonResultListIterator.next();
            }
            this.currentComparisonResult = this.comparisonResultListIterator.next();
            this.wasLastIteratorStepForward = true;
            this.updateDisplay();
        });
        this.moleculePairDisplayerStage.getLastButton().setOnAction((ActionEvent) -> {
            boolean tmpHadNext = this.comparisonResultListIterator.hasNext();
            while (this.comparisonResultListIterator.hasNext()) {
                this.currentComparisonResult = this.comparisonResultListIterator.next();
            }
            if (tmpHadNext) {
                this.wasLastIteratorStepForward = true;
            } else {
                return;
            }
            this.updateDisplay();
        });
        this.moleculePairDisplayerStage.getPreviousButton().setOnAction((ActionEvent) -> {
            if (this.wasLastIteratorStepForward) {
                // Iterator has to move one step further because of change of direction
                this.comparisonResultListIterator.previous();
            }
            this.currentComparisonResult = this.comparisonResultListIterator.previous();
            this.wasLastIteratorStepForward = false;
            this.updateDisplay();
        });
        this.moleculePairDisplayerStage.getFirstButton().setOnAction((ActionEvent) -> {
            boolean tmpHadPrevious = this.comparisonResultListIterator.hasPrevious();
            while (this.comparisonResultListIterator.hasPrevious()) {
                this.currentComparisonResult = this.comparisonResultListIterator.previous();
            }
            if (tmpHadPrevious) {
                this.wasLastIteratorStepForward = false;
            } else {
                return;
            }
            this.updateDisplay();
        });
        this.moleculePairDisplayerStage.getPositionTextField().addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent tmpEvent) -> {
            KeyCombination tmpKeyCombination = new KeyCodeCombination(KeyCode.ENTER);
            if (tmpKeyCombination.match(tmpEvent)) {
                int tmpDesiredIndex;
                try {
                    tmpDesiredIndex = Integer.parseInt(this.moleculePairDisplayerStage.getPositionTextField().getText());
                } catch (NumberFormatException ex) {
                    // This happens if the text field is empty or the input is too large
                    if (!this.moleculePairDisplayerStage.getPositionTextField().getText().isEmpty()) {
                        GeneralUtilities.showInfoDialog(this.moleculePairDisplayerStage, GeneralUtilities.getUIText("InfoDescription.NumberInputTooHigh.text"));
                    }
                    return;
                }
                int tmpCurrentIndex = this.wasLastIteratorStepForward ? this.comparisonResultListIterator.nextIndex() : this.comparisonResultListIterator.nextIndex() + 1;
                if (tmpDesiredIndex < tmpCurrentIndex && this.comparisonResultListIterator.hasPrevious()) {
                    if (this.wasLastIteratorStepForward) {
                        // Iterator has to move one step further because of change of direction
                        this.comparisonResultListIterator.previous();
                    }
                    do {
                        this.currentComparisonResult = this.comparisonResultListIterator.previous();
                        tmpCurrentIndex--;
                    } while (tmpDesiredIndex != tmpCurrentIndex && this.comparisonResultListIterator.hasPrevious());
                    this.wasLastIteratorStepForward = false;
                } else if (tmpDesiredIndex > tmpCurrentIndex && this.comparisonResultListIterator.hasNext()) {
                    if (!this.wasLastIteratorStepForward) {
                        // Iterator has to move one step further because of change of direction
                        this.comparisonResultListIterator.next();
                    }
                    do {
                        this.currentComparisonResult = this.comparisonResultListIterator.next();
                        tmpCurrentIndex++;
                    } while (tmpDesiredIndex != tmpCurrentIndex && this.comparisonResultListIterator.hasNext());
                    this.wasLastIteratorStepForward = true;
                } else {
                    return;
                }
                this.updateDisplay();
            }
        });
        this.moleculePairDisplayerStage.getSaveLeftListButton().setOnAction((ActionEvent) -> {
            this.saveMoleculeList(true);
        });
        this.moleculePairDisplayerStage.getSaveRightListButton().setOnAction((ActionEvent) -> {
            this.saveMoleculeList(false);
        });
        this.moleculePairDisplayerStage.getSaveLeftImageButton().setOnAction((ActionEvent) -> {
            this.saveMoleculeImage(true);
        });
        this.moleculePairDisplayerStage.getSaveRightImageButton().setOnAction((ActionEvent) -> {
            this.saveMoleculeImage(false);
        });
        for (int i = 0; i < this.moleculePairDisplayerStage.getAdditionalComparisonFeatureControls().length; i++) {
            Control[] tmpControlArray = this.moleculePairDisplayerStage.getAdditionalComparisonFeatureControls()[i];
            int tmpIndex = i;
            ((CheckBox) tmpControlArray[0]).selectedProperty().addListener((ObservableValue<? extends Boolean> tmpObservableValue, Boolean tmpOldValue, Boolean tmpNewValue) -> {
                try {
                    if (tmpNewValue) {
                        boolean[] tmpCalculateFeature = new boolean[this.moleculePairDisplayerStage.getAdditionalComparisonFeatureControls().length];
                        Arrays.fill(tmpCalculateFeature, false);
                        tmpCalculateFeature[tmpIndex] = true;
                        double[][] tmpCalculations = ComparisonUtility.compareMoleculePair(
                                tmpCalculateFeature, this.currentComparisonResult.getMolecule1(), this.currentComparisonResult.getMolecule2()
                        );
                        // This can be NaN if there aren't two seperate values for each molecule
                        if (!Double.isNaN(tmpCalculations[tmpIndex][0])) {
                            if (tmpCalculations[tmpIndex][1] < 0) {
                                ((Labeled) tmpControlArray[1]).setText(
                                        "| " + this.formatter.format(tmpCalculations[tmpIndex][0])
                                        + " - (" + this.formatter.format(tmpCalculations[tmpIndex][1])
                                        + ") | = " + this.formatter.format(tmpCalculations[tmpIndex][2])
                                );
                            } else {
                                ((Labeled) tmpControlArray[1]).setText(
                                        "| " + this.formatter.format(tmpCalculations[tmpIndex][0])
                                        + " - " + this.formatter.format(tmpCalculations[tmpIndex][1])
                                        + " | = " + this.formatter.format(tmpCalculations[tmpIndex][2])
                                );
                            }
                        } else {
                            ((Labeled) tmpControlArray[1]).setText(this.formatter.format(tmpCalculations[tmpIndex][2]));
                        }
                    } else {
                        ((Labeled) tmpControlArray[1]).setText("");
                    }
                } catch (CDKException CDKException) {
                    ((Labeled) tmpControlArray[1]).setText("");
                }
            });
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Set up keyboard shortcuts">
        this.moleculePairDisplayerStage.getScene().addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent tmpEvent) -> {
            KeyCombination tmpKeyCombination = new KeyCodeCombination(KeyCode.LEFT);
            if (!this.moleculePairDisplayerStage.getPreviousButton().isDisabled() && tmpKeyCombination.match(tmpEvent)) {
                this.moleculePairDisplayerStage.getPreviousButton().fire();
                tmpEvent.consume();
            }
            tmpKeyCombination = new KeyCodeCombination(KeyCode.RIGHT);
            if (!this.moleculePairDisplayerStage.getNextButton().isDisabled() && tmpKeyCombination.match(tmpEvent)) {
                this.moleculePairDisplayerStage.getNextButton().fire();
                tmpEvent.consume();
            }
        });
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Set up TextFormatters">
        this.moleculePairDisplayerStage.getPositionTextField().setTextFormatter(GeneralUtilities.getUnsignedIntegerTextFormatter());
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Set up and show the MoleculePairDisplayerStage">
        this.moleculePairDisplayerStage.setMinWidth(MSCConstants.MINIMAL_MOLECULE_DISPLAYER_WIDTH);
        this.moleculePairDisplayerStage.setWidth(
            Double.isNaN(MoleculePairDisplayerController.stageWidth) ? 
            MSCConstants.MINIMAL_MOLECULE_DISPLAYER_WIDTH : 
            MoleculePairDisplayerController.stageWidth
        );
        this.moleculePairDisplayerStage.setMinHeight(MSCConstants.MINIMAL_MOLECULE_DISPLAYER_HEIGHT);
        this.moleculePairDisplayerStage.setHeight(
            Double.isNaN(MoleculePairDisplayerController.stageHeight) ? 
            MSCConstants.MINIMAL_MOLECULE_DISPLAYER_HEIGHT : 
            MoleculePairDisplayerController.stageHeight
        );
        if (!Double.isNaN(MoleculePairDisplayerController.stageXPosition)) {
            this.moleculePairDisplayerStage.setX(MoleculePairDisplayerController.stageXPosition);
        }
        if (!Double.isNaN(MoleculePairDisplayerController.stageXPosition)) {
            this.moleculePairDisplayerStage.setY(MoleculePairDisplayerController.stageYPosition);
        }
        this.moleculePairDisplayerStage.setMaximized(MoleculePairDisplayerController.isMaximized);
        this.moleculePairDisplayerStage.showAndWait();
        MoleculePairDisplayerController.stageWidth = this.moleculePairDisplayerStage.getWidth();
        MoleculePairDisplayerController.stageHeight = this.moleculePairDisplayerStage.getHeight();
        MoleculePairDisplayerController.stageXPosition = this.moleculePairDisplayerStage.getX();
        MoleculePairDisplayerController.stageYPosition = this.moleculePairDisplayerStage.getY();
        MoleculePairDisplayerController.isMaximized = this.moleculePairDisplayerStage.isMaximized();
        //</editor-fold>
    }
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Private methods">
    /**
     * Save one of the molecule images in a file that the user chooses.
     * 
     * @param tmpSaveLeft if true the image of the left molecule will be saved, 
     * if false the image of the right molecule will be saved
     */
    private void saveMoleculeImage(boolean tmpSaveLeft) {
        try {
            this.fileChooser.getExtensionFilters().clear();
            this.fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter(GeneralUtilities.getUIText("FileExtensionFilter.All.text"), "*.*"),
                    new FileChooser.ExtensionFilter(GeneralUtilities.getUIText("FileExtensionFilter.pdf.text"), "*.pdf"),
                    new FileChooser.ExtensionFilter(GeneralUtilities.getUIText("FileExtensionFilter.png.text"), "*.png"),
                    new FileChooser.ExtensionFilter(GeneralUtilities.getUIText("FileExtensionFilter.jpeg.text"), "*.jpeg"),
                    new FileChooser.ExtensionFilter(GeneralUtilities.getUIText("FileExtensionFilter.svg.text"), "*.svg")
            );
            if (!UserPreferences.getInstance().getImageDir().isEmpty() && new File(UserPreferences.getInstance().getImageDir()).isDirectory()) {
                this.fileChooser.setInitialDirectory(new File(UserPreferences.getInstance().getImageDir()));
            } else {
                this.fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
            }
            synchronized (MoleculePairDisplayerController.class) {
                if (MoleculePairDisplayerController.lastMoleculeImageFileDirectory != null && MoleculePairDisplayerController.lastMoleculeImageFileDirectory.isDirectory()) {
                    this.fileChooser.setInitialDirectory(MoleculePairDisplayerController.lastMoleculeImageFileDirectory);
                }
            }
            this.fileChooser.setTitle(GeneralUtilities.getUIText("FileChooserTitle.MoleculeList.text"));
            final File tmpFile = this.fileChooser.showSaveDialog(this.moleculePairDisplayerStage);
            if (tmpFile == null) {
                return;
            }
            boolean tmpIsNewFile = tmpFile.createNewFile();
            if (!tmpFile.canWrite()) {
                if (tmpIsNewFile) {
                    Files.delete(tmpFile.toPath());
                }
                return;
            }
            synchronized (MoleculePairDisplayerController.class) {
                MoleculePairDisplayerController.lastMoleculeImageFileDirectory = tmpFile.getParentFile();
            }
            BufferedImage tmpBufferedImage;
            // IMPORTANT: Setting tmpSideLength too high will cause the program to throw an exception
            double tmpSideLength = 9800 * UserPreferences.getInstance().getImageQuality() + 200;
            DepictionGenerator tmpDepictionGenerator = new DepictionGenerator().withAtomColors().withSize(tmpSideLength, tmpSideLength).withFillToFit().withBackgroundColor(Color.WHITE).withMargin(tmpSideLength / 20);
            if (tmpSaveLeft) {
                tmpBufferedImage = tmpDepictionGenerator.depict(this.smilesParser.parseSmiles(this.currentComparisonResult.getMolecule1())).toImg();
            } else {
                tmpBufferedImage = tmpDepictionGenerator.depict(this.smilesParser.parseSmiles(this.currentComparisonResult.getMolecule2())).toImg();
            }
            final String tmpFormat = tmpFile.getAbsolutePath().substring(tmpFile.getAbsolutePath().lastIndexOf(".")).toLowerCase();
            Task<Void> tmpTask = new Task<Void>() {
                @Override 
                protected Void call() throws Exception {
                    try {
                        switch (tmpFormat) {
                            case ".png":
                                ImageIO.write(tmpBufferedImage, "PNG", tmpFile);
                                break;
                            case ".jpg":
                            case ".jpeg":
                                BufferedImage tmpImage = new BufferedImage(tmpBufferedImage.getWidth(), tmpBufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
                                // Change the color model of the BufferedImage for the jpeg format
                                Graphics2D tmpGraphics2D = tmpImage.createGraphics();
                                tmpGraphics2D.drawImage(tmpBufferedImage, 0, 0, null);
                                tmpGraphics2D.dispose();
                                ImageIO.write(tmpImage, "JPG", tmpFile);
                                break;
                            case ".svg":
                                DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
                                Document document = domImpl.createDocument("http://www.w3.org/2000/svg", "svg", null);
                                SVGGraphics2D tmpSvgGenerator = new SVGGraphics2D(document);
                                tmpSvgGenerator.drawImage(tmpBufferedImage, AffineTransform.getScaleInstance(100 / tmpSideLength, 100 / tmpSideLength), null);
                                try (BufferedWriter tmpWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmpFile)))) {
                                    tmpSvgGenerator.stream(tmpWriter);
                                }
                                break;
                            case ".pdf":
                                try (PDDocument tmpDocument = new PDDocument()) {
                                    PDPage tmpPage = new PDPage();
                                    tmpDocument.addPage(tmpPage);
                                    PDImageXObject tmpPDImage = LosslessFactory.createFromImage(tmpDocument, tmpBufferedImage);
                                    try (PDPageContentStream tmpStream = new PDPageContentStream(tmpDocument, tmpPage)) {
                                        float tmpWidth = tmpBufferedImage.getWidth();
                                        float tmpHeight = tmpBufferedImage.getHeight();
                                        tmpPage.setMediaBox(new PDRectangle(tmpWidth, tmpHeight));
                                        tmpStream.drawImage(tmpPDImage, 0, 0, tmpWidth, tmpHeight);
                                    }
                                    tmpDocument.save(tmpFile);
                                }
                                break;
                            default:
                                Platform.runLater(() -> {
                                    GeneralUtilities.showInfoDialog(MoleculePairDisplayerController.this.moleculePairDisplayerStage, GeneralUtilities.getUIText("InfoDescription.FileFormatNotSupported.text"));
                                });
                        }
                    } catch (IOException | DOMException ex) {
                        Platform.runLater(() -> {
                            MoleculePairDisplayerController.this.moleculePairDisplayerStage.getScene().setCursor(Cursor.DEFAULT);
                            GeneralUtilities.showErrorDialog(MoleculePairDisplayerController.this.moleculePairDisplayerStage, ex.toString(), "MoleculePairDisplayerController.saveMoleculeImage()");
                            GeneralUtilities.logException(Level.WARNING, ex);
                        });
                    } finally {
                        Platform.runLater(() -> {
                            MoleculePairDisplayerController.this.moleculePairDisplayerStage.getScene().setCursor(Cursor.DEFAULT);
                        });
                    }
                    return null;
                }
            };
            Thread tmpBackgroundThread = new Thread(tmpTask);
            this.moleculePairDisplayerStage.getScene().setCursor(Cursor.WAIT);
            tmpBackgroundThread.start();
        } catch (StringIndexOutOfBoundsException ex) {
                GeneralUtilities.showInfoDialog(this.moleculePairDisplayerStage, GeneralUtilities.getUIText("InfoDescription.FileFormatNotSupported.text"));
        } catch (IOException | CDKException ex) {
            GeneralUtilities.showErrorDialog(
                this.moleculePairDisplayerStage, ex.toString(), 
                "MoleculePairDisplayerController.saveMoleculeImage()"
            );
            GeneralUtilities.logException(Level.WARNING, ex);
        }
    }
    
    /**
     * Save either the left or the right list of molecules in a file that the 
     * user chooses.
     * 
     * @param tmpSaveLeft if true the left list of molecules will be saved, 
     * if false the right list of molecules will be saved
     */
    private void saveMoleculeList(boolean tmpSaveLeft) {
        try {
            this.fileChooser.getExtensionFilters().clear(); 
            this.fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter(GeneralUtilities.getUIText("FileExtensionFilter.All.text"), "*.*"),
                    new FileChooser.ExtensionFilter(GeneralUtilities.getUIText("FileExtensionFilter.Text.text"), "*.txt")
            );
            if (!UserPreferences.getInstance().getMoleculeListDir().isEmpty() && new File(UserPreferences.getInstance().getMoleculeListDir()).isDirectory()) {
                this.fileChooser.setInitialDirectory(new File(UserPreferences.getInstance().getMoleculeListDir()));
            } else {
                this.fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
            }
            synchronized (MoleculePairDisplayerController.class) {
                if (MoleculePairDisplayerController.lastMoleculeListFileDirectory != null && MoleculePairDisplayerController.lastMoleculeListFileDirectory.isDirectory()) {
                    this.fileChooser.setInitialDirectory(MoleculePairDisplayerController.lastMoleculeListFileDirectory);
                }
            }
            this.fileChooser.setTitle(GeneralUtilities.getUIText("FileChooserTitle.MoleculeList.text"));
            final File tmpFile = this.fileChooser.showSaveDialog(this.moleculePairDisplayerStage);
            if (tmpFile == null) {
                return;
            }
            boolean tmpIsNewFile = tmpFile.createNewFile();
            if (!tmpFile.canWrite()) {
                if (tmpIsNewFile) {
                    Files.delete(tmpFile.toPath());
                }
                return;
            }
            synchronized (MoleculePairDisplayerController.class) {
                MoleculePairDisplayerController.lastMoleculeListFileDirectory = tmpFile.getParentFile();
            }
            Task<Void> tmpTask = new Task<Void>() {
                @Override 
                protected Void call() throws Exception {
                    try (BufferedWriter tmpMoleculeWriter = new BufferedWriter(new FileWriter(tmpFile))) {
                        Iterator<ComparisonResult> tmpIterator = MoleculePairDisplayerController.this.comparisonResultList.iterator();
                        while (tmpIterator.hasNext()) {
                            ComparisonResult tmpResult = tmpIterator.next();
                            tmpMoleculeWriter.write(tmpSaveLeft ? tmpResult.getMolecule1().strip() : tmpResult.getMolecule2().strip());
                            tmpMoleculeWriter.newLine();
                        }
                    } catch (IOException ex) {
                        Platform.runLater(() -> {
                            MoleculePairDisplayerController.this.moleculePairDisplayerStage.getScene().setCursor(Cursor.DEFAULT);
                        });
                        try {
                            Files.delete(tmpFile.toPath());
                        } catch (IOException tmpEx) {
                        }
                        Platform.runLater(() -> {
                            GeneralUtilities.showErrorDialog(
                                MoleculePairDisplayerController.this.moleculePairDisplayerStage, ex.toString(), 
                                "MoleculePairDisplayerController.saveMoleculeList()"
                            );
                            GeneralUtilities.logException(Level.WARNING, ex);
                        });
                    } finally {
                        Platform.runLater(() -> {
                            MoleculePairDisplayerController.this.moleculePairDisplayerStage.getScene().setCursor(Cursor.DEFAULT);
                        });
                    }
                    return null;
                }
            };
            Thread tmpBackgroundThread = new Thread(tmpTask);
            this.moleculePairDisplayerStage.getScene().setCursor(Cursor.WAIT);
            tmpBackgroundThread.start();
        } catch (IOException ex) {
            GeneralUtilities.showErrorDialog(
                this.moleculePairDisplayerStage, ex.toString(), 
                "MoleculePairDisplayerController.saveMoleculeList()"
            );
            GeneralUtilities.logException(Level.WARNING, ex);
        }
    }
    
    /**
     * Update the MoleculePairDisplayerStage
     */
    private void updateDisplay() {
        DecimalFormat tmpFormat = new DecimalFormat("#.####");
        try {
            BufferedImage tmpBufferedImage;
            tmpBufferedImage = this.depictionGenerator.depict(this.smilesParser.parseSmiles(this.currentComparisonResult.getMolecule1())).toImg();
            this.moleculePairDisplayerStage.getLeftMoleculeImageView().setImage(SwingFXUtils.toFXImage(tmpBufferedImage, null));
            tmpBufferedImage = this.depictionGenerator.depict(this.smilesParser.parseSmiles(this.currentComparisonResult.getMolecule2())).toImg();
            this.moleculePairDisplayerStage.getRightMoleculeImageView().setImage(SwingFXUtils.toFXImage(tmpBufferedImage, null));
            this.moleculePairDisplayerStage.getInfoLabel().setText(String.format(
                    GeneralUtilities.getUIText("MoleculePairDisplayerStage.infoLabel.text"), 
                    this.comparisonFeature, this.comparisonResultList.size(), 
                    tmpFormat.format(this.frequencyValue),
                    tmpFormat.format(this.lowerBorder), tmpFormat.format(this.upperBorder),
                    this.currentComparisonResult.getMolecule1().strip(), this.currentComparisonResult.getMolecule2().strip(),
                    this.currentComparisonResult.getSimilarities()[this.comparisonFeature.getFeatureNumber()]
            ));
            this.moleculePairDisplayerStage.getPositionTextField().setText(Integer.toString(
                    this.wasLastIteratorStepForward ? 
                    this.comparisonResultListIterator.nextIndex() : 
                    this.comparisonResultListIterator.nextIndex() + 1)
            );
        } catch (CDKException ex) {
            this.moleculePairDisplayerStage.getRightMoleculeImageView().setImage(GuiUtilities.WHITE_SQUARE);
            this.moleculePairDisplayerStage.getLeftMoleculeImageView().setImage(GuiUtilities.WHITE_SQUARE);
            this.moleculePairDisplayerStage.getInfoLabel().setText(String.format(
                    GeneralUtilities.getUIText("MoleculePairDisplayerStage.infoLabel.text"), 
                    this.comparisonFeature, this.comparisonResultList.size(), 
                    tmpFormat.format(this.frequencyValue),
                    tmpFormat.format(this.lowerBorder), tmpFormat.format(this.upperBorder),
                    "", "", ""
            ));
            this.moleculePairDisplayerStage.getPositionTextField().setText("");
            GeneralUtilities.logException(Level.WARNING, ex);
        }
        try {
            boolean[] tmpCalculateFeature = new boolean[this.moleculePairDisplayerStage.getAdditionalComparisonFeatureControls().length];
            for (int i = 0; i < this.moleculePairDisplayerStage.getAdditionalComparisonFeatureControls().length; i++) {
                tmpCalculateFeature[i] = ((CheckBox) this.moleculePairDisplayerStage.getAdditionalComparisonFeatureControls()[i][0]).isSelected();
            }
            double[][] tmpCalculations = ComparisonUtility.compareMoleculePair(
                tmpCalculateFeature, this.currentComparisonResult.getMolecule1(), this.currentComparisonResult.getMolecule2()
            );
            for (int i = 0; i < this.moleculePairDisplayerStage.getAdditionalComparisonFeatureControls().length; i++) {
                if (tmpCalculateFeature[i]) {
                    // This can be NaN if there aren't two seperate values for each molecule
                    if (!Double.isNaN(tmpCalculations[i][0])) {
                        if (tmpCalculations[i][1] < 0) {
                            ((Labeled) this.moleculePairDisplayerStage.getAdditionalComparisonFeatureControls()[i][1]).setText(
                                "| " + this.formatter.format(tmpCalculations[i][0]) + 
                                " - (" + this.formatter.format(tmpCalculations[i][1]) + 
                                ") | = " + this.formatter.format(tmpCalculations[i][2])
                            );
                        } else {
                            ((Labeled) this.moleculePairDisplayerStage.getAdditionalComparisonFeatureControls()[i][1]).setText(
                                "| " + this.formatter.format(tmpCalculations[i][0]) + 
                                " - " + this.formatter.format(tmpCalculations[i][1]) + 
                                " | = " + this.formatter.format(tmpCalculations[i][2])
                            );
                        }
                    } else {
                        ((Labeled) this.moleculePairDisplayerStage.getAdditionalComparisonFeatureControls()[i][1]).setText(this.formatter.format(tmpCalculations[i][2]));
                    }
                } else {
                    ((Labeled) this.moleculePairDisplayerStage.getAdditionalComparisonFeatureControls()[i][1]).setText("");
                }
            }
        } catch (CDKException ex) {
            for (Control[] additionalComparisonFeatureControl : this.moleculePairDisplayerStage.getAdditionalComparisonFeatureControls()) {
                ((Labeled) additionalComparisonFeatureControl[1]).setText("");
            }
        }
        this.moleculePairDisplayerStage.getNextButton().setDisable(!this.comparisonResultListIterator.hasNext());
        this.moleculePairDisplayerStage.getLastButton().setDisable(!this.comparisonResultListIterator.hasNext());
        this.moleculePairDisplayerStage.getPreviousButton().setDisable(!this.comparisonResultListIterator.hasPrevious());
        this.moleculePairDisplayerStage.getFirstButton().setDisable(!this.comparisonResultListIterator.hasPrevious());
    }
    //</editor-fold>
    
}
