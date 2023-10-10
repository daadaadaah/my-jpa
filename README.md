# JPA란 무엇인가?
- 자바 진영의 ORM 기술 표준으로, 자바 애플리케이션과 RDBMS 사이에서 개발자를 대신해서 ~ 해결해주는 데이터 접근 기술 중 하나이다.

# JPA를 왜 사용할까?
### 1. 개발 생산성이 향상되기 때문에
- JPA를 사용하면, 개발자 대신 JPA가 CRUD용 SQL문을 만들고, 반복되는 JDBC API 코드를 대신 작성해주어서, 개발 생산성이 향상된다.
- 심지어, DDL 문을 자동으로 생성해주는 기능도 있다.   

### 2. 유지보수에 용이하기 때문에
- 만약, SQL을 직접 다루면 엔티티에 필드를 하나만 추가해도 관련된 SQL과 JDBC API 코드를 모두 변경해줘야 한다.
- JPA를 사용하면, 필드를 추가하거나 삭제된 경우, 개발자가 작성해야 했던 SQL과 JDBC API 코드를 JPA가 대신 처리해주기 때문에, 수정해야 할 코드가 줄어들어 유지보수에 용이하다.

### 3. 영속성 컨텍스트을 활용하여 성능적으로 이점을 얻을 수 있기 때문에
1. 1차 캐시 : 데이터를 재사용할 수 있어, DB 부하를 줄일 수 있다.
2. 지연 로딩 : 연관된 엔티티가 실제로 필요할 때만 로드되며, 불필요한 데이터베이스 쿼리를 피할 수 있다.
3. 쓰기 지연 : JPA는 쓰기 지연을 지원하여 트랜잭션을 커밋할 때까지 변경된 엔티티를 일괄적으로 데이터베이스에 적용하므로, 이로 인해 쓰기 작업을 최적화하고 데이터베이스 부하를 줄일 수 있다.

### 4. 패러다임의 불일치 해결

### 5. 데이터 접근 추상화와 벤더 독립

### 6. 표준

# mybatis랑 어떤 차이가 있는 걸까?
- mybatis은 SQL 매퍼로, 이름 그대로 객체와 SQL을 매핑해준다.
- 따라서, mybatis를 사용할 경우, SQL과 매핑할 객체만 지정하면, 지루하게 반복되는 JDBC API 사용과 응답 결과를 객체로 매핑하는 일은 개발자 대신 mybatis에서 알아서 해준다.
- 다만, mybatis를 사용하더라도, 개발자가 SQL을 직접 작성해야 하므로, SQL에 의존하는 개발을 피할 수 없다.
- 반면, JPA은 객체와 테이블을 매핑만하면, SQL을 알아서 만들어서 DB와 통신하므로, SQL에 의존하는 개발을 피할 수 있다.

# JPA 는 어떻게 사용하는게 좋을까?
> [영한님 팁](https://www.inflearn.com/questions/38771/querydsl%EA%B3%BC-jpql%EC%9D%84-%EC%84%A0%ED%83%9D%ED%95%98%EB%8A%94-%EC%B0%A8%EC%9D%B4%EA%B0%80-%EA%B6%81%EA%B8%88%ED%95%A9%EB%8B%88%EB%8B%A4) 
<img width="533" alt="스크린샷 2023-10-10 오후 4 07 05" src="https://github.com/daadaadaah/my-jpa/assets/60481383/f39f73b7-8bfe-4f84-b866-0c6fc87ae55a">

- 정리하면, (사실 자기가 속한 프로젝트 컨벤션 대로 작성하는게 최고, JPQL 을 선호하는 컨벤션이면 JPQL로, QueryDSL를 선호하는 컨벤션이면, QueryDSL로!)
1. 스프링 데이터 JPA에서 지원해주는 메소드들을 사용하기
2. 복잡한 쿼리 중에 비교적 단순한 쿼리는 @Query로 JPQL 사용 -> 커스텀 클래스 없이 편리하게 개발 할 수 있는게 장점인데, 그것 말고는 사실 장점이 없다.
3. 2번보다 복잡하면, QueryDSL로 처리
- `XXXXRepositoryCustom 인터페이스`와 `XXXXRepositoryImpl implements MemberRepositoryCustom` 클래스 만들어서 구현부 완성 -> 이게 사실 귀찮긴 함. 그레도, 나중에 컴파일 시점에 문제들도 쉽게 찾을 수 있고 코드 추적도 쉬워서 더 좋다.
4. 동적 쿼리는 QueryDSL로 처리

- 다음은 예시 코드다.

```java
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

    List<Member> findByUsername(String username); // bulkAgePlus2 테스트용으로 추가

    /**
     * [스프링 데이터 JPA가 제공하는 쿼리 메소드 기능 1] 메서드 이름으로 쿼리 생성
     * 네이밍 참고 : https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods.query-creation
     */
    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

    /**
     * [스프링 데이터 JPA가 제공하는 쿼리 메소드 기능 3] @Query 어노테이션을 사용해서 리포지토리 인터페이스에 쿼리 직접 정의
     * 장점 : JPA Named 쿼리처럼 애플리케이션 실행 시점에 쿼리 문법 오류를 발견할 수 있다.
     */
    // 엔티티 조회
    @Query("select m from Member m where m.username = :username and m.age = :age")
    List<Member> findUser(@Param("username") String username, @Param("age") int age);

    // 단순히 값 하나 조회
    @Query("select m.username from Member m")
    List<String> findUsernameList();

    // DTO 조회
    @Query("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
    List<MemberDto> findMemberDto();

    /**
     * 벌크성 수정 쿼리
     */
    @Modifying(clearAutomatically = true)
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age);

    /**
     * N+1 문제 : fetch join으로 해결
     */
    @Query("select m from Member m left join fetch m.team")
    List<Member> findMemberFetchJoin();

    @Override
    @EntityGraph(attributePaths = {"team"}) // Member 조회시, team도 같이 조회할꺼야
    List<Member> findAll();

    // 위 메서드와 동작 같음
    @EntityGraph(attributePaths = {"team"}) // Member 조회시, team도 같이 조회할꺼야
    @Query("select m from Member m")
    List<Member> findMemberEntityGraph();

    @EntityGraph(attributePaths = {"team"}) // Member 조회시, team도 같이 조회할꺼야
    List<Member> findEntityGraphByUsername(@Param("username") String username);

    /**
     * 네이티브 쿼리 -> 네이티브 SQL을 DTO로 조회할 때에는 JDBC Template or myBatis를 사용하는 걸 권장함
     * - 가급적 네이티브 쿼리는 사용하지 않은게 좋음. 정말 어쩔 수 없을 때 사용
     * - DTO를 뽑는데, 좀 더 편하게 뽑을 수 있는거, 그런데 네이티브 쿼리야, 그런데 동적 쿼리는 아니야 할 때, 스프링 데이터 Projections 활용해보자.
     */
    @Query(value = "select * from member where username = ?", nativeQuery = true)
    Member findByNativeQuery(String username);

    /**
     *  네이티브 쿼리 + 인터페이스 기반 Projections 활용
     */
    @Query(
        value = "select m.member_id as id, m.username, t.name as teamName from member m left join team t",
        countQuery = "select count(*) form member",
        nativeQuery = true
    )
    Page<MemberProjection> findByNativeProjection(Pageable pageable);
}
```

```java
public interface MemberRepositoryCustom {
    List<MemberTeamDto> search(MemberSearchCondition condition);
}
```

```java
public class MemberRepositoryImpl implements MemberRepositoryCustom  {

    private final JPAQueryFactory queryFactory;

    public MemberRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<MemberTeamDto> search(MemberSearchCondition condition) {
        return queryFactory
            .select(new QMemberTeamDto(
                member.id,
                member.username,
                member.age,
                team.id,
                team.name))
            .from(member)
            .leftJoin(member.team, team)
            .where(usernameEq(condition.getUsername()),
                teamNameEq(condition.getTeamName()),
                ageGoe(condition.getAgeGoe()),
                ageLoe(condition.getAgeLoe()))
            .fetch();
    }

    private BooleanExpression usernameEq(String username) {
        return isEmpty(username) ? null : member.username.eq(username);
    }
    private BooleanExpression teamNameEq(String teamName) {
        return isEmpty(teamName) ? null : team.name.eq(teamName);
    }
    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe == null ? null : member.age.goe(ageGoe);
    }
    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe == null ? null : member.age.loe(ageLoe);
    }
}
```



# JPA 잘 정리된 블로그
- https://cheese10yun.github.io/tags/#JPA

