package tools.tree.trie;

import fingerprinters.EncodingFingerprint;
import fingerprinters.features.FeatureMap;
import fingerprinters.geometrical.Encoding3DAtomPair;
import fingerprinters.geometrical.Encoding3DAtomTriple;
import fingerprinters.geometrical.Encoding3DCATS;
import fingerprinters.geometrical.Encoding3D;
import fingerprinters.geometrical.Encoding3DMolprint;
import fingerprinters.geometrical.Encoding3DPharmacophore2Point;
import fingerprinters.geometrical.Encoding3DPharmacophore3Point;
import fingerprinters.topological.Encoding2DAllShortestPath;
import fingerprinters.topological.Encoding2DAtomPair;
import fingerprinters.topological.Encoding2DAtomTriple;
import fingerprinters.topological.Encoding2DCATS;
import fingerprinters.topological.DepthFirstSearch;
import fingerprinters.topological.Encoding2DExtendedConnectivity;
import fingerprinters.topological.Encoding2D;
import fingerprinters.topological.Encoding2DLocalAtomEnvironment;
import fingerprinters.topological.Encoding2DMolprint;
import fingerprinters.topological.Encoding2DPharmacophore2Point;
import fingerprinters.topological.Encoding2DPharmacophore3Point;
import fingerprinters.topological.Encoding2DSHEDKey;
import io.reader.RandomAccessMDLReader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.Test;

import tools.moltyping.enumerations.EnumerationsAtomTypes.AtomLabelType;


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
		exportTrie(sdReader, new Encoding2DExtendedConnectivity(), new File("c:\\temp\\Aspirin-ExtendedConnectivity.gml"));
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
