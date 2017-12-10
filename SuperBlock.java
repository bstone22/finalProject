import java.io.*;

public class SuperBlock 
{

	private final int defaultInodeBlocks = 64; //Default Inode number

   public int totalBlocks; // the number of disk blocks
   public int totalInodes; // the number of inodes
   public int freeList;    // the block number of the free list's head
   
   public SuperBlock( int diskSize ) 
   {
   		byte[] superBlock = new byte[Disk.blocksize];
   		SysLib.rawread(0, superBlock);
   		totalBlocks = SysLib.bytes2int(superBlock, 0);
   		totalInodes = SysLib.bytes2int(superBlock, 4);
   		freeList = SysLib.bytes2int(superBlock, 8);

   		if(totalBlocks == diskSize && totalInodes > 0 && freeList >= 2)
   		{
   			return;
   		}
   		else
   		{
   			totalBlocks = diskSize;
   			format(defaultInodeBlocks);

   		}
   }

   public int getFreeBlock()
   {	
   		if(freeList < 2)
   		{
   			return -1
   		}
   		else if(freeList > totalBlocks)
   		{
   			return -1
   		}
   		else
   		{
   			int formerHead = 0;
   			byte[] findFreeBlockArr = new byte [Disk.blocksize];

   			formerHead = freeList; //Save the head that's being kicked out
   			SysLib.rawread(freeList, findFreeBlockArr); // Gte info from that block #
   			freeList = SysLib.bytes2int(findFreeBlockArr, 0);//Get the new Head from the array from where it is found
   			//Reason the offset is zero instead of 8 is because unlike block 0 the first few bytes of each block is the number of the free list (Panitz Lecture)

   			SysLib.int2bytes(-1, findFreeBlockArr, 0)
   			SysLib.rawwrite(formerHead, findFreeBlockArr);

   			sync(); // sync to the disk
   			return formerHead; //Give back the former head free block
	   	}

   }

   public void format(int fileNumber)
   {
   		byte[] formatArr = new byte [512];
   		totalInodes = fileNumber;
   		int filledBlocks = fileNumber / 16; //the blocks that will be filled with Inodes
   		if(totalInodes% 16 == 0)
   		{
   			freeList = filledBlocks+1;
   		}
   		else
   		{
   			freeList = filledBlocks + 2;
   		}

   		for(int i = 0; i < totalInodes; i++)
   		{
   			Inode newInodeEntry = new Inode(); //create an Inode for each file and call toDisk
   			newInodeEntry.toDisk();
   		}
   		sync(); //Set up the superblock

   		for(int i = freeList; i < totalBlocks; i++)
   		{
   			byte[] formatArr = new byte [512];
   			Arrays.fill(formatArr,(byte)0);
   			if(i == totalBlocks-1)
   			{
   				SysLib.int2bytes(-1, formatArr, 0);
   			}
   			else
   			{
   				SysLib.int2bytes(i+1, formatArr, 0);
   			}
   			rawwrite(i, formatArr);
   		}
   }
   
   public boolean returnBlock(int blockNumber)
   {
   		if(blockNumber > 0)
   		{
   			if(blockNumber < totalBlocks)
   			{
   				byte selectedBlkArr = new byte[Disk.diskSize];
   				SysLib.int2bytes(freeList, selectedBlkArr, 0);
   				rawwrite(blockNumber, selectedBlkArr);
   				freeList = blockNumber;
   				sync();
   				return true;
   			}
   			else
   			{
   				return false;
   			}
   		}
   		else
   		{
   			return false;
   		}
   }

   public void sync()
   {
   		byte[] syncArr = new byte[Disk.blocksize];

   		int2bytes(totalBlocks, syncArr, 0);
   		int2bytes(totalInodes, syncArr, 4);
   		int2bytes(freeList, syncArr, 8);

   		SysLib.rawwrite(0, syncArr);
   }
}