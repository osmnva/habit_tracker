package org.example.util;


public final class Printer {


    public static final String RESET  = "\u001B[0m";
    public static final String BOLD   = "\u001B[1m";
    public static final String DIM    = "\u001B[2m";
    public static final String GREEN  = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String RED    = "\u001B[31m";
    public static final String CYAN   = "\u001B[36m";
    public static final String BLUE   = "\u001B[34m";
    public static final String MAGENTA= "\u001B[35m";

    private Printer() {}



    public static void success(String msg) {
        System.out.println("  " + GREEN  + "✅  " + msg + RESET);
    }

    public static void error(String msg) {
        System.out.println("  " + RED    + "❌  " + msg + RESET);
    }

    public static void warn(String msg) {
        System.out.println("  " + YELLOW + "⚠   " + msg + RESET);
    }

    public static void info(String msg) {
        System.out.println("  " + CYAN   + "ℹ   " + msg + RESET);
    }

    public static void achievement(String msg) {
        System.out.println("  " + YELLOW + BOLD + "🎉  " + msg + RESET);
    }



    public static void header(String title) {
        int pad = Math.max(0, 46 - title.length());
        System.out.println();
        System.out.println(BOLD + "  ══ " + title + " " + "═".repeat(pad) + RESET);
        System.out.println();
    }

    public static void subHeader(String title) {
        System.out.println(BOLD + "  ── " + title + RESET);
        System.out.println();
    }

    public static void divider() {
        System.out.println("  " + DIM + "─".repeat(56) + RESET);
    }

    public static void blankLine() {
        System.out.println();
    }



    public static void prompt(String label) {
        System.out.print("  " + BOLD + label + ": " + RESET);
    }



    public static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}