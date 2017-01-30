package org.cheminfo.hook.moldraw;

import java.awt.Color;
/**
* This class keeps information about atoms
*/


public class AtomProperties {

	final static String spaces="                    ";

	private static final AtomDefinition periodicTable[] = {
		new AtomDefinition("H","Hydrogen",1.00794,Color.gray),
		new AtomDefinition("He","Helium",4.0026),
		new AtomDefinition("Li","Lithium",6.941),
		new AtomDefinition("Be","Beryllium",9.01218),
		new AtomDefinition("B","Boron",10.81),
		new AtomDefinition("C","Carbon",12.011),
		new AtomDefinition("N","Nitrogen",14.0067, Color.blue),
		new AtomDefinition("O","Oxygen",15.9994, Color.red),
		new AtomDefinition("F","Fluorine",18.998404),
		new AtomDefinition("Ne","Neon",20.179),
		new AtomDefinition("Na","Sodium",22.98977),
		new AtomDefinition("Mg","Magnesium",24.305),
		new AtomDefinition("Al","Aluminium",26.98154),
		new AtomDefinition("Si","Silicon",28.0855),
		new AtomDefinition("P","Phosphorus",30.97376),
		new AtomDefinition("S","Sulfur",32.06),
		new AtomDefinition("Cl","Chlorine",35.453),
		new AtomDefinition("Ar","Argon",39.948),
		new AtomDefinition("K","Potassium",39.0983),
		new AtomDefinition("Ca","Calcium",40.08),
		new AtomDefinition("Sc","Scandium",44.9559),
		new AtomDefinition("Ti","Titanium",47.88),
		new AtomDefinition("V","Vanadium",50.9415),
		new AtomDefinition("Cr","Chromium",51.996),
		new AtomDefinition("Mn","Manganese",54.938),
		new AtomDefinition("Fe","Iron",55.847),
		new AtomDefinition("Co","Cobalt",58.9332),
		new AtomDefinition("Ni","Nickel",58.69),
		new AtomDefinition("Cu","Copper",63.546),
		new AtomDefinition("Zn","Zinc",65.38),
		new AtomDefinition("Ga","Gallium",69.72),
		new AtomDefinition("Ge","Germanium",72.59),
		new AtomDefinition("As","Arsenic",74.9216),
		new AtomDefinition("Se","Selenium",78.96),
		new AtomDefinition("Br","Bromine",79.904),
		new AtomDefinition("Kr","Krypton",83.8),
		new AtomDefinition("Rb","Rubidium",85.4678),
		new AtomDefinition("Sr","Strontium",87.62),
		new AtomDefinition("Y","Yttrium",88.9059),
		new AtomDefinition("Zr","Zirconium",91.22),
		new AtomDefinition("Nb","Niobium",92.9064),
		new AtomDefinition("Mo","Molybdenum",95.94),
		new AtomDefinition("Tc","Technetium",98),
		new AtomDefinition("Ru","Ruthenium",101.07),
		new AtomDefinition("Rh","Rhodium",102.9055),
		new AtomDefinition("Pd","Palladium",106.42),
		new AtomDefinition("Ag","Silver",107.8682),
		new AtomDefinition("Cd","Cadmium",112.41),
		new AtomDefinition("In","Indium",114.82),
		new AtomDefinition("Sn","Tin",118.69),
		new AtomDefinition("Sb","Antimony",121.75),
		new AtomDefinition("Te","Tellurium",127.6),
		new AtomDefinition("I","Iodine",126.9045),
		new AtomDefinition("Xe","Xenon",131.29),
		new AtomDefinition("Cs","Caesium",132.9054),
		new AtomDefinition("Ba","Barium",137.33),
		new AtomDefinition("La","Lanthanum",138.9055),
		new AtomDefinition("Ce","Cerium",140.12),
		new AtomDefinition("Pr","Praseodymium",140.9077),
		new AtomDefinition("Nd","Neodymium",144.24),
		new AtomDefinition("Pm","Promethium",145),
		new AtomDefinition("Sm","Samarium",150.36),
		new AtomDefinition("Eu","Europium",151.96),
		new AtomDefinition("Gd","Gadolinium",157.25),
		new AtomDefinition("Tb","Terbium",158.9254),
		new AtomDefinition("Dy","Dysprosium",162.5),
		new AtomDefinition("Ho","Holmium",164.9304),
		new AtomDefinition("Er","Erbium",167.26),
		new AtomDefinition("Tm","Thulium",168.9342),
		new AtomDefinition("Yb","Ytterbium",173.04),
		new AtomDefinition("Lu","Lutetium",174.967),
		new AtomDefinition("Hf","Hafnium",178.49),
		new AtomDefinition("Ta","Tantalum",180.9479),
		new AtomDefinition("W","Tungsten",183.85),
		new AtomDefinition("Re","Rhenium",186.207),
		new AtomDefinition("Os","Osmium",190.2),
		new AtomDefinition("Ir","Iridium",192.22),
		new AtomDefinition("Pt","Platinum",195.08),
		new AtomDefinition("Au","Gold",196.9665),
		new AtomDefinition("Hg","Mercury",200.59),
		new AtomDefinition("Tl","Thallium",204.383),
		new AtomDefinition("Pb","Lead",207.2),
		new AtomDefinition("Bi","Bismuth",208.9804),
		new AtomDefinition("Po","Polonium",209),
		new AtomDefinition("At","Astatine",210),
		new AtomDefinition("Rn","Radon",222),
		new AtomDefinition("Fr","Francium",223),
		new AtomDefinition("Ra","Radium",226.0254),
		new AtomDefinition("Ac","Actinium",227.0278),
		new AtomDefinition("Th","Thorium",232.0381),
		new AtomDefinition("Pa","Protactinium",231.0359),
		new AtomDefinition("U","Uranium",238.0289),
		new AtomDefinition("Np","Neptunium",237.0482),
		new AtomDefinition("Pu","Plutonium",244),
		new AtomDefinition("Am","Americium",243),
		new AtomDefinition("Cm","Curium",247),
		new AtomDefinition("Bk","Berkelium",247),
		new AtomDefinition("Cf","Californium",251),
		new AtomDefinition("Es","Einsteinium",252),
		new AtomDefinition("Fm","Fermium",257),
		new AtomDefinition("Md","Mendelevium",258),
		new AtomDefinition("No","Nobelium",259),
		new AtomDefinition("Lr","Lawrencium",260)
	};


	public static String getAtomName(int atomNumber) {
		return periodicTable[atomNumber-1].atomName;
	}
	
	public static double getAtomMass(int atomNumber) {
		return periodicTable[atomNumber-1].atomMass;
	}
	
	public static int getAtomIntMass(int atomNumber) {
		return (int)Math.round(periodicTable[atomNumber-1].atomMass);
	}
	
	public static Color getAtomColor(int atomNumber) {
		return periodicTable[atomNumber-1].atomColor;
	}
	
	/** Returns the atom symbol based on the position in the periodic
	 * table of elements.
	 */
	
	public static String getAtomSymbol(int atomNumber) {
		return periodicTable[atomNumber-1].atomSymbol;
	}
	
	/** Returns the atom symbol based on the position in the periodic
	 * table of elements. Add number of spaces in front to fit the size.
	 * Maximal size is 20.
	 */
	
	public static String getAtomSymbol(int atomNumber, int size) {
		if (size>20) throw new RuntimeException("getAtomSymbol size must be lower than 20");
		String symbol=periodicTable[atomNumber-1].atomSymbol;
		return spaces.substring(0,size-symbol.length())+symbol;
	}
	
	public static int getAtomNumber(String atomSymbol) {
		for (int i = 0; i<periodicTable.length; i++) {
	    	if (periodicTable[i].atomSymbol.equals(atomSymbol)) {
	    		return i+1;	
	    	}
	    }
	    return 0;
	}
	
	
}


class AtomDefinition {
		String atomSymbol;
		String atomName;
		double atomMass;
		Color atomColor;
		
		AtomDefinition (String atomSymbol, String atomName, double atomMass, Color atomColor) {
			this.atomSymbol=atomSymbol;
			this.atomName=atomName;
			this.atomMass=atomMass;
			this.atomColor=atomColor;
		}
		
		AtomDefinition (String atomSymbol, String atomName, double atomMass) {
			this (atomSymbol, atomName, atomMass, Color.black);
		}
		
}