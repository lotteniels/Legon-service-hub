public class SearchAlgorithms {

    // LINEAR SEARCH
    // Checks every element one by one until it finds the target
    // or reaches the end of the array. Works on sorted OR unsorted arrays.
    public int linearSearch(int[] arr, int target) {
        // Loop through every index of the array, from 0 to arr.length - 1
        for (int i = 0; i < arr.length; i++) {
            // Compare the value at the current index with the target
            if (arr[i] == target) {
                // Found it! Return the index immediately.
                return i;
            }
        }
        // If we finish the loop without returning, the target isn't in the array
        return -1;
    }
