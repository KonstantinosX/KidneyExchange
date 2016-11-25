package edu.umd.cs.mechdesign.simulator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Random;

import edu.cmu.cs.dickerson.kpd.helper.Pair;
import edu.cmu.cs.dickerson.kpd.helper.VertexTimeComparator;
import edu.cmu.cs.dickerson.kpd.io.AltruistOutput;
import edu.cmu.cs.dickerson.kpd.io.PatientListOutput;
import edu.cmu.cs.dickerson.kpd.io.PatientListOutput.LPCol;
import edu.cmu.cs.dickerson.kpd.io.PatientTransplantOutput.TCol;
import edu.cmu.cs.dickerson.kpd.io.AltruistOutput.ACol;
import edu.cmu.cs.dickerson.kpd.io.PatientTransplantOutput;
import edu.cmu.cs.dickerson.kpd.structure.Vertex;
import edu.cmu.cs.dickerson.kpd.structure.VertexAltruist;
import edu.cmu.cs.dickerson.kpd.structure.VertexPair;
import edu.cmu.cs.dickerson.kpd.structure.types.BloodType;
import edu.umd.cs.mechdesign.simulator.DeceasedSimulationOutput.PCol;

public class Utils {

	/**
	 * Draws a random patient's age. We assume that in each age range, each age
	 * in uniformally distributed
	 * 
	 * The age probabilities are taken from:
	 * https://optn.transplant.hrsa.gov/data/view-data-reports/national-data/#
	 * for living kidney donors in 2015
	 * 
	 * @return Age
	 */
	protected static double drawAltruistAge() {
		/* Age Probabilities for altruistic donors */
		double Pr_PATIENT_0_1 = 0.0;
		double Pr_PATIENT_1_5 = 0.0;
		double Pr_PATIENT_6_10 = 0.0;
		double Pr_PATIENT_11_17 = 0.0;
		double Pr_PATIENT_18_34 = 0.26323268;
		double Pr_PATIENT_35_49 = 0.39484902;
		double Pr_PATIENT_50_64 = 0.30248668;
		double Pr_PATIENT_65 = 0.03943162;

		Random random = new Random();
		double r = random.nextDouble();
		double age;
		if (r <= Pr_PATIENT_0_1) {
			double r1 = random.nextDouble();
			age = 1 * r1;
			return age;
		} else if (r <= Pr_PATIENT_0_1 + Pr_PATIENT_1_5) {
			double r1 = random.nextDouble();
			age = 1 + 4 * r1;
			return age;
		} else if (r <= Pr_PATIENT_0_1 + Pr_PATIENT_1_5 + Pr_PATIENT_6_10) {
			double r1 = random.nextDouble();
			age = 5 + 5 * r1;
			return age;
		} else if (r <= Pr_PATIENT_0_1 + Pr_PATIENT_1_5 + Pr_PATIENT_6_10
				+ Pr_PATIENT_11_17) {
			double r1 = random.nextDouble();
			age = 10 + 7 * r1;
			return age;
		} else if (r <= Pr_PATIENT_0_1 + Pr_PATIENT_1_5 + Pr_PATIENT_6_10
				+ Pr_PATIENT_11_17 + Pr_PATIENT_18_34) {
			double r1 = random.nextDouble();
			age = 17 + 17 * r1;
			return age;
		} else if (r <= Pr_PATIENT_0_1 + Pr_PATIENT_1_5 + Pr_PATIENT_6_10
				+ Pr_PATIENT_11_17 + Pr_PATIENT_18_34 + Pr_PATIENT_35_49) {
			double r1 = random.nextDouble();
			age = 34 + 15 * r1;
			return age;
		} else if (r <= Pr_PATIENT_0_1 + Pr_PATIENT_1_5 + Pr_PATIENT_6_10
				+ Pr_PATIENT_11_17 + Pr_PATIENT_18_34 + Pr_PATIENT_35_49
				+ Pr_PATIENT_50_64) {
			double r1 = random.nextDouble();
			age = 49 + 15 * r1;
			return age;
		} else {
			double r1 = random.nextDouble();
			age = 65 + 15 * r1;
		}

		return age;
	}

	/**
	 * Serialize the set of patients in the pool as well as the future patients
	 * to a csv file for sharing them with the deceased donor simulator
	 * 
	 * @param entryList
	 *            The list of patients that enter the pool in the entirety of
	 *            the simulation's runtime as well as each patient's entry time
	 * @param patientsByEntryTime
	 * @param exitTimes
	 *            The set of all patients including each patient's exit time.
	 * @param transplantTimes
	 * @param path
	 * @param timeLimit
	 */
	public static void serializePatients(
			List<Pair<Double, Vertex>> patientsByEntryTime,
			Map<String, Double> patientsByExitTime,
			Map<String, Double> patientAges,
			Map<Vertex, Double> transplantTimes, String path, double timeLimit) {
		PatientListOutput out;
		try {
			out = new PatientListOutput(path);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		for (Pair<Double, Vertex> e : patientsByEntryTime) {
			/* set entry and exit times */
			VertexPair patient = (VertexPair) e.getRight();
			double entryTime = e.getLeft();
			double exitTime = patientsByExitTime.get(patient.toString());
			double waitingTime = exitTime - entryTime;
			String exitReason = "DEATH";
			if (transplantTimes.containsKey(patient)) {
				exitReason = "SUCCESSFULL_TRANSPLANT";
				exitTime = transplantTimes.get(patient);
			} else {
				/*
				 * skip and don't record this patient if we don't know what will
				 * happen to them in the future and the simulation ended before
				 * he was able to get a transplant and before his death time
				 */
				if (timeLimit < exitTime)
					continue;
			}

			out.set(LPCol.VERTEX_ID, patient.toString());
			out.set(LPCol.ENTRY_TIME, entryTime);
			out.set(LPCol.BLOOD_TYPE, patient.getBloodTypePatient());
			out.set(LPCol.DONOR_BLOOD_TYPE, patient.getBloodTypeDonor());
			out.set(LPCol.EXIT_TIME, exitTime);
			out.set(LPCol.AGE_ILLNESS, patientAges.get(patient.toString()));
			out.set(LPCol.WAITING_TIME, waitingTime);
			out.set(LPCol.EXIT_REASON, exitReason);

			/* record tuple */
			try {
				out.record();
			} catch (IOException ex) {
				ex.printStackTrace();
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

				out.set(TCol.BLOOD_TYPE, ((VertexPair) patientTransplant
						.getKey()).getBloodTypeDonor());
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
			out.set(ACol.ENTRY_TIME, altruist.getLeft());
			out.set(ACol.AGE, patientAges.get(alt.toString()));

			/* record tuple */
			try {
				out.record();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void printPatientInformation(String path,
			ArrayList<PatientInformationHolder> allinfo) {

		DeceasedSimulationOutput out;
		try {
			out = new DeceasedSimulationOutput(path);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		for (int i = 0; i < allinfo.size(); i++) {
			out.set(PCol.ID, allinfo.get(i).getPatient().getID());
			out.set(PCol.ILLNESS_TIME, allinfo.get(i).getPatient()
					.getEntryTime());
			out.set(PCol.AGE_ILLNESS, allinfo.get(i).getPatient().getAge());
			out.set(PCol.BLOOD_TYPE, allinfo.get(i).getPatient()
					.getBloodTypePatient());
			out.set(PCol.CPRA, allinfo.get(i).getPatient().getCPRA());
			out.set(PCol.IS_ALTRUIST, allinfo.get(i).getPatient()
					.isIsAnAltruist());
			out.set(PCol.EXIT_TIME, allinfo.get(i).getCurrTime());
			out.set(PCol.WAITING_TIME, (allinfo.get(i).getCurrTime() - allinfo
					.get(i).getPatient().getEntryTime()));
			out.set(PCol.EXIT_REASON, allinfo.get(i).getExit_reason());
			if (allinfo.get(i).getOrgan() == null) {
				out.set(PCol.ORGAN_BLOOD_TYPE, "");
				out.set(PCol.ORGAN_DPI, "");
			} else {
				out.set(PCol.ORGAN_BLOOD_TYPE, allinfo.get(i).getOrgan()
						.getBloodTypeDonor());
				out.set(PCol.ORGAN_DPI, allinfo.get(i).getOrgan().getDPI());
			}

			/* record tuple */
			try {
				out.record();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public static double calculateTimeOfEntry(double Age, double CurrTime) {
		Random random = new Random();
		// 2015 source:
		// https://optn.transplant.hrsa.gov/data/view-data-reports/national-data/#
		double Pr_PATIENT_0_1 = 0.000656093;
		double Pr_PATIENT_1_5 = 0.006075993;
		double Pr_PATIENT_6_10 = 0.00487791;
		double Pr_PATIENT_11_17 = 0.017828617;
		double Pr_PATIENT_18_34 = 0.116157006;
		double Pr_PATIENT_35_49 = 0.265175719;
		double Pr_PATIENT_50_64 = 0.411912369;
		double Pr_PATIENT_65 = 0.177316294;

		double AgeOfEntry = 0;
		// assume that only adults (>=18) can be altruistic donors
		// estimate the conditional probability of getting sick in each age
		// range, given your age
		if (Age <= 34) {
			double probInThisRange = (34 - Age) * Pr_PATIENT_18_34 / (34 - 18);
			double r = random.nextDouble();
			if (r <= (probInThisRange / (probInThisRange + Pr_PATIENT_35_49
					+ Pr_PATIENT_50_64 + Pr_PATIENT_65))) {
				double r1 = random.nextDouble();
				AgeOfEntry = Age + (34 - Age) * r1;
			} else if (r <= (probInThisRange + Pr_PATIENT_35_49
					/ (probInThisRange + Pr_PATIENT_35_49 + Pr_PATIENT_50_64 + Pr_PATIENT_65))) {
				double r1 = random.nextDouble();
				AgeOfEntry = 34 + 15 * r1;
			} else if (r <= ((probInThisRange + Pr_PATIENT_35_49 + Pr_PATIENT_50_64) / (probInThisRange
					+ Pr_PATIENT_35_49 + Pr_PATIENT_50_64 + Pr_PATIENT_65))) {
				double r1 = random.nextDouble();
				AgeOfEntry = 49 + 15 * r1;
			} else if (r <= ((probInThisRange + Pr_PATIENT_35_49
					+ Pr_PATIENT_50_64 + Pr_PATIENT_65) / (probInThisRange
					+ Pr_PATIENT_35_49 + Pr_PATIENT_50_64 + Pr_PATIENT_65))) {
				double r1 = random.nextDouble();
				AgeOfEntry = 65 + 15 * r1;
			}
		} else if (Age <= 49) {
			double probInThisRange = (49 - Age) * Pr_PATIENT_35_49 / (49 - 35);
			double r = random.nextDouble();
			if (r <= ((probInThisRange) / (probInThisRange + Pr_PATIENT_50_64 + Pr_PATIENT_65))) {
				double r1 = random.nextDouble();
				AgeOfEntry = Age + (49 - Age) * r1;
			} else if (r <= ((probInThisRange + Pr_PATIENT_50_64) / (probInThisRange
					+ Pr_PATIENT_50_64 + Pr_PATIENT_65))) {
				double r1 = random.nextDouble();
				AgeOfEntry = 49 + 15 * r1;
			} else if (r <= ((probInThisRange + Pr_PATIENT_50_64 + Pr_PATIENT_65) / (probInThisRange
					+ Pr_PATIENT_50_64 + Pr_PATIENT_65))) {
				double r1 = random.nextDouble();
				AgeOfEntry = 65 + 15 * r1;
			}
		} else if (Age <= 64) {
			double probInThisRange = (64 - Age) * Pr_PATIENT_50_64 / (64 - 50);
			double r = random.nextDouble();
			if (r <= ((probInThisRange) / (probInThisRange + Pr_PATIENT_65))) {
				double r1 = random.nextDouble();
				AgeOfEntry = Age + (64 - Age) * r1;
			} else if (r <= ((probInThisRange + Pr_PATIENT_65) / (probInThisRange + Pr_PATIENT_65))) {
				double r1 = random.nextDouble();
				AgeOfEntry = 65 + 15 * r1;
			}
		} else { // the altruist is older that 64
			if (Age < 85) {
				double r1 = random.nextDouble();
				AgeOfEntry = Age + (85 - Age) * r1;
			} else {
				double r1 = random.nextDouble();
				AgeOfEntry = Age + r1;
			}
		}

		double timeOfEntry = CurrTime + (AgeOfEntry - Age) * 52;

		return timeOfEntry;
	}

	public static Queue<Pair<Double, Vertex>> readAltruists(double timeLimit,
			double deceasedSimulationZeroTime,
			PatientsForDeceasedDonorGenerator DKGen) {

		// create queue than includes entry time and altruists
		Queue<Pair<Double, Vertex>> altruistsByEntryTime = new PriorityQueue<Pair<Double, Vertex>>(
				(int) timeLimit, new VertexTimeComparator());

		String csvFile = "././altruists.csv";
		String line = "";
		String cvsSplitBy = ",";
		int myline = 1;
		double livingSimulationZeroTime = 0;
		double entryTimeInLivingSimulation = 0;
		try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {

			while ((line = br.readLine()) != null) {

				// use comma as separator
				String[] info = line.split(cvsSplitBy);

				if (myline == 1) {
					livingSimulationZeroTime = Double.parseDouble(info[3]);
					// System.out.println("Simulation Time: "+info[3]);
				} else {
					// System.out.println("Age " + info[0] + " , Blood Type " +
					// info[1]);
					// create a new patient for each altruistic
					BloodType bloodTypePatient = null;
					if (info[1].equals("O")) {
						bloodTypePatient = BloodType.O;
					} else if (info[1].equals("A")) {
						bloodTypePatient = BloodType.A;
					} else if (info[1].equals("B")) {
						bloodTypePatient = BloodType.B;
					} else if (info[1].equals("AB")) {
						bloodTypePatient = BloodType.AB;
					}
					entryTimeInLivingSimulation = Double.parseDouble(info[2]);

					double TimeOfTransplantInDeceased = deceasedSimulationZeroTime
							+ (entryTimeInLivingSimulation - livingSimulationZeroTime);

					double EntryTime = calculateTimeOfEntry(
							Double.parseDouble(info[0]),
							TimeOfTransplantInDeceased);

					double ageWhenEntersWaitingList = Double
							.parseDouble(info[0])
							+ (EntryTime - TimeOfTransplantInDeceased);
					// age defined as the age that the patient will have when he
					// enters the waiting list
					// the boolean IsAnAltruist takes the value "true"
					WaitlistedPatient w = new WaitlistedPatient(
							DKGen.getNewID(), DKGen.drawCPRA(),
							ageWhenEntersWaitingList, bloodTypePatient, true);

					// System.out.println("\n Time that altruist will arrive " +
					// EntryTime + " \n");

					altruistsByEntryTime.add(new Pair<Double, Vertex>(
							EntryTime, w));
				}
				myline++;

			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return altruistsByEntryTime;

	}

	// read the csv file and return a list of patient objects
	public static Queue<Pair<Double, Vertex>> readPatientGivenLDK(
			double timeLimit, double deceasedSimulationZeroTime,
			PatientsForDeceasedDonorGenerator DKGen) {

		// create queue than includes entry time and altruists
		Queue<Pair<Double, Vertex>> patientsByLeavingTime = new PriorityQueue<Pair<Double, Vertex>>(
				(int) timeLimit, new VertexTimeComparator());

		String csvFile = "././transplants.csv";
		String line = "";
		String cvsSplitBy = ",";
		int myline = 1;
		double livingSimulationZeroTime = 0;
		try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {

			while ((line = br.readLine()) != null) {

				// use comma as separator
				String[] info = line.split(cvsSplitBy);

				if (myline == 1) {
					livingSimulationZeroTime = Double.parseDouble(info[2]);
					// System.out.println("Simulation Time: "+info[2]);
				} else {
					// System.out.println("Tranplant time " + info[0] +
					// " , Blood Type " + info[1]);
					// create a new patient for each altruistic
					BloodType bloodTypePatient = null;
					if (info[1].equals("O")) {
						bloodTypePatient = BloodType.O;
					} else if (info[1].equals("A")) {
						bloodTypePatient = BloodType.A;
					} else if (info[1].equals("B")) {
						bloodTypePatient = BloodType.B;
					} else if (info[1].equals("AB")) {
						bloodTypePatient = BloodType.AB;
					}

					WaitlistedPatient w = new WaitlistedPatient(-1, 0, 0,
							bloodTypePatient, false);

					double patientTransplantTimeInLiving = Double
							.parseDouble(info[0]);
					double patientLeavesDeceasedSimulationTime = deceasedSimulationZeroTime
							+ (patientTransplantTimeInLiving - livingSimulationZeroTime);
					// System.out.println("\n Time that he leaves the simulation "
					// + patientLeavesDeceasedSimulationTime + " \n");

					patientsByLeavingTime.add(new Pair<Double, Vertex>(
							patientLeavesDeceasedSimulationTime, w));
				}
				myline++;

			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return patientsByLeavingTime;

	}

}
