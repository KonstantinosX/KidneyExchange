package edu.umd.cs.mechdesign.simulator.tests;

import static org.junit.Assert.assertEquals;

import java.util.PriorityQueue;
import java.util.Queue;

import org.junit.Test;

import edu.cmu.cs.dickerson.kpd.helper.Pair;
import edu.cmu.cs.dickerson.kpd.structure.Cycle;
import edu.cmu.cs.dickerson.kpd.structure.Vertex;
import edu.umd.cs.mechdesign.simulator.Event;
import edu.umd.cs.mechdesign.simulator.EventType;

public class SimTests {

	@Test
	public void testNextEvent() {
		Double[] l = { 4.5, 1.4, 1.6, 9.9 };
		Queue<Double> matchingTimes = new PriorityQueue<Double>();
		Queue<Pair<Double, Cycle>> cycleTransplantTimes = new PriorityQueue<Pair<Double, Cycle>>();
		Queue<Pair<Double, Vertex>> verticesByExitTime = new PriorityQueue<Pair<Double, Vertex>>();
		Queue<Pair<Double, Vertex>> verticesByEntryTime = new PriorityQueue<Pair<Double, Vertex>>();
		Queue<Double> altTimes = new PriorityQueue<Double>();

		matchingTimes.add(4.5);
		cycleTransplantTimes.add(new Pair<Double, Cycle>(1.4, null));
		verticesByExitTime.add(new Pair<Double, Vertex>(1.6, null));
		verticesByEntryTime.add(new Pair<Double, Vertex>(9.9, null));
		altTimes.add(9.0);

		Event t = Event.getNextEvent(matchingTimes, cycleTransplantTimes,
				verticesByExitTime, verticesByEntryTime, altTimes);
		assertEquals(t.getType(), EventType.CONDUCT_TRANSPLANT);
	}
}
