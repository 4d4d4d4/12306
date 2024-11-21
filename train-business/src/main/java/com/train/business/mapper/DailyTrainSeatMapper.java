package com.train.business.mapper;

import com.train.common.base.entity.domain.DailyTrainSeat;
import com.train.common.base.entity.query.DailyTrainSeatExample;

import java.sql.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DailyTrainSeatMapper {
    long countByExample(DailyTrainSeatExample example);

    int deleteByExample(DailyTrainSeatExample example);

    int deleteByPrimaryKey(Long id);

    int insert(DailyTrainSeat record);

    int insertSelective(DailyTrainSeat record);

    List<DailyTrainSeat> selectByExample(DailyTrainSeatExample example);

    DailyTrainSeat selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") DailyTrainSeat record, @Param("example") DailyTrainSeatExample example);

    int updateByExample(@Param("record") DailyTrainSeat record, @Param("example") DailyTrainSeatExample example);

    int updateByPrimaryKeySelective(DailyTrainSeat record);

    int updateByPrimaryKey(DailyTrainSeat record);

    List<DailyTrainSeat> selectAllDSeatWithGroupAndOrder(@Param("trainCode") String trainCode, @Param("date")Date date);
}