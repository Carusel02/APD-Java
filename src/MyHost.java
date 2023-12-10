import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;


public class MyHost extends Host {

    // for PriorityBlockingQueue
    private static class TaskPriorityComp implements Comparator<Task> {
        public int compare(Task t1, Task t2) {
            // compare based on priority
            int priorityComparison = Integer.compare(t2.getPriority(), t1.getPriority());

            // compare based on start time, if priority is the same
            if (priorityComparison == 0) {
                return Integer.compare(t1.getStart(), t2.getStart());
            }

            return priorityComparison;
        }
    }

    // priority blocking queue
    private final PriorityBlockingQueue<Task> queue = new PriorityBlockingQueue<>(1000, new TaskPriorityComp());

    // lock for queue
    private final Object lock = new Object();
    // running flag
    private volatile boolean isRunning = true;
    // thread running flag
    private volatile int haveThread = 0;
    // task running
    private volatile Task globalTask;
    // time start and end
    private volatile double timeStart = 0;
    private volatile double timeEnd = 0;

    @Override
    public void run() {
        // System.out.println("I'm running...(" + this.getId() + ")");

        while (isRunning) {
            haveThread = 0;

            if(!queue.isEmpty()) { // if queue is not empty
                synchronized (lock) {
                    this.globalTask = queue.poll(); // take task from queue
                }
                executeTask();  // execute task
            }


        }


    }

    public void executeTask() {
        haveThread = 1; // we have a thread
        timeStart = Timer.getTimeDouble(); // start time

        synchronized (lock) {
            try {
                lock.wait(this.globalTask.getLeft()); // execute task
                timeEnd = Timer.getTimeDouble(); // end time
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        long diff = Math.round(timeEnd - timeStart);
        // System.out.println("[START TIME] : " + timeStart);
        // System.out.println("[END TIME] : " + timeEnd);
        // System.out.println("[DIFF] : " + diff);

        if(diff * 1000 < this.globalTask.getLeft()) {
            this.globalTask.setLeft(this.globalTask.getLeft() - 1000 * diff);
            synchronized (lock) {
                queue.offer(this.globalTask); // push task back to queue
            }
            // return;
        }

        globalTask.finish(); // finish task
    }

    @Override
    public void addTask(Task task) {
        // System.out.println("Host : " + this.getId() + " -> task : " + task.getId());

        // if we have a task
        if(this.globalTask != null) {
            // and is preemptive
            if(this.globalTask.isPreemptible()) {
                // System.out.println("Task " + this.globalTask.getId() + " is preemptive");

                synchronized (lock) {
                    queue.add(task); // add task to queue
                    lock.notify(); // notify thread
                }
                return;
            }
        }

        // if we don't have a task or is not preemptive
        queue.add(task);
    }

    @Override
    public int getQueueSize() {
        synchronized (lock) {
            return queue.size() + haveThread;
        }
    }


    @Override
    public long getWorkLeft() {
        synchronized (lock) {
            // calculate work left
            long workLeft = 0;
            for (Task task : queue) {
                workLeft += task.getLeft();
            }

            if (this.globalTask == null) {
                // if we don't have a task
                return workLeft;
            } else {
                // if we have a task
                long difference = Math.round(Timer.getTimeDouble() - timeStart);
                long remaining = Math.round(this.globalTask.getLeft() - 1000 * difference);
                return workLeft + remaining / 1000 * 1000; // round to seconds
            }
        }
    }

    @Override
    public void shutdown() {
        // System.out.println("Shutting down...(" + this.getId() + ")");
        isRunning = false;
    }
}
