package study.querydsl;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before() {
        queryFactory = new JPAQueryFactory(em);

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    public void startJPQL() {
        String qlString = "select m from Member m where m.username = :username";

        Member findMember = em.createQuery(qlString, Member.class)
                                .setParameter("username", "member1")
                                .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    /**
     * 같은 테이블을 조인해야 하는 상황이라면, 다음과 같이 별칭을 직접 지정해서 사용하고
     *      QMember qMember = new QMember("m"); //별칭 직접 지정
     * 그렇지 않으면, Q-Tpye(예 : QMember) import해서 기본 인스턴스를 사용하자
     */
    @Test
    public void startQuerydsl() {
        Member findMember = queryFactory
                                    .select(member)
                                    .from(member)
                                    .where(member.username.eq("member1"))//파라미터 바인딩 처리
                                    .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    // 이름이 memeber1이면서, 나이가 10살이 사람 조회
    @Test
    public void searchAndVersion() {
        Member findMember = queryFactory
                                    .select(member)
                                    .from(member)
                                    .where(
                                        member.username.eq("member1")
                                        .and(member.age.eq(10))
                                    )//파라미터 바인딩 처리
                                    .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    // 이름이 memeber1이면서, 나이가 10살이 사람 조회
    @Test
    public void searchCommaVersion() { // 영한님 선호 방식!
        Member findMember = queryFactory
                                    .select(member)
                                    .from(member)
                                    .where(
                                        member.username.eq("member1"),
                                        member.age.eq(10)
                                    )//파라미터 바인딩 처리
                                    .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void resultFetch() {
        List<Member> fetchMembers = queryFactory
            .selectFrom(member)
            .fetch(); // 리스트 조회, 데이터 없으면 빈 리스트 반환

        assertThat(fetchMembers.size()).isEqualTo(4);
    }

    @Test
    public void resultFetchOne() {
        Member findMember = queryFactory
            .selectFrom(member)
            .where(member.username.eq("member1"))
            .fetchOne(); // 단 건 조회, 결과가 없으면 null, 결과가 둘 이상이면 com.querydsl.core.NonUniqueResultException 발생

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void resultFetchFirst() {
        Member findMember = queryFactory
            .selectFrom(member)
            .where(member.age.goe(10))
            .fetchFirst(); // limit(1).fetchOne()

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void resultFetchResults() {
        QueryResults<Member> results = queryFactory
            .selectFrom(member)
            .where(member.age.goe(10))
            .fetchResults(); // deprecated 됨

        long count = results.getTotal();

        List<Member> content = results.getResults();

        assertThat(count).isEqualTo(4);

        assertThat(content.get(0).getUsername()).isEqualTo("member1");
    }

    @Test
    public void resultFetchCount() {
        long count = queryFactory
            .selectFrom(member)
            .where(member.age.goe(10))
            .fetchCount(); // deprecated 됨

        assertThat(count).isEqualTo(4);
    }
}
