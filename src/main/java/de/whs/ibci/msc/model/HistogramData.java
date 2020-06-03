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
package de.whs.ibci.msc.model;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Handles the frequency data, the bin borders and the bins list for one 
 * particular ComparisonFeature which is represented by its corresponding number
 * 
 * @author Jan-Mathis Hein
 */
public class HistogramData implements Callable<Void>, Serializable {

    //<editor-fold defaultstate="collapsed" desc="Private class variables">
    /**
     * A serial version UID to check whether loaded data is compatible
     */
    private static final long serialVersionUID = 5L;
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Private instance variables">
    /**
     * If true the specifically set bin borders will be used, if false the bin 
     * borders are calculated automatically
     */
    private boolean useSpecificBinBorders;
    
    /**
     * If true all returning frequencies will be relative ones and all 
     * inputted frequencies will be regarded as relative ones; 
     * if false all returning frequencies will be absolute ones and all 
     * inputted frequencies will be regarded as absolute ones
     */
    private boolean useRelativeFrequencies;
    
    /**
     * Maximal value of all comparison results
     */
    private double maxComparisonFeatureValue;

    /**
     * Minimal value of all comparison results
     */
    private double minComparisonFeatureValue;
    
    /**
     * The average value off all comparison results
     */
    private transient double averageComparisonFeatureValue;

    /**
     * The lower border of the first bin
     */
    private double lowerBinBorder;

    /**
     * The upper border of the last bin
     */
    private double upperBinBorder;

    /**
     * The lower boundary for frequency values. Is set to NaN if the data is 
     * binned
     */
    private double lowerFrequencyBound;

    /**
     * The upper boundary for frequency values. Is set to NaN if the data is 
     * binned
     */
    private double upperFrequencyBound;

    /**
     * The current maximal frequency value of any bin
     */
    private double currentMaxFrequencyValue;

    /**
     * Borders of the bins
     */
    private double[] binBorders;

    /**
     * Number which represents a ComparisonFeature
     */
    private int comparisonFeatureNumber;

    /**
     * Number of bins into which the comparison results will be devided. 
     * This will always be positive
     */
    private int numberOfBins;
    
    /**
     * The number of invalid similarity values
     */
    private int numberOfInvalidComparisonFeatureValues;

    /**
     * This frequency data. The value at each index represents the frequency of 
     * ComparisonResults in the corresponding bin.
     */
    private transient int[] frequencies;
    
    /**
     * List of all the ComparisonResults (the native data) that belong to the 
     * data set
     */
    private transient List<ComparisonResult> nativeDataList;

    /**
     * List of lists where each list corresponds to a bin and contains 
     * a sample of molecule pairs whos comparison result belongs to this bin
     */
    private transient List<List<ComparisonResult>> binsList;
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Initialize some instance variables
     * 
     * @param tmpComparisonFeatureNumber specifies the ComparisonFeature to which
     * this HistogramData belongs
     * @param tmpList reference to the native data
     */
    public HistogramData(int tmpComparisonFeatureNumber, List<ComparisonResult> tmpList) {
        this.comparisonFeatureNumber = tmpComparisonFeatureNumber;
        this.nativeDataList = tmpList;
        this.lowerBinBorder = Double.NaN;
        this.upperBinBorder = Double.NaN;
        this.lowerFrequencyBound = Double.NaN;
        this.upperFrequencyBound = Double.NaN;
        this.currentMaxFrequencyValue = 0.0;
    }
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Public methods">
    //<editor-fold defaultstate="collapsed" desc="Binning">
    /**
     * Calculate the bin borders and the corresponding frequency data. Also
     * reset upperFrequencyBound and lowerFrequencyBound to NaN
     */
    @Override
    public Void call() {
        this.lowerFrequencyBound = Double.NaN;
        this.upperFrequencyBound = Double.NaN;
        int tmpNumberOfBins;
        //<editor-fold defaultstate="collapsed" desc="Determine the bin borders and the number of bins">
        if (!this.useSpecificBinBorders) {
            double tmpLowestBinBorder = Double.isNaN(this.lowerBinBorder) ? this.minComparisonFeatureValue : this.lowerBinBorder;
            double tmpHighestBinBorder = Double.isNaN(this.upperBinBorder) ? this.maxComparisonFeatureValue : this.upperBinBorder;
            double tmpBinSize = (tmpHighestBinBorder - tmpLowestBinBorder) / this.numberOfBins;
            if (tmpBinSize < 0) {
                // If binSize is negative, reasonable values are inferred
                tmpBinSize = Math.abs(tmpBinSize);
                this.lowerBinBorder = tmpHighestBinBorder;
                this.upperBinBorder = tmpLowestBinBorder;
                tmpLowestBinBorder = this.lowerBinBorder;
                tmpHighestBinBorder = this.upperBinBorder;
            }
            if (tmpBinSize == 0) {
                tmpNumberOfBins = 1;
            } else {
                tmpNumberOfBins= this.numberOfBins;
            }
            this.binBorders = new double[tmpNumberOfBins + 1];
            for (int i = 0; i < tmpNumberOfBins; i++) {
                this.binBorders[i] = tmpLowestBinBorder + (tmpBinSize * i);
            }
            this.binBorders[tmpNumberOfBins] = tmpHighestBinBorder;
        } else {
            tmpNumberOfBins = this.numberOfBins;
        }
        //</editor-fold>
        this.binsList = new ArrayList<>(tmpNumberOfBins);
        for (int i = 0; i < tmpNumberOfBins; i++) {
            this.binsList.add(i, new LinkedList<>());
        }
        this.frequencies = new int[tmpNumberOfBins];
        Arrays.fill(this.frequencies, 0);
        Iterator<ComparisonResult> tmpIterator = this.nativeDataList.iterator();
        ComparisonResult tmpResult;
        double tmpValue;
        this.averageComparisonFeatureValue = 0.0;
        this.numberOfInvalidComparisonFeatureValues = 0;
        iteratorLoop:
        while (tmpIterator.hasNext()) {
            tmpResult = tmpIterator.next();
            tmpValue = tmpResult.getSimilarities()[this.comparisonFeatureNumber];
            if (Double.isNaN(tmpValue)) {
                this.numberOfInvalidComparisonFeatureValues++;
                continue;
            }
            this.averageComparisonFeatureValue += tmpValue;
            for (int i = 1; i < this.binBorders.length; i++) {
                // Lower bound included, upper bound excluded
                if (tmpValue < this.binBorders[i] && tmpValue >= this.binBorders[i - 1]) {
                    this.frequencies[i - 1]++;
                    if (tmpResult.hasMoleculePair()) {
                        List<ComparisonResult> tmpBin = this.binsList.get(i - 1);
                        tmpBin.add(tmpResult);
                    }
                    continue iteratorLoop;
                }
            }
            // If tmpValue exactly matches the highest bin border it contributes to the last bin
            if (tmpValue == this.binBorders[tmpNumberOfBins]) {
                this.frequencies[tmpNumberOfBins - 1]++;
                if (tmpResult.hasMoleculePair()) {
                    List<ComparisonResult> tmpBin = this.binsList.get(tmpNumberOfBins - 1);
                    tmpBin.add(tmpResult);
                }
            }
        }
        this.averageComparisonFeatureValue /= (this.nativeDataList.size() - this.numberOfInvalidComparisonFeatureValues);
        this.currentMaxFrequencyValue = this.frequencies[0];
        for (int tmpInt : this.frequencies) {
            this.currentMaxFrequencyValue = this.currentMaxFrequencyValue >= tmpInt ? this.currentMaxFrequencyValue : tmpInt;
        }
        return null;
    }
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Getters">
    /**
     * If true all returning frequencies will be relative ones and all 
     * inputted frequencies will be regarded as relative ones; 
     * if false all returning frequencies will be absolute ones and all 
     * inputted frequencies will be regarded as absolute ones
     * 
     * @return true if all returning frequencies will be relative ones and all 
     * inputted frequencies will be regarded as relative ones; 
     * return false if all returning frequencies will be absolute ones and all 
     * inputted frequencies will be regarded as absolute ones
     */
    public boolean isUseRelativeFrequencies() {
        return this.useRelativeFrequencies;
    }
    
    /**
     * Get a copy of the bin borders.
     *
     * @return a copy of the bin borders
     */
    public double[] getBinBorders() {
        return Arrays.copyOf(this.binBorders, this.binBorders.length);
    }

    /**
     * Get the maximal ComparisonFeature value
     * 
     * @return the maximal ComparisonFeature value
     */
    public double getMaxComparisonFeatureValue() {
        return this.maxComparisonFeatureValue;
    }

    /**
     * Get the minimal ComparisonFeature value
     * 
     * @return the minmal ComparisonFeature value
     */
    public double getMinComparisonFeatureValue() {
        return this.minComparisonFeatureValue;
    }
    
    /**
     * Get the average ComparisonFeature value
     * 
     * @return the average ComparisonFeature value
     */
    public double getAverageComparisonFeatureValue() {
        return this.averageComparisonFeatureValue;
    }

    /**
     * Get the value that will be or is the lowest bin border
     *
     * @return the lowest bin border
     */
    public double getLowerBinBorder() {
        return Double.isNaN(this.lowerBinBorder) ? this.minComparisonFeatureValue : this.lowerBinBorder;
    }

    /**
     * Get the value that will be or is the highest bin border
     *
     * @return the highest bin border
     */
    public double getUpperBinBorder() {
        return Double.isNaN(this.upperBinBorder) ? this.maxComparisonFeatureValue : this.upperBinBorder;
    }

    /**
     * Get the lower bound for the frequency values
     *
     * @return the lower bound for the frequency values
     */
    public double getLowerFrequencyBound() {
        if (this.useRelativeFrequencies) {
            return Double.isNaN(this.lowerFrequencyBound) ? 
                    (0.0 / (this.nativeDataList.size() - this.numberOfInvalidComparisonFeatureValues)) : 
                    (this.lowerFrequencyBound / (this.nativeDataList.size() - this.numberOfInvalidComparisonFeatureValues));
        } else {
            return Double.isNaN(this.lowerFrequencyBound) ? 0.0 : this.lowerFrequencyBound;
        }
    }

    /**
     * Get the upper bound for the frequency values
     *
     * @return the upper bound for the frequency values
     */
    public double getUpperFrequencyBound() {
        if (this.useRelativeFrequencies) {
            return Double.isNaN(this.upperFrequencyBound) ? 
                (this.currentMaxFrequencyValue  / (this.nativeDataList.size() - this.numberOfInvalidComparisonFeatureValues)) : 
                (this.upperFrequencyBound / (this.nativeDataList.size() - this.numberOfInvalidComparisonFeatureValues));
        } else {
            return Double.isNaN(this.upperFrequencyBound) ? this.currentMaxFrequencyValue : this.upperFrequencyBound;
        }
    }
    
    /**
     * Get the maximal possible frequency value
     * 
     * @return the maximal possible frequency value
     */
    public double getMaxFrequency() {
        if (this.useRelativeFrequencies) {
            return 1;
        } else {
            return (this.nativeDataList.size() - this.numberOfInvalidComparisonFeatureValues);
        }
    }

    /**
     * Get the number of bins
     *
     * @return the number of bins
     */
    public int getNumberOfBins() {
        return this.numberOfBins;
    }

    /**
     * Get the frequencies as an array where each index corresponds to a bin
     *
     * @return the frequencies
     */
    public double[] getFrequencies() {
        double tmpScaling = 1.0;
        if (this.useRelativeFrequencies) {
            tmpScaling = (this.nativeDataList.size() - this.numberOfInvalidComparisonFeatureValues);
        }
        double[] tmpResult = new double[this.frequencies.length];
        for (int i = 0; i < tmpResult.length; i++) {
            tmpResult[i] = (this.frequencies[i] / tmpScaling);
        }
        return tmpResult;
    }

    /**
     * Get the bin specified by the index as a list that contains 
     * associated ComparisonResults.
     * NOTE: If this HistogramData is loaded, this list can be empty even
     * though the frequency of the bin is not zero
     *
     * @param tmpBinIndex specifies the bin
     * @return the specified bin as a list of IAtomContainer arrays
     */
    public List<ComparisonResult> getBin(int tmpBinIndex) {
        return this.binsList.get(tmpBinIndex);
    }
    
    /**
     * Get the number corresponding to the ComparisonFeature of this 
     * HistogramData
     * 
     * @return the number corresponding to the ComparisonFeature
     */
    public int getComparisonFeautureNumber() {
        return this.comparisonFeatureNumber;
    }
    
    /**
     * Get the ComparisonFeature of this HistogramData
     * 
     * @return the ComparisonFeature of this HistogramData
     */
    public ComparisonFeature getComparisonFeauture() {
        return ComparisonFeature.values()[this.comparisonFeatureNumber];
    }
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Setters">
    /**
     * Set the useRelativeFrequencies flag. If this flag is true all 
     * returning and inputted frequencies will be realtive ones, if false all 
     * returning and inputted frequencies will be absolute ones
     * 
     * @param tmpUseRelativeFrequencies the value to which the flag will 
     * be set
     */
    public synchronized void setUseRelativeFrequencies(boolean tmpUseRelativeFrequencies) {
        this.useRelativeFrequencies = tmpUseRelativeFrequencies;
    }
    
    /**
     * Set the lower bin border
     *
     * @param tmpBorder specifies the new value of the lower bin border
     */
    public synchronized void setLowerBinBorder(double tmpBorder) {
        this.lowerBinBorder = tmpBorder;
        this.useSpecificBinBorders = false;
    }

    /**
     * Set the upper bin border
     *
     * @param tmpBorder specifies the new value of the upper bin border
     */
    public synchronized void setUpperBinBorder(double tmpBorder) {
        this.upperBinBorder = tmpBorder;
        this.useSpecificBinBorders = false;
    }

    /**
     * Set the number of bins. If the given value is smaller than one, 
     * one is used instead.
     *
     * @param tmpNumberOfBins specifies the new number of bins
     */
    public synchronized void setNumberOfBins(int tmpNumberOfBins) {
        this.numberOfBins = tmpNumberOfBins > 1 ? tmpNumberOfBins : 1;
        this.useSpecificBinBorders = false;
    }

    /**
     * Set the lower frequency bound. If the resulting frequency bounds 
     * would be irrational, reasonable values are inferred. If the given 
     * bound is not a number the method returns without doing anything
     *
     * @param tmpLowerBound specifies the new value of the lower frequency bound
     */
    public synchronized void setLowerFrequencyBound(double tmpLowerBound) {
        if (Double.isNaN(tmpLowerBound)) {
            return;
        }
        if (this.useRelativeFrequencies) {
            tmpLowerBound *= (this.nativeDataList.size() - this.numberOfInvalidComparisonFeatureValues);
        }
        double tmpUpperBound = this.upperFrequencyBound;
        if (tmpLowerBound > tmpUpperBound) {
            this.lowerFrequencyBound = tmpUpperBound;
            this.upperFrequencyBound = tmpLowerBound;
        } else {
            this.lowerFrequencyBound = tmpLowerBound;
            this.upperFrequencyBound = tmpUpperBound;
        }
    }

    /**
     * Set the upper frequency bound. If the resulting frequency bounds 
     * would be irrational, reasonable values are inferred. If the given 
     * bound is not a number the method returns without doing anything
     *
     * @param tmpUpperBound specifies the new value of the upper frequency bound
     */
    public synchronized void setUpperFrequencyBound(double tmpUpperBound) {
        if (Double.isNaN(tmpUpperBound)) {
            return;
        }
        if (this.useRelativeFrequencies) {
            tmpUpperBound *= (this.nativeDataList.size() - this.numberOfInvalidComparisonFeatureValues);
        }
        double tmpLowerBound = this.lowerFrequencyBound;
        if (tmpLowerBound > tmpUpperBound) {
            this.lowerFrequencyBound = tmpUpperBound;
            this.upperFrequencyBound = tmpLowerBound;
        } else {
            this.lowerFrequencyBound = tmpLowerBound;
            this.upperFrequencyBound = tmpUpperBound;
        }
    }
    
    /**
     * Set the maximal ComparisonFeature value
     * 
     * @param tmpValue specifies the value to set
     */
    public void setMaxComparisonFeatureValue(double tmpValue) {
        this.maxComparisonFeatureValue = tmpValue;
    }

    /**
     * Set the minimal ComparisonFeature value
     * 
     * @param tmpValue specifies the value to set
     */
    public void setMinComparisonFeatureValue(double tmpValue) {
        this.minComparisonFeatureValue = tmpValue;
    }
    
    /**
     * Set the native data list
     * 
     * @param tmpList the native data list to be set
     */
    public void setNativeDataList(List<ComparisonResult> tmpList) {
        this.nativeDataList = tmpList;
    }
    
    /**
     * Set the bin borders
     * 
     * @param tmpBinBorders the bin borders to be set
     */
    public void setSpecificBinBorders(double[] tmpBinBorders) {
        this.binBorders = Arrays.copyOf(tmpBinBorders, tmpBinBorders.length);
        this.useSpecificBinBorders = true;
    }
    //</editor-fold>
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Private methods">
    /**
     * Write the state of this object to the given ObjectOutputStream
     *
     * @param tmpObjectOutputStream the ObjectOutputStream to which information
     * will be written
     * @throws IOException if an I/O error occurs
     */
    private synchronized void writeObject(java.io.ObjectOutputStream tmpObjectOutputStream) throws IOException {
        tmpObjectOutputStream.writeBoolean(this.useRelativeFrequencies);
        tmpObjectOutputStream.writeBoolean(this.useSpecificBinBorders);
        tmpObjectOutputStream.writeDouble(this.lowerBinBorder);
        tmpObjectOutputStream.writeDouble(this.lowerFrequencyBound);
        tmpObjectOutputStream.writeDouble(this.maxComparisonFeatureValue);
        tmpObjectOutputStream.writeDouble(this.minComparisonFeatureValue);
        tmpObjectOutputStream.writeDouble(this.upperBinBorder);
        tmpObjectOutputStream.writeDouble(this.upperFrequencyBound);
        tmpObjectOutputStream.writeDouble(this.currentMaxFrequencyValue);
        tmpObjectOutputStream.writeInt(this.comparisonFeatureNumber);
        tmpObjectOutputStream.writeInt(this.numberOfBins);
        tmpObjectOutputStream.writeObject(this.binBorders);
    }
    
    /**
     * Read the state of an object from the given ObjectInputStream
     *
     * @param tmpObjectInputStream ObjectInputStream from which to read
     * @throws IOException if an I/O error occurs
     * @throws ClassNotFoundException Class of a serialized object cannot be 
     * found
     */
    private void readObject(java.io.ObjectInputStream tmpObjectInputStream) throws IOException, ClassNotFoundException {
        this.useRelativeFrequencies = tmpObjectInputStream.readBoolean();
        this.useSpecificBinBorders = tmpObjectInputStream.readBoolean();
        this.lowerBinBorder = tmpObjectInputStream.readDouble();
        this.lowerFrequencyBound = tmpObjectInputStream.readDouble();
        this.maxComparisonFeatureValue = tmpObjectInputStream.readDouble();
        this.minComparisonFeatureValue = tmpObjectInputStream.readDouble();
        this.upperBinBorder = tmpObjectInputStream.readDouble();
        this.upperFrequencyBound = tmpObjectInputStream.readDouble();
        this.currentMaxFrequencyValue = tmpObjectInputStream.readDouble();
        this.comparisonFeatureNumber = tmpObjectInputStream.readInt();
        this.numberOfBins = tmpObjectInputStream.readInt();
        this.binBorders = (double[]) tmpObjectInputStream.readObject();
    }
    
    /**
     * This method is not implemented
     */
    private void readObjectNoData() {
        throw new RuntimeException("Method \"readObjectNoData\" is not implemented");
    }
    //</editor-fold>

}
