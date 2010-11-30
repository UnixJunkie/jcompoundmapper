package fingerprinters.topological;

import java.util.List;

import org.openscience.cdk.graph.PathTools;
import org.openscience.cdk.graph.matrix.AdjacencyMatrix;
import org.openscience.cdk.interfaces.IAtomContainer;

import tools.moltyping.enumerations.EnumerationsAtomTypes.AtomLabelType;
import fingerprinters.CombinatorialPatternHelper;
import fingerprinters.features.IFeature;

public class Encoding2DPharmacophore3Point extends Encoding2D {

	final CombinatorialPatternHelper patternHelper = new CombinatorialPatternHelper();
	
	public Encoding2DPharmacophore3Point() {
		super.setSearchDepth(5);
		super.setAtomLabelType(AtomLabelType.CUSTOM);
	}

	public List<IFeature> getFingerprint(IAtomContainer ac) {
		final int[][] admat = AdjacencyMatrix.getMatrix(ac);
		final int[][] shortestPathMatrix = PathTools.computeFloydAPSP(admat);
		int distanceCutOff = (int) Math.round(super.getSearchDepth());
		List<IFeature> features = patternHelper.getFingerprint3PointPPP(ac, shortestPathMatrix, distanceCutOff);
		return features;
	}

	@Override
	public String getNameOfFingerPrinter() {
		return "3-Point Pharmacophore Pairs 2D";
	}

	@Override
	public void setAtomLabelType(AtomLabelType atomLabelType) {
		//ignore
	}
}
