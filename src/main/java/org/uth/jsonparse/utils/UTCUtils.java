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
}