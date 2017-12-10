public class Directory {
   private static int maxChars = 30; // max characters of each file name

   // Directory entries
   private int fsize[];        // each element stores a different file size.
   private char fnames[][];    // each element stores a different file name.
   private int sizeDirectory;  // size of the directory
   private int maxNumber;

   public Directory( int maxInumber ) { // directory constructor
      fsizes = new int[maxInumber];     // maxInumber = max files
      for ( int i = 0; i < maxInumber; i++ ) 
         fsize[i] = 0;                 // all file size initialized to 0
      fnames = new char[maxInumber][maxChars];
      String root = "/";                // entry(inode) 0 is "/"
      fsize[0] = root.length( );        // fsize[0] is the size of "/".
      root.getChars( 0, fsizes[0], fnames[0], 0 ); // fnames[0] includes "/"
      sizeDirectory = maxInumber;
      maxNumber = maxInumber;
   }

   public void bytes2directory( byte data[] ) 
   {
      int offset = 0;
      for(int i = 0; i < fsizes.length; i++, offest += 4)
      {
         fsizes[i] = Syslib.bytes2int(data, offset);
      }

      for(int i = 0; i < fnames.length; i++, offset += maxChars * 2) 
      {
         String fname = new String(data, offset, maxChars *2);
         fname.getChars(0, fsizes[i], fnames[i], 0);
      }

   }

   public byte[] directory2bytes( ) {
      // converts and return Directory information into a plain byte array
      // this byte array will be written back to disk
      // note: only meaningfull directory information should be converted
      // into bytes.
   }

   public short ialloc( String filename ) {
      // filename is the one of a file to be created.
      // allocates a new inode number for this filename
   }


   //boolean ifree - short Inumber
   //
   //this function takes in an iNumber as a parameter and checks if its greater than 
   //the max number of inumbers and if the number is not less than 0.  If the parameter
   //is already free in the array, it returns false as well.  If the iNumber passes
   //these two test, then the fsize is set to 0 and fnames set to null.
   public boolean ifree( short iNumber ) 
   {
      if(iNumber > maxNumber || iNumber < 0) {return false;} //inumber is greater than the max and less than 0, invalid

      if(fsize[iNumber == 0]) {return false;}                //inumber is already free

      fsizes[iNumber] = 0;
      fnames[iNumber] = null;         //****NOT SURE HERE*****//Should we also delete the name contents?
      return true;

   }

   //short namei - String filename
   //
   //this function takes in a filename and checks fname to see if
   //the name is in the array.  if it is found it returns the index
   //of the that found filename. 
   public short namei( String filename ) 
   {
      for(int i = 0; i < sizeDirectory; i++)
      {
         if(filename.equals(new String fnames[i])) { return (short)i;}
      }
      return -1;
   }
}