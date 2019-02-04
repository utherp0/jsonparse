package org.uth.jsonparse.currency;

import java.util.*;

public class Metrics
{
  private String _namespace = null;
  private String _pod = null;
  private String _region = null;
  private String _zone = null;
  private String _cluster = null;

  private Map<Long,Double> _metrics = null;

  // Accessors
  public String getNamespace() { return _namespace; }
  public String getPod() { return _pod; }
  public String getRegion() { return _region; }
  public String getZone() { return _zone; }
  public String getCluster() { return _cluster; }
  public Map<Long,Double> getMetrics() { return _metrics; }
  
  public Metrics( String namespace, String pod, String region, String zone, String cluster )
  {
    _namespace = namespace;
    _pod = pod;
    _region = region;
    _zone = zone;
    _cluster = cluster;

    _metrics = new HashMap<Long,Double>();;
  }

  public void addJSONMetric( String basejson )
  {
    //if( _pod.equals("cpmx-189-ws8l2")) { System.out.println( "cpmx-189-ws8l2 " + basejson ); }

    String working = basejson.substring(1,basejson.length()-2);
    String[] components = working.split( "," );

    Double epoch = new Double(components[0]);

    long epochUTC = (long)epoch.doubleValue() * 1000;

    Double cpu = new Double(components[1].substring(1));

    _metrics.put(epochUTC,cpu);
  }
}