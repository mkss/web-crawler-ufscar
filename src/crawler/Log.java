package crawler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {

	private String filename;
	private File f;
	private FileWriter fw;
	private BufferedWriter bw;
	private Date date;
	private DateFormat dateformat;

	public Log(){
		
		date = new Date();
		dateformat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
		filename = dateformat.format(date) + ".txt";
		f = new File (filename);
		//f = new File ("debug1");
		try {
			fw = new FileWriter(f);
			bw = new BufferedWriter(fw);
		} catch (Exception e){
			this.printtrace(e);
			e.printStackTrace();
		}
	
		
	}
	
	public String getname(){
		return filename;
	}

	public void write(String string, int lines){
		int i;
		try {
		for (i=0;i<lines;i++){
			bw.newLine();
		}
		bw.write(string);
		}catch (Exception e){
			this.printtrace(e);
			e.printStackTrace();
		}
	}
	
	public void finish(){
		try{
			bw.close();
		} catch (Exception e){
			this.printtrace(e);
			e.printStackTrace();
		}
		
	}
	
	public void printtrace(Exception ex){
		
		StackTraceElement[] traces = ex.getStackTrace();
		String string = ex.getMessage();
		int i;
		try {
			bw.newLine();
			bw.newLine();
			bw.write("Error Found! " + string);
			bw.newLine();
			for (i=0;i<traces.length;i++){
				bw.newLine();
				bw.write(traces[i].toString());
			}
			
		} catch (Exception e){
			this.printtrace(e);
			e.printStackTrace();
		}
		
		
		
	}
	
	
}


