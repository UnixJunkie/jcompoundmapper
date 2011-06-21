package fingerprinters.topological;

import org.junit.BeforeClass;
import org.junit.Test;

import fingerprinters.SameMoleculeTester;

public class AtomTriple2DTest {
	static SameMoleculeTester tester;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		tester = new SameMoleculeTester(new Encoding2DAtomTriple());
	}

	@Test
	public void checkLength() {
		tester.checkLength();
	}
	
	@Test
	public void checkFeatures() {
		tester.checkFeatures();
	}
	
	@Test
	public void checkHashedFeatures() {
		tester.checkHashedFeatures();
	}
}
