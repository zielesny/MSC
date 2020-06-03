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
package de.whs.ibci.msc.utility;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Date;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextFormatter;
import javafx.stage.Window;

/**
 * This class provides utility methods for the MSC application. The methods in 
 * this class are used for logging, to show various dialogs, to get Strings from 
 * property files and for checking whether only a single instance of the 
 * application is running
 *
 * @author Jan-Mathis Hein
 */
public final class GeneralUtilities {
    
    //<editor-fold defaultstate="collapsed" desc="Private class variables">
    /**
     * Resource bundle for the user interface texts
     */
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("resources/UIText");
    
    /**
     * A logger that documents exceptions and information about the execution of
     * the application
     */
    private static final Logger LOGGER = Logger.getLogger(GeneralUtilities.class.getName());
    
    /**
     * File used to check whether only a single instance of the application is 
     * running
     */
    private static File singleInstanceFile;
    
    /**
     * FileChannel used to attempt to acquire an exclusive lock on the
     * singleInstanceFile
     */
    private static FileChannel fileChannel;
    
    /**
     * A token representing a lock on the singleInstanceFile
     */
    private static FileLock fileLock;
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Public static methods">
    //<editor-fold defaultstate="collapsed" desc="- Single instance related">
    /**
     * Return false if the calling application is not the only currently running
     * instance or if an error occurs, true otherwise
     *
     * @return false if the calling application is not the only currently 
     * running instance or if an error occurs, true otherwise
     */
    public static boolean isSingleInstance() {
        try {
            GeneralUtilities.singleInstanceFile = new File(MSCConstants.MSC_FILES_DIRECTORY.getAbsolutePath() + File.separator + "MSC_SingleInstance.lck");
            if (GeneralUtilities.singleInstanceFile.exists()) {
                GeneralUtilities.singleInstanceFile.delete();
            }
            GeneralUtilities.fileChannel = new RandomAccessFile(GeneralUtilities.singleInstanceFile, "rw").getChannel();
            GeneralUtilities.fileLock = GeneralUtilities.fileChannel.tryLock();
            if (GeneralUtilities.fileLock == null) {
                GeneralUtilities.fileChannel.close();
                return false;
            }
            Thread tmpThread = new Thread(() -> {
                try {
                    if (GeneralUtilities.fileLock != null) {
                        GeneralUtilities.fileLock.release();
                        GeneralUtilities.fileChannel.close();
                        GeneralUtilities.singleInstanceFile.delete();
                    }
                } catch (IOException ex) {
                    // Can't log this because the log-handlers were already removed
                    ex.printStackTrace();
                }
            });
            Runtime.getRuntime().addShutdownHook(tmpThread);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="- Logging related">
    /**
     * Initialize the logger by setting the level, adding the handlers and 
     * creating the log-file.
     */
    public static synchronized void initializeLogger() {
        GeneralUtilities.LOGGER.setLevel(Level.INFO);
        GeneralUtilities.addNewHandlersToLogger();
    }
    
    /**
     * Write information about the excpetion to the log-file
     *
     * @param tmpLevel the severity of the exception
     * @param tmpException the exception that has been thrown and that will be 
     * logged
     * @return false if the logging failed and true otherwise
     */
    public static synchronized boolean logException(Level tmpLevel, Exception tmpException) {
        //<editor-fold defaultstate="collapsed" desc="Checks">
        if (tmpException == null || tmpLevel == null) {
            return false;
        }
        if (GeneralUtilities.LOGGER.getHandlers().length == 0 || GeneralUtilities.LOGGER.getHandlers()[0] == null) {
            if(!GeneralUtilities.addNewHandlersToLogger()) {
                return false;
            }
        }
        //</editor-fold>
        StringWriter tmpStringWriter = new StringWriter();
        tmpException.printStackTrace(new PrintWriter(tmpStringWriter));
        LogRecord tmpLogRecord = new LogRecord(tmpLevel, tmpStringWriter.toString());
        Handler tmpFileHandler = GeneralUtilities.LOGGER.getHandlers()[0];
        tmpFileHandler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord tmpLogRecord) {
                return tmpLogRecord.getThreadID() + ": " + new Date(tmpLogRecord.getMillis()) + ": " + tmpLogRecord.getMessage() + "\n";
            }
        });
        tmpFileHandler.publish(tmpLogRecord);
        return true;
    }
    
    /**
     * Write a given message to the log-file
     *
     * @param tmpLevel the importance of the message
     * @param tmpSourceClass the class from where the message originated
     * @param tmpSourceMethod the method from where the message originated
     * @param tmpMessage the message that will be logged
     * @return false if the logging failed and true otherwise
     */
    public static synchronized boolean logMessage(Level tmpLevel, String tmpSourceClass, String tmpSourceMethod, String tmpMessage) {
        //<editor-fold defaultstate="collapsed" desc="Checks">
        if (tmpLevel == null || tmpSourceClass == null || tmpSourceMethod == null || tmpMessage == null) {
            return false;
        }
        if (GeneralUtilities.LOGGER.getHandlers().length == 0 || GeneralUtilities.LOGGER.getHandlers()[0] == null) {
            if(!GeneralUtilities.addNewHandlersToLogger()) {
                return false;
            }
        }
        //</editor-fold>
        LogRecord tmpLogRecord = new LogRecord(tmpLevel, tmpMessage);
        tmpLogRecord.setSourceClassName(tmpSourceClass);
        tmpLogRecord.setSourceMethodName(tmpSourceMethod);
        Handler tmpFileHandler = GeneralUtilities.LOGGER.getHandlers()[0];
        tmpFileHandler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord tmpLogRecord) {
                return tmpLogRecord.getThreadID() + ": " + tmpLogRecord.getSourceClassName() +
                        ": " + tmpLogRecord.getSourceMethodName() + ": " + new Date(tmpLogRecord.getMillis()) +
                        ": " + tmpLogRecord.getLevel().toString() + ": " + tmpLogRecord.getMessage() + "\n";
            }
        });
        tmpFileHandler.publish(tmpLogRecord);
        return true;
    }
    
    /**
     * Get an abstract representation of the log-file
     *
     * @return an abstract representation of the log-file
     */
    public static synchronized File getLogFile() {
        return new File(MSCConstants.MSC_FILES_DIRECTORY.getAbsolutePath() + File.separator + "MSC_LOG.log");
    }
    
    /**
     * Close and remove all the handlers from the logger. Optionaly you 
     * can reset the log-file and logger by deleting and reconstructing the 
     * log-file and adding new handlers to the logger.
     *
     * @param tmpResetLogFile if true, the log-file and logger will be reset 
     * otherwise not
     * @return true if everything went as intended
     */
    public static synchronized boolean closeAndRemoveHandlersFromLogger(boolean tmpResetLogFile) {
        for (Handler tmpFileHandler : GeneralUtilities.LOGGER.getHandlers()) {
            tmpFileHandler.flush();
            tmpFileHandler.close();
            GeneralUtilities.LOGGER.removeHandler(tmpFileHandler);
        }
        if (tmpResetLogFile) {
            GeneralUtilities.getLogFile().delete();
            // Creates a new file
            return(GeneralUtilities.addNewHandlersToLogger());
        }
        return true;
    }
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="- Dialog related">
    /**
     * Show an error dialog with the specified information. 
     *
     * @param tmpOwner specifies the owner window of this dialog
     * @param tmpContentText a text that describes the error
     * @param tmpOrigin origin of the error
     */
    public static void showErrorDialog(Window tmpOwner, String tmpContentText , String tmpOrigin) {
        Alert tmpErrorDialog = new Alert(Alert.AlertType.ERROR, tmpContentText, ButtonType.OK);
        tmpErrorDialog.initOwner(tmpOwner);
        tmpErrorDialog.setTitle(GeneralUtilities.getUIText("ErrorAlertDialog.Title.text"));
        tmpErrorDialog.getDialogPane().setHeaderText(String.format(GeneralUtilities.getUIText("ErrorAlertDialog.Header.text"), tmpOrigin));
        Button tmpButton = (Button) tmpErrorDialog.getDialogPane().lookupButton(ButtonType.OK);
        GuiUtilities.setMinMaxPrefSize(tmpButton, GuiUtilities.SMALL_BUTTON_WIDTH, GuiUtilities.BUTTON_HEIGHT);
        tmpErrorDialog.showAndWait();
    }
    
    /**
     * Show an info dialog with the specified information
     *
     * @param tmpOwner specifies the owner window of this dialog
     * @param tmpInfoText the information that will be displayes
     */
    public static void showInfoDialog(Window tmpOwner, String tmpInfoText) {
        Alert tmpInfoDialog = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
        tmpInfoDialog.initOwner(tmpOwner);
        tmpInfoDialog.setTitle(GeneralUtilities.getUIText("InfoDialog.Title.text"));
        tmpInfoDialog.getDialogPane().setHeaderText(tmpInfoText);
        Button tmpButton = (Button) tmpInfoDialog.getDialogPane().lookupButton(ButtonType.OK);
        GuiUtilities.setMinMaxPrefSize(tmpButton, GuiUtilities.SMALL_BUTTON_WIDTH, GuiUtilities.BUTTON_HEIGHT);
        tmpInfoDialog.showAndWait();
    }
    
    /**
     * Show a confirmation dialog with the specified information
     *
     * @param tmpOwner specifies the owner window of this dialog
     * @param tmpContentText specifies the content area's text
     * @param tmpTitle the title of the dialog
     * @param tmpHeaderText specifies the header's text
     * @return the ButtonType the user chose (APPLY or CANCEL)
     */
    public static Optional<ButtonType> showConfirmationDialog(Window tmpOwner, String tmpContentText, String tmpTitle, String tmpHeaderText) {
        ButtonType tmpYesButtonType = new ButtonType(GeneralUtilities.getUIText("ConfirmationDialog.Yes.text"), ButtonBar.ButtonData.APPLY);
        ButtonType tmpNoButtonType = new ButtonType(GeneralUtilities.getUIText("ConfirmationDialog.No.text"), ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert tmpConfirmationDialog = new Alert(Alert.AlertType.CONFIRMATION, tmpContentText, tmpYesButtonType, tmpNoButtonType);
        tmpConfirmationDialog.initOwner(tmpOwner);
        tmpConfirmationDialog.setTitle(tmpTitle);
        tmpConfirmationDialog.getDialogPane().setHeaderText(tmpHeaderText);
        Button tmpButton = (Button) tmpConfirmationDialog.getDialogPane().lookupButton(tmpYesButtonType);
        GuiUtilities.setMinMaxPrefSize(tmpButton, GuiUtilities.SMALL_BUTTON_WIDTH, GuiUtilities.BUTTON_HEIGHT);
        tmpButton = (Button) tmpConfirmationDialog.getDialogPane().lookupButton(tmpNoButtonType);
        GuiUtilities.setMinMaxPrefSize(tmpButton, GuiUtilities.SMALL_BUTTON_WIDTH, GuiUtilities.BUTTON_HEIGHT);
        return tmpConfirmationDialog.showAndWait();
    }
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="- UI text related">
    /**
     * Get the text specified by the key from the resource bundle
     *
     * @param tmpKey specifies the text which to get
     * @return the retrieved text or an exception message if the retrieval was 
     * not possible
     */
    public static String getUIText(String tmpKey) {
        try {
            return RESOURCE_BUNDLE.getString(tmpKey).trim();
        } catch (MissingResourceException ex) {
            return "Key '" + tmpKey + "' not found.";
        }
    }
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="- TextFormatter related">
    /**
     * Get a TextFormatter that formats all input so that the resulting 
     * text will always represent an unsigned whole number
     *
     * @return a TextFormatter that formats all input so that the resulting 
     * text will always represent an unsigned whole number
     */
    public static TextFormatter<String> getUnsignedIntegerTextFormatter() {
        return new TextFormatter<>((TextFormatter.Change tmpChange) -> {
            if (!tmpChange.getControlNewText().matches("\\d*")) {
                tmpChange.setText("");
            }
            try {
                Integer.parseInt(tmpChange.getControlNewText());
                tmpChange.getControl().setStyle("-fx-background-color: white;");
            } catch (NumberFormatException ex) {
                tmpChange.getControl().setStyle("-fx-background-color: red;");
            }
            return tmpChange;
        });
    }
    
    /**
     * Get a TextFormatter that formats all input so that the resulting 
     * text will always represent a signed decimal number.
     *
     * @return a TextFormatter that formats all input so that the resulting 
     * text will always represent a signed decimal number
     */
    public static TextFormatter<String> getSignedDecimalNumberTextFormatter() {
        return new TextFormatter<>((TextFormatter.Change tmpChange) -> {
            if (!tmpChange.getControlNewText().matches("[-\\+]?\\d*\\.?\\d*E?\\d*")) {
                tmpChange.setText("");
            }
            try {
                Double.parseDouble(tmpChange.getControlNewText());
                tmpChange.getControl().setStyle("-fx-background-color: white;");
            } catch (NumberFormatException ex) {
                tmpChange.getControl().setStyle("-fx-background-color: red;");
            }
            return tmpChange;
        });
    }
    //</editor-fold>
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Private static methods">
    /**
     * Add new handlers to the logger and create a new log-file
     *
     * @return true if all new handlers have been added
     */
    private static synchronized boolean addNewHandlersToLogger() {
        try {
            GeneralUtilities.getLogFile().createNewFile();
            // Appends to already exisiting file
            FileHandler tmpFileHandler = new FileHandler(GeneralUtilities.getLogFile().getAbsolutePath(), true);
            GeneralUtilities.LOGGER.addHandler(tmpFileHandler);
            return true;
        } catch (IOException | SecurityException ex) {
            ex.printStackTrace();
            return false;
        }
    }
    //</editor-fold>
    
}
