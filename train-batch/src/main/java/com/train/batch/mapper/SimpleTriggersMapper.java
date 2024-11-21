package com.train.batch.mapper;

import com.train.common.base.entity.domain.SimpleTriggers;
import com.train.common.base.entity.query.SimpleTriggersExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SimpleTriggersMapper {
    long countByExample(SimpleTriggersExample example);

    int deleteByExample(SimpleTriggersExample example);

    int deleteByPrimaryKey(@Param("schedName") String schedName, @Param("triggerName") String triggerName, @Param("triggerGroup") String triggerGroup);

    int insert(SimpleTriggers record);

    int insertSelective(SimpleTriggers record);

    List<SimpleTriggers> selectByExample(SimpleTriggersExample example);

    SimpleTriggers selectByPrimaryKey(@Param("schedName") String schedName, @Param("triggerName") String triggerName, @Param("triggerGroup") String triggerGroup);

    int updateByExampleSelective(@Param("record") SimpleTriggers record, @Param("example") SimpleTriggersExample example);

    int updateByExample(@Param("record") SimpleTriggers record, @Param("example") SimpleTriggersExample example);

    int updateByPrimaryKeySelective(SimpleTriggers record);

    int updateByPrimaryKey(SimpleTriggers record);
}