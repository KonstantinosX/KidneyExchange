package edu.cmu.cs.dickerson.kpd.io;

import java.io.IOException;

public class MatchingSimulationOutput extends Output {

	public enum Col implements OutputCol {
		VERTEX_ID, 
//		TIME_ENTERED,
//		TIME_LEAVING,
//		PASSED_AWAY,
//		SUCCESSFULL_TRANSPLANT
		
		
		;

		public int getColIdx() {
			return this.ordinal();
		}
	};

	public MatchingSimulationOutput(String path) throws IOException {
		super(path, getHeader());
	}

	public static String[] getHeader() {
		String[] header = new String[Col.values().length];
		header[Col.VERTEX_ID.getColIdx()] = "Vertex ID";

		return header;
	}

}
