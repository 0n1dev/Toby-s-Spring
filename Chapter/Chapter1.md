# 오브젝트와 의존관계

> 객체지향 프로그래밍이 제공하는 폭넓은 혜택을 누릴 수 있도록 기본으로 돌아가자는 것이 스프링의 철학

## 1.1 초난감 DAO

### DAO

DAO(Data Access Object)는 DB를 사용해 데이터를 조회하거나 조작하는 기능을 전담하도록 만든 오브젝트

### 자바빈

자바빈은 두 가지 관례를 따라 만들어진 오브젝트

- 디폴트 생성자: 툴이나 프레임워크에서 리플렉션을 이용해 오브젝트를 생성하기 때문에 필요
- 프로퍼티: 자바빈이 노출하는 이름을 가진 속석을 프로퍼티 setter, getter을 가지고 있음

### 스프링을 공부한다는 것

- 코드 개선
- 코드 개선으로 장점과 미래의 유익
- 객체지향 설계의 원칙과의 상관관계
- 지금 DAO를 개선하는 경우와 그대로 사용하는 경우 차이?

## 1.2 DAO 분리

### 관심사의 분리

**왜 필요한가?**

관심사를 분리해서 개발하지 않으면 DB 접속용 암호를 변경하려고 DAO 클래스 수백개를 모두 수정하는 상황이 오거나, 다른 개발자가 개발한 코드에 변경이 생기면 내가 만든 클래스도 함께 수정을 해줘야 할수도 있따. (단일 책임 원칙, 개방 폐쇄 원칙)

**관심사 분리 특징**

- 관심이 같은 것끼리 하나의 객체 안으로 또는 친한 객체로 모이게 함
- 관심이 다른 것은 가능한 따로 떨어져서 서로 영향을 주지 않도록 분리
- 같은 관심에 효과적으로 집중할 수 있게 만듬

### 커넥션 만들기 추출

> UserDao를 보면 add() 메소드 하나에서만 적어도 3가지 관심사항 발견 가능

```java
public class UserDao {

    public void add(User user) throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver"); // MySQL 드라이버 클래스의 정보를 얻어옴
        Connection c = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/springboot", "root", "springtest"); // 커넥션 정보 가져옴

        PreparedStatement ps = c.prepareStatement(
                "insert into users(id, name, password) values(?, ?, ?)");
        ps.setString(1, user.getId());
        ps.setString(2, user.getName());
        ps.setString(3, user.getPassword());

        ps.executeUpdate();

        ps.close();
        c.close();
    }

    public User get(String id) throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");
        Connection c = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/springboot", "root", "springtest");

        PreparedStatement ps = c.prepareStatement(
                "select * from users where id = ?");
        ps.setString(1, id);

        ResultSet rs = ps.executeQuery();
        rs.next();
        User user = new User();
        user.setId(rs.getString("id"));
        user.setName(rs.getString("name"));
        user.setPassword(rs.getString("password"));

        rs.close();
        ps.close();
        c.close();

        return user;
    }
}
```

- DB와 연결을 위한 커넥션을 어떻게 가져올까라는 관심
- 사용자 등록을 위해 DB에 보낼 SQL 문장을 담은 Statement를 만들고 실행
- 작업이 끝나면 사용한 리소스를 시스템에 돌려주는 것

### 중복 코드의 메소드 추출

> 커넥션을 가져오는 중복된 코드를 분리

```java
    /**
     * 관심사 분리 (커넥션 가져오는 중복되는 코드 분리)
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    private Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");
        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/springboot", "root", "springtest");
    }
```

-> 변경사항 검증 O

### DB 커넥션 만들기의 독립

> 변화에 대응하는 수준이 아니라 아예 변화를 반기는 DAO를 만들어보자

- 데이터베이스 변경에 유연한 코드 작성
- MySQL, PostGreSQL 사용

-> PostGreSQL 사용해서 INSERT 시키면 공백이 들어감 왜그런지 못찾음 trim()으로 임시처리

### 템플릿 메소드 패턴

- 슈퍼클래스에서 기본적인 로직의 흐름을 만들고, 그 기능의 일부를 추상 메소드나 오버라이딩 가능한 protected 메소드 등으로 만든뒤 서브 클래스에서 해당 메소드를 필요에 맞게 구현하여 사용하는 패턴

### 팩토리 메소드 패턴

- 팩토리 메소드 패턴은 템플릿 메소드 패턴과 마찬가지로 상속을 통해 기능을 확장하는 패턴
- 주로 타입으로 오브젝트를 리턴하므로 서브클래스에서 정확히 어떤 클래스를 리턴할지는 슈퍼클래스에서 알지 못함

## 1.3 DAO의 확장

> 1.2에서 재해석해서 작성하려다 보니 1.3에서 설명하려는 내용과 중복되어 다시 작성

### 초난감 DAO의 기존 문제점

- 확장에 필요한 유연성 부족
- DB 연결 방법을 확장하려면 DAO 내부도 변경이 필요
- OCP에 어긋남

### 해결

- interface로 커넥션 분리
- 높은 응집도
    - 응집도가 높다는것은 변화가 일어날 때 **해당 모듈**의 변화가 크다는것
- 낮은 결합도
    - 낮은 결합도(느슨한 연결 관계)는 하나의 오브젝트가 변경이 일어날 때 관계를 맺고있는 다른 오브젝트에게 변화를 요구하는 정도가 낮다는 의미

### 전략 패턴

> MySQLDaoTest - MySQLDao - ConnectionMaker 구조를 디자인 패턴으로 보면 전략 패턴이라고 볼 수 있다.

- FConnection이라는 전략
- SConnection이라는 전략
- MySQLDao는 전략 패턴의 컨텍스트에 해당
- MySQLDaoTest에서 필요한 전략을 컨텍스트에 넣음 (전략을 바꿔가면서 사용 가능)

## 1.4 오브젝트 팩토리

1.3에서 작성한 Test코드를 보면 Test코드에서 어떤 구현 클래스를 사용할지 결정하는 기능을 주었다. <br />
테스트 코드는 말 그대로 테스트라는 관심사를 이미 가지고 있기 때문에 분리를 하자.

### 팩토리

객체의 생성 방법을 결정하고 그렇게 만들어진 오브젝트를 돌려주는 오브젝트를 `팩토리`라고 부른다.

```java
public class DaoFactory {

    public MySQLDao mySqlDao() {
        return new MySQLDao(connectionMaker());
    }

    public ConnectionMaker connectionMaker() {
        return new FConnection();
    }
}
```

DaoFactory를 보면 Dao 오브젝트를 만들어서 반환해주는 역할을하고 있다.

### 제어관계 역전

- 프로그램의 제어 흐름 구조가 뒤바뀌는 것
- 일반적인 프로그램의 흐름은 `main()` 메소드와 같이 프로그램이 시작되는 지점에서 **사용할 오브젝트 결정**, **오브젝트 생성**, **오브젝트에 있는 메소드 호출** ... 반복 (작업을 사용하는 쪽에서 제어하는 구조)
- 제어의 역전은 오브젝트가 **자신이 사용할 오브젝트를 스스로 선택하지 않음** (당연히 생성도 X)
- 제어 권한을 가진 컨테이너가 적절한 시점에 클래스의 오브젝트를 만들고 그 안의 메소드를 호출

-> 원래는 UserDao에서 어떤 Connection으로 연결할지 결정을 했다고 생각하면 DaoFactory가 공급해주는 Connection을 수동적으로 사용하여 제어가 역전되었다고 볼 수 있다. (가장 간단한 IoC 컨테이너 혹은 IoC 프레임워크 구현) <br />
-> 애플리케이션 컴포넌트의 생성과 관계설정, 사용, 생명주기 등을 관장하는 존재가 필요 (코드에서 DaoFactory의 역할)