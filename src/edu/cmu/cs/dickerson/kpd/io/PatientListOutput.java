package edu.cmu.cs.dickerson.kpd.io;

import java.io.IOException;

public class PatientListOutput extends Output {

	public enum Col implements OutputCol {
		VERTEX_ID, 
		TIME_ENTERED,
		TIME_LEAVING,
		BLOOD_TYPE,
		// PASSED_AWAY,
		// SUCCESSFULL_TRANSPLANT

		;

		public int getColIdx() {
			return this.ordinal();
		}
	};

	public PatientListOutput(String path) throws IOException {
		super(path, getHeader());
	}

	public static String[] getHeader() {
		String[] header = new String[Col.values().length];
		header[Col.VERTEX_ID.getColIdx()] = "vertex_id";
		header[Col.TIME_ENTERED.getColIdx()] = "entry_time";
		header[Col.TIME_LEAVING.getColIdx()] = "exit_time";
		header[Col.BLOOD_TYPE.getColIdx()] = "blood_type";

		return header;
	}
}
