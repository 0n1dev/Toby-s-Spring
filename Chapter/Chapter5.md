# 서비스 추상화(PSA)

- 자바에는 표준 스펙, 사용 제품, 오픈소스를 통틀어서 사용 방법과 사용 방법과 형식은 다르지만 기능과 목적이 유사한 기술이 존재
- 환경에 따라 기술이 바뀌었을 때 API를 사용하고 다른 스타일의 접근 방법을 따르는 건 매우 피곤한 일
- 스프링은 성격이 비슷한 종류의 기술을 추상화하고 이를 일관된 방법으로 사용할 수 있도록 지원

## 5.1 사용자 레벨 관리 기능 추가(리팩토링)

### 기존 코드

```java
public void upgradeLevels() {
    List<User> users = userDao.getAll();
    for (User user : users) {
        Boolean changed = null; // 레벨의 변화가 있는지 확인하는 플래그
        if (user.getLevel() == Level.BASIC && user.getLogin() >= 50) { // BASIC 레벨 업그레이드
            user.setLevel(Level.SILVER);
            changed = true;
        } else if (user.getLevel() == Level.SILVER && user.getRecommend() >= 30) { // SILVER 레벨 업그레이드
            user.setLevel(Level.GOLD);
            changed = true;
        } else if (user.getLevel() == Level.GOLD) { changed == false; } // GOLD는 변경 X
        else { changed = false; }

        if (changed) { userDao.update(user); }
    }
}
```

### 코드의 문제점

**작성된 코드를 살펴볼 때 생각 해야될 질문**

- 코드에 중복된 부분은 없는가?
- 코드가 무엇을 하는 것인지 이해하기 불편하지 않은가?
- 코드가 자신이 있어야 할 자리에 있는가?
- 앞으로 변경이 일어난다면 어떤 것이 있을 수 있고, 그 변화에 쉽게 대응할 수 있게 작성되어 있는가?

**upgradeLevels() 메소드의 문제점**

- for 루프 속에 if/else if/else 블록들이 읽기 불편
- 업그레이드 조건과, 조건에 대한 작업이 한곳에 모여있어서 로직 이해 어렵
- 플래그를 사용해서 마지막에 업데이트하여 깔끔한 코드가 아님

-> 성격이 다른 여러 가지 로직이 한곳에 섞여 있기 때문

```java
        if (user.getLevel() == Level.BASIC && user.getLogin() >= 50) { // BASIC 레벨 업그레이드
            user.setLevel(Level.SILVER);
            changed = true;
        }

        ...

        if (changed) { userDao.update(user); }
```

- `user.getLevel() == Level.BASIC` 현재 레벨 파악 로직
- `user.getLogin() >= 50` 업그레이드 조건을 담은 로직
- `user.setLevel(Level.SILVER)` 다음 단계의 레벨이 무엇이며 업그레이드를 위한 작업은 어떤 것인지 포함
- `changed = true` 로직 내부에서 필요는 없고 멀리 떨어져 있는 `if (changed) { userDao.update(user); }` 해당 코드의 작업이 필요한지 알려주기 위한 임시 플래그

-> 이러한 코드는 if 조건 블록이 레벨 개수만큼 반복됨 / 새로운 레벨이 추가된다면 Level 이늄도 수정해야하고 메서드도 수정해야 함

### 리팩토링

**기존 upgradeLevels 코드 리팩토링**

```java
public void upgradeLevels() {
    List<User> users = userDao.getAll();
    for (User user : users) {
        if (canUpgradeLevel(user)) {
            upgradeLevel(user);
        }
    }
}
```

- 역할과 책임이 명확해짐
- `canUpgradeLevel()`이 업그레이드 가능한지 true/false 리턴

**canUpgradeLevel 메서드 추가**

```java
private boolean canUpgradeLevel(User user) {
    Level currentLevel = user.getLevel();
    switch (currentLevel) {
        case BASIC: return (user.getLogin() >= 50);
        case SILVER: return (user.getRecommend() >= 30);
        case GOLD: return false;
        default: throw new IlleagalArgumentException("Unknown Level: " + currentLevel); // 현재 로직에서 다룰 수 없는 레벨이 주어지면 예외 발생 (새로운 레벨이 추가된 후 로직을 수정하지 않으면 에러 확인 가능)
    }
}
```

**upgradeLevel 메서드 추가**

```java
private void upgradeLevel(User user) {
    if (user.getLevel() == Level.BASIC) user.setLevel(Level.SILVER)
    else if (user.getLevel() == Level.SILVER) user.setLevel(Level.GOLD);
    userDao.update(user);
}
```

-> 해당 메서드는 좀 더 리팩토링이 필요하다 (레벨이 추가될 수록 if가 계속 길어짐)

**Level enum 변경**

```java
public enum Level {
    GOLD(3, null), SILVER(2, GOLD), BASIC(1, SILVER);

    private final int value;
    private final Level next;

    Level(int value, Level level) {
        this.value = value;
        this.next = level;
    }

    public int intValue() {
        return value;
    }

    public static Level valueOf(int value) {
        switch (value) {
            case 1:
                return BASIC;
            case 2:
                return SILVER;
            case 3:
                return GOLD;
            default:
                throw new AssertionError("Unknown value: " + value);
        }
    }

    public Level nextLevel() {
        return this.next;
    }

}
```

-> 다음 레벨을 볼수있도록 변경

**upgradeLevel 메서드 변경**

```java
private void upgradeLevel(User user) {
    Level nextLevel = this.level.nextLevel();
    if (nextLevel == null) {
        throw new IlleagalArgumentException(this.level + "은 업그레이드가 불가능합니다.");
    } else {
        this.level = nextLevel;
    }
}
```

### 최종적인 upgradeLevels 코드

```java
```java
public void upgradeLevels() {
    List<User> users = userDao.getAll();
    for (User user : users) {
        if (canUpgradeLevel(user)) {
            user.upgradeLevel();
            userDao.update(user);
        }
    }
}
```

## 트랜잭션 서비스 추상화

- 트랜잭션이란?
    - **더 이상 나눌 수 없는 단위 작업**
    - 작업 수행의 논리적인 단위
    - 데이터 부정합을 방지하기 위해 사용
- 트랜잭션이 충족 요건 **ACID**
    - 원자성
        - 완전히 **끝마치지 않은 경우 전혀 이루어지지 않은 것과 동일**
        - 모두 성공하거나 모두 실패하거나 둘중 하나
    - 일관성
        - 트랜잭션이 성공적으로 완료되면 **일관적인 DB 상태를 유지**하는 것을 말함
        - 여기서 일관성은 데이터 타입이 정수형인데 갑자기 문자열이 되지 않는 것을 말함
    - 격리성
        - 트랜잭션 수행시 **다른 트랜잭션의 작업이 끼어들지 못하도록 보장**하는 것을 말함
        - 트랜잭션 끼리 간섭 X
    - 지속성
        - 성공적으로 수행된 트랜잭션은 **영원히 반영**
        - commit을 하면 현재 상태를 영원히 보장

### UserService에 트랜잭션을 넣고 싶으면?

- `upgradeLevels()` 메소드를 DAO안으로 옮기는 방법
    - 비즈니스 로직과 데이터 로직을 한곳에 묶어버리는 결과 ... (단일 책임 원칙을 여태껏 지키려 했던게 무의미)
- Service에 트랜잭션을 넣으려면 **Connection 오브젝트를 계속 들고 다녀야 하는 문제점**
    - PlatformTransactionManager 주입

```java
    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public UserService(UserDao userDao, PlatformTransactionManager transactionManager) {
        this.userDao = userDao;
        this.transactionManager = transactionManager;
    }
```

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

-> PlatformTransactionManager를 어떤 의존성(DataSourceTransactionManger, JTATransactionManger) 주입을 받냐에 따라서 JDBC or JTA를 적용 (서비스 추상화)