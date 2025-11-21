package ru.liquisort.testAssignment;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class UserThread extends Thread {
    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(UserThread.class.getName());
    private static final AtomicInteger transactionCounter = new AtomicInteger(0);
    private static final int MAX_TRANSACTIONS = 30;

    private final String threadName;
    private final List<Account> accounts;
    private final Random random;
    private final CountDownLatch completionLatch;

    public UserThread(String threadName, List<Account> accounts, CountDownLatch completionLatch) {
        this.threadName = threadName;
        this.accounts = accounts;
        this.random = new Random();
        this.completionLatch = completionLatch;
    }

    @Override
    public void run() {
        log.info(String.format("Поток %s начал работу", threadName));
        try {
            while (transactionCounter.get() < MAX_TRANSACTIONS) {
                // random pause 1000 - 2000 ms
                int sleepTime = 1000 + random.nextInt(1001);
                Thread.sleep(sleepTime);

                // Check for max transactions count
                if (transactionCounter.get() >= MAX_TRANSACTIONS) {
                    break;
                }

                performTransfer();
            }
        } catch (InterruptedException e) {
            log.warning(String.format("Поток %s был прерван %s", threadName, e));
            Thread.currentThread().interrupt();
        } finally {
            completionLatch.countDown();
            log.info(String.format("Поток %s завершил работу", threadName));
        }
    }

    private void performTransfer() {
        Account fromAccount = null;
        Account toAccount = null;

        while (fromAccount == toAccount) {
            fromAccount = accounts.get(random.nextInt(accounts.size()));
            toAccount = accounts.get(random.nextInt(accounts.size()));
        }

        // Blocking accounts in order to avoid deadlock
        Account firstLock = fromAccount.getId().compareTo(toAccount.getId()) < 0 ? fromAccount : toAccount;
        Account secondLock = firstLock == fromAccount ? toAccount : fromAccount;

        boolean transferCompleted = false;

        try {
            if (firstLock.getLock().tryLock()) {
                try {
                    if (secondLock.getLock().tryLock()) {
                        try {
                            if (transactionCounter.get() >= MAX_TRANSACTIONS) {
                                return;
                            }
                            int maxTransferAmount = Math.min(fromAccount.getMoney(), 1000);
                            if (maxTransferAmount > 0) {
                                int transferAmount = 1 + random.nextInt(maxTransferAmount);
                                fromAccount.sendMoney(transferAmount);
                                toAccount.receiveMoney(transferAmount);

                                int currentTransaction = transactionCounter.incrementAndGet();

                                log.info(String.format("Транзакция №%s, Поток %s, Перевод с %s на %s на сумму %s, Балансы: %s -> %s, %s -> %s",
                                        currentTransaction, threadName, fromAccount.getId(), toAccount.getId(), transferAmount,
                                        fromAccount.getMoney() + transferAmount, fromAccount.getMoney(),
                                        toAccount.getMoney(), toAccount.getMoney() + transferAmount));

                                transferCompleted = true;
                            } else {
                                log.warning(String.format("Недостаточно средств на счёте %s для перевода", fromAccount.getId()));
                            }
                        } finally {
                            secondLock.getLock().unlock();
                        }
                    }
                } finally {
                    firstLock.getLock().unlock();
                }
            }


            if (!transferCompleted) {
                log.info(String.format("Не удалось получить блокировки для перевода в потоке %s", threadName));
            }
        } catch (Exception e) {
            log.severe(String.format("Ошибка при выполнении перевода в потоке %s. %s", threadName, e));
        }
    }
}
