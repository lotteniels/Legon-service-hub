public int[] selectionSort(int[] arr) {
        int[] a = arr.clone();
        int n = a.length;

        for (int i = 0; i < n - 1; i++) {
            int minIndex = i; // assume the current position holds the smallest value

            // Search the rest of the array for a smaller value
            for (int j = i + 1; j < n; j++) {
                if (a[j] < a[minIndex]) {
                    minIndex = j; // found a new smallest value
                }
            }

            // Swap the found minimum with the element at position i
            int temp = a[minIndex];
            a[minIndex] = a[i];
            a[i] = temp;
        }
        return a;
    }
