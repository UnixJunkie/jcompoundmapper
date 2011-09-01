package de.zbit.jcmapper.fingerprinters.topological.features;

import java.util.ArrayList;

import org.openscience.cdk.Molecule;
import org.openscience.cdk.PseudoAtom;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.smiles.SmilesGenerator;

import de.zbit.jcmapper.fingerprinters.FingerPrinterException;
import de.zbit.jcmapper.fingerprinters.features.IFeature;


public class ECFPFeature implements IFeature {
	
	private IAtom coreAtom;
	private int feature;
	private IMolecule substructure;
	private int iterationNumber;
	
	public ECFPFeature(int feature, IAtom coreAtom, IMolecule substructure, int iterationNumber){
		this.feature = feature;
		this.substructure = substructure;
		this.coreAtom=coreAtom;
		this.iterationNumber=iterationNumber;
	}
	
	@Override
	public int hashCode() {
		return feature;
	}

	@Override
	public int compareTo(IFeature arg0) {
		ECFPFeature ecfp_arg = (ECFPFeature)arg0;
		if(this.feature > ecfp_arg.feature)
			return 1;
		if(this.feature < ecfp_arg.feature)
			return -1;
		else
			return 0;
	}

	@Override
	public String featureToString() {
		String smile;
		ArrayList<DanglingBond> danglingBonds = this.detectDanglingBonds();
		
		IAtom[] tempAtoms = new IAtom[danglingBonds.size()];
		final IMolecule substructureClone = this.getNonDeepCloneOfSubstructure();
		
		for (int i = 0; i < danglingBonds.size(); i++) {
			final DanglingBond dangling = danglingBonds.get(i);
			final IBond bond = dangling.getBond();
			tempAtoms[i] = dangling.getConnectedAtom();
			final IAtom pseudoAtom = new PseudoAtom();
			bond.setAtom(pseudoAtom, dangling.getConnectedAtomPosition());
			substructureClone.addAtom(pseudoAtom);
			substructureClone.addBond(dangling.getBond());
		}
		
		smile = new SmilesGenerator().createSMILES(substructureClone);

		for (int i = 0; i < danglingBonds.size(); i++) {
			final DanglingBond connectivity = danglingBonds.get(i);
			final IBond bond = connectivity.getBond();
			bond.setAtom(tempAtoms[i], connectivity.getConnectedAtomPosition());
		}
		return smile;
	}
	
	private ArrayList<DanglingBond> detectDanglingBonds(){
		ArrayList<DanglingBond> danglingBonds = new ArrayList<DanglingBond>();
		try{
			for(IBond bond: substructure.bonds()){
				if(!substructure.contains(bond.getAtom(0))){
					danglingBonds.add(new DanglingBond(bond, bond.getAtom(0)));
					continue;
				}if(!substructure.contains(bond.getAtom(1)))
					danglingBonds.add(new DanglingBond(bond, bond.getAtom(1)));
			}
		}catch(FingerPrinterException e){
			e.printStackTrace();
			return null;
		}
		return danglingBonds;
	}
	
	public IAtom getCoreAtom(){
		return coreAtom;
	}
	
	public IMolecule getNonDeepCloneOfSubstructure(){
		IMolecule clone = new Molecule();
		for(IBond bond: this.substructure.bonds())
			clone.addBond(bond);
		
		for(IAtom atom: this.substructure.atoms())
			clone.addAtom(atom);
		
		return clone;
	}
	
	public boolean representsSameSubstructures(ECFPFeature arg){
		if(arg.substructure.getAtomCount()!=this.substructure.getAtomCount())
			return false;
		for(IAtom atom: arg.substructure.atoms()){
			if(!this.substructure.contains(atom))
				return false;
		}
		return true;
	}
	
	public int getIterationNumber(){
		return iterationNumber;
	}

	@Override
	public double getValue() {
		return 1;
	}

	@Override
	public Iterable<IAtom> representedAtoms() {
		return substructure.atoms();
	}

	@Override
	public Iterable<IBond> representedBonds() {
		return substructure.bonds();
	}
	
	
	public IMolecule representedSubstructure(){
		return this.substructure;
	}
}

