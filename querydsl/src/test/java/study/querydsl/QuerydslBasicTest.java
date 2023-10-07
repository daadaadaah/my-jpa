package study.querydsl;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
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

    @Test
    public void sort() {
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));


        List<Member> result = queryFactory
            .selectFrom(member)
            .where(member.age.eq(100))
            .orderBy(
                member.age.desc(), // 정렬 1. 회원 나이 내림차순(desc)
                member.username.asc().nullsLast() // 정룔 2. 회원 이름 올림차순(asc) (단, 회원 이름이 없으면 마지막에 출력(nulls last) )
            )
            .fetch();

        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);
        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();
    }

    @Test
    public void aggregation() throws Exception {
        // 튜플 : 데이터 타입이 여러개 일 때 사용
        // 그런데, 실무에서는 튜플보다 직접 DTO로 조회하는 방법을 주로 사용한다.
        List<Tuple> result = queryFactory
            .select(
                member.count(),
                member.age.sum(),
                member.age.avg(),
                member.age.max(),
                member.age.min()
            )
            .from(member)
            .fetch();

        Tuple tuple = result.get(0);

        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    @Test
    public void group() throws Exception {
        // 팀의 이름과 각 팀의 평균 연령을 구해라.
        List<Tuple> result = queryFactory
            .select(team.name, member.age.avg())
            .from(member)
            .join(member.team, team)
            .groupBy(team.name)
            .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);

        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);
    }

    @Test
    public void groupWithHaving() throws Exception {
        // A 팀의 이름과 각 팀의 평균 연령을 구해라.
        List<Tuple> result = queryFactory
            .select(team.name, member.age.avg())
            .from(member)
            .join(member.team, team)
            .groupBy(team.name)
            .having(team.name.eq("teamA")) // having을 이용해서, 그룹화된 결과를 제한할 수 있다.
            .fetch();

        Tuple teamA = result.get(0);

        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);
    }

    @Test
    public void join() {
        // 팀A에 소속된 모든 회원
        List<Member> result = queryFactory
            .selectFrom(member)
            .join(member.team, team)
            .where(team.name.eq("teamA"))
            .fetch();

        assertThat(result)
            .extracting("username")
            .containsExactly("member1", "member2");
    }

    /**
     * 내부 조인일 때, on도 사용할 수 있지만, 익숙한 where을 사용하고, 외부 조인일 때에는 on 사용하자
     */
    @Test
    public void innerJoinOnVsWhere() {
        // 회원과 팀을 조인하는데, 회원은 모두 조회되고, 팀은 팀 이름이 teamA인 팀만 조회도도록 하기
        List<Tuple> resultWithOn = queryFactory
            .select(member, team)
            .from(member)
            .join(member.team, team)
            .on(team.name.eq("teamA"))
            .fetch();

        List<Tuple> resultWithWhere = queryFactory
            .select(member, team)
            .from(member)
            .join(member.team, team)
            .where(team.name.eq("teamA"))
            .fetch();

        /**
         * tuple = [Member(id=1, username=member1, age=10), Team(id=1, name=teamA)]
         * tuple = [Member(id=2, username=member2, age=20), Team(id=1, name=teamA)]
         */
        assertThat(resultWithOn).isEqualTo(resultWithWhere);

        for (Tuple tuple : resultWithOn) {
            System.out.println("[resultWithOn] tuple = " + tuple);
        }

        for (Tuple tuple : resultWithWhere) {
            System.out.println("[resultWithWhere] tuple = " + tuple);
        }
    }

    /**
     * 세타 조인
     * - 연관관계 없는 필드로 조인
     * - from 절에 여러 엔티티를 선택해서 세타 조인
     * - inner 조인만 가능했고, outer 조인 불가능했었다. -> 그러나, 조인 on을 사용하면, 외부 조인 가능
     */
    @Test
    public void thetaJoin() {
        // 억지성 예제임
        // 회원의 이름이 팀 이름과 같은 회워 조회
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));


        List<Member> result = queryFactory
            .select(member)
            .from(member, team)
            .where(member.username.eq(team.name))
            .fetch();

        assertThat(result)
            .extracting("username")
            .containsExactly("teamA", "teamB");
    }


    /**
     * 회원과 팀을 조인하는데, 회원은 모두 조회되고(Member m LEFT JOIN m.team t), 팀은 팀 이름이 teamA인 팀만 조회(on t.name = 'teamA')되도록 하기
     * <날라간 JPQL> : SELECT m, t FROM Member m LEFT JOIN m.team t on t.name = 'teamA'
     * select
     *         member1,
     *         team
     *     from
     *         Member member1
     *     left join
     *         member1.team as team with team.name = ?1
     *
     * <날라간 SQL> : SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.TEAM_ID=t.id and t.name='teamA'
     * select
     *             m1_0.member_id,
     *             m1_0.age,
     *             m1_0.team_id,
     *             m1_0.username,
     *             t1_0.team_id,
     *             t1_0.name
     *         from
     *             member m1_0
     *         left join
     *             team t1_0
     *                 on t1_0.team_id=m1_0.team_id
     *                 and t1_0.name=?
     *
     */
    @Test
    public void JoinOn_leftOuterJoinYesRelation() {
        // 억지성 예제임
        // 회원의 이름이 팀 이름과 같은 회워 조회
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Tuple> result = queryFactory
            .select(member, team)
            .from(member)
            .leftJoin(member.team, team) // 연관관계 있는 필드끼리 leftJoin
            .on(team.name.eq("teamA"))
            .fetch();

        /**
         * tuple = [Member(id=1, username=member1, age=10), Team(id=1, name=teamA)]
         * tuple = [Member(id=2, username=member2, age=20), Team(id=1, name=teamA)]
         * tuple = [Member(id=3, username=member3, age=30), null]
         * tuple = [Member(id=4, username=member4, age=40), null]
         */
        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    /**
     * 세타 조인
     * - 연관관계 없는 필드로 조인
     * - 조인 on을 사용하면, 외부 조인 가능
     */
    @Test
    public void JoinOn_leftOuterJoinWithNoRelation() {
        List<Tuple> result = queryFactory
            .select(member, team)
            .from(member)
            .leftJoin(team) // 연관관계 없는 필드끼리 leftJoin
            .on(member.username.eq(team.name))
            .fetch();
        /**
         * tuple = [Member(id=1, username=member1, age=10), null]
         * tuple = [Member(id=2, username=member2, age=20), null]
         * tuple = [Member(id=3, username=member3, age=30), null]
         * tuple = [Member(id=4, username=member4, age=40), null]
         * tuple = [Member(id=5, username=teamA, age=0), Team(id=1, name=teamA)]
         * tuple = [Member(id=6, username=teamB, age=0), Team(id=2, name=teamB)]
         * tuple = [Member(id=7, username=teamC, age=0), null]
         */
        for (Tuple tuple : result) {
            System.out.println("t=" + tuple);
        };
    }

    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    public void fetchJoinNo() {
        em.flush();
        em.clear();

        Member findMember = queryFactory
            .selectFrom(member)
            .where(member.username.eq("member1"))
            .fetchOne();

        boolean isLoaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());

        assertThat(isLoaded).as("fetch join 미적용").isFalse();
    }

    @Test
    public void fetchJoinYes() {
        em.flush();
        em.clear();

        Member findMember = queryFactory
            .selectFrom(member)
            .join(member.team, team).fetchJoin() // 페치 조인!
            .where(member.username.eq("member1"))
            .fetchOne();

        boolean isLoaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());

        assertThat(isLoaded).as("fetch join 적용").isTrue();
    }

}
