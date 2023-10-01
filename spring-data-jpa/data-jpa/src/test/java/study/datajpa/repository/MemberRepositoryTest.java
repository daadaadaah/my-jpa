package study.datajpa.repository;

import jakarta.persistence.EntityManager;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.dto.MemberSearchCondition;
import study.datajpa.dto.MemberTeamDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

@SpringBootTest
@Transactional
@Rollback(value = false)
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    EntityManager em;

    @Test
    public void testMember() {
        Member member = new Member("memberA");

        Member savedMember = memberRepository.save(member);

        Member findMember = memberRepository.findById(savedMember.getId()).get();

        Assertions.assertThat(findMember.getId()).isEqualTo(member.getId());
        Assertions.assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        Assertions.assertThat(findMember).isEqualTo(member);
    }

    @Test
    public void findByUsernameAndAgeGreaterThan() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);

        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("AAA", 15);

        Assertions.assertThat(result.get(0).getUsername()).isEqualTo("AAA");
        Assertions.assertThat(result.get(0).getAge()).isEqualTo(20);
        Assertions.assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void findUser() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);

        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findUser("AAA", 10);

        Assertions.assertThat(result.get(0)).isEqualTo(m1);
    }

    @Test
    public void findUsernameList() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);

        memberRepository.save(m1);
        memberRepository.save(m2);

        List<String> result = memberRepository.findUsernameList();

        Assertions.assertThat(result.get(0)).isEqualTo("AAA");
        Assertions.assertThat(result.get(1)).isEqualTo("BBB");
    }

    @Test
    public void findMemberDto() {
        Team team = new Team("teamA");
        teamRepository.save(team);

        Member m1 = new Member("AAA", 10);
        m1.setTeam(team);
        memberRepository.save(m1);

        List<MemberDto> result = memberRepository.findMemberDto();

        Assertions.assertThat(result.get(0).getTeamName()).isEqualTo("teamA");
    }

    @Test
    public void bulkAgePlus() {
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 19));
        memberRepository.save(new Member("member3", 20));
        memberRepository.save(new Member("member4", 21));
        memberRepository.save(new Member("member5", 40));

        int result = memberRepository.bulkAgePlus(20);

        Assertions.assertThat(result).isEqualTo(3);
    }

    @Test
    public void bulkAgePlus2() {
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 19));
        memberRepository.save(new Member("member3", 20));
        memberRepository.save(new Member("member4", 21));
        memberRepository.save(new Member("member5", 40));

        int resultCount = memberRepository.bulkAgePlus(20);

        Assertions.assertThat(resultCount).isEqualTo(3);

        List<Member> result = memberRepository.findByUsername("member5");

        Assertions.assertThat(result.get(0).getAge()).isEqualTo(41); // 테스트 성공
    }

    /**
     * LazyLoading시, N+1 문제 발생
     *
     * [EntityGraph 적용 전]
     * <날라가는 쿼리>
     * select m1_0.member_id,m1_0.age,m1_0.team_id,m1_0.username from member m1_0
     *
     * select t1_0.team_id,t1_0.name from team t1_0 where t1_0.team_id=?
     * select t1_0.team_id,t1_0.name from team t1_0 where t1_0.team_id=?
     *
     * [EntityGraph 적용 후]
     * <날라가는 쿼리>
     * select m1_0.member_id,m1_0.age,t1_0.team_id,t1_0.name,m1_0.username from member m1_0 left join team t1_0 on t1_0.team_id=m1_0.team_id
     */
    @Test
    public void findMemberLazyLoading() {
        // given
        // member1 -> teamA
        // member2 -> teamB

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        teamRepository.save(teamA);
        teamRepository.save(teamB);

        Member member1 = new Member("member1", 40, teamA);
        Member member2 = new Member("member2", 40, teamB);

        memberRepository.save(member1);
        memberRepository.save(member2);

        em.flush();
        em.clear();

        List<Member> members = memberRepository.findAll();

        for (Member member : members) {
            System.out.println("member = " + member.getUsername());
            System.out.println("member.team = " + member.getTeam().getName());
        }
    }

    /**
     * LazyLoading시, N+1 문제 발생 : FetchJoin으로 해결
     *
     * <날라가는 쿼리>
     * select m1_0.member_id,m1_0.age,t1_0.team_id,t1_0.name,m1_0.username from member m1_0 left join team t1_0 on t1_0.team_id=m1_0.team_id     *
     */
    @Test
    public void findMemberFetchJoin() {
        // given
        // member1 -> teamA
        // member2 -> teamB

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        teamRepository.save(teamA);
        teamRepository.save(teamB);

        Member member1 = new Member("member1", 40, teamA);
        Member member2 = new Member("member2", 40, teamB);

        memberRepository.save(member1);
        memberRepository.save(member2);

        em.flush();
        em.clear();

        List<Member> members = memberRepository.findMemberFetchJoin();

        for (Member member : members) {
            System.out.println("member = " + member.getUsername());
            System.out.println("member.team = " + member.getTeam().getName());
        }
    }

    @Test
    public void findEntityGraphByUsername() {
        // given
        // member1 -> teamA
        // member2 -> teamB

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        teamRepository.save(teamA);
        teamRepository.save(teamB);

        Member member1 = new Member("member1", 40, teamA);
        Member member2 = new Member("member1", 40, teamB);

        memberRepository.save(member1);
        memberRepository.save(member2);

        em.flush();
        em.clear();

        List<Member> members = memberRepository.findEntityGraphByUsername("member1");

        for (Member member : members) {
            System.out.println("member = " + member.getUsername());
            System.out.println("member.team = " + member.getTeam().getName());
        }
    }

    @Test
    public void searchWithQueryDSL() {
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

        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(35);
        condition.setAgeLoe(40);
        condition.setTeamName("teamB");

        List<MemberTeamDto> result = memberRepository.search(condition);

        Assertions.assertThat(result).extracting("username").containsExactly("member4");
    }

    @Test
    public void nativeQuery() {
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);

        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();

        Member result = memberRepository.findByNativeQuery("m1");

        Assertions.assertThat(result.getUsername()).isEqualTo("m1");
    }
}
