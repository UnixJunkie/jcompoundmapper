package distance;


public class DistanceFactory {

	public static IDistanceMeasure getDistance(DistanceType type) {
		if (type == DistanceType.TANIMOTO) {
			return new DistanceTanimoto();
		} else if (type == DistanceType.MINMAX) {
			return new DistanceMinMax();
		}
		// default
		return new DistanceTanimoto();

	}

	public static enum DistanceType {
		TANIMOTO, MINMAX;
	}

	public static DistanceType getFingerPrintType(int index) {
		if (index == DistanceType.TANIMOTO.ordinal()) {
			return DistanceType.TANIMOTO;
		} else if (index == DistanceType.MINMAX.ordinal()) {
			return DistanceType.MINMAX;
		}
		// default
		return DistanceType.TANIMOTO;
	}
}
