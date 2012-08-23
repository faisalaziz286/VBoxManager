package com.kedzie.vbox.metrics;


/**
 * Metric data queried from server
 */
public class MetricQuery {
	public String name;
	public String unit;
	public int scale;
	public String object;
	public int[] values;
	
	public String toString() {
		return "Metric Query - "+name+" - "+unit+" - "+scale+" - "+object+" - # values: " + values.length;
	}
}