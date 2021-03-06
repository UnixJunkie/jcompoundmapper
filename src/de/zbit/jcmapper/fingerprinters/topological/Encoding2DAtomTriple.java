package de.zbit.jcmapper.fingerprinters.topological;

import java.util.ArrayList;
import java.util.List;

import org.openscience.cdk.graph.PathTools;
import org.openscience.cdk.graph.matrix.AdjacencyMatrix;
import org.openscience.cdk.interfaces.IAtomContainer;

import de.zbit.jcmapper.fingerprinters.CombinatorialPatternHelper;
import de.zbit.jcmapper.fingerprinters.features.IFeature;


public class Encoding2DAtomTriple extends Encoding2D {

	public Encoding2DAtomTriple() {
		super.setSearchDepth(5);
	}

	private final CombinatorialPatternHelper combPatternHelper = new CombinatorialPatternHelper();
	
	@Override
	public List<IFeature> getFingerprint(IAtomContainer ac) {
		List<IFeature> features = new ArrayList<IFeature>();
		final int[][] admat = AdjacencyMatrix.getMatrix(ac);
		final int[][] shortest_path = PathTools.computeFloydAPSP(admat);
		int distanceCutOff = super.getSearchDepth();
		features = combPatternHelper.getFingerprint3Point(ac, shortest_path, distanceCutOff, super.getTyper());
		return features;
	}

	@Override
	public String getNameOfFingerPrinter() {
		return "3-Point Atom Pairs 2D";
	}
}
