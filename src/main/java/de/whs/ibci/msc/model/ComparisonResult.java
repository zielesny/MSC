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

/**
 * Represents the result of a molecule pair comparison. Also stores the two 
 * compared molecules as SMILES.
 *
 * @author Jan-Mathis Hein
 */
public class ComparisonResult {
    
    //<editor-fold defaultstate="collapsed" desc="Private instance variables">
    /**
     * Array with the different comparison results. The index corresponds to the
     * number of the ComparisonFeature.  If the computation of a comparison 
     * failed, Double.NaN is put instead
     */
    private final double[] similarities;
    
    /**
     * SMILES encoding of the first compared molecule
     */
    private String molecule1;
    
    /**
     * SMILES encoding of the second compared molecule
     */
    private String molecule2;
    
    /**
     * Unique identifier for this object
     */
    private final int identifier;
    
    /**
     * Reason why the comparison may have failed
     */
    private final String reasonOfFailure;
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Constructors">
    /**
     * Initialize the instance variables
     * 
     * @param tmpSimilarities the comparsion results in array form
     * @param tmpMolecule1 SMILES encoding of the first compared molecule, 
     * should never be null
     * @param tmpMolecule2 SMILES encoding of the second compared molecule,
     * should never be null
     * @param tmpIdentifier specifies an unique identifier for this object
     */
    public ComparisonResult(double[] tmpSimilarities, String tmpMolecule1, String tmpMolecule2, int tmpIdentifier) {
        this.similarities = tmpSimilarities;
        this.molecule1 = tmpMolecule1;
        this.molecule2 = tmpMolecule2;
        this.identifier = tmpIdentifier;
        this.reasonOfFailure = "";
    }
    
    /**
     * Initialize the instance variables
     * 
     * @param tmpString reason why the comparison failed
     * @param tmpIdentifier specifies an unique identifier for this object
     */
    public ComparisonResult(String tmpString, int tmpIdentifier) {
        this.similarities = null;
        this.molecule1 = "";
        this.molecule2 = "";
        this.reasonOfFailure = tmpString;
        this.identifier = tmpIdentifier;
    }
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Public methods">
    /**
     * Get the comparison results. The index corresponds to the number of the 
     * ComparisonFeature. If the computation of a comparison failed, Double.NaN 
     * is put instead
     * 
     * @return the comparison results
     */
    public double[] getSimilarities() {
        return this.similarities;
    }
    
    /**
     * Get the SMILES encoding of the first compared molecule
     * 
     * @return the first molecule
     */
    public String getMolecule1() {
        return this.molecule1;
    }
    
    /**
     * Get the SMILES encoding of the second compared molecule
     * 
     * @return the second molecule
     */
    public String getMolecule2() {
        return this.molecule2;
    }
    
    /**
     * Get the unique identifier of this object
     * 
     * @return the unique identifier of this object
     */
    public int getIdentifier() {
        return this.identifier;
    }
    
    /**
     * Get the reason why the comparison failed
     * 
     * @return the reason why the comparison failed
     */
    public String getReasonOfFailure() {
        return this.reasonOfFailure;
    }
    
    /**
     * True if for both molecules a SMILES encoding is available, 
     * false otherwise
     *
     * @return true if for both molecules a SMILES encoding is available, 
     * false otherwise
     */
    public boolean hasMoleculePair() {
        return !this.molecule1.isBlank()&& !this.molecule2.isBlank();
    }
    
    /**
     * Set the first molecule
     * 
     * @param tmpMolecule specifies the molecule to set
     */
    public void setMolecule1(String tmpMolecule) {
        this.molecule1 = tmpMolecule;
    }
    
    /**
     * Set the second molecule
     * 
     * @param tmpMolecule specifies the molecule to set
     */
    public void setMolecule2(String tmpMolecule) {
        this.molecule2 = tmpMolecule;
    }
    //</editor-fold>
    
}
