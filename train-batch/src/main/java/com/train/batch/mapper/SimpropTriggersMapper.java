package com.train.batch.mapper;

import com.train.common.base.entity.domain.SimpropTriggers;
import com.train.common.base.entity.query.SimpropTriggersExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SimpropTriggersMapper {
    long countByExample(SimpropTriggersExample example);

    int deleteByExample(SimpropTriggersExample example);

    int deleteByPrimaryKey(@Param("schedName") String schedName, @Param("triggerName") String triggerName, @Param("triggerGroup") String triggerGroup);

    int insert(SimpropTriggers record);

    int insertSelective(SimpropTriggers record);

    List<SimpropTriggers> selectByExample(SimpropTriggersExample example);

    SimpropTriggers selectByPrimaryKey(@Param("schedName") String schedName, @Param("triggerName") String triggerName, @Param("triggerGroup") String triggerGroup);

    int updateByExampleSelective(@Param("record") SimpropTriggers record, @Param("example") SimpropTriggersExample example);

    int updateByExample(@Param("record") SimpropTriggers record, @Param("example") SimpropTriggersExample example);

    int updateByPrimaryKeySelective(SimpropTriggers record);

    int updateByPrimaryKey(SimpropTriggers record);
}