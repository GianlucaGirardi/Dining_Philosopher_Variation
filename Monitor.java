import java.util.PriorityQueue;

/**
 * Class Monitor
 * To synchronize dining philosophers.
 *
 * @author Serguei A. Mokhov, mokhov@cs.concordia.ca
 */
public class Monitor
{
	/*
	 * ------------
	 * Data members
	 * ------------
	 */

	 /* NOTE: Wait() will be applied on all philosophers that are waiting to eat OR talk && NotifyAll() will notify all the waiting philosophers who will need to recheck their conditions
	  * This being said, the waiting philosophers will be a mixed group (Those who waiting to eat and those who waiting to talk) and notifyAll will desuspend all of them (regardless of what they are waiting to do)
	  */
	private int numPhilosophers;
	private enum STATUS {THINKING, HUNGRY, EATING}; // Status will indicated the current state of the philosopher in question, each index corresponds to the philospher in question
	private STATUS[] states; // Array holding the states of all the philosophers
	private PriorityQueue<Integer> hungryStatus; // Array holding philosophers who's status is HUNGRY
	private boolean talk;

	/**
	 * Constructor
	 */
	public Monitor(int piNumberOfPhilosophers)
	{	
		/* Create the system (Table) */
		this.numPhilosophers = piNumberOfPhilosophers;
		states = new STATUS[piNumberOfPhilosophers];

		/* Set the default state of the system (Table) */
		for(int i=0; i < piNumberOfPhilosophers; i++){
			states[i] = STATUS.THINKING;
		}

		this.talk = false;

		/* Create Queue which will hold the philosopher that are waiting to eat */
		hungryStatus = new PriorityQueue<>();
	}

	/*
	 * -------------------------------
	 * User-defined monitor procedures
	 * -------------------------------
	 */

	/**
	 * Grants request (returns) to eat when both chopsticks/forks are available.
	 * Else forces the philosopher to wait()
	 */
	public synchronized void pickUp(final int piTID)
	{
		/* I am hungry, put me in the hungry queue */
		states[piTID-1] = STATUS.HUNGRY;
		this.hungryStatus.add(piTID);

		/* Test myself and my neighbors */
		while(true){ // Use while loop to continuously test my neighbors if i get notified 

			/* Check if my neighbors are eating and I am hungry */
			if(this.states[piTID-1] == STATUS.HUNGRY && this.states[(piTID-2 + this.numPhilosophers) % this.numPhilosophers] != STATUS.EATING && this.states[piTID % this.numPhilosophers] != STATUS.EATING){
				this.states[piTID-1] = STATUS.EATING; // If i am hungry and my neighbors are not eating, then proceed to eat
				break; 
			}
			else{
				try {
					this.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} // Put this thread into a waiting state till it is notified
			}
		}
		
		/* I can eat, remove me from the queue */
		this.hungryStatus.remove();

	}


	/**
	 * When a given philosopher's done eating, they put the chopstiks/forks down
	 * and let others know they are available.
	 */
	public synchronized void putDown(final int piTID)
	{
		/* I am done EATING */
		states[piTID-1] = STATUS.THINKING;

		/* Notify everbody else that I am done EATING, this will wake up all other threads which will give them the chance to recheck their conditions if they are picked back up by the CPU */
		this.notifyAll();
	}

	/**
	 * Only one philopher at a time is allowed to philosophy
	 * (while she is not eating).
	 */
	public synchronized void requestTalk()
	{
		/* Check if someone is talking */
		while(true){ // Once a philosopher is notified that their are done talking and i get picked back up, i might need to recheck the conditions to see if i can proceed to talk
			if(this.talk == true){
				try {
					this.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} // If someone else is talking, i need to wait
			}
			else{
				break; // If no one is talking, i can proceed to talk
			}
		}
		this.talk = true;
	}

	/**
	 * When one philosopher is done talking stuff, others
	 * can feel free to start talking.
	 */
	public synchronized void endTalk()
	{
		/* I am done talking, let everbody know  */
		this.talk = false;
		this.notifyAll();

	}
}

