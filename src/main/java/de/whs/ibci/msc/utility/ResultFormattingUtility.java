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

import de.whs.ibci.msc.model.HistogramData;
import java.text.DecimalFormat;
import java.util.Date;

/**
 * Utility class that provides methods to convert job results into formatted 
 * texts
 *
 * @author Jan-Mathis Hein
 */
public abstract class ResultFormattingUtility {

    //<editor-fold defaultstate="collapsed" desc="Public static methods">
    /**
     * Get the header for a result summary report
     *
     * @return an array that contains the individual lines of text
     */
    public static String[] getResultSummaryReportHeader() {
        String[] tmpLines = new String[6];
        tmpLines[0] = "Molecule Set Comparator";
        tmpLines[1] = "Version: " + MSCConstants.MSC_VERSION;
        tmpLines[2] = "Covered under the GNU GPLv3, copyright Jan-Mahis Hein";
        tmpLines[3] = "";
        tmpLines[4] = "Result summary report";
        tmpLines[5] = "";
        //tmpLines[5] = "Job ID:";
        return tmpLines;
    }
    
    /**
     * Get the general information block for a job
     * 
     * @param tmpDateOfCompletion specifies the date of completion of the job
     * @param tmpComputingTime specifies the computing time of the job
     * @param tmpNumberOfPairs specifies the number of pairs of the job
     * @param tmpInputFile1 specifies the job's first input file
     * @param tmpInputFile2 specifies the job's second input file
     * @return an array that contains the individual lines of text
     */
    public static String[] getGeneralInfoBlock(Date tmpDateOfCompletion, double tmpComputingTime, int tmpNumberOfPairs, String tmpInputFile1, String tmpInputFile2) {
        String[] tmpLines = new String[9];
        tmpLines[0] = "Date of completion: " + tmpDateOfCompletion.toString();
        tmpLines[1] = "Computing time: " + Double.toString(tmpComputingTime) + " (Seconds)";
        tmpLines[2] = "";
        tmpLines[3] = "Number of pairs: " + Integer.toString(tmpNumberOfPairs);
        tmpLines[4] = "";
        tmpLines[5] = "Input set files:";
        tmpLines[6] = tmpInputFile1;
        tmpLines[7] = tmpInputFile2;
        tmpLines[8] = "";
        return tmpLines;
    }
    
    /**
     * Get a text representation of an HistogramData object in vertical form.
     * This representation contains a table with the frequencies and the min, 
     * max and average ComparisonFeature values
     * 
     * @param tmpHistogramData contains the histogram data
     * @return an array that contains the individual lines of text
     */
    public static String[] getVerticalHistogramBlock(HistogramData tmpHistogramData) {
        //<editor-fold defaultstate="collapsed" desc="Initialize some variables">
        String[] tmpLines = new String[(tmpHistogramData.getBinBorders().length * 2) + 8];
        String tmpSpace;
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Bin borders preprosessing">
        String[] tmpBinBorders = new String[tmpHistogramData.getBinBorders().length];
        DecimalFormat tmpBinBorderFormatter = new DecimalFormat("##0.00000");
        int tmpMaxLength = 0;
        if (tmpHistogramData.getComparisonFeauture().isContinuous()) {
            for (int i = 0; i < tmpBinBorders.length; i++) {
                tmpBinBorders[i] = tmpBinBorderFormatter.format(tmpHistogramData.getBinBorders()[i]);
                tmpMaxLength = tmpBinBorders[i].length() > tmpMaxLength ? tmpBinBorders[i].length() : tmpMaxLength;
            }
        } else {
            for (int i = 0; i < tmpBinBorders.length; i++) {
                if (i < (tmpBinBorders.length - 1)) {
                    tmpBinBorders[i] = Integer.toString((int) Math.ceil(tmpHistogramData.getBinBorders()[i]));
                } else {
                    tmpBinBorders[i] = Integer.toString((int) Math.ceil(tmpHistogramData.getBinBorders()[i] + 1));
                }
                tmpMaxLength = tmpBinBorders[i].length() > tmpMaxLength ? tmpBinBorders[i].length() : tmpMaxLength;
            }
        }
        //</editor-fold>
        tmpLines[0] = tmpHistogramData.getComparisonFeauture().toString() + ":";
        tmpLines[1] = "";
        //<editor-fold defaultstate="collapsed" desc="Line 3 and line position">
        tmpLines[2] = "bin borders | frequency of pairs";
        int tmpLinePosition = tmpLines[2].indexOf("|");
        if ((tmpLinePosition - 2) < tmpMaxLength) {
            tmpLinePosition = tmpMaxLength + 2;
        }
        tmpSpace = "";
        while (tmpLines[2].indexOf("|") < tmpLinePosition) {
            tmpSpace += " ";
            tmpLines[2] = "bin borders " + tmpSpace + "| frequency of pairs";
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Line 4">
        tmpLines[3] = "";
        while (tmpLines[3].length() < tmpLines[2].length()) {
            tmpLines[3] += "-";
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Bin borders">
        for (int i = 0; i < tmpBinBorders.length; i++) {
            tmpSpace = "";
            while ((tmpSpace.length() + tmpBinBorders[i].length() + 2) < tmpLinePosition) {
                tmpSpace += " ";
            }
            tmpLines[4 + (i * 2)] = tmpSpace + tmpBinBorders[i] + "  |";
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Frequencies preprocessing">
        int tmpInt;
        DecimalFormat tmpFrequencyFormatter = tmpHistogramData.isUseRelativeFrequencies() ? new DecimalFormat("##0.00000") : new DecimalFormat("###");
        int tmpMaxPrePointLength = 0;
        String[] tmpFrequencies = new String[tmpHistogramData.getFrequencies().length];
        for (int i = 0; i < tmpFrequencies.length; i++) {
            tmpFrequencies[i] = tmpFrequencyFormatter.format(tmpHistogramData.getFrequencies()[i]);
            tmpInt = tmpFrequencies[i].indexOf('.') == -1 ? tmpFrequencies[i].length() : tmpFrequencies[i].indexOf('.');
            tmpMaxPrePointLength = tmpInt > tmpMaxPrePointLength ? tmpInt : tmpMaxPrePointLength;
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Frequencies">
        String[] tmpArray;
        for (int i = 0; i < tmpHistogramData.getFrequencies().length; i++) {
            tmpArray = tmpFrequencies[i].split("\\.");
            tmpSpace = "";
            while (tmpSpace.length() < tmpLinePosition) {
                tmpSpace += " ";
            }
            tmpLines[5 + (i * 2)] = tmpSpace + "|  ";
            tmpSpace = "";
            while (tmpSpace.length() < (tmpMaxPrePointLength - tmpArray[0].length())) {
                tmpSpace += " ";
            }
            tmpLines[5 + (i * 2)] += tmpSpace + tmpFrequencies[i] + (tmpHistogramData.isUseRelativeFrequencies() ? "%" : "");
        }
        //</editor-fold>
        tmpLines[3 + (tmpHistogramData.getBinBorders().length * 2)] = "";
        tmpLines[4 + (tmpHistogramData.getBinBorders().length * 2)] = "min: " + Double.toString(tmpHistogramData.getMinComparisonFeatureValue());
        tmpLines[5 + (tmpHistogramData.getBinBorders().length * 2)] = "max: " + Double.toString(tmpHistogramData.getMaxComparisonFeatureValue());
        tmpLines[6 + (tmpHistogramData.getBinBorders().length * 2)] = "average: " + Double.toString(tmpHistogramData.getAverageComparisonFeatureValue());
        tmpLines[7 + (tmpHistogramData.getBinBorders().length * 2)] = "";
        return tmpLines;
    }
    
    /**
     * Get a text representation of an HistogramData object in horizontal form.
     * This representation contains a table with the frequencies and the min, 
     * max and average ComparisonFeature values
     * 
     * @param tmpHistogramData contains the histogram data
     * @return an array that contains the individual lines of text
     */
    public static String[] getHorizontalHistogramBlock(HistogramData tmpHistogramData) {
        //<editor-fold defaultstate="collapsed" desc="Initialize some variables">
        String[] tmpStringArray;
        String[] tmpLines = new String[5];
        String tmpSpaceBetweenNumbers = "  ";
        String tmpString;
        String tmpSpace;
        String tmpPattern = "####0.0####";
        DecimalFormat tmpFormatter = new DecimalFormat(tmpPattern);
        int tmpMaxPrePointLength = 0;
        int tmpMaxPostPointLength = 0;
        int tmpMaxFrequencyLength = 0;
        int tmpNumberOfLeadingSpaces;
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Pre calculations">
        for (double tmpBinBorder : tmpHistogramData.getBinBorders()) {
            tmpStringArray = tmpFormatter.format(tmpBinBorder).split("\\.");
            tmpMaxPrePointLength = tmpStringArray[0].length() > tmpMaxPrePointLength ? tmpStringArray[0].length() : tmpMaxPrePointLength;
            tmpMaxPostPointLength = tmpStringArray[1].length() > tmpMaxPostPointLength ? tmpStringArray[1].length() : tmpMaxPostPointLength;
        }
        for (double tmpFrequency : tmpHistogramData.getFrequencies()) {
            tmpString = Double.toString(tmpFrequency);
            tmpMaxFrequencyLength = tmpString.length() > tmpMaxFrequencyLength ? tmpString.length() : tmpMaxFrequencyLength;
        }
        while ((tmpMaxPostPointLength + tmpSpaceBetweenNumbers.length() + tmpMaxPrePointLength) < tmpMaxFrequencyLength + 2) {
            tmpSpaceBetweenNumbers += " ";
        }
        while ((tmpMaxPostPointLength + tmpSpaceBetweenNumbers.length() + tmpMaxPrePointLength) > tmpMaxFrequencyLength + 2) {
            tmpMaxFrequencyLength++;
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Description line">
        tmpLines[0] = tmpHistogramData.getComparisonFeauture().toString() + ":";
        tmpLines[1] = "";
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Bin border line">
        for (double tmpBorder : tmpHistogramData.getBinBorders()) {
            tmpString = tmpFormatter.format(tmpBorder);
            while (tmpString.split("\\.")[0].length() < tmpMaxPrePointLength) {
                tmpString = " " + tmpString;
            }
            while (tmpString.split("\\.")[1].length() < tmpMaxPostPointLength) {
                tmpString += " ";
            }
            tmpLines[2] += tmpString + tmpSpaceBetweenNumbers;
        }
        tmpLines[2] = tmpLines[2].trim();
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Frequency line">
        char[] tmpArray = new char[tmpLines[2].split("\\.")[0].length()];
        for (int i = 0; i < tmpArray.length; i++) {
            tmpArray[i] = ' ';
        }
        tmpLines[3] += new String(tmpArray);
        for (double tmpFrequencyValue : tmpHistogramData.getFrequencies()) {
            tmpString = Double.toString(tmpFrequencyValue);
            tmpLines[3] += "| ";
            if (tmpString.length() < tmpMaxFrequencyLength) {
                tmpNumberOfLeadingSpaces = (tmpMaxFrequencyLength - tmpString.length()) / 2;
                tmpNumberOfLeadingSpaces += (tmpMaxFrequencyLength - tmpString.length()) % 2;
                tmpSpace = "";
                while (tmpSpace.length() != tmpNumberOfLeadingSpaces) {
                    tmpSpace += " ";
                }
                tmpString = tmpSpace + tmpString;
                tmpSpace = "";
                while (tmpSpace.length() != (tmpMaxFrequencyLength - tmpString.length())) {
                    tmpSpace += " ";
                }
                tmpString += tmpSpace;
            }
            tmpLines[3] += tmpString + " ";
        }
        tmpLines[3] += "|";
        tmpLines[4] = "";
        //</editor-fold>
        return tmpLines;
    }
    //</editor-fold>
    
}
