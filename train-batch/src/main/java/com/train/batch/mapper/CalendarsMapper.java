package com.train.batch.mapper;

import com.train.common.base.entity.domain.Calendars;
import com.train.common.base.entity.query.CalendarsExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CalendarsMapper {
    long countByExample(CalendarsExample example);

    int deleteByExample(CalendarsExample example);

    int deleteByPrimaryKey(@Param("schedName") String schedName, @Param("calendarName") String calendarName);

    int insert(Calendars record);

    int insertSelective(Calendars record);

    List<Calendars> selectByExampleWithBLOBs(CalendarsExample example);

    List<Calendars> selectByExample(CalendarsExample example);

    Calendars selectByPrimaryKey(@Param("schedName") String schedName, @Param("calendarName") String calendarName);

    int updateByExampleSelective(@Param("record") Calendars record, @Param("example") CalendarsExample example);

    int updateByExampleWithBLOBs(@Param("record") Calendars record, @Param("example") CalendarsExample example);

    int updateByExample(@Param("record") Calendars record, @Param("example") CalendarsExample example);

    int updateByPrimaryKeySelective(Calendars record);

    int updateByPrimaryKeyWithBLOBs(Calendars record);
}