/*
 * Copyright (C) 2019 Jan-Mathis Hein
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

import de.whs.ibci.msc.utility.MSCConstants;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test class of the HistogramDataManager class
 *
 * @author Jan-Mathis Hein
 */
public class HistogramDataManagerTest {
    
    //<editor-fold defaultstate="collapsed" desc="Private fields">
    /**
     * The tolerance used when two double values are compared. Every deviation
     * that is smaller than this is still accepted as equal
     */
    private static final double TOLERANCE = 0.00001;
    
    /**
     * The histogram's expected bin borders
     */
    private static final double[] EXPECTED_BIN_BORDERS = {0, 0.8, 1.6, 2.4, 3.2, 4};
    
    /**
     * A HistogramDataManager instance on which the tests are done
     */
    private HistogramDataManager histogramDataManager;
    
    /**
     * The number of pairs that were added to the HistogramDataManager
     */
    private final int numberOfPairs;
    
    /**
     * The histogram's expected frequency data
     */
    private static final int[] EXPECTED_FREQUENCY_DATA = {1, 0, 0, 6, 1};
    
    /**
     * The ComparisonFeature that is used in this test
     */
    private static final ComparisonFeature MOLECULAR_DESCRIPTOR = ComparisonFeature.ATOM_COUNT;
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Initialize a HistogramDataManager and add some comparison results to it
     * 
     * @throws java.lang.Exception
     */
    public HistogramDataManagerTest() throws Exception {
        String[][] tmpInputPairs = {
            {"CC\n", "CC\n"}, {"C\n", "CC\n"}, {"C\n", "CC\n"}, {"C\n", "CC\n"}, 
            {"C\n", "CC\n"}, {"C\n", "CC\n"}, {"C\n", "CC\n"}
        };
        int tmpIdentifier = 0;
        final boolean[] tmpCompareAspects = new boolean[ComparisonFeature.values().length];
        tmpCompareAspects[HistogramDataManagerTest.MOLECULAR_DESCRIPTOR.getFeatureNumber()] = true;
        ParseAndCompareTask tmpTask = new ParseAndCompareTask("CC\n", "OO\n", InputType.SMILES, InputType.SMILES, tmpCompareAspects, tmpIdentifier++);
        tmpTask.call();
        this.histogramDataManager = new HistogramDataManager(
            Collections.singletonList(HistogramDataManagerTest.MOLECULAR_DESCRIPTOR), 
            HistogramDataManagerTest.EXPECTED_BIN_BORDERS.length - 1, tmpTask.getResult(),
            "", ""
        );
        for (String[] tmpPair : tmpInputPairs) {
            tmpTask = new ParseAndCompareTask(tmpPair[0], tmpPair[1], InputType.SMILES, InputType.SMILES, tmpCompareAspects, tmpIdentifier++);
            tmpTask.call();
            this.histogramDataManager.addDatum(tmpTask.getResult());
        }
        this.numberOfPairs = tmpIdentifier;
    }
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Public methods">
    /**
     * Test of binData method, of class HistogramDataManager. Also assert that 
     * the borders, the number of added data and the histogram data are as 
     * expected
     */
    @Test
    public void testBinDataAndCheckResults() {
        this.histogramDataManager.getHistogramData(HistogramDataManagerTest.MOLECULAR_DESCRIPTOR).call();
        assertEquals("Test number of added data", this.numberOfPairs, this.histogramDataManager.getNumberOfComparedPairs());
        for (int i = 0; i < HistogramDataManagerTest.EXPECTED_BIN_BORDERS.length; i++) {
            assertEquals(
                "Test bin border " + Integer.toString(i), 
                HistogramDataManagerTest.EXPECTED_BIN_BORDERS[i], 
                this.histogramDataManager.getHistogramData(HistogramDataManagerTest.MOLECULAR_DESCRIPTOR).getBinBorders()[i], TOLERANCE
            );
        }
        for (int i = 0; i < HistogramDataManagerTest.EXPECTED_FREQUENCY_DATA.length; i++) {
            assertEquals(
                HistogramDataManagerTest.EXPECTED_FREQUENCY_DATA[i], 
                this.histogramDataManager.getHistogramData(HistogramDataManagerTest.MOLECULAR_DESCRIPTOR).getFrequencies()[i],
                HistogramDataManagerTest.TOLERANCE
            );
        }
    }
    
    /**
     * Test of the writeObject and readObject methods or rather the 
     * serialization and deserialization, of class HistogramDataManager. Also 
     * assert that the borders and the histogram data are as expected. 
     * NOTE: Created files and directories will normaly be deleted upon 
     * completion
     * 
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    @Test
    public void testSerializationAndDeserialization() throws IOException, ClassNotFoundException {
        File tmpTemporaryFile = new File(System.getProperty("user.dir") + File.separator + MSCConstants.OUTPUT_FILE_NAME);
        try {
            tmpTemporaryFile.createNewFile();
            assertTrue("Test if file is readable", tmpTemporaryFile.canRead());
            assertTrue("Test if file is writable", tmpTemporaryFile.canWrite());
            try (
                FileOutputStream tmpFileOutputStream = new FileOutputStream(tmpTemporaryFile); 
                ObjectOutputStream tmpObjectOutputStream = new ObjectOutputStream(tmpFileOutputStream)
            ) {
                tmpObjectOutputStream.writeObject(this.histogramDataManager);
            }
            this.histogramDataManager = null;
            try (
                FileInputStream tmpFileInputStream = new FileInputStream(tmpTemporaryFile);
                ObjectInputStream tmpOutputStream = new ObjectInputStream(tmpFileInputStream)
            ) {
                this.histogramDataManager = (HistogramDataManager) tmpOutputStream.readObject();
            }
            this.histogramDataManager.getComparisonFeatureSet().forEach((ComparisonFeature tmpComparisonFeature) -> {
                this.histogramDataManager.getHistogramData(tmpComparisonFeature).call();
            });
            for (int i = 0; i < HistogramDataManagerTest.EXPECTED_BIN_BORDERS.length; i++) {
                assertEquals(
                    "Test bin border " + Integer.toString(i),
                    HistogramDataManagerTest.EXPECTED_BIN_BORDERS[i],
                    this.histogramDataManager.getHistogramData(HistogramDataManagerTest.MOLECULAR_DESCRIPTOR).getBinBorders()[i], TOLERANCE
                );
            }
            for (int i = 0; i < HistogramDataManagerTest.EXPECTED_FREQUENCY_DATA.length; i++) {
                assertEquals(
                    HistogramDataManagerTest.EXPECTED_FREQUENCY_DATA[i],
                    this.histogramDataManager.getHistogramData(HistogramDataManagerTest.MOLECULAR_DESCRIPTOR).getFrequencies()[i],
                    HistogramDataManagerTest.TOLERANCE
                );
            }
        } finally {
            tmpTemporaryFile.delete();
        }
    }
    //</editor-fold>
    
}
