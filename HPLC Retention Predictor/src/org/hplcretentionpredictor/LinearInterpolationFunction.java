package org.hplcretentionpredictor;

import java.util.Arrays;
import java.util.Comparator;
import java.util.StringTokenizer;

import org.hplcretentionpredictor.InterpolationFunction.DataPointComparator;

class LinearInterpolationFunction
{
	public double[][] dDataArray;
	public int i;
	public double y;
	
	class DataPointComparator implements Comparator<double[]>
	{
		@Override
		public int compare(double[] arg0, double[] arg1) 
		{
			if (arg0[0] > arg1[0])
			{
				return 1;
			}
			else if (arg0[0] < arg1[0])
			{
				return -1;
			}
			else
				return 0;
		}
	}
	
	// dataPoints[x][y]
	public LinearInterpolationFunction(double[][] dataPoints)
	{
		Comparator<double[]> byXVal = new DataPointComparator();

		Arrays.sort(dataPoints, byXVal);

		dDataArray = dataPoints;
	}
	
	// In order of increasing solvent composition
	public double getAt(double x)
	{
		// If x is greater than the last number or less than the first number, extrapolate
		if (x < dDataArray[0][0])
			return extrapolateBefore(x);
		else if (x > dDataArray[dDataArray.length - 1][0])
			return extrapolateAfter(x);
		
		// Find the first number that is greater than or equal to x
		i = binarySearch(x);
//		while (x > dDataArray[i][0])
//		{
//			i++;
//			//if (i >= dDataArray.length)
//				//break;
//		}
		
		//i could be equal to dataarray.length
		
		//y = 0;
		
		if (i == dDataArray.length)
		{
			return dDataArray[dDataArray.length - 1][1];
		}
		else if (i == 0)
		{
			return dDataArray[0][1];
		}
		else
		{
			/*double dXValAfter = dDataArray[i][0];
			double dXValBefore = dDataArray[i - 1][0];
			double dXPosition = (x - dXValBefore)/(dXValAfter - dXValBefore);
			double dYValAfter = dDataArray[i][1];
			double dYValBefore = dDataArray[i - 1][1];
			
			y = (dXPosition * (dYValAfter - dYValBefore)) + dYValBefore;*/
		return (((x - dDataArray[i - 1][0])/(dDataArray[i][0] - dDataArray[i - 1][0])) * (dDataArray[i][1] - dDataArray[i - 1][1])) + dDataArray[i - 1][1];
		}

		//return y;
	}	
	
	/**
	 * binarySearch takes the value x which is provided in getAt method. This method is built only for getAt method. 
	 * @param x
	 * @return
	 */
	public int binarySearch(double x){
		int low = 0;
		int high = dDataArray.length-1;
		
		while(high - low > 1){
			int mid = (low+high)/2;
			if(x < dDataArray[mid][0]){
				high = mid;
			}
			else if(x > dDataArray[mid][0]){
				low = mid;
			}
			else{
				return mid;
			}
		}
		if(x <= dDataArray[low][0]){
			return low;
		}
		else return high;
	}
	
	public double extrapolateBefore(double x)
	{
		// Find slope
		double dSlope = (dDataArray[1][1] - dDataArray[0][1]) / (dDataArray[1][0] - dDataArray[0][0]);
		double dIntercept = dDataArray[0][1] - (dSlope * dDataArray[0][0]);
		
		return (dSlope * x) + dIntercept;
	}
	
	public double extrapolateAfter(double x)
	{
		// Find slope
		double dSlope = (dDataArray[dDataArray.length - 1][1] - dDataArray[dDataArray.length - 2][1]) / (dDataArray[dDataArray.length - 1][0] - dDataArray[dDataArray.length - 2][0]);
		double dIntercept = dDataArray[dDataArray.length - 1][1] - (dSlope * dDataArray[dDataArray.length - 1][0]);
		
		return (dSlope * x) + dIntercept;
	}
}
