package org.retentionprediction.peakfindergcfx;

public class Globals
{
	public static double[][] convertTemperatureProgramInConventionalFormToRegularForm(double[][] dTemperatureProgramInConventionalForm, double initialTemperature, double initialTime)
	{
		double[][] newTemperatureProgram = new double[(dTemperatureProgramInConventionalForm.length * 2) + 2][2];
		int iPointCount = 0;

		newTemperatureProgram[iPointCount][0] = 0.0;
		newTemperatureProgram[iPointCount][1] = initialTemperature;
		iPointCount++;
				
		newTemperatureProgram[iPointCount][0] = initialTime;
		newTemperatureProgram[iPointCount][1] = initialTemperature;
		iPointCount++;

		double dTotalTime = initialTime;
		double dLastTemp = initialTemperature;
		double dFinalTemp = initialTemperature;
		    	
		// Go through the temperature program table and create an array that contains temp vs. time
		for (int i = 0; i < dTemperatureProgramInConventionalForm.length; i++)
		{
			double dRamp = dTemperatureProgramInConventionalForm[i][0];
			dFinalTemp = dTemperatureProgramInConventionalForm[i][1];
			double dFinalTime = dTemperatureProgramInConventionalForm[i][2];

			if (dRamp != 0)
			{
				dTotalTime += (dFinalTemp - dLastTemp) / dRamp;
				newTemperatureProgram[iPointCount][0] = dTotalTime;
				newTemperatureProgram[iPointCount][1] = dFinalTemp;
				iPointCount++;
			}
					
			if (dFinalTime != 0)
			{
				if (i < dTemperatureProgramInConventionalForm.length - 1)
				{
					dTotalTime += dFinalTime;
					newTemperatureProgram[iPointCount][0] = dTotalTime;
					newTemperatureProgram[iPointCount][1] = dFinalTemp;
					iPointCount++;						
				}
			}
					
			dLastTemp = dFinalTemp;
		}
				
		newTemperatureProgram[iPointCount][0] = newTemperatureProgram[iPointCount - 1][0] * 2;
		newTemperatureProgram[iPointCount][1] = dFinalTemp;
		iPointCount++;

		// Ideal series finished
		// Now cut it down to the correct size
		double tempArray[][] = new double[iPointCount][2];
		for (int i = 0; i < iPointCount; i++)
		{
			tempArray[i][0] = newTemperatureProgram[i][0];
			tempArray[i][1] = newTemperatureProgram[i][1];
		}
		
		return tempArray;
	}
	
	public static double roundToSignificantFigures(double num, int n) 
	{
	    if (num == 0) 
	    {
	        return 0;
	    }

	    final double d = Math.ceil(Math.log10(num < 0 ? -num: num));
	    final int power = n - (int) d;

	    final double magnitude = Math.pow(10, power);
	    final long shifted = Math.round(num * magnitude);
	    
	    return shifted / magnitude;
	}
}
