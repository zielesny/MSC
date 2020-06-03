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

import de.whs.ibci.msc.utility.GeneralUtilities;
import de.whs.ibci.msc.utility.MSCConstants;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Handles, loads and permanently saves preferences of the user.
 * 
 * @author Jan-Mathis Hein
 */
public class UserPreferences {
    
    //<editor-fold defaultstate="collapsed" desc="Private class variables">
    /**
     * Instance of this class. Other instances should not exist
     */
    private static UserPreferences instance;
    
    /**
     * File where the user preferences will be saved
     */
    private static final File USER_PREFERENCE_FILE = new File(MSCConstants.MSC_FILES_DIRECTORY.getAbsolutePath() + File.separator + "UserPreferences.xml");
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Static initialization block">
    static {
        if (UserPreferences.USER_PREFERENCE_FILE.canRead()) {
            try {
                try (XMLDecoder tmpDecoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(UserPreferences.USER_PREFERENCE_FILE)))) {
                    UserPreferences.instance = (UserPreferences) tmpDecoder.readObject();
                }
            } catch (Exception ex) {
                GeneralUtilities.logMessage(Level.INFO, "", "", GeneralUtilities.getUIText("Logging.PreferencesLoadingNotPossible.text"));
                GeneralUtilities.logException(Level.WARNING, ex);
                // Sets default values
                UserPreferences.instance = new UserPreferences();
            }
        } else {
            // Sets default values
            UserPreferences.instance = new UserPreferences();
        }
    }
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Private instance variables">
    /**
     * The preference for the input directory
     */
    private String inputDir = "";
    
    /**
     * The preference for the output directory
     */
    private String outputDir = "";
    
    /**
     * The preference for the image directory
     */
    private String imageDir = "";
    
    /**
     * The preference for the molecule list directory
     */
    private String moleculeListDir = "";
    
    /**
     * The preference for the summary report directory
     */
    private String summaryReportDir = "";
    
    /**
     * The preference for the number of parallel threads
     */
    private int numberOfParallelThreads = 1;
    
    /**
     * The preference for the default number of bins
     */
    private int defaultNumberOfBins = 10;
    
    /**
     * The preference for the maximal number of molecule pairs to be saved when 
     * saving a job
     */
    private int maximalNumberOfMoleculePairsToSave = 1000;
    
    /**
     * The preference for the quality of images. 0.0 corresponds to a low 
     * quality and 1.0 to a high quality. 
     * NOTE: The value can't be higher than 1 or lower than 0
     */
    private double imageQuality = 0.5;
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Public empty constructor that does nothing. Should only be invoked in 
     * this class and by the XMLDecoder
     */
    public UserPreferences() {
    }
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Public methods">
    //<editor-fold defaultstate="collapsed" desc="Getters">
    /**
     * Get an instance. Other instances should not exist
     *
     * @return an instance
     */
    public static UserPreferences getInstance() {
        return UserPreferences.instance;
    }
    
    /**
     * Get the preference for the input directory
     *
     * @return the preference for the input directory
     */
    public String getInputDir() {
        return this.inputDir;
    }
    
    /**
     * Get the preference for the output directory
     *
     * @return the preference for the output directory
     */
    public String getOutputDir() {
        return this.outputDir;
    }
    
    /**
     * Get the preference for the image directory
     *
     * @return the preference for the image directory
     */
    public String getImageDir() {
        return this.imageDir;
    }
    
    /**
     * Get the preference for the molecule list directory
     *
     * @return the preference for the molecule list directory
     */
    public String getMoleculeListDir() {
        return this.moleculeListDir;
    }
    
    /**
     * Get the preference for the summary report directory
     *
     * @return the preference for the summary report directory
     */
    public String getSummaryReportDir() {
        return this.summaryReportDir;
    }
    
    /**
     * Get the preference for the number of parallel threads
     *
     * @return the preference for the number of parallel threads
     */
    public int getNumberOfParallelThreads() {
        return this.numberOfParallelThreads;
    }
    
    /**
     * Get the preference for the default number of bins
     *
     * @return the preference for the default number of bins
     */
    public int getDefaultNumberOfBins() {
        return this.defaultNumberOfBins;
    }
    
    /**
     * Get the preference for the maximal number of molecule pairs to be saved
     *
     * @return the preference for the maximal number of molecule pairs to be 
     * saved
     */
    public int getMaximalNumberOfMoleculePairsToSave() {
        return this.maximalNumberOfMoleculePairsToSave;
    }
    
    /**
     * Get the preference for the quality of images. 0.0 corresponds to a low 
     * quality and 1.0 to a high quality
     *
     * @return a value between 0.0 and 1.0 that represents the 
     * imageQuality preference
     */
    public double getImageQuality() {
        return this.imageQuality;
    }
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Setters">
    /**
     * Set the preference for the input directory
     *
     * @param tmpInputDir the input directory
     */
    public void setInputDir(String tmpInputDir) {
        this.inputDir = tmpInputDir;
    }
    
    /**
     * Set the preference for the output directory
     *
     * @param tmpOutputDir the output directory
     */
    public void setOutputDir(String tmpOutputDir) {
        this.outputDir = tmpOutputDir;
    }
    
    /**
     * Set the preference for the image directory
     *
     * @param tmpImageDir the image directory
     */
    public void setImageDir(String tmpImageDir) {
        this.imageDir = tmpImageDir;
    }
    
    /**
     * Set the preference for the molecule list directory
     *
     * @param tmpMoleculeListDir the molecule list directory
     */
    public void setMoleculeListDir(String tmpMoleculeListDir) {
        this.moleculeListDir = tmpMoleculeListDir;
    }
    
    /**
     * Set the preference for the summary report directory
     *
     * @param tmpSummaryReportDir the summary report directory
     */
    public void setSummaryReportDir(String tmpSummaryReportDir) {
        this.summaryReportDir = tmpSummaryReportDir;
    }
    
    /**
     * Set the preference for the number of parallel threads
     *
     * @param tmpNumberOfParallelThreads the number of parallel threads
     */
    public void setNumberOfParallelThreads(int tmpNumberOfParallelThreads) {
        this.numberOfParallelThreads = tmpNumberOfParallelThreads;
    }
    
    /**
     * Set the preference for the dafault number of bins
     *
     * @param tmpDefaultNumberOfBins the deafult number of bins
     */
    public void setDefaultNumberOfBins(int tmpDefaultNumberOfBins) {
        this.defaultNumberOfBins = tmpDefaultNumberOfBins;
    }
    
    /**
     * Set the preference for the maximal number of molecule pairs to be saved
     *
     * @param tmpMaximalNumberOfMoleculePairsToSave the maximal number of 
     * molecule pairs to be saved
     */
    public void setMaximalNumberOfMoleculePairsToSave(int tmpMaximalNumberOfMoleculePairsToSave) {
        this.maximalNumberOfMoleculePairsToSave = tmpMaximalNumberOfMoleculePairsToSave;
    }
    
    /**
     * Set the preference for the quality of images. 0.0 corresponds to a low 
     * quality and 1.0 to a high quality. If the value is higher than 1.0 or 
     * lower than 0.0, 1.0 or 0.0 is used instead
     *
     * @param tmpImageQuality the new value for the imageQuality preference
     */
    public void setImageQuality(double tmpImageQuality) {
        if (tmpImageQuality < 0.0) {
            tmpImageQuality = 0.0;
        } else if (tmpImageQuality > 1.0) {
            tmpImageQuality = 1.0;
        }
        this.imageQuality = tmpImageQuality;
    }
    //</editor-fold>
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Private methods">
    /**
     * Permanently save the preferences as an XML file
     */
    public static synchronized void savePreferences() {
        try {
            UserPreferences.USER_PREFERENCE_FILE.createNewFile();
            try (XMLEncoder tmpEncoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(UserPreferences.USER_PREFERENCE_FILE)))) {
                tmpEncoder.writeObject(UserPreferences.getInstance());
            }
        } catch (IOException ex) {
            GeneralUtilities.logMessage(Level.INFO, "", "", GeneralUtilities.getUIText("Logging.PreferencesSavingNotPossible.text"));
            GeneralUtilities.logException(Level.WARNING, ex);
        }
    }
    //</editor-fold>
    
}
