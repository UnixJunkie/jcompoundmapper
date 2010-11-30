package fingerprinters.geometrical;

import java.util.List;

import org.openscience.cdk.interfaces.IAtomContainer;

import tools.moltyping.enumerations.EnumerationsAtomTypes.AtomLabelType;
import fingerprinters.CombinatorialPatternHelper;
import fingerprinters.features.IFeature;

public class Encoding3DPharmacophore3Point extends Encoding3D {

	final CombinatorialPatternHelper patternHelper = new CombinatorialPatternHelper();
	
	public Encoding3DPharmacophore3Point() {
		super.setDistanceCutoff(6);
		super.setAtomLabelType(AtomLabelType.CUSTOM);
	}

	@Override
 	public List<IFeature> getFingerprint(IAtomContainer ac) {
		final int[][] matrix = this.computeDistanceMatrix(ac);
		int distanceCutOff = (int) Math.round(super.getDistanceCutoff());
		List<IFeature> features = patternHelper.getFingerprint3PointPPP(ac, matrix, distanceCutOff);
		return features;
	}

	@Override
	public String getNameOfFingerPrinter() {
		return "3-Point Pharmacophore Pairs 3D";
	}

	@Override
	public void setAtomLabelType(AtomLabelType atomLabelType) {
		// ignore
	}
}
