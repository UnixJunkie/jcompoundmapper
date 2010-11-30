package fingerprinters.topological.features;

import org.openscience.cdk.Atom;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.PseudoAtom;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.smiles.SmilesGenerator;

import fingerprinters.features.IFeature;

public class ECFPFeature implements IFeature {

	private final int feature;
	private IMolecule substructure;
	private final DanglingBond[] connectivityBonds;
	private IAtom coreAtom;
	private int iterationNumber;
	
	public ECFPFeature(int feature, IAtom coreAtom, IMolecule substructure, DanglingBond[] connectivityBonds, int iterationNumber) {
		this.feature = feature;
		this.substructure = substructure;
		this.connectivityBonds = connectivityBonds;
		this.coreAtom = coreAtom;
		this.iterationNumber = iterationNumber;
	}

	@Override
	public int compareTo(IFeature arg0) {
		final ECFPFeature ecfp_arg = (ECFPFeature) arg0;
		if (this.feature > ecfp_arg.feature) {
			return 1;
		}
		if (this.feature < ecfp_arg.feature) {
			return -1;
		} else {
			return 0;
		}
	}

	@Override
	public String featureToString() {
		final IAtom[] tempAtoms = new IAtom[this.connectivityBonds.length];
		final IMolecule substructureClone = this.getNonDeepCloneOfSubstructure();

		for (int i = 0; i < this.connectivityBonds.length; i++) {
			final DanglingBond connectivity = this.connectivityBonds[i];
			final IBond bond = connectivity.getBond();
			tempAtoms[i] = connectivity.getConnectedAtom();
			if (!substructureClone.contains(connectivity.getConnectedAtom())) {
				final IAtom pseudoAtom = new PseudoAtom();
				bond.setAtom(pseudoAtom, connectivity.getConnectedAtomPosition());
				substructureClone.addAtom(pseudoAtom);
			}
			substructureClone.addBond(connectivity.getBond());
		}
		String smile = new SmilesGenerator().createSMILES(substructureClone);

		for (int i = 0; i < this.connectivityBonds.length; i++) {
			final DanglingBond connectivity = this.connectivityBonds[i];
			final IBond bond = connectivity.getBond();
			bond.setAtom(tempAtoms[i], connectivity.getConnectedAtomPosition());
		}

		return smile;
	}

	public IAtom getCoreAtom() {
		return this.coreAtom;
	}

	public DanglingBond getDanglingBond(int i) {
		return this.connectivityBonds[i];
	}

	public int getIterationNumber() {
		return this.iterationNumber;
	}

	public IMolecule getNonDeepCloneOfSubstructure() {
		final IMolecule clone = new Molecule();
		for (final IBond bond : this.substructure.bonds()) {
			clone.addBond(bond);
		}

		for (final IAtom atom : this.substructure.atoms()) {
			clone.addAtom(atom);
		}

		return clone;
	}

	public int numberOfDanglingBonds() {
		return this.connectivityBonds.length;
	}

	@Override
	public double getValue() {
		return 1;
	}

	public int hashToInteger() {
		return this.feature;
	}

	public boolean hasEqualSubstructure(ECFPFeature arg) {
		if (arg.substructure.getAtomCount() != this.substructure.getAtomCount()) {
			return false;
		}
		for (final IAtom atom : arg.substructure.atoms()) {
			if (!this.substructure.contains(atom)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		return feature;
	}

	@Override
	public boolean equals(Object obj) {
		final int hashOther = ((IFeature) obj).hashCode();
		return (hashCode() == hashOther);
	}
}
