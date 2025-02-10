package edu.eci.arsw.highlandersim;

import java.util.List;
import java.util.Random;

public class Immortal extends Thread {

    private ImmortalUpdateReportCallback updateCallback = null;

    private int health;
    private final int defaultDamageValue;
    private final List<Immortal> immortalsPopulation;
    private final String name;
    private final Random r = new Random(System.currentTimeMillis());
    private boolean pause = false;
    public boolean isDead = false;

    public Immortal(String name, List<Immortal> immortalsPopulation, int health, int defaultDamageValue,
                    ImmortalUpdateReportCallback ucb) {
        super(name);
        this.updateCallback = ucb;
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.health = health;
        this.defaultDamageValue = defaultDamageValue;
    }

    public void run() {
        while (true) {

            synchronized (this) {
                while (pause) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (isDead) {
                return; // Si está muerto, el hilo termina
            }

            Immortal opponent;
            synchronized (immortalsPopulation) {
                int myIndex = immortalsPopulation.indexOf(this);
                int nextFighterIndex = r.nextInt(immortalsPopulation.size());

                // Evitar autoataque
                if (nextFighterIndex == myIndex) {
                    nextFighterIndex = (nextFighterIndex + 1) % immortalsPopulation.size();
                }

                opponent = immortalsPopulation.get(nextFighterIndex);
            }

            fight(opponent);

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void fight(Immortal opponent) {
        synchronized (immortalsPopulation) {
            if (!opponent.isDead && opponent.getHealth() > 0) {
                opponent.changeHealth(opponent.getHealth() - defaultDamageValue);
                this.changeHealth(this.getHealth() + defaultDamageValue);
                updateCallback.processReport("Fight: " + this + " vs " + opponent + "\n");

                if (opponent.getHealth() <= 0) {
                    opponent.markAsDead();
                    updateCallback.processReport(opponent + " has been defeated by " + this + "\n");
                }
            }
        }
    }

    public synchronized void changeHealth(int value) {
        health = value;
    }

    public synchronized int getHealth() {
        return health;
    }

    public synchronized void markAsDead() {
        isDead = true;
        health = 0;
    }

    @Override
    public String toString() {
        return name + "[" + health + "]";
    }

    public synchronized void pause() {
        this.pause = true;
    }

    public synchronized void resumeThread() {
        this.pause = false;
        notify();
    }

    public void stopThread() {
        boolean running = false; // Cambia el estado para detener el ciclo
    }    
}
