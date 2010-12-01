package io.writer.feature;

import fingerprinters.features.IFeature;

public class SortableFeature implements Comparable {
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
	public int compareTo(Object that) {
		return this.hash - ((SortableFeature) that).getHash();
	}
}