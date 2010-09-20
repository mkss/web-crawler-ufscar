package crawler;

import java.util.concurrent.*;

public class FilaIC extends ConcurrentLinkedQueue<String>{
	
	//public static final long serialVersionUID = 0;
	private int counter;

	public int getCounter(){
		return counter;
	}
	
	public FilaIC(){
		super ();
	}
	
	
}
