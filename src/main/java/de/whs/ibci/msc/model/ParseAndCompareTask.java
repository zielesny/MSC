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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.concurrent.Callable;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.aromaticity.Aromaticity;
import org.openscience.cdk.aromaticity.ElectronDonation;
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.fingerprint.*;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.io.SMILESWriter;
import org.openscience.cdk.io.iterator.IIteratingChemObjectReader;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
import org.openscience.cdk.qsar.descriptors.molecular.*;
import org.openscience.cdk.ringsearch.AllRingsFinder;
import org.openscience.cdk.ringsearch.RingSearch;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.similarity.Tanimoto;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.AtomTypeManipulator;

/**
 * A task that parses a pair of string encodings to a molecule pair and compares
 * the pair on the basis of specified ComparisonFeatures.
 *
 * @author Jan-Mathis Hein
 */
public class ParseAndCompareTask implements Callable<Void> {
    
    //<editor-fold defaultstate="collapsed" desc="Private instance variables">
    /**
     * True if this task has finished in any way, false otherwise
     */
    private boolean isFinished;
    
    /**
     * True if this task has been started, false otherwise
     */
    private boolean isStarted;
    
    /**
     * True if this task has been cancelled, false otherwise
     */
    private boolean isCancelled;
    
    /**
     * An array that defines on the basis of which ComparisonFeatures the 
     * comparison will be made. The index corresponds to the number of the
     * ComparisonFeature
     */
    private final boolean[] consideredComparisonFeature;
    
    /**
     * The result of the comparison
     */
    private ComparisonResult result;
    
    /**
     * InputType of the first molecule
     */
    private final InputType type1;
    
    /**
     * InputType of the second molecule
     */
    private final InputType type2;
    
    /**
     * Number that identifies this task
     */
    private final int identifier;
    
    /**
     * PropertyChangeSupport that manages the firing of PropertyChangeEvents and
     * the adding and removing of PropertyChangeListeners
     */
    private final PropertyChangeSupport propertyChangeSupport;
    
    /**
     * String encoding of the first molecule
     */
    private String molecule1;
    
    /**
     * String encoding of the second molecule
     */
    private String molecule2;
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Initialize the instance variables
     * 
     * @param tmpInput1 specifies the first input string
     * @param tmpInput2 specifies the second input string
     * @param tmpType1 specifies the first input type
     * @param tmpType2 specifies the first input type
     * @param tmpConsiderFeature specifies on the basis of which 
     * ComparisonFeatures the comparison will be made
     * @param tmpIdentifier unique identifier for this task
     */
    public ParseAndCompareTask(String tmpInput1, String tmpInput2, InputType tmpType1, InputType tmpType2, boolean[] tmpConsiderFeature, int tmpIdentifier) {
        this.isStarted = false;
        this.isFinished = false;
        this.isCancelled = false;
        this.molecule1 = tmpInput1;
        this.molecule2 = tmpInput2;
        this.type1 = tmpType1;
        this.type2 = tmpType2;
        this.consideredComparisonFeature = tmpConsiderFeature;
        this.identifier = tmpIdentifier;
        this.propertyChangeSupport = new PropertyChangeSupport(this);
    }
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Public methods">
    //<editor-fold defaultstate="collapsed" desc="- Task related">
    /**
     * Parse the input strings to molecules and compare them on the basis of the 
     * specified ComparisonFeatures
     * 
     * @return null
     */
    @Override
    public Void call() {
        try {
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Initialize variables">
            this.isStarted = true;
            double[] tmpSimilarities = new double[this.consideredComparisonFeature.length];
            IAtomContainer[] tmpMoleculePair = new IAtomContainer[2];
            SmilesParser tmpSmilesParser = new SmilesParser(SilentChemObjectBuilder.getInstance());
            IIteratingChemObjectReader<IAtomContainer> tmpSdfParser = new IteratingSDFReader(new StringReader(""), DefaultChemObjectBuilder.getInstance());
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Parse first molecule">
            switch (this.type1) {
                case SMILES:
                    tmpMoleculePair[0] = tmpSmilesParser.parseSmiles(this.molecule1);
                    break;
                case SDF:
                    tmpSdfParser.setReader(new StringReader(this.molecule1));
                    tmpMoleculePair[0] = tmpSdfParser.next();
                    break;
                default:
                    break;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Parse second molecule">
            switch (this.type2) {
                case SMILES:
                    tmpMoleculePair[1] = tmpSmilesParser.parseSmiles(this.molecule2);
                    break;
                case SDF:
                    tmpSdfParser.setReader(new StringReader(this.molecule2));
                    tmpMoleculePair[1] = tmpSdfParser.next();
                    break;
                default:
                    break;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Preprocess molecules">
            CDKAtomTypeMatcher tmpCDKAtomTypeMatcher = CDKAtomTypeMatcher.getInstance(DefaultChemObjectBuilder.getInstance());
            CDKHydrogenAdder tmpCDKHydrogenAdder = CDKHydrogenAdder.getInstance(DefaultChemObjectBuilder.getInstance());
            Aromaticity tmpAromaticity = new Aromaticity(ElectronDonation.cdk(), Cycles.or(Cycles.all(), Cycles.cdkAromaticSet()));
            RingSearch tmpRingSearch;
            for (IAtomContainer tmpMolecule : tmpMoleculePair) {
                tmpRingSearch = new RingSearch(tmpMolecule);
                for (IAtom tmpAtom : tmpMolecule.atoms()) {
                    tmpAtom.setIsInRing(tmpRingSearch.cyclic(tmpAtom));
                    IAtomType tmpType = tmpCDKAtomTypeMatcher.findMatchingAtomType(tmpMolecule, tmpAtom);
                    AtomTypeManipulator.configure(tmpAtom, tmpType);
                }
                tmpCDKHydrogenAdder.addImplicitHydrogens(tmpMolecule);
                AtomContainerManipulator.convertImplicitToExplicitHydrogens(tmpMolecule);
                // Sets aromaticity flags
                tmpAromaticity.apply(tmpMolecule);
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="AcidicGroupCountDescriptor">
            if (this.consideredComparisonFeature[ComparisonFeature.ACIDIC_GROUP_COUNT.getFeatureNumber()]) {
                AcidicGroupCountDescriptor tmpAcidicGroupCountDescriptor = new AcidicGroupCountDescriptor();
                tmpAcidicGroupCountDescriptor.initialise(DefaultChemObjectBuilder.getInstance());
                tmpSimilarities[ComparisonFeature.ACIDIC_GROUP_COUNT.getFeatureNumber()] = Math.abs(
                    Double.parseDouble(tmpAcidicGroupCountDescriptor.calculate(tmpMoleculePair[0]).getValue().toString()) -
                    Double.parseDouble(tmpAcidicGroupCountDescriptor.calculate(tmpMoleculePair[1]).getValue().toString())
                );
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="AllRingsCount">
            if (this.consideredComparisonFeature[ComparisonFeature.ALL_RINGS_COUNT.getFeatureNumber()]) {
                try {
                    AllRingsFinder.usingThreshold(AllRingsFinder.Threshold.PubChem_99);
                    AllRingsFinder tmpAllRingsFinder = new AllRingsFinder();
                    int tmpAllRingsCount1 = tmpAllRingsFinder.findAllRings(tmpMoleculePair[0]).getAtomContainerCount();
                    int tmpAllRingsCount2 = tmpAllRingsFinder.findAllRings(tmpMoleculePair[1]).getAtomContainerCount();
                    tmpSimilarities[ComparisonFeature.ALL_RINGS_COUNT.getFeatureNumber()] = Math.abs(tmpAllRingsCount1 - tmpAllRingsCount2);
                } catch (CDKException CDKException) {
                    tmpSimilarities[ComparisonFeature.ALL_RINGS_COUNT.getFeatureNumber()] = Double.NaN;
                }
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="ALOGPDescriptor">
            if (this.consideredComparisonFeature[ComparisonFeature.A_LOGP.getFeatureNumber()] ||
                this.consideredComparisonFeature[ComparisonFeature.A_LOGP_2.getFeatureNumber()] ||
                this.consideredComparisonFeature[ComparisonFeature.MOLAR_REFRACTIVITY.getFeatureNumber()]) {
                ALOGPDescriptor tmpALOGPDescriptor = new ALOGPDescriptor();
                tmpALOGPDescriptor.initialise(DefaultChemObjectBuilder.getInstance());
                String[] tmpArray1 = tmpALOGPDescriptor.calculate(tmpMoleculePair[0]).getValue().toString().split(",");
                String[] tmpArray2 = tmpALOGPDescriptor.calculate(tmpMoleculePair[1]).getValue().toString().split(",");
                // Ghose-Grippen logP
                if (this.consideredComparisonFeature[ComparisonFeature.A_LOGP.getFeatureNumber()]) {
                    tmpSimilarities[ComparisonFeature.A_LOGP.getFeatureNumber()] = Math.abs(Double.parseDouble(tmpArray1[0]) - Double.parseDouble(tmpArray2[0]));
                }
                // Ghose-Grippen logP 2
                if (this.consideredComparisonFeature[ComparisonFeature.A_LOGP_2.getFeatureNumber()]) {
                    tmpSimilarities[ComparisonFeature.A_LOGP_2.getFeatureNumber()] = Math.abs(Double.parseDouble(tmpArray1[1]) - Double.parseDouble(tmpArray2[1]));
                }
                // Molar refractivity
                if (this.consideredComparisonFeature[ComparisonFeature.MOLAR_REFRACTIVITY.getFeatureNumber()]) {
                    tmpSimilarities[ComparisonFeature.MOLAR_REFRACTIVITY.getFeatureNumber()] = Math.abs(Double.parseDouble(tmpArray1[2]) - Double.parseDouble(tmpArray2[2]));
                }
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="APolDescriptor">
            if (this.consideredComparisonFeature[ComparisonFeature.APOL.getFeatureNumber()]) {
                APolDescriptor tmpAPolDescriptor = new APolDescriptor();
                tmpAPolDescriptor.initialise(DefaultChemObjectBuilder.getInstance());
                tmpSimilarities[ComparisonFeature.APOL.getFeatureNumber()] = Math.abs(
                    Double.parseDouble(tmpAPolDescriptor.calculate(tmpMoleculePair[0]).getValue().toString()) -
                    Double.parseDouble(tmpAPolDescriptor.calculate(tmpMoleculePair[1]).getValue().toString())
                );
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="AromaticAtomsCountDescriptor">
            if (this.consideredComparisonFeature[ComparisonFeature.AROMATIC_ATOM_COUNT.getFeatureNumber()]) {
                AromaticAtomsCountDescriptor tmpAromaticAtomsCountDescriptor = new AromaticAtomsCountDescriptor();
                tmpAromaticAtomsCountDescriptor.initialise(DefaultChemObjectBuilder.getInstance());
                tmpAromaticAtomsCountDescriptor.setParameters(new Object[] {true});
                tmpSimilarities[ComparisonFeature.AROMATIC_ATOM_COUNT.getFeatureNumber()] = Math.abs(
                    Double.parseDouble(tmpAromaticAtomsCountDescriptor.calculate(tmpMoleculePair[0]).getValue().toString()) -
                    Double.parseDouble(tmpAromaticAtomsCountDescriptor.calculate(tmpMoleculePair[1]).getValue().toString())
                );
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="AromaticBondsCountDescriptor">
            if (this.consideredComparisonFeature[ComparisonFeature.AROMATIC_BOND_COUNT.getFeatureNumber()]) {
                AromaticBondsCountDescriptor tmpAromaticBondsCountDescriptor = new AromaticBondsCountDescriptor();
                tmpAromaticBondsCountDescriptor.initialise(DefaultChemObjectBuilder.getInstance());
                tmpAromaticBondsCountDescriptor.setParameters(new Object[] {true});
                tmpSimilarities[ComparisonFeature.AROMATIC_BOND_COUNT.getFeatureNumber()] = Math.abs(
                    Double.parseDouble(tmpAromaticBondsCountDescriptor.calculate(tmpMoleculePair[0]).getValue().toString()) -
                    Double.parseDouble(tmpAromaticBondsCountDescriptor.calculate(tmpMoleculePair[1]).getValue().toString())
                );
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="AtomCountDescriptor">
            AtomCountDescriptor tmpAtomCountDescriptor = new AtomCountDescriptor();
            tmpAtomCountDescriptor.initialise(DefaultChemObjectBuilder.getInstance());
            // Atom count
            if (this.consideredComparisonFeature[ComparisonFeature.ATOM_COUNT.getFeatureNumber()]) {
                tmpAtomCountDescriptor.setParameters(new Object[] {"*"});
                tmpSimilarities[ComparisonFeature.ATOM_COUNT.getFeatureNumber()] = Math.abs(
                    Double.parseDouble(tmpAtomCountDescriptor.calculate(tmpMoleculePair[0]).getValue().toString()) -
                    Double.parseDouble(tmpAtomCountDescriptor.calculate(tmpMoleculePair[1]).getValue().toString())
                );
            }
            // Carbon count
            if (this.consideredComparisonFeature[ComparisonFeature.CARBON_COUNT.getFeatureNumber()]) {
                tmpAtomCountDescriptor.setParameters(new Object[] {"C"});
                tmpSimilarities[ComparisonFeature.CARBON_COUNT.getFeatureNumber()] = Math.abs(
                    Double.parseDouble(tmpAtomCountDescriptor.calculate(tmpMoleculePair[0]).getValue().toString()) -
                    Double.parseDouble(tmpAtomCountDescriptor.calculate(tmpMoleculePair[1]).getValue().toString())
                );
            }
            // Oxygen count
            if (this.consideredComparisonFeature[ComparisonFeature.OXYGEN_COUNT.getFeatureNumber()]) {
                tmpAtomCountDescriptor.setParameters(new Object[] {"O"});
                tmpSimilarities[ComparisonFeature.OXYGEN_COUNT.getFeatureNumber()] = Math.abs(
                    Double.parseDouble(tmpAtomCountDescriptor.calculate(tmpMoleculePair[0]).getValue().toString()) -
                    Double.parseDouble(tmpAtomCountDescriptor.calculate(tmpMoleculePair[1]).getValue().toString())
                );
            }
            // Sulfur count
            if (this.consideredComparisonFeature[ComparisonFeature.SULFUR_COUNT.getFeatureNumber()]) {
                tmpAtomCountDescriptor.setParameters(new Object[] {"S"});
                tmpSimilarities[ComparisonFeature.SULFUR_COUNT.getFeatureNumber()] = Math.abs(
                    Double.parseDouble(tmpAtomCountDescriptor.calculate(tmpMoleculePair[0]).getValue().toString()) -
                    Double.parseDouble(tmpAtomCountDescriptor.calculate(tmpMoleculePair[1]).getValue().toString())
                );
            }
            // Nitrogen count
            if (this.consideredComparisonFeature[ComparisonFeature.NITROGEN_COUNT.getFeatureNumber()]) {
                tmpAtomCountDescriptor.setParameters(new Object[] {"N"});
                tmpSimilarities[ComparisonFeature.NITROGEN_COUNT.getFeatureNumber()] = Math.abs(
                    Double.parseDouble(tmpAtomCountDescriptor.calculate(tmpMoleculePair[0]).getValue().toString()) -
                    Double.parseDouble(tmpAtomCountDescriptor.calculate(tmpMoleculePair[1]).getValue().toString())
                );
            }
            // Phosphor count
            if (this.consideredComparisonFeature[ComparisonFeature.PHOSPHOR_COUNT.getFeatureNumber()]) {
                tmpAtomCountDescriptor.setParameters(new Object[] {"P"});
                tmpSimilarities[ComparisonFeature.PHOSPHOR_COUNT.getFeatureNumber()] = Math.abs(
                    Double.parseDouble(tmpAtomCountDescriptor.calculate(tmpMoleculePair[0]).getValue().toString()) -
                    Double.parseDouble(tmpAtomCountDescriptor.calculate(tmpMoleculePair[1]).getValue().toString())
                );
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="AutocorrelationDescriptorCharge">
            if (this.consideredComparisonFeature[ComparisonFeature.ATS_CHARGE1.getFeatureNumber()]) {
                AutocorrelationDescriptorCharge tmpAutocorrelationDescriptorCharge = new AutocorrelationDescriptorCharge();
                tmpAutocorrelationDescriptorCharge.initialise(DefaultChemObjectBuilder.getInstance());
                String[] tmpArray1 = tmpAutocorrelationDescriptorCharge.calculate(tmpMoleculePair[0]).getValue().toString().split(",");
                String[] tmpArray2 = tmpAutocorrelationDescriptorCharge.calculate(tmpMoleculePair[1]).getValue().toString().split(",");
                tmpSimilarities[ComparisonFeature.ATS_CHARGE1.getFeatureNumber()] = Math.abs(Double.parseDouble(tmpArray1[0]) - Double.parseDouble(tmpArray2[0]));
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="AutocorrelationDescriptorMass">
            if (this.consideredComparisonFeature[ComparisonFeature.ATS_MASS1.getFeatureNumber()]) {
                AutocorrelationDescriptorMass tmpAutocorrelationDescriptorMass = new AutocorrelationDescriptorMass();
                tmpAutocorrelationDescriptorMass.initialise(DefaultChemObjectBuilder.getInstance());
                String[] tmpArray1 = tmpAutocorrelationDescriptorMass.calculate(tmpMoleculePair[0]).getValue().toString().split(",");
                String[] tmpArray2 = tmpAutocorrelationDescriptorMass.calculate(tmpMoleculePair[1]).getValue().toString().split(",");
                tmpSimilarities[ComparisonFeature.ATS_MASS1.getFeatureNumber()] = Math.abs(Double.parseDouble(tmpArray1[0]) - Double.parseDouble(tmpArray2[0]));
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="AutocorrelationDescriptorPolarizability">
            if (this.consideredComparisonFeature[ComparisonFeature.ATS_POLARIZABILITY1.getFeatureNumber()]) {
                AutocorrelationDescriptorPolarizability tmpAutocorrelationDescriptorPolarizability = new AutocorrelationDescriptorPolarizability();
                tmpAutocorrelationDescriptorPolarizability.initialise(DefaultChemObjectBuilder.getInstance());
                String[] tmpArray1 = tmpAutocorrelationDescriptorPolarizability.calculate(tmpMoleculePair[0]).getValue().toString().split(",");
                String[] tmpArray2 = tmpAutocorrelationDescriptorPolarizability.calculate(tmpMoleculePair[1]).getValue().toString().split(",");
                tmpSimilarities[ComparisonFeature.ATS_POLARIZABILITY1.getFeatureNumber()] = Math.abs(Double.parseDouble(tmpArray1[0]) - Double.parseDouble(tmpArray2[0]));
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="BasicGroupCountDescriptor">
            if (this.consideredComparisonFeature[ComparisonFeature.BASIC_GROUP_COUNT.getFeatureNumber()]) {
                BasicGroupCountDescriptor tmpBasicGroupCountDescriptor = new BasicGroupCountDescriptor();
                tmpBasicGroupCountDescriptor.initialise(DefaultChemObjectBuilder.getInstance());
                tmpSimilarities[ComparisonFeature.BASIC_GROUP_COUNT.getFeatureNumber()] = Math.abs(
                    Double.parseDouble(tmpBasicGroupCountDescriptor.calculate(tmpMoleculePair[0]).getValue().toString()) -
                    Double.parseDouble(tmpBasicGroupCountDescriptor.calculate(tmpMoleculePair[1]).getValue().toString())
                );
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="BondCountDescriptor">
            BondCountDescriptor tmpBondCountDescriptor = new BondCountDescriptor();
            tmpBondCountDescriptor.initialise(DefaultChemObjectBuilder.getInstance());
            // Bond count
            if (this.consideredComparisonFeature[ComparisonFeature.BOND_COUNT.getFeatureNumber()]) {
                tmpBondCountDescriptor.setParameters(new Object[] {""});
                tmpSimilarities[ComparisonFeature.BOND_COUNT.getFeatureNumber()] = Math.abs(
                    Double.parseDouble(tmpBondCountDescriptor.calculate(tmpMoleculePair[0]).getValue().toString()) -
                    Double.parseDouble(tmpBondCountDescriptor.calculate(tmpMoleculePair[1]).getValue().toString())
                );
            }
            // Single bond count
            if (this.consideredComparisonFeature[ComparisonFeature.SINGLE_BOND_COUNT.getFeatureNumber()]) {
                tmpBondCountDescriptor.setParameters(new Object[] {"s"});
                tmpSimilarities[ComparisonFeature.SINGLE_BOND_COUNT.getFeatureNumber()] = Math.abs(
                    Double.parseDouble(tmpBondCountDescriptor.calculate(tmpMoleculePair[0]).getValue().toString()) -
                    Double.parseDouble(tmpBondCountDescriptor.calculate(tmpMoleculePair[1]).getValue().toString())
                );
            }
            // Double bond count
            if (this.consideredComparisonFeature[ComparisonFeature.DOUBLE_BOND_COUNT.getFeatureNumber()]) {
                tmpBondCountDescriptor.setParameters(new Object[] {"d"});
                tmpSimilarities[ComparisonFeature.DOUBLE_BOND_COUNT.getFeatureNumber()] = Math.abs(
                    Double.parseDouble(tmpBondCountDescriptor.calculate(tmpMoleculePair[0]).getValue().toString()) -
                    Double.parseDouble(tmpBondCountDescriptor.calculate(tmpMoleculePair[1]).getValue().toString())
                );
            }
            // Triple bond count
            if (this.consideredComparisonFeature[ComparisonFeature.TRIPLE_BOND_COUNT.getFeatureNumber()]) {
                tmpBondCountDescriptor.setParameters(new Object[] {"t"});
                tmpSimilarities[ComparisonFeature.TRIPLE_BOND_COUNT.getFeatureNumber()] = Math.abs(
                    Double.parseDouble(tmpBondCountDescriptor.calculate(tmpMoleculePair[0]).getValue().toString()) -
                    Double.parseDouble(tmpBondCountDescriptor.calculate(tmpMoleculePair[1]).getValue().toString())
                );
            }
            // Quadruple bond count
            if (this.consideredComparisonFeature[ComparisonFeature.QUADRUPLE_BOND_COUNT.getFeatureNumber()]) {
                tmpBondCountDescriptor.setParameters(new Object[] {"q"});
                tmpSimilarities[ComparisonFeature.QUADRUPLE_BOND_COUNT.getFeatureNumber()] = Math.abs(
                    Double.parseDouble(tmpBondCountDescriptor.calculate(tmpMoleculePair[0]).getValue().toString()) -
                    Double.parseDouble(tmpBondCountDescriptor.calculate(tmpMoleculePair[1]).getValue().toString())
                );
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="BPolDescriptor">
            if (this.consideredComparisonFeature[ComparisonFeature.BPOL.getFeatureNumber()]) {
                BPolDescriptor tmpBPolDescriptor = new BPolDescriptor();
                tmpBPolDescriptor.initialise(DefaultChemObjectBuilder.getInstance());
                tmpSimilarities[ComparisonFeature.BPOL.getFeatureNumber()] = Math.abs(
                    Double.parseDouble(tmpBPolDescriptor.calculate(tmpMoleculePair[0]).getValue().toString()) -
                    Double.parseDouble(tmpBPolDescriptor.calculate(tmpMoleculePair[1]).getValue().toString())
                );
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="CarbonTypesDescriptor">
            if (this.consideredComparisonFeature[ComparisonFeature.C1SP1_COUNT.getFeatureNumber()] ||
                this.consideredComparisonFeature[ComparisonFeature.C2SP1_COUNT.getFeatureNumber()] ||
                this.consideredComparisonFeature[ComparisonFeature.C1SP2_COUNT.getFeatureNumber()] ||
                this.consideredComparisonFeature[ComparisonFeature.C2SP2_COUNT.getFeatureNumber()] ||
                this.consideredComparisonFeature[ComparisonFeature.C3SP2_COUNT.getFeatureNumber()] ||
                this.consideredComparisonFeature[ComparisonFeature.C1SP3_COUNT.getFeatureNumber()] ||
                this.consideredComparisonFeature[ComparisonFeature.C2SP3_COUNT.getFeatureNumber()] ||
                this.consideredComparisonFeature[ComparisonFeature.C3SP3_COUNT.getFeatureNumber()] ||
                this.consideredComparisonFeature[ComparisonFeature.C4SP3_COUNT.getFeatureNumber()]) {
                CarbonTypesDescriptor tmpCarbonTypesDescriptor = new CarbonTypesDescriptor();
                tmpCarbonTypesDescriptor.initialise(DefaultChemObjectBuilder.getInstance());
                String[] tmpArray1 = tmpCarbonTypesDescriptor.calculate(tmpMoleculePair[0]).getValue().toString().split(",");
                String[] tmpArray2 = tmpCarbonTypesDescriptor.calculate(tmpMoleculePair[1]).getValue().toString().split(",");
                // C1SP1 count
                if (this.consideredComparisonFeature[ComparisonFeature.C1SP1_COUNT.getFeatureNumber()]) {
                    tmpSimilarities[ComparisonFeature.C1SP1_COUNT.getFeatureNumber()] = Math.abs(Double.parseDouble(tmpArray1[0]) - Double.parseDouble(tmpArray2[0]));
                }
                // C2SP1 count
                if (this.consideredComparisonFeature[ComparisonFeature.C2SP1_COUNT.getFeatureNumber()]) {
                    tmpSimilarities[ComparisonFeature.C2SP1_COUNT.getFeatureNumber()] = Math.abs(Double.parseDouble(tmpArray1[1]) - Double.parseDouble(tmpArray2[1]));
                }
                // C1SP2 count
                if (this.consideredComparisonFeature[ComparisonFeature.C1SP2_COUNT.getFeatureNumber()]) {
                    tmpSimilarities[ComparisonFeature.C1SP2_COUNT.getFeatureNumber()] = Math.abs(Double.parseDouble(tmpArray1[2]) - Double.parseDouble(tmpArray2[2]));
                }
                // C2SP2 count
                if (this.consideredComparisonFeature[ComparisonFeature.C2SP2_COUNT.getFeatureNumber()]) {
                    tmpSimilarities[ComparisonFeature.C2SP2_COUNT.getFeatureNumber()] = Math.abs(Double.parseDouble(tmpArray1[3]) - Double.parseDouble(tmpArray2[3]));
                }
                // C3SP2 count
                if (this.consideredComparisonFeature[ComparisonFeature.C3SP2_COUNT.getFeatureNumber()]) {
                    tmpSimilarities[ComparisonFeature.C3SP2_COUNT.getFeatureNumber()] = Math.abs(Double.parseDouble(tmpArray1[4]) - Double.parseDouble(tmpArray2[4]));
                }
                // C1SP3 count
                if (this.consideredComparisonFeature[ComparisonFeature.C1SP3_COUNT.getFeatureNumber()]) {
                    tmpSimilarities[ComparisonFeature.C1SP3_COUNT.getFeatureNumber()] = Math.abs(Double.parseDouble(tmpArray1[5]) - Double.parseDouble(tmpArray2[5]));
                }
                // C2SP3 count
                if (this.consideredComparisonFeature[ComparisonFeature.C2SP3_COUNT.getFeatureNumber()]) {
                    tmpSimilarities[ComparisonFeature.C2SP3_COUNT.getFeatureNumber()] = Math.abs(Double.parseDouble(tmpArray1[6]) - Double.parseDouble(tmpArray2[6]));
                }
                // C3SP3 count
                if (this.consideredComparisonFeature[ComparisonFeature.C3SP3_COUNT.getFeatureNumber()]) {
                    tmpSimilarities[ComparisonFeature.C3SP3_COUNT.getFeatureNumber()] = Math.abs(Double.parseDouble(tmpArray1[7]) - Double.parseDouble(tmpArray2[7]));
                }
                // C4SP3 count
                if (this.consideredComparisonFeature[ComparisonFeature.C4SP3_COUNT.getFeatureNumber()]) {
                    tmpSimilarities[ComparisonFeature.C4SP3_COUNT.getFeatureNumber()] = Math.abs(Double.parseDouble(tmpArray1[8]) - Double.parseDouble(tmpArray2[8]));
                }
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Equality">
            if (this.consideredComparisonFeature[ComparisonFeature.EQUALITY.getFeatureNumber()]) {
                SmilesGenerator tmpSmilesGenerator = new SmilesGenerator(SmiFlavor.Unique);
                String tmpUniqueSmiles1 = tmpSmilesGenerator.create(tmpMoleculePair[0]);
                String tmpUniqueSmiles2 = tmpSmilesGenerator.create(tmpMoleculePair[1]);
                tmpSimilarities[ComparisonFeature.EQUALITY.getFeatureNumber()] = tmpUniqueSmiles1.equals(tmpUniqueSmiles2) ? 1 : 0;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="EccentricConnectivityIndexDescriptor">
            if (this.consideredComparisonFeature[ComparisonFeature.ECCENTRIC_CONNECTIVITY_INDEX.getFeatureNumber()]) {
                EccentricConnectivityIndexDescriptor tmpEccentricConnectivityIndexDescriptor = new EccentricConnectivityIndexDescriptor();
                tmpEccentricConnectivityIndexDescriptor.initialise(DefaultChemObjectBuilder.getInstance());
                tmpSimilarities[ComparisonFeature.ECCENTRIC_CONNECTIVITY_INDEX.getFeatureNumber()] = Math.abs(
                    Double.parseDouble(tmpEccentricConnectivityIndexDescriptor.calculate(tmpMoleculePair[0]).getValue().toString()) - 
                    Double.parseDouble(tmpEccentricConnectivityIndexDescriptor.calculate(tmpMoleculePair[1]).getValue().toString())
                );
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="FMFDescriptor">
            if (this.consideredComparisonFeature[ComparisonFeature.FMF.getFeatureNumber()]) {
                FMFDescriptor tmpFMFDescriptor = new FMFDescriptor();
                tmpFMFDescriptor.initialise(DefaultChemObjectBuilder.getInstance());
                tmpSimilarities[ComparisonFeature.FMF.getFeatureNumber()] = Math.abs(
                    Double.parseDouble(tmpFMFDescriptor.calculate(tmpMoleculePair[0]).getValue().toString()) - 
                    Double.parseDouble(tmpFMFDescriptor.calculate(tmpMoleculePair[1]).getValue().toString())
                );
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="FractionalCSP3Descriptor">
            if (this.consideredComparisonFeature[ComparisonFeature.FCSP3.getFeatureNumber()]) {
                FractionalCSP3Descriptor tmpFractionalCSP3Descriptor = new FractionalCSP3Descriptor();
                tmpFractionalCSP3Descriptor.initialise(DefaultChemObjectBuilder.getInstance());
                tmpSimilarities[ComparisonFeature.FCSP3.getFeatureNumber()] = Math.abs(
                    Double.parseDouble(tmpFractionalCSP3Descriptor.calculate(tmpMoleculePair[0]).getValue().toString()) - 
                    Double.parseDouble(tmpFractionalCSP3Descriptor.calculate(tmpMoleculePair[1]).getValue().toString())
                );
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="FractionalPSADescriptor">
            if (this.consideredComparisonFeature[ComparisonFeature.FPSA.getFeatureNumber()]) {
                FractionalPSADescriptor tmpFractionalPSADescriptor = new FractionalPSADescriptor();
                tmpFractionalPSADescriptor.initialise(DefaultChemObjectBuilder.getInstance());
                tmpSimilarities[ComparisonFeature.FPSA.getFeatureNumber()] = Math.abs(
                    Double.parseDouble(tmpFractionalPSADescriptor.calculate(tmpMoleculePair[0]).getValue().toString()) - 
                    Double.parseDouble(tmpFractionalPSADescriptor.calculate(tmpMoleculePair[1]).getValue().toString())
                );
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="FragmentComplexityDescriptor">
            if (this.consideredComparisonFeature[ComparisonFeature.FRAGMENT_COMPLEXITY.getFeatureNumber()]) {
                FragmentComplexityDescriptor tmpFragmentComplexityDescriptor = new FragmentComplexityDescriptor();
                tmpFragmentComplexityDescriptor.initialise(DefaultChemObjectBuilder.getInstance());
                tmpSimilarities[ComparisonFeature.FRAGMENT_COMPLEXITY.getFeatureNumber()] = Math.abs(
                    Double.parseDouble(tmpFragmentComplexityDescriptor.calculate(tmpMoleculePair[0]).getValue().toString()) - 
                    Double.parseDouble(tmpFragmentComplexityDescriptor.calculate(tmpMoleculePair[1]).getValue().toString())
                );
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="HBondAcceptorCountDescriptor">
            if (this.consideredComparisonFeature[ComparisonFeature.H_BOND_ACCEPTOR_COUNT.getFeatureNumber()]) {
                HBondAcceptorCountDescriptor tmpHBondAcceptorCountDescriptor = new HBondAcceptorCountDescriptor();
                tmpHBondAcceptorCountDescriptor.initialise(DefaultChemObjectBuilder.getInstance());
                tmpHBondAcceptorCountDescriptor.setParameters(new Object[] {true});
                tmpSimilarities[ComparisonFeature.H_BOND_ACCEPTOR_COUNT.getFeatureNumber()] = Math.abs(
                    Double.parseDouble(tmpHBondAcceptorCountDescriptor.calculate(tmpMoleculePair[0]).getValue().toString()) - 
                    Double.parseDouble(tmpHBondAcceptorCountDescriptor.calculate(tmpMoleculePair[1]).getValue().toString())
                );
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="HBondDonorCountDescriptor">
            if (this.consideredComparisonFeature[ComparisonFeature.H_BOND_DONOR_COUNT.getFeatureNumber()]) {
                HBondDonorCountDescriptor tmpHBondDonorCountDescriptor = new HBondDonorCountDescriptor();
                tmpHBondDonorCountDescriptor.initialise(DefaultChemObjectBuilder.getInstance());
                tmpSimilarities[ComparisonFeature.H_BOND_DONOR_COUNT.getFeatureNumber()] = Math.abs(
                    Double.parseDouble(tmpHBondDonorCountDescriptor.calculate(tmpMoleculePair[0]).getValue().toString()) - 
                    Double.parseDouble(tmpHBondDonorCountDescriptor.calculate(tmpMoleculePair[1]).getValue().toString())
                );
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="JPlogPDescriptor">
            if (this.consideredComparisonFeature[ComparisonFeature.JP_LOGP.getFeatureNumber()]) {
                JPlogPDescriptor tmpJPlogPDescriptor = new JPlogPDescriptor();
                tmpJPlogPDescriptor.initialise(DefaultChemObjectBuilder.getInstance());
                tmpSimilarities[ComparisonFeature.JP_LOGP.getFeatureNumber()] = Math.abs(
                    Double.parseDouble(tmpJPlogPDescriptor.calculate(tmpMoleculePair[0]).getValue().toString()) -
                    Double.parseDouble(tmpJPlogPDescriptor.calculate(tmpMoleculePair[1]).getValue().toString())
                );
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="KappaShapeIndicesDescriptor">
            if (this.consideredComparisonFeature[ComparisonFeature.KAPPA_SHAPE_INDEX_1.getFeatureNumber()] ||
                this.consideredComparisonFeature[ComparisonFeature.KAPPA_SHAPE_INDEX_2.getFeatureNumber()] ||
                this.consideredComparisonFeature[ComparisonFeature.KAPPA_SHAPE_INDEX_3.getFeatureNumber()]) {
                KappaShapeIndicesDescriptor tmpKappaShapeIndicesDescriptor = new KappaShapeIndicesDescriptor();
                tmpKappaShapeIndicesDescriptor.initialise(DefaultChemObjectBuilder.getInstance());
                String[] tmpArray1 = tmpKappaShapeIndicesDescriptor.calculate(tmpMoleculePair[0]).getValue().toString().split(",");
                String[] tmpArray2 = tmpKappaShapeIndicesDescriptor.calculate(tmpMoleculePair[1]).getValue().toString().split(",");
                // First kappa shape index
                if (this.consideredComparisonFeature[ComparisonFeature.KAPPA_SHAPE_INDEX_1.getFeatureNumber()]) {
                    tmpSimilarities[ComparisonFeature.KAPPA_SHAPE_INDEX_1.getFeatureNumber()] = Math.abs(Double.parseDouble(tmpArray1[0]) - Double.parseDouble(tmpArray2[0]));
                }
                // Second kappa shape index
                if (this.consideredComparisonFeature[ComparisonFeature.KAPPA_SHAPE_INDEX_2.getFeatureNumber()]) {
                    tmpSimilarities[ComparisonFeature.KAPPA_SHAPE_INDEX_2.getFeatureNumber()] = Math.abs(Double.parseDouble(tmpArray1[1]) - Double.parseDouble(tmpArray2[1]));
                }
                // Third kappa shape index
                if (this.consideredComparisonFeature[ComparisonFeature.KAPPA_SHAPE_INDEX_3.getFeatureNumber()]) {
                    tmpSimilarities[ComparisonFeature.KAPPA_SHAPE_INDEX_3.getFeatureNumber()] = Math.abs(Double.parseDouble(tmpArray1[2]) - Double.parseDouble(tmpArray2[2]));
                }
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="LargestChainDescriptor">
            if (this.consideredComparisonFeature[ComparisonFeature.LARGEST_CHAIN.getFeatureNumber()]) {
                LargestChainDescriptor tmpLargestChainDescriptor = new LargestChainDescriptor();
                tmpLargestChainDescriptor.initialise(DefaultChemObjectBuilder.getInstance());
                // Doesn't need to check for rings because that is done in the preproccesing
                tmpLargestChainDescriptor.setParameters(new Object[] {false, false});
                tmpSimilarities[ComparisonFeature.LARGEST_CHAIN.getFeatureNumber()] = Math.abs(
                    Double.parseDouble(tmpLargestChainDescriptor.calculate(tmpMoleculePair[0]).getValue().toString()) -
                    Double.parseDouble(tmpLargestChainDescriptor.calculate(tmpMoleculePair[1]).getValue().toString())
                );
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="LargestPiSystemDescriptor">
            if (this.consideredComparisonFeature[ComparisonFeature.LARGEST_PI_SYSTEM.getFeatureNumber()]) {
                LargestPiSystemDescriptor tmpLargestPiSystemDescriptor = new LargestPiSystemDescriptor();
                tmpLargestPiSystemDescriptor.initialise(DefaultChemObjectBuilder.getInstance());
                // Doesn't need to check for aromaticity because that is done in the preproccesing
                tmpLargestPiSystemDescriptor.setParameters(new Object[] {false});
                tmpSimilarities[ComparisonFeature.LARGEST_PI_SYSTEM.getFeatureNumber()] = Math.abs(
                    Double.parseDouble(tmpLargestPiSystemDescriptor.calculate(tmpMoleculePair[0]).getValue().toString()) -
                    Double.parseDouble(tmpLargestPiSystemDescriptor.calculate(tmpMoleculePair[1]).getValue().toString())
                );
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="LongestAliphaticChainDescriptor">
            if (this.consideredComparisonFeature[ComparisonFeature.LONGEST_ALIPHATIC_CHAIN.getFeatureNumber()]) {
                LongestAliphaticChainDescriptor tmpLongestAliphaticChainDescriptor = new LongestAliphaticChainDescriptor();
                tmpLongestAliphaticChainDescriptor.initialise(DefaultChemObjectBuilder.getInstance());
                // Doesn't need to check for rings because that is done in the preproccesing
                tmpLongestAliphaticChainDescriptor.setParameters(new Object[] {false});
                tmpSimilarities[ComparisonFeature.LONGEST_ALIPHATIC_CHAIN.getFeatureNumber()] = Math.abs(
                    Double.parseDouble(tmpLongestAliphaticChainDescriptor.calculate(tmpMoleculePair[0]).getValue().toString()) -
                    Double.parseDouble(tmpLongestAliphaticChainDescriptor.calculate(tmpMoleculePair[1]).getValue().toString())
                );
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="MannholdLogPDescriptor">
            if (this.consideredComparisonFeature[ComparisonFeature.MANNHOLD_LOGP.getFeatureNumber()]) {
                MannholdLogPDescriptor tmpMannholdLogPDescriptor = new MannholdLogPDescriptor();
                tmpMannholdLogPDescriptor.initialise(DefaultChemObjectBuilder.getInstance());
                tmpSimilarities[ComparisonFeature.MANNHOLD_LOGP.getFeatureNumber()] = Math.abs(
                    Double.parseDouble(tmpMannholdLogPDescriptor.calculate(tmpMoleculePair[0]).getValue().toString()) - 
                    Double.parseDouble(tmpMannholdLogPDescriptor.calculate(tmpMoleculePair[1]).getValue().toString())
                );
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="PetitjeanNumberDescriptor">
            if (this.consideredComparisonFeature[ComparisonFeature.PETITJEAN_NUMBER.getFeatureNumber()]) {
                PetitjeanNumberDescriptor tmpPetitjeanNumberDescriptor = new PetitjeanNumberDescriptor();
                tmpPetitjeanNumberDescriptor.initialise(DefaultChemObjectBuilder.getInstance());
                tmpSimilarities[ComparisonFeature.PETITJEAN_NUMBER.getFeatureNumber()] = Math.abs(
                    Double.parseDouble(tmpPetitjeanNumberDescriptor.calculate(tmpMoleculePair[0]).getValue().toString()) - 
                    Double.parseDouble(tmpPetitjeanNumberDescriptor.calculate(tmpMoleculePair[1]).getValue().toString())
                );
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="PetitjeanShapeIndexDescriptor">
            if (this.consideredComparisonFeature[ComparisonFeature.PETITJEAN_SHAPE_INDEX.getFeatureNumber()]) {
                PetitjeanShapeIndexDescriptor tmpPetitjeanShapeIndexDescriptor = new PetitjeanShapeIndexDescriptor();
                tmpPetitjeanShapeIndexDescriptor.initialise(DefaultChemObjectBuilder.getInstance());
                String[] tmpArray1 = tmpPetitjeanShapeIndexDescriptor.calculate(tmpMoleculePair[0]).getValue().toString().split(",");
                String[] tmpArray2 = tmpPetitjeanShapeIndexDescriptor.calculate(tmpMoleculePair[1]).getValue().toString().split(",");
                tmpSimilarities[ComparisonFeature.PETITJEAN_SHAPE_INDEX.getFeatureNumber()] = Math.abs(Double.parseDouble(tmpArray1[0]) - Double.parseDouble(tmpArray2[0]));
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="RotatableBondsCountDescriptor">
            if (this.consideredComparisonFeature[ComparisonFeature.ROTATABLE_BOND_COUNT.getFeatureNumber()]) {
                RotatableBondsCountDescriptor tmpRotatableBondsCountDescriptor = new RotatableBondsCountDescriptor();
                tmpRotatableBondsCountDescriptor.initialise(DefaultChemObjectBuilder.getInstance());
                // Don't include terminal bonds and don't exclude amide bonds
                tmpRotatableBondsCountDescriptor.setParameters(new Object[] {false, false});
                tmpSimilarities[ComparisonFeature.ROTATABLE_BOND_COUNT.getFeatureNumber()] = Math.abs(
                    Double.parseDouble(tmpRotatableBondsCountDescriptor.calculate(tmpMoleculePair[0]).getValue().toString()) - 
                    Double.parseDouble(tmpRotatableBondsCountDescriptor.calculate(tmpMoleculePair[1]).getValue().toString())
                );
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="SmallRingDescriptor">
            if (this.consideredComparisonFeature[ComparisonFeature.ALL_SMALL_RINGS_COUNT.getFeatureNumber()] ||
                this.consideredComparisonFeature[ComparisonFeature.AROMATIC_RINGS_COUNT.getFeatureNumber()] ||
                this.consideredComparisonFeature[ComparisonFeature.SIZE_3_RINGS_COUNT.getFeatureNumber()] ||
                this.consideredComparisonFeature[ComparisonFeature.SIZE_4_RINGS_COUNT.getFeatureNumber()] ||
                this.consideredComparisonFeature[ComparisonFeature.SIZE_5_RINGS_COUNT.getFeatureNumber()] ||
                this.consideredComparisonFeature[ComparisonFeature.SIZE_6_RINGS_COUNT.getFeatureNumber()] ||
                this.consideredComparisonFeature[ComparisonFeature.SIZE_7_RINGS_COUNT.getFeatureNumber()] ||
                this.consideredComparisonFeature[ComparisonFeature.SIZE_8_RINGS_COUNT.getFeatureNumber()] ||
                this.consideredComparisonFeature[ComparisonFeature.SIZE_9_RINGS_COUNT.getFeatureNumber()]) {
                SmallRingDescriptor tmpSmallRingDescriptor = new SmallRingDescriptor();
                tmpSmallRingDescriptor.initialise(DefaultChemObjectBuilder.getInstance());
                double tmpRingCount1, tmpRingCount2;
                String[] tmpArray1 = tmpSmallRingDescriptor.calculate(tmpMoleculePair[0]).getValue().toString().split(",");
                String[] tmpArray2 = tmpSmallRingDescriptor.calculate(tmpMoleculePair[1]).getValue().toString().split(",");
                //All small rings count
                if (this.consideredComparisonFeature[ComparisonFeature.ALL_SMALL_RINGS_COUNT.getFeatureNumber()]) {
                    tmpRingCount1 = Double.parseDouble(tmpArray1[0]);
                    tmpRingCount2 = Double.parseDouble(tmpArray2[0]);
                    tmpSimilarities[ComparisonFeature.ALL_SMALL_RINGS_COUNT.getFeatureNumber()] = Math.abs(tmpRingCount1 - tmpRingCount2);
                }
                //Aromatic rings count
                if (this.consideredComparisonFeature[ComparisonFeature.AROMATIC_RINGS_COUNT.getFeatureNumber()]) {
                    tmpRingCount1 = Double.parseDouble(tmpArray1[1]);
                    tmpRingCount2 = Double.parseDouble(tmpArray2[1]);
                    tmpSimilarities[ComparisonFeature.AROMATIC_RINGS_COUNT.getFeatureNumber()] = Math.abs(tmpRingCount1 - tmpRingCount2);
                }
                //Size 3 rings count
                if (this.consideredComparisonFeature[ComparisonFeature.SIZE_3_RINGS_COUNT.getFeatureNumber()]) {
                    tmpRingCount1 = Double.parseDouble(tmpArray1[4]);
                    tmpRingCount2 = Double.parseDouble(tmpArray2[4]);
                    tmpSimilarities[ComparisonFeature.SIZE_3_RINGS_COUNT.getFeatureNumber()] = Math.abs(tmpRingCount1 - tmpRingCount2);
                }
                //Size 4 rings count
                if (this.consideredComparisonFeature[ComparisonFeature.SIZE_4_RINGS_COUNT.getFeatureNumber()]) {
                    tmpRingCount1 = Double.parseDouble(tmpArray1[5]);
                    tmpRingCount2 = Double.parseDouble(tmpArray2[5]);
                    tmpSimilarities[ComparisonFeature.SIZE_4_RINGS_COUNT.getFeatureNumber()] = Math.abs(tmpRingCount1 - tmpRingCount2);
                }
                //Size 5 rings count
                if (this.consideredComparisonFeature[ComparisonFeature.SIZE_5_RINGS_COUNT.getFeatureNumber()]) {
                    tmpRingCount1 = Double.parseDouble(tmpArray1[6]);
                    tmpRingCount2 = Double.parseDouble(tmpArray2[6]);
                    tmpSimilarities[ComparisonFeature.SIZE_5_RINGS_COUNT.getFeatureNumber()] = Math.abs(tmpRingCount1 - tmpRingCount2);
                }
                //Size 6 rings count
                if (this.consideredComparisonFeature[ComparisonFeature.SIZE_6_RINGS_COUNT.getFeatureNumber()]) {
                    tmpRingCount1 = Double.parseDouble(tmpArray1[7]);
                    tmpRingCount2 = Double.parseDouble(tmpArray2[7]);
                    tmpSimilarities[ComparisonFeature.SIZE_6_RINGS_COUNT.getFeatureNumber()] = Math.abs(tmpRingCount1 - tmpRingCount2);
                }
                //Size 7 rings count
                if (this.consideredComparisonFeature[ComparisonFeature.SIZE_7_RINGS_COUNT.getFeatureNumber()]) {
                    tmpRingCount1 = Double.parseDouble(tmpArray1[8]);
                    tmpRingCount2 = Double.parseDouble(tmpArray2[8]);
                    tmpSimilarities[ComparisonFeature.SIZE_7_RINGS_COUNT.getFeatureNumber()] = Math.abs(tmpRingCount1 - tmpRingCount2);
                }
                //Size 8 rings count
                if (this.consideredComparisonFeature[ComparisonFeature.SIZE_8_RINGS_COUNT.getFeatureNumber()]) {
                    tmpRingCount1 = Double.parseDouble(tmpArray1[9]);
                    tmpRingCount2 = Double.parseDouble(tmpArray2[9]);
                    tmpSimilarities[ComparisonFeature.SIZE_8_RINGS_COUNT.getFeatureNumber()] = Math.abs(tmpRingCount1 - tmpRingCount2);
                }
                //Size 9 rings count
                if (this.consideredComparisonFeature[ComparisonFeature.SIZE_9_RINGS_COUNT.getFeatureNumber()]) {
                    tmpRingCount1 = Double.parseDouble(tmpArray1[10]);
                    tmpRingCount2 = Double.parseDouble(tmpArray2[10]);
                    tmpSimilarities[ComparisonFeature.SIZE_9_RINGS_COUNT.getFeatureNumber()] = Math.abs(tmpRingCount1 - tmpRingCount2);
                }
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="SpiroAtomCountDescriptor">
            if (this.consideredComparisonFeature[ComparisonFeature.SPIRO_ATOM_COUNT.getFeatureNumber()]) {
                SpiroAtomCountDescriptor tmpSpiroAtomCountDescriptor = new SpiroAtomCountDescriptor();
                tmpSpiroAtomCountDescriptor.initialise(DefaultChemObjectBuilder.getInstance());
                tmpSimilarities[ComparisonFeature.SPIRO_ATOM_COUNT.getFeatureNumber()] = Math.abs(
                    Double.parseDouble(tmpSpiroAtomCountDescriptor.calculate(tmpMoleculePair[0]).getValue().toString()) - 
                    Double.parseDouble(tmpSpiroAtomCountDescriptor.calculate(tmpMoleculePair[1]).getValue().toString())
                );
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Tanimoto">
            // Basic
            IFingerprinter tmpFingerprinter;
            if (this.consideredComparisonFeature[ComparisonFeature.TANIMOTO_BASIC_FINGERPRINTER.getFeatureNumber()]) {
                tmpFingerprinter = new Fingerprinter();
                tmpSimilarities[ComparisonFeature.TANIMOTO_BASIC_FINGERPRINTER.getFeatureNumber()] = Tanimoto.calculate(
                    tmpFingerprinter.getBitFingerprint(tmpMoleculePair[0]),
                    tmpFingerprinter.getBitFingerprint(tmpMoleculePair[1])
                );
            }
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            // LINGO
            if (this.consideredComparisonFeature[ComparisonFeature.TANIMOTO_LINGO_FINGERPRINTER.getFeatureNumber()]) {
                tmpFingerprinter = new LingoFingerprinter();
                tmpSimilarities[ComparisonFeature.TANIMOTO_LINGO_FINGERPRINTER.getFeatureNumber()] = Tanimoto.calculate(
                    tmpFingerprinter.getBitFingerprint(tmpMoleculePair[0]),
                    tmpFingerprinter.getBitFingerprint(tmpMoleculePair[1])
                );
            }
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            // Extended
            if (this.consideredComparisonFeature[ComparisonFeature.TANIMOTO_EXTENDED_FINGERPRINTER.getFeatureNumber()]) {
                tmpFingerprinter = new ExtendedFingerprinter();
                tmpSimilarities[ComparisonFeature.TANIMOTO_EXTENDED_FINGERPRINTER.getFeatureNumber()] = Tanimoto.calculate(
                    tmpFingerprinter.getBitFingerprint(tmpMoleculePair[0]),
                    tmpFingerprinter.getBitFingerprint(tmpMoleculePair[1])
                );
            }
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            // E-State
            if (this.consideredComparisonFeature[ComparisonFeature.TANIMOTO_ESTATE_FINGERPRINTER.getFeatureNumber()]) {
                tmpFingerprinter = new EStateFingerprinter();
                tmpSimilarities[ComparisonFeature.TANIMOTO_ESTATE_FINGERPRINTER.getFeatureNumber()] = Tanimoto.calculate(
                    tmpFingerprinter.getBitFingerprint(tmpMoleculePair[0]),
                    tmpFingerprinter.getBitFingerprint(tmpMoleculePair[1])
                );
            }
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            // PubChem
            if (this.consideredComparisonFeature[ComparisonFeature.TANIMOTO_PUBCHEM_FINGERPRINTER.getFeatureNumber()]) {
                tmpFingerprinter = new PubchemFingerprinter(DefaultChemObjectBuilder.getInstance());
                tmpSimilarities[ComparisonFeature.TANIMOTO_PUBCHEM_FINGERPRINTER.getFeatureNumber()] = Tanimoto.calculate(
                    tmpFingerprinter.getBitFingerprint(tmpMoleculePair[0]),
                    tmpFingerprinter.getBitFingerprint(tmpMoleculePair[1])
                );
            }
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            // Shortest path
            if (this.consideredComparisonFeature[ComparisonFeature.TANIMOTO_SHORTEST_PATH_FINGERPRINTER.getFeatureNumber()]) {
                tmpFingerprinter = new ShortestPathFingerprinter();
                tmpSimilarities[ComparisonFeature.TANIMOTO_SHORTEST_PATH_FINGERPRINTER.getFeatureNumber()] = Tanimoto.calculate(
                    tmpFingerprinter.getBitFingerprint(tmpMoleculePair[0]),
                    tmpFingerprinter.getBitFingerprint(tmpMoleculePair[1])
                );
            }
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            // Substructure
            if (this.consideredComparisonFeature[ComparisonFeature.TANIMOTO_SUBSTRUCTURE_FINGERPRINTER.getFeatureNumber()]) {
                tmpFingerprinter = new SubstructureFingerprinter();
                tmpSimilarities[ComparisonFeature.TANIMOTO_SUBSTRUCTURE_FINGERPRINTER.getFeatureNumber()] = Tanimoto.calculate(
                    tmpFingerprinter.getBitFingerprint(tmpMoleculePair[0]),
                    tmpFingerprinter.getBitFingerprint(tmpMoleculePair[1])
                );
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="TPSADescriptor">
            if (this.consideredComparisonFeature[ComparisonFeature.TPSA.getFeatureNumber()]) {
                TPSADescriptor tmpTPSADescriptor = new TPSADescriptor();
                tmpTPSADescriptor.initialise(DefaultChemObjectBuilder.getInstance());
                // Doesn't need to check for aromaticity because that is done in the preproccesing
                tmpTPSADescriptor.setParameters(new Object[] {false});
                tmpSimilarities[ComparisonFeature.TPSA.getFeatureNumber()] = Math.abs(
                    Double.parseDouble(tmpTPSADescriptor.calculate(tmpMoleculePair[0]).getValue().toString()) - 
                    Double.parseDouble(tmpTPSADescriptor.calculate(tmpMoleculePair[1]).getValue().toString())
                );
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="VABCDescriptor">
            if (this.consideredComparisonFeature[ComparisonFeature.VABC.getFeatureNumber()]) {
                VABCDescriptor tmpVABCDescriptor = new VABCDescriptor();
                tmpVABCDescriptor.initialise(DefaultChemObjectBuilder.getInstance());
                tmpSimilarities[ComparisonFeature.VABC.getFeatureNumber()] = Math.abs(
                    Double.parseDouble(tmpVABCDescriptor.calculate(tmpMoleculePair[0]).getValue().toString()) - 
                    Double.parseDouble(tmpVABCDescriptor.calculate(tmpMoleculePair[1]).getValue().toString())
                );
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="VAdjMaDescriptor">
            if (this.consideredComparisonFeature[ComparisonFeature.V_ADJ_MA.getFeatureNumber()]) {
                VAdjMaDescriptor tmpVAdjMaDescriptor = new VAdjMaDescriptor();
                tmpVAdjMaDescriptor.initialise(DefaultChemObjectBuilder.getInstance());
                tmpSimilarities[ComparisonFeature.V_ADJ_MA.getFeatureNumber()] = Math.abs(
                    Double.parseDouble(tmpVAdjMaDescriptor.calculate(tmpMoleculePair[0]).getValue().toString()) - 
                    Double.parseDouble(tmpVAdjMaDescriptor.calculate(tmpMoleculePair[1]).getValue().toString())
                );
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="WeightDescriptor">
            if (this.consideredComparisonFeature[ComparisonFeature.MOLECULAR_WEIGHT.getFeatureNumber()]) {
                WeightDescriptor tmpWeightDescriptor = new WeightDescriptor();
                tmpWeightDescriptor.initialise(DefaultChemObjectBuilder.getInstance());
                // Calculate the weight of all atoms
                tmpWeightDescriptor.setParameters(new Object[] {"*"});
                tmpSimilarities[ComparisonFeature.MOLECULAR_WEIGHT.getFeatureNumber()] = Math.abs(
                    Double.parseDouble(tmpWeightDescriptor.calculate(tmpMoleculePair[0]).getValue().toString()) - 
                    Double.parseDouble(tmpWeightDescriptor.calculate(tmpMoleculePair[1]).getValue().toString())
                );
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="WeightedPathDescriptor">
            if (this.consideredComparisonFeature[ComparisonFeature.WEIGHTED_PATH_1.getFeatureNumber()]) {
                WeightedPathDescriptor tmpWeightedPathDescriptor = new WeightedPathDescriptor();
                tmpWeightedPathDescriptor.initialise(DefaultChemObjectBuilder.getInstance());
                String[] tmpArray1 = tmpWeightedPathDescriptor.calculate(tmpMoleculePair[0]).getValue().toString().split(",");
                String[] tmpArray2 = tmpWeightedPathDescriptor.calculate(tmpMoleculePair[1]).getValue().toString().split(",");
                tmpSimilarities[ComparisonFeature.WEIGHTED_PATH_1.getFeatureNumber()] = Math.abs(Double.parseDouble(tmpArray1[0]) - Double.parseDouble(tmpArray2[0]));
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="WienerNumbersDescriptor">
            if (this.consideredComparisonFeature[ComparisonFeature.WIENER_PATH.getFeatureNumber()] ||
                this.consideredComparisonFeature[ComparisonFeature.WIENER_POLARITY.getFeatureNumber()]) {
                WienerNumbersDescriptor tmpWienerNumbersDescriptor = new WienerNumbersDescriptor();
                tmpWienerNumbersDescriptor.initialise(DefaultChemObjectBuilder.getInstance());
                String[] tmpArray1 = tmpWienerNumbersDescriptor.calculate(tmpMoleculePair[0]).getValue().toString().split(",");
                String[] tmpArray2 = tmpWienerNumbersDescriptor.calculate(tmpMoleculePair[1]).getValue().toString().split(",");
                // Wiener path number
                if (this.consideredComparisonFeature[ComparisonFeature.WIENER_PATH.getFeatureNumber()]) {
                    tmpSimilarities[ComparisonFeature.WIENER_PATH.getFeatureNumber()] = Math.abs(Double.parseDouble(tmpArray1[0]) - Double.parseDouble(tmpArray2[0]));
                }
                // Wiener polarity number
                if (this.consideredComparisonFeature[ComparisonFeature.WIENER_POLARITY.getFeatureNumber()]) {
                    tmpSimilarities[ComparisonFeature.WIENER_POLARITY.getFeatureNumber()] = Math.abs(Double.parseDouble(tmpArray1[1]) - Double.parseDouble(tmpArray2[1]));
                }
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="XLogPDescriptor">
            if (this.consideredComparisonFeature[ComparisonFeature.X_LOGP.getFeatureNumber()]) {
                XLogPDescriptor tmpXLogPDescriptor = new XLogPDescriptor();
                tmpXLogPDescriptor.initialise(DefaultChemObjectBuilder.getInstance());
                // Aromaticity is already checked in preprocessing; use the salicyl acid correction factor
                tmpXLogPDescriptor.setParameters(new Object[] {false, true});
                tmpSimilarities[ComparisonFeature.X_LOGP.getFeatureNumber()] = Math.abs(
                    Double.parseDouble(tmpXLogPDescriptor.calculate(tmpMoleculePair[0]).getValue().toString()) - 
                    Double.parseDouble(tmpXLogPDescriptor.calculate(tmpMoleculePair[1]).getValue().toString())
                );
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="ZagrebIndexDescriptor">
            if (this.consideredComparisonFeature[ComparisonFeature.ZAGREB_INDEX.getFeatureNumber()]) {
                ZagrebIndexDescriptor tmpZagrebIndexDescriptor = new ZagrebIndexDescriptor();
                tmpZagrebIndexDescriptor.initialise(DefaultChemObjectBuilder.getInstance());
                tmpSimilarities[ComparisonFeature.ZAGREB_INDEX.getFeatureNumber()] = Math.abs(
                    Double.parseDouble(tmpZagrebIndexDescriptor.calculate(tmpMoleculePair[0]).getValue().toString()) - 
                    Double.parseDouble(tmpZagrebIndexDescriptor.calculate(tmpMoleculePair[1]).getValue().toString())
                );
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Convert the molecules into the SMILES format">
            if (this.type1 != InputType.SMILES) {
                StringWriter tmpWriter = new StringWriter();
                SMILESWriter tmpSmilesWriter = new SMILESWriter(tmpWriter);
                tmpSmilesWriter.writeAtomContainer(tmpMoleculePair[0]);
                this.molecule1 = tmpWriter.toString();
            }
            if (this.type2 != InputType.SMILES) {
                StringWriter tmpWriter = new StringWriter();
                SMILESWriter tmpSmilesWriter = new SMILESWriter(tmpWriter);
                tmpSmilesWriter.writeAtomContainer(tmpMoleculePair[1]);
                this.molecule2 = tmpWriter.toString();
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Check if canceled">
            if (this.isCancelled) {
                this.result = new ComparisonResult("The comparison was cancelled", this.identifier);
                this.isFinished = true;
                return null;
            }
            //</editor-fold>
            this.result = new ComparisonResult(tmpSimilarities, this.molecule1, this.molecule2, this.identifier);
            this.isFinished = true;
            this.propertyChangeSupport.firePropertyChange("Task completed", false, true);
            return null;
        } catch (Exception ex) {
            this.result = new ComparisonResult(ex.toString(), this.identifier);
            this.isFinished = true;
            this.propertyChangeSupport.firePropertyChange("Task completed", true, false);
            return null;
        }
    }
    
    /**
     * Cancel the execution of this task
     */
    public void cancel(){
        this.isCancelled = true;
    }
    
    /**
     * True if this task finished its execution in any way, false otherwise
     * 
     * @return true if this task finished its execution in any way, 
     * false otherwise
     */
    public boolean isFinished() {
        return this.isFinished;
    }
    
    /**
     * True if this task is still working, false otherwise
     * 
     * @return true if this task is still working, false otherwise
     */
    public boolean isWorking() {
        return this.isStarted && !this.isFinished;
    }
    
    /**
     * True is this task has been started, false otherwise
     * 
     * @return true is this task has been started, false otherwise
     */
    public boolean isStarted() {
        return this.isStarted;
    }
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="- Public properties">
    /**
     * Get the result of the execution of this task
     * 
     * @return the result of the execution of this task
     */
    public ComparisonResult getResult() {
        return this.result;
    }
    
    /**
     * Get the identifier of this task
     * 
     * @return the identifier of this task
     */
    public int getIdentifier() {
        return this.identifier;
    }
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="- Property change related methods">
    /**
     * Add a PropertyChangeListener that listens for changes from this
     * CompareAtomContainerPairTask
     *
     * @param tmpListener a listener to be added
     */
    public void addPropertyChangeListener(PropertyChangeListener tmpListener) {
        this.propertyChangeSupport.addPropertyChangeListener(tmpListener);
    }
    
    /**
     * Remove a PropertyChangeListener
     *
     * @param tmpListener a listener to be removed
     */
    public void removePropertyChangeListener(PropertyChangeListener tmpListener) {
        this.propertyChangeSupport.removePropertyChangeListener(tmpListener);
    }
    //</editor-fold>
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Protected methods">
    /**
     * Cancel the execution of this task and call Object.finialize()
     */
    @Override
    @SuppressWarnings("deprecation")
    protected void finalize() throws Throwable {
        this.cancel();
        super.finalize();
    }
    //</editor-fold>
    
}
