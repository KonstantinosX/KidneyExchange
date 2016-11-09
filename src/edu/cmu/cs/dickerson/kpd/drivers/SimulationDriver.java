package edu.cmu.cs.dickerson.kpd.drivers;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import edu.cmu.cs.dickerson.kpd.dynamic.arrivals.ExponentialArrivalDistribution;
import edu.cmu.cs.dickerson.kpd.helper.CycleTimeComparator;
import edu.cmu.cs.dickerson.kpd.helper.IOUtil;
import edu.cmu.cs.dickerson.kpd.helper.Pair;
import edu.cmu.cs.dickerson.kpd.helper.VertexTimeComparator;
import edu.cmu.cs.dickerson.kpd.solver.CycleFormulationCPLEXSolver;
import edu.cmu.cs.dickerson.kpd.solver.exception.SolverException;
import edu.cmu.cs.dickerson.kpd.solver.solution.Solution;
import edu.cmu.cs.dickerson.kpd.structure.Cycle;
import edu.cmu.cs.dickerson.kpd.structure.Pool;
import edu.cmu.cs.dickerson.kpd.structure.Vertex;
import edu.cmu.cs.dickerson.kpd.structure.VertexPair;
import edu.cmu.cs.dickerson.kpd.structure.alg.CycleGenerator;
import edu.cmu.cs.dickerson.kpd.structure.alg.CycleMembership;
import edu.cmu.cs.dickerson.kpd.structure.alg.FailureProbabilityUtil;
import edu.cmu.cs.dickerson.kpd.structure.generator.SaidmanPoolGenerator;
import edu.umd.cs.mechdesign.simulator.Event;
import edu.umd.cs.mechdesign.simulator.EventType;

public class SimulationDriver {
	private static final Logger logger = Logger
			.getLogger(SimulationDriver.class.getSimpleName());

	private static ExponentialArrivalDistribution arrivalTimeGen;
	private static ExponentialArrivalDistribution lifespanTimeGen;
	private static ExponentialArrivalDistribution transplantTimeGen;

	public static void main(String[] args) {
		int m = 6;
		double lambda = 1.0;
		// lambda = 5 or 6
		// // List of m parameters (for every one time period, expect m vertices
		// to enter, Poisson process)
		// List<Double> mList = Arrays.asList(new Double[] {
		// //0.1, 0.2, 0.5, 1.0, 2.0,
		// 100.0,
		// });
		//
		// // List of lambda parameters (every vertex has lifespan of
		// exponential clock with parameter lambda)
		// List<Double> lambdaList = Arrays.asList(new Double[] {
		// //0.025, 0.05, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9,
		// 1.0,
		// //1.5, 2.0,
		// });

		boolean doOptIPUB2 = true; // Do optimal IP solve on C(3,4) but using
									// infinite chain extension on 4-chains

		// Set up our random generators for pools (sample from UNOS data, sample
		// from Saidman distribution)
		Random r = new Random();

		// TODO Need some values for m and lambda (look at DriverApprox)
		arrivalTimeGen = new ExponentialArrivalDistribution(m, r);
		lifespanTimeGen = new ExponentialArrivalDistribution(lambda, r);
		transplantTimeGen = new ExponentialArrivalDistribution(lambda, r);

		double failure_param1 = 0.7; // e.g., constant failure rate of 70%

		// UNOSGenerator UNOSGen = UNOSGenerator.makeAndInitialize(
		// IOUtil.getBaseUNOSFilePath(), ',', r);
		SaidmanPoolGenerator SaidmanGen = new SaidmanPoolGenerator(r);

		// Optimize w.r.t. discounted or raw utility?
		boolean usingFailureProbabilities = false;
		FailureProbabilityUtil.ProbabilityDistribution failDist = FailureProbabilityUtil.ProbabilityDistribution.CONSTANT;

		if (!usingFailureProbabilities) {
			failDist = FailureProbabilityUtil.ProbabilityDistribution.NONE;
		}

		// Cycle and chain limits
		int cycleCap = 3;
		int chainCap = 4;
		int graphSize = 50;

		// IOUtil.dPrintln("\n*****\nGraph (|V|=" + graphSize + ", #" + graphRep
		// + "/" + numGraphReps + "), cap: " + chainCap + ", gen: "
		// + gen.getClass().getSimpleName() + "\n*****\n");

		// Generate pool (~5% altruists, UNOS might be different);
		int numPairs = (int) Math.round(graphSize * 0.95);
		int numAlts = graphSize - numPairs;
		Pool pool = SaidmanGen.generate(numPairs, numAlts);
		logger.info("Pool: " + pool);
		logger.info("Altruists: " + pool.getAltruists());
		// GreedyPackingSolver s = new GreedyPackingSolver(pool);

		// If we're setting failure probabilities, do that here:
		if (usingFailureProbabilities) {
			FailureProbabilityUtil.setFailureProbability(pool, failDist, r,
					failure_param1);
		}
		// All solutions (optimal, greedy packings, etc)
		// Solution optSolIP = null;
		// Solution optSolLP = null;
		Solution optSolIPUB2 = null;

		// long startCycleGen = System.nanoTime();
		CycleGenerator cg = new CycleGenerator(pool);
		// cycles = cg.generateCyclesAndChains(cycleCap, chainCap,
		// usingFailureProbabilities);
		// membership = new CycleMembership(pool, cycles);
		// long endCycleGen = System.nanoTime();
		// out.set(Col.CYCLE_GEN_TIME, endCycleGen - startCycleGen);

		// SIMULATION
		// Deque<Double> entryTimes = new ArrayDeque<Double>();
		// Deque<Double> exitTimes = new ArrayDeque<Double>();
		double currTime = 0.0;
		Event currEvent = null;
		double lastExitTime = -1;
		double timeLimit = 10;

		/**
		 * vertexID -> (arrivalTime, maxDepartureTime) This will be updated
		 * after every transplant as well as every arrival of a new patient
		 */
		Map<Integer, Pair<Double, Double>> patientTimes = new HashMap<Integer, Pair<Double, Double>>();

		Queue<Pair<Double, Vertex>> verticesByExitTime = new PriorityQueue<Pair<Double, Vertex>>(
				(int) timeLimit, new VertexTimeComparator());

		Queue<Pair<Double, Vertex>> verticesByEntryTime = new PriorityQueue<Pair<Double, Vertex>>(
				(int) timeLimit, new VertexTimeComparator());

		/**
		 * A deque that maintains the set of cycle transplants that need to
		 * happen and at what time.
		 */
		Queue<Pair<Double, Cycle>> cycleTransplantTimes = new PriorityQueue<Pair<Double, Cycle>>(
				1, new CycleTimeComparator());
		Queue<Double> matchingTimes = new LinkedList<Double>();

		// Deque<Pair<Cycle, Double>> cycleTransplantTimes = new
		// ArrayDeque<Pair<Cycle, Double>>();

		// create cycle and chain detection schedule

		// create initial entry and exit times for the individuals already in
		// the pool
		for (VertexPair v : pool.getPairs()) {

			/*
			 * get the times at which the patients/donor pairs enter the pool
			 * and exit the pool (by means of death :-/)
			 */
			if (!patientTimes.containsKey(v.getID())) {

				/*
				 * Draw arrival and departure times from exponential clocks It
				 * is okay if entryTime == exitTime; the vertex enters and is
				 * critical simultaneously
				 */
				double entryTime = currTime + arrivalTimeGen.draw();
				double exitTime = entryTime + lifespanTimeGen.draw();
				currTime = entryTime;

				if (currTime > timeLimit) {
					/*
					 * If this vertex would arrive after the time limit, we're
					 * done generating
					 */
					break;
				}
				if (exitTime > lastExitTime) {
					/*
					 * Store the exit time of the final vertex in the pool
					 * (after all other vertices have gone critical)
					 */
					lastExitTime = exitTime;
				}

				patientTimes.put(v.getID(), new Pair<Double, Double>(entryTime,
						exitTime));

				// we don't care when these vertices entered, because they're
				// already in the pool
				verticesByExitTime.add(new Pair<Double, Vertex>(exitTime, v));

			}

		}
		double startingTime = currTime;
		logger.info("Last entry : " + startingTime);
		/*
		 * NOTE : Time needs to be relevant with the exit times of the vertex
		 * pairs that are already in the pool, therefore I guess we can start
		 * the simulation at the time the last vertex pair in the pool entered
		 * the pool, and run it for some time timeLimit after that. Also, we're
		 * assuming no matchings have happened before the last vertex pair has
		 * entered. This gives a disadvantage to everyone who's already in the
		 * pool...
		 */

		/* Do the matchings at a regular interval */
		double detectionTime = currTime;

		// set the interval for when to do the matchings (every interval days);
		double interval = 1;

		while (true) {

			if (detectionTime >= timeLimit) {
				break;
			} else {
				detectionTime += interval;
			}

			matchingTimes.add(detectionTime);
		}

		System.out.println("Entry time of the last patient: " + currTime);

		// add new arrivals that will take place afterwards (only add pairs not
		// altruists for now)
		// TODO with some probability, add an altruist
		logger.info("Scheduling new vertex pair arrivals...");
		while (true) {
			int addPair = 1;
			int addAltruist = 0;
			Set<Vertex> l = SaidmanGen.addVerticesToPool(pool, addPair,
					addAltruist);

			// we're only generating one at a time
			Vertex v = l.iterator().next();

			/*
			 * Draw arrival and departure times from exponential clocks It is
			 * okay if entryTime == exitTime; the vertex enters and is critical
			 * simultaneously
			 */
			double entryTime = currTime + arrivalTimeGen.draw();
			double exitTime = entryTime + lifespanTimeGen.draw();
			currTime = entryTime;

			if (currTime > timeLimit) {
				/*
				 * If this vertex would arrive after the time limit, we're done
				 * generating
				 */
				break;
			}
			if (exitTime > lastExitTime) {
				/*
				 * Store the exit time of the final vertex in the pool (after
				 * all other vertices have gone critical)
				 */
				lastExitTime = exitTime;
			}

			// patientTimes.put(v.getID(), new Pair<Double,
			// Double>(entryTime,
			// exitTime));

			// add tp the appropriate queues
			verticesByExitTime.add(new Pair<Double, Vertex>(exitTime, v));
			verticesByEntryTime.add(new Pair<Double, Vertex>(entryTime, v));
			// System.out.println("currTime: " + currTime);
		}

		currTime = startingTime;

		// get starting event before the simulation starts running.
		// the next event can't be a transplant because we haven't had any
		// matchings yet
		currEvent = Event.getNextEvent(matchingTimes, cycleTransplantTimes,
				verticesByExitTime, verticesByEntryTime);

		/*
		 * the first event is probably going to be a death event from the
		 * initial pool
		 */
		currTime = currEvent.getTime();

		logger.info("First event is: " + currEvent);
		// System.exit(0);
		logger.info("The following events will be executed");
		System.out.println("Matching Times: " + matchingTimes);
		System.out.println("CycleTransplant Times: " + cycleTransplantTimes);
		System.out.println("VerticesBy Exit Time: " + verticesByExitTime);
		System.out.println("VerticesBy Entry Time: " + verticesByEntryTime);

		System.exit(0);
		while (currTime < timeLimit) {

			logger.info("AT TIME: " + currTime);
			if (currEvent.getType().equals(EventType.TERMINATE_SIMULATION)) {
				logger.info("NO MORE EVENTS TO PROCESS...TERMINATING SIMULATION...");
				break;
			}
			if (currEvent.getType().equals(EventType.PATIENT_DIES)) {

				/*
				 * remove vertex pair from the pool, remove the event from the
				 * queue
				 */
				Vertex toRemove = verticesByExitTime.poll().getRight();
				if (pool.removeVertex(toRemove)) {
					logger.info("We lost a patient... RIP");
				}

				/*
				 * break and remove the patient from any scheduled transplants,
				 * cycles. We've lost them :/
				 */
				Iterator<Pair<Double, Cycle>> it = cycleTransplantTimes
						.iterator();
				while (it.hasNext()) {
					Pair<Double, Cycle> cc = it.next();
					if (Cycle.isAChain(cc.getRight(), pool)) {
						// TODO How do we treat chains?
					} else {

						// the chain breaks, so we need to remove it completely
						if (Cycle.getConstituentVertices(cc.getRight(), pool)
								.contains(toRemove)) {
							it.remove();
						}
					}
				}

			}

			// update patient
			if (currEvent.getType().equals(EventType.PATIENT_ENTERS)) {
				logger.info("Adding new patient..");

				/* add new vertex to the pool */
				VertexPair toAdd = (VertexPair) verticesByEntryTime.poll()
						.getRight();
				pool.addPair(toAdd);

			}

			// check if it's time to do matchings
			if (currEvent.getType().equals(EventType.CONDUCT_MATCHINGS)) {
				// run matching, update
				logger.info("conducting matchings..");

				/*
				 * add cycles and the time they should happen to the priority
				 * queue TODO make this more sophisticated, it doesn't make
				 * sense to do the cycles simple in the order we found thems
				 */

				/*
				 * add cycles and the times at which the transplants will happen
				 */
				Solution s = conductMatches(pool, cg, cycleCap, chainCap);

				/*
				 * TODO the time of the transplant is going to be some interval
				 * in the near future (is this correct?)
				 */
				for (Cycle c : s.getMatching()) {
					cycleTransplantTimes.add(new Pair<Double, Cycle>(
							(currTime + transplantTimeGen.draw()), c));
				}

			}

			if (currEvent.getType().equals(EventType.CONDUCT_TRANSPLANT)) {
				logger.info("transplant done...");

				/*
				 * TODO conducting a transplant means removing the patients from
				 * the pool properly (is this correct?)
				 * 
				 * We don't need to remove the exit event from the
				 * vertexExitevents queue, because we check if the vertex is
				 * still in the pool before doing anything in the PATIENT_DIES
				 * event
				 */
				Cycle toTransplant = cycleTransplantTimes.poll().getRight();
				for (Vertex v : Cycle
						.getConstituentVertices(toTransplant, pool)) {
					pool.removeVertex(v);
				}
			}

			// TODO Handle the case where more than one event happens at a
			// single point in time

			// Queue[] nextEv = { matchingTimes, cycleTransplantTimes,
			// verticesByExitTime, verticesByEntryTime };

			currEvent = Event.getNextEvent(matchingTimes, cycleTransplantTimes,
					verticesByExitTime, verticesByEntryTime);
			currTime = currEvent.getTime();
			// System.exit(0);
		}
		;

	}

	private static Solution conductMatches(Pool pool, CycleGenerator cg,
			int cycleCap, int chainCap) {
		boolean usingFailureProbabilities = false;
		double failure_param1 = 0.7; // e.g., constant failure rate of 70%
		try {

			// This should happen routinely (twice a week)

			List<Cycle> cyclesUB2 = cg.generateCyclesAndChains(cycleCap,
					chainCap, usingFailureProbabilities, false, failure_param1);
			CycleMembership membershipUB2 = new CycleMembership(pool, cyclesUB2);

			// Get optimal match size for pool on C(3,some non-infinite
			// chain cap + infinite extension)
			CycleFormulationCPLEXSolver optIPUB2S = new CycleFormulationCPLEXSolver(
					pool, cyclesUB2, membershipUB2);
			Solution sol = optIPUB2S.solve();

			logger.info(sol.getMatching().toString());
			IOUtil.dPrintln("'Optimal IP' (UB extension) Value: "
					+ sol.getObjectiveValue());
			// Try to GC
			cyclesUB2 = null;
			membershipUB2 = null;
			System.gc();

			return sol;
		} catch (SolverException e) {
			e.printStackTrace();
			System.exit(-1);
			return null;
		}
	}
	// Simulation
	// t is the current time we're at and allocated_time is the maximum time we
	// should allocate to this simulation (for how long we should run it)

	// eventQueue = new Queue();
	// Do matches, add set of transplant events in the queue (these happen back
	// to back?)
	//
	// while(t < allocated_time)

	// if 1 week has passed re-run matches

	// while(true)
	//

	// t += nextEvent().getTime();

}
