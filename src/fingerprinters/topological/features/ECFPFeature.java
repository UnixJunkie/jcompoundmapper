package fingerprinters.topological.features;

import org.openscience.cdk.Molecule;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.smiles.SmilesGenerator;

import fingerprinters.features.IFeature;

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
		smile = new SmilesGenerator().createSMILES(substructure);
		return smile;
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

