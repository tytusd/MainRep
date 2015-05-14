package org.retentionprediction.lcdatabasebuilderfx.business;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

	public class GradientProgramStep{
    	
		private DoubleProperty time;
    	private DoubleProperty solventComposition;
    	public GradientProgramStep(double time, double solventComposition) {
			this.time = new SimpleDoubleProperty(time);
			this.solventComposition = new SimpleDoubleProperty(solventComposition);
		}
    	
    	public double getTime(){
    		return time.get();
    	}
    	
    	public void setTime(double time){
    		this.time.set(time);
    	}
    	
    	public DoubleProperty timeProperty(){
    		return time;
    	}
    	
    	public double getSolventComposition(){
    		return solventComposition.get();
    	}
    	
    	public void setSolventComposition(double solventComposition){
    		this.solventComposition.set(solventComposition);
    	}
    	
    	public DoubleProperty solventCompositionProperty(){
    		return solventComposition;
    	}
    }