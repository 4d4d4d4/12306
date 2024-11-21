package com.train.batch.mapper;

import com.train.common.base.entity.domain.PausedTriggerGrps;
import com.train.common.base.entity.query.PausedTriggerGrpsExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PausedTriggerGrpsMapper {
    long countByExample(PausedTriggerGrpsExample example);

    int deleteByExample(PausedTriggerGrpsExample example);

    int deleteByPrimaryKey(@Param("schedName") String schedName, @Param("triggerGroup") String triggerGroup);

    int insert(PausedTriggerGrps record);

    int insertSelective(PausedTriggerGrps record);

    List<PausedTriggerGrps> selectByExample(PausedTriggerGrpsExample example);

    int updateByExampleSelective(@Param("record") PausedTriggerGrps record, @Param("example") PausedTriggerGrpsExample example);

    int updateByExample(@Param("record") PausedTriggerGrps record, @Param("example") PausedTriggerGrpsExample example);
}