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

import de.whs.ibci.msc.model.ComparisonResult;
import de.whs.ibci.msc.model.JobTaskManager;
import de.whs.ibci.msc.utility.GeneralUtilities;
import de.whs.ibci.msc.model.MSCInputException;
import de.whs.ibci.msc.view.ChartConfigurationDialog;
import de.whs.ibci.msc.view.InputTab;
import de.whs.ibci.msc.view.MainView;
import de.whs.ibci.msc.view.OutputTab;
import de.whs.ibci.msc.model.HistogramDataManager;
import de.whs.ibci.msc.model.InputType;
import de.whs.ibci.msc.model.ComparisonFeature;
import de.whs.ibci.msc.model.HistogramData;
import de.whs.ibci.msc.utility.ResultFormattingUtility;
import de.whs.ibci.msc.view.BinBorderConfigurationBox;
import de.whs.ibci.msc.view.BinBorderConfigurationDialog;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.transform.Transform;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

/**
 * Manages the ChartConfigurationDialog, the In and OutputTab as well as 
 * related events. Also directly communicates with the JobTaskManager and 
 * converts the resulting HistogramData objects in to charts.
 *
 * @author Jan-Mathis Hein
 */
public class JobController implements PropertyChangeListener{
    
    //<editor-fold defaultstate="collapsed" desc="Private class variables">
    /**
     * Directory from where the last input file of any job was loaded
     */
    private static File lastInputFileDirectory;
    
    /**
     * Directory where the last chart file of any job was saved
     */
    private static File lastChartFileDirectory;
    
    /**
     * Directory where the last summary report of any job was saved
     */
    private static File lastSummaryReportDirectory;
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Private instance variables">
    /**
     * A dialog that enables the user to specifically set the bin borders
     */
    private BinBorderConfigurationDialog binBorderConfigurationDialog;
    
    /**
     * False if there are changes that are not saved, true otherwise
     */
    private boolean outputIsSaved;
    
    /**
     * True if the input for the lower bin border given by the user in the 
     * ChartConfigurationDialog is valid, false: Otherwise
     */
    private boolean isValidLowerBinBorder;
    
    /**
     * True if the input for the upper bin border given by the user in the 
     * ChartConfigurationDialog is valid, false otherwise
     */
    private boolean isValidUpperBinBorder;
    
    /**
     * True if the input for the lower frequency bound given by the user in 
     * the ChartConfigurationDialog is valid, false: Otherwise
     */
    private boolean isValidLowerFrequencyBound;
    
    /**
     * True if the input for the upper frequency bound given by the user in 
     * the ChartConfigurationDialog is valid, false: Otherwise
     */
    private boolean isValidUpperFrequencyBound;
    
    /**
     * The BinLabelType that is currently chosen
     */
    private BinLabelType currentlyChosenBinLabelType = BinLabelType.MAX;
    
    /**
     * A dialog that enables the user to configure the chart
     */
    private ChartConfigurationDialog chartConfigurationDialog;
    
    /**
     * The ComparisonFeature that is currently chosen
     */
    private ComparisonFeature currentlyChosenComparisonFeature;
    
    /**
     * An EventHandler that handles the action of the user clicking any 
     * "Add"-button in the binBorderConfigurationDialog
     */
    private final EventHandler<ActionEvent> binBorderConfigurationDialogAddHandler;
    
    /**
     * An EventHandler that handles the action of the user clicking any 
     * "Remove"-button in the binBorderConfigurationDialog
     */
    private final EventHandler<ActionEvent> binBorderConfigurationDialogRemoveHandler;
    
    /**
     * A FileChooser that is used to choose files for loading and saving
     */
    private final FileChooser fileChooser = new FileChooser();
    
    /**
     * The InputTab that allows the user to enter his input
     */
    private InputTab inputTab;
    
    /**
     * The JobTaskManager that manages the task that is specified by the 
     * InputTab
     */
    private final JobTaskManager jobTaskManager;
    
    /**
     * Manages the data of the histograms for all ComparisonFeatures
     */
    private HistogramDataManager histogramDataManager;
    
    /**
     * List of BinBorderConfigurationBoxes for the BinBorderConfigurationDialog.
     * Each box is for the configuration of one bin border
     */
    private final LinkedList<BinBorderConfigurationBox> binBorderConfigurationBoxList = new LinkedList<>();
    
    /**
     * The time difference, in milliseconds, between the time of the start of 
     * the job and midnight, January 1, 1970 UTC
     */
    private long calculationStartTime;
    
    /**
     * The OutputTab that displays the results of the task execution
     */
    private OutputTab outputTab;
    
    /**
     * PropertyChangeSupport that manages the firing of PropertyChangeEvents and 
     * the adding and removing of PropertyChangeListeners
     */
    private final PropertyChangeSupport propertyChangeSupport;
    
    /**
     * The chart that is currently shown. If the chart creation failed, this 
     * will be an error label
     */
    private Region currentlyShownChart;
    
    /**
     * The scene on which the In and OutputTab will be displayed
     */
    private final Scene scene;
    
    /**
     * A number that identifies this job in the current application session. It 
     * indicates the order in which different jobs have been opened or created
     */
    private String jobNumber;
    
    /**
     * The stage onto which the In and OutputTab will be projected
     */
    private final Stage applicationStage;
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Initialize the instance variables, set TextFormatters for the 
     * ChartConfigurationDialog, add event handlers for the In and OutputTab and 
     * the ChartConfigurationDialog and set up the tab that will be shown
     * 
     * @param tmpView main view of the application
     * @param tmpIsLoaded true if the job is loaded, false otherwise
     * @param tmpJobNumber identifies this job in this application session
     */
    public JobController (
        MainView tmpView, boolean tmpIsLoaded, String tmpJobNumber
    ) throws IllegalArgumentException {
        //<editor-fold defaultstate="collapsed" desc="Checks">
        if (tmpView == null) {
            throw new IllegalArgumentException("tmpView is null");
        }
        if (tmpJobNumber == null || tmpJobNumber.matches("\\W*\\w*\\D+\\W*\\w*")) {
            throw new IllegalArgumentException("tmpJobNumber is invalid");
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Initialize instance variables">
        this.applicationStage = tmpView.getPrimaryStage();
        this.scene = tmpView.getScene();
        this.jobNumber = tmpJobNumber;
        if (!tmpIsLoaded) {
            this.inputTab = new InputTab(this.jobNumber);
            this.jobTaskManager = new JobTaskManager(
                UserPreferences.getInstance().getNumberOfParallelThreads(), 
                UserPreferences.getInstance().getDefaultNumberOfBins()
            );
            this.jobTaskManager.addPropertyChangeListener(this);
        } else {
            this.jobTaskManager = null;
        }
        this.outputIsSaved = tmpIsLoaded;
        this.outputTab = new OutputTab(this.jobNumber);
        this.chartConfigurationDialog = new ChartConfigurationDialog(this.applicationStage, String.format(GeneralUtilities.getUIText("ChartConfigurationDialog.Title.text"), this.jobNumber));
        this.propertyChangeSupport = new PropertyChangeSupport(this);
        this.binBorderConfigurationDialog = new BinBorderConfigurationDialog(this.applicationStage, GeneralUtilities.getUIText("BinBorderConfigurationDialog.Title.text"));
        this.binBorderConfigurationDialogRemoveHandler = (ActionEvent tmpEvent) -> {
            Button tmpButton = (Button) tmpEvent.getSource();
            int tmpIndex = JobController.this.binBorderConfigurationBoxList.indexOf(tmpButton.getParent());
            JobController.this.binBorderConfigurationBoxList.remove(tmpIndex);
            ListIterator<BinBorderConfigurationBox> tmpIterator = JobController.this.binBorderConfigurationBoxList.listIterator(tmpIndex);
            while (tmpIterator.hasNext()) {
                int tmpNextIndex = tmpIterator.nextIndex();
                tmpIterator.next().getLabel().setText(Integer.toString(tmpNextIndex + 1));
            }
            if (JobController.this.binBorderConfigurationBoxList.size() <= 2) {                
                JobController.this.binBorderConfigurationBoxList.forEach((tmpBox1) -> {
                    // Removing is disabled if there are only two bin borders
                    tmpBox1.getRemoveButton().setDisable(true);
                });
            } else {                
                JobController.this.binBorderConfigurationBoxList.forEach((tmpBox1) -> {
                    // Removing is enabled if there are more than two bin borders
                    tmpBox1.getRemoveButton().setDisable(false);
                });
            }
            JobController.this.binBorderConfigurationDialog.getBinBorderConfigurationBoxesBox().getChildren().setAll(JobController.this.binBorderConfigurationBoxList); 
            // Remove the insets because they were only a bug fix that is no longer necessary, because the ScrollPane has been resized 
            JobController.this.binBorderConfigurationDialog.getBinBorderConfigurationBoxesBox().setPadding(Insets.EMPTY);
        };
        this.binBorderConfigurationDialogAddHandler = new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent tmpEvent) {
                Button tmpButton = (Button) tmpEvent.getSource();
                int tmpIndex = JobController.this.binBorderConfigurationBoxList.indexOf(tmpButton.getParent());
                BinBorderConfigurationBox tmpBox = new BinBorderConfigurationBox("", Integer.toString(tmpIndex + 2));
                tmpBox.getAddButton().setOnAction(this);
                tmpBox.getRemoveButton().setOnAction(JobController.this.binBorderConfigurationDialogRemoveHandler);
                tmpBox.getTextField().setTextFormatter(GeneralUtilities.getSignedDecimalNumberTextFormatter());
                JobController.this.binBorderConfigurationBoxList.add(tmpIndex + 1, tmpBox);
                ListIterator<BinBorderConfigurationBox> tmpIterator = JobController.this.binBorderConfigurationBoxList.listIterator(tmpIndex + 2);
                while (tmpIterator.hasNext()) {
                    int tmpNextIndex = tmpIterator.nextIndex();
                    tmpIterator.next().getLabel().setText(Integer.toString(tmpNextIndex + 1));
                }
                if (JobController.this.binBorderConfigurationBoxList.size() <= 2) {                
                    JobController.this.binBorderConfigurationBoxList.forEach((tmpBox1) -> {
                        // Removing is disabled if there are only two bin borders
                        tmpBox1.getRemoveButton().setDisable(true);
                    });
                } else {                
                    JobController.this.binBorderConfigurationBoxList.forEach((tmpBox1) -> {
                        // Removing is enabled if there are more than two bin borders
                        tmpBox1.getRemoveButton().setDisable(false);
                    });
                }
                JobController.this.binBorderConfigurationDialog.getBinBorderConfigurationBoxesBox().getChildren().setAll(JobController.this.binBorderConfigurationBoxList);
                // Remove the insets because they were only a bug fix that is no longer necessary, because the ScrollPane has been resized 
                JobController.this.binBorderConfigurationDialog.getBinBorderConfigurationBoxesBox().setPadding(Insets.EMPTY);
            }

        };
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="TextFormatters">
        this.outputTab.getNumberOfBinsTextField().setTextFormatter(GeneralUtilities.getUnsignedIntegerTextFormatter());
        this.chartConfigurationDialog.getLowerBinBorderInputField().setTextFormatter(new TextFormatter<>((TextFormatter.Change tmpChange) -> {
            if (!tmpChange.getControlNewText().matches("[-\\+]?\\d*\\.?\\d*E?\\d*")) {
                tmpChange.setText("");
            }
            try {
                Double.parseDouble(tmpChange.getControlNewText());
                tmpChange.getControl().setStyle("-fx-background-color: white;");
                this.isValidLowerBinBorder = true;
            } catch (NumberFormatException ex) {
                tmpChange.getControl().setStyle("-fx-background-color: red;");
                this.isValidLowerBinBorder = false;
            }
            return tmpChange;
        }));
        this.chartConfigurationDialog.getUpperBinBorderInputField().setTextFormatter(new TextFormatter<>((TextFormatter.Change tmpChange) -> {
            if (!tmpChange.getControlNewText().matches("[-\\+]?\\d*\\.?\\d*E?\\d*")) {
                tmpChange.setText("");
            }
            try {
                Double.parseDouble(tmpChange.getControlNewText());
                tmpChange.getControl().setStyle("-fx-background-color: white;");
                this.isValidUpperBinBorder = true;
            } catch (NumberFormatException ex) {
                tmpChange.getControl().setStyle("-fx-background-color: red;");
                this.isValidUpperBinBorder = false;
            }
            return tmpChange;
        }));
        this.chartConfigurationDialog.getLowerFrequencyBoundInputField().setTextFormatter(new TextFormatter<>((TextFormatter.Change tmpChange) -> {
            if (!tmpChange.getControlNewText().matches("\\d*\\.?\\d*E?\\d*")) {
                tmpChange.setText("");
            }
            try {
                Double.parseDouble(tmpChange.getControlNewText());
                tmpChange.getControl().setStyle("-fx-background-color: white;");
                this.isValidLowerFrequencyBound = true;
            } catch (NumberFormatException ex) {
                tmpChange.getControl().setStyle("-fx-background-color: red;");
                this.isValidLowerFrequencyBound = false;
            }
            return tmpChange;
        }));
        this.chartConfigurationDialog.getUpperFrequencyBoundInputField().setTextFormatter(new TextFormatter<>((TextFormatter.Change tmpChange) -> {
            if (!tmpChange.getControlNewText().matches("\\d*\\.?\\d*E?\\d*")) {
                tmpChange.setText("");
            }
            try {
                Double.parseDouble(tmpChange.getControlNewText());
                tmpChange.getControl().setStyle("-fx-background-color: white;");
                this.isValidUpperFrequencyBound = true;
            } catch (NumberFormatException ex) {
                tmpChange.getControl().setStyle("-fx-background-color: red;");
                this.isValidUpperFrequencyBound = false;
            }
            return tmpChange;
        }));
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Event handling">
        //<editor-fold defaultstate="collapsed" desc="- InputTab related">
        if (!tmpIsLoaded) {
            //<editor-fold defaultstate="collapsed" desc="- Tab related">
            this.inputTab.setOnCloseRequest((Event e) -> {
                this.propertyChangeSupport.firePropertyChange("Want to close InputTab", null, e);
            });
            this.inputTab.setOnSelectionChanged((Event e) -> {
                this.propertyChangeSupport.firePropertyChange("JobController selected", false, this.inputTab.isSelected());
            });
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="- Input files related">
            this.inputTab.getChooseFile1Button().setOnAction((ActionEvent e) -> {
                try {
                    this.fileChooser.getExtensionFilters().clear();
                    this.fileChooser.getExtensionFilters().addAll(
                            new FileChooser.ExtensionFilter(GeneralUtilities.getUIText("FileExtensionFilter.All.text"), "*.*"),
                            new FileChooser.ExtensionFilter(GeneralUtilities.getUIText("FileExtensionFilter.Text.text"), "*.txt")
                    );
                    if (!UserPreferences.getInstance().getInputDir().isEmpty() && new File(UserPreferences.getInstance().getInputDir()).isDirectory()) {
                        this.fileChooser.setInitialDirectory(new File(UserPreferences.getInstance().getInputDir()));
                    } else {
                        this.fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
                    }
                    synchronized (JobController.class) {
                        if (JobController.lastInputFileDirectory != null && JobController.lastInputFileDirectory.isDirectory()) {
                            this.fileChooser.setInitialDirectory(JobController.lastInputFileDirectory);
                        }
                    }
                    this.fileChooser.setTitle(GeneralUtilities.getUIText("FileChooserTitle.MoleculeFile.text"));
                    File tmpFile = this.fileChooser.showOpenDialog(this.applicationStage);
                    if (tmpFile != null && tmpFile.canRead()) {
                        this.inputTab.getInputFile1Label().setText(GeneralUtilities.getUIText("InputTab.inputFile1Label.text") + " " + tmpFile.getName());
                        this.jobTaskManager.setInputFile1(tmpFile);
                        this.inputTab.getJobInfoLabel().setText(GeneralUtilities.getUIText("InfoDescription.InputFile1.text"));
                        synchronized (JobController.class) {
                            JobController.lastInputFileDirectory = tmpFile.getParentFile();
                        }
                    }
                    for (CheckBox tmpCheckBox : this.inputTab.getComparisonFeatureCheckBoxes()) {
                        if (tmpCheckBox.isSelected() && this.jobTaskManager.canReadInput()) {
                            this.inputTab.getJobStartButton().setDisable(false);
                            return;
                        }
                    }
                    this.inputTab.getJobStartButton().setDisable(true);
                } catch (IllegalArgumentException ex) {
                    GeneralUtilities.showErrorDialog(this.applicationStage, ex.toString(), "JobController.inputTab.getChooseFile1Button().setOnAction($lambda)");
                    GeneralUtilities.logException(Level.WARNING, ex);
                }
            });
            this.inputTab.getChooseFile2Button().setOnAction((ActionEvent e) -> {
                try {
                    this.fileChooser.getExtensionFilters().clear();
                    this.fileChooser.getExtensionFilters().addAll(
                            new FileChooser.ExtensionFilter(GeneralUtilities.getUIText("FileExtensionFilter.All.text"), "*.*"),
                            new FileChooser.ExtensionFilter(GeneralUtilities.getUIText("FileExtensionFilter.Text.text"), "*.txt")
                    );
                    if (!UserPreferences.getInstance().getInputDir().isEmpty() && new File(UserPreferences.getInstance().getInputDir()).isDirectory()) {
                        this.fileChooser.setInitialDirectory(new File(UserPreferences.getInstance().getInputDir()));
                    } else {
                        this.fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
                    }
                    synchronized (JobController.class) {
                        if (JobController.lastInputFileDirectory != null && JobController.lastInputFileDirectory.isDirectory()) {
                            this.fileChooser.setInitialDirectory(JobController.lastInputFileDirectory);
                        }
                    }
                    this.fileChooser.setTitle(GeneralUtilities.getUIText("FileChooserTitle.MoleculeFile.text"));
                    File tmpFile = this.fileChooser.showOpenDialog(this.applicationStage);
                    if (tmpFile != null && tmpFile.canRead()) {
                        this.inputTab.getInputFile2Label().setText(GeneralUtilities.getUIText("InputTab.inputFile2Label.text") + " " + tmpFile.getName());
                        this.jobTaskManager.setInputFile2(tmpFile);
                        this.inputTab.getJobInfoLabel().setText(GeneralUtilities.getUIText("InfoDescription.InputFile2.text"));
                        synchronized (JobController.class) {
                            JobController.lastInputFileDirectory = tmpFile.getParentFile();
                        }
                    }
                    for (CheckBox tmpCheckBox : this.inputTab.getComparisonFeatureCheckBoxes()) {
                        if (tmpCheckBox.isSelected() && this.jobTaskManager.canReadInput()) {
                            this.inputTab.getJobStartButton().setDisable(false);
                            return;
                        }
                    }
                    this.inputTab.getJobStartButton().setDisable(true);
                } catch (IllegalArgumentException ex) {
                    GeneralUtilities.showErrorDialog(this.applicationStage, ex.toString(), "JobController.inputTab.getChooseFile2Button().setOnAction($lambda)");
                    GeneralUtilities.logException(Level.WARNING, ex);
                }
            });
            this.inputTab.getInputType1ChoiceBox().setOnAction((ActionEvent e) -> {
                InputType tmpType = this.inputTab.getInputType1ChoiceBox().getValue();
                if (tmpType != null) {
                    this.jobTaskManager.setInputType1(tmpType);
                }
                this.inputTab.getJobStartButton().setDisable(!this.jobTaskManager.canReadInput());
            });
            this.inputTab.getInputType2ChoiceBox().setOnAction((ActionEvent e) -> {
                InputType tmpType = this.inputTab.getInputType2ChoiceBox().getValue();
                if (tmpType != null) {
                    this.jobTaskManager.setInputType2(tmpType);
                }
                this.inputTab.getJobStartButton().setDisable(!this.jobTaskManager.canReadInput());
            });
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="- ComparisonFeature selection related">
            this.inputTab.getSelectAllComparisonFeaturesButton().setOnAction((ActionEvent e) -> {
                for (CheckBox tmpCheckBox : this.inputTab.getComparisonFeatureCheckBoxes()) {
                    tmpCheckBox.setSelected(true);
                }
                for (CheckBox tmpCheckBox : this.inputTab.getSelectAllCheckBoxes()) {
                    tmpCheckBox.setSelected(true);
                }
            });
            this.inputTab.getReverseComparisonFeatureSelectionButton().setOnAction((ActionEvent e) -> {
                for (CheckBox tmpCheckBox : this.inputTab.getComparisonFeatureCheckBoxes()) {
                    tmpCheckBox.setSelected(!tmpCheckBox.isSelected());
                }
            });
            for (CheckBox tmpCheckBox1 : this.inputTab.getComparisonFeatureCheckBoxes()) {
                tmpCheckBox1.selectedProperty().addListener((ObservableValue<? extends Boolean> tmpObservableValue, Boolean tmpOldValue, Boolean tmpNewValue) -> {
                    int tmpInt = 0;
                    for (CheckBox tmpCheckBox2 : this.inputTab.getComparisonFeatureCheckBoxes()) {
                        if (tmpCheckBox2.isSelected()) {
                            tmpInt++;
                        }
                    }
                    if (tmpInt == 0) {
                        this.inputTab.getJobInfoLabel().setText(GeneralUtilities.getUIText("InfoDescription.noFeaturesSelected.text"));
                        this.inputTab.getJobStartButton().setDisable(true);
                    } else {
                        this.inputTab.getJobInfoLabel().setText(String.format(GeneralUtilities.getUIText("InfoDescription.xFeaturesSelected.text"), tmpInt));
                        if (this.jobTaskManager.canReadInput()) {
                            this.inputTab.getJobStartButton().setDisable(false);
                        } else {
                            this.inputTab.getJobStartButton().setDisable(true);
                        }
                    }
                    
                });
            }
            ListIterator<CheckBox> tmpCheckBoxIterator = this.inputTab.getSelectAllCheckBoxes().listIterator();
            Iterator<VBox> tmpVBoxIterator = this.inputTab.getCheckBoxesBoxes().iterator();
            while (tmpCheckBoxIterator.hasNext() && tmpVBoxIterator.hasNext()) {
                tmpCheckBoxIterator.next().setOnAction(new EventHandler<ActionEvent>() {
                    
                    private final VBox box = tmpVBoxIterator.next();
                    
                    private final CheckBox checkBox = tmpCheckBoxIterator.previous();
                    
                    {
                        tmpCheckBoxIterator.next();
                    }
                    
                    @Override
                    public void handle(ActionEvent e) {
                        boolean tmpIsChecked = this.checkBox.isSelected();
                        this.box.getChildren().stream().map((tmpNode) -> (CheckBox) tmpNode).forEach((tmpCheckBox) -> {
                            tmpCheckBox.setSelected(tmpIsChecked);
                        });
                    }
                });
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="- Start and cancel related">
            this.inputTab.getJobStartButton().setOnAction((ActionEvent e) -> {
                //<editor-fold defaultstate="collapsed" desc="Checks">
                if (this.jobTaskManager.isWorking()) {
                    GeneralUtilities.showInfoDialog(this.applicationStage, GeneralUtilities.getUIText("InfoDescription.JobStillWorking.text"));
                    this.inputTab.getJobStartButton().setDisable(true);
                    return;
                }
                if (!this.jobTaskManager.canReadInput()) {
                    GeneralUtilities.showInfoDialog(this.applicationStage, GeneralUtilities.getUIText("InfoDescription.InputConfigurationNotReadable.text"));
                    this.inputTab.getJobStartButton().setDisable(true);
                    return;
                }
                //</editor-fold>
                boolean[] tmpChoosenComparisonFeatures = new boolean[this.inputTab.getComparisonFeatureCheckBoxes().length];
                for (int i = 0; i < tmpChoosenComparisonFeatures.length; i++) {
                    tmpChoosenComparisonFeatures[i] = this.inputTab.getComparisonFeatureCheckBoxes()[i].isSelected();
                }
                try {
                    // Checks if at least one ComparisonFeature is chosen 
                    this.jobTaskManager.setComparisonFeatures(tmpChoosenComparisonFeatures);
                } catch (MSCInputException | IllegalArgumentException ex) {
                    GeneralUtilities.showInfoDialog(this.applicationStage, ex.toString());
                    return;
                }
                this.inputTab.getJobStartButton().setVisible(false);
                this.inputTab.getJobCancelButton().setVisible(true);
                this.inputTab.getChooseFile1Button().setDisable(true);
                this.inputTab.getChooseFile2Button().setDisable(true);
                this.inputTab.getInputType1ChoiceBox().setDisable(true);
                this.inputTab.getInputType2ChoiceBox().setDisable(true);
                this.inputTab.getInputFile1Label().setDisable(true);
                this.inputTab.getInputFile2Label().setDisable(true);
                this.inputTab.getJobCancelButton().setDisable(false);
                this.inputTab.getReverseComparisonFeatureSelectionButton().setDisable(true);
                for (CheckBox tmpCheckBox : this.inputTab.getSelectAllCheckBoxes()) {
                    tmpCheckBox.setDisable(true);
                }
                this.inputTab.getSelectAllComparisonFeaturesButton().setDisable(true);
                for (CheckBox tmpCheckBox : this.inputTab.getComparisonFeatureCheckBoxes()) {
                    tmpCheckBox.setDisable(true);
                }
                this.inputTab.getJobProgressBar().setVisible(true);
                this.inputTab.getJobProgressBar().setProgress(0);
                this.inputTab.getJobInfoLabel().setText(GeneralUtilities.getUIText("InfoDescription.JobStarted.text"));
                try {
                    Task<Void> tmpTask = new Task<Void>() {
                        @Override 
                        protected Void call() throws Exception {
                            JobController.this.jobTaskManager.startTasks();
                            return null;
                        }
                    };
                    Thread tmpBackgroundThread = new Thread(tmpTask);
                    this.calculationStartTime = System.currentTimeMillis();
                    tmpBackgroundThread.start();
                } catch (Exception ex) {
                    GeneralUtilities.logException(Level.SEVERE, ex);
                    GeneralUtilities.showErrorDialog(this.applicationStage, ex.toString(), "JobController.inputTab.getJobStartButton().setOnAction($lambda)");
                    this.jobTaskManager.cancelWorkingProcess();
                    this.inputTab.getJobStartButton().setVisible(true);
                    this.inputTab.getJobCancelButton().setVisible(false);
                    this.inputTab.getChooseFile1Button().setDisable(false);
                    this.inputTab.getChooseFile2Button().setDisable(false);
                    this.inputTab.getInputType1ChoiceBox().setDisable(false);
                    this.inputTab.getInputType2ChoiceBox().setDisable(false);
                    this.inputTab.getInputFile1Label().setDisable(false);
                    this.inputTab.getInputFile2Label().setDisable(false);
                    this.inputTab.getReverseComparisonFeatureSelectionButton().setDisable(false);
                    for (CheckBox tmpCheckBox : this.inputTab.getSelectAllCheckBoxes()) {
                        tmpCheckBox.setDisable(false);
                    }
                    this.inputTab.getSelectAllComparisonFeaturesButton().setDisable(false);
                    for (CheckBox tmpCheckBox : this.inputTab.getComparisonFeatureCheckBoxes()) {
                        tmpCheckBox.setDisable(false);
                    }
                    this.inputTab.getJobProgressBar().setVisible(false);
                    this.inputTab.getJobProgressBar().setProgress(0);
                    this.inputTab.getJobInfoLabel().setText(GeneralUtilities.getUIText("InfoDescription.StartingJobFailed.text"));
                }
            });
            this.inputTab.getJobCancelButton().setOnAction((ActionEvent e) -> {
                this.jobTaskManager.cancelWorkingProcess();
                this.inputTab.getJobStartButton().setVisible(true);
                this.inputTab.getJobCancelButton().setVisible(false);
                this.inputTab.getChooseFile1Button().setDisable(false);
                this.inputTab.getChooseFile2Button().setDisable(false);
                this.inputTab.getInputType1ChoiceBox().setDisable(false);
                this.inputTab.getInputType2ChoiceBox().setDisable(false);
                this.inputTab.getInputFile1Label().setDisable(false);
                this.inputTab.getInputFile2Label().setDisable(false);
                this.inputTab.getReverseComparisonFeatureSelectionButton().setDisable(false);
                for (CheckBox tmpCheckBox : this.inputTab.getSelectAllCheckBoxes()) {
                    tmpCheckBox.setDisable(false);
                }
                this.inputTab.getSelectAllComparisonFeaturesButton().setDisable(false);
                for (CheckBox tmpCheckBox : this.inputTab.getComparisonFeatureCheckBoxes()) {
                    tmpCheckBox.setDisable(false);
                }
                this.inputTab.getJobProgressBar().setVisible(false);
                this.inputTab.getJobProgressBar().setProgress(0);
                synchronized (this) {
                    // Guarantees that the label is set correctly
                    Platform.runLater(() -> {
                        this.inputTab.getJobInfoLabel().setText(GeneralUtilities.getUIText("InfoDescription.JobCanceled.text"));
                    });
                }
            });
            //</editor-fold>
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="- OutputTab related">
        //<editor-fold defaultstate="collapsed" desc="outputTab">
        this.outputTab.setOnCloseRequest((Event e) -> {
            this.propertyChangeSupport.firePropertyChange("Want to close OutputTab", null, e);
        });
        this.outputTab.setOnSelectionChanged((Event e) -> {
            this.propertyChangeSupport.firePropertyChange("JobController selected", false, this.outputTab.isSelected());
        });
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="summaryReportButton">
        this.outputTab.getSummaryReportButton().setOnAction((ActionEvent e) -> {
            try {
                this.fileChooser.getExtensionFilters().clear();
                this.fileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter(GeneralUtilities.getUIText("FileExtensionFilter.All.text"), "*.*"),
                        new FileChooser.ExtensionFilter(GeneralUtilities.getUIText("FileExtensionFilter.Text.text"), "*.txt")
                );
                if (!UserPreferences.getInstance().getSummaryReportDir().isEmpty() && new File(UserPreferences.getInstance().getSummaryReportDir()).isDirectory()) {
                    this.fileChooser.setInitialDirectory(new File(UserPreferences.getInstance().getSummaryReportDir()));
                } else {
                    this.fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
                }
                synchronized (JobController.class) {
                    if (JobController.lastSummaryReportDirectory != null && JobController.lastSummaryReportDirectory.isDirectory()) {
                        this.fileChooser.setInitialDirectory(JobController.lastSummaryReportDirectory);
                    }
                }
                this.fileChooser.setTitle(GeneralUtilities.getUIText("FileChooserTitle.SaveSummaryReport.text"));
                final File tmpFile = this.fileChooser.showSaveDialog(this.applicationStage);
                if (tmpFile == null) {
                    return;
                }
                boolean tmpIsNewFile = tmpFile.createNewFile();
                if (!tmpFile.canWrite()) {
                    if (tmpIsNewFile) {
                        Files.delete(tmpFile.toPath());
                    }
                    GeneralUtilities.showInfoDialog(this.applicationStage, GeneralUtilities.getUIText("InfoDescription.FileNotWritable.text"));
                    return;
                }
                synchronized (JobController.class) {
                    JobController.lastSummaryReportDirectory = tmpFile.getParentFile();
                }
                String tmpFormat = tmpFile.getAbsolutePath().substring(tmpFile.getAbsolutePath().lastIndexOf(".")).toLowerCase();
                switch (tmpFormat) {
                    case ".txt":
                        try (BufferedWriter tmpBufferedWriter = new BufferedWriter(new FileWriter(tmpFile))) {
                            for (String tmpString : ResultFormattingUtility.getResultSummaryReportHeader()) {
                                tmpBufferedWriter.write(tmpString);
                                tmpBufferedWriter.newLine();
                            }
                            for (String tmpString : ResultFormattingUtility.getGeneralInfoBlock(
                                    new Date(this.histogramDataManager.getCalculationFinishTime()), 
                                    (this.histogramDataManager.getCalculationFinishTime() - this.histogramDataManager.getCalculationStartTime()) / 1000, 
                                    this.histogramDataManager.getNumberOfComparedPairs(), this.histogramDataManager.getInputFile1(), this.histogramDataManager.getInputFile2()
                                )) {
                                tmpBufferedWriter.write(tmpString);
                                tmpBufferedWriter.newLine();
                            }
                            for (ComparisonFeature tmpComparisonFeature : this.histogramDataManager.getComparisonFeatureSet()) {
                                for (String tmpString : ResultFormattingUtility.getVerticalHistogramBlock(this.histogramDataManager.getHistogramData(tmpComparisonFeature))) {
                                    tmpBufferedWriter.write(tmpString);
                                    tmpBufferedWriter.newLine();
                                }
                            }
                        }
                        break;
                    default:
                        GeneralUtilities.showInfoDialog(JobController.this.applicationStage, GeneralUtilities.getUIText("InfoDescription.FileFormatNotSupported.text"));
                        break;
                }
            } catch (IOException ex) {
                GeneralUtilities.showErrorDialog(this.applicationStage, ex.toString(), "JobController.outputTab.getSummaryReportButton().setOnAction($lambda)");
                GeneralUtilities.logException(Level.WARNING, ex);
            }
        });
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="imageButton">
        this.outputTab.getImageButton().setOnAction((ActionEvent e) -> {
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
                synchronized (JobController.class) {
                    if (JobController.lastChartFileDirectory != null && JobController.lastChartFileDirectory.isDirectory()) {
                        this.fileChooser.setInitialDirectory(JobController.lastChartFileDirectory);
                    }
                }
                this.fileChooser.setTitle(GeneralUtilities.getUIText("FileChooserTitle.SaveChart.text"));
                final File tmpFile = this.fileChooser.showSaveDialog(this.applicationStage);
                if (tmpFile == null) {
                    return;
                }
                boolean tmpIsNewFile = tmpFile.createNewFile();
                if (!tmpFile.canWrite()) {
                    if (tmpIsNewFile) {
                        Files.delete(tmpFile.toPath());
                    }
                    GeneralUtilities.showInfoDialog(this.applicationStage, GeneralUtilities.getUIText("InfoDescription.FileNotWritable.text"));
                    return;
                }
                synchronized (JobController.class) {
                    JobController.lastChartFileDirectory = tmpFile.getParentFile();
                }
                SnapshotParameters tmpSnapshotParameters = new SnapshotParameters();
                // IMPORTANT: Setting tmpScalingFactor too low will cause the program to throw an exception
                final float tmpScalingFactor = (float) ((-0.91) * UserPreferences.getInstance().getImageQuality() + 1);
                tmpSnapshotParameters.setTransform(Transform.scale(1 / tmpScalingFactor, 1 / tmpScalingFactor));
                final BufferedImage tmpBufferedImage = SwingFXUtils.fromFXImage(this.currentlyShownChart.snapshot(tmpSnapshotParameters, null), null);
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
                                    tmpSvgGenerator.drawImage(tmpBufferedImage, AffineTransform.getScaleInstance(tmpScalingFactor, tmpScalingFactor), null);
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
                                            float tmpWidth = tmpBufferedImage.getWidth() * tmpScalingFactor;
                                            float tmpHeight = tmpBufferedImage.getHeight() * tmpScalingFactor;
                                            tmpPage.setMediaBox(new PDRectangle(tmpWidth, tmpHeight));
                                            tmpStream.drawImage(tmpPDImage, 0, 0, tmpWidth, tmpHeight);
                                        }
                                        tmpDocument.save(tmpFile);
                                    }
                                    break;
                                default:
                                    Platform.runLater(() -> {
                                        GeneralUtilities.showInfoDialog(JobController.this.applicationStage, GeneralUtilities.getUIText("InfoDescription.FileFormatNotSupported.text"));
                                    });
                            }
                        } catch (IOException | DOMException ex) {
                            Platform.runLater(() -> {
                                JobController.this.scene.setCursor(Cursor.DEFAULT);
                                GeneralUtilities.showErrorDialog(JobController.this.applicationStage, ex.toString(), "JobController.outputTab.getSaveButton().setOnAction($lambda)");
                                GeneralUtilities.logException(Level.WARNING, ex);
                            });
                        } finally {
                            Platform.runLater(() -> {
                                JobController.this.scene.setCursor(Cursor.DEFAULT);
                            });
                        }
                        return null;
                    }
                };
                Thread tmpBackgroundThread = new Thread(tmpTask);
                this.scene.setCursor(Cursor.WAIT);
                tmpBackgroundThread.start();
            } catch (StringIndexOutOfBoundsException ex) {
                GeneralUtilities.showInfoDialog(this.applicationStage, GeneralUtilities.getUIText("InfoDescription.FileFormatNotSupported.text"));
            } catch (Exception ex) {
                GeneralUtilities.showErrorDialog(this.applicationStage, ex.toString(), "JobController.outputTab.getSaveButton().setOnAction($lambda)");
                GeneralUtilities.logException(Level.WARNING, ex);
            }
        });
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="relativeFrequenciesCheckBox">
        this.outputTab.getRelativeFrequenciesCheckBox().setOnAction((ActionEvent e) -> {
            HistogramData tmpHistogramData = this.histogramDataManager.getHistogramData(this.currentlyChosenComparisonFeature);
            tmpHistogramData.setUseRelativeFrequencies(this.outputTab.getRelativeFrequenciesCheckBox().isSelected());
            this.outputTab.getFrequency1Slider().setMin(0.0);
            this.outputTab.getFrequency1Slider().setMax(tmpHistogramData.getMaxFrequency());
            this.outputTab.getFrequency2Slider().setMin(0.0);
            this.outputTab.getFrequency2Slider().setMax(tmpHistogramData.getMaxFrequency());
            double tmpTickUnit;
            tmpTickUnit = (tmpHistogramData.getMaxFrequency() - 0.0) / 10;
            if (tmpTickUnit > 0) {
                this.outputTab.getFrequency1Slider().setMajorTickUnit(tmpTickUnit);
                this.outputTab.getFrequency2Slider().setMajorTickUnit(tmpTickUnit);
            }
            this.updateOutputTab();
        });
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="chartChoiceBox">
        this.outputTab.getChartChoiceBox().setOnAction((ActionEvent e) -> {
            ComparisonFeature tmpComparisonFeature = this.outputTab.getChartChoiceBox().getValue();
            //<editor-fold defaultstate="collapsed" desc="Checks">
            if (!this.histogramDataManager.getComparisonFeatureSet().contains(tmpComparisonFeature)) {
                GeneralUtilities.showErrorDialog(this.applicationStage, GeneralUtilities.getUIText("ErrorDescription.InvalidComparisonFeatureSelection.text"), "JobController.outputTab.getChartChoiceBox().setOnAction($lambda)");
                GeneralUtilities.logMessage(Level.WARNING, "JobController", "Constructor()", GeneralUtilities.getUIText("Logging.InvalidComparisonFeatureSelection.text"));
                return;
            }
            //</editor-fold>
            this.currentlyChosenComparisonFeature = tmpComparisonFeature;
            HistogramData tmpHistogramData = this.histogramDataManager.getHistogramData(this.currentlyChosenComparisonFeature);
            //<editor-fold defaultstate="collapsed" desc="Set up the sliders">
            double tmpMin = Double.isNaN(this.currentlyChosenComparisonFeature.getMinimalValue()) ? tmpHistogramData.getMinComparisonFeatureValue() : this.currentlyChosenComparisonFeature.getMinimalValue();
            double tmpMax = Double.isNaN(this.currentlyChosenComparisonFeature.getMaximalValue()) ? tmpHistogramData.getMaxComparisonFeatureValue() : this.currentlyChosenComparisonFeature.getMaximalValue();
            this.outputTab.getBinBorder1Slider().setMin(tmpMin);
            this.outputTab.getBinBorder1Slider().setMax(tmpMax);
            this.outputTab.getBinBorder2Slider().setMin(tmpMin);
            this.outputTab.getBinBorder2Slider().setMax(tmpMax);
            double tmpTickUnit1;
            tmpTickUnit1 = (tmpMax - tmpMin) / 10;
            if (tmpTickUnit1 > 0) {
                this.outputTab.getBinBorder1Slider().setMajorTickUnit(tmpTickUnit1);
                this.outputTab.getBinBorder2Slider().setMajorTickUnit(tmpTickUnit1);
            }
            this.outputTab.getFrequency1Slider().setMin(0.0);
            this.outputTab.getFrequency1Slider().setMax(tmpHistogramData.getMaxFrequency());
            this.outputTab.getFrequency2Slider().setMin(0.0);
            this.outputTab.getFrequency2Slider().setMax(tmpHistogramData.getMaxFrequency());
            double tmpTickUnit2;
            tmpTickUnit2 = (tmpHistogramData.getMaxFrequency() - 0.0) / 10;
            if (tmpTickUnit2 > 0) {
                this.outputTab.getFrequency1Slider().setMajorTickUnit(tmpTickUnit2);
                this.outputTab.getFrequency2Slider().setMajorTickUnit(tmpTickUnit2);
            }
            //</editor-fold>
            this.updateOutputTab();
            if (this.currentlyShownChart instanceof BarChart) {
                this.outputTab.getBinBorder1Slider().setDisable(!(tmpTickUnit1 > 0));
                this.outputTab.getBinBorder2Slider().setDisable(!(tmpTickUnit1 > 0));
                this.outputTab.getFrequency1Slider().setDisable(!(tmpTickUnit2 > 0));
                this.outputTab.getFrequency2Slider().setDisable(!(tmpTickUnit2 > 0));
            }
        });
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="numberOfBinsTextField">
        this.outputTab.getNumberOfBinsTextField().addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent tmpEvent) -> {
            KeyCombination tmpKeyCombination = new KeyCodeCombination(KeyCode.ENTER);
            if (tmpKeyCombination.match(tmpEvent)) {
                int tmpNumberOfBins;
                try {
                    tmpNumberOfBins = Integer.parseInt(this.outputTab.getNumberOfBinsTextField().getText());
                } catch (NumberFormatException ex) {
                    // This happens if the text field is empty or the input is too large
                    if (!this.outputTab.getNumberOfBinsTextField().getText().isEmpty()) {
                        GeneralUtilities.showInfoDialog(this.applicationStage, GeneralUtilities.getUIText("InfoDescription.NumberInputTooHigh.text"));
                    }
                    return;
                }
                HistogramData tmpHistogramData = this.histogramDataManager.getHistogramData(this.currentlyChosenComparisonFeature);
                if (tmpNumberOfBins >= 500) {
                    // Confirm, because binning could take a while
                    Optional<ButtonType> tmpResult = GeneralUtilities.showConfirmationDialog(
                            this.applicationStage, GeneralUtilities.getUIText("ConfirmationDialog.ManyBinsContent.text"),
                            GeneralUtilities.getUIText("ConfirmationDialog.SelectionTitle.text"),
                            String.format(GeneralUtilities.getUIText("ConfirmationDialog.ManyBinsHeader.text"), tmpNumberOfBins)
                    );
                    if (tmpResult.isPresent() && tmpResult.get().getButtonData() == ButtonBar.ButtonData.APPLY) {
                        tmpHistogramData.setNumberOfBins(tmpNumberOfBins);
                        this.outputIsSaved = false;
                    } else {
                        return;
                    }
                } else {
                    tmpHistogramData.setNumberOfBins(tmpNumberOfBins);
                    this.outputIsSaved = false;
                }
                // Resets the frequency bounds
                tmpHistogramData.call();
                // Update the reseted frequency bounds
                this.updateOutputTab();
            }
        });
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="binBorderSliders">
        this.outputTab.getBinBorder1Slider().setOnMouseReleased((MouseEvent e) -> {
            double tmpBinBorder1 = this.outputTab.getBinBorder1Slider().getValue();
            double tmpBinBorder2 = this.outputTab.getBinBorder2Slider().getValue();
            HistogramData tmpHistogramData = this.histogramDataManager.getHistogramData(this.currentlyChosenComparisonFeature);
            tmpHistogramData.setUpperBinBorder(tmpBinBorder1 > tmpBinBorder2 ? tmpBinBorder1 : tmpBinBorder2);
            tmpHistogramData.setLowerBinBorder(tmpBinBorder1 < tmpBinBorder2 ? tmpBinBorder1 : tmpBinBorder2);
            double tmpFrequency1SliderValue = this.outputTab.getFrequency1Slider().getValue();
            double tmpFrequency2SliderValue = this.outputTab.getFrequency2Slider().getValue();
            // Resets the frequency bounds
            tmpHistogramData.call();
            // Set the pre-reset frequency bounds
            tmpHistogramData.setUpperFrequencyBound(tmpFrequency1SliderValue);
            tmpHistogramData.setLowerFrequencyBound(tmpFrequency2SliderValue);
            this.updateOutputTab();
            this.outputIsSaved = false;
        });
        this.outputTab.getBinBorder2Slider().setOnMouseReleased((MouseEvent e) -> {
            double tmpBinBorder1 = this.outputTab.getBinBorder1Slider().getValue();
            double tmpBinBorder2 = this.outputTab.getBinBorder2Slider().getValue();
            HistogramData tmpHistogramData = this.histogramDataManager.getHistogramData(this.currentlyChosenComparisonFeature);
            tmpHistogramData.setUpperBinBorder(tmpBinBorder1 > tmpBinBorder2 ? tmpBinBorder1 : tmpBinBorder2);
            tmpHistogramData.setLowerBinBorder(tmpBinBorder1 < tmpBinBorder2 ? tmpBinBorder1 : tmpBinBorder2);
            double tmpFrequency1SliderValue = this.outputTab.getFrequency1Slider().getValue();
            double tmpFrequency2SliderValue = this.outputTab.getFrequency2Slider().getValue();
            // Resets the frequency bounds
            tmpHistogramData.call();
            // Set the pre-reset frequency bounds
            tmpHistogramData.setUpperFrequencyBound(tmpFrequency1SliderValue);
            tmpHistogramData.setLowerFrequencyBound(tmpFrequency2SliderValue);
            this.updateOutputTab();
            this.outputIsSaved = false;
        });
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="frequencySliders">
        this.outputTab.getFrequency1Slider().setOnMouseReleased((MouseEvent e) -> {
            double tmpFrequencyBound1 = this.outputTab.getFrequency1Slider().getValue();
            double tmpFrequencyBound2 = this.outputTab.getFrequency2Slider().getValue();
            HistogramData tmpHistogramData = this.histogramDataManager.getHistogramData(this.currentlyChosenComparisonFeature);
            tmpHistogramData.setUpperFrequencyBound(
                tmpFrequencyBound1 > tmpFrequencyBound2 ? tmpFrequencyBound1 : tmpFrequencyBound2
            );
            tmpHistogramData.setLowerFrequencyBound(
                tmpFrequencyBound1 < tmpFrequencyBound2 ? tmpFrequencyBound1 : tmpFrequencyBound2
            );
            this.updateOutputTab();
            this.outputIsSaved = false;
        });
        this.outputTab.getFrequency2Slider().setOnMouseReleased((MouseEvent e) -> {
            double tmpFrequencyBound1 = this.outputTab.getFrequency1Slider().getValue();
            double tmpFrequencyBound2 = this.outputTab.getFrequency2Slider().getValue();
            HistogramData tmpHistogramData = this.histogramDataManager.getHistogramData(this.currentlyChosenComparisonFeature);
            tmpHistogramData.setUpperFrequencyBound(
                tmpFrequencyBound1 > tmpFrequencyBound2 ? tmpFrequencyBound1 : tmpFrequencyBound2
            );
            tmpHistogramData.setLowerFrequencyBound(
                tmpFrequencyBound1 < tmpFrequencyBound2 ? tmpFrequencyBound1 : tmpFrequencyBound2
            );
            this.updateOutputTab();
            this.outputIsSaved = false;
        });
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="barLabelCheckBox">
        this.outputTab.getBarLabelCheckBox().setOnAction((ActionEvent e) -> {
            this.updateOutputTab();
        });
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="binLabelChoiceBox">
        this.outputTab.getBinLabelChoiceBox().setOnAction((ActionEvent e) -> {
            this.currentlyChosenBinLabelType = this.outputTab.getBinLabelChoiceBox().getValue();
            this.updateOutputTab();
        });
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="defaultButton">
        this.outputTab.getDefaultButton().setOnAction((ActionEvent e) -> {
            HistogramData tmpHistogramData = this.histogramDataManager.getHistogramData(this.currentlyChosenComparisonFeature);
            tmpHistogramData.setNumberOfBins(UserPreferences.getInstance().getDefaultNumberOfBins());
            tmpHistogramData.setUseRelativeFrequencies(false);
            tmpHistogramData.setLowerBinBorder(tmpHistogramData.getMinComparisonFeatureValue());
            tmpHistogramData.setUpperBinBorder(tmpHistogramData.getMaxComparisonFeatureValue());
            // Resets the frequency bounds
            tmpHistogramData.call();
            this.updateOutputTab();
        });
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="chartConfigurationButton">
        this.outputTab.getChartConfigurationButton().setOnAction((ActionEvent e) -> {
            //<editor-fold defaultstate="collapsed" desc="Checks">
            if (this.currentlyChosenComparisonFeature == null) {
                GeneralUtilities.showInfoDialog(this.applicationStage, GeneralUtilities.getUIText("InfoDescription.NoComparisonFeatureChosen.text"));
                return;
            }
            //</editor-fold>
            HistogramData tmpHistogramData = this.histogramDataManager.getHistogramData(this.currentlyChosenComparisonFeature);
            this.chartConfigurationDialog.getLowerBinBorderInputField().setText(Double.toString(tmpHistogramData.getLowerBinBorder()));
            this.chartConfigurationDialog.getUpperBinBorderInputField().setText(Double.toString(tmpHistogramData.getUpperBinBorder()));
            this.chartConfigurationDialog.getLowerFrequencyBoundInputField().setText(Double.toString(tmpHistogramData.getLowerFrequencyBound()));
            this.chartConfigurationDialog.getUpperFrequencyBoundInputField().setText(Double.toString(tmpHistogramData.getUpperFrequencyBound()));
            this.chartConfigurationDialog.setHeaderText(String.format(GeneralUtilities.getUIText("ChartConfigurationDialog.Header.text"), this.currentlyChosenComparisonFeature.toString()));
            this.chartConfigurationDialog.showAndWait();
        });
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="binBorderConfigurationButton">
        this.outputTab.getBinBorderConfigurationButton().setOnAction((ActionEvent e) -> {
            BinBorderConfigurationBox tmpBox;
            this.binBorderConfigurationBoxList.clear();
            int tmpIndex = 0;
            HistogramData tmpHistogramData = this.histogramDataManager.getHistogramData(this.currentlyChosenComparisonFeature);
            for (double tmpDouble : tmpHistogramData.getBinBorders()) {
                tmpBox = new BinBorderConfigurationBox(Double.toString(tmpDouble), Integer.toString(++tmpIndex));
                tmpBox.getAddButton().setOnAction(this.binBorderConfigurationDialogAddHandler);
                tmpBox.getRemoveButton().setOnAction(this.binBorderConfigurationDialogRemoveHandler);
                tmpBox.getTextField().setTextFormatter(GeneralUtilities.getSignedDecimalNumberTextFormatter());
                this.binBorderConfigurationBoxList.add(tmpBox);
                // Removing is disabled if there are only two bin borders
                tmpBox.getRemoveButton().setDisable(tmpHistogramData.getBinBorders().length <= 2);
            }
            this.binBorderConfigurationDialog.getBinBorderConfigurationBoxesBox().getChildren().setAll(this.binBorderConfigurationBoxList);
            this.binBorderConfigurationDialog.showAndWait();
        });
        //</editor-fold>
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="- ConfigurationDialog related">
        this.binBorderConfigurationDialog.getCancelButton().setOnAction((ActionEvent e) -> {
            this.binBorderConfigurationDialog.hide();
        });
        this.binBorderConfigurationDialog.getApplyButton().setOnAction((ActionEvent e) -> {
            ListIterator<BinBorderConfigurationBox> tmpIterator = this.binBorderConfigurationBoxList.listIterator();
            while (tmpIterator.hasNext()) {
                BinBorderConfigurationBox tmpBox = tmpIterator.next();
                // Remove all BinConfigurationBoxs with invalid textfields
                if (tmpBox.getTextField().getText() != null) {
                    try {
                        Double.parseDouble(tmpBox.getTextField().getText());
                    } catch (NumberFormatException ex) {
                        tmpIterator.remove();
                    }
                } else {
                    tmpIterator.remove();
                }
            }
            if (this.binBorderConfigurationBoxList.size() <= 1) {
                GeneralUtilities.showInfoDialog(this.applicationStage, GeneralUtilities.getUIText("InfoDescription.InvalidBinBorders.text"));
                return;
            }
            double[] tmpBinBorders = new double[this.binBorderConfigurationBoxList.size()];
            int tmpIndex = 0;
            for (BinBorderConfigurationBox tmpBox : this.binBorderConfigurationBoxList) {
                tmpBinBorders[tmpIndex] = Double.parseDouble(tmpBox.getTextField().getText());
                if (tmpIndex != 0 && tmpBinBorders[tmpIndex] <= tmpBinBorders[tmpIndex - 1]) {
                    // Every bin border has to be greater than the previous bin border
                    GeneralUtilities.showInfoDialog(this.applicationStage, GeneralUtilities.getUIText("InfoDescription.InvalidBinBorders.text"));
                    return;
                }
                tmpIndex++;
            }
            HistogramData tmpHistogramData = this.histogramDataManager.getHistogramData(this.currentlyChosenComparisonFeature);
            // Important: Do this first or else the new borders will not be used
            tmpHistogramData.setNumberOfBins(tmpBinBorders.length - 1);
            tmpHistogramData.setSpecificBinBorders(tmpBinBorders);
            this.outputIsSaved = false;
            // Resets the frequency bounds
            tmpHistogramData.call();
            // Update the reseted frequency bounds
            this.updateOutputTab();
            this.binBorderConfigurationDialog.hide();
        });
        this.binBorderConfigurationDialog.getDialogPane().addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent tmpEvent) -> {
            KeyCombination tmpKeyCombination = new KeyCodeCombination(KeyCode.ENTER);
            if (tmpKeyCombination.match(tmpEvent)) {
                this.binBorderConfigurationDialog.getApplyButton().fire();
            }
        });
        this.binBorderConfigurationDialog.getResetButton().setOnAction((ActionEvent e) -> {
            BinBorderConfigurationBox tmpBox;
            this.binBorderConfigurationBoxList.clear();
            int tmpIndex = 0;
            HistogramData tmpHistogramData = this.histogramDataManager.getHistogramData(this.currentlyChosenComparisonFeature);
            for (double tmpDouble : tmpHistogramData.getBinBorders()) {
                tmpBox = new BinBorderConfigurationBox(Double.toString(tmpDouble), Integer.toString(++tmpIndex));
                tmpBox.getAddButton().setOnAction(this.binBorderConfigurationDialogAddHandler);
                tmpBox.getRemoveButton().setOnAction(this.binBorderConfigurationDialogRemoveHandler);
                tmpBox.getTextField().setTextFormatter(GeneralUtilities.getSignedDecimalNumberTextFormatter());
                this.binBorderConfigurationBoxList.add(tmpBox);
                // Removing is disabled if there are only two bin borders
                tmpBox.getRemoveButton().setDisable(tmpHistogramData.getBinBorders().length <= 2);
            }
            this.binBorderConfigurationDialog.getBinBorderConfigurationBoxesBox().getChildren().setAll(this.binBorderConfigurationBoxList);
        });
        this.chartConfigurationDialog.getCancelButton().setOnAction((ActionEvent e) -> {
            this.chartConfigurationDialog.hide();
        });
        this.chartConfigurationDialog.getApplyButton().setOnAction((ActionEvent e) -> {
            HistogramData tmpHistogramData = this.histogramDataManager.getHistogramData(this.currentlyChosenComparisonFeature);
            if (this.isValidLowerBinBorder) {
                tmpHistogramData.setLowerBinBorder(Double.parseDouble(this.chartConfigurationDialog.getLowerBinBorderInputField().getText()));
                this.outputIsSaved = false;
            }
            if (this.isValidUpperBinBorder) {
                tmpHistogramData.setUpperBinBorder(Double.parseDouble(this.chartConfigurationDialog.getUpperBinBorderInputField().getText()));
                this.outputIsSaved = false;
            }
            // Resets the frequency bounds
            tmpHistogramData.call();
            // Set the new frequency bounds AFTER the binning
            if (this.isValidLowerFrequencyBound) {
                double tmpDouble = Double.parseDouble(this.chartConfigurationDialog.getLowerFrequencyBoundInputField().getText());
                if (tmpHistogramData.isUseRelativeFrequencies() && tmpDouble > 1.5) {
                    GeneralUtilities.showInfoDialog(
                        this.applicationStage, String.format(GeneralUtilities.getUIText("InfoDescription.RelativeFrequencyTooHigh.text"), tmpDouble)
                    );
                } else {
                    tmpHistogramData.setLowerFrequencyBound(tmpDouble);
                    this.outputIsSaved = false;
                }
            }
            if (this.isValidUpperFrequencyBound) {
                double tmpDouble = Double.parseDouble(this.chartConfigurationDialog.getUpperFrequencyBoundInputField().getText());
                if (tmpHistogramData.isUseRelativeFrequencies() && tmpDouble > 1.5) {
                    GeneralUtilities.showInfoDialog(
                        this.applicationStage, String.format(GeneralUtilities.getUIText("InfoDescription.RelativeFrequencyTooHigh.text"), tmpDouble)
                    );
                } else {
                    tmpHistogramData.setUpperFrequencyBound(tmpDouble);
                    this.outputIsSaved = false;
                }
            }
            this.updateOutputTab();
            this.chartConfigurationDialog.hide();
        });
        this.chartConfigurationDialog.getDialogPane().addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent tmpEvent) -> {
            KeyCombination tmpKeyCombination = new KeyCodeCombination(KeyCode.ENTER);
            if (tmpKeyCombination.match(tmpEvent)) {
                this.chartConfigurationDialog.getApplyButton().fire();
            }
        });
        this.chartConfigurationDialog.getResetButton().setOnAction((ActionEvent e) -> {
            HistogramData tmpHistogramData = this.histogramDataManager.getHistogramData(this.currentlyChosenComparisonFeature);
            this.chartConfigurationDialog.getLowerBinBorderInputField().setText(Double.toString(this.histogramDataManager.getHistogramData(this.currentlyChosenComparisonFeature).getLowerBinBorder()));
            this.chartConfigurationDialog.getUpperBinBorderInputField().setText(Double.toString(tmpHistogramData.getUpperBinBorder()));
            this.chartConfigurationDialog.getLowerFrequencyBoundInputField().setText(Double.toString(tmpHistogramData.getLowerFrequencyBound()));
            this.chartConfigurationDialog.getUpperFrequencyBoundInputField().setText(Double.toString(tmpHistogramData.getUpperFrequencyBound()));
        });
        this.chartConfigurationDialog.getDefaultButton().setOnAction((ActionEvent e) -> {
            HistogramData tmpHistogramData = this.histogramDataManager.getHistogramData(this.currentlyChosenComparisonFeature);
            this.chartConfigurationDialog.getLowerBinBorderInputField().setText(Double.toString(tmpHistogramData.getMinComparisonFeatureValue()));
            this.chartConfigurationDialog.getUpperBinBorderInputField().setText(Double.toString(tmpHistogramData.getMaxComparisonFeatureValue()));
            this.chartConfigurationDialog.getLowerFrequencyBoundInputField().setText(Double.toString(0.0));
            this.chartConfigurationDialog.getUpperFrequencyBoundInputField().setText(Double.toString(tmpHistogramData.getMaxFrequency()));
        });
        //</editor-fold>
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Set up the InputTab">
        if (!tmpIsLoaded) {
            this.inputTab.getButtonsTitledPane().setExpanded(true);
            this.inputTab.getInputType1ChoiceBox().setValue(InputType.SMILES);
            this.inputTab.getInputType2ChoiceBox().setValue(InputType.SMILES);
            this.inputTab.getJobStartButton().setDisable(true);
            this.inputTab.getJobCancelButton().setDisable(true);
            this.inputTab.getJobProgressBar().setVisible(false);
            this.inputTab.getJobInfoLabel().setText(GeneralUtilities.getUIText("InfoDescription.NewJob.text"));
        }
        //</editor-fold>
    }
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Public methods">
    //<editor-fold defaultstate="collapsed" desc="- Public properties">
    //<editor-fold defaultstate="collapsed" desc="Getters">
    /**
     * True if the output of this JobController is saved, false otherwise
     *
     * @return true if the output of this JobController is saved,
     * false otherwise
     */
    public boolean outputIsSaved() {
        return this.outputIsSaved;
    }
    
    /**
     * Get the InputTab of this JobController
     *
     * @return the InputTab
     */
    public InputTab getInputTab() {
        return this.inputTab;
    }
    
    /**
     * Get the JobTaskManager of this JobController or null if this
     * JobController was loaded
     *
     * @return the JobTaskManager
     */
    public JobTaskManager getJobTaskManager() {
        return this.jobTaskManager;
    }
    
    /**
     * Get the HistogramDataManager
     *
     * @return the HistogramDataManager
     */
    public HistogramDataManager getHistogramDataManager() {
        return this.histogramDataManager;
    }
    
    /**
     * Get the ComparisonFeature that is currently selected in the OutputTab
     *
     * @return the currently selected ComparisonFeature
     */
    public ComparisonFeature getCurrentlyChosenComparisonFeature() {
        return this.currentlyChosenComparisonFeature;
    }
    
    /**
     * Get the OutputTab of this JobController
     *
     * @return the OutputTab
     */
    public OutputTab getOutputTab() {
        return this.outputTab;
    }
    
    /**
     * Get the number that identifies this JobController in the current
     * application session
     *
     * @return the number of this JobController
     */
    public String getJobNumber() {
        return this.jobNumber;
    }
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Setters">
    /**
     * Set the HistogramDataManager
     *
     * @param tmpHistogramDataManager a HistogramDataManager
     */
    public void setHistogramDataManager(HistogramDataManager tmpHistogramDataManager) {
        this.histogramDataManager = tmpHistogramDataManager;
    }
    
    /**
     * Set whether the result of this JobController is saved or not
     *
     * @param tmpOutputIsSaved true if the output is saved, false otherwise
     */
    public void setOutputIsSaved(boolean tmpOutputIsSaved) {
        this.outputIsSaved = tmpOutputIsSaved;
    }
    
    /**
     * Set a value for the currentlyChosenComparisonFeature
     * 
     * @param tmpCurrentlyChosenComparisonFeature the value to be set
     */
    public void setCurrentlyChosenComparisonFeature(ComparisonFeature tmpCurrentlyChosenComparisonFeature) {
        this.currentlyChosenComparisonFeature = tmpCurrentlyChosenComparisonFeature;
    }
    //</editor-fold>
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="- Property change related methods">
    /**
     * Adds a PropertyChangeListener that listens for changes fired from this 
     * JobController
     * 
     * @param tmpListener a listener to be added
     */
    public void addPropertyChangeListener(PropertyChangeListener tmpListener) {
        this.propertyChangeSupport.addPropertyChangeListener(tmpListener);
    }
    
    /**
     * Removes a PropertyChangeListener
     * 
     * @param tmpListener a listener to be removed
     */
    public void removePropertyChangeListener(PropertyChangeListener tmpListener) {
        this.propertyChangeSupport.removePropertyChangeListener(tmpListener);
    }

    /**
     * Handle all PropertyChangeEvents that reach this JobController.
     * 
     * @param evt a PropertyChangeEvent that reached this JobController
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
            try {
                if (evt.getSource() instanceof JobTaskManager) {
                    switch (evt.getPropertyName()) {
                        case "Exception":
                            this.jobTaskManager.cancelWorkingProcess();
                            Exception tmpException = (Exception) evt.getNewValue();
                            String tmpOrigin = (String) evt.getOldValue();
                            Platform.runLater(() -> {
                                GeneralUtilities.showErrorDialog(this.applicationStage, tmpException.toString(), tmpOrigin);
                                GeneralUtilities.logException(Level.WARNING, tmpException);
                            });
                            break;
                        case "ParseAndCompareTasks progress":
                            synchronized (this) {
                                if (this.jobTaskManager.isWorking()) {
                                    double tmpProgressValue = (double) evt.getNewValue();
                                    Platform.runLater(() -> {
                                    // The JavaFX Application can be only updated from the JavaFX Application Thread.
                                    // NOTE: Applications should avoid flooding JavaFX with too many pending Runnables. Otherwise, the application may become unresponsive.
                                        this.inputTab.getJobProgressBar().setProgress(tmpProgressValue);
                                        this.inputTab.getJobInfoLabel().setText(
                                            String.format(GeneralUtilities.getUIText("InfoDescription.ReadingInputProgress.text"), (int) (tmpProgressValue * 100))
                                        );
                                    });
                                }
                            }
                            break;
                        case "ParseAndCompareTasks finished":
                            synchronized (this) {
                                if (this.jobTaskManager.isWorking()) {
                                    this.jobTaskManager.cancelWorkingProcess();
                                    this.histogramDataManager = this.jobTaskManager.getHistogramDataManager();
                                    this.histogramDataManager.setCalculationStartTime(this.calculationStartTime);
                                    this.histogramDataManager.setCalculationFinishTime(System.currentTimeMillis());
                                    Platform.runLater(() -> {
                                        this.inputTab.getJobStartButton().setVisible(true);
                                        this.inputTab.getJobCancelButton().setVisible(false);
                                        this.inputTab.getJobProgressBar().setVisible(false);
                                        this.inputTab.getJobInfoLabel().setText(GeneralUtilities.getUIText("InfoDescription.FinishedJob.text"));
                                        // Set this first to avoid NullPointerExcpetions
                                        this.currentlyChosenComparisonFeature = this.histogramDataManager.getComparisonFeatureSet().iterator().next();
                                        this.outputTab.setBinLabelChoiceBoxItems(Set.of(BinLabelType.values()));
                                        this.outputTab.setChartChoiceBoxItems(this.histogramDataManager.getComparisonFeatureSet());
                                        this.outputTab.getBinLabelChoiceBox().setValue(this.currentlyChosenBinLabelType);
                                        this.outputTab.getChartChoiceBox().setValue(this.currentlyChosenComparisonFeature);
                                        this.outputTab.getInfoLabel().setText(String.format(
                                                GeneralUtilities.getUIText("OutputTab.infoLabel.text"), this.jobNumber,
                                                this.histogramDataManager.getNumberOfComparedPairs(), this.histogramDataManager.getInputFile1(),
                                                this.histogramDataManager.getInputFile2()
                                        ));
                                        // Displays OutputTab and closes InputTab
                                        this.propertyChangeSupport.firePropertyChange("Created charts successfully", false, true);
                                    });
                                }
                            }
                            break;
                        case "Unpaired inputs":
                            Platform.runLater(() -> {
                                GeneralUtilities.showInfoDialog(this.applicationStage, String.format(
                                        GeneralUtilities.getUIText("InfoDescription.UnpairedStrings.text"),
                                        evt.getNewValue()
                                ));
                            });
                            break;
                        default:
                            GeneralUtilities.logMessage(
                                Level.INFO, "JobController", "propertyChange()",
                                String.format(GeneralUtilities.getUIText("Logging.UnexptectedPropertyChange.text"), evt.getPropertyName())
                            );
                            break;
                    }
                }
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    GeneralUtilities.showErrorDialog(this.applicationStage, ex.toString(), "JobController.propertyChange()");
                    GeneralUtilities.logException(Level.WARNING, ex);
                });
            }
    }
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="- Other methods">
    /**
     * Create a BarChart from the given HistogramData. Also add a mouse click 
     * listener to every bar.
     * 
     * @param tmpHistogramData the HistogramData from which the chart will be 
     * created
     * @param tmpBinLabelType specifies the type of labeling used for the bins
     * @return the created BarChart or an error label if something went wrong
     */
    public Region createChart(HistogramData tmpHistogramData, BinLabelType tmpBinLabelType) {
        try {
            //<editor-fold defaultstate="collapsed" desc="Set up chart">
            CategoryAxis tmpXAxis = new CategoryAxis();
            tmpXAxis.setLabel(GeneralUtilities.getUIText("OutputTab.xAxisLabel.text"));
            tmpXAxis.setTickLabelFill(Color.color(0.2, 0.2, 0.2));
            final NumberAxis tmpYAxis = new NumberAxis();
            tmpYAxis.setLabel(GeneralUtilities.getUIText("OutputTab.yAxisLabel.text"));
            tmpYAxis.setTickLabelFill(Color.color(0.2, 0.2, 0.2));
            tmpYAxis.setAutoRanging(false);
            BarChart<String, Number> tmpChart = new BarChart<>(tmpXAxis, tmpYAxis);
            tmpChart.setBarGap(0.0);
            tmpChart.setLegendVisible(false);
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="YAxis labeling">
            tmpYAxis.heightProperty().addListener(new ChangeListener<Number>() {
                
                /**
                 * True if the last resize of the chart pane was done 
                 * automatically, false otherwise
                 */
                private boolean lastResizeWasAutomatic1 = false;
                
                /**
                 * True if the last resize of the application stage was done 
                 * automatically, false otherwise
                 */
                private boolean lastResizeWasAutomatic2 = false;
                
                @Override
                public void changed(ObservableValue<? extends Number> tmpObservableValue, Number tmpOldAxisHeight, Number tmpNewAxisHeight) {
                    double tmpUpperBound = tmpHistogramData.getUpperFrequencyBound();
                    double tmpLowerBound = tmpHistogramData.getLowerFrequencyBound();
                    double tmpBoundDifference = tmpUpperBound - tmpLowerBound;
                    double tmpTickSpacing = 25.0;
                    int tmpMaxTickNumber = (int) ((double) tmpNewAxisHeight / tmpTickSpacing);
                    double[] tmpTickUnits = {1, 2.5, 5};
                    double tmpResultTickUnit = -1;
                    if (JobController.this.outputTab.getRelativeFrequenciesCheckBox().isSelected()) {
                        double[] tmpFactors = {0.0000001, 0.000001, 0.00001, 0.0001, 0.001, 0.01, 0.1};
                        outerLoop:
                        for (double tmpFactor : tmpFactors) {
                            for (double tmpTickUnit : tmpTickUnits) {
                                if (tmpMaxTickNumber * tmpFactor * tmpTickUnit >= tmpBoundDifference) {
                                    tmpResultTickUnit = tmpFactor * tmpTickUnit;
                                    break outerLoop;
                                }
                            }
                        }
                        tmpResultTickUnit = tmpResultTickUnit == -1 ? 0.0000001 : tmpResultTickUnit;
                        tmpYAxis.setMinorTickVisible(true);
                    } else {
                        long[] tmpFactors = {1L, 10L, 100L, 1000L, 10000L, 100000L, 1000000L, 10000000L, 100000000L, 1000000000L, 10000000000L, 100000000000L, 1000000000000L, 10000000000000L, 100000000000000L};
                        outerLoop:
                        for (long tmpFactor : tmpFactors) {
                            for (double tmpTickUnit : tmpTickUnits) {
                                if (tmpMaxTickNumber * tmpFactor * tmpTickUnit >= tmpBoundDifference) {
                                    tmpResultTickUnit = tmpFactor * tmpTickUnit;
                                    break outerLoop;
                                }
                            }
                        }
                        tmpResultTickUnit = tmpResultTickUnit == -1 ? 1000000000000000L : tmpResultTickUnit;
                        tmpYAxis.setMinorTickVisible(tmpResultTickUnit >= 5);
                    }
                    tmpYAxis.setTickUnit(tmpResultTickUnit);
                    tmpYAxis.setLowerBound(tmpLowerBound);
                    tmpYAxis.setUpperBound((((int) (tmpUpperBound  * 1.02 / tmpResultTickUnit)) + 1) * tmpResultTickUnit);
                    //<editor-fold defaultstate="collapsed" desc="Layout y-axis correctly">
                    // If a chart has just been created the layout of the y-axis may be incorrect.
                    // This section tries to resize the y-axis and later undo the resize in an attempt to thus correct the layout of the y-axis
                    if ((double) tmpOldAxisHeight == 0.0 && JobController.this.applicationStage.isMaximized() && !JobController.this.histogramDataManager.getHistogramData(currentlyChosenComparisonFeature).isUseRelativeFrequencies()) {
                        JobController.this.outputTab.getChartPane().setMaxHeight(JobController.this.outputTab.getChartPane().getHeight() - 10);
                        this.lastResizeWasAutomatic1 = true;
                    } else if (this.lastResizeWasAutomatic1) {
                        JobController.this.outputTab.getChartPane().setMaxHeight(Double.MAX_VALUE);
                        JobController.this.outputTab.getChartPane().autosize();
                        this.lastResizeWasAutomatic1 = false;
                    } else if ((double) tmpOldAxisHeight == 0.0 || this.lastResizeWasAutomatic2) {
                        JobController.this.applicationStage.setHeight(JobController.this.applicationStage.getHeight() + (this.lastResizeWasAutomatic2 ? -5.8 : 5));
                        this.lastResizeWasAutomatic2 = !this.lastResizeWasAutomatic2;
                    }
                    //</editor-fold>
                }
            });
            //</editor-fold>
            double[] tmpFrequencyData = tmpHistogramData.getFrequencies();
            List<XYChart.Data<String, Number>> tmpDataList = new LinkedList<>();
            for (int i = tmpFrequencyData.length - 1; i >= 0; i--) {
                //<editor-fold defaultstate="collapsed" desc="Initialize XYChart.Data">
                double tmpFrequency = tmpFrequencyData[i];
                String tmpCategory;
                int tmpPrecision = tmpHistogramData.getComparisonFeauture().isContinuous() ? 3 : 2;
                switch (tmpBinLabelType) {
                    case MIN:
                        tmpCategory = tmpHistogramData.getComparisonFeauture().isContinuous() ?
                                String.format("%." + tmpPrecision + "f", tmpHistogramData.getBinBorders()[i]) :
                                String.format("%d", (int) Math.ceil(tmpHistogramData.getBinBorders()[i]));
                        // Check for and eliminate duplicate categories
                        outerLoop:
                        while (true) {
                            for (XYChart.Data<String, Number> tmpData : tmpDataList) {
                                if (Double.parseDouble(tmpData.getXValue()) == Double.parseDouble((tmpCategory))) {
                                    tmpCategory = String.format("%." + (++tmpPrecision) + "f", tmpHistogramData.getBinBorders()[i]);
                                    continue outerLoop;
                                }
                            }
                            break;
                        }
                        break;
                    case MAX:
                        tmpCategory = tmpHistogramData.getComparisonFeauture().isContinuous() ?
                                String.format("%." + tmpPrecision + "f", tmpHistogramData.getBinBorders()[i + 1]) :
                                String.format("%d", (int) Math.ceil(tmpHistogramData.getBinBorders()[i + 1]));
                        // Check for and eliminate duplicate categories
                        outerLoop:
                        while (true) {
                            for (XYChart.Data<String, Number> tmpData : tmpDataList) {
                                if (Double.parseDouble(tmpData.getXValue()) == Double.parseDouble((tmpCategory))) {
                                    tmpCategory = String.format("%." + (++tmpPrecision) + "f", tmpHistogramData.getBinBorders()[i + 1]);
                                    continue outerLoop;
                                }
                            }
                            break;
                        }
                        break;
                    case MEAN:
                        tmpPrecision = tmpHistogramData.getComparisonFeauture().isContinuous() ? 3 : 1;
                        tmpCategory = String.format("%." + tmpPrecision + "f", (tmpHistogramData.getBinBorders()[i] + tmpHistogramData.getBinBorders()[i + 1]) / 2.0);
                        // Check for and eliminate duplicate categories
                        outerLoop:
                        while (true) {
                            for (XYChart.Data<String, Number> tmpData : tmpDataList) {
                                if (Double.parseDouble(tmpData.getXValue()) == Double.parseDouble((tmpCategory))) {
                                    tmpCategory = String.format("%." + (++tmpPrecision) + "f", (tmpHistogramData.getBinBorders()[i] + tmpHistogramData.getBinBorders()[i + 1]) / 2.0);
                                    continue outerLoop;
                                }
                            }
                            break;
                        }
                        break;
                    case INTERVAL:
                        if (i == tmpFrequencyData.length - 1 && tmpHistogramData.getComparisonFeauture().isContinuous()) {
                            tmpCategory = String.format(
                                "[%." + tmpPrecision + "f - %." + tmpPrecision +  "f]", 
                                tmpHistogramData.getBinBorders()[i], tmpHistogramData.getBinBorders()[i + 1]
                            );
                        } else if (i == tmpFrequencyData.length - 1) {
                            tmpCategory = String.format(
                                "[%d - %d]",
                                (int) Math.ceil(tmpHistogramData.getBinBorders()[i]), (int) Math.ceil(tmpHistogramData.getBinBorders()[i + 1])
                            );
                        } else if (tmpHistogramData.getComparisonFeauture().isContinuous()) {
                            tmpCategory = String.format(
                                "[%." + tmpPrecision + "f - %." + tmpPrecision +  "f)",
                                tmpHistogramData.getBinBorders()[i], tmpHistogramData.getBinBorders()[i + 1]
                            );
                        } else {
                            tmpCategory = String.format(
                                "[%d - %d)",
                                (int) Math.ceil(tmpHistogramData.getBinBorders()[i]), (int) Math.ceil(tmpHistogramData.getBinBorders()[i + 1])
                            );
                        }
                        // Check for and eliminate duplicate categories
                        outerLoop:
                        while (true) {
                            for (XYChart.Data<String, Number> tmpData : tmpDataList) {
                                if (tmpData.getXValue().equals(tmpCategory)) {
                                    tmpCategory = String.format(
                                        "[%." + (++tmpPrecision) + "f - %." + (++tmpPrecision) +  "f)",
                                        tmpHistogramData.getBinBorders()[i], tmpHistogramData.getBinBorders()[i + 1]
                                    );
                                    continue outerLoop;
                                }
                            }
                            break;
                        }
                        break;
                    default:
                        tmpCategory = "";
                }
                XYChart.Data<String, Number> tmpData = new XYChart.Data<>(tmpCategory, tmpFrequency);
                tmpDataList.add(tmpData);
                //</editor-fold>
                //<editor-fold defaultstate="collapsed" desc="Set up tmpMouseClickListener">
                StackPane tmpMouseClickListener = new StackPane();
                tmpMouseClickListener.setAlignment(Pos.TOP_CENTER);
                //<editor-fold defaultstate="collapsed" desc="Set up bar labeling">
                if (this.outputTab.getBarLabelCheckBox().isSelected()) {
                    Label tmpBarLabel = tmpHistogramData.isUseRelativeFrequencies() ? new Label(new DecimalFormat("0.000").format(tmpFrequency)) : new Label(new DecimalFormat("###").format(tmpFrequency));
                    tmpMouseClickListener.getChildren().add(tmpBarLabel);
                    tmpYAxis.heightProperty().addListener((ObservableValue<? extends Number> tmpObservableValue, Number tmpOldAxisHeight, Number tmpNewAxisHeight) -> {
                        double tmpMouseClickListenerHeight = tmpMouseClickListener.boundsInParentProperty().getValue().getHeight();
                        if (tmpMouseClickListenerHeight >= (double) tmpNewAxisHeight) {
                            tmpBarLabel.setTranslateY((tmpMouseClickListenerHeight + 5) - (double) tmpNewAxisHeight);
                            tmpBarLabel.setTextFill(Paint.valueOf("White"));
                        } else {
                            tmpBarLabel.setTranslateY(-20);
                            tmpBarLabel.setTextFill(Paint.valueOf("Black"));
                        }
                    });
                }
                //</editor-fold>
                List<ComparisonResult> tmpComparisonResultList = tmpHistogramData.getBin(i);
                int tmpBinNumber = i + 1;
                // The list may be empty even though the bin is not
                if (!tmpComparisonResultList.isEmpty()) {
                    tmpMouseClickListener.setOnMouseClicked((MouseEvent e) -> {
                        MoleculePairDisplayerController tmpController = new MoleculePairDisplayerController(
                                tmpComparisonResultList, this.applicationStage, this.fileChooser, 
                                tmpBinNumber, tmpHistogramData.getComparisonFeauture(), tmpFrequency,
                                tmpHistogramData.getBinBorders()[tmpBinNumber - 1], tmpHistogramData.getBinBorders()[tmpBinNumber]
                        );
                    });
                    tmpMouseClickListener.setOnMouseEntered((MouseEvent e) -> {
                        this.scene.setCursor(Cursor.HAND);
                    });
                    tmpMouseClickListener.setOnMouseExited((MouseEvent e) -> {
                        this.scene.setCursor(Cursor.DEFAULT);
                    });
                } else {
                    tmpMouseClickListener.setOnMouseClicked((MouseEvent e) -> {
                        GeneralUtilities.showInfoDialog(this.applicationStage, GeneralUtilities.getUIText("InfoDescription.NoMoleculesPairs.text"));
                    });
                }
                tmpData.setNode(tmpMouseClickListener);
                //</editor-fold>
            }
            //<editor-fold defaultstate="collapsed" desc="Set chart data">
            XYChart.Series<String, Number> tmpSeries = new XYChart.Series<>();
            ListIterator<XYChart.Data<String, Number>> tmpIterator = tmpDataList.listIterator(tmpDataList.size());
            while (tmpIterator.hasPrevious()) {
                tmpSeries.getData().add(tmpIterator.previous());
            }
            tmpChart.getData().add(tmpSeries);
            //</editor-fold>
            return tmpChart;
        } catch (Exception ex) {
            GeneralUtilities.showErrorDialog(
                this.applicationStage, 
                String.format(GeneralUtilities.getUIText("ErrorDescription.ChartCreationFailed.text"), tmpHistogramData.getComparisonFeauture().toString()), 
                "JobController.createChart()"
            );
            GeneralUtilities.logException(Level.WARNING, ex);
            return new Label(String.format(GeneralUtilities.getUIText("InfoDescription.ChartPlaceHolder.text"), tmpHistogramData.getComparisonFeauture().toString()));
        }
    }
    //</editor-fold>
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Private methods">
    /**
     * Update the outputTab. Set the new chart and the states of some controls
     */
    private void updateOutputTab() {
        HistogramData tmpHistogramData = this.histogramDataManager.getHistogramData(this.currentlyChosenComparisonFeature);
        this.outputTab.getFrequency1Slider().setValue(tmpHistogramData.getUpperFrequencyBound());
        this.outputTab.getFrequency2Slider().setValue(tmpHistogramData.getLowerFrequencyBound());
        this.outputTab.getBinBorder1Slider().setValue(tmpHistogramData.getUpperBinBorder());
        this.outputTab.getBinBorder2Slider().setValue(tmpHistogramData.getLowerBinBorder());
        this.outputTab.getNumberOfBinsTextField().setText(Integer.toString(tmpHistogramData.getNumberOfBins()));
        this.outputTab.getRelativeFrequenciesCheckBox().setSelected(tmpHistogramData.isUseRelativeFrequencies());
        this.currentlyShownChart = this.createChart(tmpHistogramData, this.currentlyChosenBinLabelType);
        this.outputTab.setComparisonFeatureChart(this.currentlyShownChart);
        if (this.currentlyShownChart instanceof Label) {
            this.outputTab.getImageButton().setDisable(true);
            this.outputTab.getSummaryReportButton().setDisable(true);
            this.outputTab.getBinLabelChoiceBox().setDisable(true);
            this.outputTab.getChartConfigurationButton().setDisable(true);
            this.outputTab.getDefaultButton().setDisable(true);
            this.outputTab.getBinBorderConfigurationButton().setDisable(true);
            this.outputTab.getRelativeFrequenciesCheckBox().setDisable(true);
            this.outputTab.getNumberOfBinsTextField().setDisable(true);
            this.outputTab.getBinBorder1Slider().setDisable(true);
            this.outputTab.getBinBorder2Slider().setDisable(true);
            this.outputTab.getFrequency1Slider().setDisable(true);
            this.outputTab.getFrequency2Slider().setDisable(true);
        } else if (this.currentlyShownChart instanceof BarChart) {
            this.outputTab.getImageButton().setDisable(false);
            this.outputTab.getSummaryReportButton().setDisable(false);
            this.outputTab.getBinLabelChoiceBox().setDisable(false);
            this.outputTab.getChartConfigurationButton().setDisable(false);
            this.outputTab.getDefaultButton().setDisable(false);
            this.outputTab.getBinBorderConfigurationButton().setDisable(false);
            this.outputTab.getRelativeFrequenciesCheckBox().setDisable(false);
            this.outputTab.getNumberOfBinsTextField().setDisable(false);
            this.outputTab.getBinBorder1Slider().setDisable(false);
            this.outputTab.getBinBorder2Slider().setDisable(false);
            this.outputTab.getFrequency1Slider().setDisable(false);
            this.outputTab.getFrequency2Slider().setDisable(false);
        }
    }
    //</editor-fold>
    
}