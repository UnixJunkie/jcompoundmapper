package tools.tree.trie;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.Test;

import de.zbit.jcmapper.fingerprinters.EncodingFingerprint;
import de.zbit.jcmapper.fingerprinters.features.FeatureMap;
import de.zbit.jcmapper.fingerprinters.geometrical.Encoding3D;
import de.zbit.jcmapper.fingerprinters.geometrical.Encoding3DAtomPair;
import de.zbit.jcmapper.fingerprinters.geometrical.Encoding3DAtomTriple;
import de.zbit.jcmapper.fingerprinters.geometrical.Encoding3DCATS;
import de.zbit.jcmapper.fingerprinters.geometrical.Encoding3DMolprint;
import de.zbit.jcmapper.fingerprinters.geometrical.Encoding3DPharmacophore2Point;
import de.zbit.jcmapper.fingerprinters.geometrical.Encoding3DPharmacophore3Point;
import de.zbit.jcmapper.fingerprinters.topological.DepthFirstSearch;
import de.zbit.jcmapper.fingerprinters.topological.Encoding2D;
import de.zbit.jcmapper.fingerprinters.topological.Encoding2DAllShortestPath;
import de.zbit.jcmapper.fingerprinters.topological.Encoding2DAtomPair;
import de.zbit.jcmapper.fingerprinters.topological.Encoding2DAtomTriple;
import de.zbit.jcmapper.fingerprinters.topological.Encoding2DCATS;
import de.zbit.jcmapper.fingerprinters.topological.Encoding2DECFPVariant;
import de.zbit.jcmapper.fingerprinters.topological.Encoding2DLocalAtomEnvironment;
import de.zbit.jcmapper.fingerprinters.topological.Encoding2DMolprint;
import de.zbit.jcmapper.fingerprinters.topological.Encoding2DPharmacophore2Point;
import de.zbit.jcmapper.fingerprinters.topological.Encoding2DPharmacophore3Point;
import de.zbit.jcmapper.fingerprinters.topological.Encoding2DSHEDKey;
import de.zbit.jcmapper.io.reader.RandomAccessMDLReader;
import de.zbit.jcmapper.tools.moltyping.enumerations.EnumerationsAtomTypes.AtomLabelType;
import de.zbit.jcmapper.tools.tree.trie.TrieFeatureMap;



public class TrieFeatureMapTest {
	
	@Test
	public void printFeatureMap(){
		RandomAccessMDLReader sdReader = null;
		try {
			sdReader = new RandomAccessMDLReader(new File("./resources/CID_2244_asprin.sdf"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		exportTrie(sdReader, new DepthFirstSearch(), new File("c:\\temp\\Aspirin-DFS.gml"));
		exportTrie(sdReader, new Encoding2DAllShortestPath(), new File("c:\\temp\\Aspirin-ASP.gml"));
		exportTrie(sdReader, new Encoding2DAtomPair(), new File("c:\\temp\\Aspirin-AtomPair2D.gml"));
		exportTrie(sdReader, new Encoding2DAtomTriple(), new File("c:\\temp\\Aspirin-AtomTriple2D.gml"));
		exportTrie(sdReader, new Encoding3DAtomPair(), new File("c:\\temp\\Aspirin-AtomPair3D.gml"));
		exportTrie(sdReader, new Encoding3DAtomTriple(), new File("c:\\temp\\Aspirin-AtomTriple3D.gml"));
		exportTriePharmacophore(sdReader, new Encoding2DCATS(), new File("c:\\temp\\Aspirin-CATS2DKey.gml"));
		exportTriePharmacophore(sdReader, new Encoding3DCATS(), new File("c:\\temp\\Aspirin-CATS3D.gml"));
		exportTriePharmacophore(sdReader, new Encoding2DPharmacophore2Point(), new File("c:\\temp\\Aspirin-Pharmacophore2Point2D.gml"));
		exportTriePharmacophore(sdReader, new Encoding2DPharmacophore3Point(), new File("c:\\temp\\Aspirin-Pharmacophore3Point2D.gml"));
		exportTriePharmacophore(sdReader, new Encoding3DPharmacophore2Point(), new File("c:\\temp\\Aspirin-Pharmacophore2Point3D.gml"));
		exportTriePharmacophore(sdReader, new Encoding3DPharmacophore3Point(), new File("c:\\temp\\Aspirin-Pharmacophore3Point3D.gml"));
		exportTrie(sdReader, new Encoding2DECFPVariant(), new File("c:\\temp\\Aspirin-ExtendedConnectivity.gml"));
		exportTrie(sdReader, new Encoding2DLocalAtomEnvironment(), new File("c:\\temp\\Aspirin-LocalAtomEnvironmentsStar.gml"));
		exportTrie(sdReader, new Encoding2DMolprint(), new File("c:\\temp\\Aspirin-Molprint2D.gml"));
		exportTrie(sdReader, new Encoding3DMolprint(), new File("c:\\temp\\Aspirin-Molprint3D.gml"));
		exportTriePharmacophore(sdReader, new Encoding2DSHEDKey(), new File("c:\\temp\\Aspirin-SHED.gml"));
	}
	
	private void exportTrie(RandomAccessMDLReader sdReader, EncodingFingerprint fingerprinter, File out){
		fingerprinter.setAtomLabelType(AtomLabelType.ELEMENT_SYMBOL);
		if(fingerprinter instanceof Encoding2D)
			((Encoding2D) fingerprinter).setSearchDepth(3);
		if(fingerprinter instanceof Encoding3D)
			((Encoding3D) fingerprinter).setDistanceCutoff(2);
		
		FeatureMap featureMap = new FeatureMap(fingerprinter.getFingerprint(sdReader.getMol(0)));
		featureMap.print();
		TrieFeatureMap test = new TrieFeatureMap(featureMap);
		String gml = test.getGMLString();
		try {
			FileWriter fw = new FileWriter(out);
			fw.append(gml);
			fw.close();
		} catch (IOException e) {
 			e.printStackTrace();
		}
	}
	
	private void exportTriePharmacophore(RandomAccessMDLReader sdReader, EncodingFingerprint fingerprinter, File out){
		fingerprinter.setAtomLabelType(AtomLabelType.ELEMENT_SYMBOL);
		if(fingerprinter instanceof Encoding2D)
			((Encoding2D) fingerprinter).setSearchDepth(8);
		if(fingerprinter instanceof Encoding3D)
			((Encoding3D) fingerprinter).setDistanceCutoff(2);
		
		FeatureMap featureMap = new FeatureMap(fingerprinter.getFingerprint(sdReader.getMol(0)));
		featureMap.print();
		TrieFeatureMap test = new TrieFeatureMap(featureMap);
		String gml = test.getGMLStringPPP();
		try {
			FileWriter fw = new FileWriter(out);
			fw.append(gml);
			fw.close();
		} catch (IOException e) {
 			e.printStackTrace();
		}
	}
}
