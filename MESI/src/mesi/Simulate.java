package mesi;

import static mesi.Constants.*;
import java.util.Random;

public class Simulate {
	public static void main(String args[]) {
		System.out.println("MESI protocol simulation");
		mesi();
		System.out.println("MEOSI protocol simulation");
		meosi();
	}

	/**
	 * 
	 */
	public static void mesi() {
		// Coherence protocol
		int readCoherence = 0;
		int writeCoherence = 0;
		// Frequency of state changes
		int readTransition = 0;
		int writeTransition = 0;

		int continueTill = 0;
		int totalNumberOfIterations = 1000000;

		Random random = new Random();
		Processor processorList[] = new Processor[NUM_PROCESSOR];
		for (int i = 0; i < NUM_PROCESSOR; i++) {
			processorList[i] = new Processor();
		}

		// Running it for 1000000 iterations
		while (continueTill < totalNumberOfIterations) {
			int procNumber = random.nextInt(NUM_PROCESSOR); // choosing the
															// processor
			int readOrWrite = random.nextInt(NUM_PROCESSOR); // choosing between
																// read and
																// write
			int memBlockNumber = random.nextInt(NUM_BLOCKS_MEMORY); // Choosing
																	// a random
																	// block in
																	// memory
			int blockNumber = memBlockNumber % NUM_BLOCKS;

			// System.out.println("CYCLE "+ (continueTill+1)+" READ OR WRITE "+
			// readOrWrite);
			/* Read Operation */
			if (readOrWrite == 0) {

				if (processorList[procNumber].cacheObj.blockList[blockNumber].address != memBlockNumber) {

					int firstBlockState = processorList[procNumber].cacheObj
							.getState(memBlockNumber);

					switch (firstBlockState) {
					case M:
						readCoherence++; // Actually should be set to Error

					case S:
						// Writing back to memory
						processorList[(procNumber + 1) % NUM_PROCESSOR].cacheObj
								.setExclusive(memBlockNumber);
						readTransition++;
					}
				}

				// Assumption that there is only two processors
				int firstBlockState = processorList[procNumber].cacheObj
						.getState(memBlockNumber);
				int secondBlockState = processorList[(procNumber + 1)
						% NUM_PROCESSOR].cacheObj.getState(memBlockNumber);

				switch (firstBlockState) {
				case ERROR:
					switch (secondBlockState) {
					case M:
						processorList[procNumber].cacheObj
								.setShared(memBlockNumber);
						processorList[(procNumber + 1) % NUM_PROCESSOR].cacheObj
								.setShared(memBlockNumber);
						// for MESI the block has to be written into memory
						readCoherence += 2;
						// Both change their states
						readTransition += 2;
						break;
					case E:
						processorList[procNumber].cacheObj
								.setShared(memBlockNumber);
						processorList[(procNumber + 1) % NUM_PROCESSOR].cacheObj
								.setShared(memBlockNumber);
						readCoherence++;
						readTransition += 2;
						break;
					case S:
						processorList[procNumber].cacheObj
								.setShared(memBlockNumber);
						readCoherence++;
						readTransition++;
						break;
					case I:
					case ERROR:
						processorList[procNumber].cacheObj
								.setExclusive(memBlockNumber);
						readCoherence++;
						readTransition++;
						break;

					}
					break;

				case I:
					switch (secondBlockState) {
					case M:
						processorList[procNumber].cacheObj
								.setShared(memBlockNumber);
						processorList[(procNumber + 1) % NUM_PROCESSOR].cacheObj
								.setShared(memBlockNumber);
						readCoherence += 2;
						readTransition += 2;
						break;
					case E:
						processorList[procNumber].cacheObj
								.setShared(memBlockNumber);
						processorList[(procNumber + 1) % NUM_PROCESSOR].cacheObj
								.setShared(memBlockNumber);
						readCoherence++;
						readTransition += 2;
						break;
					case S:
						// this case is not possible
						break;
					case I:
					case ERROR:
						processorList[procNumber].cacheObj
								.setExclusive(memBlockNumber);
						readCoherence++;
						readTransition++;
						break;

					}
					break;
				case E:
					switch (secondBlockState) {
					case M:
					case E:
					case S:
					case I:
					case ERROR:
						break;

					}
					break;
				case S:
					switch (secondBlockState) {
					case M:
					case E:
					case S:
					case I:
					case ERROR:
						break;

					}
					break;
				case M:
					switch (secondBlockState) {

					case M:
					case E:
					case S:
					case I:
					case ERROR:
						break;

					}
					break;
				}
				processorList[procNumber].cacheObj.blockList[blockNumber].address = memBlockNumber;
				/*
				 * firstBlockState = processorList[procNumber].cacheObj
				 * .getState(memBlockNumber); secondBlockState =
				 * processorList[(procNumber + 1) %
				 * NUM_PROCESSOR].cacheObj.getState(memBlockNumber);
				 * System.out.println(" Block State   :" + firstBlockState +
				 * " Proc Number "+ procNumber + " ,Second Block State  :" +
				 * secondBlockState+ " Proc Number "+
				 * (procNumber+1)%NUM_PROCESSOR);
				 */
			} else {
				/* WRITE Operation */

				if (processorList[procNumber].cacheObj.blockList[blockNumber].address != memBlockNumber) {
					// assert false
					int firstBlockState1 = processorList[procNumber].cacheObj
							.getState(memBlockNumber);
					switch (firstBlockState1) {
					case M:
						writeCoherence++;
						// Actually should be set to Error

					case S:
						processorList[(procNumber + 1) % NUM_PROCESSOR].cacheObj
								.setExclusive(memBlockNumber);
						writeTransition++;
					}
				}
				int firstBlockState = processorList[procNumber].cacheObj
						.getState(memBlockNumber);
				int secondBlockState = processorList[(procNumber + 1)
						% NUM_PROCESSOR].cacheObj.getState(memBlockNumber);

				switch (firstBlockState) {
				case ERROR:
					switch (secondBlockState) {

					case M:
						// Other cache has the required block in modified state
						processorList[procNumber].cacheObj
								.setModified(memBlockNumber);
						processorList[(procNumber + 1) % NUM_PROCESSOR].cacheObj
								.setInvalidate(memBlockNumber);
						writeCoherence += 2;
						writeTransition += 2;
						break;
					case E:
						processorList[procNumber].cacheObj
								.setModified(memBlockNumber);
						processorList[(procNumber + 1) % NUM_PROCESSOR].cacheObj
								.setInvalidate(memBlockNumber);
						writeCoherence++;
						writeTransition += 2;
						break;

					case S:
						assert (false); // Not possible
						break;
					case I:
					case ERROR:
						processorList[procNumber].cacheObj
								.setModified(memBlockNumber);
						writeCoherence++;
						writeTransition++;
						break;
					}
					break;
				case I:
					switch (secondBlockState) {

					case M:
						processorList[procNumber].cacheObj
								.setModified(memBlockNumber);
						processorList[(procNumber + 1) % NUM_PROCESSOR].cacheObj
								.setInvalidate(memBlockNumber);
						writeCoherence += 2;
						writeTransition += 2;
						break;

					case E:
						processorList[procNumber].cacheObj
								.setModified(memBlockNumber);
						processorList[(procNumber + 1) % NUM_PROCESSOR].cacheObj
								.setInvalidate(memBlockNumber);
						writeCoherence++;
						writeTransition += 2;
						break;

					case S:
						break;

					case I:
					case ERROR:
						processorList[procNumber].cacheObj
								.setModified(memBlockNumber);
						writeCoherence++;
						writeTransition++;
						break;
					}
					break;

				case E:
					switch (secondBlockState) {
					case M:
					case E:
					case S:
						assert (false);
						break;
					case I:
					case ERROR:
						processorList[procNumber].cacheObj
								.setModified(memBlockNumber);
						writeTransition++;
						break;
					}
					break;
				case S:
					switch (secondBlockState) {
					case M:
					case E:
						assert (false);
						break;
					case S:
						processorList[procNumber].cacheObj
								.setModified(memBlockNumber);
						processorList[(procNumber + 1) % NUM_PROCESSOR].cacheObj
								.setInvalidate(memBlockNumber);
						writeCoherence++;
						writeTransition += 2;
						break;
					case I:
						assert (false);
						break;

					}
					break;
				case M:
					switch (secondBlockState) {
					case M:
					case E:
					case S:
					case I:
						assert (false);
						break;
					}
					break;

				}
				// The block has been written- Therefore state changed to
				// Modified and address is changed
				processorList[procNumber].cacheObj.setModified(memBlockNumber);
				processorList[procNumber].cacheObj.blockList[blockNumber].address = memBlockNumber;
				/*
				 * firstBlockState = processorList[procNumber].cacheObj
				 * .getState(memBlockNumber); secondBlockState =
				 * processorList[(procNumber + 1) %
				 * NUM_PROCESSOR].cacheObj.getState(memBlockNumber);
				 * System.out.println(" Block State   :" + firstBlockState +
				 * " Proc Number "+ procNumber + " ,Second Block State  :" +
				 * secondBlockState+ " Proc Number "+
				 * (procNumber+1)%NUM_PROCESSOR);
				 */
			}

			continueTill++;
		}

		System.out.println(" Read Coherences = " + readCoherence
				+ " Write Coherences " + writeCoherence);
		System.out.println("Read State Transitions " + readTransition
				+ " Write State Transitions " + writeTransition);

	}

	public static void meosi() {

		int readCoherence = 0;
		int writeCoherence = 0;
		int readTransition = 0;
		int writeTransition = 0;
		int continueTill = 0;
		int totalNumberOfIterations = 1000000;
		Random random = new Random();
		Processor processorList[] = new Processor[NUM_PROCESSOR];
		for (int i = 0; i < NUM_PROCESSOR; i++) {
			processorList[i] = new Processor();
		}

		// Running it for 1000000 iterations
		while (continueTill < totalNumberOfIterations) {
			int procNumber = random.nextInt(NUM_PROCESSOR);
			int readOrWrite = random.nextInt(NUM_PROCESSOR);
			int memBlockNumber = random.nextInt(NUM_BLOCKS_MEMORY);

			int blockNumber = memBlockNumber % NUM_BLOCKS;
			// System.out.println("CYCLE "+ (continueTill+1)+" READ OR WRITE "+
			// readOrWrite);
			if (readOrWrite == 0) {

				if (processorList[procNumber].cacheObj.blockList[blockNumber].address != memBlockNumber) {

					int firstBlockState = processorList[procNumber].cacheObj
							.getState(memBlockNumber);
					int addressBlock2 = processorList[(procNumber + 1)
							% NUM_PROCESSOR].cacheObj.blockList[blockNumber].address;
					int secondBlockState2 = processorList[(procNumber + 1)
							% NUM_PROCESSOR].cacheObj.getState(addressBlock2);
					switch (firstBlockState) {
					case M:
						readCoherence++;
						// Actually should be set to Error
						break;
					case S:
						switch (secondBlockState2) {
						case S:
							// The second processor is set to Exclusive since
							// this writes back
							processorList[(procNumber + 1) % NUM_PROCESSOR].cacheObj
									.setExclusive(addressBlock2);
							readTransition++;
							break;

						}

					}
				}

				// Assumption that there is only two processors
				int firstBlockState = processorList[procNumber].cacheObj
						.getState(memBlockNumber);
				int secondBlockState = processorList[(procNumber + 1)
						% NUM_PROCESSOR].cacheObj.getState(memBlockNumber);
				switch (firstBlockState) {
				case ERROR:
					switch (secondBlockState) {
					case M:
						processorList[procNumber].cacheObj
								.setShared(memBlockNumber);
						processorList[(procNumber + 1) % NUM_PROCESSOR].cacheObj
								.setOwned(memBlockNumber);
						// Note: For MESI the block has to be written into
						// memory unlike here
						readCoherence++;
						// both have them have a change in states
						readTransition += 2;
						break;
					case E:
						processorList[procNumber].cacheObj
								.setShared(memBlockNumber);
						processorList[(procNumber + 1) % NUM_PROCESSOR].cacheObj
								.setShared(memBlockNumber);
						readCoherence++;
						readTransition += 2;
						break;
					case S:
						processorList[procNumber].cacheObj
								.setShared(memBlockNumber);
						readCoherence++;
						readTransition++;
						assert (false);// In our case not possible - General
										// scenario possible
						break;
					case I:
					case ERROR:
						processorList[procNumber].cacheObj
								.setExclusive(memBlockNumber);
						readCoherence++;
						readTransition++;
						break;
					case O:
						processorList[procNumber].cacheObj
								.setShared(memBlockNumber);
						readTransition++;

						break;

					}
					break;

				case I:
					switch (secondBlockState) {
					case M:
						processorList[procNumber].cacheObj
								.setShared(memBlockNumber);
						// Different from MESI protocol
						processorList[(procNumber + 1) % NUM_PROCESSOR].cacheObj
								.setOwned(memBlockNumber);
						readCoherence++;
						readTransition += 2;
						break;
					case E:
						processorList[procNumber].cacheObj
								.setShared(memBlockNumber);
						processorList[(procNumber + 1) % NUM_PROCESSOR].cacheObj
								.setShared(memBlockNumber);
						readCoherence++;
						readTransition += 2;
						break;
					case S:
						// this case is not possible
						break;
					case I:
					case ERROR:
						processorList[procNumber].cacheObj
								.setExclusive(memBlockNumber);
						readCoherence++;
						readTransition++;
						break;
					case O:
						processorList[procNumber].cacheObj
								.setShared(memBlockNumber);
						readTransition++;
						break;

					}
					break;
				case E:
					switch (secondBlockState) {

					case M:
					case E:
					case S:
					case I:
					case ERROR:
						break;

					}
					break;

				case S:
					switch (secondBlockState) {
					case M:
					case E:
					case S:
					case I:
					case ERROR:
						break;
					}
					break;
				case M:
					switch (secondBlockState) {

					case M:
					case E:
					case S:
					case I:
					case ERROR:
						break;

					}
					break;

				case O:
					switch (secondBlockState) {
					// No state changes
					}
				}
				// Setting the address after reading
				processorList[procNumber].cacheObj.blockList[blockNumber].address = memBlockNumber;
				/*
				 * firstBlockState = processorList[procNumber].cacheObj
				 * .getState(memBlockNumber); secondBlockState =
				 * processorList[(procNumber + 1) %
				 * NUM_PROCESSOR].cacheObj.getState(memBlockNumber);
				 * System.out.println(" Block State   :" + firstBlockState +
				 * " Proc Number "+ procNumber + " ,Second Block State  :" +
				 * secondBlockState+ " Proc Number "+
				 * (procNumber+1)%NUM_PROCESSOR);
				 */
				/* WRITE Operation */
			} else {
				if (processorList[procNumber].cacheObj.blockList[blockNumber].address != memBlockNumber) {

					int firstBlockState1 = processorList[procNumber].cacheObj
							.getState(memBlockNumber);
					int addressBlock2 = processorList[(procNumber + 1)
							% NUM_PROCESSOR].cacheObj.blockList[blockNumber].address;
					int secondBlockState2 = processorList[(procNumber + 1)
							% NUM_PROCESSOR].cacheObj.getState(addressBlock2);
					switch (firstBlockState1) {
					case M:
						writeCoherence++;
						// Actually should be set to Error
						break;
					case S:
						switch (secondBlockState2) {
						case S:
							processorList[(procNumber + 1) % NUM_PROCESSOR].cacheObj
									.setExclusive(addressBlock2);
							writeTransition++;
							break;

						}

					case O:
						writeCoherence++;
						if (secondBlockState2 == S)
							processorList[(procNumber + 1) % NUM_PROCESSOR].cacheObj
									.setExclusive(addressBlock2);

					}
				}
				int firstBlockState = processorList[procNumber].cacheObj
						.getState(memBlockNumber);
				int secondBlockState = processorList[(procNumber + 1)
						% NUM_PROCESSOR].cacheObj.getState(memBlockNumber);
				switch (firstBlockState) {
				case ERROR:
					switch (secondBlockState) {

					case M:
						// Other cache has the required block in modified state
						processorList[procNumber].cacheObj
								.setModified(memBlockNumber);
						processorList[(procNumber + 1) % NUM_PROCESSOR].cacheObj
								.setInvalidate(memBlockNumber);
						writeCoherence++;
						writeTransition += 2;
						break;
					case E:
						processorList[procNumber].cacheObj
								.setModified(memBlockNumber);
						processorList[(procNumber + 1) % NUM_PROCESSOR].cacheObj
								.setInvalidate(memBlockNumber);
						writeCoherence++;
						writeTransition += 2;
						break;

					case S:
						assert (false); // Possible in general scenario of n
										// processors
						break;
					case I:
					case ERROR:
						processorList[procNumber].cacheObj
								.setModified(memBlockNumber);
						writeCoherence++;
						writeTransition++;
						break;
					case O:
						processorList[(procNumber + 1) % NUM_PROCESSOR].cacheObj
								.setInvalidate(memBlockNumber);
						processorList[procNumber].cacheObj
								.setModified(memBlockNumber);
						writeTransition += 2;
						writeCoherence++;

					}
					break;
				case I:
					switch (secondBlockState) {

					case M:

						processorList[procNumber].cacheObj
								.setModified(memBlockNumber);
						processorList[(procNumber + 1) % NUM_PROCESSOR].cacheObj
								.setInvalidate(memBlockNumber);
						writeCoherence++;
						writeTransition += 2;
						break;

					case E:
						processorList[procNumber].cacheObj
								.setModified(memBlockNumber);
						processorList[(procNumber + 1) % NUM_PROCESSOR].cacheObj
								.setInvalidate(memBlockNumber);
						writeCoherence++;
						writeTransition += 2;
						break;

					case S:
						break;

					case I:
					case ERROR:
						processorList[procNumber].cacheObj
								.setModified(memBlockNumber);
						writeCoherence++;
						writeTransition++;
						break;
					case O:
						processorList[(procNumber + 1) % NUM_PROCESSOR].cacheObj
								.setInvalidate(memBlockNumber);
						processorList[procNumber].cacheObj
								.setModified(memBlockNumber);
						writeTransition += 2;
						writeCoherence++;
					}
					break;

				case E:
					switch (secondBlockState) {
					case M:
					case E:
					case S:
					case O:
						assert (false);
						break;
					case I:
					case ERROR:
						processorList[procNumber].cacheObj
								.setModified(memBlockNumber);
						writeTransition++;
						break;
					}
					break;
				case S:
					switch (secondBlockState) {
					case M:
					case E:
						assert (false);
						break;
					case S:
						processorList[procNumber].cacheObj
								.setModified(memBlockNumber);
						processorList[(procNumber + 1) % NUM_PROCESSOR].cacheObj
								.setInvalidate(memBlockNumber);
						writeCoherence++;
						writeTransition += 2;
						break;
					case O:
						processorList[procNumber].cacheObj
								.setModified(memBlockNumber);
						processorList[(procNumber + 1) % NUM_PROCESSOR].cacheObj
								.setInvalidate(memBlockNumber);
						writeTransition += 2;
						break;

					case I:
						assert (false);
						break;

					}
					break;
				case M:
					switch (secondBlockState) {
					case M:
					case E:
					case S:
					case I:
					case O:
						assert (false);
						break;
					}
					break;
				case O:

					switch (secondBlockState) {
					case ERROR:
						processorList[(procNumber + 1) % NUM_PROCESSOR].cacheObj
								.setInvalidate(memBlockNumber);
						processorList[procNumber].cacheObj
								.setModified(memBlockNumber);
						writeTransition += 2;
						break;
					case I:
						processorList[procNumber].cacheObj
								.setModified(memBlockNumber);
						writeTransition++;
						break;

					}

				}
				// Setting the block to Modified state and setting its address
				processorList[procNumber].cacheObj.setModified(memBlockNumber);
				processorList[procNumber].cacheObj.blockList[blockNumber].address = memBlockNumber;
				/*
				 * firstBlockState = processorList[procNumber].cacheObj
				 * .getState(memBlockNumber); secondBlockState =
				 * processorList[(procNumber + 1) %
				 * NUM_PROCESSOR].cacheObj.getState(memBlockNumber);
				 * System.out.println(" Block State   :" + firstBlockState +
				 * " Proc Number "+ procNumber + " ,Second Block State  :" +
				 * secondBlockState+ " Proc Number "+
				 * (procNumber+1)%NUM_PROCESSOR);
				 */

			}
			continueTill++;
		}
		System.out.println(" Read Coherences = " + readCoherence
				+ " Write Coherences " + writeCoherence);
		System.out.println("Read State Transitions " + readTransition
				+ " Write State Transitions " + writeTransition);

	}
}
