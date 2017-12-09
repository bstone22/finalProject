/************************************************************************
*                                                                       *
* Author: Brent Stone & Prarin Behdarvandian                            *
* Date:   12/07/17                                                      *
* CSS 430                                                               *
* Program 5 - File System                                               *
*                                                                       *
************************************************************************/
import java.util.Vector;

public class FileTable 
{
   public final static int UNUSED = 0;
   public final static int USED = 1;
   public final static int READ = 2;
   public final static int WRITE = 3;
   public final static int DELETE = 4; 


   private Vector table;         // the actual entity of this file table
   private Directory dir;        // the root directory 


   public FileTable( Directory directory ) // constructor
   { 
      table = new Vector( );     // instantiate a file (structure) table
      dir = directory;           // receive a reference to the Director
   }                             // from the file system

   //synchronized FileTableEntry falloc - String FileName, String Mode
   //
   //The falloc function takes in 2 paramters, the string of the file name
   //and the mode of the command.  This function is responsible for chekcing
   //for the corresponding iNode, if the node has been created, fetch it and
   //manipulate the flad in aaccordance to the mode or create a new inode.
   //The function returns a newly created file Table Entry 
   public synchronized FileTableEntry falloc( String filename, String mode ) 
   {
      short iNumber = -1;
      Inode inode = null;

      while (true)
      {
         iNumber = (filename.equals("/") ? 0 : dir.namei(filename)); //get the number from the inode given the file name

         if (iNumber >= 0)       
         {
            inode = new Inode(iNumber);   //if the iNumber > 0 (inode exist)

            if(mode.equals("r"))         //FTE is requesting a read
            {
               if(inode.flag == READ)     //if flag has been READ already, break
               {
                  break;
               }
               else if (inode.flag == WRITE) //flag set to write, wait()
               {
                  try { wait(); } catch (InterruptedException e) {}
               }
               else if (inode.flag == DELETE)   //Node needs to be deleted
               {
                  iNumber = -1;
                  return null; 
               }
               else                             //flag == Used or Unused,
               {
                  inode.flag = READ;
                  break; 
               }
            }
            else                                               //FTE request is Write, Append, Read/Write
            {
               if(inode.flag == READ || inode.flag == WRITE)   //a process is currently performing an action
               {
                  try { wait(); } catch (InterruptedException e) {}
               }
               else if (inode.flag == DELETE)                  //needs to be deleted
               {
                  iNumber = -1;
                  return null;
               }
               else                                            //flag == Used or Unused. 
               {
                  inode.flag = WRITE;
                  break;
               }
            }
         }
         else                                      //the case where an Inode needs to be created
         {
            iNumber = dir.ialloc(filename);        //get number from directory
            inode = new Inode(iNumber);            //create New Inode
            inode.flag = WRITE;                    //set flag to write
            break;
         }
      }

      inode.count++;                               //increment 
      inode.toDisk(iNumber);                       //write to disk
      FileTableEntry e = new FileTableEntry (inode, iNumber, mode);  //create a new FTE
      table.addElement(e);                         //add 
      return e;                                    //return FTE
   }

   //synchonized boolean ffree - FileTableEntry entry
   //
   //the ffree function is used to removing an FTE from the table as well as setting the
   //corresponding node flags to used and notifying other threads that are waiting.  
   public synchronized boolean ffree( FileTableEntry entry )
   {
      if(!table.remove(entry))                        //Check to see if entry can't be removed 
      {
         return false;
      }

      if(entry.inode.flag == READ && entry.inode.count  == 1)  //if flag is READ and Count = 1
      {
         notify();                                             //notify and mark as used
         entry.inode.flag = USED;
      }

      if(entry.inode.flag == WRITE)                            //if flag is WRITE, notify all incase 
      {                                                        //there are multiple processes waiting   
         notifyAll();                                          //and mark node flag as used                        
         entry.inode.flag = USED;         
      }

      entry.inode.count--;                                     //decrement the count
      entry.inode.toDisk(entry.iNumber);                       //write to disk 
      return true;                                             //returns true

   }

   //synchronized boolean fempty
   //
   //The fempty fucntion checks to see if the table is empty and returns true else reutrns false.
   public synchronized boolean fempty( ) 
   {
      return table.isEmpty( );  // return if table is empty 
   }                            // should be called before starting a format
}