package client;


import drawpad.OpenDrawPad;
import java.util.*;

/**
 * Used to launch and handle updates pertaining to the draw pad class.
 */

public class ChatDrawPad extends Observable implements Observer
{
	public void update(Observable obs, Object obj)
	{
		if (!(obj instanceof String))
			return;

		String msg = (String)obj;
		if (msg.startsWith("#send"))
		{
			setChanged();
			notifyObservers(msg.substring(6));
		}
	}

	public void updateDraw(String command){
		setChanged();
		notifyObservers(command);
	}
}