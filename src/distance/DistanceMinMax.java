package distance;

import fingerprinters.features.FeatureMap;

public class DistanceMinMax implements IDistanceMeasure {

	@Override
	public String getIdentifier() {
		return "MinMax Similarity";
	}

	@Override
	public double getSimilarity(FeatureMap a, FeatureMap b) {
		return a.computeMinMaxDistance(b);
	}

}
