package distance;

import java.util.List;

import tools.progressbar.ProgressBar;

import fingerprinters.features.FeatureMap;

public class MatrixCalculator {
	private IDistanceMeasure similaritymeasure = new DistanceTanimoto();

	public double[][] computeMatrix(List<FeatureMap> samples) {

		final int n = samples.size();
		ProgressBar progressBar = new ProgressBar(n);

		final double[][] matrix = new double[n][n];
		for (int i = 0; i < n; i++) {
			final FeatureMap featureMapI = samples.get(i);
			for (int j = i; j < n; j++) {
				final FeatureMap featureMapJ = samples.get(j);
				final double sim = this.similaritymeasure.getSimilarity(featureMapI, featureMapJ);
				matrix[i][j] = sim;
				matrix[j][i] = sim;
			}
			progressBar.DisplayBar();
		}
		return matrix;
	}

	public void setSimilaritymeasure(IDistanceMeasure similaritymeasure) {
		this.similaritymeasure = similaritymeasure;
	}

}
