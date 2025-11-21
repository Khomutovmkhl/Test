package ru.liquisort.testAssignment;

import lombok.Getter;

import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Getter
public class Account {
    private final String id;
    private final Lock lock;
    private int money;

    public Account(int money) {
        this.id = UUID.randomUUID().toString();
        this.money = money;
        this.lock = new ReentrantLock();
    }

    public void sendMoney(Integer amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Отправляемая сумма не может быть меньше нуля");
        }
        if (this.money < amount) {
            throw new IllegalStateException("Недостаточно средств на счёте " + id);
        }
        this.money -= amount;
    }

    public void receiveMoney(Integer amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Принимаемая сумма не может быть меньше нуля");
        }
        this.money += amount;
    }
}
