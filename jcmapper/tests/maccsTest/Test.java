package maccsTest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.graph.PathTools;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.interfaces.IRingSet;
import org.openscience.cdk.ringsearch.AllRingsFinder;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;

import de.zbit.jcmapper.fingerprinters.features.IFeature;
import de.zbit.jcmapper.fingerprinters.topological.MACCS166;
import de.zbit.jcmapper.fingerprinters.topological.features.PositionFeature;
import de.zbit.jcmapper.io.reader.RandomAccessMDLReader;
import de.zbit.jcmapper.io.writer.ExporterLIBSVMSparse;
import de.zbit.jcmapper.tools.moltyping.maccs.MACCSDefinitionFileReader;
import de.zbit.jcmapper.tools.moltyping.maccs.MACCSSmartsPattern;

public class Test {
	RandomAccessMDLReader sdfReader;
	RandomAccessMDLReader sdfReaderNoH;
	ArrayList<String> ownFingerprints;
	ArrayList<String> nohFingerprints;
	ArrayList<String> mayaFingerprints; 
	ArrayList<String> rdkitFingerprints;
	
	public Test(){
		try {
			this.sdfReader = new RandomAccessMDLReader(new File("./resources/ACE_MM.sdf"));
			this.sdfReaderNoH = new RandomAccessMDLReader(new File("./resources/ACE_MM_noH.sdf"));
			this.mayaFingerprints = new MayaFingerprintReader("./resources/ACE_MMMACCSKeysFP.csv").getFingerprints();
			this.rdkitFingerprints = new RDKitFingerprintReader("./resources/rdkit_fingerprints.txt").getFingerprints();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	public Test(String input){
		this.mayaFingerprints = new MayaFingerprintReader(input).getFingerprints();
	}
	
	//reads all molecules of the given sdf file.
	//computes a fingerprint for every molecule and converts it to a String.
	private void computeFingerprints(){
		//read all molecules with hydrogen atoms
		ArrayList<String> ownFingerprints = new ArrayList<String>();
		RandomAccessMDLReader sdfReader = this.getSdfReader();
		for (int i = 0; i < sdfReader.getSize(); i++){
			IMolecule molecule = sdfReader.getMol(i);
			ArrayList<IFeature> features = (ArrayList<IFeature>) new MACCS166().getFingerprint(molecule);
			String fingerprint = this.toString(features);
			ownFingerprints.add(fingerprint);
		}
		this.setOwnFingerprints(ownFingerprints);
		
		//read all molecules without hydrogen atoms
		ArrayList<String> nohFingerprints = new ArrayList<String>();
		RandomAccessMDLReader sdfReaderNoH = this.getSdfReaderNoH();
		for (int i = 0; i < sdfReaderNoH.getSize(); i++){
			IMolecule molecule = sdfReaderNoH.getMol(i);
			ArrayList<IFeature> features = (ArrayList<IFeature>) new MACCS166().getFingerprint(molecule);
			String fingerprint = this.toString(features);
			nohFingerprints.add(fingerprint);
		}
		this.setNohFingerprints(nohFingerprints);
		
	}
	
	//compares every bit of the two fingerprints.
	private int [] compareFingerprints(String fingerprint1, String fingerprint2, int [] counts ){
		char [] ownFingerArray = fingerprint1.toCharArray();
		char [] mayaFingerArray = fingerprint2.toCharArray();
		for (int i = 0; i < ownFingerArray.length; i++){
			if (!(ownFingerArray[i]==mayaFingerArray[i])){
				counts[i]++;
			}
		}
		return counts;
	}
	
	//computes for every bit, how often they differ
	public String getStatistic(ArrayList<String> fingerprints1, ArrayList<String> fingerprints2){
		String res = "";
		int [] counts = new int [166];
		for (int i = 0; i < fingerprints1.size(); i++){
			counts = this.compareFingerprints(fingerprints1.get(i), fingerprints2.get(i), counts);
		}
		res = res+ "Anzahl der Prints: " + fingerprints1.size()+ '\n';
		List<Integer> sortedCounts = new ArrayList<Integer>();
		for (int i = 0; i < counts.length; i++){
			sortedCounts.add(counts[i]);
			res = res + "Position " + i + ": " + counts[i] + '\n';
		}
		return res;
		
	}
	
	//prints all molecules, which bit at given position is either set or not set(boolean).
	public void getSequences(boolean isSet, int position, List<String> fingerprints){
		for(int i = 0; i < fingerprints.size(); i++){
			char currentBit = fingerprints.get(i).charAt(position);
			if(isSet && currentBit == '1'){
				String smiles = this.getSmilesOfMolecule(i);
				System.out.println(smiles);
			}else if(!isSet && currentBit == '0'){
				String smiles = this.getSmilesOfMolecule(i);
				System.out.println(smiles);
			}
		}
	}
	
	public String getSmilesOfMolecule(int molecule){
		IMolecule mol = this.getSdfReader().getMol(molecule);
		SmilesGenerator generator = new SmilesGenerator();
		generator.setUseAromaticityFlag(true);
		String smiles = generator.createSMILES(mol);
		return smiles;
	}
	
	//converts an fingerprint of PositionFeatures to a String
	private String toString(ArrayList<IFeature> fingerprint){
		//all entries of the storage are set to 0
		int stor[] = new int[166];
		String res = "";
		//the positions of the features are set to 1 
		for (int j = 0; j < fingerprint.size();j++){
			PositionFeature fet = (PositionFeature) fingerprint.get(j);
			stor[fet.hashCode()-1] = (int) fet.getValue();
		}
		//build the string
		for(int i = 0; i < stor.length; i++){
			res = res + stor[i];
		}
		return res;
	}
	
	

	public ArrayList<String> getMayaFingerprints() {
		return mayaFingerprints;
	}

	public void setMayaFingerprints(ArrayList<String> mayaFingerprints) {
		this.mayaFingerprints = mayaFingerprints;
	}

	public RandomAccessMDLReader getSdfReader() {
		return sdfReader;
	}

	public void setSdfReader(RandomAccessMDLReader sdfReader) {
		this.sdfReader = sdfReader;
	}

	public ArrayList<String> getOwnFingerprints() {
		return ownFingerprints;
	}

	public void setOwnFingerprints(ArrayList<String> ownFingerprints) {
		this.ownFingerprints = ownFingerprints;
	}
	
	
	
	public RandomAccessMDLReader getSdfReaderNoH() {
		return sdfReaderNoH;
	}

	public void setSdfReaderNoH(RandomAccessMDLReader sdfReaderNoH) {
		this.sdfReaderNoH = sdfReaderNoH;
	}

	public List<String> getNohFingerprints() {
		return nohFingerprints;
	}

	public void setNohFingerprints(ArrayList<String> nohFingerprints) {
		this.nohFingerprints = nohFingerprints;
	}
	
	public ArrayList<String> getRdkitFingerprints() {
		return rdkitFingerprints;
	}

	 

	public static void main(String [] args) throws CDKException, IOException{
		Test test = new Test();
		ArrayList<String> mayafingerprints = test.getMayaFingerprints();
		ArrayList<String> rdkitfingerprints = test.getRdkitFingerprints();
		test.computeFingerprints();
		ArrayList<String> ownfingerprints = test.getOwnFingerprints();
		//System.out.println(test.getStatistic(ownfingerprints, mayafingerprints));
	
			
		//String res = test.getStatistic(ownfingerprints, rdkitfingerprints);
		//System.out.println(res);
		test.getSequences(true,145,ownfingerprints);
		System.out.println();
		test.getSequences(false,145,ownfingerprints);
		
	
		
	
		
		
	}
}
