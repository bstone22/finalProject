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
		close (dirEnt);
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



}