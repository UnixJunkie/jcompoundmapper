package io.writer;

import fingerprinters.EncodingFingerprint;
import io.reader.RandomAccessMDLReader;

import java.io.File;

public interface IExporter {
	public void export(RandomAccessMDLReader reader, EncodingFingerprint fingerprinter, String label, File outputFile);
}