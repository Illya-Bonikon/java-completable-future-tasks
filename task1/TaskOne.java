package task1;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Завдання 1: Асинхронна генерація та модифікація масиву.
 * Демонстрація: supplyAsync, thenApplyAsync, thenAcceptAsync, thenRunAsync.
 */
public class TaskOne {

    // Функція для симуляції витрат часу
    private static long simulateTime(long startMillis, String taskName) {
        // Симулюємо асинхронну роботу
        try {
            TimeUnit.MILLISECONDS.sleep(200); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        long endMillis = System.currentTimeMillis();
        long duration = endMillis - startMillis;
        System.out.printf("   [Потік: %s] Час виконання '%s': %d мс\n", 
                          Thread.currentThread().getName(), taskName, duration);
        return endMillis;
    }

    // 1. Асинхронна генерація масиву (supplyAsync)
    private static CompletableFuture<int[]> generateArrayAsync(long initialStart) {
        Supplier<int[]> supplier = () -> {
            long start = System.currentTimeMillis();
            System.out.println("--- ЕТАП 1: Генерація масиву (supplyAsync) ---");
            int[] arr = new int[10];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = (int) (Math.random() * 100); // Генерація цілочисельного масиву
            }
            System.out.println("   Початковий масив (int): " + Arrays.toString(arr));
            simulateTime(start, "Генерація");
            return arr;
        };
        return CompletableFuture.supplyAsync(supplier);
    }

    // 2. Модифікація: +10 до кожного елемента (thenApplyAsync)
    private static CompletableFuture<int[]> addTenAsync(CompletableFuture<int[]> previousTask, long initialStart) {
        Function<int[], int[]> function = arr -> {
            long start = System.currentTimeMillis();
            System.out.println("\n--- ЕТАП 2: Додавання +10 (thenApplyAsync) ---");
            int[] result = Arrays.copyOf(arr, arr.length);
            for (int i = 0; i < result.length; i++) {
                result[i] += 10;
            }
            System.out.println("   Масив після +10 (int): " + Arrays.toString(result));
            simulateTime(start, "Додавання +10");
            return result;
        };
        return previousTask.thenApplyAsync(function);
    }

    // 3. Модифікація: Ділення на 2 (thenApplyAsync)
    private static CompletableFuture<Double[]> divideByTwoAsync(CompletableFuture<int[]> previousTask, long initialStart) {
        Function<int[], Double[]> function = arr -> {
            long start = System.currentTimeMillis();
            System.out.println("\n--- ЕТАП 3: Ділення на 2 (thenApplyAsync) ---");
            Double[] result = new Double[arr.length];
            for (int i = 0; i < arr.length; i++) {
                result[i] = arr[i] / 2.0; // Результат стає дробовим
            }
            simulateTime(start, "Ділення на 2");
            return result;
        };
        return previousTask.thenApplyAsync(function);
    }

    // 4. Виведення результату з інформаційним текстом (thenAcceptAsync)
    private static CompletableFuture<Void> displayResultAsync(CompletableFuture<Double[]> previousTask, long initialStart) {
        Consumer<Double[]> consumer = finalArr -> {
            long start = System.currentTimeMillis();
            System.out.println("\n--- ЕТАП 4: Виведення результату (thenAcceptAsync) ---");
            System.out.println("   РЕЗУЛЬТАТ ДІЛЕННЯ: " + Arrays.toString(finalArr));
            simulateTime(start, "Виведення результату");
        };
        return previousTask.thenAcceptAsync(consumer);
    }

    // 5. Фінальна дія (thenRunAsync)
    private static CompletableFuture<Void> finalActionAsync(CompletableFuture<Void> previousTask, long initialStart) {
        return previousTask.thenRunAsync(() -> {
            long end = System.currentTimeMillis();
            System.out.println("\n--- ЕТАП 5: Фінальна дія (thenRunAsync) ---");
            System.out.printf("   Загальний час виконання всіх асинхронних операцій: %d мс\n", (end - initialStart));
        });
    }

    public static void main(String[] args) {
        System.out.println("=== ЗАПУСК ЗАВДАННЯ 1: ЛАНЦЮЖОК CompletableFuture ===");
        
        long initialStart = System.currentTimeMillis();
        
        // Будуємо асинхронний ланцюжок
        CompletableFuture<Void> finalFuture = generateArrayAsync(initialStart)
            .thenCompose(arr -> addTenAsync(CompletableFuture.completedFuture(arr), initialStart))
            .thenCompose(arrPlusTen -> divideByTwoAsync(CompletableFuture.completedFuture(arrPlusTen), initialStart))
            .thenCompose(arrDivided -> displayResultAsync(CompletableFuture.completedFuture(arrDivided), initialStart))
            .thenCompose(v -> finalActionAsync(CompletableFuture.completedFuture(v), initialStart));

        // Очікуємо завершення всього ланцюжка
        // Використовуємо join() для блокування головного потоку до завершення всіх асинхронних завдань
        // (у реальному застосуванні, можливо, краще уникнути блокування, але тут це потрібно для демонстрації)
        try {
            finalFuture.join();
        } catch (Exception e) {
            System.err.println("Виникла помилка в асинхронному ланцюжку: " + e.getMessage());
        }

        System.out.println("\n=== ЗАВДАННЯ 1 ВИКОНАНО ===");
    }
}