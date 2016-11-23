package edu.cmu.cs.dickerson.kpd.io;

import java.io.IOException;

public class AltruistOutput extends Output {

	private static double timeZero;

	public enum ACol implements OutputCol {
		AGE, BLOOD_TYPE, ILLNESS_TIME, TIME_ZERO

		;

		public int getColIdx() {
			return this.ordinal();
		}
	};

	public AltruistOutput(String path, double timeZero) throws IOException {
		super(path, getHeader(timeZero));
		AltruistOutput.timeZero = timeZero;
	}

	public static String[] getHeader(double timeZero) {

		String[] header = new String[ACol.values().length];
		header[ACol.AGE.getColIdx()] = "altruist_age";
		header[ACol.ILLNESS_TIME.getColIdx()] = "illness_time";
		header[ACol.BLOOD_TYPE.getColIdx()] = "altruist_blood_type";
		header[ACol.TIME_ZERO.getColIdx()] = Double.toString(timeZero);
		return header;
	}
}