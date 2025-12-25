package com.example.my_pet;

/**
 * Simple tamagotchi-like creature model that tracks basic needs and mood.
 */
public class Creature {

    private int hunger = 50;
    private int tiredness = 50;
    private int boredom = 50;
    private int happiness = 50;

    public int getHunger() {
        return hunger;
    }

    public int getTiredness() {
        return tiredness;
    }

    public int getBoredom() {
        return boredom;
    }

    public int getHappiness() {
        return happiness;
    }

    public boolean isAlive() {
        return hunger < 100 &&
                tiredness < 100 &&
                boredom < 100 &&
                happiness > 0;
    }

    public void feed() {
        hunger -= 20;
        happiness += 5;
        normalize();
    }

    public void sleep() {
        tiredness -= 30;
        hunger += 10;
        normalize();
    }

    public void play() {
        boredom -= 30;
        tiredness += 10;
        happiness += 10;
        normalize();
    }

    public void timePass() {
        hunger += 2;
        tiredness += 2;
        boredom += 2;
        happiness -= 2;
        normalize();
    }

    private void normalize() {
        hunger = clamp(hunger);
        tiredness = clamp(tiredness);
        boredom = clamp(boredom);
        happiness = clamp(happiness);
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }
}


