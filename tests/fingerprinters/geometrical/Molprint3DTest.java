package fingerprinters.geometrical;

import org.junit.BeforeClass;
import org.junit.Test;

import fingerprinters.GeneralFingerPrintTester;


public class Molprint3DTest {
	static GeneralFingerPrintTester tester;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		tester = new GeneralFingerPrintTester(new Encoding3DMolprint());
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
