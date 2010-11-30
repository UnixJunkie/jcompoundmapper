package fingerprinters.features;

public class NumericStringFeature implements IFeature {

	private final String feature;
	private final double value;
	private final int hashCode;

	public NumericStringFeature(String feature, double value) {
		this.feature = feature;
		this.value = value;
		this.hashCode = feature.hashCode();
	}

	@Override
	public int compareTo(IFeature o) {
		return this.feature.compareTo(((NumericStringFeature) o).feature);
	}

	@Override
	public boolean equals(Object obj) {
		final int hashOther = ((IFeature) obj).hashCode();
		return (this.hashCode == hashOther);
	}

	@Override
	public String featureToString() {
		return this.feature;
	}

	@Override
	public double getValue() {
		return this.value;
	}

	@Override
	public int hashCode() {
		return this.hashCode;
	}
}
