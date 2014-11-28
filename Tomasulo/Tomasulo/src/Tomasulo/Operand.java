package Tomasulo;

public class Operand {
	public int tagBit;
	public int value;
	
	public Operand()
	{
	   	 tagBit = -1;
	   	 value = 0;
	}
	
	public Operand(int tagBit,int value)
	{
		 this.tagBit = tagBit;
		 this.value = value;
	}
	
	public boolean isValid()
	{
	 	if(this.tagBit == -1)
	   		return true;
	   	return false;
	}
	    
	void setValue(int value)
	{
	   	this.value = value;
	   	this.tagBit = -1;	//making it valid
	}
	    
	void setTag(int tagBit)
	{
		this.tagBit = tagBit;
	}
	
	public int getValue()
	{
		return this.value;
	}
}
