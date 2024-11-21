package com.train.batch.mapper;

import com.train.common.base.entity.domain.SchedulerState;
import com.train.common.base.entity.query.SchedulerStateExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SchedulerStateMapper {
    long countByExample(SchedulerStateExample example);

    int deleteByExample(SchedulerStateExample example);

    int deleteByPrimaryKey(@Param("schedName") String schedName, @Param("instanceName") String instanceName);

    int insert(SchedulerState record);

    int insertSelective(SchedulerState record);

    List<SchedulerState> selectByExample(SchedulerStateExample example);

    SchedulerState selectByPrimaryKey(@Param("schedName") String schedName, @Param("instanceName") String instanceName);

    int updateByExampleSelective(@Param("record") SchedulerState record, @Param("example") SchedulerStateExample example);

    int updateByExample(@Param("record") SchedulerState record, @Param("example") SchedulerStateExample example);

    int updateByPrimaryKeySelective(SchedulerState record);

    int updateByPrimaryKey(SchedulerState record);
}