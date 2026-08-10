 public int binarySearch(int[] arr, int target) {
        // Binary search only works correctly on a sorted array,
        // so we sort a COPY of it first (this does not affect the caller's array).
        int[] sortedArr = Arrays.copyOf(arr, arr.length);
        Arrays.sort(sortedArr);

        int low = 0;                     // left boundary of the search range
        int high = sortedArr.length - 1; // right boundary of the search range

        // Keep searching while there is still a valid range to check
        while (low <= high) {
            // Find the middle index of the current range
            // (low + high) / 2 can overflow for huge arrays, so we use this safer form:
            int mid = low + (high - low) / 2;

            if (sortedArr[mid] == target) {
                // Middle element is the target -> found it
                return mid;
            } else if (sortedArr[mid] < target) {
                // Target must be in the right half, so move 'low' up
                low = mid + 1;
            } else {
                // Target must be in the left half, so move 'high' down
                high = mid - 1;
            }
        }

        // low > high means the range is empty -> target not found
        return -1;
    }
}
