package com.train.batch.mapper;

import com.train.common.base.entity.domain.Locks;
import com.train.common.base.entity.query.LocksExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface LocksMapper {
    long countByExample(LocksExample example);

    int deleteByExample(LocksExample example);

    int deleteByPrimaryKey(@Param("schedName") String schedName, @Param("lockName") String lockName);

    int insert(Locks record);

    int insertSelective(Locks record);

    List<Locks> selectByExample(LocksExample example);

    int updateByExampleSelective(@Param("record") Locks record, @Param("example") LocksExample example);

    int updateByExample(@Param("record") Locks record, @Param("example") LocksExample example);
}