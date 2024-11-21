package com.train.batch.mapper;

import com.train.common.base.entity.domain.Triggers;
import com.train.common.base.entity.query.TriggersExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TriggersMapper {
    long countByExample(TriggersExample example);

    int deleteByExample(TriggersExample example);

    int deleteByPrimaryKey(@Param("schedName") String schedName, @Param("triggerName") String triggerName, @Param("triggerGroup") String triggerGroup);

    int insert(Triggers record);

    int insertSelective(Triggers record);

    List<Triggers> selectByExampleWithBLOBs(TriggersExample example);

    List<Triggers> selectByExample(TriggersExample example);

    Triggers selectByPrimaryKey(@Param("schedName") String schedName, @Param("triggerName") String triggerName, @Param("triggerGroup") String triggerGroup);

    int updateByExampleSelective(@Param("record") Triggers record, @Param("example") TriggersExample example);

    int updateByExampleWithBLOBs(@Param("record") Triggers record, @Param("example") TriggersExample example);

    int updateByExample(@Param("record") Triggers record, @Param("example") TriggersExample example);

    int updateByPrimaryKeySelective(Triggers record);

    int updateByPrimaryKeyWithBLOBs(Triggers record);

    int updateByPrimaryKey(Triggers record);
}