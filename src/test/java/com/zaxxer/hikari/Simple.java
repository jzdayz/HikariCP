package com.zaxxer.hikari;

import org.junit.Test;

import java.util.Scanner;
import java.util.concurrent.*;

public class Simple {

   @Test
   public void testScheduledExecutorService() throws Exception{
      ScheduledExecutorService service = new ScheduledThreadPoolExecutor(10);
      ScheduledFuture<?> schedule = service.schedule(() -> System.out.println(1), 2, TimeUnit.SECONDS);
      schedule.get();
   }

   @Test
   public void testSynchronousQueue() throws Exception{
      SynchronousQueue<String> s = new SynchronousQueue<>(true);

      Thread thread = Thread.currentThread();

      new Thread(()->{
         Scanner scanner = new Scanner(System.in);
         while (true) {
            String next = scanner.next();
            if (next.equals("1")){
               s.offer(next);
            }
         }
      }).start();

//      thread.interrupt();
      Object poll = s.take();
      System.out.println("取到了"+poll);
   }
}
