package com.train.batch.mapper;

import com.train.common.base.entity.domain.BlobTriggers;
import com.train.common.base.entity.query.BlobTriggersExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface BlobTriggersMapper {
    long countByExample(BlobTriggersExample example);

    int deleteByExample(BlobTriggersExample example);

    int deleteByPrimaryKey(@Param("schedName") String schedName, @Param("triggerName") String triggerName, @Param("triggerGroup") String triggerGroup);

    int insert(BlobTriggers record);

    int insertSelective(BlobTriggers record);

    List<BlobTriggers> selectByExampleWithBLOBs(BlobTriggersExample example);

    List<BlobTriggers> selectByExample(BlobTriggersExample example);

    BlobTriggers selectByPrimaryKey(@Param("schedName") String schedName, @Param("triggerName") String triggerName, @Param("triggerGroup") String triggerGroup);

    int updateByExampleSelective(@Param("record") BlobTriggers record, @Param("example") BlobTriggersExample example);

    int updateByExampleWithBLOBs(@Param("record") BlobTriggers record, @Param("example") BlobTriggersExample example);

    int updateByExample(@Param("record") BlobTriggers record, @Param("example") BlobTriggersExample example);

    int updateByPrimaryKeySelective(BlobTriggers record);

    int updateByPrimaryKeyWithBLOBs(BlobTriggers record);
}