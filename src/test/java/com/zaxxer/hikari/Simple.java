package com.zaxxer.hikari;

import java.util.Scanner;
import java.util.concurrent.SynchronousQueue;

public class Simple {
   public static void main(String[] args) throws Exception{
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
