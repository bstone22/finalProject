/************************************************************************
*                                                                       *
* Author: Brent Stone & Prarin Behdarvandian                            *
* Date:   12/07/17                                                      *
* CSS 430                                                               *
* Program 5 - File System                                               *
*                                                                       *
************************************************************************/
import java.util.*;

public class SysLib {
    public static int exec( String args[] ) {
        return Kernel.interrupt( Kernel.INTERRUPT_SOFTWARE,
                 Kernel.EXEC, 0, args );
    }

    public static int join( ) {
        return Kernel.interrupt( Kernel.INTERRUPT_SOFTWARE,
                 Kernel.WAIT, 0, null );
    }

    public static int boot( ) {
    return Kernel.interrupt( Kernel.INTERRUPT_SOFTWARE,
                 Kernel.BOOT, 0, null );
    }

    public static int exit( ) {
    return Kernel.interrupt( Kernel.INTERRUPT_SOFTWARE,
                 Kernel.EXIT, 0, null );
    }

    public static int sleep( int milliseconds ) {
    return Kernel.interrupt( Kernel.INTERRUPT_SOFTWARE,
                 Kernel.SLEEP, milliseconds, null );
    }

    public static int disk( ) {
    return Kernel.interrupt( Kernel.INTERRUPT_DISK,
                 0, 0, null );
    }

    public static int cin( StringBuffer s ) {
        return Kernel.interrupt( Kernel.INTERRUPT_SOFTWARE,
                 Kernel.READ, 0, s );
    }

    public static int cout( String s ) {
        return Kernel.interrupt( Kernel.INTERRUPT_SOFTWARE,
                 Kernel.WRITE, 1, s );
    }

    public static int cerr( String s ) {
        return Kernel.interrupt( Kernel.INTERRUPT_SOFTWARE,
                 Kernel.WRITE, 2, s );
    }

    public static int rawread( int blkNumber, byte[] b ) {
        return Kernel.interrupt( Kernel.INTERRUPT_SOFTWARE,
                 Kernel.RAWREAD, blkNumber, b );
    }

    public static int rawwrite( int blkNumber, byte[] b ) {
        return Kernel.interrupt( Kernel.INTERRUPT_SOFTWARE,
                 Kernel.RAWWRITE, blkNumber, b );
    }

    public static int sync( ) {
        return Kernel.interrupt( Kernel.INTERRUPT_SOFTWARE,
                 Kernel.SYNC, 0, null );
    }

    public static int cread( int blkNumber, byte[] b ) {
        return Kernel.interrupt( Kernel.INTERRUPT_SOFTWARE,
                 Kernel.CREAD, blkNumber, b );
    }

    public static int cwrite( int blkNumber, byte[] b ) {
        return Kernel.interrupt( Kernel.INTERRUPT_SOFTWARE,
                 Kernel.CWRITE, blkNumber, b );
    }

    public static int flush( ) {
        return Kernel.interrupt( Kernel.INTERRUPT_SOFTWARE,
                 Kernel.CFLUSH, 0, null );
    }

    public static int csync( ) {
        return Kernel.interrupt( Kernel.INTERRUPT_SOFTWARE,
                 Kernel.CSYNC, 0, null );
    }

    public static String[] stringToArgs( String s ) {
    StringTokenizer token = new StringTokenizer( s," " );
    String[] progArgs = new String[ token.countTokens( ) ];
    for ( int i = 0; token.hasMoreTokens( ); i++ ) {
        progArgs[i] = token.nextToken( );
    }
    return progArgs;
    }

    public static void short2bytes( short s, byte[] b, int offset ) {
    b[offset] = (byte)( s >> 8 );
    b[offset + 1] = (byte)s;
    }

    public static short bytes2short( byte[] b, int offset ) {
    short s = 0;
        s += b[offset] & 0xff;
    s <<= 8;
        s += b[offset + 1] & 0xff;
    return s;
    }

    public static void int2bytes( int i, byte[] b, int offset ) {
    b[offset] = (byte)( i >> 24 );
    b[offset + 1] = (byte)( i >> 16 );
    b[offset + 2] = (byte)( i >> 8 );
    b[offset + 3] = (byte)i;
    }

    public static int bytes2int( byte[] b, int offset ) {
    int n = ((b[offset] & 0xff) << 24) + ((b[offset+1] & 0xff) << 16) +
            ((b[offset+2] & 0xff) << 8) + (b[offset+3] & 0xff);
    return n;
    }

    //********************************************************************
    //                                                                   *   
    //                                                                   *
    //--------------------8 Syslib fucntions Start-----------------------*
    //                                                                   *
    //                                                                   *
    //********************************************************************

    public static int open(String filename, String mode)
    {
        String[] arguments = new String[2];
        arguments[0] = filename;  
        arguments[1] = mode;
        
        return Kernel.interrupt(Kernel.INTERRUPT_SOFTWARE, Kernel.OPEN, 0, arguments);
    }

    public static int read(int fileD, byte[] buf) 
    {
        return Kernel.interrupt(Kernel.INTERRUPT_SOFTWARE, Kernel.READ,fileD, buf);
    }

    public static int write(int fileD, byte[] buf) 
    {
        return Kernel.interrupt(Kernel.INTERRUPT_SOFTWARE, Kernel.WRITE, fileD, buf);
    }

    public static int seek(int fileD, int offset, int whence) 
    {
        int[] arguments = new int[2];
        arguments[0] = offset;
        arguments[1] = whence;

        return Kernel.interrupt(Kernel.INTERRUPT_SOFTWARE, Kernel.SEEK, fileD, arguments);
    }


    public static int close(int fileD) 
    {
        return Kernel.interrupt(Kernel.INTERRUPT_SOFTWARE, Kernel.CLOSE, fileD, null);
    }

    public static int delete(String fileName) 
    {
        return Kernel.interrupt(Kernel.INTERRUPT_SOFTWARE, Kernel.DELETE, 0, fileName);
    }

    public static int fsize(int fileD) 
    {
        return Kernel.interrupt(Kernel.INTERRUPT_SOFTWARE, Kernel.SIZE, fileD, null);
    }

    public static int format(int files) 
    {
        return Kernel.interrupt(Kernel.INTERRUPT_SOFTWARE, Kernel.FORMAT,files, null);
    }

    //********************************************************************
    //                                                                   *   
    //                                                                   *
    //--------------------8 Syslib fucntions End-------------------------*
    //                                                                   *
    //                                                                   *
    //********************************************************************

}
