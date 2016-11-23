package edu.cmu.cs.dickerson.kpd.io;

import java.io.IOException;

public class PatientTransplantOutput extends Output {

	private static double timeZero;

	public enum TCol implements OutputCol {
		TRANSPLANT_TIME, BLOOD_TYPE, TIME_ZERO

		;

		public int getColIdx() {
			return this.ordinal();
		}
	};

	public PatientTransplantOutput(String path, double timeZero)
			throws IOException {
		super(path, getHeader(timeZero));
		PatientTransplantOutput.timeZero = timeZero;
	}

	public static String[] getHeader(double timeZero) {
		String[] header = new String[TCol.values().length];
		header[TCol.TRANSPLANT_TIME.getColIdx()] = "patient_transplant_time";
		header[TCol.BLOOD_TYPE.getColIdx()] = "patient_blood_type";
		header[TCol.TIME_ZERO.getColIdx()] = Double.toString(timeZero);

		return header;
	}
}
