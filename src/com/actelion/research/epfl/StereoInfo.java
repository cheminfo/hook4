package com.actelion.research.epfl;

public enum StereoInfo {
	UNDEF, HAS_DIFFERENT_PROTONS, PRO_R, PRO_S, PRO_E, PRO_Z;

	public String getGUIString() {
		switch (this) {
		case PRO_R:
			return " -> pro R";
		case PRO_S:
			return " -> pro S";
		case PRO_E:
			return " -> pro E";
		case PRO_Z:
			return " -> pro Z";
		case UNDEF:
			return "";
		default:
			return "";
		}
	}
}
