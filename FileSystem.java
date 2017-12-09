public class FileSystem
{
	private final static ERROR = -1;
	private final static SEEK_SET = 0;
	private final static SEEK_CUR = 1;
	private final static SEEK_END = 2;

	private SuperBlock superblock;
	private Directory directory;
	private FileTable filetable;

	public FileSystem(int diskBlocks)
	{
		superblock = new SuperBlock(diskBlocks);
		directory = new Directory(superblock.totalInodes);
		filetable = new FileTable(directory);

		FileTableEntry dirEnt = open("/","r");
		int dirSize = fsize(dirEnt);
		if(dirSize > 0)
		{
			byte[] dirData = new byte(dirSize);
			read(dirEnt, dirData);
			directory.bytes2directory(dirData);
		}
		close(dirEnt);
	}

	//possbile this should be a boolean........
	public int format(int files) 
	{
		if(!filetable.fempty())
		{
			return -1;
		}
		superblock.format(files);  //superblock = new SuperBlock(files); ---->CHeck This
		directory = new Directory(superblock.totalInodes);
		filetable = new FileTable(directory);
		return 0;
	}


	public FileTableEntry open(String name,String node)
	{
		FileTableEntry fte = filetable.falloc(name, mode);
		return fte; 
	}

	public int read(FileTableEntry fte, byte[] buffer)
	{

	}

	public int seek(FileTableEntry fte, int offset, int whence)
	{
		synchonrize(fte)
		{
			if(whence == SEEK_SET)
			{
				fte.seekPtr = offset;
			}
			else if(whence == SEEK_CUR)
			{
				fte.seekPtr += offset;
			}
			else if(whence == SEEK_END)
			{
				fte.seekPtr = fte.inode.length + offset;
			}
			else
			{
				return -1;
			}

			if(fte.seekPtr < 0)	{ fte.seekPtr = 0;}

			if(fte.seekPtr > fte.inode.length) {fte.seekPtr = fte.inode.length;}

			return fte.seekPtr;
		}
		
	}


	public boolean close(FileTableEntry fte)
	{
		synchonrize(fte)
		{
			if (fte == null) {return -1;}		//if null return false
			fte.count--;						//decrease the count if there are more than one inode
			if(fte.count > 0) {return 0;}		//return true if > 0, still has pointers to fte
		}
		return filetable.ffree(fte);			//if count == 0, call ffree on the filetable 
	}

	public int delete(String filename)
	{
		int number = dir.namei(filename);		//get the iNumber assoicated with the file name;

		if (number == -1) {return -1;}			//-1 doesnt exist

		FileTableEntry fte = open(filename, "w");	//Open file table entry associated with the name

		if(close(fte) && directory.ifree(number)) { return 0; }
		else { return -1; }   					 //if both close and ifree pass, the file has been deleted
	}

	//int fsize - FileTableEntry 
	//
	//Taks a file table entry as a parameter and returns the
	//length of the iNode. The FTE is synchronized 
	public int fsize(FileTableEntry fte)
	{
		synchonrize(fte)
		{
			return fte.inode.size;
		}

	}


}