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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Stores and manages the histogram data for multiple ComparisonFeatures. 
 * Also handles the saving and loading of the histogram data
 * 
 * @author Jan-Mathis Hein
 */
public class HistogramDataManager implements Serializable {

    //<editor-fold defaultstate="collapsed" desc="Private class variables">
    /**
     * A serial version UID to check whether loaded data is compatible
     */
    private static final long serialVersionUID = 5L;
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Private instance variables">
    /**
     * List of all the ComparisonResults (the native data) that belong to the 
     * data set
     */
    private transient List<ComparisonResult> nativeDataList;
    
    /**
     * Maps all the HistogramData objects to their corresponding 
     * ComparisonFeature
     */
    private transient Map<ComparisonFeature, HistogramData> comparisonFeatureToDataMap;
    
    /**
     * The time difference, in milliseconds, between the data's calculation 
     * start time and midnight, January 1, 1970 UTC
     */
    private long calculationStartTime;
    
    /**
     * The time difference, in milliseconds, between the data's calculation 
     * finish time and midnight, January 1, 1970 UTC
     */
    private long calculationFinishTime;
    
    /**
     * Name of the first input file which was used for the comparison
     */
    private String inputFile1;
    
    /**
     * Name of the second input file which was used for the comparison
     */
    private String inputFile2;
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Initialize the instance variables and add the first ComparisonResult to 
     * the data set
     * 
     * @param tmpConsideredComparisonFeaturesList list of ComparisonFeatures for 
     * which histogram data will be stored
     * @param tmpNumberOfInitialBins initial number of bins
     * @param tmpFirstResult first ComparisonResult for the data set
     * @param tmpInputFile1 the name of the first input file
     * @param tmpInputFile2 the name of the second input file
     */
    public HistogramDataManager(
        List<ComparisonFeature> tmpConsideredComparisonFeaturesList, int tmpNumberOfInitialBins, 
        ComparisonResult tmpFirstResult, String tmpInputFile1, String tmpInputFile2
    ) throws IllegalArgumentException {
        //<editor-fold defaultstate="collapsed" desc="Checks">
        if (tmpConsideredComparisonFeaturesList == null || tmpFirstResult == null) {
            throw new IllegalArgumentException("An argument is null");
        }
        //</editor-fold>
        this.comparisonFeatureToDataMap = new EnumMap<>(ComparisonFeature.class);
        this.nativeDataList = new LinkedList<>();
        this.nativeDataList.add(tmpFirstResult);
        tmpConsideredComparisonFeaturesList.forEach((ComparisonFeature tmpComparisonFeature) -> {
            this.comparisonFeatureToDataMap.putIfAbsent(tmpComparisonFeature, new HistogramData(tmpComparisonFeature.getFeatureNumber(), this.nativeDataList));
        });
        this.comparisonFeatureToDataMap.values().forEach((HistogramData tmpHistogramData) -> {
            tmpHistogramData.setMinComparisonFeatureValue(tmpFirstResult.getSimilarities()[tmpHistogramData.getComparisonFeautureNumber()]);
            tmpHistogramData.setMaxComparisonFeatureValue(tmpFirstResult.getSimilarities()[tmpHistogramData.getComparisonFeautureNumber()]);
            tmpHistogramData.setNumberOfBins(tmpNumberOfInitialBins < 1 ? 1 : tmpNumberOfInitialBins);
        });
        this.inputFile1 = tmpInputFile1;
        this.inputFile2 = tmpInputFile2;
    }
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Public Methods">
    //<editor-fold defaultstate="collapsed" desc="- Adding data">
    /**
     * Add a new ComparisonResult to the data set
     *
     * @param tmpResult ComparisonResult to be added
     */
    public synchronized void addDatum(ComparisonResult tmpResult) {
        //<editor-fold defaultstate="collapsed" desc="Checks">
        if (tmpResult == null) {
            return;
        }
        //</editor-fold>
        this.comparisonFeatureToDataMap.values().forEach((HistogramData tmpData) -> {
            if (Double.isNaN(tmpResult.getSimilarities()[tmpData.getComparisonFeautureNumber()])) {
                return;
            }
            tmpData.setMinComparisonFeatureValue(
                tmpData.getMinComparisonFeatureValue() < tmpResult.getSimilarities()[tmpData.getComparisonFeautureNumber()] ? 
                tmpData.getMinComparisonFeatureValue() : tmpResult.getSimilarities()[tmpData.getComparisonFeautureNumber()]
            );
            tmpData.setMaxComparisonFeatureValue(
                tmpData.getMaxComparisonFeatureValue() > tmpResult.getSimilarities()[tmpData.getComparisonFeautureNumber()] ? 
                tmpData.getMaxComparisonFeatureValue() : tmpResult.getSimilarities()[tmpData.getComparisonFeautureNumber()]
            );
        });
        this.nativeDataList.add(tmpResult);
    }
    //</editor-fold>
    public synchronized ByteArrayOutputStream getObjectAsByteStream() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        baos.write(toBytes(this.inputFile1.length()));
        baos.write(this.inputFile1.getBytes());
        baos.write(toBytes(this.inputFile2.length()));
        baos.write(this.inputFile2.getBytes());
        
        return baos;
    }
    private byte[] toBytes(int i) {
        byte[] result = new byte[4];

        result[0] = (byte) (i >> 24);
        result[1] = (byte) (i >> 16);
        result[2] = (byte) (i >> 8);
        result[3] = (byte) (i /*>> 0*/);

        return result;
    }
    //<editor-fold defaultstate="collapsed" desc="- Public properties">
    /**
     * Get the number of data that were added to the data set
     *
     * @return number of added data
     */
    public int getNumberOfComparedPairs() {
        return this.nativeDataList.size();
    }
    
    /**
     * Get the set of ComparisonFeatures for which histogram data is available
     *
     * @return the set of ComparisonFeatures for which histogram data is 
     * available
     */
    public Set<ComparisonFeature> getComparisonFeatureSet() {
        return this.comparisonFeatureToDataMap.keySet();
    }
    
    /**
     * Get the HistogramData object corresponding to the given ComparisonFeature
     * 
     * @param tmpComparisonFeature specifies which HistogramData object 
     * to return
     * @return the HistogramData object corresponding to the given 
     * ComparisonFeature
     */
    public HistogramData getHistogramData(ComparisonFeature tmpComparisonFeature) {
        return this.comparisonFeatureToDataMap.get(tmpComparisonFeature);
    }
    
    /**
     * Get the list of all the ComparisonResults (the native data) that belong 
     * to the data set
     * 
     * @return a list of ComparisonResults
     */
    public List<ComparisonResult> getNativeDataList() {
        return this.nativeDataList;
    }
    
    /**
     * Get the name of the first input file
     * 
     * @return the name of the first input file
     */
    public String getInputFile1() {
        return this.inputFile1;
    }
    
    /**
     * Get the name of the second input file
     * 
     * @return the name of the second input file
     */
    public String getInputFile2() {
        return this.inputFile2;
    }
    
    /**
     * Get the time difference, in milliseconds, between the data's calculation 
     * start time and midnight, January 1, 1970 UTC
     * 
     * @return the calculationStartTime in milliseconds
     */
    public long getCalculationStartTime() {
        return this.calculationStartTime;
    }
    
    /**
     * Get the time difference, in milliseconds, between the data's calculation 
     * finish time and midnight, January 1, 1970 UTC
     * 
     * @return the calculationFinishTime in milliseconds
     */
    public long getCalculationFinishTime() {
        return this.calculationFinishTime;
    }
    
    /**
     * Set the time difference, in milliseconds, between the data's calculation 
     * start time and midnight, January 1, 1970 UTC
     * 
     * @param tmpStartTime the difference to be set in milliseconds
     */
    public void setCalculationStartTime(long tmpStartTime) {
        this.calculationStartTime = tmpStartTime;
    }
    
    /**
     * Set the time difference, in milliseconds, between the data's calculation 
     * finish time and midnight, January 1, 1970 UTC
     * 
     * @param tmpFinishTime the difference to be set in milliseconds
     */
    public void setCalculationFinishTime(long tmpFinishTime) {
        this.calculationFinishTime = tmpFinishTime;
    }
    //</editor-fold>
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Private methods">
    /**
     * Write the important contents of this HistogramDataManager to an 
     * ObjectOutputStream so this HistogramDataManager can be loaded at
     * a later point in time. Important contents are the HistogramData 
     * objects, the previously calculated similarities and the origirnal input 
     * files
     * 
     * @param tmpObjectOutputStream ObjectOutputStream to which the contents 
     * will be written
     * @throws IOException any exception thrown by the underlying OutputStream
     */
    private synchronized void writeObject(java.io.ObjectOutputStream tmpObjectOutputStream) throws IOException {
        tmpObjectOutputStream.writeObject(this.comparisonFeatureToDataMap.values().toArray(new HistogramData[this.comparisonFeatureToDataMap.values().size()]));
        Iterator<ComparisonResult> tmpIterator = this.nativeDataList.iterator();
        double[][] tmpSimilaritiesArray = new double[this.nativeDataList.size()][this.nativeDataList.get(0).getSimilarities().length];
        int tmpCounter = 0;
        while (tmpIterator.hasNext()) {
            tmpSimilaritiesArray[tmpCounter] = tmpIterator.next().getSimilarities();
            tmpCounter++;
        }
        tmpObjectOutputStream.writeObject(tmpSimilaritiesArray);
        tmpObjectOutputStream.writeUTF(this.inputFile1);
        tmpObjectOutputStream.writeUTF(this.inputFile2);
        tmpObjectOutputStream.writeLong(this.calculationStartTime);
        tmpObjectOutputStream.writeLong(this.calculationFinishTime);
    }
    
    /**
     * Read the contents of the given ObjectInputStream and use them to recreate 
     * the corresponding HistogramDataManager.
     * 
     * @param tmpObjectInputStream ObjectInputStream from which the contents 
     * will be read
     * @throws IOException any of the usual Input/Output related exceptions
     * @throws ClassNotFoundException Class of a serialized object cannot be 
     * found
     */
    private void readObject(java.io.ObjectInputStream tmpObjectInputStream) throws IOException, ClassNotFoundException {
        HistogramData[] tmpDataAray = (HistogramData[]) tmpObjectInputStream.readObject();
        this.nativeDataList = new LinkedList<>();
        this.comparisonFeatureToDataMap = new EnumMap<>(ComparisonFeature.class);
        for (HistogramData tmpData : tmpDataAray) {
            // Set nativeDataList because it transient in HistogramData
            tmpData.setNativeDataList(this.nativeDataList);
            this.comparisonFeatureToDataMap.putIfAbsent(ComparisonFeature.values()[tmpData.getComparisonFeautureNumber()], tmpData);
        }
        double[][] tmpSimilaritiesArray = (double[][]) tmpObjectInputStream.readObject();
        for (int i = 0; i < tmpSimilaritiesArray.length; i++) {
            this.nativeDataList.add(new ComparisonResult(tmpSimilaritiesArray[i], "", "", i));
        }
        this.inputFile1 = tmpObjectInputStream.readUTF();
        this.inputFile2 = tmpObjectInputStream.readUTF();
        this.calculationStartTime = tmpObjectInputStream.readLong();
        this.calculationFinishTime = tmpObjectInputStream.readLong();
    }
    
    /**
     * This method is not implemented
     */
    private void readObjectNoData() {
        throw new RuntimeException("Method \"readObjectNoData\" is not implemented");
    }
    //</editor-fold>
    
}
