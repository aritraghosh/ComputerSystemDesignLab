package Tomasulo;

public class MemoryAccessEntry {
	public Instruction instruction;
 	public int address;
	public boolean isRegister;
	public boolean isStore;
	
	MemoryAccessEntry(Instruction instruction){
		this.instruction = instruction;
		if(instruction.instr.compareTo("store")==0)
			this.isStore = true;
		else
			this.isStore = false;
		
		if (isStore()){
			if(instruction.dest.startsWith("R"))
				isRegister = true;
			else
			{
				isRegister = false;
				address = Integer.parseInt(instruction.dest);
			}
		}
		
		else
		{
			if(instruction.src1.startsWith("R"))
				isRegister = true;
			else
			{
				address = Integer.parseInt(instruction.src1);
				isRegister = false;
			}
		}
		
	}
	
	public boolean isStore(){
		return isStore;
	}
	
}
