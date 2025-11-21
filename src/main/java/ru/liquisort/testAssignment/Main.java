package ru.liquisort.testAssignment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Main {
    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(Main.class.getName());
    private static final int INITIAL_BALANCE = 10000;
    private static final int NUMBER_OF_ACCOUNTS = 4;
    private static final int NUMBER_OF_THREADS = 3;

    public static void main(String[] args) throws InterruptedException {
        List<Account> accounts = new ArrayList<>();
        Thread[] threads = new Thread[NUMBER_OF_THREADS];

        // Accounts initialization
        for (int i = 0; i < NUMBER_OF_ACCOUNTS; i++) {
            accounts.add(new Account(INITIAL_BALANCE));
        }
        log.info(String.format("Создано %d счетов с начальным балансом %d", NUMBER_OF_ACCOUNTS, INITIAL_BALANCE));

        CountDownLatch completionLatch = new CountDownLatch(NUMBER_OF_THREADS);

        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            UserThread thread = new UserThread("Thread-" + (i + 1), accounts, completionLatch);
            threads[i] = thread;
            thread.start();
        }

        // Waiting for every thread to finish
        for (Thread thread : threads) {
            thread.join();
        }

        log.info("Все потоки завершили работу");
    }

}