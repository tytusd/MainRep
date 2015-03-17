package org.retentionprediction.lcdatabasebuilderfx.business;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.PointVectorValuePair;
import org.apache.commons.math3.fitting.CurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.optim.nonlinear.vector.jacobian.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.util.Precision;

// This class solves for the retention parameters of the given compound.
class SolveParametersTask extends Task
{
	private SimpleStringProperty varianceProperty = new SimpleStringProperty("");
	private SimpleStringProperty iterationProperty = new SimpleStringProperty("");
	private SimpleStringProperty timeElapsedProperty = new SimpleStringProperty("");
	private SimpleStringProperty dHProperty = new SimpleStringProperty("");
	private SimpleStringProperty dSProperty = new SimpleStringProperty("");
	private SimpleStringProperty dCpProperty = new SimpleStringProperty("");
	private ObservableList<StandardCompound> programsList;
	
	private boolean updateVarianceReady = true;
	private boolean updateIterationReady = true;
	private boolean updateTimeElapsedReady = true;
	private boolean updateTableReady = true;
	private boolean updateHReady = true;
	private boolean updateSReady = true;
	private boolean updateCpReady = true;

	// The retention times from each temperature program
	private double[] retentionTimes = new double[6];
	private LinearInterpolationFunction[] temperaturePrograms = new LinearInterpolationFunction[6];
	private InterpolationFunction[] holdUpTimeProfiles = new InterpolationFunction[6];
	private double[] innerDiameter = new double[6];
	private double[] filmThickness = new double[6];
	private double[] dtstep = new double[6];
	
	private double[] predictedRetentionTimes = new double[6];
	private double[] expectedRetentionTimeError = new double[6];
	private double bestVariance;
	private double[] bestParameters;
	
    private SolveParametersListener solveParametersListener;

	public interface SolveParametersListener 
	{
		public void onUpdateTable(ObservableList<StandardCompound> list);
    }

	public void setSolveParametersListener(SolveParametersListener thisListener)
	{
		solveParametersListener = thisListener;
	}
	
	public void setTStep(double[] tStep)
	{
		dtstep = tStep;
	}
	
	public void setRetentionTimes(double[] retentionTimes)
	{
		this.retentionTimes = retentionTimes;
	}
	
	public void setTemperaturePrograms(LinearInterpolationFunction[] temperaturePrograms)
	{
		this.temperaturePrograms = temperaturePrograms;
	}
	
	public void setHoldUpTimeProfiles(InterpolationFunction[] holdUpTimeProfiles)
	{
		this.holdUpTimeProfiles = holdUpTimeProfiles;
	}
	
	public void setInnerDiameter(double[] innerDiameter)
	{
		this.innerDiameter = innerDiameter;
	}
	
	public void setFilmThickness(double[] filmThickness)
	{
		this.filmThickness = filmThickness;
	}

	public SimpleStringProperty dHProperty()
	{
		return this.dHProperty;
	}

	public SimpleStringProperty dSProperty()
	{
		return this.dSProperty;
	}
	
	public SimpleStringProperty dCpProperty()
	{
		return this.dCpProperty;
	}

	public SimpleStringProperty varianceProperty()
	{
		return varianceProperty;
	}
	
	public SimpleStringProperty timeElapsedProperty()
	{
		return timeElapsedProperty;
	}

	public SimpleStringProperty iterationProperty()
	{
		return iterationProperty;
	}
	
	private void updateH(final double dH)
	{
		if (!updateHReady)
			return;
		
		final String str;
		str = Float.toString((float)Globals.roundToSignificantFigures(dH, 6));
		
		updateHReady = false;
		
		Platform.runLater(new Runnable(){
            @Override
            public void run() {
            	dHProperty.set(str);
        		updateHReady = true;
            }
        });	
	}

	private void updateS(final double dS)
	{
		if (!updateSReady)
			return;
		
		final String str;
		str = Float.toString((float)Globals.roundToSignificantFigures(dS, 6));
		
		updateSReady = false;

		Platform.runLater(new Runnable(){
            @Override
            public void run() {
            	dSProperty.set(str);
        		updateSReady = true;
            }
        });	
	}
	
	private void updateCp(final double dCp)
	{
		if (!updateCpReady)
			return;
		
		final String str;
		str = Float.toString((float)Globals.roundToSignificantFigures(dCp, 6));
		
		updateCpReady = false;

		Platform.runLater(new Runnable(){
            @Override
            public void run() {
            	dCpProperty.set(str);
        		updateCpReady = true;
            }
        });	
	}
	
	private void updateVariance(double variance)
	{
		if (!updateVarianceReady)
			return;

		NumberFormat formatter1 = new DecimalFormat("#0.000000");
		NumberFormat formatter2 = new DecimalFormat("0.0000E0");

		final String str;
		if (variance < 0.0001)
			str = formatter2.format(variance);
		else
			str = formatter1.format(variance);
		
		updateVarianceReady = false;
		Platform.runLater(new Runnable(){
            @Override
            public void run() {
            	varianceProperty.set(str);
            	updateVarianceReady = true;
            }
        });	
	}
	
	private void updateTimeElapsed(long starttime)
	{
		if (!updateTimeElapsedReady)
			return;

		NumberFormat timeformatter = new DecimalFormat("00");
		
		long currentTime = System.currentTimeMillis();
		long lNumSecondsPassed = (currentTime - starttime) / 1000;
		long lNumDaysPassed = lNumSecondsPassed / (24 * 60 * 60);
		lNumSecondsPassed -= lNumDaysPassed * (24 * 60 * 60);
		long lNumHoursPassed = lNumSecondsPassed / (60 * 60);
		lNumSecondsPassed -= lNumHoursPassed * (60 * 60);
		long lNumMinutesPassed = lNumSecondsPassed / (60);
		lNumSecondsPassed -= lNumMinutesPassed * (60);
		
		final String str = timeformatter.format(lNumHoursPassed) + ":" + timeformatter.format(lNumMinutesPassed) + ":" + timeformatter.format(lNumSecondsPassed);
		updateTimeElapsedReady = false;
		Platform.runLater(new Runnable(){
            @Override
            public void run() {
            	timeElapsedProperty.set(str);
            	updateTimeElapsedReady = true;
            }
        });
	}

	private void updateIteration(final int iteration)
	{
		if (!updateIterationReady)
			return;

		updateIterationReady = false;
		Platform.runLater(new Runnable(){
            @Override
            public void run() {
            	iterationProperty.set(Integer.toString(iteration));
            	updateIterationReady = true;
            }
        });
	}

	private void updateTable()
	{
		if (!updateTableReady)
			return;

		// Make a deep copy of the standardsList
		final ObservableList<StandardCompound> currentStandardsList = FXCollections.observableArrayList();
		for (int i = 0; i < programsList.size(); i++)
		{
			StandardCompound newStandardCompound = new StandardCompound(programsList.get(i).getUse(), programsList.get(i).getName(), programsList.get(i).getMz(), programsList.get(i).getMeasuredRetentionTime(), programsList.get(i).getPredictedRetentionTime(), programsList.get(i).getIndex());
			currentStandardsList.add(newStandardCompound);
		}

		updateTableReady = false;
		Platform.runLater(new Runnable(){
            @Override
            public void run() {
            	//ParentPaneController.this.updateTable(currentStandardsList);
            	SolveParametersTask.this.solveParametersListener.onUpdateTable(currentStandardsList);
            	updateTableReady = true;
            }
        });
	}

	class RetentionErrorFunction implements ParametricUnivariateFunction
	{
		@Override
		public double value(double x, double... parameters) 
		{
			if (x < 0 || x > 6)
				return 0;
			
			double retentionTime = calcRetentionErrorForOneProgram((int)x, parameters[0], parameters[1], parameters[2]);
			return retentionTime;
		}

		@Override
		public double[] gradient(double x, double... parameters) 
		{
			if (x < 0 || x > 5)
				return new double[] {0,0,0};
			
			double[] gradient = new double[3];
			
			gradient[0] = calcRetentionErrorForOneProgram((int)x, parameters[0] + 1, parameters[1], parameters[2]) - calcRetentionErrorForOneProgram((int)x, parameters[0] - 1, parameters[1], parameters[2]);
			gradient[1] = calcRetentionErrorForOneProgram((int)x, parameters[0], parameters[1] + 1, parameters[2]) - calcRetentionErrorForOneProgram((int)x, parameters[0], parameters[1] - 1, parameters[2]);
			gradient[2] = calcRetentionErrorForOneProgram((int)x, parameters[0], parameters[1], parameters[2] + 1) - calcRetentionErrorForOneProgram((int)x, parameters[0], parameters[1], parameters[2] - 1);
			
			return gradient;
		}
	}
	
    @Override
    public void done() 
    {
    	if (!this.isCancelled())
    	{
        		updateProgress(100, 100);
        		updateMessage("Optimization complete!");
    	}
    }
    
	// For each particle:
	// Initialization:
		// Pick its values randomly within each parameters boundaries
		// Initialize the particle's best known position to it's initial position
		// If the error is lower than the swarm's lowest error, set the swarm's lowest error to this particle's error.
		// Initialize the particle's velocity with a random value (negative or positive)
	// Then, in each iteration:
		// Update the particle's velocity in each dimension:
			// vi,d = (w * vi,d) + (phip * rp * (pi,d - xi,d)) + (phig * rg * (gd - xi,d))
			// where: 
			// vi,d = velocity of particle i in d dimension
			// w = tune parameter to adjust importance of current velocity
			// phip = tune parameter to adjust importance of this particle's best known position
			// rp = random number (0 to 1) that weights the importance of this particle's best known position
			// (pi,d-xi,d) = vector pointing back to best known position for this particle
			// phig = tune parameter to adjust importance of the swarm's best known position
			// rg = random number (0 to 1) that weights the importance of the swarm's best known position
			// (gd-xi,d) = vector pointing to best known position of the swarm.

    @Override
	protected Object call() throws Exception 
	{
		long starttime = System.currentTimeMillis();

		updateProgress(-1, 0);
		updateMessage("Please wait, optimization in progress...");
		
		bestVariance = 100;
		bestParameters = new double[3];

		final double[] maxBounds = {-30000, -100, 120};
		final double[] minBounds = {-100000, -200, 50}; 

		int iteration = 0;
		
		while (/*bestVariance > 1E-3 && */iteration < 100)
		{
			iteration++;
			
			updateIteration(iteration);
			
			this.updateTimeElapsed(starttime);

			// Pick values randomly within each parameter's boundaries
			double[] position = new double[maxBounds.length];
			
			for (int param = 0; param < maxBounds.length; param++)
			{
				position[param] = (Math.random() * (maxBounds[param] - minBounds[param])) + minBounds[param];
			}
			
			double variance = calcVarianceForAllPrograms(position[0], position[1], position[2]);

			// Each cycle is one iteration for each particle
			ConvergenceChecker<PointVectorValuePair> convergenceChecker = new ConvergenceChecker<PointVectorValuePair>()
					{

				@Override
				public boolean converged(int iteration,	PointVectorValuePair previous, PointVectorValuePair current) {
					// Calculate error for previous and current
					double previousError = 0;
					double currentError = 0;
					int pos = 0;
					for (int i = 0; i < retentionTimes.length; i++)
					{
						if (retentionTimes[i] > 0)
						{
							previousError += Math.pow(retentionTimes[i] - previous.getValue()[pos], 2);
							currentError += Math.pow(retentionTimes[i] - current.getValue()[pos], 2);
							pos++;
						}
					}

					if ((currentError / previousError) > .9999999)
						return true;
					else
						return false;
				}
				
			};
			
			LevenbergMarquardtOptimizer lmOptimizer = new LevenbergMarquardtOptimizer(100, convergenceChecker, 1E-10, 1E-10, 1E-10, Precision.SAFE_MIN);
			
			CurveFitter<ParametricUnivariateFunction> fitter = new CurveFitter<ParametricUnivariateFunction>(lmOptimizer);
			for (int i = 0; i < retentionTimes.length; i++)
			{
				if (this.retentionTimes[i] > 0)
					fitter.addObservedPoint(i, this.retentionTimes[i]);
			}
			double[] bestFit = new double[3];
			
			try
			{
				bestFit = fitter.fit(100, new RetentionErrorFunction(), position);
			}
			catch (TooManyEvaluationsException e)
			{
				
			}
			catch (DimensionMismatchException e)
			{
				
			}
			
			// Now see if the error is lower than it was before
			double newError = calcVarianceForAllPrograms(bestFit[0], bestFit[1], bestFit[2]);
			
			if (newError < bestVariance)
			{
				bestVariance = newError;
				bestParameters = bestFit;
				
				this.updateVariance(bestVariance);
				this.updateH(bestFit[0]);
				this.updateS(bestFit[1]);
				this.updateCp(bestFit[2]);
				for (int i = 0; i < programsList.size(); i++)
				{
					programsList.get(i).setPredictedRetentionTime(this.predictedRetentionTimes[i]);
				}
				this.updateTable();
			}
		}
		
		// Now we have approximately the correct values. Now we should solve again with the values weighted correctly.
		
		ConvergenceChecker<PointVectorValuePair> convergenceChecker = new ConvergenceChecker<PointVectorValuePair>()
				{

			@Override
			public boolean converged(int iteration,	PointVectorValuePair previous, PointVectorValuePair current) 
			{
				// Calculate error for previous and current
				double previousError = 0;
				double currentError = 0;
				int pos = 0;
				for (int i = 0; i < retentionTimes.length; i++)
				{
					if (retentionTimes[i] > 0)
					{
						previousError += (1 / expectedRetentionTimeError[i]) * Math.pow(retentionTimes[i] - previous.getValue()[pos], 2);
						currentError += (1 / expectedRetentionTimeError[i]) * Math.pow(retentionTimes[i] - current.getValue()[pos], 2);
						pos++;
					}
				}

				if ((currentError / previousError) > .9999999)
					return true;
				else
					return false;
			}
			
		};
		
		LevenbergMarquardtOptimizer lmOptimizer = new LevenbergMarquardtOptimizer(100, convergenceChecker, 1E-10, 1E-10, 1E-10, Precision.SAFE_MIN);
		
		CurveFitter<ParametricUnivariateFunction> fitter = new CurveFitter<ParametricUnivariateFunction>(lmOptimizer);
		for (int i = 0; i < retentionTimes.length; i++)
		{
			if (retentionTimes[i] > 0)
				fitter.addObservedPoint(new WeightedObservedPoint(1 / expectedRetentionTimeError[i], i, this.retentionTimes[i]));
		}
		
		double[] bestFit = new double[3];
		
		try
		{
			bestFit = fitter.fit(100, new RetentionErrorFunction(), bestParameters);
		}
		catch (TooManyEvaluationsException e)
		{
			
		}
		catch (DimensionMismatchException e)
		{
			
		}
		
		// Now see if the error is lower than it was before
		double newError = calcVarianceForAllPrograms(bestFit[0], bestFit[1], bestFit[2]);
		
		bestVariance = newError;
		bestParameters = bestFit;
			
		this.updateVariance(bestVariance);
		this.updateH(bestFit[0]);
		this.updateS(bestFit[1]);
		this.updateCp(bestFit[2]);
		for (int i = 0; i < programsList.size(); i++)
		{
			programsList.get(i).setPredictedRetentionTime(this.predictedRetentionTimes[i]);
		}
		this.updateTable();
		
		return null;
	}
 	
	private double calcVarianceForAllPrograms(double dH, double dS, double dCp)
	{
		double totalError = 0;
		int count = 0;
		for (int i = 0; i < retentionTimes.length; i++)
		{
			if (retentionTimes[i] > 0)
			{
				totalError += Math.pow(retentionTimes[i] - calcRetentionErrorForOneProgram(i, dH, dS, dCp), 2);
				count++;
			}
		}
		return totalError / (double)count;
	}
	
    private double calcRetentionErrorForOneProgram(int programIndex, double dH, double dS, double dCp)
    {
    	double dBeta1 = Math.pow((0.25 / 2) - (0.25 / 1000), 2) / (Math.pow(0.25 / 2, 2) - Math.pow((0.25 / 2) - (0.25 / 1000), 2));
    	double dBeta2 = Math.pow((innerDiameter[programIndex] / 2) - (filmThickness[programIndex] / 1000), 2) / (Math.pow(innerDiameter[programIndex] / 2, 2) - Math.pow((innerDiameter[programIndex] / 2) - (filmThickness[programIndex] / 1000), 2));
    	double dBeta1Beta2 = dBeta1 / dBeta2;
    	
		double dtRFinal = 0;
		double dtRErrorFinal = 0;
		double dXPosition = 0;
		double dLastXPosition = 0;
		double dXMovement = 0;
		boolean bIsEluted = false;
		double dTcA = 0;
		double dTcB = 0;
		double dTc = 0;
		double dHc = 0;
		double dCurVal = 0;
		double dk = 0;
		double dXPositionError = 0;

		// Create an array of temperatures - this will speed things up
		double dMaxTime = retentionTimes[programIndex] * 10;

		dTcA = temperaturePrograms[programIndex].getAt(0);
		
		for (double t = 0; t < dMaxTime; t += dtstep[programIndex])
		{
			dTcB = temperaturePrograms[programIndex].getAt(t + dtstep[programIndex]);
			//dTcA = dTcB;

			dTc = (dTcA + dTcB) / 2;
			dHc = holdUpTimeProfiles[programIndex].getAt(dTc) / 60;
			
			dk = getK(dTc, dH, dS, dCp) * dBeta1Beta2;
			dCurVal = dtstep[programIndex] / (1 + dk);
			
			dXMovement = dCurVal / dHc;
			
			dLastXPosition = dXPosition;
			dXPosition += dXMovement;
			
			double dXMovementErrorFraction = (dk * 0.00565) / (1 + dk);
			dXPositionError += dXMovement * dXMovementErrorFraction;

			if (dXPosition >= 1)
			{
				dtRFinal = (((1 - dLastXPosition)/(dXPosition - dLastXPosition)) * dtstep[programIndex]) + t;
				double dxdt = (dXPosition - dLastXPosition) / dtstep[programIndex];
				dtRErrorFinal = dtRFinal + (dXPositionError	/ dxdt);		

				bIsEluted = true;
				break;
			}
			
			dTcA = dTcB;
		}
		
    	double dRetentionError;

		expectedRetentionTimeError[programIndex] = dtRErrorFinal - dtRFinal;

		if (bIsEluted)
		{
			dRetentionError = dtRFinal;// - retentionTimes[programIndex];
			predictedRetentionTimes[programIndex] = dtRFinal;
		}
		else
		{
			dRetentionError = ((1 - dXPosition) / dXPosition) * dMaxTime;//retentionTimes[programIndex];
			predictedRetentionTimes[programIndex] = -1;
		}
		
    	return dRetentionError;
	}
    
    private double getK(double temperature, double dH, double dS, double dCp)
    {
    	double T0 = 273.15;
    	double A = (dS - (dCp * Math.log(T0)) - dCp) / 8.3145;
    	double B = -(dH - (dCp * T0)) / 8.3145;
    	double C = dCp / 8.3145;
    	
    	double k = Math.exp(A + B * (1 / (temperature + 273.15)) + C * Math.log(temperature + 273.15));
    	
    	return k;
    }

	public ObservableList<StandardCompound> getProgramsList() {
		return programsList;
	}

	public void setProgramsList(ObservableList<StandardCompound> programsList) {
		this.programsList = programsList;
	}

	public double getBestVariance() {
		return bestVariance;
	}

	public double[] getBestParameters() {
		return bestParameters;
	}
}