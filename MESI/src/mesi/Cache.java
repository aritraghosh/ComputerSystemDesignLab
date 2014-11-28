package mesi;

import static mesi.Constants.*;

public class Cache {
	//A cache is a list of blocks
	public Block blockList[] = new Block[NUM_BLOCKS];

	public Cache() {
		for (int i = 0; i < NUM_BLOCKS; i++) {
			blockList[i] = new Block();
		}
	}

	public int getState(int blockAddress) {
		int blockNum = blockAddress % NUM_BLOCKS;
		if(blockAddress == -1)
			return ERROR;
		if (blockList[blockNum].address == blockAddress) {
			return blockList[blockNum].state;
		}
		return ERROR;
	}

	public void setInvalidate(int blockAddress) {
		int blockNum = blockAddress % NUM_BLOCKS;

		this.blockList[blockNum].setInvalidate();
	}

	public void setExclusive(int blockAddress) {
		int blockNum = blockAddress % NUM_BLOCKS;

		this.blockList[blockNum].setExclusive(blockAddress);
	}

	public void setModified(int blockAddress) {
		int blockNum = blockAddress % NUM_BLOCKS;

		this.blockList[blockNum].setModified(blockAddress);
	}

	public void setShared(int blockAddress) {
		int blockNum = blockAddress % NUM_BLOCKS;

		this.blockList[blockNum].setShared(blockAddress);
	}

	public void setOwned(int blockAddress) {
		int blockNum = blockAddress % NUM_BLOCKS;

		this.blockList[blockNum].setOwned(blockAddress);
	}

}
