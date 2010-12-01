package io.writer;

import io.reader.RandomAccessMDLReader;
import io.writer.feature.SortableFeature;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import org.openscience.cdk.interfaces.IAtomContainer;

import tools.progressbar.ProgressBar;

import fingerprinters.EncodingFingerprint;
import fingerprinters.features.FeatureMap;
import fingerprinters.features.IFeature;

public class ExporterSparseNominalWeka implements IExporter {

	/**
	 * generates a hash map with all features found in the data set
	 * 
	 * @param reader
	 * @param fingerprinter
	 * @return
	 */
	private TreeMap<IFeature, Integer> collectGlobalFeatures(RandomAccessMDLReader reader, EncodingFingerprint fingerprinter) {
		// first round: collect all features
		TreeMap<IFeature, Integer> globalFeatureHashMap = new TreeMap<IFeature, Integer>();
		for (int i = 0; i < reader.getSize(); i++) {
			IAtomContainer mol = reader.getMol(i);
			FeatureMap featureMap = new FeatureMap(fingerprinter.getFingerprint(mol));
			Set<IFeature> keys = featureMap.getKeySet();
			Iterator<IFeature> featureIterator = keys.iterator();
			while (featureIterator.hasNext()) {
				IFeature currentFeature = featureIterator.next();
				globalFeatureHashMap.put(currentFeature, 0);
			}
		}

		// assign indices 1,...,n to the n features found
		Set<IFeature> featuresGlobal = globalFeatureHashMap.keySet();
		Iterator<IFeature> featuresIter = featuresGlobal.iterator();
		
		TreeMap<IFeature, Integer> globalFeatureHashMapFinal = new TreeMap<IFeature, Integer>();
		int index = 1;
		while (featuresIter.hasNext()) {
			globalFeatureHashMapFinal.put(featuresIter.next(), index);
			index++;
		}

		return globalFeatureHashMapFinal;
	}

	/*
	 * write a ARFF header
	 */
	private void writeHeader(TreeMap<IFeature, Integer> map, FileWriter fw) throws IOException {
		Set<IFeature> keys = map.keySet();
		fw.append("@relation	MOLECULE\n");
		Iterator<IFeature> iter = keys.iterator();
		while (iter.hasNext()) {
			fw.append("@ATTRIBUTE\t+" + iter.next().featureToString() + "\t{0,1}\n");
		}
		fw.append("@ATTRIBUTE	LABEL	{}\n\n");
		fw.append("@DATA\n");
	}

	@Override
	public void export(RandomAccessMDLReader reader, EncodingFingerprint fingerprinter, String label, File outputFile) {

		int collisions = 0;
		java.util.Locale.setDefault(java.util.Locale.ENGLISH);
		DecimalFormat df = new DecimalFormat();
		Long start = System.currentTimeMillis();
		ProgressBar progressBar = new ProgressBar(reader.getSize());

		try {
			final FileWriter fw = new FileWriter(outputFile);
			TreeMap<IFeature, Integer> globalFeatureHashMap = collectGlobalFeatures(reader, fingerprinter);
			writeHeader(globalFeatureHashMap, fw);

			for (int i = 0; i < reader.getSize(); i++) {
				IAtomContainer mol = reader.getMol(i);
				FeatureMap featureMap = new FeatureMap(fingerprinter.getFingerprint(mol));
				String molLabel = (String) mol.getProperty(label);
				if (molLabel != null) {
					featureMap.setLabel(molLabel);
				} else {
					featureMap.setLabel("?");
				}

				ArrayList<SortableFeature> Features = new ArrayList<SortableFeature>();
				Set<IFeature> keys = featureMap.getKeySet();
				for (IFeature feature : keys) {
					if (feature instanceof IFeature) {
						Features.add(new SortableFeature(feature));
					}
				}

				Collections.sort(Features);

				fw.append("{");
				for (SortableFeature feature : Features) {
					int index = globalFeatureHashMap.get(feature);
					fw.append(" " + index + " 1,");
				}				
				int lastIndex = globalFeatureHashMap.keySet().size();
				fw.append(featureMap.getLabel());
				fw.append("}");
				fw.append("\n");
				progressBar.DisplayBar();
			}
			Long end = System.currentTimeMillis();
			System.out.println("Time elapsed: " + (end - start) + " ms");
			fw.close();
			System.out.println("Collisions:" + collisions);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

}