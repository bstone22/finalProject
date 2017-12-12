/************************************************************************
 *                                                                       *
 * Author: Brent Stone & Prarin Behdarvandian                            *
 * Date:   12/07/17                                                      *
 * CSS 430                                                               *
 * Program 5 - File System                                               *
 *                                                                       *
 ************************************************************************/
public class SuperBlock 
{
  private final static int SET_INODE_BLOCKS = 64;   //set iNode BLocks

  public int totalBlocks;   // the number of disk blocks
  public int totalInodes;   // the number of inodes;
  public int freeList;    // the block number of the free list's head
  
  public int inodeBlocks;
  
  //SuperBlock Construotor - int DiskSize
  //
  //The super block constructor allocates the superblock, read from disk
  //and initializes the total number of blocks, idnoes and the free block 
  //list
  public SuperBlock( int diskSize ) 
  {
    byte[] superBlock = new byte[Disk.blockSize];
    SysLib.rawread(0, superBlock);
    totalBlocks = SysLib.bytes2int(superBlock, 0);
    totalInodes = SysLib.bytes2int(superBlock, 4);
    freeList = SysLib.bytes2int(superBlock, 8);

    if(totalBlocks == diskSize && totalInodes > 0 && freeList >= 2) { return; }
    else
    {
      totalBlocks = diskSize;
      format(SET_INODE_BLOCKS);
    }
   }
  
  //void format - int inodes
  //
  //the format fucntion formats the indoes and superblock with the given number of 
  //inodes. All values and blocks are reset to the starting values, basically
  //wipes the 
  public void format(int inodes) 
  {
    byte[] block = null;    //bufferblock

    totalInodes = inodes;   //total nodes = paramters

    for (short i = 0; i < totalInodes; i++)   ///creatn new idnodes
    {
      Inode nwNode = new Inode();
      nwNode.toDisk(i);
    }

    //Check to see if the number of inodes, this will set free list
    if(inodes % 16 == 0)
    {
      freeList = inodes / 16 + 1;
    }
    else
    {
      freeList = inodes / 16 + 2;
    }

    //creante new free blocks so it can be written to disk.
    for (int i = totalBlocks - 2; i >= freeList; i--) 
    {
      block = new byte[Disk.blockSize];     //new size = blocksize
     
      for (int j = 0; j < Disk.blockSize; j++) 
      {
        block[j] = (byte) 0;              //set clear
      }
      
      SysLib.int2bytes(i + 1, block, 0);    //Write to disk
      SysLib.rawwrite(i, block);
    }

  
    SysLib.int2bytes(-1, block, 0);         //last block has null prt
    SysLib.rawwrite(totalBlocks - 1, block);

    sync();     //sync everything up
  }
  
  //void sync
  //
  //the sync method updates the superblock contents with anything that was
  //changed.  This method will write back to the disk all of the variables
  //freelise, totalblocks and totalInodes.  
  public void sync() {
        
    byte[] data = new byte[Disk.blockSize];     //buffer
    
    SysLib.int2bytes(freeList, data, 8);        //towrite back freelist
    SysLib.int2bytes(totalBlocks, data, 0);     //towrite total blocks
    SysLib.int2bytes(totalInodes, data, 4);     //towrite totaInodes 
    
    SysLib.rawwrite(0, data);
  }
  
  //int nextFreeBlock
  //
  //this function is responsible for returning the location of the next free 
  //block.  Returs the top block of the freelist
  public int nextFreeBlock() 
  {
    int location = 0;           //starting location
    
    if (freeList > 0)           //if free blocks are avaliable
    { 
      if (freeList < totalBlocks)   //if free blocks are less than total blocks
      {
          byte[] block = new byte[Disk.blockSize];    //buffer
      
          SysLib.rawread(freeList, block);            //read freelist 
          
          location = freeList;                        //location now free block 
        
          freeList = SysLib.bytes2int(block, 0);      //update freelist      
       
          return location;                            //return location
      }
    }
    return -1;                                        //an error occued, no free blocks 
  }

  //boolean returnBlock - int blockNumber 
  //
  //this function is responsible for add the freeblock back to the bottom of
  //the list.  Takes in the blocknumber as a parameter in order to have the 
  //identifier of what block needs to be added back to the list. 
  public boolean returnBlock(int blockNumber)
   {
      if(blockNumber > 0)                   //if valid blocknumberm, not superblock 
      {
        if(blockNumber < totalBlocks)       //if blocknumber is less than totalblcoks 
        {
          byte[] selectedBlkArr = new byte[Disk.blockSize];   //selected block 
          SysLib.int2bytes(freeList, selectedBlkArr, 0);      
          SysLib.rawwrite(blockNumber, selectedBlkArr);       //write contents
          freeList = blockNumber;                             //freelist set to given block 
          sync();                                             //sync everything up
          return true;
        }
        else
        {
          return false;         //larger than number of blocks 
        }
      }
      else
      {
        return false;           //refering to sueperblock, error 
      }
   }
  
}