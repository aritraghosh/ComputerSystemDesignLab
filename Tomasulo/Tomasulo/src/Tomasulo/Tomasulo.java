/**
 * 
 */
package Tomasulo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream.GetField;
import java.security.AllPermission;
import java.util.*;

public class Tomasulo {

	public static Data data = Data.getInstance();


	public static void main(String[] args) {

		registerInit("src/Tomasulo/registerInit");
		for (int i = 0; i < data.MAX_REGISTERS; i++)
			data.renamedRegisters[i] = new RegisterFileEntry();

		memoryInit("src/Tomasulo/memoryInit");
		latencyInput("src/Tomasulo/LatencyConfig");

		String string;
		Scanner input = new Scanner(System.in);
		string = input.next();

		while (string.compareTo("null") != 0) {
			Instruction instruction = new Instruction();

			// instruction id for each instruction
			instruction.id = data.global_ins_counter++;

			instruction.instr = string;

			if (instruction.instr.equals("load")
					|| instruction.instr.equals("store")) {
				instruction.dest = input.next();
				instruction.src1 = input.next();
			}

			else {
				instruction.dest = input.next();
				instruction.src1 = input.next();
				instruction.src2 = input.next();
			}

			data.instructionIdType.put(instruction.id, instruction.instr);
			data.allInstructions.add(instruction);
			string = input.next();
		}

		for (int i = 0; i < data.MAX_FU; i++)
			data.functionalUnits[i] = new FunctionalUnit();

		// start of simulation
		data.store_counter = 0;
		simulate();

		for (int i = 0; i < data.MAX_REGISTERS; i++)
			System.out.println("Value of R" + i + ": "
					+ data.registers[i].operand.value);

		for (int i = 0; i < 100; i++)
			if (data.memory[i] != 0)
				System.out.println("Address: " + i + "------- value: "
						+ data.memory[i]);
	}

	public static void registerInit(String filename){
		BufferedReader br = null;

		try {

			String sCurrentLine;
			int i=0;
			br = new BufferedReader(new FileReader(filename));

			while ((sCurrentLine = br.readLine()) != null) {
				
				
					data.registers[i++] = new RegisterFileEntry(Integer.parseInt(sCurrentLine));


			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
	}
	
	public static void memoryInit(String filename){
		BufferedReader br = null;

		try {

			String sCurrentLine;
			int i=0;
			br = new BufferedReader(new FileReader(filename));

			while ((sCurrentLine = br.readLine()) != null) {
				
				
				data.memory[i++] = Integer.parseInt(sCurrentLine);


			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
	}
	
	public static void latencyInput(String filename) {
		
		BufferedReader br = null;

		try {

			String sCurrentLine;

			br = new BufferedReader(new FileReader(filename));

			while ((sCurrentLine = br.readLine()) != null) {
				String[] linecontent = sCurrentLine.split("\t");
				
				data.latency.put(linecontent[0],Integer.parseInt(linecontent[1]));


			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	public static void simulate() {
		int cycleCount = 0;
		Instruction instruction = new Instruction();
		Vector<Instruction> nextInstruction = new Vector<Instruction>();
		int currentInstruction = 0;

		while (true) {
			// fetching a specific number of instructions
			while (currentInstruction < data.allInstructions.size()
					&& data.reorderBuffer.size() < data.MAX_REORDERBUFFER_SIZE) {
				
				nextInstruction.add(data.allInstructions
						.get(currentInstruction));
				if (data.allInstructions.get(currentInstruction).instr
						.compareTo("store") == 0
						|| data.allInstructions.get(currentInstruction).instr
								.compareTo("load") == 0)
					data.memoryAccessQueue.add(new MemoryAccessEntry(
							data.allInstructions.get(currentInstruction)));

				data.reorderBuffer.add(new ReorderBufferEntry(
						data.allInstructions.get(currentInstruction).id));
				currentInstruction++;
			}

			if (nextInstruction.isEmpty() && data.reorderBuffer.isEmpty()
					&& currentInstruction >= data.allInstructions.size())
				break;

			while (nextInstruction.size() > 0) {
				instruction = nextInstruction.get(0);

				boolean isRRFree = false;
				int freeRR = -1;
				if (instruction.instr.compareTo("store") != 0) {
					for (int j = 0; j < data.MAX_REGISTERS; j++) {
						if (!data.renamedRegisters[j].isBusy()) {
							isRRFree = true;
							freeRR = j;
							break;
						}
					}

					if (!isRRFree)
						break;
				}

				if (data.reservationStation.size() >= data.MAX_RESERVATIONSTATION_SIZE)
					break;

				// push a new instruction into reservation station
				ReservationStationEntry reservationStationEntry = new ReservationStationEntry(
						instruction.id);

				// remove the instruction from the next instruction
				nextInstruction.remove(0);

				boolean isLoad = (data.instructionIdType.get(instruction.id)
						.compareTo("load") == 0);
				boolean isStore = (data.instructionIdType.get(instruction.id)
						.compareTo("store") == 0);

				if (!isLoad && !isStore) {
					// try to push the instruction into ALU
					int src1 = getRegisterNumber(instruction.src1);

					if (src1 == -1) {
						src1 = Integer.parseInt(instruction.src1);
						reservationStationEntry.operand1.setValue(src1);
					} else if (data.registers[src1].isBusy()) {
						// System.out.println("enter" + src1
						// +"---"+data.renamedRegisters[data.registers[src1]
						// .getTag()].operand.isValid()+"----"+data.registers[src1].getTag());
						if (!data.renamedRegisters[data.registers[src1]
								.getTag()].operand.isValid())
							reservationStationEntry.operand1
									.setTag(data.registers[src1].getTag());
						else
							reservationStationEntry.operand1
									.setValue(data.renamedRegisters[data.registers[src1]
											.getTag()].operand.getValue());
					} else {
						reservationStationEntry.operand1
								.setValue(data.registers[src1].getValue());
						// reservationStationEntry.operand1.setTag(-1);
					}

					int src2 = getRegisterNumber(instruction.src2);
					if (src2 == -1) {
						src2 = Integer.parseInt(instruction.src2);
						reservationStationEntry.operand2.setValue(src2);
					} else if (data.registers[src2].isBusy()) {
						if (!data.renamedRegisters[data.registers[src2]
								.getTag()].operand.isValid())
							reservationStationEntry.operand2
									.setTag(data.registers[src2].getTag());
						else
							reservationStationEntry.operand2
									.setValue(data.renamedRegisters[data.registers[src2]
											.getTag()].operand.getValue());
					} else {
						reservationStationEntry.operand2
								.setValue(data.registers[src2].getValue());
						// reservationStationEntry.operand2.setTag(-1);
					}

					int dest = getRegisterNumber(instruction.dest);

					// renaming of register
					System.out.println("Free RR  " + freeRR);
					data.registers[dest].setTag(freeRR);
					data.registers[dest].busyBit = true;
					instruction.RRtag = freeRR;
					data.renamedRegisters[freeRR].operand.tagBit = 0; // not
																		// valid
					data.renamedRegisters[freeRR].busyBit = true;
					data.reservationStation.add(reservationStationEntry);

				}

				// load
				else if (isLoad) {
					// try to push the instruction into ALU
					int src1 = getRegisterNumber(instruction.src1);

					if (src1 == -1) {
						src1 = Integer.parseInt(instruction.src1);
						reservationStationEntry.operand1.setValue(src1);
					}

					else if (data.registers[src1].isBusy()) {
						if (!data.renamedRegisters[data.registers[src1]
								.getTag()].operand.isValid())
							reservationStationEntry.operand1
									.setTag(data.registers[src1].getTag());
						else
							reservationStationEntry.operand1
									.setValue(data.renamedRegisters[data.registers[src1]
											.getTag()].operand.getValue());
					} else {
						reservationStationEntry.operand1
								.setValue(data.registers[src1].getValue());
					}

					int dest = getRegisterNumber(instruction.dest);

					// renaming of register
					System.out.println("Free RR for load " + freeRR);
					data.registers[dest].setTag(freeRR);
					data.registers[dest].busyBit = true;
					instruction.RRtag = freeRR;
					data.renamedRegisters[freeRR].operand.tagBit = 0; // not
																		// valid
					data.renamedRegisters[freeRR].busyBit = true;
					data.reservationStation.add(reservationStationEntry);
				}

				// store
				else {
					int src1 = getRegisterNumber(instruction.src1);

					if (src1 == -1) {
						System.out.println("Not a valid input");
						System.exit(0);
					}

					else if (data.registers[src1].isBusy()) {
						if (!data.renamedRegisters[data.registers[src1]
								.getTag()].operand.isValid())
							reservationStationEntry.operand2
									.setTag(data.registers[src1].getTag());
						else
							reservationStationEntry.operand2
									.setValue(data.renamedRegisters[data.registers[src1]
											.getTag()].operand.getValue());
					} else 
						reservationStationEntry.operand2
								.setValue(data.registers[src1].getValue());

					int dest = getRegisterNumber(instruction.dest);

					if (dest == -1) {
						dest = Integer.parseInt(instruction.dest);
						reservationStationEntry.operand1.setValue(dest);
					}

					else if (data.registers[dest].isBusy()) {
						if (!data.renamedRegisters[data.registers[dest]
								.getTag()].operand.isValid())
							reservationStationEntry.operand1
									.setTag(data.registers[dest].getTag());
						else
							reservationStationEntry.operand1
									.setValue(data.renamedRegisters[data.registers[dest]
											.getTag()].operand.getValue());
					} else {
						reservationStationEntry.operand1
								.setValue(data.registers[dest].getValue());
					}
					data.reservationStation.add(reservationStationEntry);
				}
			}

			// these are the instructions to be executed
			Vector<ReservationStationEntry> toExecute = data.reservationStation
					.getExecutableInstructions();

			ReservationStationEntry toExecuteLoad = data.reservationStation
					.getLoadExecutableInstruction();
			
			ReservationStationEntry toExecuteStore = data.reservationStation
					.getStoreExecutableInstruction();

			int issued = 0;

			for (int i = 0; i < data.MAX_FU; i++) {
				if (issued == toExecute.size())
					break;

				if (data.functionalUnits[i].assignInstruction(toExecute
						.get(issued)))
					issued++;
			}

			for (int i = 0; i < data.MAX_FU; i++) {
				if (data.functionalUnits[i].isBusy())
					if (data.functionalUnits[i].instructionWaitDone())
						data.functionalUnits[i].executeInstruction();
					else
						data.functionalUnits[i].incrementTime();
			}

			
			if (toExecuteLoad != null)
				data.loadFU.assignInstruction(toExecuteLoad);
			
			if (data.loadFU.isBusy())
				if (data.loadFU.instructionWaitDone())
					data.loadFU.executeInstruction();
				else
					data.loadFU.incrementTime();

			if (toExecuteStore != null)
				data.storeFU.assignInstruction(toExecuteStore);

			if (data.storeFU.isBusy())
				if (data.storeFU.instructionWaitDone())
					data.storeFU.executeStoreInstuction();
				else
					data.storeFU.incrementTime();

			while (!data.reorderBuffer.reorderBufferEntries.isEmpty()
					&& data.reorderBuffer.reorderBufferEntries.get(0).finishBit) {
				data.reorderBuffer.reorderBufferEntries.get(0).completeBit = true;
				if (data.instructionIdType.get(
						data.reorderBuffer.reorderBufferEntries.get(0).id)
						.compareTo("store") != 0) {
					int instructionId = data.reorderBuffer.reorderBufferEntries
							.get(0).id;
					int RRId = data.allInstructions.get(instructionId).RRtag;
					int dest = getRegisterNumber(data.allInstructions
							.get(instructionId).dest);
					data.renamedRegisters[RRId].busyBit = false;
					data.registers[dest].busyBit = false;

					data.registers[dest].setValue(data.renamedRegisters[RRId]
							.getValue()); // writing into ARF
					data.renamedRegisters[RRId].operand.tagBit = 0;// not valid
				}
				data.reorderBuffer.reorderBufferEntries.remove(0);
			}
			/*
			 * for (int i = 0; i < data.MAX_REGISTERS; i++)
			 * System.out.println(data.registers[i].operand.value + " " +
			 * data.registers[i].operand.tagBit);
			 */ System.out
				 .println("=================================================");
			cycleCount++;
		}
		System.out.println("Cycle count: " + cycleCount);

	}

	// return of -1 indicates that given string is not a register and is just a
	// number
	public static int getRegisterNumber(String register) {
		if (register.startsWith("R"))
			return Integer.parseInt(register.substring(1));

		return -1;
	}
}
