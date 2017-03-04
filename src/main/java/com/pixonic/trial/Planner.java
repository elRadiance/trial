package com.pixonic.trial;


import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.*;

/**
 * Основной класс - планировщик
 */
public class Planner {


    private static final int DEFAULT_THREAD_COUNT = 3;



    private final ScheduledExecutorService executorService;


    /**
     * Конструктор по умолчанию
     * executorService будет запущен с 3-мя потоками
     */
    public Planner(){
        executorService =
                new ScheduledThreadPoolExecutor(DEFAULT_THREAD_COUNT);
    }


    /**
     * Конструктор, принимающий количество потоков для обработчика
     *
     * @param threadCount количество потоков, с которым будет запущен
     *                    executionService
     */
    public Planner(int threadCount){
        executorService =
                new ScheduledThreadPoolExecutor(threadCount);
    }


    /**
     * Метод добавляет задачу в очередь исполнения
     *
     * @param plannedExecutionTime желаемое дата-время исполнения задачи
     * @param callable сама задача
     * @param <RESULT_TYPE> параметр задачи (тип возвращаемого значения callable)
     *
     * @return future со статусом задачи
     */
    public <RESULT_TYPE> ScheduledFuture<RESULT_TYPE>
    addTask(LocalDateTime plannedExecutionTime, Callable<RESULT_TYPE> callable){
        LocalDateTime now = LocalDateTime.now();
        if(plannedExecutionTime.compareTo(now) <= 0){
            // задача просрочена или почти просрочена,
            // начинаем делать без задержки
            return executorService
                    .schedule(callable,
                            0L,
                            TimeUnit.NANOSECONDS);
        } else {
            // планируем задачу на будущее с заданной задержкой
            long delayInNanos = calculateDelayInNanos(plannedExecutionTime, now);
            return executorService
                    .schedule(callable,
                            delayInNanos,
                            TimeUnit.NANOSECONDS);
        }
    }


    /**
     * Данный метод рассчитывает задержку для executionService
     *
     * @param plannedExecutionTime желаемое время исполнения задачи
     * @param now время, когда задача поступила в обработку
     *
     * @return задержка в наносекундах между
     * plannedExecutionTime и now
     */
    private long calculateDelayInNanos(LocalDateTime plannedExecutionTime, LocalDateTime now){
        return now.until(plannedExecutionTime, ChronoUnit.NANOS);
    }


    /**
     * Почистим за собой потоки
     */
    public void finalize() throws Throwable{
        super.finalize();
        executorService.shutdown();
    }


}
