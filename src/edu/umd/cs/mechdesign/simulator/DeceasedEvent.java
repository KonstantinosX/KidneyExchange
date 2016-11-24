package edu.umd.cs.mechdesign.simulator;

import java.util.Arrays;
import java.util.Queue;
import java.util.Random;

import edu.cmu.cs.dickerson.kpd.helper.MathUtil;
import edu.cmu.cs.dickerson.kpd.helper.Pair;
import edu.cmu.cs.dickerson.kpd.structure.Cycle;
import edu.cmu.cs.dickerson.kpd.structure.Vertex;

public class DeceasedEvent {

	private double time;
	private DeceasedEventType type;

	public DeceasedEvent(double time, DeceasedEventType type) {
		this.time = time;
		this.type = type;
	}

	public double getTime() {
		return time;
	}

	public void setTime(double time) {
		this.time = time;
	}

	public DeceasedEventType getType() {
		return type;
	}

	public void setType(DeceasedEventType type) {
		this.type = type;
	}

	/**
	 * Returns the next event.
	 * 
	 * @param timesList
	 *            An array of times in the appropriate order
	 *            (nextMatchingProcess, nextTransplant ,patientDies)
	 */
	public static DeceasedEvent getNextDeceasedEvent(
			Queue<Pair<Double, Vertex>> patientsByExitTime,
			Queue<Pair<Double, Vertex>> patientsByEntryTime,
			Queue<Pair<Double, Vertex>> altruistsByEntryTime,
			Queue<Pair<Double, Vertex>> patientsByLeavingTime,
			Queue<Double> organEvents) {

		// TODO stub for baseline simulation
		
		double newPatientArrival = Double.MAX_VALUE;
		if (!patientsByEntryTime.isEmpty())
			newPatientArrival = patientsByEntryTime.peek().getLeft();

		double patientDeparture = Double.MAX_VALUE;
		if (!patientsByExitTime.isEmpty())
			patientDeparture = patientsByExitTime.peek().getLeft();
		
		double altruistArrival = Double.MAX_VALUE;
		if (!altruistsByEntryTime.isEmpty())
			altruistArrival = altruistsByEntryTime.peek().getLeft();
		
		double patientGotLDK = Double.MAX_VALUE;
		if (!patientsByLeavingTime.isEmpty())
			patientGotLDK = patientsByLeavingTime.peek().getLeft();

		double organArrives = Double.MAX_VALUE;
		if (!organEvents.isEmpty())
			organArrives = organEvents.peek();

		Double[] timesList = { patientDeparture, newPatientArrival,altruistArrival,patientGotLDK, organArrives };

		int minIndex = MathUtil.minIndex(Arrays.asList(timesList));
		Double time = timesList[minIndex];
		DeceasedEventType type = null;
		switch (minIndex) {
		case 0:
			type = DeceasedEventType.PATIENT_DIES;
			// patientsByExitTime.poll();
			break;
		case 1:
			type = DeceasedEventType.PATIENT_ENTERS;
			patientsByEntryTime.poll();
			break;
		case 2:
			type = DeceasedEventType.ALTRUIST_GETS_SICK;
			//altruistsByEntryTime.poll();
			break;
		case 3:
			type = DeceasedEventType.PATIENT_GETS_LDK;
			//patientsByLeavingTime.poll();
			break;
		case 4:
			type = DeceasedEventType.ORGAN_ARRIVES;
			organEvents.poll();
			break;
		default:
			type = DeceasedEventType.TERMINATE_SIMULATION;
		}
		return new DeceasedEvent(time, type);
	}

	@Override
	public String toString() {
		return type + ":" + time;
	}

	/**
	 * Generates a random double with in min and max
	 * 
	 * @param min
	 *            The minimum double that can be generated
	 * @param max
	 *            The maximum double that can be generated
	 * @return
	 */
	public static double randomInRange(double min, double max) {
		Random random = new Random();
		double range = max - min;
		double scaled = random.nextDouble() * range;
		double shifted = scaled + min;
		return shifted; // == (rand.nextDouble() * (max-min)) + min;
	}
}

