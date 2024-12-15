//Assignment 1 : Mohammed Razeen Rafeek 
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PalindromeSearch {
    private static final int ROWS = 1000;
    private static final int COLUMNS = 1000;
    private static final char[][] matrix = new char[ROWS][COLUMNS];
    private static final Random rand = new Random();

    public static void main(String[] args) {
        generateMatrix();
        int[] lengths = {3, 4, 5, 6};
        for (int threads = 1; threads <= 8; threads++) {
            System.out.println("***************************************************************************");
            for (int length : lengths) {
                long startTime = System.nanoTime();
                int count = searchPalindromes(length, threads);
                long duration = System.nanoTime() - startTime;
                double seconds = duration / 1e9;
                System.out.printf("%d palindromes of size %d found in %.6f s using %d threads.\n",
                        count, length, seconds, threads);
            }
        }
    }

    // Generates a 1000x1000 matrix of random characters from 'a' to 'z'
    private static void generateMatrix() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                matrix[i][j] = (char) (rand.nextInt(26) + 'a');
            }
        }
    }

    // Check if a given string is a palindrome
    private static boolean isPalindrome(String str) {
        int length = str.length();
        for (int i = 0; i < length / 2; i++) {
            if (str.charAt(i) != str.charAt(length - i - 1)) {
                return false;
            }
        }
        return true;
    }

    // Searches for palindromes of specified length using the given number of threads
    private static int searchPalindromes(int length, int numThreads) {
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        final int[] count = {0};

        for (int t = 0; t < numThreads; t++) {
            final int threadId = t;
            executor.execute(() -> {
                int localCount = 0;
                for (int i = threadId; i < ROWS; i += numThreads) {
                    for (int j = 0; j < COLUMNS; j++) {
                        // Right to Left
                        if (j <= COLUMNS - length) {
                            String right = getString(i, j, 0, 1, length);
                            if (isPalindrome(right)) localCount++;
                        }
                        // Up to Down
                        if (i <= ROWS - length) {
                            String down = getString(i, j, 1, 0, length);
                            if (isPalindrome(down)) localCount++;
                        }
                        // Diagonally Down-Right
                        if (i <= ROWS - length && j <= COLUMNS - length) {
                            String diagonal = getString(i, j, 1, 1, length);
                            if (isPalindrome(diagonal)) localCount++;
                        }
                    }
                }
                synchronized (count) {
                    count[0] += localCount;
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return count[0];
    }

    // Helper function to extract a string from the matrix in a specified direction
    private static String getString(int x, int y, int dx, int dy, int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(matrix[x + i * dx][y + i * dy]);
        }
        return sb.toString();
    }
}
