def binary_search(arr, target):
    """
    Search for target in a sorted array using binary search.
    Returns the index of target if found, else -1.
    """
    left, right = 0, len(arr) - 1

    while left <= right:
        mid = left + (right - left) // 2  # avoids overflow in other languages

        if arr[mid] == target:
            return mid
        elif arr[mid] < target:
            left = mid + 1
        else:
            right = mid - 1

    return -1
