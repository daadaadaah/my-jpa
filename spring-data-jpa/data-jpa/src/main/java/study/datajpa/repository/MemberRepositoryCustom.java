package study.datajpa.repository;

import java.util.List;
import study.datajpa.dto.MemberSearchCondition;
import study.datajpa.dto.MemberTeamDto;

public interface MemberRepositoryCustom {
    List<MemberTeamDto> search(MemberSearchCondition condition);
}
