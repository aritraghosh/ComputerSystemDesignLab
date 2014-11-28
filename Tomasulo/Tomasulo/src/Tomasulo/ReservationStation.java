package Tomasulo;

import java.util.*;

public class ReservationStation {
	public Vector<ReservationStationEntry> reservationStationEntries = new Vector<ReservationStationEntry>();
	public static Data data = Data.getInstance();

	public ReservationStation() {
		// TODO Auto-generated constructor stub
	}

	public boolean add(ReservationStationEntry reservationStationEntry) {
		if (this.reservationStationEntries.size() >= 20/*data.MAX_RESERVATIONSTATION_SIZE*/) {
			return false;
		}

		this.reservationStationEntries.add(reservationStationEntry);
		return true;
	}

	public boolean remove(int id) {
		for (int i = 0; i < this.reservationStationEntries.size(); i++)
			if (reservationStationEntries.get(i).id == id
					&& reservationStationEntries.get(i).isValid()) {
				this.reservationStationEntries.remove(i);
				return true;
			}
		return false;
	}

	public int size() {
		return this.reservationStationEntries.size();
	}

	public boolean isEmpty() {
		return this.reservationStationEntries.isEmpty();
	}

	Vector<ReservationStationEntry> getExecutableInstructions() {
		Vector<ReservationStationEntry> toReturn = new Vector<ReservationStationEntry>();
		data = Data.getInstance();
		
		for (int i = 0; i < this.reservationStationEntries.size(); i++) {
			ReservationStationEntry reservationStationEntry = reservationStationEntries
					.get(i);
			boolean isLoad = (data.instructionIdType
					.get(reservationStationEntry.id).compareTo("load") == 0);
			boolean isStore = (data.instructionIdType
					.get(reservationStationEntry.id).compareTo("store") == 0);

			if (!isLoad && !isStore && reservationStationEntry.isValid()
					&& !reservationStationEntry.isIssued())
			{
				toReturn.add(reservationStationEntry);
				if(toReturn.size() == data.MAX_FU)
					break;	
			}
		}
		return toReturn;
	}

	public ReservationStationEntry getLoadExecutableInstruction() {
		ReservationStationEntry toReturn = null;
		for (int i = 0; i < this.reservationStationEntries.size(); i++) {
			ReservationStationEntry reservationStationEntry = reservationStationEntries
					.get(i);
			boolean isLoad = (data.instructionIdType
					.get(reservationStationEntry.id).compareTo("load") == 0);

			//System.out.println(data.memoryAccessQueue.isValidLoad(reservationStationEntry));
			if (isLoad && reservationStationEntry.isValid()
					&& !reservationStationEntry.isIssued()&&
					data.memoryAccessQueue.isValidLoad(reservationStationEntry))
				return reservationStationEntries.get(i);	
		}
		return toReturn;
	}
	
	

	public ReservationStationEntry getStoreExecutableInstruction() {
		for (int i = 0; i < this.reservationStationEntries.size(); i++) {
			ReservationStationEntry reservationStationEntry = reservationStationEntries
					.get(i);
			boolean isStore = (data.instructionIdType
					.get(reservationStationEntry.id).compareTo("store") == 0);
			
			if (isStore && reservationStationEntry.isValid()
					&& !reservationStationEntry.isIssued()&&
					data.memoryAccessQueue.isValidStore(reservationStationEntry))
				return reservationStationEntries.get(i);	
		}

		return null;
	}
	
	public ReservationStationEntry getEntrybyId(int id)
	{
		for(int i = 0 ; i < this.reservationStationEntries.size(); i++)
		{
			//System.out.println(this.reservationStationEntries.get(i).id+"_______"+id);
			if(this.reservationStationEntries.get(i).id == id)
				return reservationStationEntries.get(i);
		}
		
		return null;
	}
}
