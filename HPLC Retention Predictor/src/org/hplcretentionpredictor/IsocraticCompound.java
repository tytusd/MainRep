package org.hplcretentionpredictor;
import java.util.ArrayList;


public class IsocraticCompound {

	private String id;
	private ArrayList<Double> logKList;
	private ArrayList<Double> concentrationList;
	
	public IsocraticCompound() {
		logKList = new ArrayList<Double>();
		concentrationList = new ArrayList<Double>();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ArrayList<Double> getLogKList() {
		return logKList;
	}

	public ArrayList<Double> getConcentrationList() {
		return concentrationList;
	}
	
	/**
	 * Get the value stored at index of logK arraylist.
	 * @param index
	 * @return value
	 */
	public double getlogKValue(int index){
		return getLogKList().get(index);
	}
	
	/**
	 * Set the value at index of logK arraylist.
	 * @param index
	 * @param value
	 * @return
	 */
	public double setlogKValue(int index, double value){
		return getLogKList().set(index, value);
	}
	
	
	/**
	 * Get the value stored at index of concentration arraylist.
	 * @param index
	 * @return
	 */
	public double getConcentrationValue(int index){
		return getConcentrationList().get(index);
	}
	
	/**
	 * Set the value at index of logK arraylist.
	 * @param index
	 * @param value
	 * @return
	 */
	public double setConcentrationValue(int index, double value){
		return getConcentrationList().set(index, value);
	}
	
	/**
	 * Converts a compound's log-k and solvent fraction lists into a 2-D array. 
	 * @param compound
	 * @return
	 */
	public double[][] convertListsTo2DArray(){
		int size = this.getConcentrationList().size();
		double[][] array = new double[size][2];
		for(int i = 0; i < size; i++){
			array[i][0] = this.getConcentrationValue(i);
			array[i][1] = this.getlogKValue(i);
		}
		return array;
	}
	
}
