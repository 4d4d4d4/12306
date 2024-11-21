package com.train.batch.mapper;

import com.train.common.base.entity.domain.JobDetails;
import com.train.common.base.entity.query.JobDetailsExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface JobDetailsMapper {
    long countByExample(JobDetailsExample example);

    int deleteByExample(JobDetailsExample example);

    int deleteByPrimaryKey(@Param("schedName") String schedName, @Param("jobName") String jobName, @Param("jobGroup") String jobGroup);

    int insert(JobDetails record);

    int insertSelective(JobDetails record);

    List<JobDetails> selectByExampleWithBLOBs(JobDetailsExample example);

    List<JobDetails> selectByExample(JobDetailsExample example);

    JobDetails selectByPrimaryKey(@Param("schedName") String schedName, @Param("jobName") String jobName, @Param("jobGroup") String jobGroup);

    int updateByExampleSelective(@Param("record") JobDetails record, @Param("example") JobDetailsExample example);

    int updateByExampleWithBLOBs(@Param("record") JobDetails record, @Param("example") JobDetailsExample example);

    int updateByExample(@Param("record") JobDetails record, @Param("example") JobDetailsExample example);

    int updateByPrimaryKeySelective(JobDetails record);

    int updateByPrimaryKeyWithBLOBs(JobDetails record);

    int updateByPrimaryKey(JobDetails record);
}