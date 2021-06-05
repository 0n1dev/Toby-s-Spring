# AOP

- 스프링 3대 기반 기술
    - IoC/DI
    - AOP (이번 챕터)
    - PSA

-> AOP를 활용하면 이전 챕터에서 배운 트랜잭션의 경계설정을 조금 더 세련되고 깔끔한 방식으로 바꿀수 있다.

## 트랜잭션 코드 분리

```java
    public void upgradeLevels() {

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            final List<User> users = userDao.getAll();

            for (User user : users) {
                if (canUpgradeLevel(user)) {
                    upgradeLevel(user);
                }
            }

            transactionManager.commit(status);
        } catch (RuntimeException e) {
            transactionManager.rollback(status);
            throw e;
        }
    }
```

### 비즈니스 코드

```java
            final List<User> users = userDao.getAll();

            for (User user : users) {
                if (canUpgradeLevel(user)) {
                    upgradeLevel(user);
                }
            }
```

위에 코드를 제외하고는 모두 트랜잭션 코드다

## AOP이해를 위한 디자인 패턴

### 프록시 패턴

- 자신이 클라이언트가 사용하려는 실제 대상인 것처럼 위장
- 대리자 역할 **프록시**
- 요청을 받아와 실제로 처리하는 실제 오브젝트를 **타깃**이라 부름

클라이언트 -> 프록시 -> 타깃

**사용 목적**

- 타깃 접근 방법을 제어
- 타깃에 부가적인 기능을 추가 (여기서 트랜젝션 추가!)

### 데코레이터 패턴

- 타깃에 부가적인 기능을 런타임시 다이나믹하게 부여해주기 위해 프록시를 사용하는 패턴
- 장식을 한는것으로 보여서 **데코레이터 패턴**이라 부름
- 프록시가 하나로 제한된게 아니라 프록시가 직접 타깃과 연결이 되지 않아도 됨

클라이언트 -> 라인넘버 데코레이터 -> 컬러 데코레이터 -> 페이징 데코레이터 -> 소스코드 출력

### 다이나믹 프록시

- 프록시의 문제점은 **요청을 타깃의 메소드에 위임**하는 처리도 모두 override 해주어야 함
- 프록시 클래스 내에 중복이 발생

-> 다이나믹 프록시는 이를 해결

- 자바에서 reflection API를 통해 프록시 구현

```java
Hello hello = (Hello) Proxy.newProxyInstance(
    Main.class.getClassLoader(),
    new Class[]{Hello.class},
    new UppercaseHandler(new HelloTarget())
);
```

- 첫 번째 인자: 프록시를 만든 클래스 로더
- 두 번째 인자: 어떤 인터페이스에 대해 프록시를 만들 것인지 명시
- 세 번째 인자: InvocationHandler 인터페이스의 구현체
- 리턴 값: 동적으로 만든 프록시 객체

-> 일일이 구현하는 문제는 reflection API가 해결, 중복은 InvocationHandler가 해결

**INvocationHandler**

> invoke()라는 메소드 하나만 가지고 있는 인터페이스

```java
public class UppercaseHandler implements InvocationHandler {

    Object target;

    public UppercaseHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object ret = method.invoke(target, args);
        if (ret instanceof String && method.getName().startWith("say")) {
            return ((String) ret).toUpperCase();
        } else {
            return ret;
        }
    }
}
```

- 첫 번째 인자: 프록시 객체
- 두 번째 인자: 클라이언트가 호출한 메소드 객체
- 세 번째 인자: 클라이언트가 메소드에 전달한 인자