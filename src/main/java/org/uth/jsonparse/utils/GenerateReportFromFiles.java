package org.uth.jsonparse.utils;

import java.io.*;
import java.util.*;
import org.uth.jsonparse.currency.*;

public class GenerateReportFromFiles
{
  public static void main(String[] args )  
  {
    if( args.length != 4 )
    {
      System.out.println( "Usage: java GenerateReportFromFiles sourceDir targetReportFile start(YYYY-MM-DD hh:mm:ss) finish(YYYY-MM-DD hh:mm:ss)");
      System.exit(0);
    }  

    long start = 0;
    long end = 0;
      
    try
    {
      start = UTCUtils.convertToUTC(args[2]);
      end = UTCUtils.convertToUTC(args[3]);
    }
    catch( Exception exc )
    {
      System.out.println( "Format of dates is incorrect (should be YYYY-MM-DD hh:mm:ss)");
      System.exit(0);
    }

    new GenerateReportFromFiles( args[0], args[1], start, end );
  }

  public GenerateReportFromFiles( String sourceDir, String targetFile, long start, long end )
  {
    List<Metrics> allMetrics = new ArrayList<Metrics>();

    try
    {
      // Process all the files in the target directory
      File folder = new File( sourceDir );
      File[] inputData = folder.listFiles();

      long startPoint = System.currentTimeMillis();

      for( File input : inputData )
      {
        Analyze analyser = new Analyze( input.getAbsolutePath() );

        if( analyser.readData() != true )
        {
          System.out.println( "Failed to read file " + input.getAbsolutePath() );
          System.exit(0);
        }
    
        if( analyser.loadJSON() != true )
        {
          System.out.println( "Unable to load JSON data from file " + input.getAbsolutePath() );
          System.exit(0);
        }
    
        analyser.extract();
    
        List<Metrics> metrics = analyser.getMetrics();

        for( Metrics record : metrics )
        {
          allMetrics.add(record);
        }    
      }

      long endPoint = System.currentTimeMillis();

      System.out.println( "Read " + inputData.length + " files, attained " + allMetrics.size() + " metric sets in " + ( endPoint - startPoint ) + "ms.");
    }
    catch( Exception exc )
    {
      System.out.println( "Failed to process files due to " + exc.toString());
      System.exit(0);
    }

    // Find dataset start and end dates
    long lowest = Long.MAX_VALUE;
    long highest = Long.MIN_VALUE;

    for( Metrics metric : allMetrics )
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
      System.out.println( "Refining generation between " + UTCUtils.generateHumanReadable(start) + " and " + UTCUtils.generateHumanReadable(end));
    }

    // Get the namespaces
    List<String> namespaces = new ArrayList<>();

    for( Metrics data : allMetrics )
    {
      if( !( namespaces.contains(data.getNamespace()))) namespaces.add( data.getNamespace());
    }

    System.out.println( "Found " + namespaces.size() + " namespaces in the data file.");

    // Build cpu usage counts for *namespaces*
    Map<String,Double> namespaceCounts = new HashMap<String,Double>();

    for( Metrics data : allMetrics )
    {
      // Calculate the CPU differences
      double minimum = Double.MAX_VALUE;
      double maximum = Double.MIN_VALUE;
      Map<Long,Double> values = data.getMetrics();

      for( Long datestamp : values.keySet())
      {
        // Only process if startPoint is zero *or* the timestamp falls between the targets
        if( start == 0 || 
            ( datestamp.doubleValue() >= start && datestamp.doubleValue() <= end ))
        {
          Double value = values.get(datestamp);
          
          if( value.doubleValue() < minimum ) minimum = value.doubleValue();
          if( value.doubleValue() > maximum ) maximum = value.doubleValue();
        }
      }

      double cpudiff = maximum - minimum;

      String namespace = data.getNamespace();

      if( !( namespaceCounts.containsKey(namespace)))
      {
        namespaceCounts.put(namespace, new Double(cpudiff));
      }
      else
      {
        Double current = namespaceCounts.get(namespace);

        current = ( current > cpudiff ) ? current : cpudiff;

        namespaceCounts.remove(namespace);
        namespaceCounts.put(namespace, current );
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

    for( Metrics data : allMetrics )
    {
      // Aggregate the CPU counts for this metric
      double minimum = Double.MAX_VALUE;
      double maximum = Double.MIN_VALUE;

      Map<Long,Double> values = data.getMetrics();

      System.out.println( "Metric count for pod " + data.getPod() + " = " + values.size());

      for( Long datestamp : values.keySet())
      {
        // Only process if startPoint is zero *or* the timestamp falls between the targets
        if( start == 0 || 
            ( datestamp.doubleValue() >= start && datestamp.doubleValue() <= end ))
        {
          Double value = values.get(datestamp);

          if( value.doubleValue() > maximum ) maximum = value.doubleValue();
          if( value.doubleValue() < minimum ) minimum = value.doubleValue();
        }
      }

      String podName = data.getNamespace() + "/" + data.getPod();
      double cpudiff = maximum - minimum;

      if( !( podCPUCounts.containsKey(podName)))
      {
        podCPUCounts.put(podName, new Double( cpudiff ));
      }
      else
      {
        Double current = podCPUCounts.get(podName);

        if( cpudiff > current.doubleValue())
        {
          podCPUCounts.remove(podName);
          podCPUCounts.put(podName, new Double( cpudiff));
        }
      }
    }

    // Sort the podNames
    List<String> podNames = new ArrayList<>();

    for( Metrics data : allMetrics )
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