/*
 * Prasamsha Pradhan
 * Christin Moreano
 * CS 149 Section 4
 * Spring 2015
 */


package SAMCPU;


/*Adjust MovetoReadyQueue, open comment tick++ to calculate context switching time.
 *Adjust TICK_INTERVAL to split one milisecond into many tick*/


enum Status{NoneStatus, Excuting, ReadyQueue, WaitQueue, Completed, Canceled, Timeout};
enum AlgoType { FCFS, SJF, SRT, RoundRobin};

public class Job {
	
	public static int TICK_INTERVAL = 1; //EVERY ONE TICKS PER MILISECOND
	private int jobnumber;//id
	private int priority;
	private int estimatetime;
	private Status status;
	
	private int starttick;//arrival time
	private int runningtime;
	private int turn_aroundtime;
	private int idletime;
	private int enterCPUtimes ;
	private int countTick ;

	private int finishTime;
	private int firstTimeEnterCPU;
	private int noOfContextSwitching;
	private int waitingTime;
	
	//default constructor
public Job()
{
	starttick = -1;
	jobnumber = -1;
	priority = -1;
	estimatetime = -1;
	status = Status.NoneStatus;

	
	runningtime = 0;
	turn_aroundtime = 0;
	idletime = 0;
	enterCPUtimes = 0;
	countTick = 0;

	firstTimeEnterCPU = -1;
	noOfContextSwitching = 0;
	
}
//constructor with params
	public Job(int _jobnumber, int _starttick, int _estimatetime)
	{
		starttick = _starttick * TICK_INTERVAL;
		jobnumber = _jobnumber;		
		estimatetime = _estimatetime * TICK_INTERVAL;	
		status = Status.NoneStatus;	

		runningtime = 0;
		turn_aroundtime = 0;
		idletime = 0;
		enterCPUtimes = 0;
		countTick = 0;

		firstTimeEnterCPU = -1;
		noOfContextSwitching = 0;
	}
	
	// copy constructor
	public Job (Job another) 
	{	
		this.starttick = another.starttick;
		this.jobnumber = another.jobnumber;
		this.priority = another.priority;
		this.estimatetime = another.estimatetime;
		this.status = another.status;

		
		this.runningtime = another.runningtime;
		this.turn_aroundtime = another.turn_aroundtime;
		this.idletime = another.idletime;
		this.enterCPUtimes = another.enterCPUtimes;
		this.countTick = another.countTick;

		this.firstTimeEnterCPU = another.firstTimeEnterCPU;
		this.noOfContextSwitching = another.noOfContextSwitching;	
	}
//	    this.dummy = another.dummy; // you can access  

	//update status and store the time enter CPU queue
	public void enter_CPU_queue(int stick)
	{		
		status = Status.ReadyQueue;
		
		/*enterCPUQueue = stick;	*/
		
	}
	public void MovetoReadyQueue(/*int & tick*/)
	{
		status = Status.ReadyQueue;
		noOfContextSwitching++;
//		tick++;//switching code is a tick (equals to 0.5 mlsecond)

	}
	public void EnterCPU(int tick)
	{
		status = Status.Excuting;
		enterCPUtimes++;
		countTick = 0;

		if (firstTimeEnterCPU == -1)//this is the first time the job enters CPU
			firstTimeEnterCPU = tick;
	}	
	public void WStartIO()
	{
		status = Status.WaitQueue;
	}	
	public void RCompleteIO()
	{
		status = Status.ReadyQueue;
	}	
	public void CComplete(int currenttick)//currenttick is the time the job coplete
	{
		status = Status.Completed;
		turn_aroundtime = currenttick - starttick;// + 1;
		finishTime = currenttick;
	}	
	public void TTerminate(int currenttick)
	{
		status = Status.Canceled;
		turn_aroundtime = currenttick - starttick;
	}	
	public void Tick()//only executing job
	{
		runningtime++;
		countTick++;
	}
	public void SliceExpired()
	{
		countTick = 0; //reset countTick for the next time
	}
	public void TimeOut(int currenttick)
	{
		status = Status.Timeout;
		turn_aroundtime = currenttick - starttick;
	}
	public void IncreaseIdleTime()
	{
		idletime++;
	}
	
	public int GetJobnumber()
	{
		return  jobnumber;
	}	
	public int GetPriority()
	{
		return priority;
	}
	public int GetEstimatetime()
	{
		return estimatetime;
	}
	public Status GetStatus()
	{
		return status;
	}
	public int GetCountTick()
	{
		return countTick;
	}
	public int GetRemaintime()
	{
		return estimatetime - runningtime;
	}
	
	public int GetRunningtime()
	{
		return runningtime;
	}
	public int GetIdletime()
	{
		return idletime;
	}
	public int GetTurnAroundtime()
	{
		return  turn_aroundtime;
	}
	public int GetEnterCPU()
	{
		return enterCPUtimes;
	}
	
	public int GetStartTick()
	{
		return starttick;
	}

	public int GetFinishTime()
	{
		return finishTime;
	}

	public int GetResponseTime()
	{
		return firstTimeEnterCPU - this.starttick;
	}

	public int GetNoOfContextSwitching()
	{
		return this.noOfContextSwitching;
	}

	public int GetWaitingTime()
	{
		return finishTime - this.starttick - this.estimatetime;
	}
}
