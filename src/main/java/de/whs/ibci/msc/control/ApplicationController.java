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

import de.whs.ibci.msc.model.JobTaskManager;
import de.whs.ibci.msc.utility.GeneralUtilities;
import de.whs.ibci.msc.utility.MSCConstants;
import de.whs.ibci.msc.model.HistogramDataManager;
import de.whs.ibci.msc.model.ComparisonFeature;
import de.whs.ibci.msc.model.ComparisonResult;
import de.whs.ibci.msc.model.HistogramData;
import de.whs.ibci.msc.utility.GuiUtilities;
import de.whs.ibci.msc.view.MainView;
import de.whs.ibci.msc.view.PreferencesConfigurationDialog;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Entry point and main controller of this JavaFX application. Handles events 
 * for the menu-bar and general controls. Controls the creation and deletion of 
 * JobControllers
 *
 * @author Jan-Mathis Hein
 */
public class ApplicationController extends Application implements PropertyChangeListener {
    
    //<editor-fold defaultstate="collapsed" desc="Private instance variables">
    /**
     * True if the input for the number of parallel threads given by the user in
     * the PreferencesConfigurationDialog is valid, false otherwise
     */
    private boolean isValidNumberOfParallelThreads;
    
    /**
     * True if the input for the default number of bins given by the user in
     * the PreferencesConfigurationDialog is valid, false otherwise
     */
    private boolean isValidDefaultNumberOfBins;
    
    /**
     * True if the input for the maximal number of molecule pairs to save given
     * by the user in the PreferencesConfigurationDialog is valid, false otherwise
     */
    private boolean isValidMaximalNumberOfMoleculePairsToSave;
    
    /**
     * DirectoryChooser that is used to choose the default directory for 
     * different file choosers
     */
    private final DirectoryChooser defaultDirectoryChooser = new DirectoryChooser();
    
    /**
     * Directory from where the last output file was loaded or directory where 
     * the last output file was saved
     */
    private File lastOutputDirectoryParent;
    
    /**
     * DirectoryChooser that is used to choose the directory for job saving and 
     * loading
     */
    private final DirectoryChooser outputDirectoryChooser = new DirectoryChooser();
    
    /**
     * Number of JobControllers created in this session of the application
     */
    private int numberOfCreatedJobControllers = 0;
    
    /**
     * The JobController which is currently active and shown
     */
    private JobController activeJobController;
    
    /**
     * Queue of currently opened JobControllers
     */
    private final Queue<JobController> jobControllerList = new ConcurrentLinkedQueue<>();
    
    /**
     * Main window of this application. This variable must be effectively final
     */
    private MainView view;
    
    /**
     * Dialog in which some preferences can be configured by the user
     */
    private PreferencesConfigurationDialog preferencesConfigurationDialog;
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Public methods">
    /**
     * The entry point for this JavaFX application. Starts and intializes the 
     * graphical user interface. Checks the screen size, the java version, 
     * whether this is the only currently running instance and whether the 
     * preferneces can be loaded. Sets up the file choosers and handles menu-bar
     * and general controls events.
     * 
     * NOTE: This method is called on the JavaFX Application Thread. 
     * 
     * @param primaryStage the primary stage for this application, onto which 
     * the application scene is set
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            //<editor-fold defaultstate="collapsed" desc="Checks">
            String tmpJavaVersion = System.getProperty("java.version");
            if (tmpJavaVersion.compareTo(MSCConstants.JAVA_VERSION) < 0) {
                GeneralUtilities.showErrorDialog(null, String.format(
                        GeneralUtilities.getUIText("ErrorDescription.WrongVersion.text"), 
                        tmpJavaVersion, MSCConstants.JAVA_VERSION), 
                    "ApplicationController.start()"
                );
                System.exit(-1);
            }
            if (!MSCConstants.MSC_FILES_DIRECTORY.isDirectory()) {
                MSCConstants.MSC_FILES_DIRECTORY.mkdirs();
            }
            if (!GeneralUtilities.isSingleInstance()) {
                GeneralUtilities.showErrorDialog(
                    null, 
                    GeneralUtilities.getUIText("ErrorDescription.NotSingleInstance.text") + " \n" + MSCConstants.MSC_FILES_DIRECTORY.getPath(), 
                    "ApplicationController.start()"
                );
                System.exit(-1);
            }
            if (Toolkit.getDefaultToolkit().getScreenSize().height < MSCConstants.MINIMAL_MAIN_WINDOW_HEIGHT || 
                Toolkit.getDefaultToolkit().getScreenSize().height < MSCConstants.MINIMAL_MOLECULE_DISPLAYER_HEIGHT ||
                Toolkit.getDefaultToolkit().getScreenSize().width < MSCConstants.MINIMAL_MAIN_WINDOW_WIDTH || 
                Toolkit.getDefaultToolkit().getScreenSize().width < MSCConstants.MINIMAL_MOLECULE_DISPLAYER_WIDTH) {
                GeneralUtilities.showErrorDialog(null, GeneralUtilities.getUIText("ErrorDescription.ScreenTooSmall.text"), "ApplicationController.start()");
                System.exit(-1);
            }
            // Do this after checking for single instance or 
            // otherwise an additional log-file will be created
            GeneralUtilities.initializeLogger();
            try {
                // Check if the preferences are available
                UserPreferences.getInstance();
            } catch (SecurityException | IllegalStateException ex) {
                GeneralUtilities.showErrorDialog(null, GeneralUtilities.getUIText("ErrorDescription.CanNotInstantiatePreferences.text"), "ApplicationController.start()");
                GeneralUtilities.logException(Level.WARNING, ex);
                System.exit(-1);
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Start and show GUI">
            GeneralUtilities.logMessage(Level.INFO, "", "", GeneralUtilities.getUIText("Logging.ApplicationStart.text"));
            this.view = new MainView(primaryStage);
            this.view.getPrimaryStage().setMinHeight(MSCConstants.MINIMAL_MAIN_WINDOW_HEIGHT);
            this.view.getPrimaryStage().setMinWidth(MSCConstants.MINIMAL_MAIN_WINDOW_WIDTH);
            this.view.getPrimaryStage().setHeight(MSCConstants.MINIMAL_MAIN_WINDOW_HEIGHT);
            this.view.getPrimaryStage().setWidth(MSCConstants.MINIMAL_MAIN_WINDOW_WIDTH);
            this.view.getPrimaryStage().show();
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Set up the PreferencesConfigurationDialog">
            this.preferencesConfigurationDialog = new PreferencesConfigurationDialog(this.view.getPrimaryStage(), GeneralUtilities.getUIText("PreferencesConfigurationDialog.title.text"));
            this.preferencesConfigurationDialog.getNumberOfParallelThreadsField().setTextFormatter(new TextFormatter<>((TextFormatter.Change tmpChange) -> {
                if (!tmpChange.getControlNewText().matches("\\d*")) {
                    tmpChange.setText("");
                }
                try {
                    if (Integer.parseInt(tmpChange.getControlNewText()) == 0) {
                        tmpChange.getControl().setStyle("-fx-background-color: red;");
                        this.isValidNumberOfParallelThreads = false;
                    }
                    tmpChange.getControl().setStyle("-fx-background-color: white;");
                    this.isValidNumberOfParallelThreads = true;
                } catch (NumberFormatException ex) {
                    tmpChange.getControl().setStyle("-fx-background-color: red;");
                    this.isValidNumberOfParallelThreads = false;
                }
                return tmpChange;
            }));
            this.preferencesConfigurationDialog.getDefaultNumberOfBinsField().setTextFormatter(new TextFormatter<>((TextFormatter.Change tmpChange) -> {
                if (!tmpChange.getControlNewText().matches("\\d*")) {
                    tmpChange.setText("");
                }
                try {
                    if (Integer.parseInt(tmpChange.getControlNewText()) == 0) {
                        tmpChange.getControl().setStyle("-fx-background-color: red;");
                        this.isValidDefaultNumberOfBins = false;
                    }
                    tmpChange.getControl().setStyle("-fx-background-color: white;");
                    this.isValidDefaultNumberOfBins = true;
                } catch (NumberFormatException ex) {
                    tmpChange.getControl().setStyle("-fx-background-color: red;");
                    this.isValidDefaultNumberOfBins = false;
                }
                return tmpChange;
            }));
            this.preferencesConfigurationDialog.getMaximalNumberOfMoleculePairsToSaveField().setTextFormatter(new TextFormatter<>((TextFormatter.Change tmpChange) -> {
                if (!tmpChange.getControlNewText().matches("\\d*")) {
                    tmpChange.setText("");
                }
                try {
                    Integer.parseInt(tmpChange.getControlNewText());
                    tmpChange.getControl().setStyle("-fx-background-color: white;");
                    this.isValidMaximalNumberOfMoleculePairsToSave = true;
                } catch (NumberFormatException ex) {
                    tmpChange.getControl().setStyle("-fx-background-color: red;");
                    this.isValidMaximalNumberOfMoleculePairsToSave = false;
                }
                return tmpChange;
            }));
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Event handling">
            //<editor-fold defaultstate="collapsed" desc="- General controls related">
            this.view.getPrimaryStage().setOnCloseRequest((WindowEvent e) -> {
                Optional<ButtonType> tmpResult = GeneralUtilities.showConfirmationDialog(
                    this.view.getPrimaryStage(), GeneralUtilities.getUIText("ConfirmationDialog.ClosingApplicationContent.text"),
                    GeneralUtilities.getUIText("ConfirmationDialog.ClosingTitle.text"), 
                    GeneralUtilities.getUIText("ConfirmationDialog.ClosingApplicationHeader.text")
                );
                if (tmpResult.isPresent() && tmpResult.get().getButtonData() == ButtonBar.ButtonData.APPLY) {
                    Platform.exit();
                } else {
                    e.consume();
                }
            });
            this.view.getCenterButton().setOnAction((ActionEvent e) -> {
                this.view.getPrimaryStage().centerOnScreen();
            });
            this.view.getMinButton().setOnAction((ActionEvent e) -> {
                this.view.getPrimaryStage().setMaximized(false);
                this.view.getPrimaryStage().setHeight(MSCConstants.MINIMAL_MAIN_WINDOW_HEIGHT);
                this.view.getPrimaryStage().setWidth(MSCConstants.MINIMAL_MAIN_WINDOW_WIDTH);
            });
            this.view.getMaxButton().setOnAction((ActionEvent e) -> {
                this.view.getPrimaryStage().setMaximized(true);
            });
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="- Menu bar related">
            this.view.getSaveJobMenuItem().setOnAction((ActionEvent e) -> {
                if (this.activeJobController == null || this.activeJobController.getHistogramDataManager() == null) {
                    GeneralUtilities.showInfoDialog(this.view.getPrimaryStage(), GeneralUtilities.getUIText("InfoDescription.SavingNotPossible.text"));
                    return;
                }
                if (!UserPreferences.getInstance().getOutputDir().isEmpty() && new File(UserPreferences.getInstance().getOutputDir()).isDirectory()) {
                    this.outputDirectoryChooser.setInitialDirectory(new File(UserPreferences.getInstance().getOutputDir()));
                } else {
                    this.outputDirectoryChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
                }
                if (this.lastOutputDirectoryParent != null && this.lastOutputDirectoryParent.isDirectory()) {
                    this.outputDirectoryChooser.setInitialDirectory(this.lastOutputDirectoryParent);
                }
                this.outputDirectoryChooser.setTitle(GeneralUtilities.getUIText("FileChooserTitle.SaveOutput.text"));
                final File tmpDirectory = this.outputDirectoryChooser.showDialog(this.view.getPrimaryStage());
                if (tmpDirectory == null) {
                    return;
                }
                if (tmpDirectory.isDirectory()) {
                    if (tmpDirectory.list() != null && tmpDirectory.list().length != 0) {
                        Optional<ButtonType> tmpResult = GeneralUtilities.showConfirmationDialog(
                            this.view.getPrimaryStage(), GeneralUtilities.getUIText("ConfirmationDialog.DirectoryAlreadyExistsContent.text"),
                            GeneralUtilities.getUIText("ConfirmationDialog.SelectionTitle.text"), 
                            GeneralUtilities.getUIText("ConfirmationDialog.DirectoryAlreadyExistsHeader.text")
                        );
                        if (tmpResult.isPresent() && tmpResult.get().getButtonData() == ButtonBar.ButtonData.APPLY) {
                            new File(tmpDirectory.getAbsolutePath() + File.separator + "output.ser").delete();
                            new File(tmpDirectory.getAbsolutePath() + File.separator + "README.txt").delete();
                            new File(tmpDirectory.getAbsolutePath() + File.separator + "moleculeSet1.txt").delete();
                            new File(tmpDirectory.getAbsolutePath() + File.separator + "moleculeSet2.txt").delete();
                        } else {
                            return;
                        }
                    }
                } else {
                    if (!tmpDirectory.mkdirs()) {
                        GeneralUtilities.showErrorDialog(this.view.getPrimaryStage(), GeneralUtilities.getUIText("ErrorDescription.CanNotCreateDirectory.text"), "ApplicationController.view.getSaveJobMenuItem().setOnAction()");
                    }
                }
                this.lastOutputDirectoryParent = tmpDirectory.getParentFile();
                final JobController tmpJobController = this.activeJobController;
                final HistogramDataManager tmpHistogramDataManager = tmpJobController.getHistogramDataManager();
                Task<Void> tmpTask = new Task<Void>() {

                    @Override
                    protected Void call() throws Exception {
                        File tmpFirstMoleculeSetFile = new File(tmpDirectory.getAbsolutePath() + File.separator + MSCConstants.MOLECULE_SET_1_FILE);
                        File tmpSecondMoleculeSetFile = new File(tmpDirectory.getAbsolutePath() + File.separator + MSCConstants.MOLECULE_SET_2_FILE);
                        File tmpOutputFile = new File(tmpDirectory.getAbsolutePath() + File.separator + MSCConstants.OUTPUT_FILE_NAME);
                        File tmpInfoFile = new File(tmpDirectory.getAbsolutePath() + File.separator + MSCConstants.INFO_FILE_NAME);
                        try (
                            ObjectOutputStream tmpObjectOutputStream = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(tmpOutputFile), 64 * 1024));
                            BufferedWriter tmpInfoFileWritter = new BufferedWriter(new FileWriter(tmpInfoFile));
                            BufferedWriter tmpFirstMoleculeSetFileWritter = new BufferedWriter(new FileWriter(tmpFirstMoleculeSetFile));
                            BufferedWriter tmpSecondMoleculeSetFileWritter = new BufferedWriter(new FileWriter(tmpSecondMoleculeSetFile));
                        ) {
                            //<editor-fold defaultstate="collapsed" desc="Write output file">
                            synchronized (tmpHistogramDataManager) {
                                tmpObjectOutputStream.writeObject(tmpHistogramDataManager);
                            }
                            //</editor-fold>
                            //<editor-fold defaultstate="collapsed" desc="Write info file">
                            tmpInfoFileWritter.write(String.format(
                                    GeneralUtilities.getUIText("SavingJob.InfoFile.text"),
                                    tmpFirstMoleculeSetFile.getName(), tmpSecondMoleculeSetFile.getName(), tmpOutputFile.getName()
                            ));
                            //</editor-fold>
                            //<editor-fold defaultstate="collapsed" desc="Write molecule set files">
                            Iterator<ComparisonResult> tmpIterator = tmpHistogramDataManager.getNativeDataList().iterator();
                            ComparisonResult tmpComparisonResult;
                            int tmpCounter = 0;
                            while (tmpIterator.hasNext() && tmpCounter < UserPreferences.getInstance().getMaximalNumberOfMoleculePairsToSave()) {
                                tmpComparisonResult = tmpIterator.next();
                                tmpFirstMoleculeSetFileWritter.write(tmpComparisonResult.getMolecule1().trim());
                                tmpFirstMoleculeSetFileWritter.newLine();
                                tmpSecondMoleculeSetFileWritter.write(tmpComparisonResult.getMolecule2().trim());
                                tmpSecondMoleculeSetFileWritter.newLine();
                                tmpCounter++;
                            }
                            //</editor-fold>
                            tmpJobController.setOutputIsSaved(true);
                        } catch (IOException ex) {
                            Platform.runLater(() -> {
                                ApplicationController.this.view.getScene().setCursor(Cursor.DEFAULT);
                                GeneralUtilities.showErrorDialog(ApplicationController.this.view.getPrimaryStage(), ex.toString(), "ApplicationController.view.getSaveJobMenuItem().setOnAction($lambda)");
                                GeneralUtilities.logException(Level.WARNING, ex);
                            });
                        } finally {
                            Platform.runLater(() -> {
                                ApplicationController.this.view.getScene().setCursor(Cursor.DEFAULT);
                            });
                        }
                        return null;
                    }

                };
                Thread tmpBackgroundThread = new Thread(tmpTask);
                this.view.getScene().setCursor(Cursor.WAIT);
                tmpBackgroundThread.start();
            });
            this.view.getLoadJobOutputMenuItem().setOnAction((ActionEvent e) -> {
                if (!UserPreferences.getInstance().getOutputDir().isEmpty() && new File(UserPreferences.getInstance().getOutputDir()).isDirectory()) {
                    this.outputDirectoryChooser.setInitialDirectory(new File(UserPreferences.getInstance().getOutputDir()));
                } else {
                    this.outputDirectoryChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
                }
                if (this.lastOutputDirectoryParent != null && this.lastOutputDirectoryParent.isDirectory()) {
                    this.outputDirectoryChooser.setInitialDirectory(this.lastOutputDirectoryParent);
                }
                this.outputDirectoryChooser.setTitle(GeneralUtilities.getUIText("FileChooserTitle.LoadOutput.text"));
                File tmpDirectory = this.outputDirectoryChooser.showDialog(this.view.getPrimaryStage());
                if (tmpDirectory == null) {
                    return;
                }
                if (!tmpDirectory.isDirectory()) {
                    GeneralUtilities.showInfoDialog(this.view.getPrimaryStage(), GeneralUtilities.getUIText("InfoDescription.FileNotReadable.text"));
                    return;
                }
                this.lastOutputDirectoryParent = tmpDirectory.getParentFile();
                Task<Void> tmpTask = new Task<Void>() {
                    
                    @Override 
                    protected Void call() throws Exception {
                        File tmpFirstMoleculeSetFile = new File(tmpDirectory.getAbsolutePath() + File.separator + MSCConstants.MOLECULE_SET_1_FILE);
                        File tmpSecondMoleculeSetFile = new File(tmpDirectory.getAbsolutePath() + File.separator + MSCConstants.MOLECULE_SET_2_FILE);
                        File tmpOutputFile = new File(tmpDirectory.getAbsolutePath() + File.separator + MSCConstants.OUTPUT_FILE_NAME);
                        try (
                            ObjectInputStream tmpObjectInputStream = new ObjectInputStream(new GZIPInputStream(new FileInputStream(tmpOutputFile), 64 * 1024));
                            BufferedReader tmpFirstMoleculeSetFileReader = new BufferedReader(new FileReader(tmpFirstMoleculeSetFile));
                            BufferedReader tmpSecondMoleculeSetFileReader = new BufferedReader(new FileReader(tmpSecondMoleculeSetFile));
                        ) {
                            //<editor-fold defaultstate="collapsed" desc="Read output file">
                            HistogramDataManager tmpHistogramDataManager = (HistogramDataManager) tmpObjectInputStream.readObject();
                            //</editor-fold>
                            //<editor-fold defaultstate="collapsed" desc="Read molecule set files">
                            Iterator<ComparisonResult> tmpIterator = tmpHistogramDataManager.getNativeDataList().iterator();
                            ComparisonResult tmpComparisonResult;
                            String tmpLine1 = null;
                            String tmpLine2 = null;
                            while (
                                (tmpLine1 = tmpFirstMoleculeSetFileReader.readLine()) != null && 
                                (tmpLine2 = tmpSecondMoleculeSetFileReader.readLine()) != null && 
                                tmpIterator.hasNext()
                            ) {
                                tmpComparisonResult = tmpIterator.next();
                                tmpComparisonResult.setMolecule1(tmpLine1);
                                tmpComparisonResult.setMolecule2(tmpLine2);
                            }
                            //</editor-fold>
                            //<editor-fold defaultstate="collapsed" desc="Bin data">
                            tmpHistogramDataManager.getComparisonFeatureSet().forEach((ComparisonFeature tmpComparisonFeature) -> {
                                HistogramData tmpHistogramData = tmpHistogramDataManager.getHistogramData(tmpComparisonFeature);
                                double tmpLowerFrequencyBound = tmpHistogramData.getLowerFrequencyBound();
                                double tmpUpperFrequencyBound = tmpHistogramData.getUpperFrequencyBound();
                                // Resets the frequency bounds
                                tmpHistogramData.call();
                                // Set the pre-reset frequency bounds
                                tmpHistogramData.setLowerFrequencyBound(tmpLowerFrequencyBound);
                                tmpHistogramData.setUpperFrequencyBound(tmpUpperFrequencyBound);
                            });
                            //</editor-fold>
                            Platform.runLater(() -> {
                                ApplicationController.this.createNewJobController(true, tmpHistogramDataManager);
                                ApplicationController.this.view.getScene().setCursor(Cursor.DEFAULT);
                            });
                        } catch (IOException | ClassNotFoundException | IllegalArgumentException ex) {
                            Platform.runLater(() -> {
                                ApplicationController.this.view.getScene().setCursor(Cursor.DEFAULT);
                                GeneralUtilities.showErrorDialog(ApplicationController.this.view.getPrimaryStage(), ex.toString(), "ApplicationController.view.getLoadJobOutputMenuItem().setOnAction($lambda)");
                                GeneralUtilities.logException(Level.WARNING, ex);
                            });
                        }
                        return null;
                    }
                    
                };
                Thread tmpBackgroundThread = new Thread(tmpTask);
                this.view.getScene().setCursor(Cursor.WAIT);
                tmpBackgroundThread.start();
            });
            this.view.getNewJobMenuItem().setOnAction((ActionEvent e) -> {
                this.createNewJobController(false, null);
            });
            this.view.getDefaultInputDirectoryMenuItem().setOnAction((ActionEvent e) -> {
                try {
                    this.defaultDirectoryChooser.setTitle(GeneralUtilities.getUIText("FileChooserTitle.DefaultInputDirectory.text"));
                    if (!UserPreferences.getInstance().getInputDir().isEmpty() && new File(UserPreferences.getInstance().getInputDir()).isDirectory()) {
                        this.defaultDirectoryChooser.setInitialDirectory(new File(UserPreferences.getInstance().getInputDir()));
                    } else {
                        this.defaultDirectoryChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
                    }
                    File tmpFile = this.defaultDirectoryChooser.showDialog(this.view.getPrimaryStage());
                    if (tmpFile != null && tmpFile.isDirectory()) {
                        UserPreferences.getInstance().setInputDir(tmpFile.getAbsolutePath());
                        UserPreferences.savePreferences();
                    }
                } catch (Exception ex) {
                    GeneralUtilities.showErrorDialog(this.view.getPrimaryStage(), ex.toString(), "ApplicationController.view.getInputDirectoryMenuItem().setOnAction($lambda)");
                    GeneralUtilities.logException(Level.WARNING, ex);
                }
            });
            this.view.getDefaultOutputDirectoryMenuItem().setOnAction((ActionEvent e) -> {
                try {
                    this.defaultDirectoryChooser.setTitle(GeneralUtilities.getUIText("FileChooserTitle.DefaultOutputDirectory.text"));
                    if (!UserPreferences.getInstance().getOutputDir().isEmpty() && new File(UserPreferences.getInstance().getOutputDir()).isDirectory()) {
                        this.defaultDirectoryChooser.setInitialDirectory(new File(UserPreferences.getInstance().getOutputDir()));
                    } else {
                        this.defaultDirectoryChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
                    }
                    File tmpFile = this.defaultDirectoryChooser.showDialog(this.view.getPrimaryStage());
                    if (tmpFile != null && tmpFile.isDirectory()) {
                        UserPreferences.getInstance().setOutputDir(tmpFile.getAbsolutePath());
                        UserPreferences.savePreferences();
                    }
                } catch (Exception ex) {
                    GeneralUtilities.showErrorDialog(this.view.getPrimaryStage(), ex.toString(), "ApplicationController.view.getOutputDirectoryMenuItem().setOnAction($lambda)");
                    GeneralUtilities.logException(Level.WARNING, ex);
                }
            });
            this.view.getDefaultImageDirectoryMenutItem().setOnAction((ActionEvent e) -> {
                try {
                    this.defaultDirectoryChooser.setTitle(GeneralUtilities.getUIText("FileChooserTitle.DefaultImageDirectory.text"));
                    if (!UserPreferences.getInstance().getImageDir().isEmpty() && new File(UserPreferences.getInstance().getImageDir()).isDirectory()) {
                        this.defaultDirectoryChooser.setInitialDirectory(new File(UserPreferences.getInstance().getImageDir()));
                    } else {
                        this.defaultDirectoryChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
                    }
                    File tmpFile = this.defaultDirectoryChooser.showDialog(this.view.getPrimaryStage());
                    if (tmpFile != null && tmpFile.isDirectory()) {
                        UserPreferences.getInstance().setImageDir(tmpFile.getAbsolutePath());
                        UserPreferences.savePreferences();
                    }
                } catch (Exception ex) {
                    GeneralUtilities.showErrorDialog(this.view.getPrimaryStage(), ex.toString(), "ApplicationController.view.getImageDirectoryMenuItem().setOnAction($lambda)");
                    GeneralUtilities.logException(Level.WARNING, ex);
                }
            });
            this.view.getDefaultMoleculeListDirectoryMenuItem().setOnAction((ActionEvent e) -> {
                try {
                    this.defaultDirectoryChooser.setTitle(GeneralUtilities.getUIText("FileChooserTitle.DefaultMoleculeListDirectory.text"));
                    if (!UserPreferences.getInstance().getMoleculeListDir().isEmpty() && new File(UserPreferences.getInstance().getMoleculeListDir()).isDirectory()) {
                        this.defaultDirectoryChooser.setInitialDirectory(new File(UserPreferences.getInstance().getMoleculeListDir()));
                    } else {
                        this.defaultDirectoryChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
                    }
                    File tmpFile = this.defaultDirectoryChooser.showDialog(this.view.getPrimaryStage());
                    if (tmpFile != null && tmpFile.isDirectory()) {
                        UserPreferences.getInstance().setMoleculeListDir(tmpFile.getAbsolutePath());
                        UserPreferences.savePreferences();
                    }
                } catch (Exception ex) {
                    GeneralUtilities.showErrorDialog(this.view.getPrimaryStage(), ex.toString(), "ApplicationController.view.getMoleculeListDirectoryMenuItem().setOnAction($lambda)");
                    GeneralUtilities.logException(Level.WARNING, ex);
                }
            });
            this.view.getDefaultSummaryReportDirectoryMenuItem().setOnAction((ActionEvent e) -> {
                try {
                    this.defaultDirectoryChooser.setTitle(GeneralUtilities.getUIText("FileChooserTitle.DefaultSummaryReportDirectory.text"));
                    if (!UserPreferences.getInstance().getSummaryReportDir().isEmpty() && new File(UserPreferences.getInstance().getSummaryReportDir()).isDirectory()) {
                        this.defaultDirectoryChooser.setInitialDirectory(new File(UserPreferences.getInstance().getSummaryReportDir()));
                    } else {
                        this.defaultDirectoryChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
                    }
                    File tmpFile = this.defaultDirectoryChooser.showDialog(this.view.getPrimaryStage());
                    if (tmpFile != null && tmpFile.isDirectory()) {
                        UserPreferences.getInstance().setSummaryReportDir(tmpFile.getAbsolutePath());
                        UserPreferences.savePreferences();
                    }
                } catch (Exception ex) {
                    GeneralUtilities.showErrorDialog(this.view.getPrimaryStage(), ex.toString(), "ApplicationController.view.getDefaultSummaryReportDirectoryMenuItem().setOnAction($lambda)");
                    GeneralUtilities.logException(Level.WARNING, ex);
                }
            });
            this.view.getOtherPreferencesMenuItem().setOnAction((ActionEvent e) -> {
                this.preferencesConfigurationDialog.getNumberOfParallelThreadsField().setText(Integer.toString(UserPreferences.getInstance().getNumberOfParallelThreads()));
                this.preferencesConfigurationDialog.getDefaultNumberOfBinsField().setText(Integer.toString(UserPreferences.getInstance().getDefaultNumberOfBins()));
                this.preferencesConfigurationDialog.getMaximalNumberOfMoleculePairsToSaveField().setText(Integer.toString(UserPreferences.getInstance().getMaximalNumberOfMoleculePairsToSave()));
                this.preferencesConfigurationDialog.getImageQualitySlider().setValue(UserPreferences.getInstance().getImageQuality());
                this.preferencesConfigurationDialog.showAndWait();
            });
            this.view.getBrowseLogMenuItem().setOnAction((ActionEvent e) -> {
                File tmpLogFile = GeneralUtilities.getLogFile();
                if (!Desktop.isDesktopSupported()) {
                    GeneralUtilities.showInfoDialog(this.view.getPrimaryStage(), GeneralUtilities.getUIText("InfoDescription.DesktopNotSupported.text"));
                    return;
                }
                if (!tmpLogFile.canRead()) {
                    GeneralUtilities.showInfoDialog(this.view.getPrimaryStage(), GeneralUtilities.getUIText("InfoDescription.LogFileNotReadable.text"));
                    return;
                }
                try {
                    Desktop.getDesktop().open(tmpLogFile);
                } catch (IOException ex) {
                    GeneralUtilities.showErrorDialog(this.view.getPrimaryStage(), ex.toString(), "ApplicationController.view.getBrowseLogMenuItem().setOnAction($lambda)");
                    GeneralUtilities.logException(Level.WARNING, ex);
                }
            });
            this.view.getResetLogMenuItem().setOnAction((ActionEvent e) -> {
                // Also deletes the log file and then resets the handlers
                GeneralUtilities.closeAndRemoveHandlersFromLogger(true);
                GeneralUtilities.logMessage(Level.INFO, "", "", GeneralUtilities.getUIText("Logging.NewLoggingSession.text"));
            });
            this.view.getAboutMenuItem().setOnAction((ActionEvent e) -> {
                Alert tmpAboutDialog = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
                tmpAboutDialog.initOwner(this.view.getPrimaryStage());
                tmpAboutDialog.setTitle(GeneralUtilities.getUIText("InfoDialog.AboutTitle.text"));
                tmpAboutDialog.setHeaderText(null);
                tmpAboutDialog.getDialogPane().setContentText(String.format(GeneralUtilities.getUIText("InfoDescription.About.text"), MSCConstants.MSC_VERSION));
                Button tmpButton = (Button) tmpAboutDialog.getDialogPane().lookupButton(ButtonType.OK);
                GuiUtilities.setMinMaxPrefSize(tmpButton, GuiUtilities.SMALL_BUTTON_WIDTH, GuiUtilities.BUTTON_HEIGHT);
                tmpAboutDialog.showAndWait();
            });
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="- Preferences dialog related">
            this.preferencesConfigurationDialog.getCancelButton().setOnAction((ActionEvent e) -> {
                this.preferencesConfigurationDialog.hide();
            });
            this.preferencesConfigurationDialog.getApplyButton().setOnAction((ActionEvent e) -> {
                if (this.isValidNumberOfParallelThreads) {
                    UserPreferences.getInstance().setNumberOfParallelThreads(Integer.parseInt(this.preferencesConfigurationDialog.getNumberOfParallelThreadsField().getText()));
                    UserPreferences.savePreferences();
                    this.jobControllerList.stream().filter((JobController tmpJobController) -> tmpJobController.getJobTaskManager() != null).forEach((JobController tmpJobController) -> {
                        tmpJobController.getJobTaskManager().setNumberOfParallelThreads(UserPreferences.getInstance().getNumberOfParallelThreads());
                    });
                }
                if (this.isValidDefaultNumberOfBins) {
                    UserPreferences.getInstance().setDefaultNumberOfBins(Integer.parseInt(this.preferencesConfigurationDialog.getDefaultNumberOfBinsField().getText()));
                    UserPreferences.savePreferences();
                    this.jobControllerList.stream().filter((JobController tmpJobController) -> tmpJobController.getJobTaskManager() != null).forEach((JobController tmpJobController) -> {
                        tmpJobController.getJobTaskManager().setDefaultNumberOfBins(UserPreferences.getInstance().getDefaultNumberOfBins());
                    });
                }
                if (this.isValidMaximalNumberOfMoleculePairsToSave) {
                    UserPreferences.getInstance().setMaximalNumberOfMoleculePairsToSave(Integer.parseInt(this.preferencesConfigurationDialog.getMaximalNumberOfMoleculePairsToSaveField().getText()));
                    UserPreferences.savePreferences();
                }
                UserPreferences.getInstance().setImageQuality(this.preferencesConfigurationDialog.getImageQualitySlider().getValue());
                UserPreferences.savePreferences();
                this.preferencesConfigurationDialog.hide();
            });
            this.preferencesConfigurationDialog.getDialogPane().addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent tmpEvent) -> {
                KeyCombination tmpKeyCombination = new KeyCodeCombination(KeyCode.ENTER);
                if (tmpKeyCombination.match(tmpEvent)) {
                    this.preferencesConfigurationDialog.getApplyButton().fire();
                }
            });
            this.preferencesConfigurationDialog.getResetButton().setOnAction((ActionEvent e) -> {
                this.preferencesConfigurationDialog.getNumberOfParallelThreadsField().setText(Integer.toString(UserPreferences.getInstance().getNumberOfParallelThreads()));
                this.preferencesConfigurationDialog.getDefaultNumberOfBinsField().setText(Integer.toString(UserPreferences.getInstance().getDefaultNumberOfBins()));
                this.preferencesConfigurationDialog.getMaximalNumberOfMoleculePairsToSaveField().setText(Integer.toString(UserPreferences.getInstance().getMaximalNumberOfMoleculePairsToSave()));
                this.preferencesConfigurationDialog.getImageQualitySlider().setValue(UserPreferences.getInstance().getImageQuality());
            });
            //</editor-fold>
            //</editor-fold>
        } catch (Exception ex) {
            GeneralUtilities.showErrorDialog(null, ex.toString(), "ApplicationController.start()");
            GeneralUtilities.logException(Level.WARNING, ex);
        }
    }

    /**
     * Handle PropertyChangeEvents that are coming from the JobControllers and 
     * update the GUI accordingly
     * 
     * @param evt PropertyChangeEvent that was fired by one of the 
     * JobControllers
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        try {
            if (evt.getSource() instanceof JobController) {
                JobController tmpJobController = (JobController) evt.getSource();
                switch (evt.getPropertyName()) {
                    case "Created charts successfully":
                        this.view.getTabPane().getTabs().add(tmpJobController.getOutputTab());
                        this.view.getCenterPane().setStyle("-fx-background-color: rgb(200, 200, 200);");
                        this.view.getTabPane().getSelectionModel().select(tmpJobController.getOutputTab());
                        this.view.getTabPane().getTabs().remove(tmpJobController.getInputTab());
                        break;
                    case "Want to close InputTab":
                        if (tmpJobController.getJobTaskManager().isWorking()) {
                            Optional<ButtonType> tmpResult = GeneralUtilities.showConfirmationDialog(
                                this.view.getPrimaryStage(), GeneralUtilities.getUIText("ConfirmationDialog.ClosingInputTabContent.text"),
                                GeneralUtilities.getUIText("ConfirmationDialog.ClosingTitle.text"), 
                                GeneralUtilities.getUIText("ConfirmationDialog.ClosingInputTabHeader.text")
                            );
                            if (tmpResult.isPresent() && tmpResult.get().getButtonData() == ButtonBar.ButtonData.APPLY) {
                                this.closeJobController(tmpJobController);
                                // The Tab hasn't been closed yet
                                if (this.view.getTabPane().getTabs().size() <= 1) {
                                    this.view.getCenterPane().setStyle("-fx-background-color: rgb(220, 220, 220);");
                                }
                                this.activeJobController = null;
                            } else {
                                // Destroy the closing event
                                if (evt.getNewValue() instanceof Event) {
                                    Event tmpEvent = (Event) evt.getNewValue();
                                    tmpEvent.consume();
                                }
                            }
                        } else {
                            this.closeJobController(tmpJobController);
                            // The Tab hasn't been closed yet
                            if (this.view.getTabPane().getTabs().size() <= 1) {
                                this.view.getCenterPane().setStyle("-fx-background-color: rgb(220, 220, 220);");
                            }
                            this.activeJobController = null;
                        }
                        break;
                    case "Want to close OutputTab":
                        if (!tmpJobController.outputIsSaved()) {
                            Optional<ButtonType> tmpResult = GeneralUtilities.showConfirmationDialog(
                                this.view.getPrimaryStage(), GeneralUtilities.getUIText("ConfirmationDialog.ClosingOutputTabContent.text"),
                                GeneralUtilities.getUIText("ConfirmationDialog.ClosingTitle.text"), 
                                GeneralUtilities.getUIText("ConfirmationDialog.ClosingOutputTabHeader.text")
                            );
                            if (tmpResult.isPresent() && tmpResult.get().getButtonData() == ButtonBar.ButtonData.APPLY) {
                                this.closeJobController(tmpJobController);
                                // The Tab hasn't been closed yet
                                if (this.view.getTabPane().getTabs().size() <= 1) {
                                    this.view.getCenterPane().setStyle("-fx-background-color: rgb(220, 220, 220);");
                                }
                                this.activeJobController = null;
                            } else {
                                // Destroy the closing event
                                if (evt.getNewValue() instanceof Event) {
                                    Event tmpEvent = (Event) evt.getNewValue();
                                    tmpEvent.consume();
                                }
                            }
                        } else {
                            this.closeJobController(tmpJobController);
                            // The Tab hasn't been closed yet
                            if (this.view.getTabPane().getTabs().size() <= 1) {
                                this.view.getCenterPane().setStyle("-fx-background-color: rgb(220, 220, 220);");
                            }
                            this.activeJobController = null;
                        }
                        break;
                    case "JobController selected":
                        this.activeJobController = tmpJobController;
                        break;
                    default:
                        GeneralUtilities.logMessage(
                            Level.INFO, "ApplicationController", "propertyChange()", 
                            String.format(GeneralUtilities.getUIText("Logging.UnexptectedPropertyChange.text"), evt.getPropertyName())
                        );
                        break;
                }
            }
        } catch (Exception ex) {
            GeneralUtilities.showErrorDialog(this.view.getPrimaryStage(), ex.toString(), "ApplicationController.propertyChange()");
            GeneralUtilities.logException(Level.WARNING, ex);
        }
    }
    
    /**
     * This is called whenever this JavaFX Application is closed and it closes 
     * all open JobControllers
     */
    @Override
    public void stop() {
        this.jobControllerList.forEach((tmpJobController) -> {
            this.closeJobController(tmpJobController);
        });
        this.activeJobController = null;
        GeneralUtilities.logMessage(Level.INFO, "", "", GeneralUtilities.getUIText("Logging.ApplicationClosed.text"));
        // Closes the log file
        GeneralUtilities.closeAndRemoveHandlersFromLogger(false);
    }
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Private methods">
    /**
     * Close the given JobController and its JobTaskManager
     * 
     * @param tmpJobController the JobController that will be closed
     */
    private void closeJobController(JobController tmpJobController) {
        tmpJobController.removePropertyChangeListener(this);
        JobTaskManager tmpJobTaskManager = tmpJobController.getJobTaskManager();
        if (tmpJobTaskManager != null) {
            tmpJobTaskManager.cancelWorkingProcess();
        }
        this.jobControllerList.remove(tmpJobController);
    }
    
    /**
     * Create a new JobController, add it to the JobController list and 
     * handle some related things
     */
    private synchronized void createNewJobController(boolean tmpIsLoaded, HistogramDataManager tmpHistogramDataManager) {
        this.numberOfCreatedJobControllers++;
        try {
            JobController tmpController = new JobController(
                this.view, tmpIsLoaded, Integer.toString(this.numberOfCreatedJobControllers)
            );
            tmpController.addPropertyChangeListener(this);
            this.jobControllerList.add(tmpController);
            if (tmpIsLoaded) {
                    tmpController.setHistogramDataManager(tmpHistogramDataManager);
                    // Set this first to avoid NullPointerExcpetions
                    tmpController.setCurrentlyChosenComparisonFeature(tmpHistogramDataManager.getComparisonFeatureSet().iterator().next());
                    tmpController.getOutputTab().setBinLabelChoiceBoxItems(Set.of(BinLabelType.values()));
                    tmpController.getOutputTab().setChartChoiceBoxItems(tmpController.getHistogramDataManager().getComparisonFeatureSet());
                    tmpController.getOutputTab().getBinLabelChoiceBox().setValue(BinLabelType.MAX);
                    tmpController.getOutputTab().getChartChoiceBox().setValue(tmpController.getHistogramDataManager().getComparisonFeatureSet().iterator().next());
                    tmpController.getOutputTab().getInfoLabel().setText(String.format(
                            GeneralUtilities.getUIText("OutputTab.infoLabel.text"),
                            tmpController.getJobNumber(), 
                            tmpController.getHistogramDataManager().getNumberOfComparedPairs(),
                            tmpHistogramDataManager.getInputFile1(), tmpHistogramDataManager.getInputFile2()
                    ));
                    this.view.getTabPane().getTabs().add(tmpController.getOutputTab());
                    this.view.getCenterPane().setStyle("-fx-background-color: rgb(200, 200, 200);");
                    this.view.getTabPane().getSelectionModel().select(tmpController.getOutputTab());
            } else {
                this.view.getTabPane().getTabs().add(tmpController.getInputTab());
                this.view.getCenterPane().setStyle("-fx-background-color: rgb(200, 200, 200);");
                this.view.getTabPane().getSelectionModel().select(tmpController.getInputTab());
            }
        } catch (IllegalArgumentException ex) {
            this.numberOfCreatedJobControllers--;
            GeneralUtilities.showErrorDialog(this.view.getPrimaryStage(), ex.toString(), "ApplicationController.createNewJobController()");
            GeneralUtilities.logException(Level.WARNING, ex);
        }
    }
    //</editor-fold>
    
}
