package fingerprinters.topological;

import org.junit.BeforeClass;
import org.junit.Test;

import fingerprinters.GeneralFingerPrintTester;

public class AtomTriple2DTest {
	static GeneralFingerPrintTester tester;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		tester = new GeneralFingerPrintTester(new Encoding2DAtomTriple());
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
