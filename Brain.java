//
//	File:			Brain.java
//	Author:		Krzysztof Langner
//	Date:			1997/04/28
//
//    Modified by:	Paul Marlow

//    Modified by:      Edgar Acosta
//    Date:             March 4, 2008

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.lang.Math;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;
import java.util.Scanner;

class Brain extends Thread implements SensorInput
{
	//---------------------------------------------------------------------------
	// This constructor:
	// - stores connection to krislet
	// - starts thread for this object
	public Brain(SendCommand krislet, 
			String team, 
			char side, 
			int number, 
			String playMode)
	{
		m_timeOver = false;
		m_krislet = krislet;
		m_memory = new Memory();
		//m_team = team;
		m_side = side;
		m_number = number;
		m_playMode = playMode;
		start();
	}
	
	public void run()
	{
		ObjectInfo ball;						
		String kick_bit = "1";					// initialize kick_bit
		String[] logfiles = {"log_1_l.txt","log_2_l.txt","log_3_l.txt","log_4_l.txt","log_5_l.txt","log_1_r.txt","log_2_r.txt","log_3_r.txt","log_4_r.txt","log_5_r.txt"};
		
		//first place all the players at random positions 
		//Also make all the logfiles empty so that only values of new game can be stored
		if(Pattern.matches("^before_kick_off.*",m_playMode)) {
			m_krislet.move( -Math.random()*52.5 , 34 - Math.random()*68.0 );
			int iterator;
			try {
				for(iterator=0;iterator<10;iterator++) {
					File file = new File(logfiles[iterator]);
					if (file.exists()) {
					    RandomAccessFile raf = new RandomAccessFile(file, "rw");
					    raf.setLength(0);
					}
				}	
			}catch(Exception e) {System.out.println(e);}
		}
		while( !m_timeOver )
		{
			String combination;
			String action;
			String B;
			String D;

			ball =  m_memory.getObject("ball");				//get ball object
			
			if( ball == null ) {
				B = "0"; D = "0";
			}
			else{
				B = "1";
				if(ball.m_distance > 1.0) 
					D = "1";
				else 
					D = "0";
			}

			combination = B+D;
			action = findRuleGetAction(combination, kick_bit);		//find action corresponding the given combination and kick_bit from the text file

			//perform actions

			if(Pattern.matches("^waitturn.*",action)) {
				String temp[] = action.split(":");
				kick_bit = temp[1];
				String raw_arg = temp[0].substring(temp[0].indexOf("(")+1,temp[0].length()-1);
				Double arg = Double.valueOf(raw_arg);
				m_krislet.turn(arg);
				m_memory.waitForNewInfo();
			}
			if(Pattern.matches("^kick.*",action)) {
				String temp[] = action.split(":");
				kick_bit = temp[1];				
				String raw_arg = temp[0].substring(temp[0].indexOf("(")+1,temp[0].length()-1);
				String acttemp[] = raw_arg.split(",");						
				Double arg1 = Double.valueOf(acttemp[0]);								
				Double arg2 = null ;
				if (acttemp[1].equals("direction"))
			   		arg2 = (double) ball.m_direction;
			   	else
			   		arg2 = Double.valueOf(acttemp[1]);
				m_krislet.kick(arg1, arg2);
			}
			if(Pattern.matches("^justdash.*",action)) {
				String temp[] = action.split(":");
				kick_bit = temp[1];
				String raw_arg = temp[0].substring(temp[0].indexOf("(")+1,temp[0].length()-1);
				Double arg = null ;
				if (raw_arg.equals("distance"))
					arg = (double) ball.m_distance;
			   	else
			   		arg = Double.valueOf(raw_arg);
				m_krislet.dash(10*arg);	    		
			}

			if(Pattern.matches("^turnordash.*",action)) {
				String temp[] = action.split(":");
				kick_bit = temp[1];
				String raw_arg = temp[0].substring(temp[0].indexOf("(")+1,temp[0].length()-1);
				String acttemp[] = raw_arg.split(",");						
				Double arg1 = null;								
				Double arg2 = null;
				if (acttemp[0].equals("distance"))
					arg1 = (double) ball.m_distance;
			   	else
			   		arg1 = Double.valueOf(acttemp[0]);
				if (acttemp[1].equals("direction"))
					arg2 = (double) ball.m_direction;
			   	else
			   		arg2 = Double.valueOf(acttemp[1]);
				if( ball.m_direction != 0 )
					m_krislet.turn(arg2);
				else
					m_krislet.dash(10*arg1);			    			
			}
			if(action  == null) 
				m_krislet.dash(10*ball.m_distance); 
			
			// sleep one step to ensure that we will not send
			// two commands in one cycle.
			try{
				Thread.sleep(2*SoccerParams.simulator_step);
			}catch(Exception e){
				System.out.println(e);}
		}
		m_krislet.bye();
	}

	//FIND RULES FROM TXT FILE

	public String findRuleGetAction(String combination, String K) {
		File file = new File("question2.txt");
		String action = null;
		String line;
		try {
			Scanner input = new Scanner(file);			
			while (input.hasNextLine()) {
				line = input.nextLine().toLowerCase();		//made the strings in lower case for case insensitive comparison
				if(line.contains(combination.toLowerCase()+":"+K)) {
					String parts[] = line.split("-");
					action = parts[1];
					//write logs in respective file
					String logfile = "log_"+String.valueOf(m_number)+"_"+m_side+".txt";
					try(FileWriter fw = new FileWriter(logfile, true);
						    BufferedWriter bw = new BufferedWriter(fw);
						    PrintWriter out = new PrintWriter(bw))
						{
							out.println(line);						    
						} catch (IOException e) {
						   System.out.println(e);
						}
					break;
				}		    
			}
		} catch (FileNotFoundException e) {e.printStackTrace();}	
		return action;
	}



	//===========================================================================
	// Here are suporting functions for implement logic


	//===========================================================================
	// Implementation of SensorInput Interface

	//---------------------------------------------------------------------------
	// This function sends see information
	public void see(VisualInfo info)
	{
		m_memory.store(info);
	}


	//---------------------------------------------------------------------------
	// This function receives hear information from player
	public void hear(int time, int direction, String message)
	{
	}

	//---------------------------------------------------------------------------
	// This function receives hear information from referee
	public void hear(int time, String message)
	{						 
		if(message.compareTo("time_over") == 0)
			m_timeOver = true;

	}


	//===========================================================================
	// Private members
	private SendCommand	    m_krislet;			// robot which is controled by this brain
	private Memory			m_memory;				// place where all information is stored
	private char			m_side;
	volatile private boolean m_timeOver;
	private String   m_playMode;
	private int m_number;

}
