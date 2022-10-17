package dev.nicotopia.nonogram;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 * Hello world!
 *
 */
public class App {
    private interface ReadInts {
        public int[] apply(BufferedReader br) throws IOException;
    }

    public static void main(String[] args) {
        run();
        System.exit(0);
        Nonogram nonogram = new Nonogram(5, 5);
        nonogram.setRowRule(0, Arrays.asList(2));
        nonogram.setRowRule(1, Arrays.asList(2));
        nonogram.setRowRule(2, Arrays.asList(2));
        nonogram.setRowRule(3, Arrays.asList(4));
        nonogram.setRowRule(4, Arrays.asList(3));
        nonogram.setColumnRule(0, Arrays.asList(3));
        nonogram.setColumnRule(1, Arrays.asList(3));
        nonogram.setColumnRule(2, Arrays.asList(2));
        nonogram.setColumnRule(3, Arrays.asList(2, 1));
        nonogram.setColumnRule(4, Arrays.asList(2));
        //nonogram.solve();
        //nonogram.printSolution(System.out);
        //nonogram.reset();
        DeductiveSolver solver = new DeductiveSolver(nonogram);
        solver.solve();
    }

    private static void run() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.print("Row and column count: ");
            int rowCol[] = readIntegers(br);
            Nonogram nonogram = new Nonogram(rowCol[0], rowCol[1]);
            for (;;) {
                readAndSolve(br, nonogram);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void readAndSolve(BufferedReader br, Nonogram nonogram) throws IOException {
        nonogram.reset();
        ReadInts readInts = nonogram.getRowCount() < 10 && nonogram.getColumnCount() < 10 ? App::readSingleDigitIntegers
                : App::readIntegers;
        for (int i = 0; i < nonogram.getRowCount(); ++i) {
            System.out.printf("Row %2d: ", i);
            int rule[] = readInts.apply(br);
            if (rule == null) {
                i = i == 0 ? -1 : i - 2;
            } else {
                nonogram.setRowRule(nonogram.getRowCount() - 1 - i, Arrays.stream(rule).boxed().toList());
            }
        }
        for (int i = 0; i < nonogram.getColumnCount(); ++i) {
            System.out.printf("Col %2d: ", i);
            int rule[] = readInts.apply(br);
            if (rule == null) {
                i = i == 0 ? -1 : i - 2;
            } else {
                int reversed[] = new int[rule.length];
                for (int j = 0; j < rule.length; ++j) {
                    reversed[rule.length - 1 - j] = rule[j];
                }
                nonogram.setColumnRule(i, Arrays.stream(reversed).boxed().toList());
            }
        }
        DeductiveSolver solver = new DeductiveSolver(nonogram);
        solver.solve();
        nonogram.printSolution(System.out);
    }

    private static int[] readIntegers(BufferedReader br) throws IOException {
        String line = br.readLine();
        if (line == null || line.isBlank()) {
            return null;
        }
        String ints[] = line.split("\\s+");
        int result[] = new int[ints.length];
        for (int i = 0; i < ints.length; ++i) {
            try {
                result[i] = Integer.valueOf(ints[i]);
            } catch (NumberFormatException ex) {
                System.err.printf("%s is no integer\n", ints[i]);
                return null;
            }
        }
        return result;
    }

    private static int[] readSingleDigitIntegers(BufferedReader br) throws IOException {
        String ints = br.readLine();
        int result[] = new int[ints.length()];
        for (int i = 0; i < ints.length(); ++i) {
            result[i] = ints.charAt(i) - '0';
            if (result[i] < 0 || 9 < result[i]) {
                System.err.printf("%s is no single digit integer\n", ints.charAt(i));
                return null;
            }
        }
        return result;
    }
}
