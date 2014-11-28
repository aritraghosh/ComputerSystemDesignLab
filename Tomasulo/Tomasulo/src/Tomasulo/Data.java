package Tomasulo;

import java.util.*;

/*
 * Singleton class to store data across classes
 */

public class Data {
	public static Data data = new Data();
	public int MAX_REGISTERS = 8;
	public int MAX_FU = 2;
	public int INSTRUCTIONS_PER_CYCLE = 1;
	public int global_ins_counter = 0;
	public long store_counter;
	public long global_time = 0;
	public int MAX_REORDERBUFFER_SIZE = 50; //user input
	public int MAX_RESERVATIONSTATION_SIZE = 50; //user input
	
	public RegisterFileEntry registers[] = new RegisterFileEntry[MAX_REGISTERS];
	public RegisterFileEntry renamedRegisters[] = new RegisterFileEntry[MAX_REGISTERS];
	public int memory[] = new int[100];
	public int renameMemory[] = new int[100];
	public FunctionalUnit functionalUnits[] = new FunctionalUnit[MAX_FU];
	public FunctionalUnit storeFU = new FunctionalUnit();
	public FunctionalUnit loadFU = new FunctionalUnit();
	
	public Vector<Instruction> allInstructions = new Vector<Instruction>();
	public HashMap<String, Integer> latency = new HashMap<String, Integer>();
	public HashMap<Integer, String> instructionIdType = new HashMap<Integer, String>();
	public HashMap<Integer, Buffer>Buffers = new HashMap<Integer,Buffer>() ;
	public ReorderBuffer reorderBuffer = new ReorderBuffer();
	public ReservationStation reservationStation = new ReservationStation();
	public MemoryAccessQueue memoryAccessQueue = new MemoryAccessQueue();

	private Data() {
	}

	public static Data getInstance() {
		return data;
	}
}
