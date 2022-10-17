package dev.nicotopia.nonogram;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public class Nonogram {
    public interface ChangeListener {
        public void onSet(int row, int col, State state);
    }

    public enum State {
        SET, EMPTY, UNKNOWN
    }

    private final State[][] game;
    private final List<List<Integer>> rowRules = new LinkedList<>();
    private final List<List<Integer>> columnRules = new LinkedList<>();
    private final List<ChangeListener> changeListeners = new LinkedList<>();

    public Nonogram(int rowCount, int columnCount) {
        this.game = new State[rowCount][columnCount];
        for (int r = 0; r < rowCount; ++r) {
            Arrays.fill(this.game[r], State.UNKNOWN);
            this.rowRules.add(Arrays.asList(0));
        }
        for (int c = 0; c < columnCount; ++c) {
            this.columnRules.add(Arrays.asList(0));
        }
    }

    public int getColumnCount() {
        return this.game[0].length;
    }

    public int getRowCount() {
        return this.game.length;
    }

    public State[] getRow(int row) {
        return Arrays.copyOf(this.game[row], this.getColumnCount());
    }

    public State[] getColumn(int col) {
        State[] column = new State[this.getRowCount()];
        for (int i = 0; i < column.length; ++i) {
            column[i] = this.get(i, col);
        }
        return column;
    }

    public List<Integer> getRowRule(int rowIdx) {
        return Collections.unmodifiableList(this.rowRules.get(rowIdx));
    }

    public List<Integer> getColumnRule(int colIdx) {
        return Collections.unmodifiableList(this.columnRules.get(colIdx));
    }

    public void reset() {
        for (int r = 0; r < this.getRowCount(); ++r) {
            Arrays.fill(this.game[r], State.UNKNOWN);
        }
    }

    public State get(int row, int col) {
        return this.game[row][col];
    }

    public void set(int row, int col, State state) {
        if (this.game[row][col] != state) {
            this.game[row][col] = state;
            this.changeListeners.forEach(l -> l.onSet(row, col, state));
        }
    }

    public void setRowRule(int row, List<Integer> rule) {
        this.rowRules.set(row, rule);
    }

    public void setColumnRule(int col, List<Integer> rule) {
        this.columnRules.set(col, rule);
    }

    public boolean solve() {
        return this.solve(0);
    }

    private boolean solve(int row) {
        if (row == this.getRowCount()) {
            return true;
        }
        List<Integer> rule = this.rowRules.get(row);
        int[] distribution = new int[rule.size() == 1 && rule.get(0) == 0 ? 1 : rule.size() + 1];
        int setSum = this.rowRules.get(row).stream().mapToInt(Integer::valueOf).sum();
        distribution[0] = this.getColumnCount() - (this.rowRules.get(row).size() - 1) - setSum;
        do {
            this.applyDistribution(row, distribution);
            //5this.printSolution(System.out);
            if (this.checkColumns() && this.solve(row + 1)) {
                return true;
            }
            //System.out.println("nope!");
        } while (this.nextDistribution(distribution));
        Arrays.fill(this.game[row], State.UNKNOWN);
        return false;
    }

    private boolean nextDistribution(int distribution[]) {
        if (distribution.length == 1) {
            return false;
        }

        int endVal = distribution[distribution.length - 1];
        distribution[distribution.length - 1] = 0;
        int i = distribution.length - 2;
        while (i != 0 && distribution[i] == 0) {
            --i;
        }
        if (i == 0 && distribution[0] == 0) {
            return false;
        }
        --distribution[i];
        distribution[i + 1] = 1 + endVal;
        return true;
    }

    private void applyDistribution(int row, int[] distribution) {
        List<Integer> rule = this.rowRules.get(row);
        int rulePos = 0;
        int col = 0;
        for (int i = 0; i < distribution.length; ++i) {
            Arrays.fill(this.game[row], col, col + distribution[i], State.EMPTY);
            col += distribution[i];
            if (rulePos != rule.size()) {
                Arrays.fill(this.game[row], col, col + rule.get(rulePos), State.SET);
                col += rule.get(rulePos);
                if (++rulePos != rule.size()) {
                    this.game[row][col++] = State.EMPTY;
                }
            }
        }
    }

    private List<Integer> buildRule(List<State> values) {
        List<Integer> rule = new LinkedList<>();
        int count = 0;
        for (State state : values) {
            if (state == State.SET) {
                ++count;
            } else if (state == State.EMPTY && count != 0) {
                rule.add(count);
                count = 0;
            } else if (state == State.UNKNOWN) {
                break;
            }
        }
        if (count != 0 || rule.isEmpty()) {
            rule.add(count);
        }
        return rule;
    }

    private boolean checkRule(List<Integer> rule, List<State> values) {
        List<Integer> builtRule = this.buildRule(values);
        return builtRule.size() <= rule.size()
                && rule.subList(0, builtRule.size() - 1).equals(builtRule.subList(0, builtRule.size() - 1))
                && builtRule.get(builtRule.size() - 1) <= rule.get(builtRule.size() - 1);
    }

    private boolean checkColumns() {
        for (int col = 0; col < this.getColumnCount(); ++col) {
            final int c0 = col;
            List<State> column = Arrays.stream(this.game).map(r -> r[c0]).toList();
            if (!this.checkRule(this.columnRules.get(col), column)) {
                return false;
            }
        }
        return true;
    }

    public void printSolution(PrintStream out) {
        out.print("┌");
        out.print("───┬".repeat(this.getColumnCount() - 1));
        out.println("───┐");
        for (int r = 0; r < this.getRowCount(); ++r) {
            if (r != 0) {
                out.print("├");
                out.print("───┼".repeat(this.getColumnCount() - 1));
                out.println("───┤");
            }
            Stream<String> innerRowStr = Arrays.stream(this.game[r])
                    .map(s -> s == State.SET ? "█" : s == State.EMPTY ? " " : "?");
            out.printf("│ %s │\n", String.join(" │ ", innerRowStr.toList()));
        }
        out.print("└");
        out.print("───┴".repeat(this.getColumnCount() - 1));
        out.println("───┘");
    }

    public void addChangeListener(ChangeListener l) {
        this.changeListeners.add(l);
    }

    public void removeChangeListener(ChangeListener l) {
        this.changeListeners.remove(l);
    }
}
