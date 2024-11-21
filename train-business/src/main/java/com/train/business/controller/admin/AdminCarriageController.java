package com.train.business.controller.admin;

import cn.hutool.core.util.StrUtil;
import com.train.common.base.entity.domain.DailyTrainCarriage;
import com.train.common.base.entity.domain.TrainCarriage;
import com.train.common.base.entity.query.CarriageQuery;
import com.train.common.base.entity.query.DailyCarriageQuery;
import com.train.common.base.entity.vo.*;
import com.train.common.base.service.CarriageService;
import com.train.common.base.service.DailyCarriageService;
import com.train.common.base.service.TrainService;
import com.train.common.resp.Result;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 车厢管理类</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/10/17 下午3:52</li>
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
@RequestMapping("admin/carriage")
public class AdminCarriageController {
    @DubboReference(version = "1.0.0", check = false)
    private CarriageService carriageService;

    @DubboReference(version = "1.0.0", check = false)
    private DailyCarriageService dailyCarriageService;

    @PostMapping("/addCarriage")
    public Result batchAdd(@RequestBody List<CarriageVo> carriageVoList){
        String msg =  carriageService.batchAdd(carriageVoList);
        return Result.ok().message(msg);
    }

    @GetMapping("/deleteCarriage")
    public Result batchDelete(@RequestParam("ids") List<Long> ids){
        carriageService.batchDelete(ids);
        return Result.ok();
    }

    @PostMapping("/updateCarriage")
    public Result updateCarriage(@RequestBody TrainCarriage trainCarriage){
        return carriageService.updateCarriage(trainCarriage);
    }

    @PostMapping("/listByCondition")
    public Result selectCarriagesByConditionWithPage(@RequestBody CarriageQuery query){
        PaginationResultVo<TrainCarriage> result = carriageService.selectAllByConditionWithPage(query);
        return Result.ok().data("result", result);
    }

    /**
     * 根据火车code查询火车车厢情况
     * @param reqMap 火车编码
     * @return
     */
    @PostMapping("/getCarriagesIndex")
    public Result getCarriagesIndex( @RequestBody Map<String, String> reqMap){
        String trainCode = reqMap.get("trainCode");
        List<CarriageIndexVo> result = carriageService.getAllCarriagesIndex(trainCode);
        return Result.ok().data("result", result);
    }

    /**
     * 根据火车编码模糊查询火车
     * @param reqMap
     * @return
     */
    @PostMapping("/getTrainByCode")
    public Result getTrainByCode( @RequestBody Map<String, String> reqMap){
        String trainCode = reqMap.get("trainCode");
        if (StrUtil.isBlank(trainCode) || trainCode.equals("null")){
            trainCode = "";
        }
       List<TrainVo> result =  carriageService.getAllTrainByCode(trainCode);
        return Result.ok().data("result", result);
    }
    @PostMapping("/creatSeat")
    public Result createSeatByCarriage(@RequestBody CarriageVo carriageVo){
        carriageService.createCarriage(carriageVo);
        return Result.ok();
    }
    @PostMapping("/day/createSeat")
    public Result createDSeatByDCarriage(@RequestBody DailyCarriageVo carriageVo){
        dailyCarriageService.createCarriage(carriageVo);
        return Result.ok();
    }

    /**
     * 每日数据接口
     */
    @PostMapping("/day/listByCondition")
    public Result selectDCarriagesByConditionWithPage(@RequestBody DailyCarriageQuery query){
        PaginationResultVo<DailyTrainCarriage> result = dailyCarriageService.selectAllByConditionWithPage(query);
        return Result.ok().data("result", result);
    }
    @GetMapping("/day/batchDelCarriage")
    public Result batchDelDCarriage(@RequestParam("ids") List<Long> ids){
        return dailyCarriageService.batchDelCarriage(ids);
    }
    @PostMapping("/day/addDCarriage")
    public Result addDCarriage(@RequestBody DailyCarriageVo carriageVo){
        return dailyCarriageService.addDCarriage(carriageVo);
    }
    @PostMapping("/day/updateDCarriage")
    public Result updateDCarriage(@RequestBody DailyTrainCarriage carriage){
        return dailyCarriageService.updateDCarriage(carriage);
    }
    /**
     * 生成某日的每日车厢表
     */
    @PostMapping("/day/createDCarriage")
    public Result createDCarriage(@RequestBody DailyCarriageVo vo){
        return dailyCarriageService.creatDayCarriage(vo);
    }


}
