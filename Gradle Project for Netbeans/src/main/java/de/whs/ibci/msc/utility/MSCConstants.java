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

/**
 * Defines some constants for the MSC application. 
 * This class can't be instantiated
 *
 * @author Jan-Mathis Hein
 */
public final class MSCConstants {
    
    //<editor-fold defaultstate="collapsed" desc="Public instance variables">
    /**
     * The main window's minimal width
     */
    public final static double MINIMAL_MAIN_WINDOW_WIDTH = 514.0;
    
    /**
     * The main window's minimal height
     */
    public final static double MINIMAL_MAIN_WINDOW_HEIGHT = 686.0;
    
    /**
     * The MoleculePairDisplayerStage's minimal width
     */
    public final static double MINIMAL_MOLECULE_DISPLAYER_WIDTH = 859.0;
    
    /**
     * The MoleculePairDisplayerStage's minimal height
     */
    public final static double MINIMAL_MOLECULE_DISPLAYER_HEIGHT = 444.0;
    
    /**
     * Directory where important MSC files will be saved
     */
    public static final File MSC_FILES_DIRECTORY = new File(System.getProperty("user.dir") + File.separator + "MSC_Files");
    
    /**
     * A String representing the current version of the MSC
     */
    public final static String MSC_VERSION = "1.0";
    
    /**
     * Minimal java version needed to run the application.
     */
    public final static String JAVA_VERSION = "11";
    
    /**
     * String representing the name of an info file
     */
    public final static String INFO_FILE_NAME = "README.txt";
    
    /**
     * String representing the name of an output file
     */
    public final static String OUTPUT_FILE_NAME = "jobOutput.mosc";
    
    /**
     * String representing the name of an molecule set 1 file
     */
    public final static String MOLECULE_SET_1_FILE = "moleculeSet1.txt";
    
    /**
     * String representing the name of an molecule set 2 file
     */
    public final static String MOLECULE_SET_2_FILE = "moleculeSet2.txt";
    //</editor-fold>
    
}
