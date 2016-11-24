package edu.umd.cs.mechdesign.simulator;

import java.io.IOException;

import edu.cmu.cs.dickerson.kpd.io.AltruistOutput;
import edu.cmu.cs.dickerson.kpd.io.Output;
import edu.cmu.cs.dickerson.kpd.io.OutputCol;
import edu.cmu.cs.dickerson.kpd.io.AltruistOutput.ACol;

public class DeceasedSimulationOutput extends Output {


	public enum PCol implements OutputCol {
		ID, ILLNESS_TIME, AGE_ILLNESS, BLOOD_TYPE, CPRA, IS_ALTRUIST, EXIT_TIME, WAITING_TIME, EXIT_REASON, 
		ORGAN_BLOOD_TYPE, ORGAN_DPI;

		public int getColIdx() {
			return this.ordinal();
		}
	};

	public DeceasedSimulationOutput(String path) throws IOException {
		super(path, getHeader());

	}

	public static String[] getHeader() {

		String[] header = new String[PCol.values().length];
		header[PCol.ID.getColIdx()] = "id";
		header[PCol.ILLNESS_TIME.getColIdx()] = "illness_time";
		header[PCol.AGE_ILLNESS.getColIdx()] = "age_got_ill";
		header[PCol.BLOOD_TYPE.getColIdx()] = "blood_type";
		header[PCol.CPRA.getColIdx()] = "CPRA";
		header[PCol.IS_ALTRUIST.getColIdx()] = "is_an_altruist";
		header[PCol.EXIT_TIME.getColIdx()] = "exit_time";
		header[PCol.WAITING_TIME.getColIdx()] = "time_in_waiting_list";
		header[PCol.EXIT_REASON.getColIdx()] = "exit_reason";
		header[PCol.ORGAN_BLOOD_TYPE.getColIdx()] = "organ_blood_type";
		header[PCol.ORGAN_DPI.getColIdx()] = "organ_DPI";
		return header;
	}
}
