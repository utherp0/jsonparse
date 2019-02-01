package org.uth.jsonparse.tests;

import org.uth.jsonparse.utils.*;

public class FileReadTest
{
  public static void main( String[] args )
  {
    if( args.length != 1 )
    {
      System.out.println( "Usage: java FileReadTest targetFile");
      System.exit(0);
    }

    try
    {
      long start = System.currentTimeMillis();
      String contents = FileRead.read(args[0]);
      long end = System.currentTimeMillis();

      System.out.println( "Read " + contents.length() + " characters in " + ( end - start ) + "ms." );
    }
    catch( Exception exc )
    {
      System.out.println( "Test failed due to " + exc.toString());
    }
  }
}