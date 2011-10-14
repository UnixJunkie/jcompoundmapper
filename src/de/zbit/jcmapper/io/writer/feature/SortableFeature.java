package de.zbit.jcmapper.io.writer.feature;

import de.zbit.jcmapper.fingerprinters.features.IFeature;

public class SortableFeature implements Comparable<SortableFeature> {
	final IFeature pattern;
	final int hash;

	public SortableFeature(IFeature feature, boolean useAromaticityFlag) {
		this.pattern = feature;
		this.hash = feature.featureToString(useAromaticityFlag).hashCode();
	}

	public int getHash() {
		return hash;
	}

	public double getValue() {
		return pattern.getValue();
	}

	public IFeature getFeature() {
		return pattern;
	}
	
	public String getString(boolean useAromaticityFlag) {
		return this.pattern.featureToString(useAromaticityFlag);
	}

	@Override
	public int compareTo(SortableFeature that) {
		return this.hash - ((SortableFeature) that).getHash();
	}
}