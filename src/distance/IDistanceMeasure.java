package distance;

import fingerprinters.features.FeatureMap;

public interface IDistanceMeasure {
	public String getIdentifier();

	public double getSimilarity(FeatureMap a, FeatureMap b);
}
