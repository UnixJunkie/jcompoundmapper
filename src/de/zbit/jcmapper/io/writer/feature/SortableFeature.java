package de.zbit.jcmapper.io.writer.feature;

import de.zbit.jcmapper.fingerprinters.features.IFeature;

public class SortableFeature implements Comparable<SortableFeature> {
	final IFeature pattern;
	final int hash;

	public SortableFeature(IFeature feature) {
		this.pattern = feature;
		this.hash = feature.featureToString().hashCode();
	}

	public int getHash() {
		return hash;
	}

	public double getValue() {
		return pattern.getValue();
	}

	public String getString() {
		return this.pattern.featureToString();
	}

	@Override
	public int compareTo(SortableFeature that) {
		return this.hash - ((SortableFeature) that).getHash();
	}
}