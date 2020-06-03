/*
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

import java.util.Arrays;
import java.util.Collection;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test class of the ParseAndCompareTask class
 *
 * @author Jan-Mathis Hein
 */
@RunWith(Parameterized.class)
public class ParseAndCompareTaskTest {
    
    //<editor-fold defaultstate="collapsed" desc="Private class variables">
    /**
     * The tolerance used when two double values are compared. Every deviation 
     * that is smaller than this is still accepted as equal
     */
    private static final double TOLERANCE = 0.00001;
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Public instance variables">
    /**
     * The first input molecule
     */
    @Parameter(0)
    public String molecule1;
    
    /**
     * The second input molecule
     */
    @Parameter(1)
    public String molecule2;
    
    /**
     * The first input type
     */
    @Parameter(2)
    public InputType inputType1;
    
    /**
     * The second input type
     */
    @Parameter(3)
    public InputType inputType2;
    
    /**
     * Defines which descriptors will be used for the molecule comparison
     */
    @Parameter(4)
    public boolean[] useDescriptors;
    
    /**
     * The the expected results of the comparison
     */
    @Parameter(5)
    public Double[] expectedResults;
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Public methods">
    /**
     * Create the parameter sets that contain the inputs and the expected
     * outputs that will be used for testing
     *
     * @return a collection where each item is an array that contains a
     * parameter set
     * @throws java.lang.Exception
     */
    @Parameters
    public static Collection<Object[]> createParameterSets() throws Exception {
        //<editor-fold defaultstate="collapsed" desc="Create first parameter set">
        String tmpMolecule1 = "COC(N)CP\n";
        String tmpMolecule2 = "COC(O)CS\n";
        InputType tmpInputType1 = InputType.SMILES;
        InputType tmpInputType2 = InputType.SMILES;
        boolean[] tmpCompareAspects = new boolean[ComparisonFeature.values().length];
        Double[] tmpExpectedResults = new Double[ComparisonFeature.values().length];
        // If a result is not set, that descriptor will not be used
        tmpExpectedResults[ComparisonFeature.TANIMOTO_BASIC_FINGERPRINTER.getFeatureNumber()] = 0.277777;
        tmpExpectedResults[ComparisonFeature.ATOM_COUNT.getFeatureNumber()] = 2.0;
        tmpExpectedResults[ComparisonFeature.CARBON_COUNT.getFeatureNumber()] = 0.0;
        tmpExpectedResults[ComparisonFeature.OXYGEN_COUNT.getFeatureNumber()] = 1.0;
        tmpExpectedResults[ComparisonFeature.SULFUR_COUNT.getFeatureNumber()] = 1.0;
        tmpExpectedResults[ComparisonFeature.NITROGEN_COUNT.getFeatureNumber()] = 1.0;
        tmpExpectedResults[ComparisonFeature.PHOSPHOR_COUNT.getFeatureNumber()] = 1.0;
        tmpExpectedResults[ComparisonFeature.MOLECULAR_WEIGHT.getFeatureNumber()] = 1.069143575;
        for (int i = 0; i < tmpExpectedResults.length; i++) {
            tmpCompareAspects[i] = tmpExpectedResults[i] != null;
        }
        //</editor-fold>
        Object[][] tmpData = {
            {tmpMolecule1, tmpMolecule2, tmpInputType1, tmpInputType2, tmpCompareAspects, tmpExpectedResults}
        };
        return Arrays.asList(tmpData);
    }
    
    /**
     * Test of call method, of class ParseAndCompareTask. Also assert that the 
     * results and state of the task are as expected
     */
    @Test
    public void testCall() throws Exception {
        ParseAndCompareTask tmpTask = new ParseAndCompareTask(this.molecule1, this.molecule2, this.inputType1, this.inputType2, this.useDescriptors, 0);
        tmpTask.call();
        assertFalse("Test if this is working", tmpTask.isWorking());
        assertTrue("Test if this is finished", tmpTask.isFinished());
        assertTrue("Test if this was started", tmpTask.isStarted());
        ComparisonResult tmpResult = tmpTask.getResult();
        assertEquals("Test for exceptions that occured during comparing", tmpResult.getReasonOfFailure(), "");
        Arrays.stream(ComparisonFeature.values()).filter((ComparisonFeature descriptor) -> this.useDescriptors[descriptor.getFeatureNumber()]).forEach((ComparisonFeature descriptor) -> {
            assertEquals(
                    "Test " + descriptor.toString(),
                    tmpTask.getResult().getSimilarities()[descriptor.getFeatureNumber()],
                    this.expectedResults[descriptor.getFeatureNumber()],
                    ParseAndCompareTaskTest.TOLERANCE
            );
        });
    }
    //</editor-fold>
    
}
