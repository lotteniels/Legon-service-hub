package com.campushub.algorithms.sort;

public class QuickSort {

    public int[] quickSort(int[] arr) {
        int[] a = arr.clone();
        quickSortHelper(a, 0, a.length - 1);
        return a;
    }

    private void quickSortHelper(int[] a, int low, int high) {
        if (low < high) {
            int pivotIndex = partition(a, low, high);

            quickSortHelper(a, low, pivotIndex - 1);
            quickSortHelper(a, pivotIndex + 1, high);
        }
    }

    private int partition(int[] a, int low, int high) {
        int pivot = a[high];
        int i = low - 1;

        for (int j = low; j < high; j++) {
            if (a[j] < pivot) {
                i++;
                int temp = a[i];
                a[i] = a[j];
                a[j] = temp;
            }
        }

        int temp = a[i + 1];
        a[i + 1] = a[high];
        a[high] = temp;

        return i + 1;
    }
}

