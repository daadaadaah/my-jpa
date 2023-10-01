package study.datajpa.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import study.datajpa.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

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
}
