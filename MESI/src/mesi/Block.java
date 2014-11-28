package mesi;
import static mesi.Constants.*;

public class Block {
	
	public int state;
	public int address;
	
	
	
	public Block(){
		this.state = ERROR;  //Initially all blocks are set to ERROR state.
		this.address = -1;
	}
	
	public void setInvalidate(){
		this.state = I;
	}
	
	public void setModified(int blockAddress){
		this.state = M;
		this.address = blockAddress;
	}
	
	public void setExclusive(int blockAddress){
		this.state = E;
		this.address = blockAddress;
	}
	
	public void setInvalid(int blockAddress){
		this.state = I;
		this.address = blockAddress;
	}
	
	public void setShared(int blockAddress){
		this.state = S;
		this.address = blockAddress;
	}
	
	public void setOwned(int blockAddress){
		this.state = O;
		this.address = blockAddress;
	}
	

}
