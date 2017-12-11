public class Inode {
   private final static int iNodeSize = 32;       // fix to 32 bytes
   private final static int directSize = 11;      // # direct pointers

   public int length;                             // file size in bytes
   public short count;                            // # file-table entries pointing to this
   public short flag;                             // 0 = unused, 1 = used, ...
   public short direct[] = new short[directSize]; // direct pointers
   public short indirect;                         // a indirect pointer

   Inode( ) {                                     // a default constructor
      length = 0;
      count = 0;
      flag = 1;
      for ( int i = 0; i < directSize; i++ )
         direct[i] = -1;
      indirect = -1;
   }

   Inode( short iNumber )
   {                       // retrieving inode from disk
       int blockNumber = 1+iNumber/16;
       byte[] data = new byte[Disk.blockSize];
       SysLib.rawread(blockNumber,data);
       int offset = (iNumber % 16)*32;

       length = SysLib.bytes2int(data,offset);
       offset += 4;
       count = SysLib.bytes2short(data, offset);
       offset += 2;
       flag = SysLib.bytes2short(data, offset);
       offset += 2;

       for(int i = 0; i < directSize; i++)
       {
           direct[i] = SysLib.bytes2short(data, offset);
           offset+=2;
       }

       indirect = SysLib.bytes2short(data,offset);
   }

   public void toDisk( short iNumber )
   {
       int bNum = (iNumber+1)/16;
       byte[] toDiskArr = new byte[Disk.blockSize];

       SysLib.rawread(bNum,toDiskArr);
       int position = (iNumber%16)*32;
       SysLib.int2bytes(length, toDiskArr, position);
       position+=4;
       SysLib.short2bytes(count, toDiskArr, position);
       position+=2;
       SysLib.short2bytes(flag, toDiskArr, position);
       position+=2;

       for(int i = 0; i < directSize; i++)
       {
           SysLib.short2bytes(direct[i], toDiskArr, position);
           position+=2;
       }
       SysLib.short2bytes(indirect, toDiskArr, position);
       SysLib.rawwrite(bNum, toDiskArr);
   }

   public short getIndexBlockNumber(int entry, short offset)
   {

       int look = (entry / Disk.blockSize);
       if (look < 11) {

           if (direct[look] >= 0) {

               return -1;
           }


           if ((look > 0) && (direct[look - 1] == -1)) {

               return -2;
           }


           direct[look] = offset;

           return 0;
       }


       if (indirect < 0) {

           return -3;
       }


       byte[] byteData = new byte[Disk.blockSize];

       SysLib.rawread(indirect, byteData);

       int blockSpace = 2 * (look - 11);
       if (SysLib.bytes2short(byteData, blockSpace) > 0) {

           return -1;
       }


       SysLib.short2bytes(offset, byteData, blockSpace);
       SysLib.rawwrite(indirect, byteData);


       return 0;
   }

   public boolean setIndexBlock(short indexBlockNumber)
   {
       for (int i = 0; i < 11; i++) {

           if(direct[i] == -1) {

               return false;
           }
       }


       if (indirect != -1) {

           return false;
       }


       indirect = indexBlockNumber;
       byte[] byteData = new byte[Disk.blockSize];

       for (int i = 0; i < (Disk.blockSize / 2); i++) {
           SysLib.short2bytes((short)-1, byteData, (2 * i));
       }

       SysLib.rawwrite(indexBlockNumber, byteData);


       return true;
   }

   public short findTargetBlock(int offset)
   {
       if(offset < 0)
       {
           return -1;
       }
       else
       {
           if((offset/Disk.blockSize) < 11)
           {
               return direct[offset/Disk.blockSize];
           }
           else
           {
               if(indirect > 0)
               {
                   byte[] findBlock = new byte[Disk.blockSize];
                   SysLib.rawread(indirect, findBlock);
                   short result = SysLib.bytes2short(findBlock,((offset/Disk.blockSize)-11)*2);
                   return  result;
               }
               else
               {
                   return -1;
               }
           }
       }
   }

    byte[] deallocatedIndirect() {

        if (indirect < 0)
        {
            return null;
        }
        else
        {
            byte[] byteData = new byte[Disk.blockSize];
            SysLib.rawread(indirect, byteData);

            indirect = -1;

            return byteData;
        }

    }


}