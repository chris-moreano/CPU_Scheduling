
/*
 * Prasamsha Pradhan
 * Christin Moreano
 * CS 149 Section 4
 * Spring 2015
 */

package SAMCPU;

 import java.io.*;
 import java.util.*;

 
public class CPUScheduleObj {
    
	public static String OUTPUT_FILE_NAME = "CPUScheduleOutput.out";
	
   private Job[] listjob;//store the job enter the system
	private int numberOfJobs;//number of the jobs enter the system

	private ArrayList<Integer> queue;
	
	private int executingjob ;//job executing CPU
	
	private int numberOfTick;//the number of ticks to finish all jobs
			
	private AlgoType algorithm;
	
	private int quantum_size;
	
	FileWriter fw;
	PrintWriter pw;
	
	CPUScheduleObj(AlgoType _algorithm, int quantum, int numJobs, Job[] jobs, PrintWriter _pw)
	{	
		listjob = new Job[numJobs];
		for(int i = 0; i < numJobs; i++)
			listjob[i] = jobs[i];
			
		queue = new ArrayList<Integer>();
		algorithm = _algorithm;
		executingjob = -1;	
		numberOfTick = 0;
		quantum_size = quantum * Job.TICK_INTERVAL;
		numberOfJobs = numJobs;
		
				
			pw = _pw;
			
	}	
	
	public static Job[] GenerateProcessList(int numberOfJobs)
	{   
		Job[] jobs = new Job[numberOfJobs];       
		int _jobnumber = 0;		
		int _arrivalTime;		
		int _estimateTime;
				
		Random randomGenerator = new Random();
    									           		
		while (_jobnumber < numberOfJobs)
		{
			_arrivalTime = randomGenerator.nextInt(201);//Generate a random integer in the range 0..200
			_estimateTime = randomGenerator.nextInt(10) + 1;//Generate a random integer in the range 1..10
			_jobnumber++;
			Job job = new Job(_jobnumber, _arrivalTime, _estimateTime);
			jobs[_jobnumber - 1] = job;					
		}
		
		//sort
		for(int i = 0;i<jobs.length-1;i++)
		{
			for(int j = i+1;j<jobs.length;j++)
			{
				if(jobs[i].GetStartTick()>jobs[j].GetStartTick())
				{
					Job temp;
					temp = jobs[i];
					jobs[i] = jobs[j];
					jobs[j] = temp;
				}
			}	
		}
		return jobs;
	}
	
		
	int getNumberOfCompletedJob()
	{
		int count = 0; 
		for(int i = 0; i < numberOfJobs; i++)
		{
			if(listjob[i].GetStatus() == Status.Completed)
				count++;				
		}
		return count;
	}
	
	void MainProcess()
	{		
		int i = 0;
		while(getNumberOfCompletedJob() < numberOfJobs)
		{				
			ProcessEveryTick(i);
			numberOfTick++;
			i++;
		}
		FinalStatistics();
				
	}
	
	void ProcessEveryTick(int tick)
	{			
		CheckNewJob(tick);

		//Check done job
		CheckCompleteProcess(tick);		
				
		//Check time slice expired and pick job to CPU
		ProcessInternalEvents(tick);
			
		UpdateIdleTime();
	}
	void CheckNewJob(int tick)
	{			
		//Check_new_jobs;	/* check if new jobs are entering the system */		
		for(int i = 0; i < numberOfJobs; i++)
		{			
			if(listjob[i].GetStartTick() == tick)
			{				
				queue.add(listjob[i].GetJobnumber());								
				
				listjob[i].enter_CPU_queue(tick);			 
			}
		}

	}
	
	int GetNextProcessFromReadyQueue(AlgoType algotype)
	{
		int index = -1;
		Job tmpjob;
		int shortesttime = -1;
		switch(algotype)
		{
			case FCFS:
				for(int i = 0; i < numberOfJobs; i++)
				{
					tmpjob  = listjob[i];
					if(tmpjob.GetStatus() == Status.ReadyQueue)
					{
						index = i;
						break;
					}					
				}
				break;
				case SJF:
					for(int i = 0; i < numberOfJobs; i++)
					{
						tmpjob = listjob[i];
						if(tmpjob.GetStatus() == Status.ReadyQueue)
						{
							if(shortesttime == -1)
							{
								index = i;
								shortesttime = tmpjob.GetEstimatetime();								
							}
							if(tmpjob.GetEstimatetime() < shortesttime)
							{
								index = i;
								shortesttime = tmpjob.GetEstimatetime();
							}
						}
					}
				break;
				case SRT:					
					for(int i = 0; i < numberOfJobs; i++)
					{
						tmpjob = listjob[i];
						if(tmpjob.GetStatus() == Status.ReadyQueue)
						{	
							if(shortesttime == -1)
							{
								index = i;
								shortesttime = tmpjob.GetRemaintime();								
							}						
							if(tmpjob.GetRemaintime() < shortesttime)
							{
								index = i;
								shortesttime = tmpjob.GetRemaintime();
							}
						}
					}
					if(executingjob != -1 && shortesttime >= listjob[GetIndexJobFromQueue(executingjob)].GetRemaintime())
						index = -1;//initialize the shortest remain time is remain time of executing job
				break;
				case RoundRobin:					
										
					if(queue.size() > 0)
					{
						int jobNum = queue.get(0);
						index = GetIndexJobFromQueue(jobNum);
					}
						
				break;
		}
		return index;
	}
	
	void DeleteJobFromQueue()
	{	
		queue.remove(0);//remove the front element for Round Robin algorithm
	}
	
	void ProcessInternalEvents(int tick)
	{		
		//Check time slice event
		if(executingjob == -1)//no job is executing
		{
			int nextprocess = GetNextProcessFromReadyQueue(algorithm);
			if(nextprocess != -1)
			{	
				
				listjob[nextprocess].EnterCPU(tick);				
				executingjob = listjob[nextprocess].GetJobnumber();	
				if(algorithm == AlgoType.RoundRobin)
					DeleteJobFromQueue();	
			}
		}
		else
		{
			if(algorithm == AlgoType.RoundRobin)
			{				
				if(listjob[GetIndexJobFromQueue(executingjob)].GetCountTick() >= quantum_size)
				{					
					listjob[GetIndexJobFromQueue(executingjob)].SliceExpired();
					int nextprocess = GetNextProcessFromReadyQueue(algorithm);//find the job with highest priority
					if(listjob[GetIndexJobFromQueue(executingjob)].GetRemaintime() > 0)//always
					{
							
							if(nextprocess != -1)
							{								
								
								listjob[GetIndexJobFromQueue(executingjob)].MovetoReadyQueue();
								queue.add(listjob[GetIndexJobFromQueue(executingjob)].GetJobnumber());
								listjob[nextprocess].EnterCPU(tick);
								DeleteJobFromQueue();
								executingjob = listjob[nextprocess].GetJobnumber();									
							}
					}
				}
			}//SRT and RoundRobin
			else if(algorithm == AlgoType.SRT)
			{
			
				int nextprocess = GetNextProcessFromReadyQueue(algorithm);//find the job with highest priority
				if(listjob[GetIndexJobFromQueue(executingjob)].GetRemaintime() > 0)//always
				{
						
						if(nextprocess != -1)
						{							
							
							listjob[GetIndexJobFromQueue(executingjob)].MovetoReadyQueue();
							listjob[nextprocess].EnterCPU(tick);
							executingjob = listjob[nextprocess].GetJobnumber();								
						}
				}
				
			}//SRT and RoundRobin
			
		}//exist executing job
		
	}
	
	void CheckCompleteProcess(int tick)
	{
		if(executingjob != -1)
		{
			Job _curjob = listjob[GetIndexJobFromQueue(executingjob)];
			listjob[GetIndexJobFromQueue(executingjob)].Tick();
						
			if(listjob[GetIndexJobFromQueue(executingjob)].GetRemaintime() == 0)
			{
				listjob[GetIndexJobFromQueue(executingjob)].CComplete(tick);
				executingjob = -1;				
			}
		}
	}	
	
	
	void UpdateIdleTime()
	{		
		for(int i = 0; i < numberOfJobs; i++)
		{			
			if(listjob[i].GetStatus() == Status.ReadyQueue)
				listjob[i].IncreaseIdleTime();
		}
	}
	
	public void Output(String strout)
	{
		pw.println(strout); 
		System.out.println(strout);
	}
		
	void FinalStatistics()
	{
		int numberCompletedJob = 0;
		double totalCPUBurtTime = 0;
		double totalwaittime = 0;
		double totalturn_aroundtime = 0;
		double totalResponseTime = 0;
		int totalNoOfContexSwitching = 0;
		Job _tmpjob;
		for(int i = 0; i < numberOfJobs; i++)
		{
			_tmpjob = listjob[i];
			if(_tmpjob.GetStatus() == Status.Completed)
			{
				numberCompletedJob++;
				totalCPUBurtTime += _tmpjob.GetEstimatetime();
				totalturn_aroundtime += _tmpjob.GetTurnAroundtime();
				totalwaittime += _tmpjob.GetWaitingTime();
				totalResponseTime += _tmpjob.GetResponseTime();
				totalNoOfContexSwitching += _tmpjob.GetNoOfContextSwitching();
			}			
		}	 
		

		Output("********************************************************************\n");
		Output(String.format("************** Scheduling algorithm: %s ************************\n", new Object[]{algorithm.toString()}));
		if (this.algorithm == AlgoType.RoundRobin)
			Output(String.format("************** Quantum: %d ************************\n", new Object[]{quantum_size/Job.TICK_INTERVAL}));
		
		Output("********************************************************************\n\n");
		Output("pid  arrival  CPU-burst  finish  waiting-time  turn-around  reponse-time  No.of-Context\n");
		for(int i = 0; i < numberOfJobs; i++)
		{
			_tmpjob = listjob[i];
			if(_tmpjob.GetStatus() == Status.Completed)
			{				
				Output(String.format(" %2d   %5d   %8d   %5d   %8d   %8d   %9d   %11d\n", new Object[]{_tmpjob.GetJobnumber(), _tmpjob.GetStartTick()/Job.TICK_INTERVAL, _tmpjob.GetEstimatetime()/Job.TICK_INTERVAL, _tmpjob.GetFinishTime()/Job.TICK_INTERVAL, _tmpjob.GetWaitingTime()/Job.TICK_INTERVAL, _tmpjob.GetTurnAroundtime()/Job.TICK_INTERVAL, _tmpjob.GetResponseTime()/Job.TICK_INTERVAL, _tmpjob.GetNoOfContextSwitching()}));
			}			
		}
		Output(String.format("Average CPU burst time = %.2f ms	Average waiting time = %.2f ms\n", new Object[]{ totalCPUBurtTime/(Job.TICK_INTERVAL*numberOfJobs), totalwaittime/(Job.TICK_INTERVAL*numberOfJobs)}));
		Output(String.format("Average turn around time = %.2f ms	Average response time = %.2f ms\n", new Object[]{totalturn_aroundtime/(Job.TICK_INTERVAL*numberOfJobs), totalResponseTime/(Job.TICK_INTERVAL*numberOfJobs)}));
		Output(String.format("Total No. of Context Switching Performed = %d\n", new Object[]{totalNoOfContexSwitching}));
		
		
//		pw.close();
		
	}
	
	int GetIndexJobFromQueue(int _jobnumber)
	{
		int index = -1;
		for(int i = 0; i < numberOfJobs; i++)
			if((listjob[i]).GetJobnumber() == _jobnumber)
				index = i;
		return index;		
	}	
		
    
    public static void main(String[] args) {
    	AlgoType _algorithm = AlgoType.SRT;
    	int quantumSize = 1;
		int numberJobs = 50;
		
		if(args.length == 2)
		{
			quantumSize = Integer.parseInt(args[0]);
			numberJobs = Integer.parseInt(args[1]);
		}
		
		try{
		
			File out = new File(CPUScheduleObj.OUTPUT_FILE_NAME);  
			FileWriter fw = new FileWriter (out);  
			PrintWriter pw = new PrintWriter(fw);
	
    	
	    	AlgoType[] algorithms = new AlgoType[]{AlgoType.FCFS, AlgoType.SJF, AlgoType.RoundRobin}; 
	    		
	    	for(int i = 0; i < 5; i++)//For the same set of input parameters, run each algorithm for 5 times	 
	    	{
	    		Job[] jobs = GenerateProcessList(numberJobs);//different arrival times and expected total run time
	    	
		    	for(int j = 0; j < algorithms.length; j++)
		    	{
		    		_algorithm = algorithms[j];
		    		CPUScheduleObj schedobj = new CPUScheduleObj(_algorithm, quantumSize, numberJobs, jobs, pw);
					schedobj.MainProcess();
		    	}  
	    	}
	    	
	    	pw.close();
		}
		catch(IOException ioe)
		{
		}
	   
    }
}
