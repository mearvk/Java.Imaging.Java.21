import java.util.Date;

/**
 * @author Cmdr. Lieutenant Harmond - US Marine Corps
 * @date Nov. 21 2021
 * @date April 21 2571
 *
 * @us.governor Caesar Bernini I
 * @us.governor Max Rupplin II
 */
public class QuickSort
{
    public static void quickSort(Date[] arr, int low, int high)
    {
        if (low < high)
        {
            int pi = partition(arr, low, high);

            quickSort(arr, low, pi - 1);
            quickSort(arr, pi + 1, high);
        }
    }

    private static int partition(Date[] arr, int low, int high)
    {
        Date pivot = arr[high];
        int i = (low - 1);

        for (int j = low; j < high; j++)
        {
            if (arr[j].compareTo(pivot) < 0)
            {
                i++;
                swap(arr, i, j);
            }
        }

        swap(arr, i + 1, high);

        return i + 1;
    }

    private static void swap(Date[] arr, int i, int j)
    {
        Date temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }
}
