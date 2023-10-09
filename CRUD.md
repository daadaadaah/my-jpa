# 🎯 Entity Create
## 🚀 JPA
### 연관관계가 없는 기본적인 엔티티
```java
em.persist(memberA);
```
- 위 명령어로 `회원A`를 영속화하면, 영속성 컨텍스트는 1차 캐시에 회원 엔티티를 저장하면서 동시에, 회원 엔티티 정보로 등록 쿼리(Insert SQL)을 만들어 쓰기 지연 SQL 저장소에 보관한다.
- 그렇게 차곡차곡 쿼리를 모아둔 후, 트랜잭션을 commit 할 때 모아둔 쿼리를 DB에 보낸다. -> 트랜잭션을 지원하는 쓰기 지연

### 연관관계가 있는 엔티티
- WIP

## 🚀 JPQL 또는 QueryDSL



# 🎯 Entity Read
## 🚀 JPA
### 연관관계가 없는 기본적인 엔티티
```java
em.find(memberA);
```
- 위 명령어로 `회원A`를 호출하면, 
1) 먼저, 1차 캐시에서 엔티티가 있는지 찾아보고,
2-1) 만약 1차 캐시에 엔티티가 있다면, 그 엔티티를 반환한다.
2-2) 만약, 1차 캐시에 엔티티가 없다면, DB에서 조회한 후
3) 조회환 데이터로 엔티티를 생성해서 1차 캐시에 저장한 후
4) 조회한 엔티티를 반환한다.
- 만약, 동일한 식별자로 엔티티를 조회하면, 조회할 떄마다 동일한 인스터스가 조회된다.
- 따라서, 영속성 컨텍스트로 인해 `엔티티 동일성`이 보장되고, DB 접근 없이 엔티티를 조회할 수 있어서 `성능상 이점`이 있다.


### 연관관계가 있는 엔티티
- WIP

### 연관관계가 없는 기본적인 엔티티
- JPA는 영속성 컨텍스트에 엔티티가 있는지 조회 후 없으면 DB 조회를 하는 것과 달리, DB 조회 먼저 한 후 가져온 데이터가 영속성 컨텍스트에 동일한 값이 있다면 DB에서 가져온 값을 버리게 된다. 

## 🚀 JPQL 또는 QueryDSL


## JPA와 JPQL이 다르게 동작하는 이유는 무엇일까?



# 🎯 Entity Update
## 🚀 JPA
### 연관관계가 없는 기본적인 엔티티
- JPA로 엔티티를 수정할 떄, 단순히 엔티티를 조회해서 데이터만 변경하면 된다.
- 왜냐하면, 엔티티의 변경사항을 DB에 자동반영하는 변경 감지 기능(dirty checking)이 있기 때문이다.
- 단, 변경 감지는 영속성 컨텍스트가 관리하는 영속 상태의 엔티티에만 적용된다.
- 따라서, 비영속, 준영속처럼 영속성 컨텍스트의 관리를 받지 못하는 엔티티는 값을 변경해도 DB에 반영되지 않는다. 
- JPA는 엔티티를 영속성 컨텍스트에 보관할 때, 최초 상태를 복사해서 스냅샷으로 저장해둔다.
```java
Member memberA = em.find(memberA);
memberA.setAge(newAge);
```
- 위 명령어로 `회원 A`의 나이를 수정하면, 변경 감지 기능으로 인해 다음과 같은 순서로 동작한다.
1) TX를 commit 하면, 엔티티 메니저 내부에서 먼저 flush()가 호출된다.
2) 그렇게 flush 시점에 엔티티와 스냅샷을 비교해서 변경된 엔티티를 찾는다.
3) 이떄, 변경된 엔티티가 있으면 수정 쿼리를 생성해서, 쓰기 지연 SQL 저장소에 보낸다.
4) 그렇게 차곡차곡 쿼리를 모아둔 후 쓰기 지연 저장소의 SQL을 DB에 보낸다.
5) DB TX를 commit 한다.
- 이때, JPA의 기본 전략은 엔티티의 모든 필드를 업데이트한다.
- 따라서, 만약, 필드가 많거나 저장되는 내용이 너무 크면, 수정된 데이터만 사용해서 동적으로 UPDATE SQL을 생성하는 전략을 선택하면 된다.
- 그런데, 한 테이블에 컬럼이 많다는 것은 어쩌면 잘못된 설계라는 시그널이다.
- 따라서, 설계를 수정해보는 것도 좋다.

> 엔티티의 모든 필드를 업데이트 하면 좋은 점은 무엇인가?
> 1. 모든 필드를 사용하면, 바인됭되는 데이터가 다르더라도, 수정 쿼리가 항상 같다. 따라서, 애플리케이션 로딩 시점에 수정 쿼리를 미리 생성해두고 재사용할 수 있다.
> 2. DB에 동일한 쿼리를 보내면, DB는 이전에 한번 파싱된 쿼리를 재사용할 수 있다. 

> 어떻게 수정된 데이터만 사용해서 동적으로 UPDATE SQL을 생성할 수 있나?
```java
@Entity
@org.hibernate.annotations.DynamicUpdate // 이거 사용하면 됨!
@Table(name = "Member")
public class Member {

}
```
- 참고로, 데이터를 저장할 때 데이터가 존재하는(null이 아닌) 필드만으로 INSERT SQL을 동적으로 생성하는 @DynamicInsert도 있다.


### 연관관계가 있는 엔티티
- WIP


## 🚀 JPQL 또는 QueryDSL
- WIP

# 🎯 Entity Delete
## 🚀 JPA
### 연관관계가 없는 엔티티
- 엔티티를 삭제하려면, 먼저 삭제 대상 엔티티를 조회해야 한다.
```java
Member memberA = em.find(member);
em.remove(memberA);
```
1. em.remove(memberA)를 호출하는 순간, memberA는 영속성 컨텍스트에서 제거된다.
2. 그리고, 삭제 쿼리를 쓰기 지연 SQL 저장소에 등록한다.
3. 이후 TX를 commit 해서 flush를 호출하면, 실제 DB에 삭제 쿼리가 전달된다.


### 연관관계가 있는 엔티티
- WIP


## 🚀 JPQL 또는 QueryDSL
- WIP
  