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
 * Defines different features that can be used to compare molecules
 *
 * @author Jan-Mathis Hein
 */
public enum ComparisonFeature {
    
    //<editor-fold defaultstate="collapsed" desc="Definitions">
    TANIMOTO_BASIC_FINGERPRINTER ("Basic fingerprint", true, 0.0, 1.0, "The Tanimoto coefficient with the basic CDK fingerprint"),
    TANIMOTO_LINGO_FINGERPRINTER ("LINGO fingerprint", true, 0.0, 1.0, "The Tanimoto coefficient with the LINGO fingerprint"),
    TANIMOTO_EXTENDED_FINGERPRINTER ("Extended fingerprint", true, 0.0, 1.0, "The Tanimoto coefficient with the extended CDK fingerprint"),
    TANIMOTO_ESTATE_FINGERPRINTER ("E-State fingerprint", true, 0.0, 1.0, "The Tanimoto coefficient with the E-State fingerprint"),
    TANIMOTO_PUBCHEM_FINGERPRINTER ("PubChem fingerprint", true, 0.0, 1.0, "The Tanimoto coefficient with the pubchem fingerprint"),
    TANIMOTO_SHORTEST_PATH_FINGERPRINTER ("Shortest path fingerprint", true, 0.0, 1.0, "The Tanimoto coefficient with the shortest path fingerprint"),
    TANIMOTO_SUBSTRUCTURE_FINGERPRINTER ("Substructure fingerprint", true, 0.0, 1.0, "The Tanimoto coefficient with default substructure fingerprint"),
    ATOM_COUNT ("Atom count", false, 0.0, Double.NaN, "The difference of the counts of all atoms"),
    CARBON_COUNT ("C count", false, 0.0, Double.NaN, "The difference of the counts of all carbon atoms"),
    OXYGEN_COUNT ("O count", false, 0.0, Double.NaN, "The difference of the counts of all oyxgen atoms"),
    SULFUR_COUNT ("S count", false, 0.0, Double.NaN, "The difference of the counts of all sulfur atoms"),
    NITROGEN_COUNT ("N count", false, 0.0, Double.NaN, "The difference of the counts of all nitrogen atoms"),
    PHOSPHOR_COUNT ("P count", false, 0.0, Double.NaN, "The difference of the counts of all phosphor atoms"),
    MOLECULAR_WEIGHT ("Molecular weight", true, 0.0, Double.NaN, "The difference of molecular weights"),
    MANNHOLD_LOGP ("Mannhold LogP", true, Double.NaN, Double.NaN, "The difference of Mannhold LogP values"),
    APOL ("Atomic polarizability", true, Double.NaN, Double.NaN, "The difference of atomic polarizabilites"),
    ACIDIC_GROUP_COUNT ("Acidic group count", false, 0.0, Double.NaN, "The difference of the acidic group counts"),
    AROMATIC_ATOM_COUNT ("Aromatic atom count", false, 0.0, Double.NaN, "The difference of the counts of all aromatic atoms"),
    BASIC_GROUP_COUNT ("Basic group count", false, 0.0, Double.NaN, "The difference of basic group counts"),
    BOND_COUNT ("Bond count", false, 0.0, Double.NaN, "The difference of counts of all bonds"),
    AROMATIC_BOND_COUNT ("Aromatic bond count", false, 0.0, Double.NaN, "The difference of counts of all aromatic bonds"),
    SINGLE_BOND_COUNT ("Single bond count", false, 0.0, Double.NaN, "The difference of counts of all  single bonds"),
    DOUBLE_BOND_COUNT ("Double bond count", false, 0.0, Double.NaN, "The difference of counts of all double bonds"),
    TRIPLE_BOND_COUNT ("Triple bond count", false, 0.0, Double.NaN, "The difference of counts of all triple bonds"),
    QUADRUPLE_BOND_COUNT ("Quadruple bond count", false, 0.0, Double.NaN, "The difference of counts of all quadruple bonds"),
    ALL_SMALL_RINGS_COUNT ("Small rings count", false, 0.0, Double.NaN, "The difference of the counts of all rings with a size of 9 or smaller"),
    AROMATIC_RINGS_COUNT ("Aromatic rings count", false, 0.0, Double.NaN, "The difference of the counts of all aromatic rings with a size of 9 or smaller"),
    SIZE_3_RINGS_COUNT ("Size 3 rings count", false, 0.0, Double.NaN, "The difference of the counts of all rings with size 3"),
    SIZE_4_RINGS_COUNT ("Size 4 rings count", false, 0.0, Double.NaN, "The difference of the counts of all rings with size 4"),
    SIZE_5_RINGS_COUNT ("Size 5 rings count", false, 0.0, Double.NaN, "The difference of the counts of all rings with size 5"),
    SIZE_6_RINGS_COUNT ("Size 6 rings count", false, 0.0, Double.NaN, "The difference of the counts of all rings with size 6"),
    SIZE_7_RINGS_COUNT ("Size 7 rings count", false, 0.0, Double.NaN, "The difference of the counts of all rings with size 7"),
    SIZE_8_RINGS_COUNT ("Size 8 rings count", false, 0.0, Double.NaN, "The difference of the counts of all rings with size 8"),
    SIZE_9_RINGS_COUNT ("Size 9 rings count", false, 0.0, Double.NaN, "The difference of the counts of all rings with size 9"),
    ALL_RINGS_COUNT ("All rings count", false, 0.0, Double.NaN, "The difference of the counts of all rings (no size limitation"),
    ATS_CHARGE1 ("ATS charge", true, Double.NaN, Double.NaN, "The difference of the autocorrelations of a topological structure, where the weights are equal to the charges"),
    ATS_MASS1 ("ATS mass", true, Double.NaN, Double.NaN, "The difference of the autocorrelations of a topological structure, where the weights are equal to the masses"),
    ATS_POLARIZABILITY1 ("ATS polarizability", true, Double.NaN, Double.NaN, "The difference of the autocorrelations of a topological structure, where the weights are equal to the polarizabilities"),
    BPOL ("Bond polarizabilities", true, Double.NaN, Double.NaN, "The difference of the bond polarizabilites"),
    C1SP1_COUNT ("C1SP1 count", false, 0.0, Double.NaN, "The difference of the counts of sp1 hybridized carbons bound to 1 other carbon"),
    C2SP1_COUNT ("C2SP1 count", false, 0.0, Double.NaN, "The difference of the counts of sp1 hybridized carbons bound to 2 other carbons"),
    C1SP2_COUNT ("C1SP2 count", false, 0.0, Double.NaN, "The difference of the counts of sp2 hybridized carbons bound to 1 other carbon"),
    C2SP2_COUNT ("C2SP2 count", false, 0.0, Double.NaN, "The difference of the counts of sp2 hybridized carbons bound to 2 other carbons"),
    C3SP2_COUNT ("C3SP2 count", false, 0.0, Double.NaN, "The difference of the counts of sp2 hybridized carbons bound to 3 other carbons"),
    C1SP3_COUNT ("C1SP3 count", false, 0.0, Double.NaN, "The difference of the counts of sp3 hybridized carbons bound to 1 other carbon"),
    C2SP3_COUNT ("C2SP3 count", false, 0.0, Double.NaN, "The difference of the counts of sp3 hybridized carbons bound to 2 other carbons"),
    C3SP3_COUNT ("C3SP3 count", false, 0.0, Double.NaN, "The difference of the counts of sp3 hybridized carbons bound to 3 other carbons"),
    C4SP3_COUNT ("C4SP3 count", false, 0.0, Double.NaN, "The difference of the counts of sp3 hybridized carbons bound to 4 other carbons"),
    ECCENTRIC_CONNECTIVITY_INDEX ("Eccentric connectivity", false, Double.NaN, Double.NaN, "The difference of the eccentric conectivity indices"),
    FMF ("Fraction of MF", true, 0.0, 1.0, "The difference of the fractions of the number of heavy atoms in the molecular framework to the total number of heavy atoms"),
    FCSP3 ("Fraction of SP3 carbons", true, 0.0, 1.0, "The difference of the fractions of the number of sp3 hybridized carbons to the total number of carbons"),
    FPSA ("Fractional PSA", true, 0.0, 1.0, "The difference of the polar surface areas expressed as a ratio to the molecular size"),
    FRAGMENT_COMPLEXITY ("Fragment complexity", true, Double.NaN, Double.NaN, "The difference of the values of the FragmentComplexityDescriptor of the CDK"),
    H_BOND_ACCEPTOR_COUNT ("H-bond acceptor count", false, 0.0, Double.NaN, "The difference of the counts of H-bond acceptors"),
    H_BOND_DONOR_COUNT ("H-bond donor count", false, 0.0, Double.NaN, "The difference of the counts of H-bond donors"),
    JP_LOGP ("JPLogP", true, Double.NaN, Double.NaN, "The difference of the JPLogP values"),
    KAPPA_SHAPE_INDEX_1 ("1. kappa shape index", true, Double.NaN, Double.NaN, "The difference of the first kappa shape indices"),
    KAPPA_SHAPE_INDEX_2 ("2. kappa shape index", true, Double.NaN, Double.NaN, "The difference of the second kappa shape indices"),
    KAPPA_SHAPE_INDEX_3 ("3. kappa shape index", true, Double.NaN, Double.NaN, "The difference of the third kappa shape indices"),
    LARGEST_CHAIN ("Largest chain", false, 0.0, Double.NaN, "The difference of the lengths of the longest chains"),
    LARGEST_PI_SYSTEM ("Largest pi system", false, 0.0, Double.NaN, "The difference of the sizes of the largest pi-systems"),
    LONGEST_ALIPHATIC_CHAIN ("Longest aliphatic chain", false, 0.0, Double.NaN, "The difference of the lengths of the longest aliphatic chains"),
    PETITJEAN_NUMBER ("Petitjean number", true, Double.NaN, Double.NaN, "The difference of the Petitjean numbers"),
    PETITJEAN_SHAPE_INDEX ("Petitjean shape index", true, Double.NaN, Double.NaN, "The difference of the topological Petitjean shape indices"),
    ROTATABLE_BOND_COUNT ("Rotatable bond count", false, 0.0, Double.NaN, "The difference of the counts of rotatable bonds"),
    SPIRO_ATOM_COUNT ("Spiro atom count", false, 0.0, Double.NaN, "The difference of the coutns of spiro-atoms"),
    TPSA ("Topolgical PSA", true, 0.0, Double.NaN, "The difference of the topological polar surface areas"),
    VABC ("VdW volume (using VABC)", true, 0.0, Double.NaN, "The difference of the Van der Waals volumes calculated using VABC method"),
    V_ADJ_MA ("Vertex adjacency", true, Double.NaN, Double.NaN, "The difference of the vertex adjacency numbers"),
    WEIGHTED_PATH_1 ("Weighted path descriptor", true, Double.NaN, Double.NaN, "The difference of the values of the weighted path descriptor which characterizes molecular branching"),
    WIENER_PATH ("Wiener path number", true, Double.NaN, Double.NaN, "The difference of the Wiener path numbers"),
    WIENER_POLARITY ("Wiener polarity number", true, Double.NaN, Double.NaN, "The difference of the Wiener polarity numbers"),
    X_LOGP ("XLogP", true, Double.NaN, Double.NaN, "The difference of the XLogP values"),
    ZAGREB_INDEX ("Zagreb index", false, Double.NaN, Double.NaN, "The difference of the Zagreb indices"),
    A_LOGP ("Ghose-Crippen LogP", true, Double.NaN, Double.NaN, "The difference of the ALogP values (Ghose-Crippen LogKow)"),
    A_LOGP_2 ("Ghose-Crippen LogP 2", true, Double.NaN, Double.NaN, "The difference of the ALogP2 values (Ghose-Crippen LogKow)"),
    MOLAR_REFRACTIVITY ("Molar refractivity", true, Double.NaN, Double.NaN, "The difference of the molar refractivities"),
    EQUALITY ("Equality", false, 0.0, 1.0, "Returns 1 if the unique SMILES are the same, else returns 0");
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Private variables">
    /**
     * If true this ComparisonFeature has continuous values, if false it has 
     * discrete values
     */
    private final boolean isContinuous;
    
    /**
     * The minimal value this ComparisonFeature can take on or NaN if no such 
     * value is defined
     */
    private final double minimalValue;
    
    /**
     * The maximal value this ComparisonFeature can take on or NaN if no such 
     * value is defined
     */
    private final double maximalValue;
    
    /**
     * A number that uniquely corresponds to a ComparisonFeature
     */
    private final int featureNumber;
    
    /**
     * The value for the featureNumber of the next ComparisonFeature
     */
    private static int nextFeatureNumber = 0;
    
    /**
     * A more userfriendly name for the ComparisonFeature
     */
    private final String name;
    
    /**
     * A detailed description for the ComparisonFeature
     */
    private final String detailedDescription;
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Initialize the instance variables
     * 
     * @param tmpName specifies a more userfriendly name for the 
     * ComparisonFeature
     * @param tmpIsContinuous if true this ComparisonFeature has continuous 
     * values, if false it has discrete values 
     * @param tmpMinValue the minimal value this ComparisonFeature can take on 
     * or NaN if no such value is defined
     * @param tmpMaxValue the minimal value this ComparisonFeature can take on 
     * or NaN if no such value is defined
     * @param tmpDescription a detailed description for the ComparisonFeature
     */
    private ComparisonFeature(String tmpName, boolean tmpIsContinuous, double tmpMinValue, double tmpMaxValue, String tmpDescription) {
        this.featureNumber = ComparisonFeature.getNextFeatureNumber();
        this.isContinuous = tmpIsContinuous;
        this.name = tmpName;
        this.minimalValue = tmpMinValue;
        this.maximalValue = tmpMaxValue;
        this.detailedDescription = tmpDescription;
    }
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Public methods">
    /**
     * If true this ComparisonFeature has continuous values, if false it has 
     * discrete values
     * 
     * @return true if this ComparisonFeature has continuous values, 
     * false if it has discrete values
     */
    public boolean isContinuous() {
        return this.isContinuous;
    }
    
    /**
     * Get the minimal value this ComparisonFeature can take on or NaN if no 
     * such value is defined
     * 
     * @return the minimal value this ComparisonFeature can take on or NaN if no 
     * such value is defined
     */
    public double getMinimalValue() {
        return this.minimalValue;
    }
    
    /**
     * Get the maximal value this ComparisonFeature can take on or NaN if no 
     * such value is defined
     * 
     * @return the maximal value this ComparisonFeature can take on or NaN if no 
     * such value is defined
     */
    public double getMaximalValue() {
        return this.maximalValue;
    }
    
    /**
     * Get the feature number corresponding to the ComparisonFeature
     * 
     * @return the feature number
     */
    public int getFeatureNumber() {
        return this.featureNumber;
    }
    
    /**
     * Get the value for the next featureNumber an increment nextFeatureNumber 
     * by one
     * 
     * @return the value for the next featureNumber
     */
    private static int getNextFeatureNumber() {
        return ComparisonFeature.nextFeatureNumber++;
    }
    
    /**
     * Get a detailed description for the ComparisonFeature
     * 
     * @return a detailed description for the ComparisonFeature
     */
    public String getDetailedDescription() {
        return this.detailedDescription;
    }
    
    /**
     * Return a string representation of the ComparisonFeature
     * 
     * @return a string representation of the ComparisonFeature
     */
    @Override
    public String toString() {
        return this.name;
    }
    //</editor-fold>
}
