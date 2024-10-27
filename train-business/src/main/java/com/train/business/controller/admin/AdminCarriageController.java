package com.train.business.controller.admin;

import com.train.common.base.entity.domain.TrainCarriage;
import com.train.common.base.entity.query.CarriageQuery;
import com.train.common.base.entity.vo.CarriageIndexVo;
import com.train.common.base.entity.vo.CarriageVo;
import com.train.common.base.entity.vo.PaginationResultVo;
import com.train.common.base.entity.vo.TrainCodeVo;
import com.train.common.base.service.CarriageService;
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
    private TrainService trainService;

    @PostMapping("/addCarriage")
    public Result batchAdd(@RequestBody List<CarriageVo> carriageVoList){
        String msg =  carriageService.batchAdd(carriageVoList);
        return Result.ok().message(msg);
    }

    @PostMapping("/deleteCarriage")
    public Result batchDelete(@RequestBody List<Long> ids){
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
     * @param trainCode 火车编码
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
     * @param trainCode
     * @return
     */
    @PostMapping("/getTrainByCode")
    public Result getTrainByCode( @RequestBody Map<String, String> reqMap){
        String trainCode = reqMap.get("trainCode");

       List<TrainCodeVo> result =  carriageService.getAllTrainByCode(trainCode);
        return Result.ok().data("result", result);
    }
    @PostMapping("/creatSeat")
    public Result createSeatByCarriage(@RequestBody CarriageVo carriageVo){
        carriageService.createCarriage(carriageVo);
        return Result.ok();
    }
}