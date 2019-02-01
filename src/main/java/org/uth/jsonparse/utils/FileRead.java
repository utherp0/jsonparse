package org.uth.jsonparse.utils;

import java.io.*;

public class FileRead
{
  private FileRead() {}

  // Utility to read the contents of the file into a parsable String object
  public static String read( String filepath ) throws IOException
  {
    BufferedReader bufferedReader = new BufferedReader( new FileReader( filepath ));

    StringBuffer input = new StringBuffer();

    String line = null;

    while( ( line = bufferedReader.readLine()) != null )
    {
      input.append( line );
    }

    bufferedReader.close();

    return input.toString();
  }
}