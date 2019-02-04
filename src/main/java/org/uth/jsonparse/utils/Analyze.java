package org.uth.jsonparse.utils;

import java.util.*;
import java.io.*;
import org.uth.jsonparse.currency.*;
import com.jayway.jsonpath.*;
import net.minidev.json.JSONArray;

public class Analyze
{
  Object _parsedFile = null;
  String _targetFile = null;
  String _rawData = null;
  List<Metrics> _metrics = null;

  public Analyze( String inputFile )
  {
    _targetFile = inputFile;
    _metrics = new ArrayList<Metrics>();
  }

  // Accessors
  public List<Metrics> getMetrics() { return _metrics; }

  public Object getParsedData() { return _parsedFile; }

  public boolean readData()
  {
    try
    {
      _rawData = FileRead.read(_targetFile );
      return true;
    }
    catch( Exception exc )
    {
      return false;
    }
  }

  public boolean loadJSON()
  {
    try
    {
      _parsedFile = Configuration.defaultConfiguration().jsonProvider().parse(_rawData);
      return true;
    }
    catch( Exception exc )
    {
      return false;
    }
  }

  public void extract()
  {
    if( _parsedFile == null ) return;

    List<String> names = JsonPath.read( _parsedFile, "$.data.result[*].metric.pod_name");

    for( int loop = 0; loop < names.size(); loop++ )
    {
      HashMap<String,Object> records = JsonPath.read( _parsedFile, "$.data.result[" + loop + "]");
      JSONArray values = JsonPath.read( _parsedFile, "$.data.result[" + loop + "].values");

      // Extract the metadata and create a Metrics object with it
      Map<String,String> metadata = (HashMap<String,String>)records.get("metric");

      String cluster = metadata.get("cluster");
      String namespace = metadata.get("namespace");
      String pod = metadata.get("pod_name");
      String zone = metadata.get("zone");
      String region = metadata.get("region");

      Metrics metrics = new Metrics(namespace, pod, region, zone, cluster);

      // Extract the actual metrics (epoch/cpu) and add them to the Metrics object
      //JSONArray metricsValues = (JSONArray)records.get("values");
      JSONArray metricsValues = values;

      for( int metricsLoop = 0; metricsLoop < metricsValues.size(); metricsLoop++ )
      {
        metrics.addJSONMetric(metricsValues.get(metricsLoop).toString());
      }

      _metrics.add(metrics);
    }
  }
}