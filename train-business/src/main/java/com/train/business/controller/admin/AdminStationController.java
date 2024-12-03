package com.train.business.controller.admin;

import com.train.common.aspect.annotation.CacheEvictByPrefix;
import com.train.common.base.entity.domain.DailyTrainStation;
import com.train.common.base.entity.domain.Station;
import com.train.common.base.entity.query.DailyStationQuery;
import com.train.common.base.entity.vo.DailyStationVo;
import com.train.common.base.entity.vo.StationVo;
import com.train.common.base.service.DailyStationService;
import com.train.common.base.service.StationService;
import com.train.common.base.entity.query.StationQuery;
import com.train.common.base.entity.vo.PaginationResultVo;
import com.train.common.entity.SystemConstants;
import com.train.common.resp.Result;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 火车车站控制器</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/8/29 下午2:31</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024，. All rights reserved.
 * @Author cqy.
 */
@RestController
@RequestMapping("admin/station")
public class AdminStationController {
    @DubboReference(version = "1.0.0", check = false)
    private StationService stationService;
    @DubboReference(version = "1.0.0", check = false)
    private DailyStationService dailyStationService;

    @PostMapping("/listByCondition")

    public Result getStationNameList(@RequestBody StationQuery station){
        PaginationResultVo<Station> allStation = stationService.getAllStation(station);

        return Result.ok().data("result", allStation);
    }

    @PostMapping("/addStation")
    @CacheEvictByPrefix(prefix = SystemConstants.CACHE_STATION_PREFIX)
    public Result addStation(@RequestBody List<StationVo> list){
        stationService.addStationList(list);
        return Result.ok();
    }
    @PostMapping("/editStation")
    @CacheEvictByPrefix(prefix = SystemConstants.CACHE_STATION_PREFIX)
    public Result editStation(@RequestBody List<Station> list){
        stationService.editStationList(list);
        return Result.ok();
    }

    @GetMapping("/deleteStation")
    @CacheEvictByPrefix(prefix = SystemConstants.CACHE_STATION_PREFIX)
    public Result deleteStation(@RequestParam("ids") List<String> ids){
        stationService.deleteStationByIds(ids);
        return Result.ok();
    }

    // 每日车站数据
    @PostMapping("/day/listByCondition")
    public Result queryAllDayStation(@RequestBody DailyStationQuery stationQuery){
        PaginationResultVo<DailyTrainStation> allStation = dailyStationService.getAllDStation(stationQuery);
        return Result.ok().data("result", allStation);
    }
    @PostMapping("/day/addDStation")
    public Result addDStation(@RequestBody DailyStationVo vo){
        return dailyStationService.addDStation(vo);
    }
    @PostMapping("/day/updateDStation")
    public Result updateDStation(@RequestBody DailyTrainStation station){
        return dailyStationService.updateDStation(station);
    }
    @GetMapping("/day/batchDelDStation")
    public Result batchDelDStation(@RequestParam("ids") List<Long> ids){
        return dailyStationService.batchDelStation(ids);
    }
}
