package fingerprinters.geometrical;

import org.junit.BeforeClass;
import org.junit.Test;

import fingerprinters.GeneralFingerPrintTester;

public class Pharmacophore3Point3DTest {
	static GeneralFingerPrintTester tester;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		tester = new GeneralFingerPrintTester(new Encoding3DPharmacophore3Point());
	}

	@Test
	public void computeMatrix() {
		tester.checkMatrix();
	}

	@Test
	public void benchmarkMatrix() {
		tester.benchmarkMatrix();

	}
}
