package com.github.shynixn.blockball.lib;

import java.util.*;

public final class SMathUtils {
    public static boolean tryPInt(String value) {
        try {
            Integer.parseInt(value);
        } catch (final NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static boolean tryPDouble(String value) {
        try {
            Double.parseDouble(value);
        } catch (final NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static <K, V extends Comparable<? super V>> List<Map.Entry<K, V>> entriesSortedByValues(Map<K, V> map) {
        final List<Map.Entry<K, V>> sortedEntries = new ArrayList<>(map.entrySet());
        Collections.sort(sortedEntries,
                new Comparator<Map.Entry<K, V>>() {
                    @Override
                    public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2) {
                        return e2.getValue().compareTo(e1.getValue());
                    }
                }
        );
        return sortedEntries;
    }
}
