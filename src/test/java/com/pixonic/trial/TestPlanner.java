package com.pixonic.trial;


import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * Класс, тестирующий планировщик
 */
public class TestPlanner {

    // Экземпляр тестируемого планировщика
    // несмотря на то, что тест может запускаться
    // многопоточно, тест сделан так, что ему
    // это не вредит
    private Planner PLANNER;

    // Класс исполняемой задачи для теста
    private static final class TestCallable implements Callable {

        private final AtomicInteger counter;
        private final int myNumber;

        private TestCallable(AtomicInteger counter, int myNumber){
            this.myNumber = myNumber;
            this.counter = counter;
        }

        @Override
        public Object call() throws Exception {
            counter.set(myNumber);
            return myNumber;
        }
    }



    @Before
    public void initialize(){
        PLANNER = new Planner(4);
    }


    @Test
    public void testStandardCase(){
        AtomicInteger counter = new AtomicInteger(666);
        TestCallable task1 = new TestCallable(counter, 7);
        LocalDateTime time1 = LocalDateTime.now()
                .plus(5L, ChronoUnit.SECONDS);
        TestCallable task2 = new TestCallable(counter, 22);
        LocalDateTime time2 = LocalDateTime.now()
                .plus(8L, ChronoUnit.SECONDS);

        ScheduledFuture future1 = PLANNER.addTask(time2, task2);
        ScheduledFuture future2 = PLANNER.addTask(time1, task1);

        blockUntilFinished(future1, future2);

        assertEquals("Test standard case. Incorrect execution order or not executed",
                22,
                counter.get());
    }


    @Test
    public void testTimeInThePastCase(){
        AtomicInteger counter = new AtomicInteger(999);
        TestCallable task1 = new TestCallable(counter, 33);
        LocalDateTime time1 = LocalDateTime.now()
                .minus(5L, ChronoUnit.SECONDS);
        TestCallable task2 = new TestCallable(counter, 55);
        LocalDateTime time2 = LocalDateTime.now()
                .minus(50L, ChronoUnit.SECONDS);

        ScheduledFuture future1 = PLANNER.addTask(time1, task1);
        ScheduledFuture future2 = PLANNER.addTask(time2, task2);

        blockUntilFinished(future1, future2);

        assertNotEquals("Test time in the past case. Not executed",
                999,
                counter.get());
    }



    private void blockUntilFinished(ScheduledFuture ... futures){
        for(ScheduledFuture future : futures){
            while(!future.isDone()){
                try{
                    Thread.sleep(50L);
                } catch (InterruptedException e){
                    return;
                }
            }
        }
    }




}
