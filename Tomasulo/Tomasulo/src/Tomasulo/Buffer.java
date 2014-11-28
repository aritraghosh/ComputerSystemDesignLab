package Tomasulo;

public class Buffer {
	public int value;
	public boolean validity;
	
	Buffer(int value)
	{
		this.value = value;
		this.validity = true;
	}
}
