package org.retentionprediction.lcdatabasebuilderfx.ui;


import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Random;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.paint.Color;

import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.fitting.CurveFitter;
import org.apache.commons.math3.optim.nonlinear.vector.jacobian.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.PointVectorValuePair;
import org.retentionprediction.lcdatabasebuilderfx.business.Globals;
import org.retentionprediction.lcdatabasebuilderfx.business.StandardCompound;
import org.retentionprediction.lcdatabasebuilderfx.ui.BackcalculateController.PredictedRetentionObject;

import boswell.graphcontrolfx.GraphControlFX;

public class SolveParametersTask extends Task{

	public interface SolveParametersListener 
	{
		public void onUpdateTable(ObservableList<StandardCompound> list);
    }
	
	private BackcalculateController[] backcalculateController;
	private ObservableList<StandardCompound> programList;

	private SimpleStringProperty varianceProperty = new SimpleStringProperty("");
	private SimpleStringProperty iterationProperty = new SimpleStringProperty("");
	private SimpleStringProperty timeElapsedProperty = new SimpleStringProperty("");
	private SimpleStringProperty AZeroProperty = new SimpleStringProperty("");
	private SimpleStringProperty AOneProperty = new SimpleStringProperty("");
	private SimpleStringProperty ATwoProperty = new SimpleStringProperty("");
	private SimpleStringProperty BOneProperty = new SimpleStringProperty("");
	private SimpleStringProperty BTwoProperty = new SimpleStringProperty("");
	
	private boolean updateVarianceReady = true;
	private boolean updateIterationReady = true;
	private boolean updateTimeElapsedReady = true;
	private boolean updateTableReady = true;
	private boolean updateAZeroReady = true;
	private boolean updateAOneReady = true;
	private boolean updateATwoReady = true;
	private boolean updateBOneReady = true;
	private boolean updateBTwoReady = true;
	protected SolveParametersListener solveParametersListener;
	private double bestVariance;
	private double[] bestParameters = new double[5];
	private GraphControlFX retentionSolverTimeGraph;
	private int measuredRetentionTimesSeries = 0;
	private int predictedRetentionTimesSeries = 0;
	
	private boolean isInjectionMode = false;
	
	public SolveParametersListener getSolveParametersListener() {
		return solveParametersListener;
	}

	public void setSolveParametersListener(
			SolveParametersListener solveParametersListener) {
		this.solveParametersListener = solveParametersListener;
	}

	@Override
	protected Object call() throws Exception {
		long time = System.currentTimeMillis();
		updateProgress(-1, 0);
		updateMessage("Please wait, optimization in progress...");
		int iteration = 0, maxIterations = 1000;
		bestVariance = 100;
		
		Random[] rand = new Random[5];
		for(int i = 0; i < 5; i++){
			rand[i] = new Random();
		}
		
		double[] measuredRetentionTimes = new double[programList.size()];
		for(int i = 0; i < programList.size(); i++){
			measuredRetentionTimes[i]  = programList.get(i).getMeasuredRetentionTime();
		}
		
		if(isInjectionMode){
			maxIterations = 200;
		}
		
		while(iteration < maxIterations){
			iteration++;
			updateIteration(iteration+"");
			
			double[] params = new double[rand.length];
			for(int i = 0; i < params.length; i++){
				params[i] = 10 - 20*rand[i].nextDouble();
			}
			
			
			//double variance = calcVarianceForAllPrograms(params[0], params[1], params[2], params[3], params[4]);
			
			// Each cycle is one iteration for each particle
			ConvergenceChecker<PointVectorValuePair> convergenceChecker = new ConvergenceChecker<PointVectorValuePair>()
					{

				@Override
				public boolean converged(int iteration,	PointVectorValuePair previous, PointVectorValuePair current) {
					// Calculate error for previous and current
					double previousError = 0;
					double currentError = 0;
					int pos = 0;
					for (int i = 0; i < programList.size(); i++)
					{
						if (programList.get(i).getMeasuredRetentionTime() > 0)
						{
							previousError += Math.pow(programList.get(i).getMeasuredRetentionTime() - previous.getValue()[pos], 2);
							currentError += Math.pow(programList.get(i).getMeasuredRetentionTime() - current.getValue()[pos], 2);
							pos++;
						}
					}

					if ((currentError / previousError) > .9999999)
						return true;
					else
						return false;
				}
				
			};
			LevenbergMarquardtOptimizer lmOptimizer = new LevenbergMarquardtOptimizer(convergenceChecker);
			
			CurveFitter<ParametricUnivariateFunction> fitter = new CurveFitter<ParametricUnivariateFunction>(lmOptimizer);
			for (int i = 0; i < programList.size(); i++)
			{
				if (programList.get(i).getMeasuredRetentionTime() > 0)
					fitter.addObservedPoint(i, programList.get(i).getMeasuredRetentionTime());
			}
			double[] bestFit = new double[5];
			
			try
			{
				bestFit = fitter.fit(100, new RetentionErrorFunction(), params);
			}
			catch (TooManyEvaluationsException e)
			{
				
			}
			catch (DimensionMismatchException e)
			{
				
			}
			
			double newError = calcVarianceForAllPrograms(bestFit[0], bestFit[1], bestFit[2], bestFit[3], bestFit[4]);
			
			if (newError < bestVariance)
			{
				//Slope at phi  = 0.1
				double padeNumerator = (bestFit[0] + bestFit[1]*0.1 + bestFit[2]*0.1*0.1);
	 			double padeDenominator = (1 + bestFit[3]*0.1 + bestFit[4]*0.1*0.1);
	 			
	 			if(padeDenominator == 0){
	 				continue;
	 			}
				double slope1 = (padeDenominator*(bestFit[1] + 2*bestFit[2]*0.1) - padeNumerator*(bestFit[3] + 2*bestFit[4]*0.1))/(padeDenominator*padeDenominator);
				
				//Slope at phi  = 5
				padeNumerator = (bestFit[0] + bestFit[1]*5 + bestFit[2]*5*5);
	 			padeDenominator = (1 + bestFit[3]*5 + bestFit[4]*5*5);
	 			
	 			if(padeDenominator == 0){
	 				continue;
	 			}
				double slope2 = (padeDenominator*(bestFit[1] + 2*bestFit[2]*5) - padeNumerator*(bestFit[3] + 2*bestFit[4]*5))/(padeDenominator*padeDenominator);
	 			
				//Slope at 0.1 and at 5 should be negative because they are decreasing. Also, slope at 0.1 should be less than the slope at 5.
				if(slope1 > slope2 || slope1 > 0 || slope2 > 0){
	 				continue;
	 			}
	 			
	 			boolean skipThisIteration = false;
	 			
	 			//Now check if every 0.2 of phi between 0 and 5 is negative. if it isn't, then we do not have valid coefficients for Pade.
	 			for(double phi = 0.0001; phi <= 5.0001; phi = phi + 0.2 ){
	 				//Slope at phi  = 0.1
					padeNumerator = (bestFit[0] + bestFit[1]*phi + bestFit[2]*phi*phi);
		 			padeDenominator = (1 + bestFit[3]*phi + bestFit[4]*phi*phi);
		 			
		 			if(padeDenominator == 0){
		 				continue;
		 			}
					double slope = (padeDenominator*(bestFit[1] + 2*bestFit[2]*phi) - padeNumerator*(bestFit[3] + 2*bestFit[4]*phi))/(padeDenominator*padeDenominator);
					if(slope > 0){
						skipThisIteration = true;
						break;
					}
	 			}
	 			
	 			if(skipThisIteration){
	 				continue;
	 			}
				
				bestVariance = newError;
				bestParameters = bestFit;
				
				this.updateVariance(bestVariance);
				this.updateAZero(bestFit[0]);
				this.updateAOne(bestFit[1]);
				this.updateATwo(bestFit[2]);
				this.updateBOne(bestFit[3]);
				this.updateBTwo(bestFit[4]);
				updateGraphs(bestFit[0], bestFit[1], bestFit[2], bestFit[3], bestFit[4]);
				for (int i = 0; i < programList.size(); i++)
				{
					double injectionTime = programList.get(i).getInjectionTime();
					double predictedRetTime;
					if(isInjectionMode){
						predictedRetTime = backcalculateController[0].predictRetentionFromLogKPhiRelationship(bestFit[0], bestFit[1], bestFit[2], bestFit[3], bestFit[4],injectionTime).dPredictedRetentionTime;
						
					}
					else{
						predictedRetTime = backcalculateController[i].predictRetentionFromLogKPhiRelationship(bestFit[0], bestFit[1], bestFit[2], bestFit[3], bestFit[4],injectionTime).dPredictedRetentionTime;
					}
					programList.get(i).setPredictedRetentionTime(predictedRetTime);
				}
				this.updateTable();
			}
			updateTimeElapsed(time);
		}
		return null;
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
	
    private void updateGraphs(double a0, double a1, double a2, double b1, double b2){
		retentionSolverTimeGraph.RemoveSeries(measuredRetentionTimesSeries);
		retentionSolverTimeGraph.RemoveAllSeries();
		measuredRetentionTimesSeries = retentionSolverTimeGraph.AddSeries("Measured Retention Times Series", Color.RED, 1, false, false);
		for(double i = 0.01; i < 99; i = i + 0.5){
			double padeNumerator = (a0 + a1*i + a2*i*i);
 			double padeDenominator = (1 + b1*i + b2*i*i);
 			if(padeDenominator == 0){
 				continue;
 			}
 			double logk = padeNumerator/padeDenominator;
 			if(logk < -3){
 				logk = -3;
 			}
 			
			retentionSolverTimeGraph.AddDataPoint(measuredRetentionTimesSeries, i, logk);
		}
		retentionSolverTimeGraph.AutoScaleToSeries(measuredRetentionTimesSeries);
		retentionSolverTimeGraph.autoScaleY();
		retentionSolverTimeGraph.repaint();
    }
    
	private double calcVarianceForAllPrograms(double a0, double a1, double a2, double b1, double b2)
	{
		double totalError = 0;
		int count = 0;
		for (int i = 0; i < programList.size(); i++)
		{
			if (programList.get(i).getMeasuredRetentionTime() > 0)
			{
				double injectionTime = programList.get(i).getInjectionTime();
				if(isInjectionMode){
					totalError += Math.pow(programList.get(i).getMeasuredRetentionTime() - backcalculateController[0].predictRetentionFromLogKPhiRelationship(a0, a1, a2, b1, b2, injectionTime).dPredictedRetentionTime, 2);
				}
				else totalError += Math.pow(programList.get(i).getMeasuredRetentionTime() - backcalculateController[i].predictRetentionFromLogKPhiRelationship(a0, a1, a2, b1, b2, injectionTime).dPredictedRetentionTime, 2);
				count++;
			}
		}
		return totalError / (double)count;
	}
	
    class RetentionErrorFunction implements ParametricUnivariateFunction{

		@Override
		public double value(double x, double... parameters) {
			
			double injectionTime = programList.get((int)x).getInjectionTime();
			if(isInjectionMode){
				return backcalculateController[0].predictRetentionFromLogKPhiRelationship(parameters[0], parameters[1], parameters[2], parameters[3], parameters[4], injectionTime).dPredictedRetentionTime;
			}
			return backcalculateController[(int) x].predictRetentionFromLogKPhiRelationship(parameters[0], parameters[1], parameters[2], parameters[3], parameters[4], injectionTime).dPredictedRetentionTime;
		}

		@Override
		public double[] gradient(double x, double... parameters) {
			double[] gradient = new double[5];
			double injectionTime = 0.0;
			if(isInjectionMode){
				programList.get((int)x).getInjectionTime();
				x = 0;
			}
			gradient[0] = backcalculateController[(int)x].predictRetentionFromLogKPhiRelationship(parameters[0] + 1, parameters[1], parameters[2], parameters[3], parameters[4], injectionTime).dPredictedRetentionTime - backcalculateController[(int)x].predictRetentionFromLogKPhiRelationship(parameters[0] - 1, parameters[1], parameters[2], parameters[3], parameters[4], injectionTime).dPredictedRetentionTime;
			gradient[1] = backcalculateController[(int)x].predictRetentionFromLogKPhiRelationship(parameters[0], parameters[1] + 1, parameters[2], parameters[3], parameters[4], injectionTime).dPredictedRetentionTime - backcalculateController[(int)x].predictRetentionFromLogKPhiRelationship(parameters[0], parameters[1] - 1, parameters[2], parameters[3], parameters[4], injectionTime).dPredictedRetentionTime;
			gradient[2] = backcalculateController[(int)x].predictRetentionFromLogKPhiRelationship(parameters[0], parameters[1], parameters[2] + 1, parameters[3], parameters[4], injectionTime).dPredictedRetentionTime - backcalculateController[(int)x].predictRetentionFromLogKPhiRelationship(parameters[0], parameters[1], parameters[2] - 1, parameters[3], parameters[4], injectionTime).dPredictedRetentionTime;
			gradient[3] = backcalculateController[(int)x].predictRetentionFromLogKPhiRelationship(parameters[0], parameters[1], parameters[2], parameters[3] + 1, parameters[4], injectionTime).dPredictedRetentionTime - backcalculateController[(int)x].predictRetentionFromLogKPhiRelationship(parameters[0], parameters[1], parameters[2], parameters[3] - 1, parameters[4], injectionTime).dPredictedRetentionTime;
			gradient[4] = backcalculateController[(int)x].predictRetentionFromLogKPhiRelationship(parameters[0], parameters[1], parameters[2], parameters[3], parameters[4] + 1, injectionTime).dPredictedRetentionTime - backcalculateController[(int)x].predictRetentionFromLogKPhiRelationship(parameters[0], parameters[1], parameters[2], parameters[3], parameters[4] - 1, injectionTime).dPredictedRetentionTime;
			return gradient;
		}
    	
    }
    

	public void setBackCalculateController(
			BackcalculateController[] backcalculateController) {
		this.backcalculateController = backcalculateController;
	}

	public void setProgramList(ObservableList<StandardCompound> programList) {
		this.programList = programList;
		
	}

	public SimpleStringProperty getATwoProperty() {
		return ATwoProperty;
	}

	public void setATwoProperty(SimpleStringProperty aTwoProperty) {
		ATwoProperty = aTwoProperty;
	}

	public SimpleStringProperty getAZeroProperty() {
		return AZeroProperty;
	}

	public void setAZeroProperty(SimpleStringProperty aZeroProperty) {
		AZeroProperty = aZeroProperty;
	}

	public SimpleStringProperty getAOneProperty() {
		return AOneProperty;
	}

	public void setAOneProperty(SimpleStringProperty aOneProperty) {
		AOneProperty = aOneProperty;
	}

	public SimpleStringProperty getBOneProperty() {
		return BOneProperty;
	}

	public void setBOneProperty(SimpleStringProperty bOneProperty) {
		BOneProperty = bOneProperty;
	}

	public SimpleStringProperty getBTwoProperty() {
		return BTwoProperty;
	}

	public void setBTwoProperty(SimpleStringProperty bTwoProperty) {
		BTwoProperty = bTwoProperty;
	}

	private void updateAZero(final double aZero)
	{
		if (!updateAZeroReady)
			return;
		
		final String str;
		str = Float.toString((float)Globals.roundToSignificantFigures(aZero, 6));
		
		updateAZeroReady = false;
		
		Platform.runLater(new Runnable(){
            @Override
            public void run() {
            	AZeroProperty.set(str);
        		updateAZeroReady = true;
            }
        });	
	}

	private void updateAOne(final double aOne)
	{
		if (!updateAOneReady)
			return;
		
		final String str;
		str = Float.toString((float)Globals.roundToSignificantFigures(aOne, 6));
		
		updateAOneReady = false;
		
		Platform.runLater(new Runnable(){
            @Override
            public void run() {
            	AOneProperty.set(str);
        		updateAOneReady = true;
            }
        });	
	}
	
	private void updateATwo(final double aTwo)
	{
		if (!updateATwoReady)
			return;
		
		final String str;
		str = Float.toString((float)Globals.roundToSignificantFigures(aTwo, 6));
		
		updateATwoReady = false;
		
		Platform.runLater(new Runnable(){
            @Override
            public void run() {
            	ATwoProperty.set(str);
        		updateATwoReady = true;
            }
        });	
	}
	
	private void updateBOne(final double bOne)
	{
		if (!updateBOneReady)
			return;
		
		final String str;
		str = Float.toString((float)Globals.roundToSignificantFigures(bOne, 6));
		
		updateBOneReady = false;
		
		Platform.runLater(new Runnable(){
            @Override
            public void run() {
            	BOneProperty.set(str);
        		updateBOneReady = true;
            }
        });	
	}
	
	private void updateBTwo(final double bTwo)
	{
		if (!updateBTwoReady)
			return;
		
		final String str;
		str = Float.toString((float)Globals.roundToSignificantFigures(bTwo, 6));
		
		updateBTwoReady = false;
		
		Platform.runLater(new Runnable(){
            @Override
            public void run() {
            	BTwoProperty.set(str);
        		updateBTwoReady = true;
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

	private void updateIteration(final String iteration)
	{
		if (!updateIterationReady)
			return;

		updateIterationReady = false;
		Platform.runLater(new Runnable(){
            @Override
            public void run() {
            	iterationProperty.set(iteration);
            	updateIterationReady = true;
            }
        });
	}

	public SimpleStringProperty getIterationProperty() {
		return iterationProperty;
	}

	public void setIterationProperty(SimpleStringProperty iterationProperty) {
		this.iterationProperty = iterationProperty;
	}

	public SimpleStringProperty getVarianceProperty() {
		return varianceProperty;
	}

	public void setVarianceProperty(SimpleStringProperty varianceProperty) {
		this.varianceProperty = varianceProperty;
	}

	public SimpleStringProperty getTimeElapsedProperty() {
		return timeElapsedProperty;
	}

	public void setTimeElapsedProperty(SimpleStringProperty timeElapsedProperty) {
		this.timeElapsedProperty = timeElapsedProperty;
	}

	private void updateTable()
	{
		if (!updateTableReady)
			return;

		// Make a deep copy of the standardsList
		final ObservableList<StandardCompound> currentStandardsList = FXCollections.observableArrayList();
		for (int i = 0; i < programList.size(); i++)
		{
			StandardCompound newStandardCompound = new StandardCompound(programList.get(i).getUse(), programList.get(i).getName(), programList.get(i).getMz(), programList.get(i).getMeasuredRetentionTime(), programList.get(i).getPredictedRetentionTime(), programList.get(i).getIndex());
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

	public void setGraphControl(GraphControlFX retentionSolverTimeGraph) {
		this.retentionSolverTimeGraph = retentionSolverTimeGraph;
		
	}

	public boolean isInjectionMode() {
		return isInjectionMode;
	}

	public void setInjectionMode(boolean isInjectionMode) {
		this.isInjectionMode = isInjectionMode;
	}
	
}
