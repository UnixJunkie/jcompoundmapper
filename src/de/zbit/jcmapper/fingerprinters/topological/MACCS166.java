package de.zbit.jcmapper.fingerprinters.topological;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openscience.cdk.Atom;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.graph.PathTools;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.cdk.interfaces.IRingSet;
import org.openscience.cdk.ringsearch.AllRingsFinder;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;

import de.zbit.jcmapper.fingerprinters.features.IFeature;
import de.zbit.jcmapper.fingerprinters.topological.features.PositionFeature;
import de.zbit.jcmapper.tools.moltyping.maccs.MACCSDefinitionFileReader;
import de.zbit.jcmapper.tools.moltyping.maccs.MACCSSmartsPattern;

public class MACCS166 extends Encoding2D {

	private List<MACCSSmartsPattern> SMARTS;
	static int counter = 0;

	public MACCS166() {
		super();
		MACCSDefinitionFileReader reader = new MACCSDefinitionFileReader();
		this.SMARTS = reader.readMACCSDefinitions();
	}

	@Override
	public List<IFeature> getFingerprint(IAtomContainer ac) {
		ArrayList<IFeature> fingerprint = new ArrayList<IFeature>();
		List<MACCSSmartsPattern> SMARTS = this.getSMARTS();
		try {
			final SMARTSQueryTool sqt = new SMARTSQueryTool("C");
			for (MACCSSmartsPattern pattern : SMARTS) {
				if(pattern.getSMARTS().equals("?"))
					continue;
				//set all bits, which can be done with SMARTS
				sqt.setSmarts(pattern.getSMARTS());
				if (sqt.matches(ac)) {
					List<List<Integer>> matchingAtoms = sqt.getUniqueMatchingAtoms();
					if(matchingAtoms.size() >= pattern.getFrequency()){
					PositionFeature currentFeature = new PositionFeature(pattern.getPosition());
					fingerprint.add(currentFeature);
					}
				}
			}
		} catch (CDKException e) {
			e.printStackTrace();
		}
		//the following bits has to be done outside of smarts because the matching is incorrect
		
		//bit 22:  3 M ring
		if(this.checkBit22(ac)){
			PositionFeature feature22 = new PositionFeature(22);
			fingerprint.add(feature22);
		}
		//bit 120: Heterocyclic atom > 1
		if(this.checkBit120(ac)){
			PositionFeature feature120 = new PositionFeature(120);
			fingerprint.add(feature120);
		}
		//bit 121: N Heterocycle
		if (this.checkBit121(ac)){
			PositionFeature feature121 = new PositionFeature(121);
			fingerprint.add(feature121);
		}
		//bit 16: QAA@1
		if(this.checkBit16(ac)){
			PositionFeature feature16 = new PositionFeature(16);
			fingerprint.add(feature16);
		}
		//bit 137: Heterocycle
		if(this.checkBit137(ac)){
			PositionFeature feature137 = new PositionFeature(137);
			fingerprint.add(feature137);
		}
		//bit 79: NAAN
		if(this.checkBit79(ac)){
			PositionFeature feature79 = new PositionFeature(79);
			fingerprint.add(feature79);
		}
		//bit 101: 8M ring or larger
		if(this.checkBit101(ac)){
			PositionFeature feature101 = new PositionFeature(101);
			fingerprint.add(feature101);
		}
		
		// the two bits 125 and 166 has to be done outside of SMARTS.

		// bit 125: aromatic ring > 1
		if (this.checkBit125(ac)) {
			PositionFeature feature125 = new PositionFeature(125); 
			fingerprint.add(feature125);
		}
		
		//bit 166: fragments
		if(this.chekBit166(ac)){
			PositionFeature feature166 = new PositionFeature(166);
			fingerprint.add(feature166);
		}
		// the last two bits 1 and 44 are completely ignored, because no exact
		// definition was found.
		return fingerprint;
	}
	
	
	//bit 166: fragments
	private boolean chekBit166(IAtomContainer ac) {
		boolean setBit166 = false;
		IMoleculeSet part = ConnectivityChecker.partitionIntoMolecules(ac);
		if (part.getMoleculeCount() > 1){
			setBit166 = true;
		}
		
		return setBit166;
	}

	// bit 125: aromatic ring > 1
	private boolean checkBit125(IAtomContainer ac) {
		boolean setBit125 = false;
		AllRingsFinder ringFinder = new AllRingsFinder();
		try {
			//get all rings of the molecule.
			IRingSet rings = ringFinder.findAllRings(ac);
			int ringCount = 0;
			//check if the rings are aromatic.
			for (int i = 0; i < rings.getAtomContainerCount(); i++) {
				IAtomContainer ring = rings.getAtomContainer(i);
				boolean allAromatic = true;
				Iterator<IBond> bonds = ring.bonds().iterator();
				//the ring is aromatic, if all bonds are aromatic.
				while (bonds.hasNext()) {
					IBond bond = bonds.next();
					if (!bond.getFlag(CDKConstants.ISAROMATIC)) {
						allAromatic = false;
						break;
					}
				}
				if (allAromatic)
					ringCount++;
				if (ringCount > 1) {
					setBit125 = true;
					break;
				}
			}
		} catch (CDKException e) {
			e.printStackTrace();
		}
		return setBit125;
	}
	
	//bit 22:  3 M ring
	private boolean checkBit22(IAtomContainer ac){
		boolean setBit22 = false;
		AllRingsFinder ringFinder = new AllRingsFinder();
		try {
			//find all rings
			IRingSet rings = ringFinder.findAllRings(ac);
			//check if there are rings with three atoms
			for (int i = 0; i < rings.getAtomContainerCount(); i++) {
				IAtomContainer currentRing = rings.getAtomContainer(i);
				int numberOfRingAtoms = currentRing.getAtomCount();
				if(numberOfRingAtoms == 3){
					setBit22 = true;
					break;
				}
			}
			
		} catch (CDKException e) {
			e.printStackTrace();
		}
		return setBit22;
	}
	
	//bit 120: Heterocyclic atom > 1
	private boolean checkBit120(IAtomContainer ac){
		boolean setBit120 = false;
		int counter = 0;
		ArrayList<IAtom> heterocyclicAtoms = new ArrayList<IAtom>();
		AllRingsFinder ringFinder = new AllRingsFinder();
		try {
			//find all rings
			IRingSet rings = ringFinder.findAllRings(ac);
			//collect all heterocyclic atoms in a list
			for (int i = 0; i < rings.getAtomContainerCount(); i++) {
				IAtomContainer currentRing = rings.getAtomContainer(i);
				for(int j = 0; j < currentRing.getAtomCount(); j++){
					IAtom currentAtom = currentRing.getAtom(j);
					if(!(currentAtom.getSymbol().equals("C"))){
						if((!heterocyclicAtoms.contains(currentAtom))){
							heterocyclicAtoms.add(currentAtom);
						}
					}
				}
			}
		}  catch (CDKException e) {
			e.printStackTrace();
		}
		counter = heterocyclicAtoms.size();
		if(counter >= 2){
			setBit120 = true;
		}
		return setBit120;
	}
	
	//bit 121: N Heterocycle
	private boolean checkBit121(IAtomContainer ac){
		boolean setBit121 = false;
		ArrayList<IAtom> heterocyclicAtoms = new ArrayList<IAtom>();
		AllRingsFinder ringFinder = new AllRingsFinder();
		try {
			//find all rings
			IRingSet rings = ringFinder.findAllRings(ac);
			//search after one N atom at the rings
			for (int i = 0; i < rings.getAtomContainerCount(); i++) {
				//if one N atom was found, stop
				if(setBit121) break;
				IAtomContainer currentRing = rings.getAtomContainer(i);
				//search after one N atom
				for(int j = 0; j < currentRing.getAtomCount(); j++){
					IAtom currentAtom = currentRing.getAtom(j);
					if(currentAtom.getSymbol().equals("N")){
						setBit121 = true;
						break;
					}
				}
			}
		}  catch (CDKException e) {
			e.printStackTrace();
		}
		return setBit121;
	}
	//bit 16: QAA@1
	private boolean checkBit16(IAtomContainer ac){
		boolean setBit16 = false;
		int atomCounter = 0;
		AllRingsFinder ringFinder = new AllRingsFinder();
		try {
			//find all rings
			IRingSet rings = ringFinder.findAllRings(ac);
			//check if there are rings with three atoms
			for (int i = 0; i < rings.getAtomContainerCount(); i++) {
				IAtomContainer currentRing = rings.getAtomContainer(i);
				int numberOfRingAtoms = currentRing.getAtomCount();
				if(numberOfRingAtoms == 3){
					//count every atom of the ring, which isn't a C or H atom.
					for(int j = 0; j < 3; j++){
						String currentAtomSymbol = currentRing.getAtom(j).getSymbol();
						if(!(currentAtomSymbol.equals("C") || currentAtomSymbol.equals("H"))){
							atomCounter++;
						}
					}
					//check if exactly one atom isn't a C or an H atom.
					if (atomCounter == 1){
						setBit16 = true;
						break;
					} else {
						atomCounter = 0;
					}
				}
			}
			
		} catch (CDKException e) {
			e.printStackTrace();
		}
		return setBit16;
	}
	
	//bit 137: Heterocycle 
	private boolean checkBit137(IAtomContainer ac){
		boolean setBit137 = false;
		AllRingsFinder ringFinder = new AllRingsFinder();
		try {
			//find all rings
			IRingSet rings = ringFinder.findAllRings(ac);
			for (int i = 0; i < rings.getAtomContainerCount(); i++) {
				if(setBit137) break;
				IAtomContainer currentRing = rings.getAtomContainer(i);
				//check if at least one atom of the current ring isn't a C atom.
				for(int j = 0; j < currentRing.getAtomCount(); j++){
					IAtom currentAtom = currentRing.getAtom(j);
					if(!(currentAtom.getSymbol().equals("C"))){
						setBit137 = true;
						break;
					}
				}
			}
		}  catch (CDKException e) {
			e.printStackTrace();
		}
		return setBit137;
	}
	
	//bit 101: 8M Ring or larger
	private boolean checkBit101(IAtomContainer ac){
		boolean setBit101 = false;
		AllRingsFinder ringFinder = new AllRingsFinder();
		try {
			//find all rings
			IRingSet rings = ringFinder.findAllRings(ac);
			//search if one ring exists with eight or more atoms
			for (int i = 0; i < rings.getAtomContainerCount(); i++){
				IAtomContainer currentRing = rings.getAtomContainer(i);
				int ringSize = currentRing.getAtomCount();
				if(ringSize >= 8){
					setBit101 = true;
					break;
				}
			}
		} catch (CDKException e) {
			e.printStackTrace();
		}
		return setBit101;
	}
	

	//bit 79: NAAN
	private boolean checkBit79(IAtomContainer ac){
		boolean setBit79 = false;
		//search all N-atoms.
		for(int i = 0; i < ac.getAtomCount(); i++){
			IAtom currentAtom = ac.getAtom(i);
			if(currentAtom.getSymbol().equals("N")){
				//if an N-atom was found, search all paths with length 3
				List<List<IAtom>> paths = PathTools.getPathsOfLength(ac, currentAtom, 3);
				//check the paths
				//wrong paths are deleted
				paths = this.checkPaths(paths);
				if(!paths.isEmpty()){
					setBit79 = true;
					break;
				}
			}
		}
		return setBit79;
	}
	
	//check the paths. All incorrect paths are deleted.
	private List<List<IAtom>> checkPaths(List<List<IAtom>> paths){
		//copy the paths
		ArrayList<List<IAtom>> pathsCopy = new ArrayList<List<IAtom>>(paths);
		//check for every path, if the last atom of the path is an N-atom
		for(int i = 0; i < paths.size(); i++){
			List<IAtom> currentPath = paths.get(i);
			IAtom lastAtomOfPath = currentPath.get(currentPath.size()-1);
			//if the last atom isn't an N-atom, this path will be removed.
			if(!(lastAtomOfPath.getSymbol().equals("N"))){
				pathsCopy.remove(currentPath);
			}
		}
		return pathsCopy;
		
	}

	@Override
	public String getNameOfFingerPrinter() {
		return "MACCS166";
	}

	public List<MACCSSmartsPattern> getSMARTS() {
		return SMARTS;
	}
	
}
