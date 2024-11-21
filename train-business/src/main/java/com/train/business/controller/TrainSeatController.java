package com.train.business.controller;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.train.common.base.entity.domain.DailyTrainSeat;
import com.train.common.base.entity.resp.CarriageSeatResp;
import com.train.common.base.service.DailySeatService;
import com.train.common.base.service.SeatService;
import com.train.common.resp.Result;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 车座控制器</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/11/17 下午5:08</li>
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
@RequestMapping("/seat")
public class TrainSeatController {
    private static final Logger log = LoggerFactory.getLogger(TrainSeatController.class);
    @Autowired
    private SeatService seatService;
    @Autowired
    private DailySeatService dailySeatService;

    /**
     * 根据查询条件查询所有车座情况
     * @param trainCode 火车编码
     * @param date 日期
     * @return
     */
    @GetMapping("query-all-seat")
    public Result queryAllSeat(@NotEmpty(message = "火车编码不能为空") String trainCode,
                               @NotNull(message = "请指定查询时间")
                               @DateTimeFormat(pattern = "yyyy-MM-dd")
                               @JsonFormat(pattern = "yyyy-MM-dd")  Date date ){
        log.info("trainCode:{}, date:{}",trainCode,date);
        // key是车厢号、value对应车厢内部车座信息
        Map<String, CarriageSeatResp> result = new HashMap<>();
        DailyTrainSeat dailyTrainSeat = new DailyTrainSeat();
        dailyTrainSeat.setTrainCode(trainCode);
        dailyTrainSeat.setDate(date);
        List<DailyTrainSeat> dailyTrainSeats = dailySeatService.selectDSeatByConditionExample(dailyTrainSeat);
        for (DailyTrainSeat trainSeat : dailyTrainSeats) {
                String key = "车厢" + trainSeat.getCarriageIndex();
            CarriageSeatResp carriageSeatResp = result.get(key);
            if(carriageSeatResp == null){
                Map<String, List<String>> seats = new HashMap<>();
                result.put(key, new CarriageSeatResp(seats));
                carriageSeatResp = result.get(key);
            }
            String col = trainSeat.getCol();
            Map<String, List<String>> seats = carriageSeatResp.getSeats();
            List<String> strings = seats.get(col);
            if(strings == null){
                List<String> list = new ArrayList<>();
                seats.put(col, list);
                strings = seats.get(col);
            }
            String sell = trainSeat.getSell();
            if(StrUtil.isNotEmpty(sell)){
                strings.add(sell);
            }
        }
        return Result.ok().data("result", result);
    }
}
