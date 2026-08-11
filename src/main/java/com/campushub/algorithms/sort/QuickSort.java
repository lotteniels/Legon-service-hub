public int[] quickSort(int[] arr) {
        int[] a = arr.clone();
        quickSortHelper(a, 0, a.length - 1);
        return a;
    }

    private void quickSortHelper(int[] a, int low, int high) {
        if (low < high) {
            // Partition the array and get the pivot's final resting index
            int pivotIndex = partition(a, low, high);

            quickSortHelper(a, low, pivotIndex - 1);  // sort elements before pivot
            quickSortHelper(a, pivotIndex + 1, high);  // sort elements after pivot
        }
    }

    // Rearranges a[low..high], places the pivot (last element) in its correct
    // sorted position, and returns that final index.
    private int partition(int[] a, int low, int high) {
        int pivot = a[high]; // choose the last element as the pivot
        int i = low - 1;     // index of the last element smaller than the pivot

        for (int j = low; j < high; j++) {
            if (a[j] < pivot) {
                i++;
                // swap a[i] and a[j]
                int temp = a[i];
                a[i] = a[j];
                a[j] = temp;
            }
        }

        // Place the pivot right after the last smaller element
        int temp = a[i + 1];
        a[i + 1] = a[high];
        a[high] = temp;

        return i + 1; // this is the pivot's correct sorted index
    }
}
