/************************************************************************
*                                                                       *
* Author: Brent Stone & Prarin Behdarvandian                            *
* Date:   12/07/17                                                      *
* CSS 430                                                               *
* Program 5 - File System                                               *
*                                                                       *
************************************************************************/
public class Directory {
   private final int maxChars = 30; // max characters of each file name
   private final int BYTES_ALLOC = 64;
   private final int MAX_BYTES = 60;
   private final int CHAR_MAX = 30;
   private final int BLOCK_SPACE = 4; 

   // Directory entries
   private int fsize[];        // each element stores a different file size.
   private char fnames[][];    // each element stores a different file name.
   private int sizeDirectory;  // size of the directory
   private int maxNumber;      //


   public Directory( int maxInumber ) { // directory constructor
      fsize = new int[maxInumber];     // maxInumber = max files
      for ( int i = 0; i < maxInumber; i++ ) 
         fsize[i] = 0;                 // all file size initialized to 0
      fnames = new char[maxInumber][maxChars];
      String root = "/";                // entry(inode) 0 is "/"
      fsize[0] = root.length( );        // fsize[0] is the size of "/".
      root.getChars( 0, fsize[0], fnames[0], 0 ); // fnames[0] includes "/"
      sizeDirectory = maxInumber;
      maxNumber = maxInumber;
   }

   //byte directory2bytes
   //
   //This function is responsible for coverting bytes 2 directory
   //overall function was inspired by slides that has the bytes to
   //directory function.  
   public void bytes2directory(byte data[]) 
   {
      int spacing = 0;
      for (int i = 0; i < sizeDirectory; i++) {
         
         fsize[i] = SysLib.bytes2int(data, spacing);   //bytes to int int the fsize array   
         spacing += BLOCK_SPACE;                      //increment by block space = 4
      }
      
      for (int i = 0; i < sizeDirectory; i++) 
      {
         (new String(data, spacing, MAX_BYTES)).getChars(0, fsize[i], fnames[i], 0);   //calls new string to create and 
         spacing += MAX_BYTES;                                                         //chars from  
         //increase by max byes = 60;  
      }         
   }

   //byte directory2bytes
   //
   //This function is responsible for coverting directory information
   //int an byte array that will be written to the disk. 
   public byte[] directory2bytes( ) 
   {
      int spacing = 0;                                                  //block spacking
      byte[] directoryToBytes = new byte[sizeDirectory * BYTES_ALLOC];  //byte array

      for(int i = 0; i < sizeDirectory; i++)
      {
         SysLib.int2bytes(fsize[i],directoryToBytes,spacing);        //calls int to bytes with parametrts
         spacing += 4;                                      //increase block spacing by 4
      }

      for(int i = 0; i < sizeDirectory; i++)
      {
         byte[] temp = (new String(fnames[i],0,fsize[i])).getBytes();      //gets bytes for string using both f
                                                                           //fnames and fsize         
         System.arraycopy(temp, 0, directoryToBytes,spacing,temp.length);        //copy
         spacing += 60;                                                    //increase by max byes 
      }

      return directoryToBytes;
   }

   // short ialloc - String filename
   
   // the function is responsible fpr allcoating a new file using the next
   // available iNumber within the fsize.   
   public short ialloc( String filename ) 
   {
      for (int i = 0; i < sizeDirectory; i++)
      {
         if(fsize[i] == 0)                   //if not in sue
         {
            fsize[i] = Math.min(filename.length(), maxChars); //take the smaller size
            filename.getChars(0,fsize[i],fnames[i],0);        //call cetChars on the file
            return (short) i;                                 //return index
         }
      }
      return (short)-1;
   }

   // boolean ifree - short Inumber
   
   // this function takes in an iNumber as a parameter and checks if its greater than 
   // the max number of inumbers and if the number is not less than 0.  If the parameter
   // is already free in the array, it returns false as well.  If the iNumber passes
   // these two test, then the fsize is set to 0 and fnames set to null.
   public boolean ifree( short iNumber ) 
   {
      if(iNumber < CHAR_MAX && fsize[iNumber] > 0) 
         {
            fsize[iNumber] = 0;
            return true;
         } //inumber is greater than the max and less than 0, invalid
      
      return false;

   }

   //short namei - String filename
   //
   //this function takes in a filename and checks fname to see if
   //the name is in the array.  if it is found it returns the index
   //of the that found filename. 
   public short namei( String filename ) 
   {
      for(short i = 0; i < fsize.length; i++)
      {
        if(fsize[i] == filename.length())
        {
            String check = new String(fnames[i],0,fsize[i]);

            if(check.equals(filename)) {return i;}
        }
      }
      return -1;
   }
}