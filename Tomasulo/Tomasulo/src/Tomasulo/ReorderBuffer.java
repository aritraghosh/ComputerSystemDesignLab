package Tomasulo;

import java.util.*;

public class ReorderBuffer {
	public Vector<ReorderBufferEntry> reorderBufferEntries = new Vector<ReorderBufferEntry>();
	public static Data data = Data.getInstance();

	public ReorderBuffer() {
		// TODO Auto-generated constructor stub
		Vector<ReorderBufferEntry> reorderBufferEntries = new Vector<ReorderBufferEntry>();
	}

	public boolean setIssue(long id) {
		for (int i = 0; i < this.reorderBufferEntries.size(); i++)
			if (this.reorderBufferEntries.get(i).id == id) {
				reorderBufferEntries.get(i).issueBit = true;
				return true;
			}
		return false;
	}

	public boolean add(ReorderBufferEntry reorderBufferEntry) {
	//	if (this.reorderBufferEntries.size() >= data.MAX_REORDERBUFFER_SIZE) {
	//		return false;
	//	}

		this.reorderBufferEntries.add(reorderBufferEntry);
		return true;
	}

	public boolean isEmpty() {
		return this.reorderBufferEntries.isEmpty();
	}
	
	public int size()
	{
		return this.reorderBufferEntries.size();
	}

}
