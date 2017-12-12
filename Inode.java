/************************************************************************
*                                                                       *
* Author: Brent Stone & Prarin Behdarvandian                            *
* Date:   12/07/17                                                      *
* CSS 430                                                               *
* Program 5 - File System                                               *
*                                                                       *
************************************************************************/			
public class Inode 
{
	
	private final static int SIZE_INODE = 32;	// fix to SIZE_INODE bytes
	private final static int MAX_BYTES_ALLOWED = 512;
	private final static int SIZE_OF_BLOCK= 16;

	public int length;		// file size in bytes
	public short count;		// # file-table entries pointing to this
	public short flag;		// 0 = unused, 1 = used, ...
	public short direct[] = new short[11];		// direct pointers
	public short indirect;	// a new indirect pointer
	public int directSize = 11;
	
	//Default Constructor
	//
	//Sets variables to starting values, also sets the 11 pointer to -1 and the 
	//indirect to -1
	Inode() 
	{		
		length = 0;
		count = 0;
		flag = 1;
		
		for (int i = 0; i < directSize; i++) 
		{
			direct[i] = -1;
		}		
		indirect = -1;	
	}
	
	
	//Inode - short iNUmber
	//
	//This constructor takes in a short that is the inumber, then suses the inumber to 
	//then calculate the block number, figure out the new lenght, count and flag associaed 
	//wotj the blocknumber.  Also sets the 11 direct pointers and 1 indriret pointer 
   	Inode( short iNumber )
   	{                       				
       int blockNumber = 1+iNumber/SIZE_OF_BLOCK;		//get needed block number
       byte[] data = new byte[Disk.blockSize];			//new buffer data
       SysLib.rawread(blockNumber,data);				//write, with block number and buffer

       int offset = (iNumber % SIZE_OF_BLOCK)*SIZE_INODE;	//get offset

       length = SysLib.bytes2int(data,offset);				//get new length
       offset += 4;

       count = SysLib.bytes2short(data, offset);			//get new count
       offset += 2;

       flag = SysLib.bytes2short(data, offset);				//get new flag
       offset += 2;

       for(int i = 0; i < directSize; i++)					//11 direct prts		
       {
           direct[i] = SysLib.bytes2short(data, offset);	
           offset+=2;
       }

       indirect = SysLib.bytes2short(data,offset);			//1 indirect 
       offset += 2;											//increment offset 
   	}
	
	
   	//toDisk - short iNumber
   	//
   	//This fucntion writes to the disk and takes in a short inumber as a parameter
	public int toDisk(short iNumber) 
	{
		byte[] toDiskArr = new byte[SIZE_INODE];			//byte buffer
		
		int offset = 0;										//starting offset
		int position = (iNumber%SIZE_OF_BLOCK)*SIZE_INODE;	//starting position, paramater user 
		
		SysLib.int2bytes(length, toDiskArr, offset);		//lenght
		offset += 4;
		
		SysLib.short2bytes(count, toDiskArr, offset);		//count
		offset += 2;
		
		SysLib.short2bytes(flag, toDiskArr, offset);		//flag
		offset += 2;
		
		for (int i = 0; i < 11; i++) 
		{														//direct pointers
			SysLib.short2bytes(direct[i], toDiskArr, offset);
			offset += 2;
		}
		
		SysLib.short2bytes(indirect, toDiskArr, offset);		//indirect pointer
		offset += 2;
		
		int numbBlocks = 1 + (iNumber / SIZE_OF_BLOCK);			//number of blocks
		byte[] data = new byte[MAX_BYTES_ALLOWED];				//buffer data to write
		SysLib.rawread(numbBlocks, data);						
		
		offset = ((iNumber % SIZE_OF_BLOCK) * SIZE_INODE);		//calacualte offset
		
		System.arraycopy(toDiskArr, 0, data, offset, SIZE_INODE);	//write to disk
		SysLib.rawwrite(numbBlocks, data);							
		
		return 0;		//success!
	}
	
	//getIndexBlockNumber - int fte, short offest
	//
	//This function is used in the write poriton of the SystemFile and gets the index of 
	//the block given the current file talbe entry and current offset, which is the 
	//seek pointer and the next free block, respectivly.
	public int getIndexBlockNumber(int fte, short offset) 
	{		
		int search = (fte / MAX_BYTES_ALLOWED);						//fte / max byes will give yoe pointer

		if (search < directSize) 									//if direct pointer
		{	
			if (direct[search] >= 0) { return -1; }					//uncessful if greater than 0
			
			if ((search > 0) && (direct[search - 1] == -1)) { return -2;}	//another uncessful case
			
			direct[search] = offset;								//if found!
			
			return 0;
		}
		
		if (indirect < 0) { return -3;}					//if found in the indriect ponter
		
		byte[] data = new byte[MAX_BYTES_ALLOWED];		//buffer
	
		SysLib.rawread(indirect, data);					//indirect pointer and data	
		
		int blockSpace = 2 * (search - directSize);		//see where this pointe is

		if (SysLib.bytes2short(data, blockSpace) > 0) { return -1; }
			
		SysLib.short2bytes(offset, data, blockSpace);	//write to disk
		SysLib.rawwrite(indirect, data);
		
		return 0;
	}
	
	
	//setIndexBlock - short iNumber
	//
	//this function sets the index of the block number.  It uses the inumber
	//tofind the ponter that is not in the direct pointers
	public boolean setIndexBlock(short iNumber) 
	{
		for (int i = 0; i < directSize; i++) 			//if direct pointer are unused, error 
		{		
			if(direct[i] == -1) { return false;}
		}
		
		if (indirect != -1) { return false; }			//if indriec is not used, error
		
		indirect = iNumber;								//if passes those test, indirect is i number

		byte[] data = new byte[MAX_BYTES_ALLOWED];		//data buffer
		
		for (int i = 0; i < (MAX_BYTES_ALLOWED / 2); i++) 	//loop 256 
		{
			SysLib.short2bytes((short)-1, data, (2 * i));	//syslib call to short2bytes with buffer
		}
		
		SysLib.rawwrite(iNumber, data);			//inumber and data buffer
			
		return true;
	}
	
	//int FindTargetBlock - int offset
	//
	//this function is responsible for findting the target block within the direct block pointer
	//or indirect block.  
	public int findTargetBlock(int offset) 
	{
		int indirblk; 						  	//indreict offset
		int block = offset/ Disk.blockSize;		//offset block
		

		if (block < directSize) {return direct[block];}	//if block is under 11,(direct pointer blocks), return

		if (indirect == -1) {return -1;}				//if indriect is still -1,(unused) - error

		byte[] tempdata = new byte[Disk.blockSize];			//tempdata buffer
		SysLib.rawread(indirect, tempdata);

		indirblk = block - directSize;					//get the indirect block

		return SysLib.bytes2short(tempdata, indirblk * 2);	//return in of indirect block * size. 
	}
	
	//byte[] freeIndirectBlock
	//
	//this fucntion is used to free the indirect block
	public byte[] freeIndirectBlock() 
	{
		if(indirect == -1) {return null;}			//if -1, then unused

		byte[] data = new byte[Disk.blockSize];		//buffer
		SysLib.rawread(indirect, data);				//empty buffer and indirect block 
		indirect = -1;								//set unsued 

		return data; 								//return buffer
	}
	
	
}