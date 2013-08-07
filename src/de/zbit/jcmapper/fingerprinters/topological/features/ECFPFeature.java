package de.zbit.jcmapper.fingerprinters.topological.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.openscience.cdk.Molecule;
import org.openscience.cdk.PseudoAtom;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.smiles.SmilesGenerator;

import de.zbit.jcmapper.fingerprinters.EncodingFingerprint;
import de.zbit.jcmapper.fingerprinters.FingerPrinterException;
import de.zbit.jcmapper.fingerprinters.features.IFeature;
import de.zbit.jcmapper.fingerprinters.topological.Encoding2DECFP.BondOrderIdentifierTupel;
import de.zbit.jcmapper.tools.moltyping.MoltyperException;

public class ECFPFeature implements IFeature {
	
	private static boolean substructureHash=false; 
	
	private IAtom coreAtom;
	private int feature;
	private IMolecule substructure;
	private int iterationNumber;
	private int parent;
	private List<BondOrderIdentifierTupel> connections;
	private EncodingFingerprint encodingFingerprint;
	
	public ECFPFeature(EncodingFingerprint encodingFingerprint,IAtom coreAtom, IMolecule substructure, int iterationNumber, int parent, List<BondOrderIdentifierTupel> connections) {
		this.substructure = substructure;
		this.coreAtom = coreAtom;
		this.iterationNumber = iterationNumber;
		this.parent = parent;
		this.connections = connections;
		this.encodingFingerprint=encodingFingerprint;

		//needs to be the last function call in the constructor
		this.feature=0;
		try {
			this.feature=computeFeatureHash();
		} catch (MoltyperException e) {
			//silently do nothing
		}
	}

	private int computeFeatureHash() throws MoltyperException{
		int hashCode=0;
		if(substructureHash){
			int atomSize=substructure.getAtomCount();
			int bondSize=substructure.getBondCount();
			int[] atombondHash = new int[atomSize+bondSize];
			for(int i=0;i<atomSize;i++){
				IAtom atom=substructure.getAtom(i);
				atombondHash[i]=this.encodingFingerprint.getAtomLabel(atom).hashCode();
			}
			for(int i=0;i<bondSize;i++){
				IBond bond=substructure.getBond(i);
				atombondHash[i+atomSize]=this.encodingFingerprint.getBondLabel(bond).hashCode();
			}
			Arrays.sort(atombondHash);
			
			hashCode=Arrays.hashCode(atombondHash);
		}
		else{
			int connectionsSize=0;
			if (this.connections!=null){
				connectionsSize=this.connections.size();
			}
			int[] featureHash = new int[connectionsSize*2+2];
			featureHash[0] = this.iterationNumber;
			featureHash[1] = this.parent;
			//System.out.println("this.iterationNumber="+this.iterationNumber);
			//System.out.println("this.parent="+this.parent);
			
			if (this.connections!=null){
				Collections.sort(this.connections);
				for(int i=1;i<=this.connections.size();i++){
					BondOrderIdentifierTupel tupel = this.connections.get(i-1);
					featureHash[i*2]=tupel.bondOrder;
					featureHash[i*2+1]=tupel.atomIdentifier;
					//System.out.println("featureHash["+i+"*2]=tupel.bondOrder="+tupel.bondOrder);
					//System.out.println("featureHash["+i+"*2+1]=tupel.atomIdentifier="+tupel.atomIdentifier);
				}
			}
			//System.out.println(featureToString(true));
			hashCode=Arrays.hashCode(featureHash);
		}
		
		//System.out.println("hashCode="+hashCode);
		return hashCode;
	}

	public static boolean isSubstructureHash() {
		return substructureHash;
	}

	public static void setSubstructureHash(boolean substructureHash) {
		ECFPFeature.substructureHash = substructureHash;
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
	public String featureToString(boolean useAromaticFlag) {
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
		
		smile = new SmilesGenerator(useAromaticFlag).createSMILES(substructureClone);

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

	public int getParent() {
		return parent;
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

