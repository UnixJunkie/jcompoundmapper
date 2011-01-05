package fingerprinters;

import fingerprinters.FingerPrinterException.ErrorCode;
import fingerprinters.geometrical.Encoding3DAtomPair;
import fingerprinters.geometrical.Encoding3DAtomTriple;
import fingerprinters.geometrical.Encoding3DCATS;
import fingerprinters.geometrical.Encoding3DMolprint;
import fingerprinters.geometrical.Encoding3DPharmacophore2Point;
import fingerprinters.geometrical.Encoding3DPharmacophore3Point;
import fingerprinters.topological.Encoding2DAllPaths;
import fingerprinters.topological.Encoding2DAllShortestPath;
import fingerprinters.topological.Encoding2DAtomPair;
import fingerprinters.topological.Encoding2DAtomTriple;
import fingerprinters.topological.Encoding2DCATS;
import fingerprinters.topological.Encoding2DECFP;
import fingerprinters.topological.Encoding2DECFPVariant;
import fingerprinters.topological.Encoding2DLocalAtomEnvironment;
import fingerprinters.topological.Encoding2DMolprint;
import fingerprinters.topological.Encoding2DPharmacophore2Point;
import fingerprinters.topological.Encoding2DPharmacophore3Point;
import fingerprinters.topological.Encoding2DSHEDKey;

public class FingerPrinterFactory {

	public static EncodingFingerprint getFingerprinter(FingerprintType type) throws FingerPrinterException{
		if (type == FingerprintType.RAD2D) {
			return new Encoding2DMolprint();
		} else if (type == FingerprintType.RAD3D) {
			return new Encoding3DMolprint();
		} else if (type == FingerprintType.ASP) {
			return new Encoding2DAllShortestPath();
		} else if (type == FingerprintType.AP2D) {
			return new Encoding2DAtomPair();
		} else if (type == FingerprintType.AT2D) {
			return new Encoding2DAtomTriple();
		} else if (type == FingerprintType.CATS2D) {
			return new Encoding2DCATS();
		} else if (type == FingerprintType.DFS) {
			return new Encoding2DAllPaths();
		} else if (type == FingerprintType.ECFP) {
			return new Encoding2DECFP();
		} else if (type == FingerprintType.ECFPVariant) {
			return new Encoding2DECFPVariant();
		} else if (type == FingerprintType.LSTAR) {
			return new Encoding2DLocalAtomEnvironment();
		} else if (type == FingerprintType.PHAP2POINT2D) {
			return new Encoding2DPharmacophore2Point();
		} else if (type == FingerprintType.PHAP3POINT2D) {
			return new Encoding2DPharmacophore3Point();
		} else if (type == FingerprintType.PHAP2POINT3D) {
			return new Encoding3DPharmacophore2Point();
		} else if (type == FingerprintType.PHAP3POINT3D) {
			return new Encoding3DPharmacophore3Point();
		} else if (type == FingerprintType.AP3D) {
			return new Encoding3DAtomPair();
		} else if (type == FingerprintType.AT3D) {
			return new Encoding3DAtomTriple();
		} else if (type == FingerprintType.SHED) {
			return new Encoding2DSHEDKey();
		} else if (type == FingerprintType.CATS3D) {
			return new Encoding3DCATS();
		} else if (type == FingerprintType.ECFP) {
			return new Encoding2DECFPVariant();
		}
		throw new FingerPrinterException(ErrorCode.UNKNOWN_FINGERPRINTER_TYPE,FingerPrinterFactory.class.toString(),type.toString());
	}
	
	public static enum FingerprintType {
		DFS,

		ASP,

		AP2D,

		AT2D,

		AP3D,

		AT3D,

		CATS2D,

		CATS3D,

		PHAP2POINT2D,

		PHAP3POINT2D,

		PHAP2POINT3D,

		PHAP3POINT3D,

		ECFP,
		
		ECFPVariant,

		LSTAR,
		
		SHED,

		RAD2D,

		RAD3D;
	}
}
