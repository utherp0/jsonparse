package org.uth.jsonparse.utils;

import java.io.*;

public class Preparse
{
  public static void main( String[] args )
  {
    if( args.length != 3 )
    {
      System.out.println( "Usage java Preparse targetFile targetDirectory keep(true|false)");
      System.exit(0);
    }

    new Preparse( args[0], args[1], ( args[2].equalsIgnoreCase( "true ")));
  }

  public Preparse( String targetFile, String targetDirectory, boolean keep )
  {
    // Read through file generating extracted JSON for each record
    try
    {
      BufferedReader bufferedReader = new BufferedReader( new FileReader( new File( targetFile )));

      String line = null;
      StringBuffer working = new StringBuffer();
      StringBuffer individualRecord = new StringBuffer();

      int fileCount = 0;

      while( ( line = bufferedReader.readLine()) != null )
      {
        working.append(line);
        System.out.println( line.length());

        // If we're at the start of the file, strip the initial json
        if( working.toString().indexOf( "[{\"metric\"") != -1 )
        {
          int wipePosition = working.toString().indexOf( "[{\"metric\"");
          working = new StringBuffer( working.toString().substring(wipePosition));
        }

        // If we're at the end of a record, extract
        while( working.toString().indexOf("]]}") != -1 )
        {
          System.out.println( "DETECTED");
          String temp = working.toString().substring(1, working.toString().indexOf("]]}") + 3 );
          working = new StringBuffer( working.toString().substring(working.toString().indexOf("]]}") + 3));

          individualRecord = new StringBuffer( "{\"status\":\"success\",\"data\":\n{\"resultType\":\"matrix\",\"result\":[");
          individualRecord.append( temp );
          individualRecord.append( "]}}");

          // Get the pod name
          int podNameStart = working.toString().indexOf("\"pod_name\":\"");
          int podNameEnd = working.toString().indexOf("\"", podNameStart + "\"pod_name\":\"".length());
          String podName = working.toString().substring(podNameStart + "\"pod_name\":\"".length(),podNameEnd);

          System.out.println( podName );

          // Write a file
          PrintWriter writer = new PrintWriter( targetDirectory + File.separator + podName + ".json");
          writer.println( individualRecord.toString());
          writer.close();

          fileCount++;
          System.out.print( fileCount + " " );
        }

      }
    }
    catch( IOException exc )
    {
      System.out.println( "Failed to read file " + targetFile + " due to " + exc.toString());
    }
  }
}