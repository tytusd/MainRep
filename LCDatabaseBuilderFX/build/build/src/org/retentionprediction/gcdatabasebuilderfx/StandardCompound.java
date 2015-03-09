package org.retentionprediction.gcdatabasebuilderfx;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableStringValue;

public class StandardCompound implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private static final long classVersion = 1;
	
	private BooleanProperty use;
	private StringProperty name;
	private StringProperty mz;
	private DoubleProperty measuredRetentionTime;
	private DoubleProperty predictedRetentionTime;
	private DoubleProperty error;
	private IntegerProperty index;
	
	// This string binding produces the correct value for the predicted retention time
	private StringBinding predictedRetentionTimeStringBinding;
	
	// This string binding produces the correct value for the predicted retention time
	private StringBinding measuredRetentionTimeStringBinding;
	
	// This string binding produces the correct value for the predicted retention time
	private StringBinding errorStringBinding;
	
	StandardCompound()
	{
		init(false, "default", "0", 0.0, 0.0, -1);
	}

	StandardCompound(boolean use, String name, String mz, double measuredRetentionTime, double predictedRetentionTime, int index)
	{
		init(use, name, mz, measuredRetentionTime, predictedRetentionTime, index);
	}
	
	private void init(boolean use, String name, String mz, double measuredRetentionTime, double predictedRetentionTime, int index)
	{
		this.use = new SimpleBooleanProperty();
		this.name = new SimpleStringProperty();
		this.mz = new SimpleStringProperty();
		this.measuredRetentionTime = new SimpleDoubleProperty();
		this.predictedRetentionTime = new SimpleDoubleProperty();
		this.error = new SimpleDoubleProperty();
		this.index = new SimpleIntegerProperty();

		this.use.set(use);
		this.name.set(name);
		this.mz.set(mz);
		this.measuredRetentionTime.set(measuredRetentionTime);
		this.predictedRetentionTime.set(predictedRetentionTime);
		this.error.bind(this.measuredRetentionTime.subtract(this.predictedRetentionTime));
		this.index.set(index);
		
		// This string binding produces the correct value for the predicted retention time
		this.predictedRetentionTimeStringBinding = new StringBinding(){
			@Override
			protected String computeValue() 
			{
				super.bind(StandardCompound.this.predictedRetentionTime);
				
				double value = StandardCompound.this.predictedRetentionTime.get();
				if (value < 0)
				{
					return "Did not elute";
				}
				else if (value == 0)
				{
					return "";
				}
				
				NumberFormat formatter = new DecimalFormat("#0.0000");
				String retVal = formatter.format(value);
				return retVal;
			}
		};
		
		// This string binding produces the correct value for the predicted retention time
		this.measuredRetentionTimeStringBinding = new StringBinding(){
			@Override
			protected String computeValue() 
			{
				super.bind(StandardCompound.this.measuredRetentionTime);
				
				double value = StandardCompound.this.measuredRetentionTime.get();
				if (value <= 0)
				{
					return "";
				}
				
				NumberFormat formatter = new DecimalFormat("#0.0000");
				String retVal = formatter.format(value);
				return retVal;
			}
		};
		
		// This string binding produces the correct value for the predicted retention time
		this.errorStringBinding = new StringBinding(){
			@Override
			protected String computeValue() 
			{
				super.bind(StandardCompound.this.measuredRetentionTime, StandardCompound.this.predictedRetentionTime);
				
				if (StandardCompound.this.predictedRetentionTime.get() <= 0 || StandardCompound.this.measuredRetentionTime.get() <= 0)
				{
					return "";
				}
							
				double value = error.get();
				NumberFormat formatter = new DecimalFormat("#0.0000");
				String retVal = formatter.format(value);
				return retVal;
			}
		};
	}
	
	public void makeEqualTo(StandardCompound desiredCompound)
	{
		this.setUse(desiredCompound.getUse());
		this.setName(desiredCompound.getName());
		this.setMz(desiredCompound.getMz());
		this.setMeasuredRetentionTime(desiredCompound.getMeasuredRetentionTime());
		this.setPredictedRetentionTime(desiredCompound.getPredictedRetentionTime());
		this.setIndex(desiredCompound.getIndex());
	}
	
	public String getMeasuredRetentionTimeString()
	{
		return measuredRetentionTimeStringBinding.get();
	}
	
	public StringProperty measuredRetentionTimeStringProperty()
	{
		StringProperty stringProperty = new SimpleStringProperty();
		stringProperty.bind(measuredRetentionTimeStringBinding);
		return stringProperty;
	}
	
	public void setMeasuredRetentionTimeString(String str)
	{
		
	}
	
	public String getPredictedRetentionTimeStringBinding()
	{
		return predictedRetentionTimeStringBinding.get();
	}
	
	public ObservableStringValue predictedRetentionTimeStringBindingObservableValue()
	{
		return (ObservableStringValue)predictedRetentionTimeStringBinding;
	}
	
	public String getErrorStringBinding()
	{
		return errorStringBinding.get();
	}
	
	public ObservableStringValue errorStringBindingObservableValue()
	{
		return (ObservableStringValue)errorStringBinding;
	}

	public int getIndex()
	{
		return index.get();
	}
	
	public void setIndex(int index)
	{
		this.index.set(index);
	}
	
	public IntegerProperty indexProperty()
	{
		return index;
	}
	
	public boolean getUse()
	{
		return use.get();
	}
	
	public void setUse(boolean use)
	{
		this.use.set(use);
	}
	
	public BooleanProperty useProperty() 
	{
	    return use;
	}
	
	public String getName()
	{
		return name.get();
	}
	
	public void setName(String name)
	{
		this.name.set(name);
	}

	public StringProperty nameProperty() 
	{
	    return name;
	}

	public String getMz()
	{
		return mz.get();
	}
	
	public void setMz(String mz)
	{
		this.mz.set(mz);
	}

	public StringProperty mzProperty() 
	{
	    return mz;
	}

	public double getMeasuredRetentionTime()
	{
		return measuredRetentionTime.get();
	}
	
	public void setMeasuredRetentionTime(double measuredRetentionTime)
	{
		this.measuredRetentionTime.set(measuredRetentionTime);
	}
	
	public DoubleProperty measuredRetentionTimeProperty() 
	{
	    return measuredRetentionTime;
	}

	public double getPredictedRetentionTime()
	{
		return predictedRetentionTime.get();
	}
	
	public void setPredictedRetentionTime(double predictedRetentionTime)
	{
		this.predictedRetentionTime.set(predictedRetentionTime);
	}
	
	public DoubleProperty predictedRetentionTimeProperty() 
	{
	    return predictedRetentionTime;
	}
	
	public double getError()
	{
		return error.get();
	}
	
	public DoubleProperty errorProperty()
	{
		return error;
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException
	{
		out.writeLong(this.classVersion);
		
		out.writeBoolean(this.use.getValue());
		out.writeObject(this.name.getValue());
		out.writeObject(this.mz.getValue());
		out.writeDouble(this.measuredRetentionTime.getValue());
		out.writeDouble(this.predictedRetentionTime.getValue());
		out.writeInt(this.index.getValue());
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		long lVersion = in.readLong();
		
		if (lVersion >= 1)
		{
			init(in.readBoolean(), (String)in.readObject(), (String)in.readObject(), in.readDouble(), in.readDouble(), in.readInt());
		}
	}
}