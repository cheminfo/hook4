package org.cheminfo.hook.util;

import java.awt.geom.Point2D;

public class HookStatistics
{
	
	public static double meanY(Point2D.Double[] points)
	{
		double sum=0;
		
		for (int p=0; p < points.length; p++)
			sum+=points[p].y;
		
		return sum/points.length;
	}
	
	public static double standardDeviationY(Point2D.Double[] points)
	{
		double squareDeviationsSum=0;
		
		double mean=HookStatistics.meanY(points);

		for (int p=0; p < points.length; p++)
			squareDeviationsSum+=(points[p].y-mean)*(points[p].y-mean);
		
		return Math.sqrt(squareDeviationsSum/(points.length));
	}
	
	public static double zedFactor(Point2D.Double[] controlPoints, Point2D.Double[] samplePoints)
	{
		double meanControl=HookStatistics.meanY(controlPoints);
		double meanSample=HookStatistics.meanY(samplePoints);
		
		double sdControl=HookStatistics.standardDeviationY(controlPoints);
		double sdSample=HookStatistics.standardDeviationY(samplePoints);
		
		return 1-(3*sdControl+3*sdSample)/(Math.abs(meanControl-meanSample));
	}
	

	public static double meanY(double[] points)
	{
		double sum=0;
		
		for (int p=0; p < points.length; p++)
			sum+=points[p];
		
		return sum/points.length;
	}
	
	public static double standardDeviationY(double[] points)
	{
		double squareDeviationsSum=0;
		
		double mean=HookStatistics.meanY(points);

		for (int p=0; p < points.length; p++)
			squareDeviationsSum+=(points[p]-mean)*(points[p]-mean);
		
		return Math.sqrt(squareDeviationsSum/(points.length));
	}
	
	public static double zedFactor(double[] controlPoints, double[] samplePoints)
	{
		double meanControl=HookStatistics.meanY(controlPoints);
		double meanSample=HookStatistics.meanY(samplePoints);
		
		double sdControl=HookStatistics.standardDeviationY(controlPoints);
		double sdSample=HookStatistics.standardDeviationY(samplePoints);
		
		return 1-(3*sdControl+3*sdSample)/(Math.abs(meanControl-meanSample));
	}

	
}