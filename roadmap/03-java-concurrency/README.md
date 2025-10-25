# 03. Java 동시성과 멀티스레딩

> **학습 기간**: 1-2주
> **난이도**: ⭐⭐⭐⭐☆
> **전제 조건**: Java 기초, Golang goroutine 또는 C++ 멀티스레딩 경험

## 📚 학습 목표

Golang의 goroutine + channel, C++의 스레드 경험을 활용하여 Java의 동시성 모델을 이해하고 실무에 적용합니다.

## 🎯 핵심 개념 비교

### Java vs Golang vs C++ 동시성 모델

| 개념 | Java | Golang | C++ |
|------|------|--------|-----|
| **기본 단위** | Thread (OS 스레드) | Goroutine (경량 스레드) | std::thread (OS 스레드) |
| **생성 비용** | 높음 (~1MB 스택) | 낮음 (~2KB 스택) | 높음 |
| **통신 방식** | 공유 메모리 + Lock | Channel (CSP) | 공유 메모리 + mutex |
| **동기화** | synchronized, Lock | Channel, sync 패키지 | mutex, condition_variable |
| **스케줄러** | OS 스케줄러 | Go 런타임 스케줄러 | OS 스케줄러 |
| **비동기** | CompletableFuture | Goroutine + Channel | std::async, std::future |

---

## 1. Thread 기초

### 1.1 Thread 생성 및 실행

**방법 1: Thread 클래스 상속**
```java
public class MyThread extends Thread {
    @Override
    public void run() {
        for (int i = 0; i < 5; i++) {
            System.out.println(Thread.currentThread().getName() + ": " + i);
            try {
                Thread.sleep(1000);  // 1초 대기
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        MyThread thread1 = new MyThread();
        MyThread thread2 = new MyThread();

        thread1.start();  // 새 스레드에서 run() 실행
        thread2.start();
    }
}
```

**방법 2: Runnable 인터페이스 구현 (추천)**
```java
public class RunnableExample {
    public static void main(String[] args) {
        // 익명 클래스
        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Thread 1 running");
            }
        });

        // 람다 표현식
        Thread thread2 = new Thread(() -> {
            System.out.println("Thread 2 running");
        });

        thread1.start();
        thread2.start();

        // Thread join (대기)
        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```

**Golang 비교**:
```go
// Golang goroutine
go func() {
    fmt.Println("Goroutine running")
}()

// 대기
var wg sync.WaitGroup
wg.Add(1)
go func() {
    defer wg.Done()
    fmt.Println("Goroutine with WaitGroup")
}()
wg.Wait()
```

**C++ 비교**:
```cpp
// C++ std::thread
std::thread t1([]() {
    std::cout << "Thread running" << std::endl;
});

t1.join();  // 대기
```

---

## 2. 동기화 (Synchronization)

### 2.1 synchronized 키워드

**메서드 동기화**:
```java
public class Counter {
    private int count = 0;

    // 메서드 전체 동기화
    public synchronized void increment() {
        count++;
    }

    public synchronized int getCount() {
        return count;
    }
}

// 사용 예제
public class SynchronizedExample {
    public static void main(String[] args) throws InterruptedException {
        Counter counter = new Counter();

        // 100개 스레드가 각각 1000번 증가
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Thread thread = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    counter.increment();
                }
            });
            threads.add(thread);
            thread.start();
        }

        // 모든 스레드 대기
        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println("Final count: " + counter.getCount());  // 100000
    }
}
```

**블록 동기화**:
```java
public class BlockSynchronization {
    private final Object lock = new Object();
    private int count = 0;

    public void increment() {
        synchronized (lock) {
            count++;
        }
    }

    // 다른 락 객체 사용 가능
    private final Object lock1 = new Object();
    private final Object lock2 = new Object();

    public void method1() {
        synchronized (lock1) {
            // lock1으로 보호되는 코드
        }
    }

    public void method2() {
        synchronized (lock2) {
            // lock2로 보호되는 코드 (method1과 독립적)
        }
    }
}
```

**C++ 비교**:
```cpp
// C++ mutex
class Counter {
private:
    int count = 0;
    std::mutex mtx;

public:
    void increment() {
        std::lock_guard<std::mutex> lock(mtx);
        count++;
    }

    int getCount() {
        std::lock_guard<std::mutex> lock(mtx);
        return count;
    }
};
```

### 2.2 volatile 키워드

```java
public class VolatileExample {
    // volatile: 가시성 보장 (CPU 캐시가 아닌 메인 메모리에서 직접 읽기/쓰기)
    private volatile boolean running = true;

    public void start() {
        new Thread(() -> {
            while (running) {
                // 작업 수행
            }
            System.out.println("Thread stopped");
        }).start();
    }

    public void stop() {
        running = false;  // 모든 스레드에 즉시 반영
    }
}
```

**주의**: `volatile`은 가시성만 보장, 원자성은 보장하지 않음
```java
// 잘못된 예: volatile은 count++의 원자성을 보장하지 않음
private volatile int count = 0;

public void increment() {
    count++;  // 위험! (읽기 -> 증가 -> 쓰기는 원자적이지 않음)
}

// 올바른 방법: synchronized 또는 AtomicInteger 사용
```

---

## 3. java.util.concurrent 패키지

### 3.1 Lock 인터페이스

**ReentrantLock**: synchronized보다 유연한 락
```java
import java.util.concurrent.locks.*;

public class LockExample {
    private final Lock lock = new ReentrantLock();
    private int count = 0;

    public void increment() {
        lock.lock();
        try {
            count++;
        } finally {
            lock.unlock();  // 반드시 unlock (finally에서)
        }
    }

    // tryLock: 타임아웃 지원
    public boolean tryIncrement() {
        if (lock.tryLock()) {
            try {
                count++;
                return true;
            } finally {
                lock.unlock();
            }
        }
        return false;
    }

    // 타임아웃과 인터럽트 지원
    public void incrementWithTimeout() throws InterruptedException {
        if (lock.tryLock(1, TimeUnit.SECONDS)) {
            try {
                count++;
            } finally {
                lock.unlock();
            }
        } else {
            System.out.println("Could not acquire lock");
        }
    }
}
```

**ReadWriteLock**: 읽기/쓰기 분리
```java
public class ReadWriteLockExample {
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();
    private Map<String, String> cache = new HashMap<>();

    public String get(String key) {
        readLock.lock();  // 여러 스레드가 동시에 읽기 가능
        try {
            return cache.get(key);
        } finally {
            readLock.unlock();
        }
    }

    public void put(String key, String value) {
        writeLock.lock();  // 쓰기는 배타적
        try {
            cache.put(key, value);
        } finally {
            writeLock.unlock();
        }
    }
}
```

### 3.2 Atomic 클래스

**원자적 연산 보장**:
```java
import java.util.concurrent.atomic.*;

public class AtomicExample {
    // 락 없이 스레드 안전한 카운터
    private AtomicInteger count = new AtomicInteger(0);

    public void increment() {
        count.incrementAndGet();  // 원자적 증가
    }

    public int getCount() {
        return count.get();
    }

    // CAS (Compare-And-Swap) 연산
    public boolean updateIfEquals(int expected, int newValue) {
        return count.compareAndSet(expected, newValue);
    }
}

// 다른 Atomic 클래스들
AtomicLong atomicLong = new AtomicLong(0);
AtomicBoolean atomicBoolean = new AtomicBoolean(false);
AtomicReference<String> atomicRef = new AtomicReference<>("initial");
```

---

## 4. ExecutorService (스레드 풀)

### 4.1 기본 사용법

```java
import java.util.concurrent.*;

public class ExecutorExample {
    public static void main(String[] args) {
        // 고정 크기 스레드 풀
        ExecutorService executor = Executors.newFixedThreadPool(4);

        // 작업 제출
        for (int i = 0; i < 10; i++) {
            int taskId = i;
            executor.submit(() -> {
                System.out.println("Task " + taskId + " on " +
                                   Thread.currentThread().getName());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // Executor 종료
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
}
```

### 4.2 다양한 Executor 타입

```java
// 1. 고정 크기 스레드 풀
ExecutorService fixedPool = Executors.newFixedThreadPool(10);

// 2. 단일 스레드 Executor
ExecutorService singleThread = Executors.newSingleThreadExecutor();

// 3. 캐시 스레드 풀 (필요에 따라 스레드 생성/재사용)
ExecutorService cachedPool = Executors.newCachedThreadPool();

// 4. 스케줄링 Executor
ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

// 5초 후 실행
scheduler.schedule(() -> System.out.println("Delayed task"), 5, TimeUnit.SECONDS);

// 초기 지연 2초, 주기 1초로 반복 실행
scheduler.scheduleAtFixedRate(() -> {
    System.out.println("Periodic task");
}, 2, 1, TimeUnit.SECONDS);
```

### 4.3 Future와 Callable

**Future로 결과 받기**:
```java
public class FutureExample {
    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // Callable: 결과를 반환하고 예외를 던질 수 있음
        Callable<Integer> task = () -> {
            Thread.sleep(2000);
            return 42;
        };

        Future<Integer> future = executor.submit(task);

        System.out.println("Doing other work...");

        // 결과 대기 (블로킹)
        Integer result = future.get();
        System.out.println("Result: " + result);

        // 타임아웃과 함께 결과 받기
        try {
            Integer result2 = future.get(1, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            System.out.println("Task timed out");
            future.cancel(true);  // 작업 취소
        }

        executor.shutdown();
    }
}
```

**Golang 비교**:
```go
// Golang channel로 결과 받기
resultChan := make(chan int)

go func() {
    time.Sleep(2 * time.Second)
    resultChan <- 42
}()

result := <-resultChan
fmt.Println("Result:", result)
```

---

## 5. CompletableFuture (비동기 프로그래밍)

### 5.1 기본 사용법

```java
import java.util.concurrent.CompletableFuture;

public class CompletableFutureExample {
    public static void main(String[] args) {
        // 비동기 작업 실행
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            sleep(1000);
            return "Hello";
        });

        // 결과 처리 (논블로킹)
        future.thenApply(result -> result + " World")
              .thenAccept(System.out::println);

        // 블로킹해서 결과 기다림
        String result = future.join();
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
```

### 5.2 체이닝과 조합

```java
public class CompletableFutureChaining {
    public static void main(String[] args) {
        // 여러 비동기 작업 체이닝
        CompletableFuture.supplyAsync(() -> {
            System.out.println("Fetching user...");
            return "user123";
        })
        .thenApplyAsync(userId -> {
            System.out.println("Fetching user details for: " + userId);
            return new User(userId, "John", "john@example.com");
        })
        .thenApplyAsync(user -> {
            System.out.println("Fetching orders for: " + user.name);
            return List.of(new Order(1, 100.0), new Order(2, 200.0));
        })
        .thenAccept(orders -> {
            System.out.println("Total orders: " + orders.size());
        })
        .exceptionally(ex -> {
            System.err.println("Error: " + ex.getMessage());
            return null;
        });
    }

    record User(String id, String name, String email) {}
    record Order(int id, double amount) {}
}
```

### 5.3 여러 Future 조합

```java
public class CompletableFutureCombine {
    public static void main(String[] args) throws Exception {
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            sleep(1000);
            return "Result 1";
        });

        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            sleep(2000);
            return "Result 2";
        });

        CompletableFuture<String> future3 = CompletableFuture.supplyAsync(() -> {
            sleep(1500);
            return "Result 3";
        });

        // 모든 Future가 완료될 때까지 대기
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
            future1, future2, future3
        );

        allFutures.thenRun(() -> {
            try {
                System.out.println(future1.get());
                System.out.println(future2.get());
                System.out.println(future3.get());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // 첫 번째로 완료되는 Future의 결과
        CompletableFuture<Object> anyFuture = CompletableFuture.anyOf(
            future1, future2, future3
        );

        System.out.println("First completed: " + anyFuture.get());
    }
}
```

**Golang 비교**:
```go
// Golang으로 여러 goroutine 결과 받기
ch1 := make(chan string)
ch2 := make(chan string)
ch3 := make(chan string)

go func() { time.Sleep(1 * time.Second); ch1 <- "Result 1" }()
go func() { time.Sleep(2 * time.Second); ch2 <- "Result 2" }()
go func() { time.Sleep(1500 * time.Millisecond); ch3 <- "Result 3" }()

// 모든 결과 받기
result1 := <-ch1
result2 := <-ch2
result3 := <-ch3

// 첫 번째 결과만 받기
select {
case result := <-ch1:
    fmt.Println("First:", result)
case result := <-ch2:
    fmt.Println("First:", result)
case result := <-ch3:
    fmt.Println("First:", result)
}
```

---

## 6. 동시성 컬렉션

### 6.1 ConcurrentHashMap

```java
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentHashMapExample {
    private ConcurrentHashMap<String, Integer> cache = new ConcurrentHashMap<>();

    public void updateCache(String key, int value) {
        cache.put(key, value);
    }

    public Integer getFromCache(String key) {
        return cache.get(key);
    }

    // 원자적 업데이트
    public void incrementCounter(String key) {
        cache.compute(key, (k, v) -> (v == null) ? 1 : v + 1);
    }

    // putIfAbsent
    public void addIfNotExists(String key, int value) {
        cache.putIfAbsent(key, value);
    }
}
```

### 6.2 BlockingQueue

**Producer-Consumer 패턴**:
```java
import java.util.concurrent.*;

public class ProducerConsumerExample {
    private static BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(10);

    static class Producer implements Runnable {
        @Override
        public void run() {
            try {
                for (int i = 0; i < 20; i++) {
                    System.out.println("Producing: " + i);
                    queue.put(i);  // 큐가 가득 차면 대기
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    static class Consumer implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    Integer item = queue.take();  // 큐가 비어있으면 대기
                    System.out.println("Consuming: " + item);
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) {
        new Thread(new Producer()).start();
        new Thread(new Consumer()).start();
    }
}
```

**Golang Channel 비교**:
```go
// Golang channel
queue := make(chan int, 10)

// Producer
go func() {
    for i := 0; i < 20; i++ {
        fmt.Println("Producing:", i)
        queue <- i
        time.Sleep(100 * time.Millisecond)
    }
    close(queue)
}()

// Consumer
go func() {
    for item := range queue {
        fmt.Println("Consuming:", item)
        time.Sleep(500 * time.Millisecond)
    }
}()
```

---

## 7. 동시성 유틸리티

### 7.1 CountDownLatch

```java
import java.util.concurrent.CountDownLatch;

public class CountDownLatchExample {
    public static void main(String[] args) throws InterruptedException {
        int workerCount = 5;
        CountDownLatch latch = new CountDownLatch(workerCount);

        for (int i = 0; i < workerCount; i++) {
            int workerId = i;
            new Thread(() -> {
                System.out.println("Worker " + workerId + " starting");
                sleep(1000);
                System.out.println("Worker " + workerId + " done");
                latch.countDown();  // 카운트 감소
            }).start();
        }

        System.out.println("Waiting for workers...");
        latch.await();  // 카운트가 0이 될 때까지 대기
        System.out.println("All workers completed");
    }
}
```

### 7.2 CyclicBarrier

```java
import java.util.concurrent.CyclicBarrier;

public class CyclicBarrierExample {
    public static void main(String[] args) {
        int parties = 3;
        CyclicBarrier barrier = new CyclicBarrier(parties, () -> {
            System.out.println("All parties arrived, proceeding...");
        });

        for (int i = 0; i < parties; i++) {
            int partyId = i;
            new Thread(() -> {
                try {
                    System.out.println("Party " + partyId + " arriving");
                    sleep(partyId * 1000);
                    barrier.await();  // 모든 파티가 도착할 때까지 대기
                    System.out.println("Party " + partyId + " proceeding");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
```

### 7.3 Semaphore

```java
import java.util.concurrent.Semaphore;

public class SemaphoreExample {
    private static Semaphore semaphore = new Semaphore(3);  // 최대 3개 허용

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            int taskId = i;
            new Thread(() -> {
                try {
                    System.out.println("Task " + taskId + " waiting for permit");
                    semaphore.acquire();  // 허가 획득
                    System.out.println("Task " + taskId + " acquired permit");
                    sleep(2000);
                    System.out.println("Task " + taskId + " releasing permit");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    semaphore.release();  // 허가 반환
                }
            }).start();
        }
    }
}
```

---

## 🛠 실습 프로젝트

### 프로젝트 1: 멀티스레드 파일 다운로더

```java
import java.util.concurrent.*;
import java.util.*;

public class ParallelDownloader {
    private static final int THREAD_POOL_SIZE = 4;

    public static void main(String[] args) throws Exception {
        List<String> urls = Arrays.asList(
            "https://example.com/file1.txt",
            "https://example.com/file2.txt",
            "https://example.com/file3.txt",
            "https://example.com/file4.txt",
            "https://example.com/file5.txt"
        );

        downloadFiles(urls);
    }

    public static void downloadFiles(List<String> urls) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        CountDownLatch latch = new CountDownLatch(urls.size());

        for (String url : urls) {
            executor.submit(() -> {
                try {
                    downloadFile(url);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
        System.out.println("All downloads completed");
    }

    private static void downloadFile(String url) {
        System.out.println("Downloading: " + url + " on " +
                           Thread.currentThread().getName());
        // 실제 다운로드 로직 시뮬레이션
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("Completed: " + url);
    }
}
```

### 프로젝트 2: 게임 서버 플레이어 세션 매니저

```java
import java.util.concurrent.*;
import java.util.*;

public class PlayerSessionManager {
    private final ConcurrentHashMap<String, PlayerSession> sessions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public PlayerSessionManager() {
        // 5초마다 만료된 세션 정리
        scheduler.scheduleAtFixedRate(this::cleanupExpiredSessions, 5, 5, TimeUnit.SECONDS);
    }

    public void createSession(String playerId) {
        PlayerSession session = new PlayerSession(playerId);
        sessions.put(playerId, session);
        System.out.println("Session created for player: " + playerId);
    }

    public PlayerSession getSession(String playerId) {
        PlayerSession session = sessions.get(playerId);
        if (session != null) {
            session.updateLastActivity();
        }
        return session;
    }

    public void removeSession(String playerId) {
        sessions.remove(playerId);
        System.out.println("Session removed for player: " + playerId);
    }

    private void cleanupExpiredSessions() {
        long now = System.currentTimeMillis();
        sessions.entrySet().removeIf(entry -> {
            boolean expired = (now - entry.getValue().getLastActivity()) > 60000;  // 1분
            if (expired) {
                System.out.println("Expired session for player: " + entry.getKey());
            }
            return expired;
        });
    }

    public void shutdown() {
        scheduler.shutdown();
    }

    static class PlayerSession {
        private final String playerId;
        private final AtomicLong lastActivity = new AtomicLong(System.currentTimeMillis());
        private final AtomicInteger score = new AtomicInteger(0);

        public PlayerSession(String playerId) {
            this.playerId = playerId;
        }

        public void updateLastActivity() {
            lastActivity.set(System.currentTimeMillis());
        }

        public long getLastActivity() {
            return lastActivity.get();
        }

        public void incrementScore(int points) {
            score.addAndGet(points);
        }

        public int getScore() {
            return score.get();
        }
    }

    public static void main(String[] args) throws Exception {
        PlayerSessionManager manager = new PlayerSessionManager();

        // 여러 플레이어 세션 생성
        for (int i = 0; i < 5; i++) {
            String playerId = "Player" + i;
            manager.createSession(playerId);
        }

        // 세션 활동 시뮬레이션
        for (int i = 0; i < 10; i++) {
            String playerId = "Player" + (i % 5);
            PlayerSession session = manager.getSession(playerId);
            if (session != null) {
                session.incrementScore(10);
                System.out.println(playerId + " score: " + session.getScore());
            }
            Thread.sleep(1000);
        }

        Thread.sleep(70000);  // 세션 만료 대기
        manager.shutdown();
    }
}
```

---

## 📖 학습 리소스

### 서적
- **Java Concurrency in Practice** - Brian Goetz (필독!)
- **Effective Java (3rd Edition)** - Joshua Bloch (동시성 챕터)

### 온라인 강의
- [Java 동시성 프로그래밍](https://www.inflearn.com/course/java-concurrent-programming)

### 공식 문서
- [java.util.concurrent API](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/package-summary.html)

---

## ✅ 체크리스트

- [ ] Thread와 Runnable 이해
- [ ] synchronized와 volatile 차이
- [ ] Lock 인터페이스 사용법
- [ ] Atomic 클래스 활용
- [ ] ExecutorService와 Thread Pool
- [ ] CompletableFuture 체이닝
- [ ] BlockingQueue (Producer-Consumer)
- [ ] ConcurrentHashMap 사용
- [ ] CountDownLatch, CyclicBarrier, Semaphore
- [ ] 데드락 이해 및 방지
- [ ] 실습 프로젝트 2개 완료

---

## 🚀 다음 단계

**→ [04. Spring Core & 의존성 주입](../04-spring-core/)**

## 💡 Golang/C++ 개발자를 위한 팁

**Golang과의 차이**:
- Goroutine의 경량성 vs Java Thread의 무거움 → ExecutorService로 완화
- Channel vs BlockingQueue: 비슷한 역할, 다른 API
- select 문 → CompletableFuture 조합으로 대체

**C++과의 차이**:
- std::mutex → synchronized 또는 Lock
- std::condition_variable → wait/notify
- RAII (자동 락 해제) → try-finally 또는 try-with-resources

**게임 서버 경험 활용**:
- 플레이어 세션 관리 → ConcurrentHashMap
- 이벤트 처리 → BlockingQueue
- 타이머 → ScheduledExecutorService
