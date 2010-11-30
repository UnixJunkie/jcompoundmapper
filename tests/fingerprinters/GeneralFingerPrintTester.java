package fingerprinters;

import fingerprinters.features.FeatureMap;
import fingerprinters.features.IFeature;
import io.reader.RandomAccessMDLReader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import junit.framework.Assert;
import distance.DistanceMinMax;
import distance.DistanceTanimoto;
import distance.IDistanceMeasure;

public class GeneralFingerPrintTester {

	public RandomAccessMDLReader reader = null;
	public final ArrayList<ArrayList<IFeature>> features = new ArrayList<ArrayList<IFeature>>();
	public final ArrayList<FeatureMap> featuremaps = new ArrayList<FeatureMap>();
	public final EncodingFingerprint fingerprinter;

	public GeneralFingerPrintTester(EncodingFingerprint fingerPrinter) {
		this.fingerprinter = fingerPrinter;
		try {
			reader = new RandomAccessMDLReader(new File("./resources/ACE_MM.sdf"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		long millisStart = System.currentTimeMillis();
		for (int i = 0; i < reader.getSize(); i++) {
			List<IFeature> featuresOfMolecule = fingerprinter.getFingerprint(reader.getMol(i));
			Assert.assertNotNull(featuresOfMolecule);
			Assert.assertTrue(featuresOfMolecule.size()>0);
			features.add((ArrayList<IFeature>)featuresOfMolecule);
		}
		long millisEnd = System.currentTimeMillis();
		System.out.println("MolMapper = " + fingerprinter.getNameOfFingerPrinter()
				+ ", time for generating features = " + (millisEnd - millisStart) + " ms");

		for (int i = 0; i < features.size(); i++) {
			FeatureMap featureMap = new FeatureMap(features.get(i));
			BitSet hFingerprint = featureMap.getHashedFingerPrint(1024);
			System.out.println(hFingerprint.toString());
			Assert.assertNotNull(featureMap);
			featuremaps.add(featureMap);
		}
	}

	public double[][] checkMatrix() {
		final int dim = featuremaps.size();
		IDistanceMeasure distance = new DistanceMinMax();
		double[][] matrix = new double[dim][dim];
		for (int i = 0; i < dim; i++) {
			for (int j = i; j < dim; j++) {
				double simIJ = distance.getSimilarity(featuremaps.get(i), featuremaps.get(j));
				double simJI = distance.getSimilarity(featuremaps.get(j), featuremaps.get(i));
				Assert.assertEquals(simIJ, simJI);
				matrix[i][j] = simIJ;
				matrix[j][i] = simIJ;
				Assert.assertFalse(matrix[i][j] < 0);
				Assert.assertFalse(matrix[i][j] > 1);
				Assert.assertFalse(Double.isNaN(matrix[i][j]));
			}
		}
		return matrix;
	}

	public void benchmarkMatrix() {
		final int dim = featuremaps.size();
		IDistanceMeasure distance = new DistanceTanimoto();
		long millisStart = System.currentTimeMillis();
		for (int i = 0; i < dim; i++) {
			for (int j = i; j < dim; j++) {
				distance.getSimilarity(featuremaps.get(i), featuremaps.get(j));
			}
		}
		long millisEnd = System.currentTimeMillis();
		System.out.println("MolMapper = " + fingerprinter.getNameOfFingerPrinter()
				+ ", time for distance matrix ("+ distance.getIdentifier() +  ") = "+(millisEnd-millisStart)+" ms");
	}
}
