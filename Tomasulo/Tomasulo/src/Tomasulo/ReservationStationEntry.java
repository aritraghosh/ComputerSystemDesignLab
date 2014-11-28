package Tomasulo;

public class ReservationStationEntry {
	public int id;
	public Operand operand1 = new Operand();
	public Operand operand2 = new Operand();
	public Operand storeOperand = new Operand();
	private boolean isIssued;
	public boolean isForwarded;
	public int forwardedValue;
	private static Data data = Data.getInstance();
	
	public ReservationStationEntry(int id) {
		this.id = id;
		this.isIssued = false;
	}
	
	public boolean isValid() 
	{
		data = Data.getInstance();
		
		if(data.instructionIdType.get(id).compareTo("load") == 0)
			return this.operand2.isValid();
		
		return (this.operand1.isValid() && this.operand2.isValid());
	}
	
	public void setIssued(boolean isIssued)
	{
		this.isIssued = isIssued;
	}
	
	public boolean isIssued()
	{
		return this.isIssued;
	}
}
