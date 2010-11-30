package fingerprinters.geometrical;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import tools.moltyping.enumerations.EnumerationsAtomTypes.AtomLabelType;

import fingerprinters.GeneralFingerPrintTester;

public class AtomPair3DTest {
	static GeneralFingerPrintTester tester;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Encoding3DAtomPair fingerprint = new Encoding3DAtomPair();
		tester = new GeneralFingerPrintTester(fingerprint);
	}

	@Test
	public void computeMatrix() {
		 tester.checkMatrix();
  	}

	@Test 
	public void benchmarkMatrix() {
		tester.benchmarkMatrix();
	}
	
	@Test
	public void checkParameter(){
		Encoding3DAtomPair fingerprint = new Encoding3DAtomPair();
		fingerprint.setAtomLabelType(AtomLabelType.CDK_ATOM_TYPES);
		Assert.assertEquals(fingerprint.getAtomLabelType(), AtomLabelType.CDK_ATOM_TYPES); 
		fingerprint.setDistanceCutoff(5.5);
		Assert.assertEquals(fingerprint.getDistanceCutoff(), 5.5, 0.0);
		fingerprint.setStretchingFactor(5.5);
		Assert.assertEquals(fingerprint.getDistanceCutoff(), 5.5, 0.0);
	}
}
