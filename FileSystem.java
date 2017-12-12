/************************************************************************
*                                                                       *
* Author: Brent Stone & Prarin Behdarvandian                            *
* Date:   12/07/17                                                      *
* CSS 430                                                               *
* Program 5 - File System                                               *
*                                                                       *
************************************************************************/
public class FileSystem
{
	//used for the seek function
	private final static int SEEK_SET = 0;
	private final static int SEEK_CUR = 1;
	private final static int SEEK_END = 2;
	private final static int DIRECT_PTR_SIZE = 11;

	private SuperBlock superblock;	
	private Directory directory;
	private FileTable filetable;

	//Constructor
	//
	//The file system constructor, creaets a superbloc, directory and
	//filetable. This fucntion also opens a FTE to read the file from disk
	//If the directory as data, it must be copied. 
	public FileSystem(int diskBlocks)
	{
		superblock = new SuperBlock(diskBlocks);
		directory = new Directory(superblock.totalInodes);
		filetable = new FileTable(directory);

		FileTableEntry dirEnt = open("/","r");
		int dirSize = fsize(dirEnt);
		if(dirSize > 0)
		{
			byte[] dirData = new byte[dirSize];
			read(dirEnt, dirData);
			directory.bytes2directory(dirData);
		}
		close(dirEnt);
	}


	//void sync 
	//
	//The sync method copies the system back to the disk. It is responsible for writing
	//directory information to the disk, through directory 2 bytes.  
	public void sync()
	{
		FileTableEntry start = open("/","w");		//start sync 
		write(start, directory.directory2bytes());	//write to 
		close(start);								//close when done
		superblock.sync();							//update superblock

	}

	//boolean format - int files
	//
	//This funnction is to format the the disk which erases all of the data.
	//files is the paramter that is used to passs to the superblock to format. 
	//the number of files.   
	public boolean format(int files) 
	{
		if(!filetable.fempty()) {return false;}				//per instructions

		superblock.format(files); 							//superblock format						
		directory = new Directory(superblock.totalInodes);	//Directory format
		filetable = new FileTable(directory);				//filetable format
		return true;
	}

	//FileTableEntry open - string name, string mode
	//
	//This funnction is responsible for opening a file with a parameter string
	//name and mode.  The function creates a new FTE, by calling the falloc 
	//function.  if the mode is a Write, then the deallocate blocks associated
	//with the FTE
	public FileTableEntry open(String name,String mode)
	{
		FileTableEntry fte = filetable.falloc(name, mode);		//fte entry, with name andmode

		if (mode == "w" && !deallocate(fte)) { return null;}	//if write, dealloc

		return fte; 			
	}

	//boolean deallocate - string name, string mode
	//
	//This funnction is responsible removing the blocks associated with
	//the FTE from memory and writes them to disk.  
	private boolean deallocate(FileTableEntry fte)
	{
		if(fte ==  null) {return false;}				//if null no need to deallocate

		for (short i = 0; i < DIRECT_PTR_SIZE; i++)		//check direct pointers
		{
			if (fte.inode.direct[i] != -1)				//if used
			{
				superblock.returnBlock(i);				//return block
				fte.inode.direct[i] = -1;				//set to -1
			}
		}

		byte [] idPtr = fte.inode.freeIndirectBlock();	//data from the indirect pointer

		if (idPtr != null)								//if being used
		{
			short block;

			while((block = SysLib.bytes2short(idPtr, 0)) != -1)	//while loop to check, indirect pointers
			{
				superblock.returnBlock(block);					//return block
			}
		}

		fte.inode.toDisk(fte.iNumber);							//write to disk
		return true;
    }

    //int read - FiletableEntry, byte
	//
	//This funnction is responsible for reading a file, given the FTE.  It Checks the block to make
	//sure it is valid to read from, then reads into the buffer.  If is also responsible for moving
	//the pointers after reading to make sure the everything is kept up to date while the reading
	//is taking place. 
	public int read(FileTableEntry fte, byte[] buffer)
	{
		if(fte.mode == "w" || fte.mode == "a") {return -1;}	//not correct mode, should not be here

		synchronized(fte)
		{
			int bytesLeft = buffer.length;			//getting total number of byes to read
			int blockSize = 512;					//use for block size
			int bytesRead = 0;						//bytes read
			int position = 0;						//current position 

			while(bytesLeft > 0 && fte.seekPtr < fsize(fte) )			//while still bytes to read and seekptr is less than size
			{
				int current = fte.inode.findTargetBlock(fte.seekPtr);	//get current block

				if (current > -1)										//if not an error
				{

					byte [] data = new byte[blockSize];					//allocate data space
					SysLib.rawread(current,data);						//write current block and data

					int offset = fte.seekPtr % blockSize;				//get offset
					int blocksRemaining = blockSize - position;			//get blocks remaining
					int fileRemaining = fsize(fte) - fte.seekPtr;		//get file remaining

					if(blocksRemaining < fileRemaining)					//if blocks reamin is less than file left
					{
						position = blocksRemaining;						//poistion is blocks remaining
					}
					else
					{
						position = fileRemaining;						//position is file remain
					}

					if(position > bytesLeft) { position = bytesLeft;}  	//if position is larger, correct. 

					System.arraycopy(data, offset, buffer, bytesRead, position); //copy data

					bytesRead += position;		//update total bytes read
					bytesLeft -= position;		//update bytes remaining
					fte.seekPtr += position;	//updation seek ptr position
				}
				else
				{
					break;						//current < 0
				}
			}
			return bytesRead;
		}

	}

    //int write - FiletableEntry, byte
	//
	//This funnction is responsible for writing as much data into the buffer as 
	//posiible. The function starts with the seek pointer and moves as the data,
	//is being written. The number of byes that are written is used to move the seek
	//pointer.Additional blocks are added if need, 
	public int write(FileTableEntry fte, byte[] buffer)
	{
		if(fte.mode == "r") {return -1;} 		//not supposed to be here

		synchronized (fte)
		{
			int position;						//position of ptr
			int bytesWritten = 0;				//how many bytes are written
			int size = buffer.length;			//size of buffer
			int blockSize = 512;				//constant block size

			while(size > 0)
			{
				int loc = fte.inode.findTargetBlock(fte.seekPtr);	//find location of block

				if(loc == -1)										//if -1 get some blocks
				{
					short nextLoc = (short) superblock.nextFreeBlock();	//next location of blocks allocated by super

					int test = fte.inode.getIndexBlockNumber(fte.seekPtr, nextLoc);	//test check for block

					switch(test)
					{
						case -1: {return -1;}	//not valid index

						case -2: {return -1;}	//not valid index

						case -3:				//valid index
						{
							short freeBlock = (short) this.superblock.nextFreeBlock();	//get next free block

							if(!fte.inode.setIndexBlock(freeBlock) || fte.inode.getIndexBlockNumber(fte.seekPtr, nextLoc) !=0)
							{
								return -1;	//if the indirect points are empty and check for a pointer error then return erro 	
							}

						}
					}

						loc = nextLoc;	//save next block to current block 
				}

				byte[] tempBuff = new byte[blockSize];	//new temp buffer
				SysLib.rawread(loc, tempBuff);			//raw read with location and temp buffer

				position = fte.seekPtr % blockSize;		//find position
				int bytesLeft = blockSize - position;	//subtract to see how many bytes left to write

				if(bytesLeft > size)	//if bytes left is greater than. copy array, write with block location and temp buffer
				{
					System.arraycopy(buffer, bytesWritten, tempBuff, position,size);
					SysLib.rawwrite(loc,tempBuff);
					bytesWritten += size;		//update
					fte.seekPtr += size;		//update
					size = 0;					//update
				}
				else		//if not then there are still bytes to write, and update variables 
				{
					System.arraycopy(buffer, bytesWritten, tempBuff, position,bytesLeft);
					SysLib.rawwrite(loc,tempBuff);
					bytesWritten += bytesLeft;
					fte.seekPtr += bytesLeft;
					size -= bytesLeft;

				}

				if(fte.seekPtr > fte.inode.length) {fte.inode.length = fte.seekPtr; }
				//if pointer is greater than lenght, update the length.
			}

			fte.inode.toDisk(fte.iNumber);		//to disk
			return bytesWritten;				//return written
		}
	}


	//int seek - FileTableEntry, int, int, 
	//
	//This method is responsible for finding the ponter that is associated with the
	//FTE. Per instructions on the file haded to us, if the user attemps to seek,
	//the pointer to a negative number the function sets it to 0, and if it attempts
	//to seek the point past the file size, the function must set it to the endl.
	public int seek(FileTableEntry fte, int offset, int whence)
	{
		synchronized(fte)
		{
			if(whence == SEEK_SET)			//Starts at the beg. of file
			{
				fte.seekPtr = offset;
			}
			else if(whence == SEEK_CUR)		//seeks current spot
			{
				fte.seekPtr += offset;
			}
			else if(whence == SEEK_END)		//from the end of the file
			{
				fte.seekPtr = fte.inode.length + offset;
			}
			else
			{
				return -1;					
			}

			if(fte.seekPtr < 0)	{ fte.seekPtr = 0;}		//negative = 0

			if(fte.seekPtr > fte.inode.length) {fte.seekPtr = fte.inode.length;} //longer = lenght

			return fte.seekPtr;	
		}		
	}


	//boolean close - FileTableEntry FTE 
	//
	//This method is responsible for closing the entry in the file table, given a
	//FTE. 
	public boolean close(FileTableEntry fte)
	{
		synchronized(fte)
		{
			fte.count--;						//decrease the count if there are more than one inode
			if(fte.count != 0) {return true;}	//return true if > 0, still has pointers to fte
		}
		return filetable.ffree(fte);			//if count == 0, call ffree on the filetable 
	}

	//boolean delete - String Filename 
	//
	//This method is responsible for deleting a specific file that is given pass to the 
	//fucntion name by the string paratemer.  Firds the inumber throught the directory
	//and creates an FTE.  Calls close to close out the FTE and ifree to release the 
	//inumber. 
	public boolean delete(String filename)
	{
		short number = directory.namei(filename);	//get the iNumber assoicated with the file name;

		if (number == -1) {return false;}			//-1 doesnt exist

		FileTableEntry fte = open(filename, "w");	//Open file table entry associated with the name

		if(close(fte) && directory.ifree(number)) { return true; }
		else { return false; }   					 //if both close and ifree pass, the file has been deleted
	}


	//int fsize - FileTableEntry 
	//
	//Taks a file table entry as a parameter and returns the
	//length of the iNode. The FTE is synchronized 
	public int fsize(FileTableEntry fte)
	{
		synchronized(fte)
		{
			return fte.inode.length;
		}

	}

}
