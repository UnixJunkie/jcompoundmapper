package fingerprinters;

import java.util.List;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;

import tools.moltyping.ExtendedAtomAndBondTyper;
import tools.moltyping.MoltyperException;
import tools.moltyping.enumerations.EnumerationsAtomTypes.AtomLabelType;
import fingerprinters.features.IFeature;

public abstract class EncodingFingerprint {

	private final ExtendedAtomAndBondTyper atomTyper = new ExtendedAtomAndBondTyper(AtomLabelType.ELEMENT_NEIGHBOR);

	protected String getAtomLabel(IAtom atom) throws MoltyperException {
		return this.atomTyper.getAtomLabel(atom);
	}

	public AtomLabelType getAtomLabelType() {
		return this.atomTyper.getAtomLabelType();
	}

	protected String getBondLabel(IBond bond) throws MoltyperException {
		return ExtendedAtomAndBondTyper.getBondSymbol(bond);
	}

	public abstract List<IFeature> getFingerprint(IAtomContainer ac);

	public abstract String getNameOfFingerPrinter();

	public boolean isHashable() {
		return false;
	}

	public void setAtomLabelType(AtomLabelType atomLabelType) {
		this.atomTyper.setAtomLabelType(atomLabelType);
	}
	
	public ExtendedAtomAndBondTyper getTyper(){
		return atomTyper;	
	}
}
