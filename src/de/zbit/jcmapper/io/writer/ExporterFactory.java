package de.zbit.jcmapper.io.writer;

public class ExporterFactory {
	public static IExporter getExporter(ExporterType type) {
		if (type == ExporterType.LIBSVM_MATRIX) {
			return new ExporterLIBSVMMatrix();
		} else if (type == ExporterType.LIBSVM_SPARSE) {
			return new ExporterLIBSVMSparse();
		} else if (type == ExporterType.FULL_CSV) {
			return new ExporterFullFingerprintCSV();
		} else if (type == ExporterType.WEKA_HASHED) {
			return new ExporterHashWeka();
		} else if (type == ExporterType.STRING_PATTERNS) {
			return new ExporterSparseString();
		} else if (type == ExporterType.BENCHMARKS) {
			return new ExporterBenchmark();
		} else if (type == ExporterType.WEKA_NOMINAL) {
			return new ExporterSparseNominalWeka();
		}
		return new ExporterLIBSVMSparse();
	}

	public static enum ExporterType {
		LIBSVM_SPARSE, LIBSVM_MATRIX, FULL_CSV, STRING_PATTERNS, WEKA_HASHED, WEKA_NOMINAL, BENCHMARKS;
	}

	public static ExporterType getExporterType(int index) {
		if (index == ExporterType.LIBSVM_MATRIX.ordinal()) {
			return ExporterType.LIBSVM_MATRIX;
		} else if (index == ExporterType.LIBSVM_SPARSE.ordinal()) {
			return ExporterType.LIBSVM_SPARSE;
		} else if (index == ExporterType.BENCHMARKS.ordinal()) {
			return ExporterType.BENCHMARKS;
		} else if (index == ExporterType.FULL_CSV.ordinal()) {
			return ExporterType.FULL_CSV;
		} else if (index == ExporterType.STRING_PATTERNS.ordinal()) {
			return ExporterType.STRING_PATTERNS;
		}else if (index == ExporterType.WEKA_HASHED.ordinal()) {
			return ExporterType.WEKA_HASHED;
		}else if (index == ExporterType.WEKA_NOMINAL.ordinal()) {
			return ExporterType.WEKA_NOMINAL;
		}
		return ExporterType.LIBSVM_SPARSE;
	}
}
