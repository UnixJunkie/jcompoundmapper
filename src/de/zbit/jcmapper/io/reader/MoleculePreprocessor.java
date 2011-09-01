package de.zbit.jcmapper.io.reader;

import java.util.Map;

import org.openscience.cdk.Molecule;
import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

public final class MoleculePreprocessor {

	/**
	 * standard preparation protocol: remove hydrogens, types, ring detection
	 * 
	 * @param mol
	 * @throws Exception
	 */
	public static Molecule prepareMoleculeRemoveHydrogens(Molecule mol) {
		try {
			AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol);
			CDKHueckelAromaticityDetector.detectAromaticity(mol);
		} catch (final Exception e) {
			System.out.println("An error ocurred while typing structure.");
 		}
		mol = removeHydrogens(mol);
		return mol;
	}

	
	/**
	 * types the molecule, leaves the hydrogens attached
	 * @param mol
	 * @return
	 */
	public static Molecule prepareMoleculeConserveHydrogens(Molecule mol) {
		try {
			AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol);
			CDKHueckelAromaticityDetector.detectAromaticity(mol);
		} catch (final Exception e) {
			System.out.println("An error ocurred while typing structure.");
		}
		return mol;
	}


	/**
	 * remove hydrogens
	 * 
	 * @param mol
	 * @throws Exception
	 */
	private static Molecule removeHydrogens(Molecule mol) {
		final Map<Object, Object> map = mol.getProperties();
		mol = (Molecule) AtomContainerManipulator.removeHydrogensPreserveMultiplyBonded(mol);
		mol.setProperties(map);
		return mol;
	}
}
