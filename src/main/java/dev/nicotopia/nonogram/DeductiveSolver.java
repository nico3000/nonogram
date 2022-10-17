package dev.nicotopia.nonogram;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

import dev.nicotopia.nonogram.Nonogram.State;

public class DeductiveSolver implements Nonogram.ChangeListener {
    private final Nonogram game;
    private final List<List<Nonogram.State[]>> possibleRows = new LinkedList<>();
    private final List<List<Nonogram.State[]>> possibleColumns = new LinkedList<>();
    private final Set<Integer> rowsToCheck = new HashSet<>();
    private final Set<Integer> colsToCheck = new HashSet<>();

    public DeductiveSolver(Nonogram game) {
        this.game = game;
        this.game.addChangeListener(this);
        for (int r = 0; r < this.game.getRowCount(); ++r) {
            this.possibleRows.add(this.getPossible(this.game.getRowRule(r), this.game.getColumnCount()));
            this.updatePossibleRows(r);
        }
        for (int c = 0; c < this.game.getColumnCount(); ++c) {
            this.possibleColumns.add(this.getPossible(this.game.getColumnRule(c), this.game.getRowCount()));
            this.updatePossibleColumns(c);
        }
    }

    public boolean solve() {
        this.rowsToCheck.addAll(IntStream.range(0, this.possibleRows.size()).mapToObj(Integer::valueOf).toList());
        this.colsToCheck.addAll(IntStream.range(0, this.possibleColumns.size()).mapToObj(Integer::valueOf).toList());
        while (!this.rowsToCheck.isEmpty() || !this.colsToCheck.isEmpty()) {
            while (!this.rowsToCheck.isEmpty()) {
                Iterator<Integer> iter = this.rowsToCheck.iterator();
                int r = iter.next();
                iter.remove();
                if (this.possibleRows.get(r).isEmpty()) {
                    return false;
                }
                Nonogram.State[] current = this.game.getRow(r);
                Nonogram.State[] common = this.getCommonStates(this.possibleRows.get(r).iterator());
                this.queryCommon(current, common, (i, s) -> this.game.set(r, i, s));
            }
            while (!this.colsToCheck.isEmpty()) {
                Iterator<Integer> iter = this.colsToCheck.iterator();
                int c = iter.next();
                iter.remove();
                if (this.possibleColumns.get(c).isEmpty()) {
                    return false;
                }
                Nonogram.State[] current = this.game.getColumn(c);
                Nonogram.State[] common = this.getCommonStates(this.possibleColumns.get(c).iterator());
                this.queryCommon(current, common, (i, s) -> this.game.set(i, c, s));
            }
        }
        return true;
    }

    private Nonogram.State[] getCommonStates(Iterator<Nonogram.State[]> iterator) {
        Nonogram.State[] common = this.copyStates(iterator.next());
        while (iterator.hasNext()) {
            Nonogram.State[] next = iterator.next();
            for (int i = 0; i < common.length; ++i) {
                if (next[i] != common[i]) {
                    common[i] = State.UNKNOWN;
                }
            }
        }
        return common;
    }

    private Nonogram.State[] copyStates(Nonogram.State[] states) {
        return Arrays.copyOf(states, states.length);
    }

    private void queryCommon(Nonogram.State[] current, Nonogram.State[] common,
            BiConsumer<Integer, Nonogram.State> onChange) {
        for (int i = 0; i < common.length; ++i) {
            if (common[i] != State.UNKNOWN && common[i] != current[i]) {
                onChange.accept(i, common[i]);
            }
        }
    }

    @Override
    public void onSet(int row, int col, Nonogram.State state) {
        if (this.possibleRows.get(row).removeIf(s -> s[col] != state)) {
            this.rowsToCheck.add(row);
        }
        if (this.possibleColumns.get(col).removeIf(s -> s[row] != state)) {
            this.colsToCheck.add(col);
        }
    }

    private List<Nonogram.State[]> getPossible(List<Integer> rule, int size) {
        int distribution[] = new int[rule.get(0) == 0 ? 1 : rule.size() + 1];
        long sum = rule.stream().mapToInt(Integer::valueOf).sum();
        distribution[0] = size - ((int) sum + rule.size() - 1);
        List<Nonogram.State[]> possibilities = new LinkedList<>();
        do {
            possibilities.add(this.distributionToStateArray(distribution, rule, size));
        } while (this.nextDistribution(distribution));
        return possibilities;
    }

    private Nonogram.State[] distributionToStateArray(int[] distribution, List<Integer> rule, int size) {
        Nonogram.State[] state = new Nonogram.State[size];
        Arrays.fill(state, Nonogram.State.EMPTY);
        int pos = 0;
        for (int i = 0; i < rule.size(); ++i) {
            int base = pos + distribution[distribution.length - 1 - i];
            Arrays.fill(state, base, base + rule.get(i), Nonogram.State.SET);
            pos = base + rule.get(i) + 1;
        }
        return state;
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

    private void updatePossibleRows(int row) {
        Iterator<Nonogram.State[]> iter = this.possibleRows.get(row).iterator();
        while (iter.hasNext()) {
            Nonogram.State[] state = iter.next();
            for (int i = 0; i < state.length; ++i) {
                if (this.game.get(row, i) != State.UNKNOWN && this.game.get(row, i) != state[i]) {
                    iter.remove();
                    break;
                }
            }
        }
    }

    private void updatePossibleColumns(int col) {
        Iterator<Nonogram.State[]> iter = this.possibleColumns.get(col).iterator();
        while (iter.hasNext()) {
            Nonogram.State[] state = iter.next();
            for (int i = 0; i < state.length; ++i) {
                if (this.game.get(i, col) != State.UNKNOWN && this.game.get(i, col) != state[i]) {
                    iter.remove();
                    break;
                }
            }
        }
    }
}
