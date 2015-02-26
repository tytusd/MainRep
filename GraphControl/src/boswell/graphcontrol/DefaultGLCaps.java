package boswell.graphcontrol;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLException;
import javax.media.opengl.GLProfile;

public class DefaultGLCaps extends GLCapabilities
{

	public DefaultGLCaps(GLProfile glp) throws GLException {
		super(glp);
		
	    this.setDoubleBuffered(true);
	    this.setHardwareAccelerated(true);
	}
	
}