package io.writer;

import io.reader.RandomAccessMDLReader;

import java.io.FileWriter;
import java.util.ArrayList;

import fingerprinters.EncodingFingerprint;
import fingerprinters.features.IFeature;

public abstract class ExporterLinear implements IExporter {

	protected abstract void writeHeader(FileWriter fw,EncodingFingerprint fingerprinter,String label,RandomAccessMDLReader reader);

	protected abstract int writeFingerprint(ArrayList<IFeature> fingerprint, FileWriter fw, String label);
	
}
