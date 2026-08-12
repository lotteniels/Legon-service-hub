package com.campushub.util;

import com.campushub.structures.linear.DynamicArray;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

// Owner: Graphs and Optimization

/**
 * Reader for the Database pod's CSV exports.
 *
 * <p>Written by hand rather than with a library because the exports are not plain
 * comma-splitting: several campus location names contain commas inside quotes, the
 * files carry a UTF-8 byte-order mark, and line endings are CRLF.
 */
public final class Csv {

    private Csv() {
    }

    /**
     * Every row of {@code file}, header included, split into fields.
     *
     * @throws UncheckedIOException if the file cannot be read
     */
    public static DynamicArray<String[]> rows(Path file) {
        String text;
        try {
            text = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
        } catch (IOException cause) {
            throw new UncheckedIOException("cannot read " + file, cause);
        }
        if (!text.isEmpty() && text.charAt(0) == '﻿') {
            text = text.substring(1);
        }

        DynamicArray<String[]> rows = new DynamicArray<>();
        DynamicArray<String> currentRow = new DynamicArray<>();
        StringBuilder cell = new StringBuilder();
        boolean insideQuotes = false;

        for (int position = 0; position < text.length(); position++) {
            char character = text.charAt(position);

            if (insideQuotes) {
                if (character != '"') {
                    cell.append(character);
                } else if (position + 1 < text.length() && text.charAt(position + 1) == '"') {
                    cell.append('"');
                    position++;
                } else {
                    insideQuotes = false;
                }
                continue;
            }

            if (character == '"') {
                insideQuotes = true;
            } else if (character == ',') {
                currentRow.add(cell.toString());
                cell.setLength(0);
            } else if (character == '\n') {
                currentRow.add(cell.toString());
                cell.setLength(0);
                rows.add(toArray(currentRow));
                currentRow = new DynamicArray<>();
            } else if (character != '\r') {
                cell.append(character);
            }
        }

        if (cell.length() > 0 || !currentRow.isEmpty()) {
            currentRow.add(cell.toString());
            rows.add(toArray(currentRow));
        }
        return rows;
    }

    /**
     * Position of the first of {@code names} present in {@code header}.
     *
     * <p>Several names are accepted because the same field is not always labelled
     * consistently between the CSV export and {@code schema.sql}.
     *
     * @throws IllegalArgumentException if none of them is present
     */
    public static int requiredColumn(String[] header, Path source, String... names) {
        for (String name : names) {
            int found = column(header, name);
            if (found >= 0) {
                return found;
            }
        }
        StringBuilder wanted = new StringBuilder();
        for (int index = 0; index < names.length; index++) {
            wanted.append(index == 0 ? "" : " or ").append('\'').append(names[index]).append('\'');
        }
        throw new IllegalArgumentException(
                "column " + wanted + " missing from " + source.getFileName());
    }

    /** Position of {@code name} in {@code header}, or -1 if absent. */
    public static int column(String[] header, String name) {
        for (int index = 0; index < header.length; index++) {
            if (header[index].trim().equalsIgnoreCase(name)) {
                return index;
            }
        }
        return -1;
    }

    /** Trimmed value at {@code column}, or null when absent or blank. */
    public static String field(String[] row, int column) {
        if (column < 0 || column >= row.length) {
            return null;
        }
        String value = row[column].trim();
        return value.isEmpty() ? null : value;
    }

    /**
     * Trimmed value at {@code column}.
     *
     * @throws IllegalArgumentException if it is absent or blank
     */
    public static String requiredField(String[] row, int column, Path source, String name) {
        String value = field(row, column);
        if (value == null) {
            throw new IllegalArgumentException(
                    "blank '" + name + "' in " + source.getFileName());
        }
        return value;
    }

    /** True if every cell in the row is blank. */
    public static boolean isBlankRow(String[] row) {
        for (String cell : row) {
            if (!cell.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static String[] toArray(DynamicArray<String> cells) {
        String[] array = new String[cells.size()];
        for (int index = 0; index < cells.size(); index++) {
            array[index] = cells.get(index);
        }
        return array;
    }
}
