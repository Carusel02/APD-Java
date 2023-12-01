import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.LinkedList;
import java.util.Queue;

public class MyHost extends Host {

    // for PriorityBlockingQueue
    private static class TaskPriorityComp implements Comparator<Task> {
        public int compare(Task t1, Task t2) {
            // Compare based on priority
            int priorityComparison = Integer.compare(t2.getPriority(), t1.getPriority());

            // If priorities are the same, compare based on arrival time
            if (priorityComparison == 0) {
                return Integer.compare(t1.getStart(), t2.getStart());
            }

            return priorityComparison;
        }
    }

    // unique per host
    private final PriorityBlockingQueue<Task> queue = new PriorityBlockingQueue<>(100, new TaskPriorityComp());
    private final Object lock = new Object();
    private volatile boolean isRunning = true;

    private volatile int haveThread = 0;

    private volatile Task globalTask;

    private volatile double timeStart = 0;
    private volatile double timeEnd = 0;

    @Override
    public void run() {
        System.out.println("I'm running...(" + this.getId() + ")");

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

        // System.out.println("[START TIME] : " + timeStart);
        // System.out.println("[END TIME] : " + timeEnd);
        long diff = Math.round(timeEnd - timeStart);
        // System.out.println("[DIFF] : " + diff);

        if(diff * 1000 < this.globalTask.getLeft()) {
            this.globalTask.setLeft(this.globalTask.getLeft() - 1000 * diff);
            // System.out.println("[REMAIN TIME] : " + this.globalTask.getLeft());
            synchronized (lock) {
                queue.offer(this.globalTask);
            }
        }

        globalTask.finish(); // finish task
    }

    @Override
    public void addTask(Task task) {
        // System.out.println("[INFO] Host : " + this.getId() + " -> task : " + task.getId());

        // if we have a task
        if(this.globalTask != null) {
            // and is preemptive
            if(this.globalTask.isPreemptible()) {
                System.out.println("Task " + this.globalTask.getId() + " is preemptive");

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
            if (this.globalTask == null) {
                return queue.stream().mapToLong(Task::getLeft).sum();
            } else{
                double timerIs = Timer.getTimeDouble();
                // System.out.println("----------> [TIME IS : " + timerIs + "]");
                long difference = Math.round(Timer.getTimeDouble() - timeStart);
                // System.out.println("----------> [DIFFERENCE IS : " + difference + "]");
                long remaining = Math.round(this.globalTask.getLeft() - 1000 * difference);
                // System.out.println("----------> [REMAINING IS : " + remaining + "]");
                long sum = queue.stream().mapToLong(Task::getLeft).sum();
                // System.out.println("----------> [SUM IS : " + sum + "]");
                // System.out.println("----------> [FINAL IS : " + (sum + remaining / 1000 * 1000) + "]");
                // System.out.println();
                return sum + remaining / 1000 * 1000;
            }

        }
    }

    @Override
    public void shutdown() {
        System.out.println("Shutting down...(" + this.getId() + ")");
        isRunning = false;
    }
}
