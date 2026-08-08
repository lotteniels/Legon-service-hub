 public int[] insertionSort(int[] arr) {
        int[] a = arr.clone();

        // Start from the second element (index 1) since a single element is "sorted"
        for (int i = 1; i < a.length; i++) {
            int key = a[i];     // the value we're trying to insert correctly
            int j = i - 1;      // start comparing with the element just before it

            // Shift elements bigger than 'key' one position to the right
            while (j >= 0 && a[j] > key) {
                a[j + 1] = a[j];
                j--;
            }

            // Place 'key' into its correct spot
            a[j + 1] = key;
        }
        return a;
    }
