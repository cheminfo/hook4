package com.actelion.research.nemo.jcamp;

public class Label {
	private String rawLabel;
	private String normalizedLabel;
	
	//JCAMP standard labels
	public final static Label STD_TITLE = fromString("TITLE");
	public final static Label STD_JCAMP_DX = fromString("JCAMP-DX");
	public final static Label STD_DATA_TYPE = fromString("DATA TYPE");
	public final static Label STD_OBSERVE_NUCLEUS = fromString(".OBSERVE NUCLEUS");
	public final static Label STD_OBSERVE_FREQUENCY = fromString(".OBSERVE FREQUENCY");

	//Actelion specific labels
	public final static Label ACT_NEMOVIEW = fromString("$NEMO VIEW");
	
	//Bruker specific labels
	public final static Label BRUKER_DEVICE = fromString("ORIGIN");
	public final static Label BRUKER_SAMPLE_NAME = fromString("$NAME");
	public final static Label BRUKER_MEASUREMENT_DATE = fromString("$DATE");
	public final static Label BRUKER_MEASUREMENT_POS = fromString("$AUTOPOS");
	public final static Label BRUKER_OPERATOR = fromString("$USER");
	public final static Label BRUKER_NO_SCANS = fromString("$NS");
	public final static Label BRUKER_SOLVENT = fromString(".SOLVENT NAME");
	public final static Label BRUKER_TEMPERATURE = fromString("$TE");
	public final static Label BRUKER_EXPERIMENT_TYPE = fromString("$EXP");
	public final static Label BRUKER_PROBEHEAD = fromString("$PROBHD");
	public final static Label BRUKER_EXPNO = fromString("$EXPNO");
	
	//Varian specific labels
	public final static Label VARIAN_DEVICE = fromString("SPECTROMETER/DATA SYSTEM");
	public final static Label VARIAN_SAMPLE_NAME = fromString("$samplename");
	public final static Label VARIAN_MEASUREMENT_DATE = fromString("$time_run");
	public final static Label VARIAN_MEASUREMENT_POS = fromString("$loc_");
	public final static Label VARIAN_OPERATOR = fromString("$operator_");
	public final static Label VARIAN_NO_SCANS = fromString("$ct");
	public final static Label VARIAN_SOLVENT = fromString("$solvent");
	public final static Label VARIAN_TEMPERATURE = fromString("$temp");
	public final static Label VARIAN_EXPERIMENT_TYPE = fromString("$pslabel");

	public Label(String label) {
		if(label.matches(".*[^\\u0000-\\u007F].*"))
			throw(new RuntimeException("Jcamp standard does not allow non ASCII characters!"));

		rawLabel = label.split("=", 2)[0].trim();
		
		if(rawLabel.startsWith("##"))
			rawLabel = rawLabel.replaceFirst("##", "").trim();
		
		normalizedLabel = rawLabel.toUpperCase().replaceAll("[-_/ ]", "");
	}
	
	public String get()
	{
		return "##" + rawLabel;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((normalizedLabel == null) ? 0 : normalizedLabel.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Label other = (Label) obj;
		if (normalizedLabel == null) {
			if (other.normalizedLabel != null)
				return false;
		} else if (!normalizedLabel.equals(other.normalizedLabel))
			return false;
		return true;
	}
	
	public String toString() {
		return get();
	}

	public static Label fromString(String label)
	{
		return new Label(label);
	}
}
