package de.zbit.jcmapper.fingerprinters.topological.features;

import java.util.LinkedList;
import java.util.List;

import org.openscience.cdk.PseudoAtom;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.smiles.SmilesGenerator;

public class ECFPVariantFeature extends ECFPFeature {

	private final DanglingBond[] connectivityBonds;
	
	public ECFPVariantFeature(int feature, IAtom coreAtom, IMolecule substructure, DanglingBond[] connectivityBonds, int iterationNumber) {
		super(feature, coreAtom, substructure, iterationNumber);
		this.connectivityBonds = connectivityBonds;
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

	public DanglingBond getDanglingBond(int i) {
		return this.connectivityBonds[i];
	}

	public int numberOfDanglingBonds() {
		return this.connectivityBonds.length;
	}

	@Override
	public double getValue() {
		return 1;
	}

	public boolean hasEqualSubstructure(ECFPVariantFeature arg) {
		if (arg.representedSubstructure().getAtomCount() != this.representedSubstructure().getAtomCount()) {
			return false;
		}
		for (final IAtom atom : arg.representedSubstructure().atoms()) {
			if (!this.representedSubstructure().contains(atom)) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public Iterable<IBond> representedBonds(){
		List<IBond> bonds = new LinkedList<IBond>();
		for(IBond bond: this.representedSubstructure().bonds()){
			bonds.add(bond);
		}
		for(DanglingBond bond: connectivityBonds){
			bonds.add(bond.getBond());
		}
		return bonds;
	}
}
