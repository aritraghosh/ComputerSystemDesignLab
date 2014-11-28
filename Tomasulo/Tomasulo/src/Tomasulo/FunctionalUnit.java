package Tomasulo;

public class FunctionalUnit {
	public boolean isBusy;
	public int time;
	public ReservationStationEntry reservationStationEntry = new ReservationStationEntry(
			0);
	public static Data data = Data.getInstance();

	public FunctionalUnit() {
		this.isBusy = false;
		this.time = 1;
	}

	public boolean isBusy() {
		return this.isBusy;
	}

	public boolean assignInstruction(ReservationStationEntry rsEntry) {
		data = Data.getInstance();
		if (this.isBusy || !(rsEntry.isValid()))
			return false;
		this.reservationStationEntry = rsEntry;
		this.isBusy = true;
		this.reservationStationEntry.setIssued(true);
		// add in reorder buffer
		return true;
	}

	public boolean instructionWaitDone() {
		if (this.isBusy()) {
			String type = data.instructionIdType
					.get(this.reservationStationEntry.id);
			if (this.time >= data.latency.get(type)) {
				return true;
			}
			return false;
		}
		return true;
	}

	public void executeInstruction() {
		int result = 0;
		String type = data.instructionIdType
				.get(this.reservationStationEntry.id);
		System.out.println("executing "+type);

		if (type.compareTo("add") == 0)
			result = this.reservationStationEntry.operand1.value
					+ this.reservationStationEntry.operand2.value;
		else if (type.compareTo("sub") == 0)
			result = this.reservationStationEntry.operand1.value
					- this.reservationStationEntry.operand2.value;
		else if (type.compareTo("mul") == 0)
			result = this.reservationStationEntry.operand1.value
					* this.reservationStationEntry.operand2.value;
		else if (type.compareTo("div") == 0)
			result = this.reservationStationEntry.operand1.value
					/ this.reservationStationEntry.operand2.value;
		else if (type.compareTo("and") == 0)
			result = this.reservationStationEntry.operand1.value
					& this.reservationStationEntry.operand2.value;
		else if (type.compareTo("or") == 0)
			result = this.reservationStationEntry.operand1.value
					| this.reservationStationEntry.operand2.value;
		else if (type.compareTo("xor") == 0)
			result = this.reservationStationEntry.operand1.value
					^ this.reservationStationEntry.operand2.value;
		else if (type.compareTo("load") == 0)
		{
			ReservationStationEntry r = data.reservationStation.getEntrybyId(this.reservationStationEntry.id);
			if(!r.isForwarded)
				result = data.memory[this.reservationStationEntry.operand1.value];
			else
			{
				if(data.memoryAccessQueue.isRegister(this.reservationStationEntry.id))
					result = this.reservationStationEntry.operand1.value;
				else
					result = r.forwardedValue;
			}
		}

		this.time = 1;

		int RRtag = data.allInstructions.get(this.reservationStationEntry.id).RRtag;
		data.renamedRegisters[RRtag].operand.setValue(result); // entry in
																// renamed
																// registers is
																// still busy
		data.reservationStation.remove(this.reservationStationEntry.id);

		for (int i = 0; i < data.reservationStation.size(); i++) {
			if (data.reservationStation.reservationStationEntries.get(i).operand1.tagBit == RRtag)
				data.reservationStation.reservationStationEntries.get(i).operand1
						.setValue(result);
			if (data.reservationStation.reservationStationEntries.get(i).operand2.tagBit == RRtag)
				data.reservationStation.reservationStationEntries.get(i).operand2
						.setValue(result);
		}

		for (int i = 0; i < data.reorderBuffer.size(); i++) {
			if (data.reorderBuffer.reorderBufferEntries.get(i).id == this.reservationStationEntry.id)
				data.reorderBuffer.reorderBufferEntries.get(i).finishBit = true;
		}
		this.isBusy = false;
		data.memoryAccessQueue.remove(this.reservationStationEntry.id);
	}

	public void executeStoreInstuction() {
		System.out.println("storing " +this.reservationStationEntry.operand2
				.getValue()+" in address "+this.reservationStationEntry.operand1.getValue() );
		
		data.memory[this.reservationStationEntry.operand1.getValue()] = this.reservationStationEntry.operand2
				.getValue();
		for (int i = 0; i < data.reorderBuffer.size(); i++) {
			if (data.reorderBuffer.reorderBufferEntries.get(i).id == this.reservationStationEntry.id)
				data.reorderBuffer.reorderBufferEntries.get(i).finishBit = true;
		}
		this.time = 1;
		this.isBusy = false;
		data.reservationStation.remove(this.reservationStationEntry.id);
		data.memoryAccessQueue.remove(this.reservationStationEntry.id);
	}

	public boolean incrementTime() {
		if (this.isBusy()) {
			this.time++;
			return true;
		}
		return false;
	}
}
