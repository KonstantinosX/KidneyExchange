package edu.umd.cs.mechdesign.simulator.tests;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

import org.junit.Test;

import edu.cmu.cs.dickerson.kpd.helper.Pair;
import edu.cmu.cs.dickerson.kpd.solver.solution.Solution;
import edu.cmu.cs.dickerson.kpd.structure.Cycle;
import edu.cmu.cs.dickerson.kpd.structure.Edge;
import edu.cmu.cs.dickerson.kpd.structure.Pool;
import edu.cmu.cs.dickerson.kpd.structure.Vertex;
import edu.cmu.cs.dickerson.kpd.structure.alg.CycleGenerator;
import edu.cmu.cs.dickerson.kpd.structure.generator.SaidmanPoolGenerator;
import edu.umd.cs.mechdesign.simulator.Event;
import edu.umd.cs.mechdesign.simulator.EventType;
import edu.umd.cs.mechdesign.simulator.SimulationDriver;

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

	@Test
	public void testChains() {
		SaidmanPoolGenerator saidman = new SaidmanPoolGenerator(new Random(10));
		Pool p = saidman.generate(50, 5);
		Solution s = SimulationDriver.conductMatches(p, new CycleGenerator(p),
				3, 4);
		// System.out.println("Altruists: "
		// + Cycle.getConstituentAltruists(s.getMatching(), p));

		List<Edge> chainEdges = new ArrayList<>();

		for (Cycle c : s.getMatching()) {
			if (Cycle.isAChain(c, p)) {
				if (Cycle.getConstituentVertices(c, p).size() > 3) {
					System.out.println(c);

					boolean startRemoving = false;

					Iterator<Edge> chainIter = c.getEdges().iterator();
					while (chainIter.hasNext()) {

						Edge e = chainIter.next();

						if (e.getSrc().equals("47")) {
							break;
						} else {
							chainEdges.add(e);
						}

					}

					Cycle brokenChain = Cycle.makeCycle(chainEdges,
							c.getWeight());
					// System.out.println(brokenChain);
					assertEquals("< (33 : 53) (40 : 33) (41 : 40) > @ 4.0",
							brokenChain.toString());
					return;
				}
			}
		}
	}

	@Test
	public void testRandomScheduling() {
		double currTime = 34.33343;
		for (int i = 0; i < 10; i++) {
			System.out
					.println(Event.randomInRange(0 + currTime, 12 + currTime));
		}
	}
}
