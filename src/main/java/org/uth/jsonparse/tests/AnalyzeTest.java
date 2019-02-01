package org.uth.jsonparse.tests;

import org.uth.jsonparse.utils.Analyze;

import net.minidev.json.JSONArray;

import com.jayway.jsonpath.*;
import java.util.*;

public class AnalyzeTest
{
  public static void main( String[] args )
  {
    if( args.length != 1 )
    {
      System.out.println( "Usage: java AnalyzeTest targetFile" );
      System.exit(0);
    }

    Analyze analyser = new Analyze(args[0]);

    long start = System.currentTimeMillis();
    boolean dataRead = analyser.readData();
    long end = System.currentTimeMillis();

    System.out.println( "Read Data [" + dataRead + "] in " + ( end - start ) + "ms." );

    start = System.currentTimeMillis();
    boolean jsonLoad = analyser.loadJSON();
    end = System.currentTimeMillis();

    System.out.println( "Convert to parsable format [" + jsonLoad + "] in " + ( end - start ) + "ms." );

    // Example data pulls
    List<String> names = JsonPath.read( analyser.getParsedData(), "$.data.result[*].metric.pod_name");
    System.out.println( "Read " + names.size() + " names." );

    HashMap<String,Object> records = JsonPath.read( analyser.getParsedData(), "$.data.result[1]");
    System.out.println( records.size() );

    for( String key : records.keySet())
    {
      System.out.println( key + " " + records.get(key).getClass().getSimpleName());
    }
    System.out.println("");

    Map<String,String> metadata = (HashMap<String,String>)records.get("metric");

    for( String key : metadata.keySet())
    {
      System.out.println( key + ":" + metadata.get(key));
    }

    JSONArray metricsValues = (JSONArray)records.get("values");

    System.out.println( "Values size:" + metricsValues.size());
    System.out.println( "First entry:" + metricsValues.get(0));

    for( int loop = 0; loop < metricsValues.size(); loop++ )
    {
      String working = metricsValues.get(loop).toString();

      working = working.substring(1,working.length()-2);
      String[] components = working.split( "," );
  
      Float epoch = new Float(components[0]);
      Float cpu = new Float(components[1].substring(1));
  
      System.out.println( epoch.toString() + " " + cpu.toString() );  
    }

    //for( String name : names )
    //{
    //  System.out.print( name + " " );
    //}

    //System.out.println( "" );

    //List<String> authors = JsonPath.read(json, "$.data.result.__name__".book[*].author");
  }
}