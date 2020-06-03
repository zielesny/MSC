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
import org.openscience.cdk.qsar.IMolecularDescriptor;
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
 * This abstract class provides utility methods for the comparison of
 * molecule pairs
 *
 * @author Jan-Mathis Hein
 */
public abstract class ComparisonUtility {
    
    //<editor-fold defaultstate="collapsed" desc="Private class variables">
    /**
     * A molecular descriptor that calculates the number of acidic groups 
     */
    private static final AcidicGroupCountDescriptor ACIDIC_GROUP_COUNT_DESCRIPTOR = new AcidicGroupCountDescriptor();
    
    /**
     * Calculates the total number of rings in a molecule
     */
    private static final AllRingsFinder ALL_RINGS_FINDER = new AllRingsFinder();
    
    /**
     * A molecular descriptor that calculates the number of aromatic atoms
     */
    private static final AromaticAtomsCountDescriptor AROMATIC_ATOMS_COUNTS_DESCRIPTOR = new AromaticAtomsCountDescriptor();
    
    /**
     * A molecular descriptor that calculates the number of aromatic bonds
     */
    private static final AromaticBondsCountDescriptor AROMATIC_BONDS_COUNT_DESCRIPTOR = new AromaticBondsCountDescriptor();
    
    /**
     * Used to set the aromaticity flags of atoms in molecules
     */
    private static final Aromaticity AROMATICITY = new Aromaticity(ElectronDonation.cdk(), Cycles.or(Cycles.all(), Cycles.cdkAromaticSet()));
    
    /**
     * A molecular descriptor that calculates the number of atoms
     */
    private static final AtomCountDescriptor ATOM_COUNT_DESCRIPTOR = new AtomCountDescriptor();
    
    /**
     * A molecular descriptor that calculates the atomic polarizability
     */
    private static final APolDescriptor A_POL_DESCRIPTOR = new APolDescriptor();
    
    /**
     * A molecular descriptor that calculates the autocorrelation of a 
     * topological structure, where the weight is equal to the charges.
     */
    private static final AutocorrelationDescriptorCharge AUTOCORRELATION_DESCRIPTOR_CHARGE = new AutocorrelationDescriptorCharge();
    
    /**
     * A molecular descriptor that calculates the autocorrelation of a 
     * topological structure, where the weight is equal to the mass.
     */
    private static final AutocorrelationDescriptorMass AUTOCORRELATION_DESCRIPTOR_MASS = new AutocorrelationDescriptorMass();
    
    /**
     * A molecular descriptor that calculates the autocorrelation of a 
     * topological structure, where the weight is equal to the polarizability.
     */
    private static final AutocorrelationDescriptorPolarizability AUTOCORRELATION_DESCRIPTOR_POLARIZABILITY = new AutocorrelationDescriptorPolarizability();
    
    /**
     * A molecular descriptor that calculates the number of basic groups
     */
    private static final BasicGroupCountDescriptor BASCIS_GROUP_COUNT_DESCRIPTOR = new BasicGroupCountDescriptor();
    
    /**
     * A molecular descriptor that calculates the number of bonds
     */
    private static final BondCountDescriptor BOND_COUNT_DESCRIPTOR = new BondCountDescriptor();
    
    /**
     * A molecular descriptor that calculates the bond polarizability
     */
    private static final BPolDescriptor B_POL_DESCRIPTOR = new BPolDescriptor();
    
    /**
     * A molecular descriptor that calculates the number of different carbon 
     * types
     */
    private static final CarbonTypesDescriptor CARBON_TYPES_DESCRIPTOR = new CarbonTypesDescriptor();
    
    /**
     * Matches atom types to atoms
     */
    private static final CDKAtomTypeMatcher CDK_ATOM_TYPE_MATCHER = CDKAtomTypeMatcher.getInstance(DefaultChemObjectBuilder.getInstance());
    
    /**
     * Adds implicit hydrogens to a molecule
     */
    private static final CDKHydrogenAdder CDK_HYDROGEN_ADDER = CDKHydrogenAdder.getInstance(DefaultChemObjectBuilder.getInstance());
    
    /**
     * A molecular descriptor that calculates the eccentric connectivity
     */
    private static final EccentricConnectivityIndexDescriptor ECCENTRIC_CONNECTIVITY_DESCRIPTOR = new EccentricConnectivityIndexDescriptor();
    
    /**
     * A molecular descriptor that calculates the fraction of the molecular 
     * framework
     */
    private static final FMFDescriptor FMF_DESCRIPTOR = new FMFDescriptor();
    
    /**
     * A molecular descriptor that calculates the fraction of CSP3 atoms
     */
    private static final FractionalCSP3Descriptor FRACTIONAL_CSP3_DESCRIPTOR = new FractionalCSP3Descriptor();
    
    /**
     * A molecular descriptor that calculates the fraction of the PSA
     */
    private static final FractionalPSADescriptor FRACTIONAL_PSA_DESCRIPTOR = new FractionalPSADescriptor();
    
    /**
     * A molecular descriptor that calculates the fragment complexity
     */
    private static final FragmentComplexityDescriptor FRAGMENT_COMPLEXITY_DESCRIPTOR = new FragmentComplexityDescriptor();
    
    /**
     * A molecular descriptor that calculates the number of H-bond acceptors
     */
    private static final HBondAcceptorCountDescriptor HBOND_ACCEPTOR_COUNT_DESCRIPTOR = new HBondAcceptorCountDescriptor();
    
    /**
     * A molecular descriptor that calculates the number of H-bond donors
     */
    private static final HBondDonorCountDescriptor HBOND_DONOR_COUNT_DESCRIPTOR = new HBondDonorCountDescriptor();
    
    /**
     * Generates a basic fingerprint for a molecule
     */
    private static final IFingerprinter BASIC_FINGERPRINTER = new Fingerprinter();
    
    /**
     * Generates a LINGO fingerprint for a molecule
     */
    private static final IFingerprinter LINGO_FINGERPRINTER = new LingoFingerprinter();
    
    /**
     * Generates an extended fingerprint for a molecule
     */
    private static final IFingerprinter EXTENDED_FINGERPRINTER = new ExtendedFingerprinter();
    
    /**
     * Generates a E-State fingerprint for a molecule
     */
    private static final IFingerprinter ESTATE_FINGERPRINTER = new EStateFingerprinter();
    
    /**
     * Generates a PubChem fingerprint for a molecule
     */
    private static final IFingerprinter PUBCHEM_FINGERPRINTER = new PubchemFingerprinter(DefaultChemObjectBuilder.getInstance());
    
    /**
     * Generates a shortest path fingerprint for a molecule
     */
    private static final IFingerprinter SHORTEST_PATH_FINGERPRINTER = new ShortestPathFingerprinter();
    
    /**
     * Generates a fingerprint for a molecule based on a given substructure set
     */
    private static final IFingerprinter SUBSTRUCTURE_FINGERPRINTER = new SubstructureFingerprinter();
    
    /**
     * A molecular descriptor that calculates the JP-logP value
     */
    private static final JPlogPDescriptor JP_LOGP_DESCRIPTOR = new JPlogPDescriptor();
    
    /**
     * A molecular descriptor that calculates the kappa shape indices
     */
    private static final KappaShapeIndicesDescriptor KAPPA_SHAPE_INDICES_DESCRIPTOR = new KappaShapeIndicesDescriptor();
    
    /**
     * A molecular descriptor that calculates the length of the largest chain
     */
    private static final LargestChainDescriptor LARGEST_CHAIN_DESCRIPTOR = new LargestChainDescriptor();
    
    /**
     * A molecular descriptor that calculates the size of the largest pi system
     */
    private static final LargestPiSystemDescriptor LARGEST_PI_SYSTEM_DESCRIPTOR = new LargestPiSystemDescriptor();
    
    /**
     * A molecular descriptor that calculates the lenght of the longest 
     * aliphatic chain
     */
    private static final LongestAliphaticChainDescriptor LONGEST_ALIPHATIC_CHAIN_DESCRIPTOR = new LongestAliphaticChainDescriptor();
    
    /**
     * A molecular descriptor that calculates the Mannhold-logP value
     */
    private static final MannholdLogPDescriptor MANNHOLD_LOG_P_DESCRIPTOR = new MannholdLogPDescriptor();
    
    /**
     * A molecular descriptor that calculates the Petitjean number
     */
    private static final PetitjeanNumberDescriptor PETITJEAN_NUMBER_DESCRIPTOR = new PetitjeanNumberDescriptor();
    
    /**
     * A molecular descriptor that calculates the Petitjean shape index
     */
    private static final PetitjeanShapeIndexDescriptor PETITJEAN_SHAPE_INDEX_DESCRIPTOR = new PetitjeanShapeIndexDescriptor();
    
    /**
     * A molecular descriptor that calculates the number of rotatable bonds
     */
    private static final RotatableBondsCountDescriptor ROTATABLE_BOND_COUNT = new RotatableBondsCountDescriptor();
    
    /**
     * A molecular descriptor that calculates the number of small rings
     */
    private static final SmallRingDescriptor SMALL_RING_DESCRIPTOR = new SmallRingDescriptor();
    
    /**
     * Generates unique and canoncical SMILES
     */
    private static final SmilesGenerator SMILES_GENERATOR = new SmilesGenerator(SmiFlavor.Unique);
    
    /**
     * Parses SMILES to IAtomContainers
     */
    private static final SmilesParser SMILES_PARSER = new SmilesParser(SilentChemObjectBuilder.getInstance());
    
    /**
     * A molecular descriptor that calculates the number of spiro atoms
     */
    private static final SpiroAtomCountDescriptor SPIRO_ATOM_COUNT_DESCRIPTOR = new SpiroAtomCountDescriptor();
    
    /**
     * A molecular descriptor that calculates the TPSA
     */
    private static final TPSADescriptor TPSA_DESCRIPTOR = new TPSADescriptor();
    
    /**
     * A molecular descriptor that calculates the VdW volume
     */
    private static final VABCDescriptor VABC_DESCRIPTOR = new VABCDescriptor();
    
    /**
     * A molecular descriptor that calculates the vertex adjacency
     */
    private static final VAdjMaDescriptor V_ADJ_MA_DESCRIPTOR = new VAdjMaDescriptor();
    
    /**
     * A molecular descriptor that calculates the molecular weight
     */
    private static final WeightDescriptor WEIGHT_DESCRIPTOR = new WeightDescriptor();
    
    /**
     * A molecular descriptor that calculates the weighted path
     */
    private static final WeightedPathDescriptor WEIGHTER_PATH_DESCRIPTOR = new WeightedPathDescriptor();
    
    /**
     * A molecular descriptor that calculates the Wiener numbers
     */
    private static final WienerNumbersDescriptor WIENER_NUMBERS_DESCRIPTOR = new WienerNumbersDescriptor();
    
    /**
     * A molecular descriptor that calculates the X-logP value
     */
    private static final XLogPDescriptor X_LOGP_DESCRIPTOR = new XLogPDescriptor();
    
    /**
     * A molecular descriptor that calculates the Zagreb index
     */
    private static final ZagrebIndexDescriptor ZAGREB_INDEY_DESCRIPTOR = new ZagrebIndexDescriptor();
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Static initialization block">
    static {
        ComparisonUtility.ACIDIC_GROUP_COUNT_DESCRIPTOR.initialise(DefaultChemObjectBuilder.getInstance());
        AllRingsFinder.usingThreshold(AllRingsFinder.Threshold.PubChem_99);
        ComparisonUtility.A_POL_DESCRIPTOR.initialise(DefaultChemObjectBuilder.getInstance());
        ComparisonUtility.AROMATIC_ATOMS_COUNTS_DESCRIPTOR.initialise(DefaultChemObjectBuilder.getInstance());
        ComparisonUtility.AROMATIC_BONDS_COUNT_DESCRIPTOR.initialise(DefaultChemObjectBuilder.getInstance());
        ComparisonUtility.ATOM_COUNT_DESCRIPTOR.initialise(DefaultChemObjectBuilder.getInstance());
        ComparisonUtility.AUTOCORRELATION_DESCRIPTOR_CHARGE.initialise(DefaultChemObjectBuilder.getInstance());
        ComparisonUtility.AUTOCORRELATION_DESCRIPTOR_MASS.initialise(DefaultChemObjectBuilder.getInstance());
        ComparisonUtility.AUTOCORRELATION_DESCRIPTOR_POLARIZABILITY.initialise(DefaultChemObjectBuilder.getInstance());
        ComparisonUtility.BASCIS_GROUP_COUNT_DESCRIPTOR.initialise(DefaultChemObjectBuilder.getInstance());
        ComparisonUtility.BOND_COUNT_DESCRIPTOR.initialise(DefaultChemObjectBuilder.getInstance());
        ComparisonUtility.B_POL_DESCRIPTOR.initialise(DefaultChemObjectBuilder.getInstance());
        ComparisonUtility.CARBON_TYPES_DESCRIPTOR.initialise(DefaultChemObjectBuilder.getInstance());
        ComparisonUtility.ECCENTRIC_CONNECTIVITY_DESCRIPTOR.initialise(DefaultChemObjectBuilder.getInstance());
        ComparisonUtility.FMF_DESCRIPTOR.initialise(DefaultChemObjectBuilder.getInstance());
        ComparisonUtility.FRACTIONAL_CSP3_DESCRIPTOR.initialise(DefaultChemObjectBuilder.getInstance());
        ComparisonUtility.FRAGMENT_COMPLEXITY_DESCRIPTOR.initialise(DefaultChemObjectBuilder.getInstance());
        ComparisonUtility.HBOND_ACCEPTOR_COUNT_DESCRIPTOR.initialise(DefaultChemObjectBuilder.getInstance());
        ComparisonUtility.HBOND_DONOR_COUNT_DESCRIPTOR.initialise(DefaultChemObjectBuilder.getInstance());
        ComparisonUtility.JP_LOGP_DESCRIPTOR.initialise(DefaultChemObjectBuilder.getInstance());
        ComparisonUtility.KAPPA_SHAPE_INDICES_DESCRIPTOR.initialise(DefaultChemObjectBuilder.getInstance());
        ComparisonUtility.LARGEST_CHAIN_DESCRIPTOR.initialise(DefaultChemObjectBuilder.getInstance());
        ComparisonUtility.LARGEST_PI_SYSTEM_DESCRIPTOR.initialise(DefaultChemObjectBuilder.getInstance());
        ComparisonUtility.LONGEST_ALIPHATIC_CHAIN_DESCRIPTOR.initialise(DefaultChemObjectBuilder.getInstance());
        ComparisonUtility.MANNHOLD_LOG_P_DESCRIPTOR.initialise(DefaultChemObjectBuilder.getInstance());
        ComparisonUtility.PETITJEAN_NUMBER_DESCRIPTOR.initialise(DefaultChemObjectBuilder.getInstance());
        ComparisonUtility.PETITJEAN_SHAPE_INDEX_DESCRIPTOR.initialise(DefaultChemObjectBuilder.getInstance());
        ComparisonUtility.ROTATABLE_BOND_COUNT.initialise(DefaultChemObjectBuilder.getInstance());
        ComparisonUtility.SMALL_RING_DESCRIPTOR.initialise(DefaultChemObjectBuilder.getInstance());
        ComparisonUtility.SPIRO_ATOM_COUNT_DESCRIPTOR.initialise(DefaultChemObjectBuilder.getInstance());
        ComparisonUtility.TPSA_DESCRIPTOR.initialise(DefaultChemObjectBuilder.getInstance());
        ComparisonUtility.VABC_DESCRIPTOR.initialise(DefaultChemObjectBuilder.getInstance());
        ComparisonUtility.V_ADJ_MA_DESCRIPTOR.initialise(DefaultChemObjectBuilder.getInstance());
        ComparisonUtility.WEIGHT_DESCRIPTOR.initialise(DefaultChemObjectBuilder.getInstance());
        ComparisonUtility.WEIGHTER_PATH_DESCRIPTOR.initialise(DefaultChemObjectBuilder.getInstance());
        ComparisonUtility.WIENER_NUMBERS_DESCRIPTOR.initialise(DefaultChemObjectBuilder.getInstance());
        ComparisonUtility.X_LOGP_DESCRIPTOR.initialise(DefaultChemObjectBuilder.getInstance());
        ComparisonUtility.ZAGREB_INDEY_DESCRIPTOR.initialise(DefaultChemObjectBuilder.getInstance());
    }
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Public methods">
    /**
     * Compare the given molecule based on the specified ComparisonFeatures and 
     * return an array containing the individual values 
     * (if available, else return NaN) and the resulting similarity for every 
     * ComparisonFeature
     * 
     * @param tmpCalculateFeature specifies the ComparisonFeatures which will be
     * calculated
     * @param tmpSmiles1 the first molecule in SMILES form
     * @param tmpSmiles2 the second molecule in SMILES form
     * @return an array where each element is an array of the form 
     * {firstComparisonFeatureValue, secondComparisonFeatureValue, 
     * resultingSimilarity}. The index of the element corresponds to the 
     * featureNumber of the corresponding ComparisonFeature. If there are no 
     * individual values available for the molecules, NaN is returned instead.
     * @throws org.openscience.cdk.exception.CDKException if there is something 
     * wrong with the molecules
     */
    public static synchronized double[][] compareMoleculePair(boolean[] tmpCalculateFeature, String tmpSmiles1, String tmpSmiles2) throws CDKException {
        double[][] tmpResult = new double[tmpCalculateFeature.length][3];
        //<editor-fold defaultstate="collapsed" desc="Preprocess molecules">
        IAtomContainer tmpMolecule1 = ComparisonUtility.SMILES_PARSER.parseSmiles(tmpSmiles1);
        IAtomContainer tmpMolecule2 = ComparisonUtility.SMILES_PARSER.parseSmiles(tmpSmiles2);
        RingSearch tmpRingSearch = new RingSearch(tmpMolecule1);
        for (IAtom tmpAtom : tmpMolecule1.atoms()) {
            tmpAtom.setIsInRing(tmpRingSearch.cyclic(tmpAtom));
            IAtomType tmpType = ComparisonUtility.CDK_ATOM_TYPE_MATCHER.findMatchingAtomType(tmpMolecule1, tmpAtom);
            AtomTypeManipulator.configure(tmpAtom, tmpType);
        }
        ComparisonUtility.CDK_HYDROGEN_ADDER.addImplicitHydrogens(tmpMolecule1);
        AtomContainerManipulator.convertImplicitToExplicitHydrogens(tmpMolecule1);
        // Set aromaticity flags
        ComparisonUtility.AROMATICITY.apply(tmpMolecule1);
        tmpRingSearch = new RingSearch(tmpMolecule2);
        for (IAtom tmpAtom : tmpMolecule2.atoms()) {
            tmpAtom.setIsInRing(tmpRingSearch.cyclic(tmpAtom));
            IAtomType tmpType = ComparisonUtility.CDK_ATOM_TYPE_MATCHER.findMatchingAtomType(tmpMolecule2, tmpAtom);
            AtomTypeManipulator.configure(tmpAtom, tmpType);
        }
        ComparisonUtility.CDK_HYDROGEN_ADDER.addImplicitHydrogens(tmpMolecule2);
        AtomContainerManipulator.convertImplicitToExplicitHydrogens(tmpMolecule2);
        // Set aromaticity flags
        ComparisonUtility.AROMATICITY.apply(tmpMolecule2);
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="AcidicGroupCountDescriptor">
        if (tmpCalculateFeature[ComparisonFeature.ACIDIC_GROUP_COUNT.getFeatureNumber()]) {
            tmpResult[ComparisonFeature.ACIDIC_GROUP_COUNT.getFeatureNumber()] = calculateDescriptorDifference(
                ComparisonUtility.ACIDIC_GROUP_COUNT_DESCRIPTOR, tmpMolecule1, tmpMolecule2
            );
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="AllRingsCount">
        if (tmpCalculateFeature[ComparisonFeature.ALL_RINGS_COUNT.getFeatureNumber()]) {
            try {
                int tmpRingCount1 = ComparisonUtility.ALL_RINGS_FINDER.findAllRings(tmpMolecule1).getAtomContainerCount();
                int tmpRingCount2 = ComparisonUtility.ALL_RINGS_FINDER.findAllRings(tmpMolecule2).getAtomContainerCount();
                tmpResult[ComparisonFeature.ALL_RINGS_COUNT.getFeatureNumber()] = new double[] {tmpRingCount1, tmpRingCount2, Math.abs(tmpRingCount1 - tmpRingCount2)};
            } catch (CDKException CDKException) {
                tmpResult[ComparisonFeature.ALL_RINGS_COUNT.getFeatureNumber()] = new double[] {Double.NaN, Double.NaN, Double.NaN};
            }
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="ALOGPDescriptor">
        if (tmpCalculateFeature[ComparisonFeature.A_LOGP.getFeatureNumber()] ||
            tmpCalculateFeature[ComparisonFeature.A_LOGP_2.getFeatureNumber()] ||
            tmpCalculateFeature[ComparisonFeature.MOLAR_REFRACTIVITY.getFeatureNumber()]) {
            ALOGPDescriptor tmpALOGPDescriptor = new ALOGPDescriptor();
            tmpALOGPDescriptor.initialise(DefaultChemObjectBuilder.getInstance());
            String[] tmpArray1 = tmpALOGPDescriptor.calculate(tmpMolecule1).getValue().toString().split(",");
            String[] tmpArray2 = tmpALOGPDescriptor.calculate(tmpMolecule2).getValue().toString().split(",");
            // Ghose-Grippen logP
            if (tmpCalculateFeature[ComparisonFeature.A_LOGP.getFeatureNumber()]) {
                tmpResult[ComparisonFeature.A_LOGP.getFeatureNumber()] = new double[] {Double.parseDouble(tmpArray1[0]), Double.parseDouble(tmpArray2[0]), Math.abs(Double.parseDouble(tmpArray1[0]) - Double.parseDouble(tmpArray2[0]))};
            }
            // Ghose-Grippen logP 2
            if (tmpCalculateFeature[ComparisonFeature.A_LOGP_2.getFeatureNumber()]) {
                tmpResult[ComparisonFeature.A_LOGP_2.getFeatureNumber()] = new double[] {Double.parseDouble(tmpArray1[1]), Double.parseDouble(tmpArray2[1]), Math.abs(Double.parseDouble(tmpArray1[1]) - Double.parseDouble(tmpArray2[1]))};
            }
            // Molar refractivity
            if (tmpCalculateFeature[ComparisonFeature.MOLAR_REFRACTIVITY.getFeatureNumber()]) {
                tmpResult[ComparisonFeature.MOLAR_REFRACTIVITY.getFeatureNumber()] = new double[] {Double.parseDouble(tmpArray1[2]), Double.parseDouble(tmpArray2[2]), Math.abs(Double.parseDouble(tmpArray1[2]) - Double.parseDouble(tmpArray2[2]))};
            }
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="APolDescriptor">
        if (tmpCalculateFeature[ComparisonFeature.APOL.getFeatureNumber()]) {
            tmpResult[ComparisonFeature.APOL.getFeatureNumber()] = calculateDescriptorDifference(
                ComparisonUtility.A_POL_DESCRIPTOR, tmpMolecule1, tmpMolecule2
            );
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="AromaticAtomsCountDescriptor">
        if (tmpCalculateFeature[ComparisonFeature.AROMATIC_ATOM_COUNT.getFeatureNumber()]) {
            ComparisonUtility.AROMATIC_ATOMS_COUNTS_DESCRIPTOR.setParameters(new Object[] {true});
            tmpResult[ComparisonFeature.AROMATIC_ATOM_COUNT.getFeatureNumber()] = calculateDescriptorDifference(
                ComparisonUtility.AROMATIC_ATOMS_COUNTS_DESCRIPTOR, tmpMolecule1, tmpMolecule2
            );
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="AromaticBondsCountDescriptor">
        if (tmpCalculateFeature[ComparisonFeature.AROMATIC_BOND_COUNT.getFeatureNumber()]) {
            ComparisonUtility.AROMATIC_BONDS_COUNT_DESCRIPTOR.setParameters(new Object[] {true});
            tmpResult[ComparisonFeature.AROMATIC_BOND_COUNT.getFeatureNumber()] = calculateDescriptorDifference(
                ComparisonUtility.AROMATIC_BONDS_COUNT_DESCRIPTOR, tmpMolecule1, tmpMolecule2
            );
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="AtomCountDescriptor">
        // Atom count
        if (tmpCalculateFeature[ComparisonFeature.ATOM_COUNT.getFeatureNumber()]) {
            ComparisonUtility.ATOM_COUNT_DESCRIPTOR.setParameters(new Object[] {"*"});
            tmpResult[ComparisonFeature.ATOM_COUNT.getFeatureNumber()] = calculateDescriptorDifference(
                ComparisonUtility.ATOM_COUNT_DESCRIPTOR, tmpMolecule1, tmpMolecule2
            );
        }
        // Carbon count
        if (tmpCalculateFeature[ComparisonFeature.CARBON_COUNT.getFeatureNumber()]) {
            ComparisonUtility.ATOM_COUNT_DESCRIPTOR.setParameters(new Object[] {"C"});
            tmpResult[ComparisonFeature.CARBON_COUNT.getFeatureNumber()] = calculateDescriptorDifference(
                ComparisonUtility.ATOM_COUNT_DESCRIPTOR, tmpMolecule1, tmpMolecule2
            );
        }
        // Oxygen count
        if (tmpCalculateFeature[ComparisonFeature.OXYGEN_COUNT.getFeatureNumber()]) {
            ComparisonUtility.ATOM_COUNT_DESCRIPTOR.setParameters(new Object[] {"O"});
            tmpResult[ComparisonFeature.OXYGEN_COUNT.getFeatureNumber()] = calculateDescriptorDifference(
                ComparisonUtility.ATOM_COUNT_DESCRIPTOR, tmpMolecule1, tmpMolecule2
            );
        }
        // Sulfur count
        if (tmpCalculateFeature[ComparisonFeature.SULFUR_COUNT.getFeatureNumber()]) {
            ComparisonUtility.ATOM_COUNT_DESCRIPTOR.setParameters(new Object[] {"S"});
            tmpResult[ComparisonFeature.SULFUR_COUNT.getFeatureNumber()] = calculateDescriptorDifference(
                ComparisonUtility.ATOM_COUNT_DESCRIPTOR, tmpMolecule1, tmpMolecule2
            );
        }
        // Nitrogen count
        if (tmpCalculateFeature[ComparisonFeature.NITROGEN_COUNT.getFeatureNumber()]) {
            ComparisonUtility.ATOM_COUNT_DESCRIPTOR.setParameters(new Object[] {"N"});
            tmpResult[ComparisonFeature.NITROGEN_COUNT.getFeatureNumber()] = calculateDescriptorDifference(
                ComparisonUtility.ATOM_COUNT_DESCRIPTOR, tmpMolecule1, tmpMolecule2
            );
        }
        // Phosphor count
        if (tmpCalculateFeature[ComparisonFeature.PHOSPHOR_COUNT.getFeatureNumber()]) {
            ComparisonUtility.ATOM_COUNT_DESCRIPTOR.setParameters(new Object[] {"P"});
            tmpResult[ComparisonFeature.PHOSPHOR_COUNT.getFeatureNumber()] = calculateDescriptorDifference(
                ComparisonUtility.ATOM_COUNT_DESCRIPTOR, tmpMolecule1, tmpMolecule2
            );
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="AutocorrelationDescriptorCharge">
        if (tmpCalculateFeature[ComparisonFeature.ATS_CHARGE1.getFeatureNumber()]) {
            String[] tmpArray1 = ComparisonUtility.AUTOCORRELATION_DESCRIPTOR_CHARGE.calculate(tmpMolecule1).getValue().toString().split(",");
            String[] tmpArray2 = ComparisonUtility.AUTOCORRELATION_DESCRIPTOR_CHARGE.calculate(tmpMolecule2).getValue().toString().split(",");
            tmpResult[ComparisonFeature.ATS_CHARGE1.getFeatureNumber()] = new double[] {Double.parseDouble(tmpArray1[0]), Double.parseDouble(tmpArray2[0]), Math.abs(Double.parseDouble(tmpArray1[0]) - Double.parseDouble(tmpArray2[0]))};
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="AutocorrelationDescriptorMass">
        if (tmpCalculateFeature[ComparisonFeature.ATS_MASS1.getFeatureNumber()]) {
            String[] tmpArray1 = ComparisonUtility.AUTOCORRELATION_DESCRIPTOR_MASS.calculate(tmpMolecule1).getValue().toString().split(",");
            String[] tmpArray2 = ComparisonUtility.AUTOCORRELATION_DESCRIPTOR_MASS.calculate(tmpMolecule2).getValue().toString().split(",");
            tmpResult[ComparisonFeature.ATS_MASS1.getFeatureNumber()] = new double[] {Double.parseDouble(tmpArray1[0]), Double.parseDouble(tmpArray2[0]), Math.abs(Double.parseDouble(tmpArray1[0]) - Double.parseDouble(tmpArray2[0]))};
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="AutocorrelationDescriptorPolarizability">
        if (tmpCalculateFeature[ComparisonFeature.ATS_POLARIZABILITY1.getFeatureNumber()]) {
            String[] tmpArray1 = ComparisonUtility.AUTOCORRELATION_DESCRIPTOR_POLARIZABILITY.calculate(tmpMolecule1).getValue().toString().split(",");
            String[] tmpArray2 = ComparisonUtility.AUTOCORRELATION_DESCRIPTOR_POLARIZABILITY.calculate(tmpMolecule2).getValue().toString().split(",");
            tmpResult[ComparisonFeature.ATS_POLARIZABILITY1.getFeatureNumber()] = new double[] {Double.parseDouble(tmpArray1[0]), Double.parseDouble(tmpArray2[0]), Math.abs(Double.parseDouble(tmpArray1[0]) - Double.parseDouble(tmpArray2[0]))};
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="BasicGroupCountDescriptor">
        if (tmpCalculateFeature[ComparisonFeature.BASIC_GROUP_COUNT.getFeatureNumber()]) {
            tmpResult[ComparisonFeature.BASIC_GROUP_COUNT.getFeatureNumber()] = calculateDescriptorDifference(
                ComparisonUtility.BASCIS_GROUP_COUNT_DESCRIPTOR, tmpMolecule1, tmpMolecule2
            );
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="BondCountDescriptor">
        // Bond count
        if (tmpCalculateFeature[ComparisonFeature.BOND_COUNT.getFeatureNumber()]) {
            ComparisonUtility.BOND_COUNT_DESCRIPTOR.setParameters(new Object[] {""});
            tmpResult[ComparisonFeature.BOND_COUNT.getFeatureNumber()] = calculateDescriptorDifference(
                ComparisonUtility.BOND_COUNT_DESCRIPTOR, tmpMolecule1, tmpMolecule2
            );
        }
        // Single bond count
        if (tmpCalculateFeature[ComparisonFeature.SINGLE_BOND_COUNT.getFeatureNumber()]) {
            ComparisonUtility.BOND_COUNT_DESCRIPTOR.setParameters(new Object[] {"s"});
            tmpResult[ComparisonFeature.SINGLE_BOND_COUNT.getFeatureNumber()] = calculateDescriptorDifference(
                ComparisonUtility.BOND_COUNT_DESCRIPTOR, tmpMolecule1, tmpMolecule2
            );
        }
        // Double bond count
        if (tmpCalculateFeature[ComparisonFeature.DOUBLE_BOND_COUNT.getFeatureNumber()]) {
            ComparisonUtility.BOND_COUNT_DESCRIPTOR.setParameters(new Object[] {"d"});
            tmpResult[ComparisonFeature.DOUBLE_BOND_COUNT.getFeatureNumber()] = calculateDescriptorDifference(
                ComparisonUtility.BOND_COUNT_DESCRIPTOR, tmpMolecule1, tmpMolecule2
            );
        }
        // Triple bond count
        if (tmpCalculateFeature[ComparisonFeature.TRIPLE_BOND_COUNT.getFeatureNumber()]) {
            ComparisonUtility.BOND_COUNT_DESCRIPTOR.setParameters(new Object[] {"t"});
            tmpResult[ComparisonFeature.TRIPLE_BOND_COUNT.getFeatureNumber()] = calculateDescriptorDifference(
                ComparisonUtility.BOND_COUNT_DESCRIPTOR, tmpMolecule1, tmpMolecule2
            );
        }
        // Quadruple bond count
        if (tmpCalculateFeature[ComparisonFeature.QUADRUPLE_BOND_COUNT.getFeatureNumber()]) {
            ComparisonUtility.BOND_COUNT_DESCRIPTOR.setParameters(new Object[] {"q"});
            tmpResult[ComparisonFeature.QUADRUPLE_BOND_COUNT.getFeatureNumber()] = calculateDescriptorDifference(
                ComparisonUtility.BOND_COUNT_DESCRIPTOR, tmpMolecule1, tmpMolecule2
            );
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="BPolDescriptor">
        if (tmpCalculateFeature[ComparisonFeature.BPOL.getFeatureNumber()]) {
            tmpResult[ComparisonFeature.BPOL.getFeatureNumber()] = calculateDescriptorDifference(
                ComparisonUtility.B_POL_DESCRIPTOR, tmpMolecule1, tmpMolecule2
            );
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="CarbonTypesDescriptor">
        if (tmpCalculateFeature[ComparisonFeature.C1SP1_COUNT.getFeatureNumber()] ||
            tmpCalculateFeature[ComparisonFeature.C2SP1_COUNT.getFeatureNumber()] ||
            tmpCalculateFeature[ComparisonFeature.C1SP2_COUNT.getFeatureNumber()] ||
            tmpCalculateFeature[ComparisonFeature.C2SP2_COUNT.getFeatureNumber()] ||
            tmpCalculateFeature[ComparisonFeature.C3SP2_COUNT.getFeatureNumber()] ||
            tmpCalculateFeature[ComparisonFeature.C1SP3_COUNT.getFeatureNumber()] ||
            tmpCalculateFeature[ComparisonFeature.C2SP3_COUNT.getFeatureNumber()] ||
            tmpCalculateFeature[ComparisonFeature.C3SP3_COUNT.getFeatureNumber()] ||
            tmpCalculateFeature[ComparisonFeature.C4SP3_COUNT.getFeatureNumber()]) {
            String[] tmpArray1 = ComparisonUtility.CARBON_TYPES_DESCRIPTOR.calculate(tmpMolecule1).getValue().toString().split(",");
            String[] tmpArray2 = ComparisonUtility.CARBON_TYPES_DESCRIPTOR.calculate(tmpMolecule2).getValue().toString().split(",");
            // C1SP1 count
            if (tmpCalculateFeature[ComparisonFeature.C1SP1_COUNT.getFeatureNumber()]) {
                tmpResult[ComparisonFeature.C1SP1_COUNT.getFeatureNumber()] = new double[] {Double.parseDouble(tmpArray1[0]), Double.parseDouble(tmpArray2[0]), Math.abs(Double.parseDouble(tmpArray1[0]) - Double.parseDouble(tmpArray2[0]))};
            }
            // C2SP1 count
            if (tmpCalculateFeature[ComparisonFeature.C2SP1_COUNT.getFeatureNumber()]) {
                tmpResult[ComparisonFeature.C2SP1_COUNT.getFeatureNumber()] = new double[] {Double.parseDouble(tmpArray1[1]), Double.parseDouble(tmpArray2[1]), Math.abs(Double.parseDouble(tmpArray1[1]) - Double.parseDouble(tmpArray2[1]))};
            }
            // C1SP2 count
            if (tmpCalculateFeature[ComparisonFeature.C1SP2_COUNT.getFeatureNumber()]) {
                tmpResult[ComparisonFeature.C1SP2_COUNT.getFeatureNumber()] = new double[] {Double.parseDouble(tmpArray1[2]), Double.parseDouble(tmpArray2[2]), Math.abs(Double.parseDouble(tmpArray1[2]) - Double.parseDouble(tmpArray2[2]))};
            }
            // C2SP2 count
            if (tmpCalculateFeature[ComparisonFeature.C2SP2_COUNT.getFeatureNumber()]) {
                tmpResult[ComparisonFeature.C2SP2_COUNT.getFeatureNumber()] = new double[] {Double.parseDouble(tmpArray1[3]), Double.parseDouble(tmpArray2[3]), Math.abs(Double.parseDouble(tmpArray1[3]) - Double.parseDouble(tmpArray2[3]))};
            }
            // C3SP2 count
            if (tmpCalculateFeature[ComparisonFeature.C3SP2_COUNT.getFeatureNumber()]) {
                tmpResult[ComparisonFeature.C3SP2_COUNT.getFeatureNumber()] = new double[] {Double.parseDouble(tmpArray1[4]), Double.parseDouble(tmpArray2[4]), Math.abs(Double.parseDouble(tmpArray1[4]) - Double.parseDouble(tmpArray2[4]))};
            }
            // C1SP3 count
            if (tmpCalculateFeature[ComparisonFeature.C1SP3_COUNT.getFeatureNumber()]) {
                tmpResult[ComparisonFeature.C1SP3_COUNT.getFeatureNumber()] = new double[] {Double.parseDouble(tmpArray1[5]), Double.parseDouble(tmpArray2[5]), Math.abs(Double.parseDouble(tmpArray1[5]) - Double.parseDouble(tmpArray2[5]))};
            }
            // C2SP3 count
            if (tmpCalculateFeature[ComparisonFeature.C2SP3_COUNT.getFeatureNumber()]) {
                tmpResult[ComparisonFeature.C2SP3_COUNT.getFeatureNumber()] = new double[] {Double.parseDouble(tmpArray1[6]), Double.parseDouble(tmpArray2[6]), Math.abs(Double.parseDouble(tmpArray1[6]) - Double.parseDouble(tmpArray2[6]))};
            }
            // C3SP3 count
            if (tmpCalculateFeature[ComparisonFeature.C3SP3_COUNT.getFeatureNumber()]) {
                tmpResult[ComparisonFeature.C3SP3_COUNT.getFeatureNumber()] = new double[] {Double.parseDouble(tmpArray1[7]), Double.parseDouble(tmpArray2[7]), Math.abs(Double.parseDouble(tmpArray1[7]) - Double.parseDouble(tmpArray2[7]))};
            }
            // C4SP3 count
            if (tmpCalculateFeature[ComparisonFeature.C4SP3_COUNT.getFeatureNumber()]) {
                tmpResult[ComparisonFeature.C4SP3_COUNT.getFeatureNumber()] = new double[] {Double.parseDouble(tmpArray1[8]), Double.parseDouble(tmpArray2[8]), Math.abs(Double.parseDouble(tmpArray1[8]) - Double.parseDouble(tmpArray2[8]))};
            }
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Equality">
        if (tmpCalculateFeature[ComparisonFeature.EQUALITY.getFeatureNumber()]) {
            String tmpUniqueSmiles1 = ComparisonUtility.SMILES_GENERATOR.create(tmpMolecule1);
            String tmpUniqueSmiles2 = ComparisonUtility.SMILES_GENERATOR.create(tmpMolecule2);
            tmpResult[ComparisonFeature.EQUALITY.getFeatureNumber()] = new double[] {Double.NaN, Double.NaN, tmpUniqueSmiles1.equals(tmpUniqueSmiles2) ? 1 : 0};
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="EccentricConnectivityIndexDescriptor">
        if (tmpCalculateFeature[ComparisonFeature.ECCENTRIC_CONNECTIVITY_INDEX.getFeatureNumber()]) {
            tmpResult[ComparisonFeature.ECCENTRIC_CONNECTIVITY_INDEX.getFeatureNumber()] = calculateDescriptorDifference(
                ComparisonUtility.ECCENTRIC_CONNECTIVITY_DESCRIPTOR, tmpMolecule1, tmpMolecule2
            );
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="FMFDescriptor">
        if (tmpCalculateFeature[ComparisonFeature.FMF.getFeatureNumber()]) {
            tmpResult[ComparisonFeature.FMF.getFeatureNumber()] = calculateDescriptorDifference(
                ComparisonUtility.FMF_DESCRIPTOR, tmpMolecule1, tmpMolecule2
            );
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="FractionalCSP3Descriptor">
        if (tmpCalculateFeature[ComparisonFeature.FCSP3.getFeatureNumber()]) {
            tmpResult[ComparisonFeature.FCSP3.getFeatureNumber()] = calculateDescriptorDifference(
                ComparisonUtility.FRACTIONAL_CSP3_DESCRIPTOR, tmpMolecule1, tmpMolecule2
            );
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="FractionalPSADescriptor">
        if (tmpCalculateFeature[ComparisonFeature.FPSA.getFeatureNumber()]) {
            ComparisonUtility.FRACTIONAL_PSA_DESCRIPTOR.initialise(DefaultChemObjectBuilder.getInstance());
            tmpResult[ComparisonFeature.FPSA.getFeatureNumber()] = calculateDescriptorDifference(
                ComparisonUtility.FRACTIONAL_PSA_DESCRIPTOR, tmpMolecule1, tmpMolecule2
            );
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="FragmentComplexityDescriptor">
        if (tmpCalculateFeature[ComparisonFeature.FRAGMENT_COMPLEXITY.getFeatureNumber()]) {
            tmpResult[ComparisonFeature.FRAGMENT_COMPLEXITY.getFeatureNumber()] = calculateDescriptorDifference(
                ComparisonUtility.FRAGMENT_COMPLEXITY_DESCRIPTOR, tmpMolecule1, tmpMolecule2
            );
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="HBondAcceptorCountDescriptor">
        if (tmpCalculateFeature[ComparisonFeature.H_BOND_ACCEPTOR_COUNT.getFeatureNumber()]) {
            ComparisonUtility.HBOND_ACCEPTOR_COUNT_DESCRIPTOR.setParameters(new Object[] {true});
            tmpResult[ComparisonFeature.H_BOND_ACCEPTOR_COUNT.getFeatureNumber()] = calculateDescriptorDifference(
                ComparisonUtility.HBOND_ACCEPTOR_COUNT_DESCRIPTOR, tmpMolecule1, tmpMolecule2
            );
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="HBondDonorCountDescriptor">
        if (tmpCalculateFeature[ComparisonFeature.H_BOND_DONOR_COUNT.getFeatureNumber()]) {
            tmpResult[ComparisonFeature.H_BOND_DONOR_COUNT.getFeatureNumber()] = calculateDescriptorDifference(
                ComparisonUtility.HBOND_DONOR_COUNT_DESCRIPTOR, tmpMolecule1, tmpMolecule2
            );
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="JPlogPDescriptor">
        if (tmpCalculateFeature[ComparisonFeature.JP_LOGP.getFeatureNumber()]) {
            tmpResult[ComparisonFeature.JP_LOGP.getFeatureNumber()] = calculateDescriptorDifference(
                ComparisonUtility.JP_LOGP_DESCRIPTOR, tmpMolecule1, tmpMolecule2
            );
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="KappaShapeIndicesDescriptor">
        if (tmpCalculateFeature[ComparisonFeature.KAPPA_SHAPE_INDEX_1.getFeatureNumber()] ||
            tmpCalculateFeature[ComparisonFeature.KAPPA_SHAPE_INDEX_2.getFeatureNumber()] ||
            tmpCalculateFeature[ComparisonFeature.KAPPA_SHAPE_INDEX_3.getFeatureNumber()]) {
            String[] tmpArray1 = ComparisonUtility.KAPPA_SHAPE_INDICES_DESCRIPTOR.calculate(tmpMolecule1).getValue().toString().split(",");
            String[] tmpArray2 = ComparisonUtility.KAPPA_SHAPE_INDICES_DESCRIPTOR.calculate(tmpMolecule2).getValue().toString().split(",");
            // First kappa shape index
            if (tmpCalculateFeature[ComparisonFeature.KAPPA_SHAPE_INDEX_1.getFeatureNumber()]) {
                tmpResult[ComparisonFeature.KAPPA_SHAPE_INDEX_1.getFeatureNumber()] = new double[] {Double.parseDouble(tmpArray1[0]), Double.parseDouble(tmpArray2[0]), Math.abs(Double.parseDouble(tmpArray1[0]) - Double.parseDouble(tmpArray2[0]))};
            }
            // Second kappa shape index
            if (tmpCalculateFeature[ComparisonFeature.KAPPA_SHAPE_INDEX_2.getFeatureNumber()]) {
                tmpResult[ComparisonFeature.KAPPA_SHAPE_INDEX_2.getFeatureNumber()] = new double[] {Double.parseDouble(tmpArray1[1]), Double.parseDouble(tmpArray2[1]), Math.abs(Double.parseDouble(tmpArray1[1]) - Double.parseDouble(tmpArray2[1]))};
            }
            // Third kappa shape index
            if (tmpCalculateFeature[ComparisonFeature.KAPPA_SHAPE_INDEX_3.getFeatureNumber()]) {
                tmpResult[ComparisonFeature.KAPPA_SHAPE_INDEX_3.getFeatureNumber()] = new double[] {Double.parseDouble(tmpArray1[2]), Double.parseDouble(tmpArray2[2]), Math.abs(Double.parseDouble(tmpArray1[2]) - Double.parseDouble(tmpArray2[2]))};
            }
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="LargestChainDescriptor">
        if (tmpCalculateFeature[ComparisonFeature.LARGEST_CHAIN.getFeatureNumber()]) {
            // Doesn't need to check for rings because that is done in the preproccesing
            ComparisonUtility.LARGEST_CHAIN_DESCRIPTOR.setParameters(new Object[] {false, false});
            tmpResult[ComparisonFeature.LARGEST_CHAIN.getFeatureNumber()] = calculateDescriptorDifference(
                ComparisonUtility.LARGEST_CHAIN_DESCRIPTOR, tmpMolecule1, tmpMolecule2
            );
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="LargestPiSystemDescriptor">
        if (tmpCalculateFeature[ComparisonFeature.LARGEST_PI_SYSTEM.getFeatureNumber()]) {
            // Doesn't need to check for aromaticity because that is done in the preproccesing
            ComparisonUtility.LARGEST_PI_SYSTEM_DESCRIPTOR.setParameters(new Object[] {false});
            tmpResult[ComparisonFeature.LARGEST_PI_SYSTEM.getFeatureNumber()] = calculateDescriptorDifference(
                ComparisonUtility.LARGEST_PI_SYSTEM_DESCRIPTOR, tmpMolecule1, tmpMolecule2
            );
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="LongestAliphaticChainDescriptor">
        if (tmpCalculateFeature[ComparisonFeature.LONGEST_ALIPHATIC_CHAIN.getFeatureNumber()]) {
            // Doesn't need to check for rings because that is done in the preproccesing
            ComparisonUtility.LONGEST_ALIPHATIC_CHAIN_DESCRIPTOR.setParameters(new Object[] {false});
            tmpResult[ComparisonFeature.LONGEST_ALIPHATIC_CHAIN.getFeatureNumber()] = calculateDescriptorDifference(
                ComparisonUtility.LONGEST_ALIPHATIC_CHAIN_DESCRIPTOR, tmpMolecule1, tmpMolecule2
            );
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="MannholdLogPDescriptor">
        if (tmpCalculateFeature[ComparisonFeature.MANNHOLD_LOGP.getFeatureNumber()]) {
            tmpResult[ComparisonFeature.MANNHOLD_LOGP.getFeatureNumber()] = calculateDescriptorDifference(
                ComparisonUtility.MANNHOLD_LOG_P_DESCRIPTOR, tmpMolecule1, tmpMolecule2
            );
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="PetitjeanNumberDescriptor">
        if (tmpCalculateFeature[ComparisonFeature.PETITJEAN_NUMBER.getFeatureNumber()]) {
            tmpResult[ComparisonFeature.PETITJEAN_NUMBER.getFeatureNumber()] = calculateDescriptorDifference(
                ComparisonUtility.PETITJEAN_NUMBER_DESCRIPTOR, tmpMolecule1, tmpMolecule2
            );
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="PetitjeanShapeIndexDescriptor">
        if (tmpCalculateFeature[ComparisonFeature.PETITJEAN_SHAPE_INDEX.getFeatureNumber()]) {
            String[] tmpArray1 = ComparisonUtility.PETITJEAN_SHAPE_INDEX_DESCRIPTOR.calculate(tmpMolecule1).getValue().toString().split(",");
            String[] tmpArray2 = ComparisonUtility.PETITJEAN_SHAPE_INDEX_DESCRIPTOR.calculate(tmpMolecule2).getValue().toString().split(",");
            tmpResult[ComparisonFeature.PETITJEAN_SHAPE_INDEX.getFeatureNumber()] = new double[] {Double.parseDouble(tmpArray1[0]), Double.parseDouble(tmpArray2[0]), Math.abs(Double.parseDouble(tmpArray1[0]) - Double.parseDouble(tmpArray2[0]))};
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="RotatableBondsCountDescriptor">
        if (tmpCalculateFeature[ComparisonFeature.ROTATABLE_BOND_COUNT.getFeatureNumber()]) {
            // Don't include terminal bonds and don't exclude amide bonds
            ComparisonUtility.ROTATABLE_BOND_COUNT.setParameters(new Object[] {false, false});
            tmpResult[ComparisonFeature.ROTATABLE_BOND_COUNT.getFeatureNumber()] = calculateDescriptorDifference(
                ComparisonUtility.ROTATABLE_BOND_COUNT, tmpMolecule1, tmpMolecule2
            );
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="SmallRingDescriptor">
        if (tmpCalculateFeature[ComparisonFeature.ALL_SMALL_RINGS_COUNT.getFeatureNumber()]) {
            int tmpRingCount1 = Integer.parseInt(ComparisonUtility.SMALL_RING_DESCRIPTOR.calculate(tmpMolecule1).getValue().toString().split(",")[0]);
            int tmpRingCount2 = Integer.parseInt(ComparisonUtility.SMALL_RING_DESCRIPTOR.calculate(tmpMolecule2).getValue().toString().split(",")[0]);
            tmpResult[ComparisonFeature.ALL_SMALL_RINGS_COUNT.getFeatureNumber()] = new double[] {tmpRingCount1, tmpRingCount2, Math.abs(tmpRingCount1 - tmpRingCount2)};
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="SpiroAtomCountDescriptor">
        if (tmpCalculateFeature[ComparisonFeature.SPIRO_ATOM_COUNT.getFeatureNumber()]) {
            tmpResult[ComparisonFeature.SPIRO_ATOM_COUNT.getFeatureNumber()] = calculateDescriptorDifference(
                ComparisonUtility.SPIRO_ATOM_COUNT_DESCRIPTOR, tmpMolecule1, tmpMolecule2
            );
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Tanimoto">
        // Basic fingerprint
        if (tmpCalculateFeature[ComparisonFeature.TANIMOTO_BASIC_FINGERPRINTER.getFeatureNumber()]) {
            tmpResult[ComparisonFeature.TANIMOTO_BASIC_FINGERPRINTER.getFeatureNumber()] = calculateTanimotoCoefficient(
                ComparisonUtility.BASIC_FINGERPRINTER, tmpMolecule1, tmpMolecule2
            );
        }
        // LINGO fingerprint
        if (tmpCalculateFeature[ComparisonFeature.TANIMOTO_LINGO_FINGERPRINTER.getFeatureNumber()]) {
            tmpResult[ComparisonFeature.TANIMOTO_LINGO_FINGERPRINTER.getFeatureNumber()] = calculateTanimotoCoefficient(
                ComparisonUtility.LINGO_FINGERPRINTER, tmpMolecule1, tmpMolecule2
            );
        }
        // Extended fingerprint
        if (tmpCalculateFeature[ComparisonFeature.TANIMOTO_EXTENDED_FINGERPRINTER.getFeatureNumber()]) {
            tmpResult[ComparisonFeature.TANIMOTO_EXTENDED_FINGERPRINTER.getFeatureNumber()] = calculateTanimotoCoefficient(
                ComparisonUtility.EXTENDED_FINGERPRINTER, tmpMolecule1, tmpMolecule2
            );
        }
        // E-State fingerprint
        if (tmpCalculateFeature[ComparisonFeature.TANIMOTO_ESTATE_FINGERPRINTER.getFeatureNumber()]) {
            tmpResult[ComparisonFeature.TANIMOTO_ESTATE_FINGERPRINTER.getFeatureNumber()] = calculateTanimotoCoefficient(
                ComparisonUtility.ESTATE_FINGERPRINTER, tmpMolecule1, tmpMolecule2
            );
        }
        // PubChem fingerprint
        if (tmpCalculateFeature[ComparisonFeature.TANIMOTO_PUBCHEM_FINGERPRINTER.getFeatureNumber()]) {
            tmpResult[ComparisonFeature.TANIMOTO_PUBCHEM_FINGERPRINTER.getFeatureNumber()] = calculateTanimotoCoefficient(
                ComparisonUtility.PUBCHEM_FINGERPRINTER, tmpMolecule1, tmpMolecule2
            );
        }
        // Shortest path fingerprint
        if (tmpCalculateFeature[ComparisonFeature.TANIMOTO_SHORTEST_PATH_FINGERPRINTER.getFeatureNumber()]) {
            tmpResult[ComparisonFeature.TANIMOTO_SHORTEST_PATH_FINGERPRINTER.getFeatureNumber()] = calculateTanimotoCoefficient(
                ComparisonUtility.SHORTEST_PATH_FINGERPRINTER, tmpMolecule1, tmpMolecule2
            );
        }
        // Substructure fingerprint
        if (tmpCalculateFeature[ComparisonFeature.TANIMOTO_SUBSTRUCTURE_FINGERPRINTER.getFeatureNumber()]) {
            tmpResult[ComparisonFeature.TANIMOTO_SUBSTRUCTURE_FINGERPRINTER.getFeatureNumber()] = calculateTanimotoCoefficient(
                ComparisonUtility.SUBSTRUCTURE_FINGERPRINTER, tmpMolecule1, tmpMolecule2
            );
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="TPSADescriptor">
        if (tmpCalculateFeature[ComparisonFeature.TPSA.getFeatureNumber()]) {
            // Doesn't need to check for aromaticity because that is done in the preproccesing
            ComparisonUtility.TPSA_DESCRIPTOR.setParameters(new Object[] {false});
            tmpResult[ComparisonFeature.TPSA.getFeatureNumber()] = calculateDescriptorDifference(
                ComparisonUtility.TPSA_DESCRIPTOR, tmpMolecule1, tmpMolecule2
            );
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="VABCDescriptor">
        if (tmpCalculateFeature[ComparisonFeature.VABC.getFeatureNumber()]) {
            tmpResult[ComparisonFeature.VABC.getFeatureNumber()] = calculateDescriptorDifference(
                ComparisonUtility.VABC_DESCRIPTOR, tmpMolecule1, tmpMolecule2
            );
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="VAdjMaDescriptor">
        if (tmpCalculateFeature[ComparisonFeature.V_ADJ_MA.getFeatureNumber()]) {
            tmpResult[ComparisonFeature.V_ADJ_MA.getFeatureNumber()] = calculateDescriptorDifference(
                ComparisonUtility.V_ADJ_MA_DESCRIPTOR, tmpMolecule1, tmpMolecule2
            );
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="WeightDescriptor">
        if (tmpCalculateFeature[ComparisonFeature.MOLECULAR_WEIGHT.getFeatureNumber()]) {
            tmpResult[ComparisonFeature.MOLECULAR_WEIGHT.getFeatureNumber()] = calculateDescriptorDifference(
                ComparisonUtility.WEIGHT_DESCRIPTOR, tmpMolecule1, tmpMolecule2
            );
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="WeightedPathDescriptor">
        if (tmpCalculateFeature[ComparisonFeature.WEIGHTED_PATH_1.getFeatureNumber()]) {
            String[] tmpArray1 = ComparisonUtility.WEIGHTER_PATH_DESCRIPTOR.calculate(tmpMolecule1).getValue().toString().split(",");
            String[] tmpArray2 = ComparisonUtility.WEIGHTER_PATH_DESCRIPTOR.calculate(tmpMolecule2).getValue().toString().split(",");
            tmpResult[ComparisonFeature.WEIGHTED_PATH_1.getFeatureNumber()] = new double[] {Double.parseDouble(tmpArray1[0]), Double.parseDouble(tmpArray2[0]), Math.abs(Double.parseDouble(tmpArray1[0]) - Double.parseDouble(tmpArray2[0]))};
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="WienerNumbersDescriptor">
        if (tmpCalculateFeature[ComparisonFeature.WIENER_PATH.getFeatureNumber()] ||
            tmpCalculateFeature[ComparisonFeature.WIENER_POLARITY.getFeatureNumber()]) {
            String[] tmpArray1 = ComparisonUtility.WIENER_NUMBERS_DESCRIPTOR.calculate(tmpMolecule1).getValue().toString().split(",");
            String[] tmpArray2 = ComparisonUtility.WIENER_NUMBERS_DESCRIPTOR.calculate(tmpMolecule2).getValue().toString().split(",");
            // Wiener path number
            if (tmpCalculateFeature[ComparisonFeature.WIENER_PATH.getFeatureNumber()]) {
                tmpResult[ComparisonFeature.WIENER_PATH.getFeatureNumber()] = new double[] {Double.parseDouble(tmpArray1[0]), Double.parseDouble(tmpArray2[0]), Math.abs(Double.parseDouble(tmpArray1[0]) - Double.parseDouble(tmpArray2[0]))};
            }
            // Wiener polarity number
            if (tmpCalculateFeature[ComparisonFeature.WIENER_POLARITY.getFeatureNumber()]) {
                tmpResult[ComparisonFeature.WIENER_POLARITY.getFeatureNumber()] = new double[] {Double.parseDouble(tmpArray1[1]), Double.parseDouble(tmpArray2[1]), Math.abs(Double.parseDouble(tmpArray1[1]) - Double.parseDouble(tmpArray2[1]))};
            }
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="XLogPDescriptor">
        if (tmpCalculateFeature[ComparisonFeature.X_LOGP.getFeatureNumber()]) {
            // Aromaticity is already checked in preprocessing; use the salicyl acid correction factor
            ComparisonUtility.X_LOGP_DESCRIPTOR.setParameters(new Object[] {false, true});
            tmpResult[ComparisonFeature.X_LOGP.getFeatureNumber()] = calculateDescriptorDifference(
                ComparisonUtility.X_LOGP_DESCRIPTOR, tmpMolecule1, tmpMolecule2
            );
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="ZagrebIndexDescriptor">
        if (tmpCalculateFeature[ComparisonFeature.ZAGREB_INDEX.getFeatureNumber()]) {
            tmpResult[ComparisonFeature.ZAGREB_INDEX.getFeatureNumber()] = calculateDescriptorDifference(
                ComparisonUtility.ZAGREB_INDEY_DESCRIPTOR, tmpMolecule1, tmpMolecule2
            );
        }
        //</editor-fold>
        return tmpResult;
    }
    //</editor-fold>
    //
    //<editor-fold defaultstate="collapsed" desc="Private methods">
    /**
     * Calculate the individual descriptor values and their difference of the 
     * two given molecules and the given descriptor
     * 
     * @param tmpDescriptor specifies the descriptor
     * @param tmpMolecule1 the first molecule
     * @param tmpMolecule2 the second molecule
     * @return an array of the form 
     * {firstDescriptorValue, secondDescriptorValue, difference}
     */
    private static double[] calculateDescriptorDifference(IMolecularDescriptor tmpDescriptor, IAtomContainer tmpMolecule1, IAtomContainer tmpMolecule2) {
        double tmpResult1 = Double.parseDouble(tmpDescriptor.calculate(tmpMolecule1).getValue().toString());
        double tmpResult2 = Double.parseDouble(tmpDescriptor.calculate(tmpMolecule2).getValue().toString());
        return new double[] {tmpResult1, tmpResult2, Math.abs(tmpResult1 - tmpResult2)};
    };
    
    /**
     * Calculate the Tanimoto coefficient for the two given molecules with 
     * the given fingerprint
     * 
     * @param tmpFingerprinter specifies the fingerprint
     * @param tmpMolecule1 the first molecule
     * @param tmpMolecule2 the second molecule
     * @return an array of the form {NaN, NaN, Tanimoto coefficient}
     */
    private static double[] calculateTanimotoCoefficient(IFingerprinter tmpFingerprinter, IAtomContainer tmpMolecule1, IAtomContainer tmpMolecule2) {
        try {
            return new double[] {Double.NaN, Double.NaN, Tanimoto.calculate(tmpFingerprinter.getBitFingerprint(tmpMolecule1), tmpFingerprinter.getBitFingerprint(tmpMolecule2))};
        } catch (CDKException ex) {
            return new double[] {Double.NaN, Double.NaN, Double.NaN};
        }
    }
    //</editor-fold>
    
}
