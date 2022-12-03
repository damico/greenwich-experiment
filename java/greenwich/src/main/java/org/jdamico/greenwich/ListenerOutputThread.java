package org.jdamico.greenwich;

public class ListenerOutputThread extends Thread {
	
	public void run() {
		TimeSpaceRuntime.readUntil();
	}

}
