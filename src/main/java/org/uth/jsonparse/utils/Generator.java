package org.uth.jsonparse.utils;

import java.util.*;
import org.uth.jsonparse.currency.*;

public class Generator
{
  public static void main( String[] args )
  {
    if( args.length != 1 && args.length != 3 )
    {
      System.out.println( "Usage: java Generator targetFile" );
      System.out.println( "Usage: java Generator targetFile start(YYYY-MM-DD hh:mm:ss) end(YYYY-MM-DD hh:mm:ss)");
      System.exit(0);
    }

    long start = 0;
    long end = 0;

    if( args.length == 3 )
    {
      try
      {
        start = UTCUtils.convertToUTC(args[1]);
        end = UTCUtils.convertToUTC(args[2]);
      }
      catch( Exception exc )
      {
        System.out.println( "Format of dates is incorrect (should be YYYY-MM-DD hh:mm:ss)");
        System.exit(0);
      }
    }

    new Generator( args[0], start, end );
  }

  public Generator( String targetFile, long startPoint, long endPoint )
  {
    System.out.println( "Analysing " + targetFile );

    long start = System.currentTimeMillis();
    Analyze analyser = new Analyze( targetFile );

    if( analyser.readData() != true )
    {
      System.out.println( "Failed to read file " + targetFile );
      System.exit(0);
    }

    if( analyser.loadJSON() != true )
    {
      System.out.println( "Unable to load JSON data from file " + targetFile );
      System.exit(0);
    }

    analyser.extract();

    List<Metrics> metrics = analyser.getMetrics();

    long end = System.currentTimeMillis();

    System.out.println( "Read " + metrics.size() + " pod metrics in " + ( end - start ) + "ms." );

    // Find dataset start and end dates
    long lowest = Long.MAX_VALUE;
    long highest = Long.MIN_VALUE;

    for( Metrics metric : metrics )
    {
      Map<Long,Double> values = metric.getMetrics();

      for( Long value : values.keySet())
      {
        if( value.longValue() < lowest ) lowest = value.longValue();
        if( value.longValue() > highest ) highest = value.longValue();
      }
    }

    System.out.println( UTCUtils.generateHumanReadable(lowest) + " start of metrics.");
    System.out.println( UTCUtils.generateHumanReadable(highest) + " end of metrics.");

    if( start != 0 )
    {
      System.out.println( "Refining generation between " + UTCUtils.generateHumanReadable(startPoint) + " and " + UTCUtils.generateHumanReadable(endPoint));
    }

    // Get the namespaces
    List<String> namespaces = new ArrayList<>();

    for( Metrics data : metrics )
    {
      if( !( namespaces.contains(data.getNamespace()))) namespaces.add( data.getNamespace());
    }

    System.out.println( "Found " + namespaces.size() + " namespaces in the data file.");

    // Build cpu usage counts for *namespaces*
    Map<String,Double> namespaceCounts = new HashMap<String,Double>();

    for( Metrics data : metrics )
    {
      // Aggregate the CPU counts for this metric
      double count = 0;
      Map<Long,Double> values = data.getMetrics();

      for( Long datestamp : values.keySet())
      {
        // Only process if startPoint is zero *or* the timestamp falls between the targets
        if( startPoint == 0 || 
            ( datestamp.doubleValue() >= startPoint && datestamp.doubleValue() <= endPoint ))
        {
          Double value = values.get(datestamp);
          count += value.doubleValue();
        }
      }

      String namespace = data.getNamespace();

      if( !( namespaceCounts.containsKey(namespace)))
      {
        namespaceCounts.put(namespace, new Double(count));
      }
      else
      {
        Double current = namespaceCounts.get(namespace);
        Double newValue = new Double( current.doubleValue() + count );

        namespaceCounts.remove(namespace);
        namespaceCounts.put(namespace, newValue );
      }
    }

    // Sort the namespaces 
    Collections.sort(namespaces, String.CASE_INSENSITIVE_ORDER);

    StringBuilder namespaceOutput = new StringBuilder();
    for( String name : namespaces )
    {
      System.out.printf( name + " : %f", namespaceCounts.get(name) );
      System.out.println( "" ) ;

      namespaceOutput.append( name + "," + namespaceCounts.get(name) +"\n" );
    }

    //System.out.println( namespaceOutput.toString());

    // Now namespace/pod cpu counts
    Map<String,Double> podCPUCounts = new HashMap<>();

    for( Metrics data : metrics )
    {
      // Aggregate the CPU counts for this metric
      double count = 0;
      Map<Long,Double> values = data.getMetrics();

      System.out.println( "Metric count for pod " + data.getPod() + " = " + values.size());

      for( Long datestamp : values.keySet())
      {
        // Only process if startPoint is zero *or* the timestamp falls between the targets
        if( startPoint == 0 || 
            ( datestamp.doubleValue() >= startPoint && datestamp.doubleValue() <= endPoint ))
        {
          Double value = values.get(datestamp);
          //if( data.getPod().equals( "cpmx-189-ws8l2") && data.getNamespace().equals( "dk0257-c") ) System.out.println( "INC COUNT cpmx-189-ws8l2  with " + value );

          count += value.doubleValue();
        }
      }

      String podName = data.getNamespace() + "/" + data.getPod();

      if( !( podCPUCounts.containsKey(podName)))
      {
        //if( data.getPod().equals( "cpmx-189-ws8l2") && data.getNamespace().equals( "dk0257-c")  ) System.out.println( "CREATING cpmx-189-ws8l2  with " + count );
        podCPUCounts.put(podName, new Double(count));
      }
      else
      {
        Double current = podCPUCounts.get(podName);
        //if( data.getPod().equals( "cpmx-189-ws8l2")  && data.getNamespace().equals( "dk0257-c")  ) System.out.println( "CURRENTLY cpmx-189-ws8l2  with " + current );
        //if( data.getPod().equals( "cpmx-189-ws8l2")  && data.getNamespace().equals( "dk0257-c")  ) System.out.println( "ADDING cpmx-189-ws8l2  with " + count );


        Double newValue = new Double( current.doubleValue() + count );

        //if( data.getPod().equals( "cpmx-189-ws8l2")  && data.getNamespace().equals( "dk0257-c")  ) System.out.println( "REPLACING cpmx-189-ws8l2  with " + newValue );
      
        podCPUCounts.remove(podName);
        podCPUCounts.put(podName, newValue );
      }
    }

    // Sort the podNames
    List<String> podNames = new ArrayList<>();

    for( Metrics data : metrics )
    {
      String aggregateName = data.getNamespace() + "/" + data.getPod();

      if( !( podNames.contains(aggregateName))) podNames.add( aggregateName);
    }

    Collections.sort(podNames, String.CASE_INSENSITIVE_ORDER);

    StringBuilder podOutput = new StringBuilder();
    for( String name : podNames )
    {
      System.out.printf( name + " : %f", podCPUCounts.get(name) );
      System.out.println( "" ) ;

      podOutput.append( name + "," + podCPUCounts.get(name) +"\n" );
    }

    //System.out.println( podOutput.toString());
  }
}