/*
 * Copyright (C) 2013, 2014 Brett Wooldridge
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zaxxer.hikari.pool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * A Runnable that is scheduled in the future to report leaks.  The ScheduledFuture is
 * cancelled if the connection is closed before the leak time expires.
 *
 *  这个task用于检测泄露，也就是获取了连接，但是久久没有还，这个任务如果被调度，说明已经达到指定的时间
 *  没有归还连接
 *
 * @author Brett Wooldridge
 */
class ProxyLeakTask implements Runnable
{
   private static final Logger LOGGER = LoggerFactory.getLogger(ProxyLeakTask.class);
   static final ProxyLeakTask NO_LEAK;

   private ScheduledFuture<?> scheduledFuture;
   private String connectionName;
   private Exception exception;
   private String threadName;
   private boolean isLeaked;

   static
   {
      NO_LEAK = new ProxyLeakTask() {
         @Override
         void schedule(ScheduledExecutorService executorService, long leakDetectionThreshold) {}

         @Override
         public void run() {}

         @Override
         public void cancel() {}
      };
   }

   ProxyLeakTask(final PoolEntry poolEntry)
   {
      this.exception = new Exception("Apparent connection leak detected");
      this.threadName = Thread.currentThread().getName();
      this.connectionName = poolEntry.connection.toString();
   }

   private ProxyLeakTask()
   {
   }

   void schedule(ScheduledExecutorService executorService, long leakDetectionThreshold)
   {
      scheduledFuture = executorService.schedule(this, leakDetectionThreshold, TimeUnit.MILLISECONDS);
   }

   /**
    *  任务启动，说明发生了泄露，太久没还连接
    */
   /** {@inheritDoc} */
   @Override
   public void run()
   {
      isLeaked = true;

      final StackTraceElement[] stackTrace = exception.getStackTrace();
      final StackTraceElement[] trace = new StackTraceElement[stackTrace.length - 5];
      System.arraycopy(stackTrace, 5, trace, 0, trace.length);

      exception.setStackTrace(trace);
      LOGGER.warn("Connection leak detection triggered for {} on thread {}, stack trace follows", connectionName, threadName, exception);
   }

   /**
    *  发生异常，归还或者驱逐连接的时候会调用
    */
   void cancel()
   {
      scheduledFuture.cancel(false);
      if (isLeaked) {
         LOGGER.info("Previously reported leaked connection {} on thread {} was returned to the pool (unleaked)", connectionName, threadName);
      }
   }
}
