package com.train.business.controller.admin;

import com.train.common.base.entity.domain.DailyTrain;
import com.train.common.base.entity.domain.Station;
import com.train.common.base.entity.domain.Train;
import com.train.common.base.entity.query.DailyTrainQuery;
import com.train.common.base.entity.query.TrainQuery;
import com.train.common.base.entity.vo.DailyTrainVo;
import com.train.common.base.entity.vo.PaginationResultVo;
import com.train.common.base.entity.vo.StationVo;
import com.train.common.base.entity.vo.TrainVo;
import com.train.common.base.service.DailyTrainService;
import com.train.common.base.service.TrainService;
import com.train.common.resp.Result;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 火车控制器</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/8/29 下午2:30</li>
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
@RequestMapping("admin/train")
public class AdminTrainController {
    @DubboReference(version = "1.0.0", check = false)
    private TrainService trainService;
    @DubboReference(version = "1.0.0", check = false)
    private DailyTrainService dailyTrainService;

    @RequestMapping("listByCondition")
    public Result listByCondition(@RequestBody TrainQuery trainQuery){
        PaginationResultVo<Train> paginationResultVo = trainService.getAllTrain(trainQuery);
        return Result.ok().data("result", paginationResultVo);
    }
    @PostMapping("/addTrain")
    public Result addTrain(@RequestBody List<TrainVo> list){
        trainService.addTrainList(list);
        return Result.ok();
    }
    @PostMapping("/editTrain")
    public Result editTrain(@RequestBody List<Train> list){
        trainService.updateTrains(list);
        return Result.ok();
    }

    @GetMapping("/delTrain")
    public Result deleteTrains(@RequestParam("ids") List<String> ids){
        trainService.deleteTrainByIds(ids);
        return Result.ok();
    }


     // 每日火车数据

    /**
     * 每日火车数据分页条件查询接口
     * @param trainQuery 查询条件（根据时间精准查询、根据火车编码模糊查询）
     * @return 返回数据列表
     */
    @PostMapping("/day/listByCondition")
    public Result dailyTrainDataByPC(@RequestBody DailyTrainQuery trainQuery){
        PaginationResultVo<DailyTrain> result = dailyTrainService.getAllDTrainByConditionWithPage(trainQuery);
        return Result.ok().data("result", result);
    }

    /**
     * 根据id批量删除
     */
    @GetMapping("/day/batchDelTrain")
    public Result removeDailyTrain(@RequestParam("ids") List<Long> ids){
       return dailyTrainService.removeDailyTrain(ids);
    }
    /**
     * 单独增加每日火车数据
     */
    @PostMapping("/day/addDTrain")
    public Result addDailyTrain(@RequestBody DailyTrainVo dailyTrainVo){
        return dailyTrainService.addDailyTrain(dailyTrainVo);
    }
    @PostMapping("day/updateDTrain")
    public Result updateDTrain(@RequestBody DailyTrain dailyTrain){
        return dailyTrainService.updateDTrain(dailyTrain);
    }
    @PostMapping("/day/batchInsertDTrain")
    public Result batchInsertDTrain(@RequestBody DailyTrainQuery query){
        return  dailyTrainService.batchInsertDTrain(query);
    }
}
