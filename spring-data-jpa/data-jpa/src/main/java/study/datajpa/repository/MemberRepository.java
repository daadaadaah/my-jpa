package study.datajpa.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;

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
