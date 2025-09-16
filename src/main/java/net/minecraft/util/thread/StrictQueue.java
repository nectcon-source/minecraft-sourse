package net.minecraft.util.thread;

import com.google.common.collect.Queues;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nullable;

public interface StrictQueue<T, F> {
   @Nullable
   F pop();

   boolean push(T var1);

   boolean isEmpty();

   public static final class FixedPriorityQueue implements StrictQueue<StrictQueue.IntRunnable, Runnable> {
      private final List<Queue<Runnable>> queueList;

      public FixedPriorityQueue(int var1) {
         this.queueList = IntStream.range(0,var1).mapToObj(var0 -> Queues.<Runnable>newConcurrentLinkedQueue()).collect(Collectors.toList());
      }

      @Nullable
      public Runnable pop() {
         for(Queue<Runnable> var2 : this.queueList) {
            Runnable var3 = (Runnable)var2.poll();
            if (var3 != null) {
               return var3;
            }
         }

         return null;
      }

      public boolean push(StrictQueue.IntRunnable var1) {
         int var2 = var1.getPriority();
         (this.queueList.get(var2)).add(var1);
         return true;
      }

      @Override
      public boolean isEmpty() {
         return this.queueList.stream().allMatch(Collection::isEmpty);
      }
   }

   public static final class IntRunnable implements Runnable {
      private final int priority;
      private final Runnable task;

      public IntRunnable(int var1, Runnable var2) {
         this.priority = var1;
         this.task = var2;
      }

      @Override
      public void run() {
         this.task.run();
      }

      public int getPriority() {
         return this.priority;
      }
   }

   public static final class QueueStrictQueue<T> implements StrictQueue<T, T> {
      private final Queue<T> queue;

      public QueueStrictQueue(Queue<T> var1) {
         this.queue = var1;
      }

      @Nullable
      @Override
      public T pop() {
         return this.queue.poll();
      }

      @Override
      public boolean push(T var1) {
         return this.queue.add(var1);
      }

      @Override
      public boolean isEmpty() {
         return this.queue.isEmpty();
      }
   }
}
