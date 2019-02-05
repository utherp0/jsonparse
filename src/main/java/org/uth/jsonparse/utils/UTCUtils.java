package org.uth.jsonparse.utils;

import java.util.*;
import java.text.*;

public class UTCUtils
{
  private UTCUtils() {}

  public static long convertToUTC( String input ) throws ParseException
  {
    DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
    Date date = format.parse(input);

    return date.getTime();
  }

  public static String generateHumanReadable( long input )
  {
    Date date = new Date( input );
    SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss z"); 

    return sdf.format(date);
  }

  public static void main( String args[] )
  {
    if( args.length != 1 )
    {
      System.out.println( "Usage: java UTCUtils input (either yyyy-mm-dd hh:mm:ss or UNIX Epoch Time");
      System.exit(0);
    }

    try
    {
      if( args[0].indexOf( "-" ) != -1 )
      {
        System.out.println( args[0] + " : " + UTCUtils.convertToUTC(args[0]));
      }
      else
      {
        System.out.println( args[0] + " : " + UTCUtils.generateHumanReadable(Long.parseLong(args[0])));
      }
    }
    catch( Exception exc )
    {
      System.out.println( "Exception occurred " + exc.toString());
    }
  }
}