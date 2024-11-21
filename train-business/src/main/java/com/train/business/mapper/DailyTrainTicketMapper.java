package com.train.business.mapper;

import com.train.common.base.entity.domain.DailyTrainTicket;
import com.train.common.base.entity.query.DailyTrainTicketExample;
import java.util.List;

import com.train.common.base.entity.query.TicketQuery;
import com.train.common.base.entity.query.UTrainTicketQuery;
import com.train.common.base.entity.resp.TrainTicketResp;
import org.apache.ibatis.annotations.Param;

public interface DailyTrainTicketMapper {
    long countByExample(DailyTrainTicketExample example);

    int deleteByExample(DailyTrainTicketExample example);

    int deleteByPrimaryKey(Long id);

    int insert(DailyTrainTicket record);

    int insertSelective(DailyTrainTicket record);

    List<DailyTrainTicket> selectByExample(DailyTrainTicketExample example);

    DailyTrainTicket selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") DailyTrainTicket record, @Param("example") DailyTrainTicketExample example);

    int updateByExample(@Param("record") DailyTrainTicket record, @Param("example") DailyTrainTicketExample example);

    int updateByPrimaryKeySelective(DailyTrainTicket record);

    int updateByPrimaryKey(DailyTrainTicket record);

    List<DailyTrainTicket> selectByConditionQuery(@Param("q") TicketQuery query);

    List<DailyTrainTicket> queryTicketByCondition(@Param("q") UTrainTicketQuery query);

}