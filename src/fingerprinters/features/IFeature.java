package fingerprinters.features;

public interface IFeature extends Comparable<IFeature>{

	public abstract String featureToString();

	public abstract double getValue();

	@Override
	public abstract int hashCode();
	
	@Override
	public abstract boolean equals(Object feature);

}