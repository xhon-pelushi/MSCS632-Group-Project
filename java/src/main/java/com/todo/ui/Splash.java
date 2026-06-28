package com.todo.ui;

/**
 * Animated startup splash. Reveals a "JAVA" ASCII banner line-by-line and then
 * runs a short rotating spinner before the main menu appears.
 */
public final class Splash {

    private static final String RESET  = "[0m";
    private static final String BOLD   = "[1m";
    private static final String ORANGE = "[38;5;208m";
    private static final String CYAN   = "[36m";
    private static final String GREEN  = "[32m";

    private static final String[] JAVA_ART = {
            "     ██  █████  ██   ██  █████ ",
            "     ██ ██   ██ ██   ██ ██   ██",
            "     ██ ███████ ██   ██ ███████",
            "██   ██ ██   ██  ██ ██  ██   ██",
            " █████  ██   ██   ███   ██   ██",
    };

    private static final String[] SPINNER = {"|", "/", "-", "\\"};

    private Splash() {
    }

    public static void show() {
        clearScreen();
        System.out.println();
        for (String line : JAVA_ART) {
            System.out.println(BOLD + ORANGE + line + RESET);
            sleep(70);
        }
        System.out.println();

        String label = CYAN + "  Brewing your tasks " + RESET;
        for (int i = 0; i < 16; i++) {
            System.out.print("\r" + label + ORANGE + SPINNER[i % SPINNER.length] + RESET);
            System.out.flush();
            sleep(80);
        }
        System.out.println("\r" + label + GREEN + "done" + RESET);
        sleep(150);
    }

    private static void clearScreen() {
        System.out.print("[2J[H");
        System.out.flush();
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
