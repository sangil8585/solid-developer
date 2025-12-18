# 클래스 파일과 바이트코드

---

## 📋 목차

1. [클래스 로딩과 클래스 객체](#part-1-클래스-로딩과-클래스-객체)
2. [클래스로더](#part-2-클래스로더)
3. [클래스 파일 구조](#part-3-클래스-파일-구조)
4. [바이트코드](#part-4-바이트코드)
5. [리플렉션](#part-5-리플렉션)

---

# Part 1: 클래스 로딩과 클래스 객체

## 🤔 클래스 로딩이 필요한 이유

### 문제 상황
```
Spring Boot 프로젝트
  ├── UserService.class
  ├── ProductRepository.class
  ├── OrderController.class
  ├── ... (수백 개의 클래스)
  └── Spring Framework 클래스들
```

**모든 클래스를 프로그램 시작 시 한꺼번에 로딩한다면?**

❌ **메모리 낭비**: 사용하지 않을 클래스까지 전부 메모리 차지  
❌ **느린 시작**: 모든 클래스 파일을 읽고 검증하는 데 오래 걸림  
❌ **비효율**: 실제로는 일부만 사용하는데 전체를 로딩

### JVM의 해결책: Lazy Loading (지연 로딩)

✅ **필요한 순간에만 로딩**  
✅ **빠른 프로그램 시작**  
✅ **효율적인 메모리 사용**

```java
// UserService를 처음 사용하는 순간
UserService service = new UserService();  // ← 이 순간 클래스 로딩 발생!
```

---

## ⏰ 클래스 로딩이 발생하는 시점

### 1️⃣ 객체 생성 시
```java
MyClass obj = new MyClass();  // 처음 사용 → 로딩 발생
```

### 2️⃣ 정적 멤버 접근 시
```java
// 정적 메서드 호출
MyClass.staticMethod();  // 로딩 발생

// 정적 필드 접근
int value = MyClass.staticField;  // 로딩 발생
```

### 3️⃣ 리플렉션 사용 시
```java
Class<?> clazz = Class.forName("com.example.MyClass");  // 명시적 로딩
```

### ⚠️ 예외: 컴파일 타임 상수
```java
public class MyClass {
    public static final int MAX_VALUE = 100;  // 컴파일 타임 상수
    public static String name = "변상일";      // 일반 static 변수
}

// 실행 결과
System.out.println(MyClass.MAX_VALUE);     // 로딩 안 함 (100이 코드에 직접 삽입됨)
System.out.println(MyClass.name);   // 로딩 발생 (런타임에 값 필요)
```

---

## 🔄 클래스 로딩의 3단계 프로세스

```
┌─────────────────┐
│ .class 파일     │  (디스크에 저장)
│ (바이트코드)     │
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│  1️⃣ Loading (로딩)                      │
│  - 클래스 파일을 찾아서 바이트 배열로      │
│    읽어들임                              │
│  - 클래스로더가 담당                      │
└────────┬────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│  2️⃣ Linking (링킹)                      │
│                                         │
│  📌 Verification (검증)                 │
│  - 바이트코드가 Java 명세를 따르는지      │
│  - 보안 검사 수행                        │
│                                         │
│  📌 Preparation (준비)                  │
│  - static 변수 메모리 할당               │
│  - 기본값으로 초기화 (0, null, false)    │
│                                         │
│  📌 Resolution (해석)                   │
│  - 심볼릭 참조 → 실제 메모리 주소         │
└────────┬────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│  3️⃣ Initialization (초기화)             │
│  - static 변수에 실제 값 할당            │
│  - static 블록 실행                      │
│  - ⚠️ 클래스당 단 한 번만 실행           │ 
└────────┬────────────────────────────────┘
         │
         ▼
┌─────────────────┐
│ 메모리에서       │
│ 실행 가능한 상태 │
└─────────────────┘
```

### 코드로 보는 초기화 단계

```java
public class MyClass {
    static int value = 100;  // Preparation: 0 → Initialization: 100
    
    static {
        System.out.println("클래스 초기화!");  // Initialization 단계에서 실행
        value = 200;
    }
}

// 실행 순서
// 1. Loading: MyClass.class 파일 읽기
// 2. Linking:
//    - Verification: 바이트코드 검증
//    - Preparation: value = 0 (기본값)
//    - Resolution: 심볼릭 참조 해석
// 3. Initialization:
//    - value = 100 할당
//    - static 블록 실행 ("클래스 초기화!" 출력)
//    - value = 200 할당
```

---

## 🎯 클래스 객체란?

클래스가 로딩되면 JVM은 힙 메모리에 **Class 타입의 객체**를 생성합니다.

### 개념 이해
```
UserService.class 파일 (디스크)
         ↓ 로딩
    ┌────────────────────────┐
    │ 메서드 영역             │  ← 실제 클래스 정보 저장
    │ - 바이트코드            │     (개발자 직접 접근 불가)
    │ - 상수 풀               │
    │ - 필드/메서드 정보      │
    └────────┬───────────────┘
             │ 미러 관계
             ▼
    ┌────────────────────────┐
    │ 힙 영역                 │
    │ Class<UserService>     │  ← 클래스 객체
    │ (메타데이터)            │     (개발자 접근 가능)
    └────────┬───────────────┘
             │ 참조
             ├──→ UserService instance 1
             ├──→ UserService instance 2
             └──→ UserService instance 3
```

### 클래스 객체의 특징

**1. 싱글톤**: 하나의 클래스 = 하나의 Class 객체
```java
UserService obj1 = new UserService();
UserService obj2 = new UserService();
UserService obj3 = new UserService();

// 모두 같은 Class 객체를 참조
obj1.getClass() == obj2.getClass()  // true
obj2.getClass() == obj3.getClass()  // true
```

**2. 메타데이터 저장소**
```
Class 객체가 담고 있는 정보:
✓ 클래스 이름 (패키지 포함)
✓ 필드 정보 (이름, 타입, 접근제어자)
✓ 메서드 정보 (이름, 파라미터, 리턴타입)
✓ 생성자 정보
✓ 부모 클래스
✓ 구현한 인터페이스
✓ 어노테이션
```

---

## 🔍 클래스 객체를 얻는 3가지 방법

### 방법 1: 클래스 리터럴 (.class)
```java
Class<?> clazz = UserService.class;

// 특징
✓ 컴파일 타임에 타입 체크
✓ 가장 안전하고 빠름
✓ 예외 처리 불필요
```

### 방법 2: Class.forName()
```java
try {
    Class<?> clazz = Class.forName("com.example.UserService");
} catch (ClassNotFoundException e) {
    System.err.println("클래스를 찾을 수 없습니다!");
}

// 특징
✓ 런타임에 동적 로딩
✓ 문자열로 클래스 이름 전달
✓ ClassNotFoundException 발생 가능
✓ 설정 파일 기반 로딩에 유용
```

**실무 예제: JDBC 드라이버 로딩**
```java
// MySQL 드라이버를 동적으로 로딩
Class.forName("com.mysql.cj.jdbc.Driver");
```

### 방법 3: getClass()
```java
UserService obj = new UserService();
Class<?> clazz = obj.getClass();

// 특징
✓ 이미 생성된 객체에서 호출
✓ 실제 런타임 타입 확인
✓ 다형성 상황에서 유용
```

**다형성 예제**
```java
Animal animal = new Dog();  // 부모 타입으로 참조

animal.getClass().getName()  // "Dog" 반환 (실제 타입)
Animal.class.getName()       // "Animal" 반환 (선언 타입)
```

### 세 방법 모두 같은 객체를 반환
```java
Class<?> c1 = UserService.class;
Class<?> c2 = Class.forName("com.example.UserService");
UserService obj = new UserService();
Class<?> c3 = obj.getClass();

// 모두 동일한 Class 객체
System.out.println(c1 == c2);  // true
System.out.println(c2 == c3);  // true
System.out.println(c1 == c3);  // true
```

---

# Part 2: 클래스로더

## 🎯 클래스로더의 역할

클래스로더는 .class 파일을 찾아서 JVM 메모리로 로딩하는 주체입니다.

**핵심 역할**
```
1. 클래스 파일 찾기
   ├─ 파일 시스템 탐색
   ├─ JAR 파일 내부 탐색
   └─ 네트워크에서 다운로드

2. 네임스페이스 제공
   └─ 같은 이름의 클래스도 다른 클래스로더면 별개 취급

3. 보안 관문
   └─ 악의적인 클래스 차단
```

---

## 🏗️ 클래스로더의 계층 구조

```
┌─────────────────────────────────────┐
│  Bootstrap ClassLoader              │  ← 최상위 (네이티브 코드)
│  ═══════════════════════════════════│
│  역할: Java 핵심 라이브러리 로딩      │
│  위치: $JAVA_HOME/lib/rt.jar        │
│                                     │
│  예시:                              │
│  • java.lang.Object                 │
│  • java.lang.String                 │
│  • java.util.ArrayList              │
└─────────────┬───────────────────────┘
              │ 부모-자식 관계
              ▼
┌─────────────────────────────────────┐
│  Extension/Platform ClassLoader     │  ← 중간 계층
│  ═══════════════════════════════════│
│  역할: 확장 라이브러리 로딩           │
│  위치: $JAVA_HOME/lib/ext/          │
│                                     │
│  예시:                              │
│  • 보안 관련 확장                   │
│  • 암호화 라이브러리                 │
└─────────────┬───────────────────────┘
              │ 부모-자식 관계
              ▼
┌─────────────────────────────────────┐
│  Application ClassLoader            │  ← 우리가 사용하는 클래스로더
│  ═══════════════════════════════════│
│  역할: 애플리케이션 클래스 로딩       │
│  위치: Classpath (-cp 옵션)         │
│                                     │
│  예시:                              │
│  • com.example.UserService          │
│  • com.example.ProductRepository    │
│  • 외부 라이브러리 (Spring 등)       │
└─────────────────────────────────────┘
```

---

## 🔄 위임 모델 (Delegation Model)

클래스 로딩 요청이 들어오면 **부모부터 확인**하는 방식입니다.

```
클래스 로딩 요청: "java.lang.String"

Application ClassLoader
    │
    │ "내가 로딩하기 전에 부모에게 먼저 물어봐야지"
    ▼
Extension ClassLoader
    │
    │ "나도 부모에게 먼저 물어봐야지"
    ▼
Bootstrap ClassLoader
    │
    │ "java.lang.String? 내가 가지고 있어!"
    ▼
✅ 로딩 완료 (Bootstrap이 담당)
```

```
클래스 로딩 요청: "com.example.UserService"

Application ClassLoader
    │
    │ "부모에게 먼저 물어보자"
    ▼
Extension ClassLoader
    │
    │ "부모에게 먼저 물어보자"
    ▼
Bootstrap ClassLoader
    │
    │ "com.example.UserService? 나한테 없는데?"
    ▼
❌ 없음
    │
    ▼
Extension ClassLoader
    │
    │ "부모가 없다면 내가 찾아보자... 나도 없네"
    ▼
❌ 없음
    │
    ▼
Application ClassLoader
    │
    │ "그럼 내가 찾아보자... 있다!"
    ▼
✅ 로딩 완료 (Application이 담당)
```

### 왜 이렇게 복잡하게?

**보안과 일관성 보장**
```java
// 악의적인 개발자가 java.lang.String을 만들어서 클래스패스에 넣었다면?

// 위임 모델이 없다면:
Application ClassLoader가 가짜 String을 로딩  ❌ 위험!

// 위임 모델이 있으면:
Bootstrap ClassLoader가 항상 진짜 String을 먼저 로딩  ✅ 안전!
```

**클래스 일관성**
```
java.lang.Object는 항상 Bootstrap ClassLoader가 로딩
→ 모든 객체의 최상위 클래스가 동일하게 유지
→ JVM의 안정성 보장
```

---

## 🛠️ 커스텀 클래스로더

필요하다면 직접 클래스로더를 만들 수 있습니다.

**사용 예시**
```
• 데이터베이스에서 클래스 로딩
• 네트워크를 통해 암호화된 클래스 받아서 복호화 후 로딩
• 플러그인 시스템 구현
• 핫 스왑 (코드 변경 시 재시작 없이 적용)
```

**Tomcat의 클래스로더 구조**
```
Bootstrap ClassLoader
    │
    ▼
System ClassLoader
    │
    ▼
Common ClassLoader (Tomcat 공통)
    │
    ├──→ WebApp1 ClassLoader  → app1.war의 클래스들
    ├──→ WebApp2 ClassLoader  → app2.war의 클래스들
    └──→ WebApp3 ClassLoader  → app3.war의 클래스들
```

각 웹 애플리케이션이 독립적인 클래스로더를 가지므로 같은 이름의 클래스가 있어도 충돌하지 않습니다!

---

# Part 3: 클래스 파일 구조

## 🔎 javap 도구로 클래스 파일 들여다보기

javap는 컴파일된 .class 파일을 사람이 읽을 수 있는 형태로 보여주는 도구입니다.

```bash
# 기본 사용법
javap UserService

# 메서드의 바이트코드 보기
javap -c UserService

# 상수 풀까지 자세히 보기
javap -v UserService

# private 멤버까지 모두 보기
javap -p -v UserService
```

**예제: 간단한 클래스**
```java
public class Calculator {
    public int add(int a, int b) {
        return a + b;
    }
}
```

**javap -c Calculator 출력**
```
public class Calculator {
  public Calculator();
    Code:
       0: aload_0
       1: invokespecial #1  // Method java/lang/Object."<init>":()V
       4: return

  public int add(int, int);
    Code:
       0: iload_1
       1: iload_2
       2: iadd
       3: ireturn
}
```

---

## 📦 클래스 파일의 구조

클래스 파일은 정해진 순서로 구성되어 있습니다.

```
┌─────────────────────────────────────┐
│  매직 넘버 (4 bytes)                 │  0xCAFEBABE (항상 고정)
├─────────────────────────────────────┤
│  Minor Version (2 bytes)            │  클래스 파일 버전
│  Major Version (2 bytes)            │  (Java 8 = 52, Java 11 = 55)
├─────────────────────────────────────┤
│  Constant Pool (상수 풀)            │  ⭐ 가장 중요!
│  - 상수 개수                        │
│  - 상수들...                        │
├─────────────────────────────────────┤
│  Access Flags                       │  public, final, abstract 등
├─────────────────────────────────────┤
│  This Class                         │  현재 클래스 이름
│  Super Class                        │  부모 클래스 이름
├─────────────────────────────────────┤
│  Interfaces                         │  구현한 인터페이스 목록
├─────────────────────────────────────┤
│  Fields                             │  필드 정보
├─────────────────────────────────────┤
│  Methods                            │  메서드 정보 + 바이트코드
├─────────────────────────────────────┤
│  Attributes                         │  추가 속성 (어노테이션 등)
└─────────────────────────────────────┘
```

### 매직 넘버: 0xCAFEBABE

```
왜 CAFEBABE?
☕ CAFE = 커피
👶 BABE = 베이비

Java의 로고가 커피잔인 것과 연결!
JVM은 파일을 읽을 때 제일 먼저 이 값을 확인해서
"이게 Java 클래스 파일이 맞나?" 검증합니다.
```

---

## ⭐ 상수 풀 (Constant Pool)

상수 풀은 클래스 파일에서 가장 중요한 부분입니다. 클래스에서 사용되는 모든 상수와 참조가 여기에 저장됩니다.

**저장되는 정보**
```
• 문자열 리터럴
• 클래스와 인터페이스 이름
• 필드 이름과 타입
• 메서드 이름과 시그니처
• 숫자 상수
```

**왜 상수 풀을 사용할까?**
```java
public class Example {
    public void method1() {
        System.out.println("Hello World");
    }
    
    public void method2() {
        System.out.println("Hello World");
    }
    
    public void method3() {
        System.out.println("Hello World");
    }
}
```

**상수 풀 없이 저장한다면:**
```
"Hello World"를 3번 저장 → 메모리 낭비
```

**상수 풀을 사용하면:**
```
상수 풀:
  #1 = "Hello World"

method1: #1 참조
method2: #1 참조
method3: #1 참조

→ 한 번만 저장하고 인덱스로 참조!
```

### 상수 풀 예제

```bash
javap -v Example
```

```
Constant pool:
   #1 = Methodref          #6.#20         // java/lang/Object."<init>":()V
   #2 = Fieldref           #21.#22        // java/lang/System.out:Ljava/io/PrintStream;
   #3 = String             #23            // Hello World
   #4 = Methodref          #24.#25        // java/io/PrintStream.println:(Ljava/lang/String;)V
   #5 = Class              #26            // Example
   #6 = Class              #27            // java/lang/Object
   ...
  #23 = Utf8               Hello World
```

---

# Part 4: 바이트코드

## 💻 바이트코드란?

바이트코드는 JVM이 실행하는 명령어입니다. 우리가 작성한 Java 코드가 컴파일되면 바이트코드로 변환됩니다.

**특징**
```
• 각 명령어가 1바이트로 표현 (256개 명령어 가능)
• 플랫폼 독립적 (Write Once, Run Anywhere)
• JVM이 네이티브 코드로 변환하여 실행
```

**Java 코드 → 바이트코드 → 실행**
```
UserService.java
    │ javac (컴파일)
    ▼
UserService.class (바이트코드)
    │ JVM
    ├─→ Windows: 네이티브 코드로 변환
    ├─→ Linux: 네이티브 코드로 변환
    └─→ Mac: 네이티브 코드로 변환
```

---

# Part 5: 리플렉션

## 🔮 리플렉션이란?

리플렉션은 런타임에 클래스의 정보를 검사하고 조작할 수 있는 능력을 말합니다.

일반적으로 프로그램을 작성할 때는 컴파일 시점에 모든 것이 결정됩니다. 
어떤 클래스를 사용할지, 어떤 메서드를 호출할지, 어떤 필드에 접근할지 전부 코드에 명시적으로 작성합니다. 
그러나 리플렉션을 사용하면 이런 것들을 런타임에 동적으로 결정할 수 있습니다.

예를 들어 설정 파일에서 클래스 이름을 읽어와서 그 클래스의 인스턴스를 생성하고, 
특정 어노테이션이 붙은 메서드를 찾아서 호출하는 것이 가능합니다.

**일반적인 프로그래밍**
```java
// 컴파일 시점에 모든 것이 결정됨
UserService service = new UserService();
service.findById(1L);
```

**리플렉션 사용**
```java
// 런타임에 동적으로 결정
String className = "com.example.UserService";
Class<?> clazz = Class.forName(className);
Object service = clazz.getDeclaredConstructor().newInstance();

Method method = clazz.getMethod("findById", Long.class);
method.invoke(service, 1L);
```

---

## 📖 리플렉션으로 정보 읽기
클래스 객체를 얻으면 그 클래스에 대한 모든 정보를 읽을 수 있습니다.
- 필드
- 메서드
- 생성자
- 어노테이션

### 필드 정보
```java
Class<?> clazz = UserService.class;
Field[] fields = clazz.getDeclaredFields();

for (Field field : fields) {
    System.out.println("필드명: " + field.getName());
    System.out.println("타입: " + field.getType().getName());
    System.out.println("접근제어자: " + Modifier.toString(field.getModifiers()));
    
    // 어노테이션 확인
    if (field.isAnnotationPresent(Autowired.class)) {
        System.out.println("→ @Autowired 발견!");
    }
    System.out.println("---");
}
```

### 메서드 정보
```java
Method[] methods = clazz.getDeclaredMethods();

for (Method method : methods) {
    System.out.println("메서드명: " + method.getName());
    System.out.println("리턴타입: " + method.getReturnType().getName());
    
    // 파라미터 정보
    Parameter[] params = method.getParameters();
    for (Parameter param : params) {
        System.out.println("  파라미터: " + param.getName() + 
                         " (" + param.getType().getName() + ")");
    }
    
    // 어노테이션 확인
    if (method.isAnnotationPresent(Transactional.class)) {
        System.out.println("→ @Transactional 발견!");
    }
}
```

---

## ✏️ 리플렉션으로 조작하기

리플렉션은 정보를 읽는 것뿐만 아니라 실제로 조작도 가능합니다.
- 객체생성
- 필드 R/W
- 메서드 호출

### 1. 객체 생성
```java
Class<?> clazz = UserService.class;

// 기본 생성자로 생성
Constructor<?> constructor = clazz.getDeclaredConstructor();
Object instance = constructor.newInstance();

// 파라미터 있는 생성자로 생성
Constructor<?> constructor2 = clazz.getDeclaredConstructor(
    UserRepository.class
);
Object instance2 = constructor2.newInstance(repository);
```

### 2. 필드 값 읽고 쓰기
```java
UserService service = new UserService();
Class<?> clazz = service.getClass();

// private 필드 접근
Field field = clazz.getDeclaredField("userRepository");
field.setAccessible(true);  // private 접근 가능하게

// 값 읽기
Object value = field.get(service);

// 값 쓰기
UserRepository newRepo = new UserRepository();
field.set(service, newRepo);
```

### 3. 메서드 호출
```java
UserService service = new UserService();
Class<?> clazz = service.getClass();

// public 메서드 호출
Method method = clazz.getMethod("findById", Long.class);
Object result = method.invoke(service, 1L);

// private 메서드 호출
Method privateMethod = clazz.getDeclaredMethod("validateId", Long.class);
privateMethod.setAccessible(true);
Object result2 = privateMethod.invoke(service, 1L);
```

---

## 🌱 Spring Boot가 리플렉션을 사용하는 방법

Spring Boot의 컴포넌트 스캔과 의존성 주입은 모두 리플렉션 기반입니다.

### 1단계: 컴포넌트 스캔
```java
// Spring이 내부적으로 하는 일
String basePackage = "com.example";

// 패키지의 모든 .class 파일 찾기
for (String className : findAllClasses(basePackage)) {
    // 클래스 로딩
    Class<?> clazz = Class.forName(className);
    
    // @Component, @Service 등 확인
    if (clazz.isAnnotationPresent(Component.class) ||
        clazz.isAnnotationPresent(Service.class) ||
        clazz.isAnnotationPresent(Repository.class)) {
        
        System.out.println("빈으로 등록: " + clazz.getName());
        // 빈 생성 로직 실행...
    }
}
```

### 2단계: 빈 생성
```java
// UserService를 빈으로 등록
Class<?> clazz = UserService.class;

// 생성자 찾기
Constructor<?> constructor = clazz.getDeclaredConstructor();

// 객체 생성
Object bean = constructor.newInstance();

// Spring 컨테이너에 등록
beanFactory.registerBean("userService", bean);
```

### 3단계: 의존성 주입 (필드 주입)
```java
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;  // 여기에 주입!
}

// Spring이 하는 일
Class<?> clazz = UserService.class;
Object bean = beanFactory.getBean("userService");

// 모든 필드 검사
for (Field field : clazz.getDeclaredFields()) {
    if (field.isAnnotationPresent(Autowired.class)) {
        // 필요한 타입의 빈 찾기
        Class<?> fieldType = field.getType();  // UserRepository
        Object dependency = beanFactory.getBean(fieldType);
        
        // private 필드 접근 가능하게
        field.setAccessible(true);
        
        // 의존성 주입!
        field.set(bean, dependency);
        
        System.out.println("주입 완료: " + field.getName());
    }
}
```

### 4단계: 초기화 메서드 호출
```java
@Service
public class UserService {
    @PostConstruct
    public void init() {
        System.out.println("초기화!");
    }
}

// Spring이 하는 일
for (Method method : clazz.getDeclaredMethods()) {
    if (method.isAnnotationPresent(PostConstruct.class)) {
        method.invoke(bean);  // init() 호출
    }
}
```

### 5단계: AOP 프록시 생성
```java
@Service
public class UserService {
    @Transactional
    public void save(User user) {
        // 저장 로직
    }
}

// Spring이 하는 일
Class<?> clazz = UserService.class;
Method method = clazz.getMethod("save", User.class);

if (method.isAnnotationPresent(Transactional.class)) {
    // 프록시 객체 생성
    Object proxy = Proxy.newProxyInstance(
        clazz.getClassLoader(),
        clazz.getInterfaces(),
        (proxyObj, m, args) -> {
            System.out.println("트랜잭션 시작");
            try {
                Object result = m.invoke(bean, args);  // 원본 메서드 호출
                System.out.println("트랜잭션 커밋");
                return result;
            } catch (Exception e) {
                System.out.println("트랜잭션 롤백");
                throw e;
            }
        }
    );
}
```

---

## ⚠️ 리플렉션의 성능 고려사항

**리플렉션이 느린 이유**
```
1. 타입 체크를 런타임에 해야 함
   → 컴파일러가 도와줄 수 없음

2. 메서드 이름을 문자열로 찾아야 함
   → 메서드 테이블 탐색 오버헤드

3. JVM 최적화가 어려움
   → 인라이닝 같은 최적화 불가능

4. 보안 검사가 필요함
   → setAccessible() 호출 시 권한 확인
```

**성능 비교**
```java
// 일반 호출 (빠름)
service.findById(1L);  // 1나노초

// 리플렉션 호출 (느림)
Method method = clazz.getMethod("findById", Long.class);
method.invoke(service, 1L);  // 100나노초

약 100배 느림!
```

**Spring의 해결책: 캐싱**
```java
// 애플리케이션 시작 시 (단 1회)
Class<?> clazz = UserService.class;
Method method = clazz.getMethod("findById", Long.class);
Object bean = clazz.newInstance();

// 캐싱
cache.put("userService", bean);
cache.put("findById", method);

// 실제 실행 시 (수천번)
Object bean = cache.get("userService");  // 캐시에서 가져옴
bean.findById(1L);  // 일반 메서드 호출
```

---

