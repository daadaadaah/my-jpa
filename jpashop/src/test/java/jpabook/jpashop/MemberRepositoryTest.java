package jpabook.jpashop;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;

    @Test
    @Transactional // 테스트 코드에서 작성 되면, 테스트가 끝난 다음에 롤백을 한다.
//    @Rollback(value = false) // 롤백 안하고 싶으면, 이 어노테이션을 추가하면 된다.
    @Commit
    public void testMember() throws Exception {
        // given
        Member member = new Member();
        member.setUsername("memberA");

        // when
        Long savedId = memberRepository.save(member);
        Member findMember = memberRepository.find(savedId);

        // then
        Assertions.assertEquals(findMember.getId(), member.getId());
        Assertions.assertEquals(findMember.getUsername(), member.getUsername());

        // 테스트 성공하는 이유 : 같은 트랜잭션 안에서 저장하고 조회 하면, 영속성 컨텍스트 같아서, id가 같으면, 같은 entity로 본다.
        // 따라서, insert 쿼리만 날라라고, select 쿼리는 안 날라 간다. 왜냐하면, 1차 캐시에 있기 때문에,
        Assertions.assertEquals(findMember, member);
    }
}