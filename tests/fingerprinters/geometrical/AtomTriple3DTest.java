package fingerprinters.geometrical;

import org.junit.BeforeClass;
import org.junit.Test;

import fingerprinters.SameMoleculeTester;

public class AtomTriple3DTest {
	static SameMoleculeTester tester;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		tester = new SameMoleculeTester(new Encoding3DAtomTriple());
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
