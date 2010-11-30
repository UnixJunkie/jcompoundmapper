package io.writer;

import io.reader.RandomAccessMDLReader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openscience.cdk.interfaces.IAtomContainer;

import fingerprinters.EncodingFingerprint;
import fingerprinters.features.IFeature;

public class ExporterKeyWeka extends ExporterLinear {
	private String[] initkeys = { "AA", "AD", "AL", "AN", "AP", "DD", "DL", "DN", "DP", "LL", "LN", "LP", "NN", "NP",
			"PP" };

	@Override
	protected int writeFingerprint(ArrayList<IFeature> fingerprint, FileWriter fw, String label) {
		try {
			fw.append(label);
			for (int key = 0; key < fingerprint.size(); key++) {
				fw.append("," + fingerprint.get(key).getValue());
			}
			fw.append("\n");
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return 0;
	}


	@Override
	public void export(RandomAccessMDLReader reader, EncodingFingerprint fingerprinter, String label, File outputFile) {

		try {
			final FileWriter fw = new FileWriter(outputFile);
			writeHeader(fw,fingerprinter,label,reader);
			List<IFeature> features;
 
			for (int i = 0; i < reader.getSize(); i++) {
				IAtomContainer mol = reader.getMol(i);
				features = fingerprinter.getFingerprint(mol);
				String molLabel = (String) mol.getProperty(label);
				if (molLabel == null) {
					molLabel = "?";
				}
				writeFingerprint((ArrayList<IFeature>) features, fw, molLabel);
			}
			fw.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void writeHeader(FileWriter fw, EncodingFingerprint fingerprinter,String label,RandomAccessMDLReader reader) {
		try {
 			fw.append("@relation\tx\n");
			fw.append("@ATTRIBUTE\tlabel\tNUMERIC\n");

			for (String key : initkeys) {
				fw.append("@ATTRIBUTE\t" + key + "\tNUMERIC\n");
			}

			fw.append("\n@DATA\n");
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
}