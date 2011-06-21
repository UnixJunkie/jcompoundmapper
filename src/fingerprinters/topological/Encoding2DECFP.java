package fingerprinters.topological;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
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
import fingerprinters.FingerPrinterException;
import fingerprinters.features.IFeature;
import fingerprinters.topological.features.ECFPFeature;

public class Encoding2DECFP extends Encoding2D {
	private int iteration;
	private ArrayList<IFeature> completeFeatures;
	private IAtomContainer molecule;
	private Map<IAtom,ECFPFeature> featuresOfLastIteration;
	
	@Override
	public ArrayList<IFeature> getFingerprint(IAtomContainer molecule){
		try{
			calculateFingerprint(molecule);
		}catch(Exception e){
			e.printStackTrace();
		}
		return this.completeFeatures;
	}
	
	private void calculateFingerprint(IAtomContainer ac) throws FingerPrinterException, MoltyperException,CDKException{
		this.iteration=0;
		this.completeFeatures=new ArrayList<IFeature>();
		this.molecule=ac;
		this.featuresOfLastIteration = new HashMap<IAtom,ECFPFeature>();
		
		computeInitialIdentifiers();
		
		for(int i=0;i<this.getSearchDepth();i++){
			iteration++;
			computeIteration();
		}
		
		this.featuresOfLastIteration=null;
		this.molecule=null;
	}
	
	private void computeInitialIdentifiers() throws FingerPrinterException, MoltyperException{
		for(IAtom atom: molecule.atoms()){
			int hashCode = this.getAtomLabel(atom).hashCode();
			IMolecule substructure = new Molecule();
			substructure.addAtom(atom);
			for(IBond bond: molecule.getConnectedBondsList(atom)){
				substructure.addBond(bond);
			}
			ECFPFeature ecfpFeature = new ECFPFeature(hashCode, atom ,substructure,this.iteration);
			this.featuresOfLastIteration.put(atom, ecfpFeature);
			completeFeatures.add(ecfpFeature);
		}
	}
	
	private void computeIteration() throws FingerPrinterException, MoltyperException{
		Map<IAtom,ECFPFeature> featuresOfIteration = new HashMap<IAtom, ECFPFeature>();
		List<ECFPFeature> features = new LinkedList<ECFPFeature>();
		
		for(IAtom atom: featuresOfLastIteration.keySet()){
			ECFPFeature feature = computeIterationForAtom(atom);
			features.add(feature);
			featuresOfIteration.put(atom,feature);
		}
		
		removeDuplicateSubstructures(features);
		completeFeatures.addAll(features);
		this.featuresOfLastIteration = featuresOfIteration;
	}
	
	private ECFPFeature computeIterationForAtom(IAtom atom) throws FingerPrinterException, MoltyperException{
		ECFPFeature oldFeature = featuresOfLastIteration.get(atom);
		IMolecule newSubstructure = oldFeature.getNonDeepCloneOfSubstructure();
		List<BondOrderIdentifierTupel> connectivity = new ArrayList<BondOrderIdentifierTupel>();

		for(IAtom connectedAtom: molecule.getConnectedAtomsList(atom)){
			int identifierOfConnectedAtom = featuresOfLastIteration.get(connectedAtom).hashCode();
			connectivity.add(new BondOrderIdentifierTupel(this.getBondOrder(molecule.getBond(atom,connectedAtom)),identifierOfConnectedAtom));
			IMolecule structure = this.featuresOfLastIteration.get(connectedAtom).representedSubstructure();
			for(IAtom a: structure.atoms()){
				if(!newSubstructure.contains(a))
					newSubstructure.addAtom(a);
			}
			for(IBond b: structure.bonds()){
				if(!newSubstructure.contains(b))
					newSubstructure.addBond(b);
			}
		}
		
		int featureHashCode = computeFeatureHash(oldFeature.hashCode(),connectivity);
		ECFPFeature newFeature = new ECFPFeature(featureHashCode, atom, newSubstructure, this.iteration);
		return newFeature;
	}
	
	private int computeFeatureHash(int featureOfCoreAtom, List<BondOrderIdentifierTupel> extensions){
		int[] featureHash = new int[extensions.size()*2+2];
		featureHash[0] = iteration;
		featureHash[1] = featureOfCoreAtom;
		
		Collections.sort(extensions);
		
		for(int i=1;i<=extensions.size();i++){
			BondOrderIdentifierTupel tupel = extensions.get(i-1);
			featureHash[i*2]=tupel.bondOrder;
			featureHash[i*2+1]=tupel.atomIdentifier;
		}

		return Arrays.hashCode(featureHash);
	}
	
	private void removeDuplicateSubstructures(Collection<ECFPFeature> newFeatures){
		Iterator<ECFPFeature> iter = newFeatures.iterator();
		
		while(iter.hasNext()){
			ECFPFeature featureToCheck = iter.next();
			if(hasDuplicate(featureToCheck)){
				iter.remove();
				continue;
			}
			for(ECFPFeature feature: newFeatures){
				if(feature!=featureToCheck && featureToCheck.representsSameSubstructures(feature)){
					if(featureToCheck.hashCode()>=feature.hashCode()){
						iter.remove();
						break;
					}
				}
			}
		}
	}
	
	private boolean hasDuplicate(ECFPFeature feature){
		for(IFeature f: completeFeatures){
			if(feature.representsSameSubstructures((ECFPFeature)f)){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String getNameOfFingerPrinter() {
		return "ECFP";
	}
	
	private class BondOrderIdentifierTupel implements Comparable<BondOrderIdentifierTupel>{
		private int bondOrder;
		private int atomIdentifier;
		
		public BondOrderIdentifierTupel(int bondOrder, int atomIdentifier){
			this.bondOrder=bondOrder;
			this.atomIdentifier=atomIdentifier;
		}
		
		@Override
		public int compareTo(BondOrderIdentifierTupel o) {
			if(this.bondOrder<o.bondOrder)
				return -1;
			else if(this.bondOrder>o.bondOrder)
				return 1;
			else{
				if(this.atomIdentifier<o.atomIdentifier)
					return -1;
				else if(this.atomIdentifier>o.atomIdentifier)
					return 1;
				else
					return 0;
			}
		}
	}
	
	private int getBondOrder(IBond bond){
		if(bond.getFlag(CDKConstants.ISAROMATIC))
			return 4;
		else
			return bond.getOrder().ordinal();
		}
}
