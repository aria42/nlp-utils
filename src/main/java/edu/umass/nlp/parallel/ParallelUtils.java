package edu.umass.nlp.parallel;

import edu.umass.nlp.functional.Fn;
import edu.umass.nlp.functional.Functional;
import edu.umass.nlp.utils.Collections;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


public class ParallelUtils {

  public static void doParallelWork(List<? extends Runnable> runnables,
                                    int numThreads)
  {
    final ExecutorService executor  = Executors.newFixedThreadPool(numThreads);
    for (final Runnable runnable : runnables) {
      executor.execute(runnable);
    }
    try {
      executor.shutdown();
      executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
    } catch (InterruptedException ie) {
      ie.printStackTrace();
    }
  }

  public static void main(String[] args) {
    List<Integer> ints = new ArrayList<Integer>();
    for (int i=1; i <= 100; ++i) {
      ints.add(i);
    }
    List<List<Integer>> parts = Collections.partition(ints, Runtime.getRuntime().availableProcessors());
    class Worker implements Runnable {
      double sum = 0.0;
      List<Integer> part;
      Worker(List<Integer> part) {
        this.part = part;
      }
      public void run() {
        for (Integer i : part) {
          sum += i;
        }
      }
    }
    List<Worker> workers = Functional.map(parts, new Fn<List<Integer>, Worker>() {
      public Worker apply(List<Integer> input) {
        return new Worker(input);
      }});
    doParallelWork(workers, Runtime.getRuntime().availableProcessors());
    double totalSum = 0.0;
    System.out.println("numThreads: " + workers.size());
    for (Worker worker : workers) {
      totalSum += worker.sum;
    }
    System.out.println("total sum: " + totalSum);
  }

}
