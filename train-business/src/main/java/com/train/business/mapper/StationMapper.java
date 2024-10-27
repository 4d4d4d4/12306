package com.train.business.mapper;

import com.train.common.base.entity.domain.Station;
import com.train.common.base.entity.query.SimplePage;
import com.train.common.base.entity.query.StationExample;
import java.util.List;

import com.train.common.base.entity.query.StationQuery;
import com.train.common.base.entity.vo.StationVo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

public interface StationMapper {
    long countByExample(StationExample example);

    int deleteByExample(StationExample example);

    int deleteByPrimaryKey(Long id);

    int insert(Station record);

    int insertSelective(Station record);

    List<Station> selectByExample(StationExample example);

    Station selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") Station record, @Param("example") StationExample example);

    int updateByExample(@Param("record") Station record, @Param("example") StationExample example);

    int updateByPrimaryKeySelective(Station record);

    int updateByPrimaryKey(Station record);

    // 根据条件进行分页查询
    List<Station> selectByCondition(@Param("station") StationQuery station, @Param("page") SimplePage page);

    // 条件查询总数
    Integer selectAllStationCount(@Param("station")StationQuery stationQuery);

    void addStationList(@Param("list") List<StationVo> list);

    void editStationList(@Param("list") List<Station> list);

    void deleteStationByIds(@Param("ids") List<String> ids);
}