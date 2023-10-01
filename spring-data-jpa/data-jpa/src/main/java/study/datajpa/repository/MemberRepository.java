package study.datajpa.repository;

import java.util.List;
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
}
