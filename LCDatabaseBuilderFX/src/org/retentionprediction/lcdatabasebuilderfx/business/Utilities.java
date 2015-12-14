package org.retentionprediction.lcdatabasebuilderfx.business;

import javafx.collections.ObservableList;

import org.retentionprediction.lcdatabasebuilderfx.ui.BackcalculateController;

public class Utilities {

	public static String getInterpolatedSimpleGradient(BackcalculateController controller){
		LinearInterpolationFunction gradientFunction = controller.interpolatedSimpleGradient;
		LinearInterpolationFunction gradientDifferenceFunction = controller.interpolatedGradientDifferenceProfile;
		
		StringBuilder sb = new StringBuilder("");
		ObservableList<StandardCompound> list = controller.getStandardsList();
		double max = list.get(list.size()-1).getMeasuredRetentionTime() * 1.5;
		double step = max / 1000;
		
		for(double t = 0; t < max; t += step){
			double value = (gradientFunction.getAt(t) + gradientDifferenceFunction.getAt(t))/100;
			sb.append("("+t+","+value+")" + ",");
		}
		sb.setLength(sb.length()-1);
		return sb.toString();
	}
	
	public static String getInterpolatedDeadTime(BackcalculateController controller){
		InterpolationFunction deadTimeFunction = controller.initialInterpolatedDeadTimeProfile;
		InterpolationFunction deadTimeDifferenceFunction = controller.interpolatedDeadTimeDifferenceProfile;
		
		StringBuilder sb = new StringBuilder("");
		ObservableList<StandardCompound> list = controller.getStandardsList();
		double max = list.get(list.size()-1).getMeasuredRetentionTime() * 1.5;
		double step = max / 1000;
		
		for(double t = 0; t < max; t += step){
			double value = (deadTimeFunction.getAt(t) + deadTimeDifferenceFunction.getAt(t))/60;
			sb.append("("+t+","+value+")" + ",");
		}
		sb.setLength(sb.length()-1);
		return sb.toString();
	}
}
