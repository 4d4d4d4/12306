package com.train.batch.mapper;

import com.train.common.base.entity.domain.FiredTriggers;
import com.train.common.base.entity.query.FiredTriggersExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FiredTriggersMapper {
    long countByExample(FiredTriggersExample example);

    int deleteByExample(FiredTriggersExample example);

    int deleteByPrimaryKey(@Param("schedName") String schedName, @Param("entryId") String entryId);

    int insert(FiredTriggers record);

    int insertSelective(FiredTriggers record);

    List<FiredTriggers> selectByExample(FiredTriggersExample example);

    FiredTriggers selectByPrimaryKey(@Param("schedName") String schedName, @Param("entryId") String entryId);

    int updateByExampleSelective(@Param("record") FiredTriggers record, @Param("example") FiredTriggersExample example);

    int updateByExample(@Param("record") FiredTriggers record, @Param("example") FiredTriggersExample example);

    int updateByPrimaryKeySelective(FiredTriggers record);

    int updateByPrimaryKey(FiredTriggers record);
}