package edu.umd.cs.mechdesign.simulator;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import edu.cmu.cs.dickerson.kpd.helper.Pair;
import edu.cmu.cs.dickerson.kpd.io.AltruistOutput;
import edu.cmu.cs.dickerson.kpd.io.PatientListOutput;
import edu.cmu.cs.dickerson.kpd.io.PatientListOutput.Col;
import edu.cmu.cs.dickerson.kpd.io.PatientTransplantOutput.TCol;
import edu.cmu.cs.dickerson.kpd.io.AltruistOutput.ACol;
import edu.cmu.cs.dickerson.kpd.io.PatientTransplantOutput;
import edu.cmu.cs.dickerson.kpd.structure.Vertex;
import edu.cmu.cs.dickerson.kpd.structure.VertexAltruist;
import edu.cmu.cs.dickerson.kpd.structure.VertexPair;

public class Utils {

	/**
	 * Serialize the set of patients in the pool as well as the future patients
	 * to a csv file for sharing them with the deceased donor simulator
	 * 
	 * @param entryList
	 *            The list of patients that enter the pool in the entirety of
	 *            the simulation's runtime as well as each patient's entry time
	 * @param exitTimes
	 *            The set of all patients including each patient's exit time.
	 * @param path
	 */
	public static void serializePatients(List<Pair<Double, Vertex>> entryList,
			Map<String, Double> exitTimes, String path) {
		PatientListOutput out;
		try {
			out = new PatientListOutput(path);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		for (Pair<Double, Vertex> p : entryList) {
			/* set entry and exit times */
			VertexPair patient = (VertexPair) p.getRight();
			out.set(Col.VERTEX_ID, patient.toString());
			out.set(Col.TIME_ENTERED, p.getLeft());
			out.set(Col.TIME_LEAVING, exitTimes.get(patient.toString()));
			out.set(Col.BLOOD_TYPE, patient.getBloodTypePatient());

			/* record tuple */
			try {
				out.record();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Serialize the set of transplants that happened in the simulation
	 * including the time the transplants happened to a csv file for sharing
	 * them with the deceased donor simulator
	 */
	public static void serializeTransplants(String path,
			Map<Vertex, Double> transplantTimes, double startingTime) {
		PatientTransplantOutput out;
		try {
			out = new PatientTransplantOutput(path, startingTime);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		for (Entry<Vertex, Double> patientTransplant : transplantTimes
				.entrySet()) {
			Vertex v = patientTransplant.getKey();
			if (!(v instanceof VertexAltruist)) {

				out.set(TCol.BLOOD_TYPE,
						((VertexPair) patientTransplant.getKey()).getBloodTypeDonor());
				out.set(TCol.TRANSPLANT_TIME, patientTransplant.getValue());

				/* record tuple */
				try {
					out.record();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}

	}

	/**
	 * Writes information about the altruists that entered the simulation to a
	 * csv file
	 * 
	 * @param altruistsPath
	 * @param patientAges
	 * @param altruistsByEntryTime
	 * @param startingTime
	 */
	public static void serializeAltruists(String altruistsPath,
			Map<String, Double> patientAges,
			Queue<Pair<Double, Vertex>> altruistsByEntryTime,
			double startingTime) {

		AltruistOutput out;
		try {
			out = new AltruistOutput(altruistsPath, startingTime);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		for (Pair<Double, Vertex> altruist : altruistsByEntryTime) {
			Vertex alt = altruist.getRight();
			out.set(ACol.BLOOD_TYPE, ((VertexAltruist) alt).getBloodTypeDonor());
			out.set(ACol.AGE, patientAges.get(alt.toString()));

			/* record tuple */
			try {
				out.record();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// read the csv file and return a list of patient objects
	// public static List<DeceasedDonorPatient> readPatients(String file) {
	// String line = "";
	// String cvsSplitBy = ",";
	//
	// try (BufferedReader br = new BufferedReader(new FileReader(file))) {
	//
	// while ((line = br.readLine()) != null) {
	//
	// // use comma as separator
	// String[] patient = line.split(cvsSplitBy);
	//
	// }
	//
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	//
	// }

}
