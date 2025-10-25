# 01. Java 언어 기초

> **학습 기간**: 1-2주
> **난이도**: ⭐⭐☆☆☆
> **전제 조건**: Golang 기본 문법 이해

## 📚 학습 목표

Golang 경험을 활용하여 Java의 기본 문법과 객체지향 프로그래밍 개념을 빠르게 습득합니다.

## 🎯 핵심 학습 내용

### 1. 개발 환경 설정

#### 1.1 JDK 설치
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-17-jdk

# macOS (Homebrew)
brew install openjdk@17

# 버전 확인
java -version
javac -version
```

#### 1.2 IDE 설정
**추천**: IntelliJ IDEA Community Edition
- 다운로드: https://www.jetbrains.com/idea/download/
- 필수 플러그인: Lombok, Maven/Gradle Helper

**대안**: Eclipse, VS Code (Extension Pack for Java)

#### 1.3 첫 프로젝트 생성
```java
// HelloWorld.java
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello, Java!");
    }
}
```

```bash
# 컴파일
javac HelloWorld.java

# 실행
java HelloWorld
```

---

### 2. Java 기본 문법 (Golang과 비교)

#### 2.1 변수와 자료형

**Java**:
```java
// 기본형 (Primitive Types)
int age = 25;
double price = 19.99;
boolean isActive = true;
char grade = 'A';

// 참조형 (Reference Types)
String name = "John";
Integer boxedAge = 25; // Auto-boxing

// 타입 추론 (Java 10+)
var count = 10;  // int로 추론
var message = "Hello";  // String으로 추론
```

**Golang 비교**:
```go
// Golang
age := 25
price := 19.99
isActive := true
name := "John"
```

**주요 차이점**:
- Java: 명시적 타입 선언 필요 (var 키워드는 제한적)
- Golang: `:=` 연산자로 간단한 타입 추론
- Java: Primitive vs Reference 타입 구분
- Golang: 모든 타입이 동일하게 처리됨

#### 2.2 제어문

**조건문**:
```java
// if-else
if (age >= 18) {
    System.out.println("Adult");
} else {
    System.out.println("Minor");
}

// switch (Java 14+ Enhanced Switch)
String day = "Monday";
String result = switch (day) {
    case "Monday", "Tuesday" -> "Weekday";
    case "Saturday", "Sunday" -> "Weekend";
    default -> "Invalid day";
};
```

**반복문**:
```java
// for loop
for (int i = 0; i < 10; i++) {
    System.out.println(i);
}

// for-each loop
int[] numbers = {1, 2, 3, 4, 5};
for (int num : numbers) {
    System.out.println(num);
}

// while loop
int count = 0;
while (count < 5) {
    System.out.println(count++);
}
```

**Golang과의 비교**:
- Java는 전통적인 C 스타일 for 루프 사용
- Golang의 `range`는 Java의 for-each와 유사
- Java는 `while`/`do-while` 별도 존재

#### 2.3 함수 (메서드)

**Java**:
```java
public class Calculator {
    // 접근 제어자 반환타입 메서드명(매개변수)
    public static int add(int a, int b) {
        return a + b;
    }

    // 오버로딩 (Overloading)
    public static double add(double a, double b) {
        return a + b;
    }

    // 가변 인자 (Varargs)
    public static int sum(int... numbers) {
        int total = 0;
        for (int num : numbers) {
            total += num;
        }
        return total;
    }
}

// 사용
int result = Calculator.add(5, 3);
int total = Calculator.sum(1, 2, 3, 4, 5);
```

**Golang 비교**:
```go
// Golang
func add(a, b int) int {
    return a + b
}

// 다중 반환값
func divide(a, b int) (int, error) {
    if b == 0 {
        return 0, errors.New("division by zero")
    }
    return a / b, nil
}
```

**주요 차이점**:
- Java: 메서드 오버로딩 지원
- Golang: 다중 반환값 지원 (Java는 객체나 배열로 대체)
- Java: 접근 제어자 필수 (public, private, protected, default)

---

### 3. 객체지향 프로그래밍 (OOP)

#### 3.1 클래스와 객체

**Java**:
```java
public class User {
    // 필드 (인스턴스 변수)
    private String username;
    private String email;
    private int age;

    // 생성자
    public User(String username, String email, int age) {
        this.username = username;
        this.email = email;
        this.age = age;
    }

    // Getter
    public String getUsername() {
        return username;
    }

    // Setter
    public void setUsername(String username) {
        this.username = username;
    }

    // 메서드
    public void displayInfo() {
        System.out.println("User: " + username + ", Age: " + age);
    }
}

// 객체 생성
User user = new User("john", "john@example.com", 25);
user.displayInfo();
```

**Golang 비교**:
```go
type User struct {
    Username string
    Email    string
    Age      int
}

func (u *User) DisplayInfo() {
    fmt.Printf("User: %s, Age: %d\n", u.Username, u.Age)
}

// 객체 생성
user := User{
    Username: "john",
    Email:    "john@example.com",
    Age:      25,
}
user.DisplayInfo()
```

**주요 차이점**:
- Java: 캡슐화 강제 (private 필드 + getter/setter)
- Golang: 대문자로 public/private 구분
- Java: 생성자 명시적 정의
- Golang: 구조체 리터럴로 초기화

#### 3.2 상속 (Inheritance)

**Java**:
```java
// 부모 클래스
public class Animal {
    protected String name;

    public Animal(String name) {
        this.name = name;
    }

    public void makeSound() {
        System.out.println("Some sound");
    }
}

// 자식 클래스
public class Dog extends Animal {
    public Dog(String name) {
        super(name);  // 부모 생성자 호출
    }

    @Override
    public void makeSound() {
        System.out.println("Woof!");
    }

    public void fetch() {
        System.out.println(name + " is fetching the ball");
    }
}

// 사용
Dog dog = new Dog("Buddy");
dog.makeSound();  // "Woof!"
dog.fetch();
```

**Golang과의 비교**:
- Java: 클래스 기반 상속 (`extends`)
- Golang: 구조체 임베딩 (상속 개념 없음)
- Java: 단일 상속만 가능
- Golang: 여러 구조체 임베딩 가능

#### 3.3 인터페이스 (Interface)

**Java**:
```java
// 인터페이스 정의
public interface Drawable {
    void draw();  // 추상 메서드 (Java 8 이전)

    // Default 메서드 (Java 8+)
    default void display() {
        System.out.println("Displaying...");
    }
}

// 인터페이스 구현
public class Circle implements Drawable {
    private double radius;

    public Circle(double radius) {
        this.radius = radius;
    }

    @Override
    public void draw() {
        System.out.println("Drawing circle with radius: " + radius);
    }
}

// 사용
Drawable drawable = new Circle(5.0);
drawable.draw();
drawable.display();
```

**Golang 비교**:
```go
type Drawable interface {
    Draw()
}

type Circle struct {
    Radius float64
}

func (c Circle) Draw() {
    fmt.Printf("Drawing circle with radius: %.2f\n", c.Radius)
}

// 암시적 구현 (명시적 선언 불필요)
var drawable Drawable = Circle{Radius: 5.0}
drawable.Draw()
```

**주요 차이점**:
- Java: 명시적 인터페이스 구현 (`implements`)
- Golang: 암시적 인터페이스 구현
- Java: Default 메서드 지원 (Java 8+)
- Golang: 메서드만 정의 가능

#### 3.4 다형성 (Polymorphism)

```java
public interface PaymentMethod {
    void processPayment(double amount);
}

public class CreditCard implements PaymentMethod {
    @Override
    public void processPayment(double amount) {
        System.out.println("Processing credit card payment: $" + amount);
    }
}

public class PayPal implements PaymentMethod {
    @Override
    public void processPayment(double amount) {
        System.out.println("Processing PayPal payment: $" + amount);
    }
}

// 다형성 활용
public class PaymentProcessor {
    public void process(PaymentMethod method, double amount) {
        method.processPayment(amount);  // 동적 바인딩
    }
}

// 사용
PaymentProcessor processor = new PaymentProcessor();
processor.process(new CreditCard(), 100.0);
processor.process(new PayPal(), 50.0);
```

---

### 4. 제네릭 (Generics)

#### 4.1 제네릭 클래스

```java
// 제네릭 클래스 정의
public class Box<T> {
    private T item;

    public void setItem(T item) {
        this.item = item;
    }

    public T getItem() {
        return item;
    }
}

// 사용
Box<String> stringBox = new Box<>();
stringBox.setItem("Hello");
String value = stringBox.getItem();

Box<Integer> intBox = new Box<>();
intBox.setItem(42);
int number = intBox.getItem();
```

#### 4.2 제네릭 메서드

```java
public class Utils {
    // 제네릭 메서드
    public static <T> void printArray(T[] array) {
        for (T element : array) {
            System.out.print(element + " ");
        }
        System.out.println();
    }

    // 제한된 타입 파라미터
    public static <T extends Comparable<T>> T findMax(T[] array) {
        T max = array[0];
        for (T element : array) {
            if (element.compareTo(max) > 0) {
                max = element;
            }
        }
        return max;
    }
}

// 사용
Integer[] numbers = {3, 7, 2, 9, 1};
Utils.printArray(numbers);
Integer max = Utils.findMax(numbers);
```

---

### 5. 컬렉션 프레임워크

#### 5.1 List

```java
import java.util.*;

// ArrayList (동적 배열)
List<String> names = new ArrayList<>();
names.add("Alice");
names.add("Bob");
names.add("Charlie");

// 요소 접근
String first = names.get(0);

// 반복
for (String name : names) {
    System.out.println(name);
}

// LinkedList (연결 리스트)
List<Integer> numbers = new LinkedList<>();
numbers.add(1);
numbers.add(2);
```

#### 5.2 Set

```java
// HashSet (중복 불허)
Set<String> uniqueNames = new HashSet<>();
uniqueNames.add("Alice");
uniqueNames.add("Bob");
uniqueNames.add("Alice");  // 중복, 추가되지 않음

System.out.println(uniqueNames.size());  // 2

// TreeSet (정렬된 Set)
Set<Integer> sortedNumbers = new TreeSet<>();
sortedNumbers.add(5);
sortedNumbers.add(1);
sortedNumbers.add(3);
// 자동 정렬: [1, 3, 5]
```

#### 5.3 Map

```java
// HashMap
Map<String, Integer> ages = new HashMap<>();
ages.put("Alice", 25);
ages.put("Bob", 30);
ages.put("Charlie", 28);

// 값 조회
Integer aliceAge = ages.get("Alice");

// 키 존재 확인
if (ages.containsKey("Bob")) {
    System.out.println("Bob's age: " + ages.get("Bob"));
}

// 반복
for (Map.Entry<String, Integer> entry : ages.entrySet()) {
    System.out.println(entry.getKey() + ": " + entry.getValue());
}

// Java 8+ forEach
ages.forEach((name, age) -> System.out.println(name + ": " + age));
```

**Golang과의 비교**:
```go
// Golang Map
ages := make(map[string]int)
ages["Alice"] = 25
ages["Bob"] = 30

// 반복
for name, age := range ages {
    fmt.Printf("%s: %d\n", name, age)
}
```

---

### 6. 람다와 Stream API (Java 8+)

#### 6.1 람다 표현식

```java
// 기존 방식 (익명 클래스)
Runnable runnable1 = new Runnable() {
    @Override
    public void run() {
        System.out.println("Hello");
    }
};

// 람다 방식
Runnable runnable2 = () -> System.out.println("Hello");

// 함수형 인터페이스
@FunctionalInterface
interface Calculator {
    int calculate(int a, int b);
}

Calculator add = (a, b) -> a + b;
Calculator multiply = (a, b) -> a * b;

System.out.println(add.calculate(5, 3));       // 8
System.out.println(multiply.calculate(5, 3));  // 15
```

#### 6.2 Stream API

```java
import java.util.stream.*;

List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

// Filter와 Collect
List<Integer> evenNumbers = numbers.stream()
    .filter(n -> n % 2 == 0)
    .collect(Collectors.toList());
// [2, 4, 6, 8, 10]

// Map (변환)
List<Integer> squared = numbers.stream()
    .map(n -> n * n)
    .collect(Collectors.toList());
// [1, 4, 9, 16, 25, ...]

// Reduce (집계)
int sum = numbers.stream()
    .reduce(0, (a, b) -> a + b);
// 55

// 복합 연산
double average = numbers.stream()
    .filter(n -> n > 5)
    .mapToInt(n -> n)
    .average()
    .orElse(0.0);
```

**Golang과의 비교**:
```go
// Golang (함수형 프로그래밍 라이브러리 없이)
numbers := []int{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}
var evenNumbers []int
for _, n := range numbers {
    if n%2 == 0 {
        evenNumbers = append(evenNumbers, n)
    }
}
```

---

### 7. 예외 처리

#### 7.1 try-catch-finally

**Java**:
```java
public class ExceptionExample {
    public static void divide(int a, int b) {
        try {
            int result = a / b;
            System.out.println("Result: " + result);
        } catch (ArithmeticException e) {
            System.err.println("Error: Division by zero");
        } finally {
            System.out.println("Finally block executed");
        }
    }

    // 예외 던지기
    public static void validateAge(int age) throws IllegalArgumentException {
        if (age < 0) {
            throw new IllegalArgumentException("Age cannot be negative");
        }
    }
}
```

**Golang 비교**:
```go
func divide(a, b int) (int, error) {
    if b == 0 {
        return 0, errors.New("division by zero")
    }
    return a / b, nil
}

// 사용
result, err := divide(10, 0)
if err != nil {
    fmt.Println("Error:", err)
    return
}
```

**주요 차이점**:
- Java: try-catch 예외 처리
- Golang: error 반환값으로 처리
- Java: Checked/Unchecked Exception 구분
- Golang: defer로 정리 작업 (finally와 유사)

#### 7.2 try-with-resources (Java 7+)

```java
// 자동 리소스 관리
try (BufferedReader reader = new BufferedReader(new FileReader("file.txt"))) {
    String line = reader.readLine();
    System.out.println(line);
} catch (IOException e) {
    e.printStackTrace();
}
// reader는 자동으로 close됨
```

**Golang의 defer와 비교**:
```go
file, err := os.Open("file.txt")
if err != nil {
    return err
}
defer file.Close()  // 함수 종료 시 자동 실행
```

---

### 8. Java 최신 기능 (Java 11+)

#### 8.1 var 키워드 (Java 10+)

```java
// 타입 추론
var name = "John";  // String
var age = 25;       // int
var list = new ArrayList<String>();

// 주의: 초기화 필수
// var x;  // 컴파일 에러
```

#### 8.2 Text Blocks (Java 15+)

```java
// 기존 방식
String json = "{\n" +
              "  \"name\": \"John\",\n" +
              "  \"age\": 25\n" +
              "}";

// Text Blocks
String json2 = """
    {
      "name": "John",
      "age": 25
    }
    """;
```

#### 8.3 Records (Java 14+)

```java
// 기존 DTO 클래스
public class Point {
    private final int x;
    private final int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    // equals, hashCode, toString 구현 필요
}

// Record 사용
public record Point(int x, int y) {
    // 자동 생성: 생성자, getter, equals, hashCode, toString
}

// 사용
Point p = new Point(10, 20);
System.out.println(p.x());  // 10
System.out.println(p);       // Point[x=10, y=20]
```

#### 8.4 Pattern Matching for instanceof (Java 16+)

```java
// 기존 방식
Object obj = "Hello";
if (obj instanceof String) {
    String str = (String) obj;
    System.out.println(str.toUpperCase());
}

// Pattern Matching
if (obj instanceof String str) {
    System.out.println(str.toUpperCase());
}
```

---

## 🛠 실습 프로젝트

### 프로젝트 1: CLI 계산기
간단한 명령줄 계산기를 구현하여 기본 문법을 익힙니다.

```java
import java.util.Scanner;

public class Calculator {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter first number: ");
        double num1 = scanner.nextDouble();

        System.out.print("Enter operator (+, -, *, /): ");
        char operator = scanner.next().charAt(0);

        System.out.print("Enter second number: ");
        double num2 = scanner.nextDouble();

        double result = switch (operator) {
            case '+' -> num1 + num2;
            case '-' -> num1 - num2;
            case '*' -> num1 * num2;
            case '/' -> num1 / num2;
            default -> throw new IllegalArgumentException("Invalid operator");
        };

        System.out.println("Result: " + result);
    }
}
```

### 프로젝트 2: 학생 관리 시스템
OOP 개념을 활용한 학생 관리 시스템

```java
import java.util.*;

// Student Record
record Student(String id, String name, int age, double gpa) {
    public void displayInfo() {
        System.out.printf("ID: %s, Name: %s, Age: %d, GPA: %.2f%n",
                          id, name, age, gpa);
    }
}

// StudentManager Class
class StudentManager {
    private Map<String, Student> students = new HashMap<>();

    public void addStudent(Student student) {
        students.put(student.id(), student);
    }

    public Student findStudent(String id) {
        return students.get(id);
    }

    public List<Student> findTopStudents(int count) {
        return students.values().stream()
            .sorted((s1, s2) -> Double.compare(s2.gpa(), s1.gpa()))
            .limit(count)
            .toList();
    }

    public void displayAllStudents() {
        students.values().forEach(Student::displayInfo);
    }
}

// Main
public class StudentSystem {
    public static void main(String[] args) {
        StudentManager manager = new StudentManager();

        manager.addStudent(new Student("S001", "Alice", 20, 3.8));
        manager.addStudent(new Student("S002", "Bob", 21, 3.5));
        manager.addStudent(new Student("S003", "Charlie", 19, 3.9));

        System.out.println("All Students:");
        manager.displayAllStudents();

        System.out.println("\nTop 2 Students:");
        manager.findTopStudents(2).forEach(Student::displayInfo);
    }
}
```

---

## 📖 학습 리소스

### 공식 문서
- [Oracle Java Tutorials](https://docs.oracle.com/javase/tutorial/)
- [Java Language Specification](https://docs.oracle.com/javase/specs/)

### 온라인 강의
- [백기선 - 자바 기초](https://www.inflearn.com/course/the-java-java8)
- [모던 자바 인 액션](https://www.hanbit.co.kr/store/books/look.php?p_code=B1496778547)

### 서적
- **Effective Java (3rd Edition)** - Joshua Bloch
- **Core Java Volume I** - Cay S. Horstmann
- **Head First Java** - Kathy Sierra (입문자용)

### 연습 사이트
- [Codewars](https://www.codewars.com/) - Java 문제 풀이
- [LeetCode](https://leetcode.com/) - 알고리즘 (Java)
- [Exercism](https://exercism.org/tracks/java) - Java Track

---

## ✅ 체크리스트

### 기본 문법
- [ ] JDK 설치 및 IDE 설정
- [ ] Hello World 작성 및 실행
- [ ] 변수, 자료형 이해
- [ ] 제어문 (if, switch, for, while) 숙지
- [ ] 메서드 작성 및 호출

### 객체지향
- [ ] 클래스와 객체 생성
- [ ] 상속과 다형성 이해
- [ ] 인터페이스 정의 및 구현
- [ ] 접근 제어자 이해
- [ ] 생성자와 초기화 블록

### 고급 기능
- [ ] 제네릭 사용법
- [ ] 컬렉션 프레임워크 (List, Set, Map)
- [ ] 람다와 Stream API
- [ ] 예외 처리 (try-catch-finally)
- [ ] Java 11+ 최신 기능

### 실습
- [ ] CLI 계산기 프로젝트
- [ ] 학생 관리 시스템 프로젝트
- [ ] 알고리즘 문제 10개 이상 풀이 (LeetCode/Codewars)

---

## 🚀 다음 단계

Java 기초를 마스터했다면 다음 단계로 진행하세요:

**→ [02. JVM과 메모리 관리](../02-jvm-memory/)**

---

## 💡 Golang 개발자를 위한 팁

1. **패키지 구조**: Java는 디렉토리 구조와 패키지 이름이 일치해야 함
2. **에러 처리**: try-catch에 익숙해지기 (Golang의 error 반환과 다름)
3. **객체지향**: Java는 모든 것이 클래스 기반 (Golang의 구조체보다 무겁게 느껴질 수 있음)
4. **타입 시스템**: Java는 더 엄격한 타입 검사
5. **빌드 시스템**: `go build` 대신 Maven/Gradle 사용

**학습 전략**:
- Golang의 간결함과 Java의 명시성을 비교하며 학습
- 각 개념마다 Golang으로 어떻게 구현하는지 떠올리며 대응시키기
- Java의 장점(강력한 타입 시스템, 풍부한 라이브러리)에 집중
