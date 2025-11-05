# 자바 모듈

---

## 주요 내용
* 자바의 플랫폼 모듈
* 접근 제어 의미론의 변경
* 모듈화된 애플리케이션 작성
* 다중 릴리스 JAR

---

자바 플랫폼 모듈은 자바9에서 부터 제공 되었다. 향후 모듈의 도입은 애플리케이션 아키텍처에 깊은 영향을 미치며, 모듈은 프로세스 풋프린트, 시작 비용, 웜업 시간
같은 측면에서 관심이 있는 현대적 프로젝트에 많은 장점을 제공한다. 모듈은 또한 복잡한 의존성으로 자바 애플리케이션을 괴롭히는 소위 JAR 지옥 문제를 해결하는데 도움이 될 수 있다.

### 배경설명
모듈은 자바9부터 도입된 자바 언어에서 기본적으로 새로운 개념이다. 모듈은 런타임에 의미를 가지는 응용 프로그램 배포 및 의존성의 단위이다.
이는 다음과 같은 기존 자바 개념과 다르다.
* JAR파일은 런타임에 보이지 않으며, 단순휘 클래스 파일들을 포함하고 있는 압축된 디렉토리이다.
* 패키지는 실제로 접근 제어를 위해 클래스를 그룹화하기 위한 네임스페이스다.
* 의존성은 클래스 레벨에서만 정의된다.
* 접근 제어와 리플렉션이 결합돼 명확한 배포 단위 경계 없이 최소한의 시행으로, 근본적으로 개방적인 시스템을 생성한다.


 #### 리플렉션이란?
 자바 프로그램이 실행 중에 클래스의 메타데이터(클래스 이름, 필드, 메서드, 생성자 등)를 동적으로 조회하고 조작할 수 있게 해주는 기능을 말한다.
 보통 자바에서는 new 라는 키워드로 객체를 만들고, 클래스나 메서드 이름을 컴파일 시점에 확정해야하는데, 리플렉션을 사용하면 클래스 이름을 문자열로 받아서 동적으로
 정보를 읽거나 객체를 생성 할 수 있다.

```java
    Class<?> clazz = Class.forName("com.example.MyClass");
    
    // 객체 생성
    Object obj = clazz.getDeclaredConstructor().newInstance();
    
    // 메서드 호출
    Method method = clazz.getDeclaredMethod("sayHello");
    method.invoke(obj);
    
    // 필드 접근
    Field field = clazz.getDeclaredField("name");
    field.setAccessible(true);
    field.set(obj, "Reflection Test");
    
    System.out.println(field.get(obj)); // "Reflection Test"
```
여기서 Class.forName()은 문자열로 클래스 이름을 받아서 Class 객체를 로드하고,
getDeclaredMethod() 나 getDeclaredField()로 런타임 시점에 내부 구조를 조작할 수 있다.

테스트 코드를 작성할때, 리플렉션을 많이 사용한다.
예를 들어 
```java
public class User {
    private int age;

    private boolean isAdult() {
        return age >= 20;
    }
}
```
이런 private은 외부에서 접근이 어렵다.
그런데 테스트를 하려면 내부 상태나 private 메서드를 검증하고 싶을 수 있잖음?

이럴때 잠깐 써서 "열고 보는"거다.
```java
@Test
void testIsAdult() throws Exception {
    User user = new User();

    Field field = User.class.getDeclaredField("age");
    field.setAccessible(true); // private 무시
    field.set(user, 25); // age 필드에 값 주입

    Method method = User.class.getDeclaredMethod("isAdult");
    method.setAccessible(true);
    boolean result = (boolean) method.invoke(user); // private 메서드 실행

    assertTrue(result);
}
```
이형태로 age 자체는 private 이지만 열어서 테스트가 가능하다.

### Spring 같은 프레임워크를 사용한다면
Spring이 내부적으로 의존성 주입(DI)을 리플렉션으로 처리한다.
테스트 환경에서 스프링 컨테이너가 private 필드에 Mock 객체를 "리플렉션으로 꽂아넣는" 것이다.
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService; // <-- 내부적으로 리플렉션으로 주입됨

    @Mock
    private UserRepository userRepository;
}
```
여기서 @InjectMocks는 리플렉션을 이용해서
userService의 public UserRepository userRepository 필드에
Mock 객체를 강제로 넣어준다.

다시 돌아와서...

### 모듈은 다음같은 특징을 가진다.
* 모듈간의 의존성 정보를 정의하므로 컴파일 또는 애플리케이션 시작 시점에서 모든 종류의 해결과 연결 문제를 감지 할 수 있다.
* 적절한 캡슐화를 제공해서 내부 패키지와 클래스를 조작하려는 사용자로 부터 안전하게 보호 할 수 있다.
* 최신 자바 런타임에서 이해하고 사용할 수 있는 메타 데이터가 포함된 적절한 배포 단위이며, 자바 타입 시스템에서(예: 리플렉션을 통해) 표현된다.

### 모듈 시스템의 목표는 배포 단위(모듈)를 가능한 한 서로 독립적으로 만드는 것이다.
비록 실제 애플리케이션은 관련 기능(예: 보안)을 제공하는 모듈의 그룹에 종속될 수 있지만, 모듈은 개별적으로 로드 및 링크도리 수 있도록 설계 됐다.

---

## 프로젝트 직소
OpenJDK 내부에 프로잭트는 아래와 같은 목표를 가지며 모든 기능을 갖춘 모듈화 솔루션을 제공을 하는것을 목표로 한다.
* JDK플랫폼 소스 모듈화하기
* 프로세스 풋프린트 줄이기
* 애플리케이션 시작 시간 개선
* JDK와 애플리케이션 코드에서 모듈 사용할 수 있게 하기
* 자바에서 처음으로 진정한 의미의 엄격한 캡슐화 허용

