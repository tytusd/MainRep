package visualizer;

import java.util.EventObject;

public class DocumentChangedEvent extends EventObject
{
	private static final long serialVersionUID = 1L;

	public DocumentChangedEvent(Object source) 
	{
		super(source);
	}
}
