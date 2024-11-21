package com.train.batch.mapper;

import com.train.common.base.entity.domain.CronTriggers;
import com.train.common.base.entity.query.CronTriggersExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CronTriggersMapper {
    long countByExample(CronTriggersExample example);

    int deleteByExample(CronTriggersExample example);

    int deleteByPrimaryKey(@Param("schedName") String schedName, @Param("triggerName") String triggerName, @Param("triggerGroup") String triggerGroup);

    int insert(CronTriggers record);

    int insertSelective(CronTriggers record);

    List<CronTriggers> selectByExample(CronTriggersExample example);

    CronTriggers selectByPrimaryKey(@Param("schedName") String schedName, @Param("triggerName") String triggerName, @Param("triggerGroup") String triggerGroup);

    int updateByExampleSelective(@Param("record") CronTriggers record, @Param("example") CronTriggersExample example);

    int updateByExample(@Param("record") CronTriggers record, @Param("example") CronTriggersExample example);

    int updateByPrimaryKeySelective(CronTriggers record);

    int updateByPrimaryKey(CronTriggers record);
}