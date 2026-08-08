public int[] mergeSort(int[] arr) {
        int[] a = arr.clone();
        if (a.length <= 1) {
            // An array of 0 or 1 elements is already sorted
            return a;
        }
        mergeSortHelper(a, 0, a.length - 1);
        return a;
    }

    // Recursively splits the array between indices left and right
    private void mergeSortHelper(int[] a, int left, int right) {
        if (left < right) {
            int mid = left + (right - left) / 2; // midpoint, overflow-safe

            mergeSortHelper(a, left, mid);       // sort left half
            mergeSortHelper(a, mid + 1, right);  // sort right half
            merge(a, left, mid, right);          // merge the two sorted halves
        }
    }

    // Merges two sorted sub-arrays: a[left..mid] and a[mid+1..right]
    private void merge(int[] a, int left, int mid, int right) {
        int n1 = mid - left + 1;   // size of left sub-array
        int n2 = right - mid;      // size of right sub-array

        int[] leftArr = new int[n1];
        int[] rightArr = new int[n2];

        // Copy data into the two temporary arrays
        for (int i = 0; i < n1; i++) leftArr[i] = a[left + i];
        for (int j = 0; j < n2; j++) rightArr[j] = a[mid + 1 + j];

        int i = 0, j = 0;       // pointers for leftArr and rightArr
        int k = left;           // pointer for the main array 'a'

        // Compare elements from both sub-arrays and place the smaller one back into 'a'
        while (i < n1 && j < n2) {
            if (leftArr[i] <= rightArr[j]) {
                a[k] = leftArr[i];
                i++;
            } else {
                a[k] = rightArr[j];
                j++;
            }
            k++;
        }

        // Copy any remaining elements from leftArr (if rightArr ran out first)
        while (i < n1) {
            a[k] = leftArr[i];
            i++;
            k++;
        }

        // Copy any remaining elements from rightArr (if leftArr ran out first)
        while (j < n2) {
            a[k] = rightArr[j];
            j++;
            k++;
        }
    }
