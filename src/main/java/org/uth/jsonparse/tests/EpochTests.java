package org.uth.jsonparse.tests;

import java.text.*;
import java.util.*;

public class EpochTests
{
  public static void main( String[] args )
  {
    long testVal = (long)1546978015.882 * 1000;

    Date date = new Date( testVal );
    SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss z"); 

    System.out.println( "Converted: " + sdf.format(date));

    try
    {
      String testDate = "2019-01-20 02:05:34";

      DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
      date = format.parse(testDate);

      System.out.println( "UTC Conversion: " + date.getTime());
    }
    catch( Exception exc )
    {
      System.out.println( "Exception occurred  " + exc.toString());
    }
  }
}