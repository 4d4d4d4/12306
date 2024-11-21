package com.train.business.controller.admin;

import com.train.common.base.entity.domain.DailyTrainStation;
import com.train.common.base.entity.domain.TrainStation;
import com.train.common.base.entity.query.DailyTrainStationQuery;
import com.train.common.base.entity.query.StationIndexQuery;
import com.train.common.base.entity.query.TrainStationQuery;
import com.train.common.base.entity.vo.DailyTrainStationVo;
import com.train.common.base.entity.vo.PaginationResultVo;
import com.train.common.base.entity.vo.StationIndexVo;
import com.train.common.base.service.DailyTrainStationService;
import com.train.common.base.service.TrainStationService;
import com.train.common.resp.Result;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 火车车站管理器</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/11/1 下午10:49</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024，. All rights reserved.
 * @Author cqy.
 */
@RequestMapping("/admin/tStation")
@RestController
public class AdminTrainStationController {
    @DubboReference(version = "1.0.0", check = false)
    private TrainStationService trainStationService;
    @DubboReference(version = "1.0.0", check = false)
    private DailyTrainStationService dTrainStationService;

    @PostMapping("/day/listByCondition")
    public Result listByDCondition(@RequestBody DailyTrainStationQuery query){
        PaginationResultVo<DailyTrainStation> result = dTrainStationService.selectAllDTStation(query);
        return Result.ok().data("result", result);
    }
    @GetMapping("/day/batchDelDTStation")
    public Result batchDTStation(@RequestParam("ids") List<Long> ids){
        return dTrainStationService.batchDelDTStation(ids);
    }
    @PostMapping("/day/addDTStation")
    public Result addDStation(@RequestBody DailyTrainStationVo vo){
        return dTrainStationService.addDTStation(vo);
    }
    @PostMapping("/day/updateDTStation")
    public Result updateDStation(@RequestBody DailyTrainStation dailyTrainStation){
        return dTrainStationService.updateDTStation(dailyTrainStation);
    }
    @PostMapping("/day/getStationByCondition")
    public Result getStationByCondition(@RequestBody StationIndexQuery query){
        List<StationIndexVo> result =  dTrainStationService.getStationByCondition(query);
        return Result.ok().data("result", result);
    }

    @PostMapping("/getStationByCondition")
    public Result getStationByCondition(@RequestBody TrainStationQuery query){
        PaginationResultVo<TrainStation> resultVo = trainStationService.getStationByCondition(query);
        return Result.ok().data("result",resultVo);
    }
}
