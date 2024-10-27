package com.train.business.controller.admin;

import com.train.common.base.entity.domain.Station;
import com.train.common.base.entity.domain.Train;
import com.train.common.base.entity.query.TrainQuery;
import com.train.common.base.entity.vo.PaginationResultVo;
import com.train.common.base.entity.vo.StationVo;
import com.train.common.base.entity.vo.TrainVo;
import com.train.common.base.service.TrainService;
import com.train.common.resp.Result;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

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
}
