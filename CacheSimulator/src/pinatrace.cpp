/*BEGIN_LEGAL 
  Intel Open Source License 

  Copyright (c) 2002-2014 Intel Corporation. All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are
met:

Redistributions of source code must retain the above copyright notice,
this list of conditions and the following disclaimer.  Redistributions
in binary form must reproduce the above copyright notice, this list of
conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.  Neither the name of
the Intel Corporation nor the names of its contributors may be used to
endorse or promote products derived from this software without
specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE INTEL OR
ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
END_LEGAL */
#include <stdio.h>
#include <fstream>
#include <iostream>
#include <cstring>
#include <cstdlib>
#include <cmath>
#include <vector>
#include <cstring>
#include <sstream>
#include <string>
#include "pin.H"

using namespace std;
#define ADDRESS_SIZE 48     //12 bit address in hexadecimal(4 bits)
FILE * trace;
int mainMemLatency;
long long globalCounter;
int replacementPolicy;
long long totalTime = 0;
//#define DEBUG 0  // For maintaining inclusivity

/**
 *   Block in a cache set
 */
class block{
	public:
		long long tag;   // The tag_bits identifying the block
		int timeStamp; 	// For LRU policy
		int frequency; 	//For LFU policy
		bool dirtyBit;


		block() {
			tag = -1;
			timeStamp = -1;
			frequency=-1;
			dirtyBit = false;
		}

		block(long long _tag, int _timeStamp, int _frequency) {
			tag = _tag;
			timeStamp = _timeStamp;
			frequency= _frequency;
			dirtyBit = false;
		}
};

/* Cache set- made of blocks */
class cacheSet{
	public:
		vector<block> blockList;
		int numBlocks;

		//numBlocks = associativity of the cache to which cache set belongs.
		cacheSet(int _numBlocks){
			numBlocks = _numBlocks;
			blockList.resize(_numBlocks);
		}
};

/* Class for a single cache-vector of cache-sets */
class cache{
	public:
		int cacheIndex;                 	//L1 or L2 or L3 ...
		vector<cacheSet> cacheSetList;  	//structure of the cache
		int numSets;           			//Number of sets
		long long hitCount;          		//Total number of cache hits in that particular level
		long long missCount;
		long long accessCount;  		//Total number of cache accesses for that level
		int hitLatency;        			//Hit latency in terms of number of cycles
		int associativity;     			//Associativity of the cache of that particular level
		int replacePolicy;     			//Replacement policy for the cache
		int blockSize;
		int offsetSize;
		int setSize;
		int tagSize;


		cache(int _cacheIndex, int _numSets, int _associativity, int _blockSize,int _replacePolicy,int _hitLatency) {
			cacheIndex = _cacheIndex;
			numSets = _numSets;
			associativity = _associativity;
			blockSize = _blockSize;
			replacePolicy= _replacePolicy;
			hitLatency= _hitLatency;
			offsetSize = log2(_blockSize);		//size of int
			setSize = log2(_numSets);
			tagSize = ADDRESS_SIZE - offsetSize - setSize;
			hitCount = 0;
			missCount = 0;
			accessCount = 0;

			for(int i=0;i<numSets;i++)
				cacheSetList.push_back(cacheSet(associativity));	//associativity = number of blocks
		}

		/**
		 * Function to get the set index of a given memory address
		 */
		long long getCacheSet(long long val) {
			long long t = 0;
			for(int i = 0; i < setSize; i++)
				t = t<<1 | 1;
			t = t<<offsetSize;
			return (val & t)>>offsetSize;

		}

		/**
		 * Function to get the block offset of a given memory
		 */
		long long  getOffset(long long val) {
			long long t = 0;
			for(int i = 0; i < offsetSize; i++)
				t = t<<1 | 1;
			return val & t;
		}

		/**
		 * Function to get the tag bits of a given memory address
		 */
		long long getTag(long long val){
			long long t = 0;
			for(int i = 0; i < tagSize; i++)
				t = t<<1 | 1;
			t = t<<(offsetSize+setSize);
			return (val & t)>>(offsetSize+setSize);
		}
		/**
		*   Function to get the adress 
		*/
		long long getAddress( long long tag, long long setIndex, long long blockOffset) {
			long long value = tag;
			value = (value<<setSize) + setIndex ;
			value = (value<<offsetSize) + blockOffset;
			return value;
		}


		/**
		 * Function to check if a given value is present in cache
		 */
		bool contains(long long value) {
			//number of cache accesses incremented
			long long cacheSetBits = getCacheSet(value);
			long long tagBits = getTag(value);
			accessCount++;
			totalTime += hitLatency;

			for(vector<block>::iterator it = cacheSetList[cacheSetBits].blockList.begin(); it != cacheSetList[cacheSetBits].blockList.end(); ++it){
				if (it->tag == tagBits) {
					hitCount++;
					it->timeStamp = globalCounter++;	//for LRU
					it->frequency++;			//for LFU
					return true;
				}
			}
			missCount++;
			return false;
		}

		/**
		 * Function to remove a given block
		 */
		void removeBlock(long long value, vector<cache>&cacheList) {
			long long cacheSetBits = getCacheSet(value);
			long long tagBits = getTag(value);
			accessCount++;
			for(vector<block>::iterator it = cacheSetList[cacheSetBits].blockList.begin();it != cacheSetList[cacheSetBits].blockList.end(); ++it) {
				if (it->tag == tagBits){
					it->tag = -1;
					it->timeStamp = -1;
					it->frequency = -1;

					if(cacheIndex == (int)cacheList.size()-1 && it->dirtyBit)
						totalTime += mainMemLatency;

					else if(it->dirtyBit)
					{
						cacheList[cacheIndex+1].appendBlockInCache(value ,cacheList, replacePolicy);
						cacheList[cacheIndex+1].setDirtyBit(value);
					}
					it->dirtyBit = false;

					break;
				}
			} 
		}


		/**
		 * Function to add a block containing the given memory address into a cache. Append block which is already searched.
		 */
		void appendBlockInCache(long long value, vector<cache>&cacheList, int replacementPolicy) {
			long long cacheSetBits = getCacheSet(value);
			long long tagBits = getTag(value);
			int leastParameter = 0;    
			int blockIndex = -1;
			long long tagBitRemove = -1;
			int i = 0;
			long long address=0;

			accessCount++;
			if(checkInCache(value))		//Update the data in block
				return;
			if(replacementPolicy == 0){

				for(vector<block>::iterator it = cacheSetList[cacheSetBits].blockList.begin();
						it != cacheSetList[cacheSetBits].blockList.end(); ++it, ++i) {
					if (leastParameter > it->timeStamp || leastParameter == 0){
						blockIndex = i;
						leastParameter = it->timeStamp;
						tagBitRemove = it->tag;
						address = getAddress(it->tag,cacheSetBits,0); 
					}
				}
		
                           }

			else if(replacementPolicy == 1)
				for(vector<block>::iterator it = cacheSetList[cacheSetBits].blockList.begin();
						it != cacheSetList[cacheSetBits].blockList.end(); ++it, ++i) {
					
					if (leastParameter > it->frequency || leastParameter == 0){
						blockIndex = i;
						leastParameter = it->frequency;
						tagBitRemove = it->tag;
						address = getAddress(it->tag,cacheSetBits,0); 
					}
				}
			else if(replacementPolicy == 2){
				for(vector<block>::iterator it = cacheSetList[cacheSetBits].blockList.begin();
						it != cacheSetList[cacheSetBits].blockList.end(); ++it, ++i) {
					if(it->tag == -1){

						address = getAddress(it->tag,cacheSetBits,0);
						blockIndex = i;
						break;						
					}
				}
				if(i == (int)cacheSetList[cacheSetBits].blockList.size()){
					blockIndex = rand()%cacheSetList[cacheSetBits].blockList.size();   //Random replacement
					tagBitRemove = cacheSetList[cacheSetBits].blockList.at(blockIndex).tag;
					address = getAddress(tagBitRemove,cacheSetBits,0); 
				}
			}     

			else{
				cout<<"Unknown replacement policy"<<endl;
				exit(-1);
			}	

			if (tagBitRemove != -1)
				for(int j=1; j <= cacheIndex; j++)
					if(j != cacheIndex && cacheList[j].checkInCache(address))
						cacheList[j].removeBlock(address,cacheList);

			
			//Updating  the block that is being written
			cacheSetList[cacheSetBits].blockList[blockIndex].tag = tagBits;
			cacheSetList[cacheSetBits].blockList[blockIndex].timeStamp = globalCounter++;
			cacheSetList[cacheSetBits].blockList[blockIndex].frequency = 1;	
		}

		/*sets dirtybit of a value to 1*/
		void setDirtyBit(long long value, bool dirtyBit = true)
		{
			long long cacheSetBits = getCacheSet(value);
			long long tagBits = getTag(value);

			for(vector<block>::iterator it = cacheSetList[cacheSetBits].blockList.begin(); it != cacheSetList[cacheSetBits].blockList.end(); ++it){
				if (it->tag == tagBits) {
					it->dirtyBit = dirtyBit;
					break;
				}
			}
		}

		/* For checking  if value is in cache-Different from contains -used only for checking correctness   */
		bool checkInCache(long long value) {
			long long cacheSetIndex = getCacheSet( value);
			long long tagBits = getTag( value);
			for(vector<block>::iterator it = cacheSetList[cacheSetIndex].blockList.begin();
					it != cacheSetList[cacheSetIndex].blockList.end(); ++it) {
				if(it->tag == tagBits)
					return true;
			}
			return false;
		}
		/* Checking if the inclusive property is satisfied */
		bool checkInclusivity( vector<cache>& cacheList)
		{
			int i = 0;
			for(vector<cacheSet>::iterator it = cacheSetList.begin();it !=  cacheSetList.end(); ++it, ++i) {
				for(vector<block>::iterator it1 = it->blockList.begin(); it1 != it->blockList.end(); ++it1) {
					if(it1->tag == -1){
						continue;
					}
					long long value = getAddress( it1->tag, i,  0);
					for(int k=cacheIndex;k<(int)cacheList.size();k++)
						if(!cacheList[k].checkInCache (value))
						{
							cout<< k <<" Failed here"<< value <<endl;

							return false;

						}			
				}
			}
			return true;
		}            

};

//Vector of all the Data cache starting from index 1 
vector<cache> cacheList;

/*Function to convert hexadecimal to decimal */
long long hexToDecimal(string s) {
	return (long long)strtol(s.c_str(), 0, 16);
}


/**
 * function to convert a void* variable's value to integer
 */
long long  getDecimal(VOID* addr) {
	stringstream s;
	s<<((int*)addr);
	return hexToDecimal(s.str());
}


/**
 * Function to convert decimal to binary
 */
string decimalToBinary(long long value){
	string s;
	while(value != 0){
		s.push_back((value&1)+'0');
		value >>= 1;
	}

	while(s.length() < ADDRESS_SIZE)
		s.push_back('0');

	string reverse;
	for(int i=0;i<(int)s.length();i++)
		reverse.push_back(s[i]);
	return reverse;
}




/**
 * Function to simulator a read operation on a given Instruction or Data cache
 */
void cacheInsReadSimulator(long long value){
	int i = 1;

	for(i = 1; i < (int)cacheList.size(); i++)
		if(cacheList[i].contains(value)) 		
			break;

	if(i == (int)cacheList.size())
		totalTime += mainMemLatency;

	for(int j = i-1; j > 0; j--)
		cacheList[j].appendBlockInCache(value, cacheList, cacheList[j].replacePolicy);
}


/**
 * Function to simulator a write operation on a given Instruction or Data cache
 */
void cacheInsWriteSimulator(long long value) {
	int i;
	for(i = 1; i < (int)cacheList.size(); ++i) {
		if (cacheList[i].checkInCache(value))	//time added in read simulator
		{
			cacheList[i].setDirtyBit(value);
			break;
		}
	}
	cacheInsReadSimulator(value);

}
/**
 * Function to check correctness after each read and write
 */
bool checkInvariant(){
	for(int i=1;i<(int)cacheList.size();i++)  
		if(!cacheList[i].checkInclusivity(cacheList))
			return false;
	return true;
}

// Print a memory read record
VOID RecordMemRead(VOID * ip, VOID * addr)
{
	//fprintf(trace,"%p: R %p\n", ip, addr);
	cacheInsReadSimulator(getDecimal(addr));
	cacheInsReadSimulator(getDecimal(ip));	
#ifdef DEBUG

	if(!checkInvariant()){
		cout <<"Cache invariant lost while reading "<<getDecimal(addr)<<endl;	
		exit(-1);
	}
#endif
}

// Print a memory write record
VOID RecordMemWrite(VOID * ip, VOID * addr)
{
	//fprintf(trace,"%p: W %p\n", ip, addr);
	cacheInsWriteSimulator(getDecimal(addr));
	cacheInsReadSimulator(getDecimal(ip));	
#ifdef DEBUG
	if(!checkInvariant()){
		cout <<"Cache invariant lost while writing"<<endl;
		exit(-1);
	}
#endif
}

// Is called for every instruction and instruments reads and writes
VOID Instruction(INS ins, VOID *v)
{
	// Instruments memory accesses using a predicated call, i.e.
	// the instrumentation is called iff the instruction will actually be executed.
	//
	// On the IA-32 and Intel(R) 64 architectures conditional moves and REP 
	// prefixed instructions appear as predicated instructions in Pin.
	UINT32 memOperands = INS_MemoryOperandCount(ins);

	// Iterate over each memory operand of the instruction.
	for (UINT32 memOp = 0; memOp < memOperands; memOp++)
	{
		if (INS_MemoryOperandIsRead(ins, memOp))
		{
			INS_InsertPredicatedCall(
					ins, IPOINT_BEFORE, (AFUNPTR)RecordMemRead,
					IARG_INST_PTR,
					IARG_MEMORYOP_EA, memOp,
					IARG_END);
		}
		// Note that in some architectures a single memory operand can be 
		// both read and written (for instance incl (%eax) on IA-32)
		// In that case we instrument it once for read and once for write.
		if (INS_MemoryOperandIsWritten(ins, memOp))
		{
			INS_InsertPredicatedCall(
					ins, IPOINT_BEFORE, (AFUNPTR)RecordMemWrite,
					IARG_INST_PTR,
					IARG_MEMORYOP_EA, memOp,
					IARG_END);
		}
	}
}


/**
 * Custom Output function to print the required results
 */
void displayStatistics() {
	int replacementPolicy = cacheList[1].replacePolicy; //Assumption: Same for All Cache Levels
	cout<<" Cache Performance Statistics"<<endl;
	if(replacementPolicy == 0)
		cout << "Cache Replacement Policy used is LRU "<<endl;
	else if(replacementPolicy == 1)
		cout << "Cache Replacement Policy used is LFU "<<endl;
	else if(replacementPolicy == 2)
		cout << "Cache Replacement Policy used is  RR"<<endl;
	else 
		cout << "Unknown Cache Replacement Policy"<<endl;
	cout << "Time Taken "<< totalTime<<endl;
	for(int i=1; i<(int)cacheList.size();i++) {
		cout<<"Cache Level :"<<i<<endl;
		cout << "Miss Count : " << cacheList[i].hitCount <<endl;
		cout<<"Hit Count: "<< cacheList[i].missCount<<endl;
		cout<<"Miss ratio: "<<((((float)cacheList[i].missCount)/cacheList[i].accessCount))<<endl;
		cout<<"Total Memory Accesses: "<<(cacheList[i].accessCount)<<endl;
	}
}

VOID Fini(INT32 code, VOID *v)
{
	//fprintf(trace, "#eof\n");
	displayStatistics();
	//fclose(trace);
}

/* ===================================================================== */
/* Print Help Message                                                    */
/* ===================================================================== */

INT32 Usage()
{
	PIN_ERROR( "This Pintool prints a trace of memory addresses\n" 
			+ KNOB_BASE::StringKnobSummary() + "\n");
	return -1;
}

/* Take in the configurations of the cache */
void getCacheConfig(){
	int size,associativity,blockSize,hitLatency,cacheReplacementPolicy,numSets;
	int numCacheLevels;
	cin>>numCacheLevels;
	cacheList.push_back(cache(0,0,0,0,0,0)); //Dummy cache to have index start from 1
	for(int i=0;i<numCacheLevels;i++){
		cin>>size>>associativity>>blockSize>>hitLatency>>cacheReplacementPolicy;
		numSets=size*1024/(associativity*blockSize);
		cacheList.push_back(cache(i+1, numSets,associativity, blockSize,cacheReplacementPolicy,hitLatency));
	}
	cin>>mainMemLatency;
}

/* ===================================================================== */
/* Main                                                                  */
/* ===================================================================== */


int main(int argc, char *argv[])
{
	if (PIN_Init(argc, argv)) return Usage();

	//trace = fopen("pinatrace.out", "w");
	INS_AddInstrumentFunction(Instruction, 0);
	PIN_AddFiniFunction(Fini, 0);
	getCacheConfig();
	// Never returns
	PIN_StartProgram();
	return 0;
}
