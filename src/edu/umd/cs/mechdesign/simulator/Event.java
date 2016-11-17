package edu.umd.cs.mechdesign.simulator;

import java.util.Arrays;
import java.util.Queue;

import edu.cmu.cs.dickerson.kpd.helper.MathUtil;
import edu.cmu.cs.dickerson.kpd.helper.Pair;
import edu.cmu.cs.dickerson.kpd.structure.Cycle;
import edu.cmu.cs.dickerson.kpd.structure.Vertex;

public class Event {

	private double time;
	private EventType type;

	public Event(double time, EventType type) {
		this.time = time;
		this.type = type;
	}

	public double getTime() {
		return time;
	}

	public void setTime(double time) {
		this.time = time;
	}

	public EventType getType() {
		return type;
	}

	public void setType(EventType type) {
		this.type = type;
	}

	/**
	 * Returns the next event.
	 * 
	 * @param timesList
	 *            An array of times in the appropriate order
	 *            (nextMatchingProcess, nextTransplant ,patientDies)
	 */
	public static Event getNextEvent(Queue<Double> matchingTimes,
			Queue<Pair<Double, Cycle>> cycleTransplantTimes,
			Queue<Pair<Double, Vertex>> verticesByExitTime,
			Queue<Pair<Double, Vertex>> verticesByEntryTime,
			Queue<Double> altEvents) {

		// TODO stub for baseline simulation
		double nextTransplantTime = Double.MAX_VALUE;
		if (!cycleTransplantTimes.isEmpty())
			nextTransplantTime = cycleTransplantTimes.peek().getLeft();

		double newPatientArrival = Double.MAX_VALUE;
		if (!verticesByEntryTime.isEmpty())
			newPatientArrival = verticesByEntryTime.peek().getLeft();

		double patientDeparture = Double.MAX_VALUE;
		if (!verticesByExitTime.isEmpty())
			patientDeparture = verticesByExitTime.peek().getLeft();

		double matchingTime = Double.MAX_VALUE;
		if (!matchingTimes.isEmpty())
			matchingTime = matchingTimes.peek();

		double altMatching = Double.MAX_VALUE;
		if (!altEvents.isEmpty())
			altMatching = altEvents.peek();

		Double[] timesList = { matchingTime, nextTransplantTime,
				patientDeparture, newPatientArrival, altMatching };

		int minIndex = MathUtil.minIndex(Arrays.asList(timesList));
		Double time = timesList[minIndex];
		EventType type = null;
		switch (minIndex) {
		case 0:
			type = EventType.CONDUCT_MATCHINGS;
			matchingTimes.poll();
			break;
		case 1:
			type = EventType.CONDUCT_TRANSPLANT;
			// cycleTransplantTimes.poll();
			break;
		case 2:
			type = EventType.PATIENT_DIES;
			// verticesByExitTime.poll();
			break;
		case 3:
			type = EventType.PATIENT_ENTERS;
			verticesByEntryTime.poll();
			break;
		case 4:
			type = EventType.ALTRUIST_ENTERS;
			// altEvents.poll();
			break;
		default:
			type = EventType.TERMINATE_SIMULATION;
		}
		return new Event(time, type);
	}

	@Override
	public String toString() {
		return type + ":" + time;
	}
}
