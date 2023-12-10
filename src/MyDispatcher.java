/* Implement this class. */

import java.util.List;

public class MyDispatcher extends Dispatcher {

    // for RoundRobin policy
    private int lastHost = -1;

    public MyDispatcher(SchedulingAlgorithm algorithm, List<Host> hosts) {
        super(algorithm, hosts);
    }

    // send task to a host based on algorithm policy
    @Override
    public synchronized void addTask(Task task) {
        switch (algorithm) {
            case ROUND_ROBIN -> RoundRobin(task);
            case SHORTEST_QUEUE -> ShortestQueue(task);
            case SIZE_INTERVAL_TASK_ASSIGNMENT -> SizeIntervalTaskAssignment(task);
            case LEAST_WORK_LEFT -> LeastWorkLeft(task);
        }
    }

    // RoundRobin policy
    public synchronized void RoundRobin(Task task) {
        // System.out.println("Dispatcher : " + task);
        int nextHost = (lastHost + 1) % hosts.size();
        hosts.get(nextHost).addTask(task);
        lastHost = nextHost;
    }

    // ShortestQueue policy
    public synchronized void ShortestQueue(Task task){
        // System.out.println("Dispatcher : " + task);

        // index of host with minimum id and minimum queue size
        int index = 0;

        // set a minimum value
        int minQueue = hosts.get(0).getQueueSize();
        // find minimum queue size
        for(Host host : hosts){
            if(host.getQueueSize() < minQueue){
                minQueue = host.getQueueSize();
            }
        }

        // find the host with minimum id and minimum queue size
        long minID = Integer.MAX_VALUE;
        for(Host host : hosts){
            if(host.getQueueSize() == minQueue && host.getId() < minID){
                minID = host.getId();
                index = hosts.indexOf(host);
            }
        }

        // add task to host with minimum id and minimum queue size
        hosts.get(index).addTask(task);
    }

    // SizeIntervalTaskAssignment policy
    public synchronized void SizeIntervalTaskAssignment(Task task){
        switch(task.getType()) {
            case SHORT -> hosts.get(0).addTask(task);
            case MEDIUM -> hosts.get(1).addTask(task);
            case LONG -> hosts.get(2).addTask(task);
        }
    }

    // LeastWorkLeft policy
    public synchronized void LeastWorkLeft(Task task){
        // System.out.println("Dispatcher : " + task);

        // index of host with minimum id and minimum calculation of work left
        int index = 0;

        // set a minimum value
        long minWork = hosts.get(0).getWorkLeft();
        // find minimum queue size
        for(Host host : hosts){
            if(host.getWorkLeft() < minWork){
                minWork = host.getWorkLeft();
            }
        }

        // find the host with minimum id and minimum queue size
        long minID = Integer.MAX_VALUE;
        for(Host host : hosts){
            if(host.getWorkLeft() == minWork && host.getId() < minID){
                minID = host.getId();
                index = hosts.indexOf(host);
            }
        }

        // add task to host with minimum id and minimum queue size
        hosts.get(index).addTask(task);
    }


}
