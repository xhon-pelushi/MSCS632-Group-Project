package com.todo.ui;

/**
 * Small ANSI helper: colors plus two animation primitives used across the UI -
 * a staggered menu reveal (via {@link #sleep}) and a bar that sweeps across the
 * screen when an option is selected.
 */
public final class Ansi {

    public static final String RESET  = "[0m";
    public static final String BOLD   = "[1m";
    public static final String ORANGE = "[38;5;208m";
    public static final String CYAN   = "[36m";
    public static final String GREEN  = "[32m";
    public static final String GRAY   = "[38;5;245m";

    private static final int SWEEP_WIDTH = 44;

    private Ansi() {
    }

    /** Draws a colored bar running left-to-right across the screen, then clears it. */
    public static void sweep(String color) {
        for (int width = 1; width <= SWEEP_WIDTH; width++) {
            System.out.print("\r" + color + "━".repeat(width) + RESET);
            System.out.flush();
            sleep(8);
        }
        System.out.print("\r" + " ".repeat(SWEEP_WIDTH) + "\r");
        System.out.flush();
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
