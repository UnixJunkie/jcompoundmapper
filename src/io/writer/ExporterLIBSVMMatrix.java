package io.writer;

import io.reader.RandomAccessMDLReader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.openscience.cdk.interfaces.IAtomContainer;

import tools.progressbar.ProgressBar;

import distance.DistanceMinMax;
import distance.IDistanceMeasure;
import distance.MatrixCalculator;
import fingerprinters.EncodingFingerprint;
import fingerprinters.features.FeatureMap;
import fingerprinters.features.FeatureMapHelper;

public class ExporterLIBSVMMatrix implements IExporter {

	IDistanceMeasure distanceMeasure = new DistanceMinMax();

	public IDistanceMeasure getDistanceMeasure() {
		return distanceMeasure;
	}

	public void setDistanceMeasure(IDistanceMeasure distanceMeasure) {
		this.distanceMeasure = distanceMeasure;
	}

	@Override
	public void export(RandomAccessMDLReader reader, EncodingFingerprint fingerprinter, String label, File outputFile) {
		Long start = System.currentTimeMillis();
		ArrayList<FeatureMap> featuremaps = new ArrayList<FeatureMap>();
		
		ProgressBar progressbar = new ProgressBar(reader.getSize());
		for (int i = 0; i < reader.getSize(); i++) {
			IAtomContainer mol = reader.getMol(i);
			FeatureMap featureMap = new FeatureMap(fingerprinter.getFingerprint(mol));
			String molLabel = (String) mol.getProperty(label);
			if (molLabel != null) {
				featureMap.setLabel(molLabel);
			} else {
				featureMap.setLabel(ExporterHelper.getMolName(mol) + "_INDEX=" + i);
			}
			featuremaps.add(featureMap);
			progressbar.DisplayBar();
		}
		FeatureMapHelper.printFeatureMapStatistics(featuremaps);
		Long end = System.currentTimeMillis();
		System.out.println("Time elapsed (feature generation): " + (end - start) + " ms");
		
		final DecimalFormat df = new DecimalFormat();
		final MatrixCalculator matrixCalculator = new MatrixCalculator();
		matrixCalculator.setSimilaritymeasure(this.distanceMeasure);
		
		start = System.currentTimeMillis();
		final double[][] matrix = matrixCalculator.computeMatrix(featuremaps);
		end = System.currentTimeMillis();
		System.out.println("Time elapsed (matrix computation): " + (end - start) + " ms");

		try {
			final FileWriter fw = new FileWriter(outputFile);
			for (int i = 0; i < matrix.length; i++) {
 				fw.append(featuremaps.get(i).getLabel() + " 0:" + (i + 1));
				for (int j = 0; j < matrix[i].length; j++) {
					fw.append(" " + (j + 1) + ":" + df.format(matrix[i][j]));
				}
				fw.append("\n");
			}
			fw.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
}
