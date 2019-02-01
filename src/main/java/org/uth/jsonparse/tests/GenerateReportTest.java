package org.uth.jsonparse.tests;

import org.uth.jsonparse.utils.*;
import org.uth.jsonparse.currency.*;
import java.util.*;

public class GenerateReportTest
{
  public static void main( String[] args )
  {
    if( args.length != 1 )
    {
      System.out.println( "Usage: java GenerateReportTest targetFile" );
      System.exit(0);
    }

    new GenerateReportTest( args[0]);
  }

  public GenerateReportTest( String target )
  {
    long start = System.currentTimeMillis();
    Analyze analyser = new Analyze( target );

    if( analyser.readData() != true )
    {
      System.out.println( "Failed to read file " + target );
      System.exit(0);
    }

    if( analyser.loadJSON() != true )
    {
      System.out.println( "Unable to load JSON data from file " + target );
    }

    analyser.extract();

    List<Metrics> metrics = analyser.getMetrics();

    long end = System.currentTimeMillis();

    System.out.println( "Read " + metrics.size() + " pod metrics in " + ( end - start ) + "ms." );

    // Work out how many namespaces
    List<String> namespaces = new ArrayList<>();

    for( Metrics data : metrics )
    {
      if( !( namespaces.contains(data.getNamespace()))) namespaces.add( data.getNamespace());
    }

    System.out.println( "Data contains " + namespaces.size() + " namespaces." );

    // Build cpu usage counts for *namespaces*
    Map<String,Double> namespaceCounts = new HashMap<String,Double>();

    for( Metrics data : metrics )
    {
      // Aggregate the CPU counts for this metric
      double count = 0;
      Map<Long,Double> values = data.getMetrics();

      for( Double value : values.values())
      {
        count += value.doubleValue();
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

    for( String name : namespaces )
    {
      System.out.printf( name + " : %f", namespaceCounts.get(name) );
      System.out.println( "" ) ;
    }
  }
}