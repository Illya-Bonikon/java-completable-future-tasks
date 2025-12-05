import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Завдання 2: Асинхронне обчислення добутку різниць послідовності.
 * Демонстрація: supplyAsync, thenApplyAsync.
 */
public class TaskTwo {

    // Функція для симуляції витрат часу
    private static long simulateTime(long startMillis, String taskName) {
        // Симулюємо асинхронну роботу
        try {
            TimeUnit.MILLISECONDS.sleep(300); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        long endMillis = System.currentTimeMillis();
        long duration = endMillis - startMillis;
        System.out.printf("   [Потік: %s] Час виконання '%s': %d мс\n", 
                          Thread.currentThread().getName(), taskName, duration);
        return endMillis;
    }

    // 1. Асинхронна генерація послідовності (supplyAsync)
    private static CompletableFuture<double[]> generateSequenceAsync() {
        Supplier<double[]> supplier = () -> {
            long start = System.currentTimeMillis();
            System.out.println("--- ЕТАП 1: Генерація послідовності (supplyAsync) ---");
            final int SIZE = 20;
            double[] sequence = new double[SIZE];
            for (int i = 0; i < SIZE; i++) {
                sequence[i] = Math.random() * 10.0; // Генеруємо дійсні числа
            }
            System.out.println("   Початкова послідовність: " + Arrays.toString(sequence));
            simulateTime(start, "Генерація послідовності");
            return sequence;
        };
        return CompletableFuture.supplyAsync(supplier);
    }

    // 2. Асинхронне обчислення добутку різниць (thenApplyAsync)
    private static CompletableFuture<Double> calculateProductAsync(CompletableFuture<double[]> previousTask) {
        Function<double[], Double> function = sequence -> {
            long start = System.currentTimeMillis();
            System.out.println("\n--- ЕТАП 2: Обчислення добутку різниць (thenApplyAsync) ---");
            
            if (sequence.length < 2) {
                simulateTime(start, "Обчислення добутку");
                return 0.0; // Недостатньо елементів для обчислення
            }
            
            double product = 1.0;
            for (int i = 1; i < sequence.length; i++) {
                product *= (sequence[i] - sequence[i - 1]);
            }
            
            simulateTime(start, "Обчислення добутку");
            return product;
        };
        return previousTask.thenApplyAsync(function);
    }

    // 3. Асинхронне виведення результату та часу (thenAcceptAsync)
    private static CompletableFuture<Void> displayResultAndTimeAsync(CompletableFuture<Double> previousTask, long initialStart) {
        return previousTask.thenAcceptAsync(result -> {
            long start = System.currentTimeMillis();
            System.out.println("\n--- ЕТАП 3: Виведення результату та часу (thenAcceptAsync) ---");
            
            long end = System.currentTimeMillis();
            long totalDuration = end - initialStart;

            System.out.printf("   РЕЗУЛЬТАТ ОБЧИСЛЕННЯ: %.4f\n", result);
            System.out.printf("   ЧАС РОБОТИ УСІХ АСИНХРОННИХ ОПЕРАЦІЙ: %d мс\n", totalDuration);
            simulateTime(start, "Виведення результату і часу");
        });
    }

    public static void main(String[] args) {
        System.out.println("=== ЗАПУСК ЗАВДАННЯ 2: ОБЧИСЛЕННЯ ДОБУТКУ РІЗНИЦЬ ===");
        
        long initialStart = System.currentTimeMillis();
        
        // Будуємо асинхронний ланцюжок
        CompletableFuture<Void> finalFuture = generateSequenceAsync()
            .thenCompose(seq -> calculateProductAsync(CompletableFuture.completedFuture(seq)))
            .thenCompose(product -> displayResultAndTimeAsync(CompletableFuture.completedFuture(product), initialStart));

        // Очікуємо завершення всього ланцюжка
        try {
            finalFuture.join();
        } catch (Exception e) {
            System.err.println("Виникла помилка в асинхронному ланцюжку: " + e.getMessage());
        }
        
        System.out.println("\n=== ЗАВДАННЯ 2 ВИКОНАНО ===");
    }
}