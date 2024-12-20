package com.train.business.controller.admin;

import cn.hutool.core.util.StrUtil;
import com.train.common.aspect.annotation.CacheEvictByPrefix;
import com.train.common.base.entity.domain.DailyTrainSeat;
import com.train.common.base.entity.domain.TrainCarriage;
import com.train.common.base.entity.domain.TrainSeat;
import com.train.common.base.entity.query.DailySeatQuery;
import com.train.common.base.entity.query.SeatQuery;
import com.train.common.base.entity.vo.*;
import com.train.common.base.service.CarriageService;
import com.train.common.base.service.DailySeatService;
import com.train.common.base.service.SeatService;
import com.train.common.entity.SystemConstants;
import com.train.common.resp.Result;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 车座管理控制器</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/10/18 下午8:51</li>
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
@RequestMapping("admin/seat")
public class AdminSeatController {
    @DubboReference(version = "1.0.0", check = false)
    private SeatService seatService;

    @DubboReference(version = "1.0.0", check = false)
    private CarriageService carriageService;

    @DubboReference(version = "1.0.0", check = false)
    private DailySeatService dSeatService;

    @PostMapping("/addSeat")
    @CacheEvictByPrefix(prefix = SystemConstants.CACHE_STATION_PREFIX)
    public Result batchAddSeat(@RequestBody SeatVo seatVo){
        Integer carriageIndex = seatVo.getCarriageIndex();
        String trainCode = seatVo.getTrainCode();
        if(carriageIndex == null || StrUtil.isBlank(trainCode)){
            return Result.error().message("火车码或者车厢序号不能为空");
        }
        TrainCarriage trainCarriage = carriageService.selectByTrainCodeAndCarriageIndex(trainCode, carriageIndex);

        return seatService.addSeat(trainCarriage, seatVo);
    }
    @GetMapping("/delSeat")
    @CacheEvictByPrefix(prefix = SystemConstants.CACHE_STATION_PREFIX)
    public Result delSeat(@RequestParam("ids") List<Long> ids){
        seatService.delSeats(ids);
        return Result.ok();
    }
    @PostMapping("/updateSeat")
    @CacheEvictByPrefix(prefix = SystemConstants.CACHE_STATION_PREFIX)
    public  Result editSeat(@RequestBody TrainSeat trainSeat){
        Integer carriageIndex = trainSeat.getCarriageIndex();
        String trainCode = trainSeat.getTrainCode();
        if(carriageIndex == null || StrUtil.isBlank(trainCode)){
            return Result.error().message("火车码或者车厢序号不能为空");
        }
        TrainCarriage trainCarriage = carriageService.selectByTrainCodeAndCarriageIndex(trainCode, carriageIndex);
        return seatService.editSeat(trainCarriage, trainSeat);
    }

    @PostMapping("/listByCondition")
    public Result listByCondition(@RequestBody SeatQuery seatQuery){
        PaginationResultVo<TrainSeat> resultVo = seatService.selectPageByCondition(seatQuery);
        return Result.ok().data("result", resultVo);
    }

    @PostMapping("/getRowIndexs")
    public Result getRowIndex(@RequestBody SeatVo seatVo){
        String col = seatVo.getCol();
        String trainCode = seatVo.getTrainCode();
        String carriageIndex = String.valueOf(seatVo.getCarriageIndex());
        List<SeatColVo> res = seatService.getRowIndex(trainCode, carriageIndex, col);
        return Result.ok().data("result", res);
    }

    // 每日车座数据
    @PostMapping("/day/listByCondition")
    public Result queryAllDSeat(@RequestBody DailySeatQuery q){
        PaginationResultVo<DailyTrainSeat> result = dSeatService.queryAllDSeat(q);
        return Result.ok().data("result", result);
    }
    @GetMapping("/day/batchDSeat")
    public Result batchDSeat(@RequestParam("ids") List<Long> ids){
        return dSeatService.batchDelSeat(ids);
    }
    @PostMapping("/day/addDSeat")
    public Result addDSeat(@RequestBody DailySeatVo seatVo){
        return dSeatService.insertDSeat(seatVo);
    }
    @PostMapping("/day/updateDSeat")
    public Result updateDSeat(@RequestBody DailyTrainSeat seat)
    {
        return dSeatService.updateDSeat(seat);
    }

    /**
     * 创建某日车座
     * @param vo
     * @return
     */
    @PostMapping("/day/createDSeat")
    public Result createDSeat(@RequestBody DailySeatVo vo){
        return  dSeatService.createDSeat(vo);
    }
}
