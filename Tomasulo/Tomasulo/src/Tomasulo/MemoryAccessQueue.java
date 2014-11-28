package Tomasulo;

import java.util.Vector;

public class MemoryAccessQueue {

	public Vector<MemoryAccessEntry> memoryAccessEntries = new Vector<MemoryAccessEntry>();
	public static Data data = Data.getInstance();

	public void add(MemoryAccessEntry memoryAccessEntry) {
		this.memoryAccessEntries.add(memoryAccessEntry);
	}

	public void remove(int id) {
		for (int i = 0; i < memoryAccessEntries.size(); i++) {
			if (memoryAccessEntries.get(i).instruction.id == id) {
				memoryAccessEntries.remove(i);
				break;
			}
		}

	}


	public boolean isRegister(int id)
	{
		for (int i = 0; i < memoryAccessEntries.size(); i++) {
			if (memoryAccessEntries.get(i).instruction.id == id) {
				return memoryAccessEntries.get(i).isRegister;
			}
		}
		return false;
	}
	
	// assuming that operand1 in ReservationStationEntry is valid
	public boolean isValidLoad(ReservationStationEntry reservationStationEntry) {
		data = Data.getInstance();
		int i;
		for (i = 0; i < memoryAccessEntries.size(); i++) {
			if (!memoryAccessEntries.get(i).isStore)
				if (memoryAccessEntries.get(i).instruction.id == reservationStationEntry.id)
					break;
		}
		i--;

		for (; i >= 0; i--) {
			if (!memoryAccessEntries.get(i).isStore)
				if (memoryAccessEntries.get(i).instruction.id == reservationStationEntry.id)
					return true;
				else
					continue;

			ReservationStationEntry r = data.reservationStation
					.getEntrybyId(memoryAccessEntries.get(i).instruction.id);
			
			if (!r.operand1.isValid())
				return false;
			else if (r.operand1.getValue() != reservationStationEntry.operand1
					.getValue())
				continue;
			
			else 
			{
				if (r.operand2.isValid()) {
					reservationStationEntry.isForwarded = true;
					reservationStationEntry.forwardedValue = r.operand2.getValue();
					return true;
				}

				// operand forwarding
				else {
					reservationStationEntry.isForwarded = true;
					int dest = Integer
							.parseInt(memoryAccessEntries.get(i).instruction.src1
									.substring(1));
					reservationStationEntry.operand1
							.setTag(data.registers[dest].getTag());
					return false;
				}
			}
		}
		return true;
	}

	public boolean isValidStore(ReservationStationEntry reservationStationEntry) {

		int i;
		for (i = 0; i < memoryAccessEntries.size(); i++) {
			if (memoryAccessEntries.get(i).isStore)
				if (memoryAccessEntries.get(i).instruction.id == reservationStationEntry.id)
					break;
		}
		i--;
		for (; i >= 0; i--) {
			ReservationStationEntry r = data.reservationStation
					.getEntrybyId(memoryAccessEntries.get(i).instruction.id);

			if (!r.operand1.isValid()
					|| r.operand1.getValue() == reservationStationEntry.operand1
							.getValue())
				return false;
		}
		return true;
	}
}
