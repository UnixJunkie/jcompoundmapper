package de.zbit.jcmapper.fingerprinters.topological;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.openscience.cdk.Molecule;
import org.openscience.cdk.graph.PathTools;
import org.openscience.cdk.graph.matrix.AdjacencyMatrix;
import org.openscience.cdk.interfaces.IAtomContainer;

import de.zbit.jcmapper.fingerprinters.features.IFeature;
import de.zbit.jcmapper.fingerprinters.features.NumericStringFeature;
import de.zbit.jcmapper.tools.moltyping.PharmacophorePointAssigner;
import de.zbit.jcmapper.tools.moltyping.pharmacophore.PotentialPharmacophorePoint;



public class Encoding2DSHEDKey extends Encoding2D {

	private final PharmacophorePointAssigner pAssigner = new PharmacophorePointAssigner();
	String[] initkeys = { "AA", "AD", "AL", "AN", "AP", "DD", "DL", "DN", "DP", "LL", "LN", "LP", "NN", "NP", "PP" };

	private HashMap<String, ArrayList<SHEDAtomContainer>> findPharmacophores(Molecule mol) {
		HashMap<Integer, Vector<PotentialPharmacophorePoint>> pharmacophorePoints = pAssigner
				.getPharmacophorePoints(mol);
		HashMap<String, ArrayList<SHEDAtomContainer>> result = new HashMap<String, ArrayList<SHEDAtomContainer>>();
		for (int atomNr = 0, n = mol.getAtomCount(); atomNr < n; atomNr++) {
			if (pharmacophorePoints.get(atomNr) == null) {
				continue;
			}
			for (PotentialPharmacophorePoint pharmacophorePoint : pharmacophorePoints.get(atomNr)) {			
				if (!result.containsKey(pharmacophorePoint.getPharmacophoreType())) {
					result.put(pharmacophorePoint.getPharmacophoreType(), new ArrayList<SHEDAtomContainer>());
				}
				result.get(pharmacophorePoint.getPharmacophoreType()).add(new SHEDAtomContainer(atomNr));
			}
		}				
		return result;
	}

	private HashMap<String, Integer[]> calculateDistribution(
			HashMap<String, ArrayList<SHEDAtomContainer>> pharmacophores, Molecule mol) {
		final int[][] admat = AdjacencyMatrix.getMatrix(mol);
		final int[][] shortest_path = PathTools.computeFloydAPSP(admat);
		int count = 0;
		HashMap<String, Integer[]> result = new HashMap<String, Integer[]>();
		Set<String> keys = pharmacophores.keySet();

		for (String key1 : keys) {
			if (!pharmacophores.containsKey(key1)) {
				continue;
			}

			for (String key2 : keys) {
				if (!pharmacophores.containsKey(key2)) {
					continue;
				}
				String combinedKey = key1 + key2;
				if (!result.containsKey(combinedKey)) {
					result.put(combinedKey, new Integer[21]);
					for (int i = 0; i < 21; i++) {
						result.get(combinedKey)[i] = 0;
					}
				}
				for (SHEDAtomContainer ac1 : pharmacophores.get(key1)) {
					for (SHEDAtomContainer ac2 : pharmacophores.get(key2)) {
						int value = shortest_path[ac1.getAtomNumber()][ac2.getAtomNumber()];
						
						if (value == 0) {
							continue;
						}
						
						if (value > 20) {
							value = 20;
						}
						result.get(combinedKey)[value] += 1;
						count += 1;
					}
				}
			}
		}
		return result;

	}

	private class SHEDAtomContainer {

		private int atomNumber;

		public SHEDAtomContainer(int number) {
			this.atomNumber = number;
		}

		public int getAtomNumber() {
			return atomNumber;
		}

	}

	private double calculateSHE(Integer[] distribution) {
		double result = 0;
		double sum = 0;
		int count = 0;
		for (int value : distribution) {
			sum += value;
		}
		if (!(sum == 0)) {
			for (int value : distribution) {
				if (value != 0) {
					count += 1;
					double pValue = ((double) value) / sum;
					result += (pValue) * Math.log(pValue);					
				}
			}
		}

		if (result < 0) {
			result=-result;
		}
		return Math.pow(Math.E,result);
		
		
	
	}

	@Override
	public List<IFeature> getFingerprint(IAtomContainer ac) {

		HashMap<String, Integer[]> distribution = calculateDistribution(findPharmacophores((Molecule) ac),
				(Molecule) ac);

		List<IFeature> result = new ArrayList<IFeature>();

		for (String initkey : initkeys) {
			double keyvalue = 0;			
			if (distribution.containsKey(initkey)) 			
			{			
				keyvalue = calculateSHE(distribution.get(initkey));				
			}			
			result.add(new NumericStringFeature(initkey, keyvalue));
		}			
		return result;
	}
	@Override
	public String getNameOfFingerPrinter() {
		return "SHED";
	}
}
