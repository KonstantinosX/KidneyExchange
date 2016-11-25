package edu.cmu.cs.dickerson.kpd.io;

import java.io.IOException;

public class PatientListOutput extends Output {

	public enum LPCol implements OutputCol {
		VERTEX_ID, ENTRY_TIME, EXIT_TIME, AGE_ILLNESS, BLOOD_TYPE, WAITING_TIME, EXIT_REASON, DONOR_BLOOD_TYPE;
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
		String[] header = new String[LPCol.values().length];
		header[LPCol.VERTEX_ID.getColIdx()] = "id";
		header[LPCol.ENTRY_TIME.getColIdx()] = "illness_time";
		header[LPCol.EXIT_TIME.getColIdx()] = "exit_time";
		header[LPCol.AGE_ILLNESS.getColIdx()] = "age_got_ill";
		header[LPCol.BLOOD_TYPE.getColIdx()] = "blood_type";
		header[LPCol.WAITING_TIME.getColIdx()] = "time_in_waiting_list";
		header[LPCol.EXIT_REASON.getColIdx()] = "exit_reason";
		header[LPCol.DONOR_BLOOD_TYPE.getColIdx()] = "organ_blood_type";

		return header;
	}
}
