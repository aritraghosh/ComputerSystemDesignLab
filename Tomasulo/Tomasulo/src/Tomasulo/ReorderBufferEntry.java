package Tomasulo;

public class ReorderBufferEntry {
	boolean busyBit;
	boolean issueBit;
	boolean finishBit;
	boolean completeBit;
	int loadValue;
	int storeValue;
	int storeAddress;
	boolean isStore;
	int id;
	
	public ReorderBufferEntry(int id) {
		this.busyBit = true;
		this.issueBit = false;
		this.finishBit = false;
		this.completeBit = false;
		this.id = id;
		this.loadValue = Integer.MIN_VALUE;
		this.storeValue = Integer.MIN_VALUE;
		this.storeAddress = -1;
		this.isStore = false;
	}
	
	public boolean isBusy()
	{
		return this.busyBit;
	}
	
	public boolean isIssued()
	{
		return this.issueBit;
	}
	
	public boolean isCompleted()
	{
		return this.completeBit;
	}
	
	public boolean isFinished()
	{
		return this.finishBit;
	}
	
	public boolean setLoadValue(int value)
	{
		if(!this.isCompleted() && this.isFinished())
		{
			this.loadValue = value;
			this.completeBit = true;
			return true;
		}
		
		return false;
	}
}
