package org.retentionprediction.lcdatabasebuilderfx.ui;


import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;

import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.fitting.CurveFitter;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.PointVectorValuePair;
import org.apache.commons.math3.optim.nonlinear.vector.jacobian.LevenbergMarquardtOptimizer;
import org.retentionprediction.lcdatabasebuilderfx.business.Globals;
import org.retentionprediction.lcdatabasebuilderfx.business.StandardCompound;

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
	private DoubleProperty progressBarProperty = new SimpleDoubleProperty(-1.0);
	
	private Button copyToClipboard;
	
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
	private double instrumentDeadTime = 0;
	
	private double[] measuredRetentionTimes;
	
	private boolean isInjectionMode = false;
	private boolean isSolverDone = false;
	
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
		
		int iteration = 0, maxIterations = 5000, noChangeCount = 0;
		bestVariance = 100;
		
		Random[] rand = new Random[5];
		for(int i = 0; i < 5; i++){
			rand[i] = new Random();
		}
		
		measuredRetentionTimes = new double[programList.size()];
		for(int i = 0; i < programList.size(); i++){
			measuredRetentionTimes[i]  = programList.get(i).getMeasuredRetentionTime() - instrumentDeadTime;
		}
		
		if(isInjectionMode){
			maxIterations = 1000;
		}
		
		while(iteration < maxIterations){
			double[] params = new double[rand.length];
			for(int i = 0; i < params.length; i++){
				params[i] = 10 - 20*rand[i].nextDouble();
			}
			
			boolean isGoodGuess = false;
			
			ArrayList<Double> predRetTime = new ArrayList<Double>(8);
			for (int i = 0; i < programList.size(); i++)
			{
				double injectionTime = programList.get(i).getInjectionTime();
				double predictedRetTime;
				if(isInjectionMode){
					predictedRetTime = backcalculateController[0].predictRetentionFromLogKPhiRelationship(params[0], params[1], params[2], params[3], params[4],injectionTime).dPredictedRetentionTime;
				}
				else{
					predictedRetTime = backcalculateController[i].predictRetentionFromLogKPhiRelationship(params[0], params[1], params[2], params[3], params[4],injectionTime).dPredictedRetentionTime;
				}
				predRetTime.add(measuredRetentionTimes[i] - predictedRetTime);
			}
			
			double signum = Math.signum(predRetTime.get(0));
			for(int k = 1; k < predRetTime.size(); k++){
				if(Math.signum(predRetTime.get(k)) != signum){
					isGoodGuess = true;
					break;
				}
			}
			
			if(!isGoodGuess){
				continue;
			}
			
			iteration++;
			updateIteration(iteration+"");
			
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
						if (measuredRetentionTimes[i] > 0)
						{
							previousError += Math.pow(measuredRetentionTimes[i] - previous.getValue()[pos], 2);
							currentError += Math.pow(measuredRetentionTimes[i] - current.getValue()[pos], 2);
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
				if (measuredRetentionTimes[i] > 0)
					fitter.addObservedPoint(i, measuredRetentionTimes[i]);
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
				noChangeCount = 0;
				this.updateTable();
			}
			else
			{
				noChangeCount++;
			}
			
			if(noChangeCount == 200){
				break;
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
    		Platform.runLater(new Runnable(){

				@Override
				public void run() {
					progressBarProperty.set(1);
				}
    			
    		});
    		updateProgress(100, 100);
        	updateMessage("Optimization complete!");
        	isSolverDone = true;
    	}
    	copyToClipboard.setDisable(false);
    }
	
    public void updateGraphs(double a0, double a1, double a2, double b1, double b2){
		retentionSolverTimeGraph.RemoveSeries(measuredRetentionTimesSeries);
		retentionSolverTimeGraph.RemoveAllSeries();
		measuredRetentionTimesSeries = retentionSolverTimeGraph.AddSeries("Measured Retention Times Series", Color.RED, 1, false, false);
		for(double i = 0; i < 1.0; i = i + 0.01){
			double padeNumerator = (a0 + a1*i + a2*i*i);
 			double padeDenominator = (1 + b1*i + b2*i*i);
 			if(padeDenominator == 0){
 				continue;
 			}
 			double logk = padeNumerator/padeDenominator;
 			
 			
			retentionSolverTimeGraph.AddDataPoint(measuredRetentionTimesSeries, i*100.0, logk);
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
			if (measuredRetentionTimes[i] > 0)
			{
				double injectionTime = programList.get(i).getInjectionTime();
				if(isInjectionMode){
					totalError += Math.pow(measuredRetentionTimes[i] - backcalculateController[0].predictRetentionFromLogKPhiRelationship(a0, a1, a2, b1, b2, injectionTime).dPredictedRetentionTime, 2);
				}
				else totalError += Math.pow(measuredRetentionTimes[i] - backcalculateController[i].predictRetentionFromLogKPhiRelationship(a0, a1, a2, b1, b2, injectionTime).dPredictedRetentionTime, 2);
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
				injectionTime = programList.get((int)x).getInjectionTime();
				x = 0;
			}
			double[] values = new double[10];
			values[0] = backcalculateController[(int)x].predictRetentionFromLogKPhiRelationship(parameters[0] + 0.001, parameters[1], parameters[2], parameters[3], parameters[4], injectionTime).dPredictedRetentionTime; 
			values[1] = backcalculateController[(int)x].predictRetentionFromLogKPhiRelationship(parameters[0] - 0.001, parameters[1], parameters[2], parameters[3], parameters[4], injectionTime).dPredictedRetentionTime;
			values[2] =  backcalculateController[(int)x].predictRetentionFromLogKPhiRelationship(parameters[0], parameters[1] + 0.001, parameters[2], parameters[3], parameters[4], injectionTime).dPredictedRetentionTime; 
			values[3] = backcalculateController[(int)x].predictRetentionFromLogKPhiRelationship(parameters[0], parameters[1] - 0.001, parameters[2], parameters[3], parameters[4], injectionTime).dPredictedRetentionTime;
			values[4] = backcalculateController[(int)x].predictRetentionFromLogKPhiRelationship(parameters[0], parameters[1], parameters[2] + 0.001, parameters[3], parameters[4], injectionTime).dPredictedRetentionTime;
			values[5] = backcalculateController[(int)x].predictRetentionFromLogKPhiRelationship(parameters[0], parameters[1], parameters[2] - 0.001, parameters[3], parameters[4], injectionTime).dPredictedRetentionTime;
			values[6] = backcalculateController[(int)x].predictRetentionFromLogKPhiRelationship(parameters[0], parameters[1], parameters[2], parameters[3] + 0.001, parameters[4], injectionTime).dPredictedRetentionTime; 
			values[7] = backcalculateController[(int)x].predictRetentionFromLogKPhiRelationship(parameters[0], parameters[1], parameters[2], parameters[3] - 0.001, parameters[4], injectionTime).dPredictedRetentionTime;
			values[8] = backcalculateController[(int)x].predictRetentionFromLogKPhiRelationship(parameters[0], parameters[1], parameters[2], parameters[3], parameters[4] + 0.001, injectionTime).dPredictedRetentionTime;
			values[9] = backcalculateController[(int)x].predictRetentionFromLogKPhiRelationship(parameters[0], parameters[1], parameters[2], parameters[3], parameters[4] - 0.001, injectionTime).dPredictedRetentionTime;;
			
			for(int i = 0; i < values.length; i++){
				if(values[i] == -1){
					values[i] = 999; 
				}
			}
			
			gradient[0] = values[0] - values[1];
			gradient[1] = values[2] - values[3];
			gradient[2] = values[4] - values[5];
			gradient[3] = values[6] - values[7];
			gradient[4] = values[8] - values[9];
			for(int i = 0; i < gradient.length; i++){
				gradient[i] *= 2000;
			}
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
	
	public void setProgressBarProperty(DoubleProperty property){
		this.progressBarProperty = property;
	}
	
	public DoubleProperty getProgressBarProperty(){
		return this.progressBarProperty;
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
			StandardCompound newStandardCompound = new StandardCompound(programList.get(i).getUse(), programList.get(i).getName(), programList.get(i).getMz(), programList.get(i).getMeasuredRetentionTime(), programList.get(i).getPredictedRetentionTime()+instrumentDeadTime, programList.get(i).getIndex());
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

	public double getInstrumentDeadTime() {
		return instrumentDeadTime;
	}

	public void setInstrumentDeadTime(double instrumentDeadTime) {
		this.instrumentDeadTime = instrumentDeadTime;
	}

	public String copyProfileToClipboard() {
		String outString = "";
		String eol = System.getProperty("line.separator");
		outString += "Pade's approximant coefficients for log K vs Phi relationship:" + eol;
		outString += "a0:\t"+ getAZeroProperty().get() + eol;
		outString += "a1:\t"+ getAOneProperty().get() + eol;
		outString += "a2:\t"+ getATwoProperty().get() + eol;
		outString += "b1:\t"+ getBOneProperty().get() + eol;
		outString += "b2:\t"+ getBTwoProperty().get() + eol;
		outString += "Variance:\t"+ getVarianceProperty().get() + eol;
		outString += "Time Elapsed:\t"+ getTimeElapsedProperty().get() + eol;
		
		if(isInjectionMode){
			outString += "Injection #\t Experimental Retention Time(min)\t Calculated Retention Time(min)\t Error(min)" + eol;
			for(StandardCompound s : programList){
				outString += s.getIndex() + "\t" + s.getMeasuredRetentionTime() + "\t" + s.getPredictedRetentionTime() + "\t" + s.getError() + eol;
			}
		}
		else{
			outString += "Gradient\t Experimental Retention Time(min)\t Calculated Retention Time(min)\t Error(min)" + eol;
			for(StandardCompound s : programList){
				char gradientName = (char)(s.getIndex() + 65);
				outString += "Gradient "+ gradientName + ":\t" + s.getMeasuredRetentionTime() + "\t" + s.getPredictedRetentionTime() + "\t" + s.getError() + eol;
			}
		}
		
		return outString;
	}

	public Map<String,String> exportToXml() {
		Map<String,String> map = new HashMap<String,String>();
		map.put("a0",getAZeroProperty().get());
		map.put("a1", getAOneProperty().get());
		map.put("a2", getATwoProperty().get());
		map.put("b1",getBOneProperty().get());
		map.put("b2", getBTwoProperty().get());
		map.put("Variance", getVarianceProperty().get());
		map.put("TimeElapsed", getTimeElapsedProperty().get());
		
		if(isInjectionMode){
			StringBuilder injectionTime = new StringBuilder();
			StringBuilder expRetTime = new StringBuilder();
			StringBuilder calcRetTime = new StringBuilder();
			StringBuilder error = new StringBuilder();
			
			for(StandardCompound s : programList){
				injectionTime.append(s.getInjectionTime()+"$");
				expRetTime.append(s.getMeasuredRetentionTime()+"$");
				calcRetTime.append(s.getPredictedRetentionTime()+"$");
				error.append(s.getError()+"$");
			}
			injectionTime.setLength(injectionTime.length()-1);
			expRetTime.setLength(expRetTime.length()-1);
			calcRetTime.setLength(calcRetTime.length()-1);
			error.setLength(error.length()-1);
			map.put("InjectionTimes", injectionTime.toString());
			map.put("ExperimentalRetentionTimes", expRetTime.toString());
			map.put("CalculatedRetentionTimes", calcRetTime.toString());
			map.put("Errors", error.toString());
		}
		else{
			StringBuilder gradients = new StringBuilder();
			StringBuilder expRetTime = new StringBuilder();
			StringBuilder calcRetTime = new StringBuilder();
			StringBuilder error = new StringBuilder();
			
			for(StandardCompound s : programList){
				char gradientName = (char)(s.getIndex() + 65);
				String gradient = "Gradient"+gradientName;
				gradients.append(gradient+"$");
				expRetTime.append(s.getMeasuredRetentionTime()+"$");
				calcRetTime.append(s.getPredictedRetentionTime()+"$");
				error.append(s.getError()+"$");
			}
			gradients.setLength(gradients.length()-1);
			expRetTime.setLength(expRetTime.length()-1);
			calcRetTime.setLength(calcRetTime.length()-1);
			error.setLength(error.length()-1);
			map.put("GradientNames", gradients.toString());
			map.put("ExperimentalRetentionTimes", expRetTime.toString());
			map.put("CalculatedRetentionTimes", calcRetTime.toString());
			map.put("Errors", error.toString());
			
		}
		
		return map;
	}
	
	public void setCopyToClipboardButton(Button copyToClipboard) {
		this.copyToClipboard = copyToClipboard;
	}

	public boolean isSolverDone() {
		return isSolverDone;
	}

	public void setSolverDone(boolean isSolverDone) {
		this.isSolverDone = isSolverDone;
	}

}
