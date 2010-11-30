package tools.moltyping.enumerations;

public class EnumerationsAtomTypes {
	public static enum AtomLabelType {
 		CDK_ATOM_TYPES,
		/** element symbol + #atom neighbours */
		ELEMENT_NEIGHBOR,
		/** element symbol + ring flag +#atom neighbours */
		ELEMENT_NEIGHBOR_RING,
		/** Only element symbols */
		ELEMENT_SYMBOL,
		CUSTOM,
		DAYLIGHT_INVARIANT,
		DAYLIGHT_INVARIANT_RING,
	}

	public static AtomLabelType getAtomLabeltypeForIndex(int index) {
		if (index == AtomLabelType.CDK_ATOM_TYPES.ordinal()) {
			return AtomLabelType.CDK_ATOM_TYPES;
		}
		if (index == AtomLabelType.ELEMENT_SYMBOL.ordinal()) {
			return AtomLabelType.ELEMENT_SYMBOL;
		}
		if (index == AtomLabelType.ELEMENT_NEIGHBOR.ordinal()) {
			return AtomLabelType.ELEMENT_NEIGHBOR;
		}
		if (index == AtomLabelType.ELEMENT_NEIGHBOR_RING.ordinal()) {
			return AtomLabelType.ELEMENT_NEIGHBOR_RING;
		}
		if (index == AtomLabelType.DAYLIGHT_INVARIANT.ordinal()) {
			return AtomLabelType.DAYLIGHT_INVARIANT;
		}
		if (index == AtomLabelType.DAYLIGHT_INVARIANT_RING.ordinal()) {
			return AtomLabelType.DAYLIGHT_INVARIANT_RING;
		}
	 
		// else return default
		return AtomLabelType.ELEMENT_NEIGHBOR;
	}
}
