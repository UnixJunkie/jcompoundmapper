package fingerprinters.topological;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IMolecule;

import tools.moltyping.MoltyperException;
import tools.moltyping.enumerations.EnumerationsAtomTypes.AtomLabelType;
import fingerprinters.FingerPrinterException;
import fingerprinters.features.IFeature;
import fingerprinters.topological.features.DanglingBond;
import fingerprinters.topological.features.ECFPFeature;

public class Encoding2DExtendedConnectivity extends Encoding2D {
	private int currentIteration;
	private ArrayList<IFeature> completeFeatures;
	private IAtomContainer molecule;
	private Map<IAtom, Integer> hashedAtomLabels;
	private Map<IAtom, ECFPFeature> featuresOfLastIteration;

	public Encoding2DExtendedConnectivity() {
		super.setSearchDepth(4);
		super.setAtomLabelType(AtomLabelType.DAYLIGHT_INVARIANT_RING);
	}
	
	@Override
	public ArrayList<IFeature> getFingerprint(IAtomContainer molecule) {
		try {
			this.calculateFingerprint(molecule);
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return this.completeFeatures;
	}
	
	private void calculateFingerprint(IAtomContainer ac) throws FingerPrinterException, MoltyperException{
		this.molecule = ac;
		this.initialize();
		this.computeInitialIteration();
		
		for (int i = 0; i < super.getSearchDepth(); i++) {
			this.currentIteration++;
			this.computeIteration();
		}
		
	}
	
	private void initialize() throws MoltyperException, FingerPrinterException{
		this.hashedAtomLabels = new HashMap<IAtom, Integer>();
		this.featuresOfLastIteration = new HashMap<IAtom, ECFPFeature>();
		this.currentIteration = 0;
		this.completeFeatures = new ArrayList<IFeature>();
	}
	
	private void computeInitialIteration() throws MoltyperException, FingerPrinterException{
		for (IAtom atom : this.molecule.atoms()) {
			int hashCode = this.getAtomLabel(atom).hashCode();
			IMolecule substructure = new Molecule();
			substructure.addAtom(atom);
			ECFPFeature ecfpFeature = new ECFPFeature(hashCode, atom, substructure,
													this.generateExtensionBondList(atom), this.currentIteration);
			this.hashedAtomLabels.put(atom, hashCode);
			this.featuresOfLastIteration.put(atom, ecfpFeature);
			this.completeFeatures.add(ecfpFeature);
		}
	}
	
	private DanglingBond[] generateExtensionBondList(IAtom atom) throws FingerPrinterException {
		List<IBond> bonds = this.molecule.getConnectedBondsList(atom);
		DanglingBond[] connectivityBonds = new DanglingBond[bonds.size()];
		int i = 0;
		for (IBond bond : bonds) {
			connectivityBonds[i] = new DanglingBond(bond, bond.getConnectedAtom(atom));
			i++;
		}
		return connectivityBonds;
	}

	private void computeIteration() throws FingerPrinterException, MoltyperException {
		List<ECFPFeature> newFeatures = new ArrayList<ECFPFeature>();
		
		for (IAtom atom : this.hashedAtomLabels.keySet()) {
			newFeatures.add(this.computeIterationForAtom(atom));
		}
		
		this.removeDuplicateSubstructures(newFeatures);
		this.completeFeatures.addAll(newFeatures);
	}

	private ECFPFeature computeIterationForAtom(IAtom atom) throws FingerPrinterException, MoltyperException {
		final ECFPFeature oldFeature = this.featuresOfLastIteration.get(atom);
		final IMolecule newSubstructure = oldFeature.getNonDeepCloneOfSubstructure();
		final int numDanglingBonds = oldFeature.numberOfDanglingBonds();

		final List<BondOrderAtomIdentifierTupel> connections = new ArrayList<BondOrderAtomIdentifierTupel>(numDanglingBonds);
		final Map<IBond, DanglingBond> newConnectionCandidates = new HashMap<IBond, DanglingBond>();

		for (int i = 0; i < numDanglingBonds; i++) {
			final DanglingBond connection = oldFeature.getDanglingBond(i);
			final IAtom connectedAtom = connection.getBond().getAtom(connection.getConnectedAtomPosition());
			final int identifierOfConnectedAtom = this.hashedAtomLabels.get(connectedAtom);
			newSubstructure.addAtom(connectedAtom);
			newSubstructure.addBond(connection.getBond());
			connections
					.add(new BondOrderAtomIdentifierTupel(this.getBondOrder(connection.getBond()), identifierOfConnectedAtom));

			final ArrayList<DanglingBond> newConnections = this.getConnectionsOfAtom(connection.getBond(), connectedAtom);
			for (final DanglingBond dbond : newConnections) {
				if (!newConnectionCandidates.containsKey(dbond.getBond())) {
					newConnectionCandidates.put(dbond.getBond(), dbond);
				}
			}
		}

		final Iterator<DanglingBond> iter = newConnectionCandidates.values().iterator();
		
		while (iter.hasNext()) {
			final DanglingBond bondToCheck = iter.next();
			if (newSubstructure.contains(bondToCheck.getConnectedAtom())) {
				if (!newSubstructure.contains(bondToCheck.getBond())) {
					newSubstructure.addBond(bondToCheck.getBond());
				}
				iter.remove();
			}
		}
		
		final DanglingBond[] newDanglingBonds = newConnectionCandidates.values().toArray(
				new DanglingBond[newConnectionCandidates.size()]);
		final int featureHashCode = this.computeFeatureHash(oldFeature.hashToInteger(), connections);
		final ECFPFeature newFeature = new ECFPFeature(featureHashCode, atom, newSubstructure, newDanglingBonds,
				this.currentIteration);
		this.featuresOfLastIteration.put(atom, newFeature);
		return newFeature;
	}

	private int computeFeatureHash(int featureOfCoreAtom, List<BondOrderAtomIdentifierTupel> extensions) {
		final int[] featureHash = new int[extensions.size() * 2 + 2];
		featureHash[0] = this.currentIteration;
		featureHash[1] = featureOfCoreAtom;
		Collections.sort(extensions);

		for (int i = 1; i <= extensions.size(); i++) {
			final BondOrderAtomIdentifierTupel tupel = extensions.get(i - 1);
			featureHash[i * 2] = tupel.bondOrder;
			featureHash[i * 2 + 1] = tupel.atomIdentifier;
		}

		return Arrays.hashCode(featureHash);
	}

	private int getBondOrder(IBond bond) throws MoltyperException {
		if (bond.getFlag(CDKConstants.ISAROMATIC)) {
			return 4;
		} else {
			return bond.getOrder().ordinal();
		}
	}

	private ArrayList<DanglingBond> getConnectionsOfAtom(IBond fromBond, IAtom atom) throws FingerPrinterException {
		final ArrayList<DanglingBond> connections = new ArrayList<DanglingBond>();
		for (final IBond bond : this.molecule.getConnectedBondsList(atom)) {
			if (bond != fromBond) {
				connections.add(new DanglingBond(bond, bond.getConnectedAtom(atom)));
			}
		}
		return connections;
	}

	@Override
	public String getNameOfFingerPrinter() {
		return "ECFP";
	}

	private boolean hasDuplicate(ECFPFeature feature) {
		for (final IFeature f : this.completeFeatures) {
			if (feature.hasEqualSubstructure((ECFPFeature) f)) {
				return true;
			}
		}
		return false;
	}

	private void removeDuplicateSubstructures(List<ECFPFeature> newFeatures) {
		final Iterator<ECFPFeature> iter = newFeatures.iterator();

		while (iter.hasNext()) {
			final ECFPFeature featureToCheck = iter.next();
			if (this.hasDuplicate(featureToCheck)) {
				iter.remove();
				continue;
			}
			for (final ECFPFeature feature : newFeatures) {
				if (feature != featureToCheck && featureToCheck.hasEqualSubstructure(feature)) {
					if (featureToCheck.hashToInteger() >= feature.hashToInteger()) {
						iter.remove();
						break;
					}
				}
			}
		}
	}

	private class BondOrderAtomIdentifierTupel implements Comparable<BondOrderAtomIdentifierTupel> {
		private final int bondOrder;
		private final int atomIdentifier;

		public BondOrderAtomIdentifierTupel(int bondOrder, int atomIdentifier) {
			this.bondOrder = bondOrder;
			this.atomIdentifier = atomIdentifier;
		}

		@Override
		public int compareTo(BondOrderAtomIdentifierTupel o) {
			if (this.bondOrder < o.bondOrder) {
				return -1;
			} else if (this.bondOrder > o.bondOrder) {
				return 1;
			} else {
				if (this.atomIdentifier < o.atomIdentifier) {
					return -1;
				} else if (this.atomIdentifier > o.atomIdentifier) {
					return 1;
				} else {
					return 0;
				}
			}
		}
	}
}
