package de.zbit.jcmapper.io.reader;

import java.util.Map;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.AtomTypeManipulator;

public final class MoleculePreprocessor {

	/**
	 * standard preparation protocol: remove hydrogens, types, ring detection
	 * 
	 * @param mol
	 * @throws Exception
	 */
	public static Molecule prepareMoleculeRemoveHydrogens(Molecule mol) {
		boolean errorFlag=false;
		Molecule molOrig=mol;
		try {
			AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol);
			CDKHueckelAromaticityDetector.detectAromaticity(mol);
			mol = addExplicitHydrogens(mol);
			
		} catch (final Exception e) {
			//TODO: This is not clean, it is better throwing an Exception and catching it in ALL implementing classes.
			System.out.println("An error ocurred while typing structure, using unprocessed molecule. "+e.getMessage());
			e.printStackTrace();
			errorFlag=true;
 		}
		mol = removeHydrogens(mol);
		if(errorFlag){
			mol=molOrig;
		}
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
			System.out.println("An error ocurred while typing structure:"+e.getMessage());
			//e.printStackTrace();
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
	
	/**
	 * add hydrogens
	 * 
	 * @param mol
	 * @throws CDKException
	 */
	private static Molecule addExplicitHydrogens(Molecule mol) throws CDKException{
		
		CDKAtomTypeMatcher matcher = CDKAtomTypeMatcher.getInstance(mol.getBuilder());
		for (IAtom atom : mol.atoms()) {
		     IAtomType type = matcher.findMatchingAtomType(mol, atom);
		     try{
		    	 AtomTypeManipulator.configure(atom, type);
		     }
		     catch(IllegalArgumentException e){
		    	 throw new CDKException(e.toString()+" for atom "+atom.getAtomicNumber()+" "+atom.getSymbol());
		     }
		   }
		CDKHydrogenAdder hydrogenAdder = CDKHydrogenAdder.getInstance(mol.getBuilder());
		hydrogenAdder.addImplicitHydrogens(mol);
		AtomContainerManipulator.convertImplicitToExplicitHydrogens(mol);
		
		return mol;
		
	}
	
	
	
}
