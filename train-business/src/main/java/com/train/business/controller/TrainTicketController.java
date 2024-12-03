package com.train.business.controller;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.train.common.aspect.annotation.GlobalAnnotation;
import com.train.common.base.entity.domain.*;
import com.train.common.base.entity.query.UTrainTicketQuery;
import com.train.common.base.entity.req.PassengerTicketReq;
import com.train.common.base.entity.req.TicketOrderReq;
import com.train.common.base.entity.resp.TrainTicketResp;
import com.train.common.base.service.*;
import com.train.common.entity.CreateImageCode;
import com.train.common.enums.*;
import com.train.common.resp.Result;
import com.train.common.resp.enmus.ResultStatusEnum;
import com.train.common.resp.exception.BusinessException;
import com.train.common.utils.IdStrUtils;
import com.train.common.utils.RedisUtils;
import com.train.common.utils.StringTool;
import com.train.common.utils.ThreadLocalUtils;
import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.dubbo.config.annotation.DubboReference;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 火车车票查询</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/11/11 上午11:38</li>
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
@RequestMapping("/ticket")
public class TrainTicketController {
    private static final Logger log = LoggerFactory.getLogger(TrainTicketController.class);
    @DubboReference(version = "1.0.0", check = false)
    private DailyTicketService dailyTicketService;

    @DubboReference(version = "1.0.0", check = false)
    private DailyTrainService dailyTrainService;
    @DubboReference(version = "1.0.0", check = false)
    private OrderConfirmService orderConfirmService;
    @DubboReference(version = "1.0.0", check = false)
    private DailyStationService dailyStationService;
    @DubboReference(version = "1.0.0", check = false)
    private DailySeatService dailySeatService;
    @DubboReference(version = "1.0.0", check = false)
    private TicketService ticketService;
    @Resource
    private RedisTemplate<String, String> redisTemplate; // 用于操作票锁的redis工具
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private IdStrUtils idStrUtils;

    @PostMapping("/queryTicketByCondition")
    public Result queryTicketByCondition(@RequestBody UTrainTicketQuery query) {
        List<TrainTicketResp> result = dailyTicketService.queryTicketByCondition(query);
        return Result.ok().data("result", result);
    }

    /**
     * 生成图片验证码
     *
     * @param response
     * @param type     验证码类型 用于 1. 购票验证码
     */
    @GetMapping(value = "/getImgCode", produces = {"image/jpeg"})
    @GlobalAnnotation(checkLogin = true)
    public void checkCode(HttpServletResponse response,
                          @RequestParam(required = true) String type) {
        CreateImageCode createImageCode = new CreateImageCode(130, 30, 5, 10);
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/jpeg");
        Long currentId = ThreadLocalUtils.getCurrentId();
        String code = createImageCode.getCode();
        try {
            redisUtils.setEx(RedisEnums.CHECK_CODE_ENUM.getPrefix() + currentId + type, code, RedisEnums.CHECK_CODE_ENUM.getTime() * 10);
            createImageCode.write(response.getOutputStream());
        } catch (IOException e) {
            log.info("验证码写入失败：{}", e.getMessage());
            redisUtils.remove(RedisEnums.CHECK_CODE_ENUM.getPrefix() + currentId + type);
            throw new BusinessException(ResultStatusEnum.CODE_500.getCode(), "验证码异常，请联系管理员");
        }
    }

    // TODO 完善数据校验
    @PostMapping("/order/confirm")
    @GlobalAnnotation(checkLogin = true)
    @GlobalTransactional(timeoutMills = 300000, name = "train-confirm-order")
    public Result confirmOrder(@Valid @RequestBody TicketOrderReq req) {
        // 进入抢票接口去抢锁
        String lock_key = req.getDate() + "_" + req.getTrainCode();
        final String threadID = String.valueOf(Thread.currentThread().getId());
        // 抢锁并设置五秒的过期时间
        Boolean getLock = redisTemplate.opsForValue().setIfAbsent(lock_key, threadID, 5, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(getLock)) {
            log.info("日期:{},火车编码:{},{}线程抢票失败", req.getDate(), req.getTrainCode(), threadID);
            return Result.ok().message("业务繁忙请稍后再试");
        }
        log.info("日期:{},火车编码:{},{}线程抢票成功", req.getDate(), req.getTrainCode(), threadID);
        Thread thread = watchDog(lock_key, threadID);
        thread.start();
        try {
            log.info("车票订单seataID: {}", RootContext.getXID());
            // 数据校验 1.火车存在 2.购票人是否重复购票或者购票时间冲突
            log.info("开始执行车票订购接口，接受的数据是：【{}】", JSON.toJSONString(req));
            String imageCode = req.getImageCode();
            List<PassengerTicketReq> tickets = req.getTickets();
            String trainCode = req.getTrainCode();
            Long trainTicketId = req.getTrainTicketId();
            String start = req.getStart();
            String end = req.getEnd();
            Date date = req.getDate();
            String type = CheckCodeTypeEnum.TICKET_ORDER.getType();
            Long currentId = ThreadLocalUtils.getCurrentId();
            Date now = new Date();

            // 验证码校核
//            String redisCode = (String) redisUtils.get(RedisEnums.CHECK_CODE_ENUM.getPrefix() + currentId + type);
//            if (!imageCode.equals(redisCode)) {
//                log.info("校验码校核失败，存储的校验码是：【{}】， 接受的校验码是：【{}】", redisCode, imageCode);
//                throw new BusinessException(ResultStatusEnum.CODE_505);
//            }
            DailyTrainTicket dailyTrainTicket = dailyTicketService.selectDTrainByUniqueIndex(date, trainCode, start, end);
            log.info("车票源数据;根据唯一键查找出来的数据：【{}】", JSON.toJSONString(dailyTrainTicket));
            String resultErrorMessage = ""; // 失败返回信息
            ConfirmOrder order = new ConfirmOrder();
            order.setCreateTime(now);
            order.setUpdateTime(now);
            Long orderId = idStrUtils.snowFlakeLong();
            order.setId(orderId);
            log.info("初始化订单表车票数据:{}", JSON.toJSONString(tickets));
            order.setTickets(JSON.toJSONString(tickets));
            order.setDate(date);
            order.setDailyTrainTicketId(trainTicketId);
            order.setEnd(end);
            order.setStart(start);
            order.setStatus(ConfirmOrderStatusEnum.INIT.getCode());
            order.setMemberId(currentId);
            order.setTrainCode(trainCode);
            // 将购票信息存入数据库
            int inserts = orderConfirmService.insertRecord(order);
            boolean isChooseSeat = StringTool.filedValueIsSameInObjList(tickets, PassengerTicketReq::getSeatTypeCode); // 是否进行选座
            try {
                // 余票查询校验 预减
                String oneSeatType = tickets.get(0).getSeatTypeCode();
                List<PassengerOrderInformation> infos = new ArrayList<>(); // 构造各个乘客相对位置列表
                for (PassengerTicketReq ticket : tickets) {
                    PassengerOrderInformation passengerOrderInformation = new PassengerOrderInformation();
                    String seat = ticket.getSeat();
                    String seatTypeCode = ticket.getSeatTypeCode();
                    SeatTypeEnum seatTypeEnumByCode = SeatTypeEnum.getSeatTypeEnumByCode(seatTypeCode);
                    if (seatTypeEnumByCode == null) {
                        log.error("根据座位类型编码:【{}】，查询到的座位类型枚举为null", seatTypeCode);
                        throw new BusinessException(ResultStatusEnum.CODE_500);
                    }
                    switch (seatTypeEnumByCode) {
                        case YDZ -> {
                            Integer ydz = dailyTrainTicket.getYdz();
                            if (ydz < 1) {
                                throw new BusinessException(ResultStatusEnum.CODE_510.getCode(), "一等票余票不足");
                            }
                            dailyTrainTicket.setYdz(ydz - 1);
                        }
                        case EDZ -> {
                            Integer edz = dailyTrainTicket.getEdz();
                            if (edz < 1) {
                                throw new BusinessException(ResultStatusEnum.CODE_510.getCode(), "二等票余票不足");
                            }
                            dailyTrainTicket.setEdz(edz - 1);
                        }
                        case RWZ -> {
                            Integer rw = dailyTrainTicket.getRw();
                            if (rw < 1) {
                                throw new BusinessException(ResultStatusEnum.CODE_510.getCode(), "软卧余票不足");
                            }
                            dailyTrainTicket.setYdz(rw - 1);
                        }
                        case YWZ -> {
                            Integer yw = dailyTrainTicket.getYw();
                            if (yw < 1) {
                                throw new BusinessException(ResultStatusEnum.CODE_510.getCode(), "硬卧余票不足");
                            }
                            dailyTrainTicket.setYdz(yw - 1);
                        }
                    }
                    if (isChooseSeat && seat != null) {
                        // 选座
                        // 1.计算所有座位的相对位置
                        String seatCode = String.valueOf(seat.charAt(0)); // ACDF
                        String rowNumber = String.valueOf(seat.charAt(1)); // 1/2
                        // 将对应字母转化位置 如果是第二排则增加该类型车座的列数 例如 一等座 C1 => 2 F2 => 8 得出相对位置的值
                        Integer stringToNumberByEnum = StringTool.stringToNumberByEnum(seatTypeCode, seatCode);
                        // 计算乘车所选座位的相对位置
                        int relativeLocation = rowNumber.equals("1") ? stringToNumberByEnum : stringToNumberByEnum + SeatColumnEnum.getColCountBySeatTypeCode(seatTypeCode);
                        passengerOrderInformation.setIndex(relativeLocation);
                        passengerOrderInformation.setSeatCode(seatCode);
                    } else {
                        isChooseSeat = false;
                    }
                    passengerOrderInformation.setPassengerName(ticket.getPassengerName());
                    passengerOrderInformation.setPassengerType(ticket.getPassengerType());
                    passengerOrderInformation.setPassengerId(ticket.getPassengerId());
                    passengerOrderInformation.setSeatType(seatTypeCode);
                    infos.add(passengerOrderInformation);
                }
                // 进行选座

                // 1.0 根据火车编码、日期、座位类型查询出所有符合条件的车票
                DailyTrainSeat dailyTrainSeat = new DailyTrainSeat();
                dailyTrainSeat.setTrainCode(trainCode);
                dailyTrainSeat.setDate(date);
                if (isChooseSeat && StrUtil.isNotBlank(oneSeatType)) {
                    dailyTrainSeat.setSeatType(oneSeatType);
                }
                // 2.0 筛选掉车票中和初始站终点站冲突的车票(中途站有被其他乘客购买的情况)
                Integer startStationIndex = dailyTrainTicket.getStartIndex(); // 起始站从1开始
                Integer endStationIndex = dailyTrainTicket.getEndIndex();
                List<DailyTrainSeat> dailyTrainSeats = dailySeatService.selectDSeatByConditionExample(dailyTrainSeat).stream().filter((item) -> {
                    String sell = item.getSell();
                    return !sell.substring(startStationIndex - 1, endStationIndex - 1).contains("1");
                }).toList();


                if (isChooseSeat) { // 选座逻辑
                    log.info("进入选座逻辑");
                    // 根据车厢分组（选座必须是同一车厢内）
                    Integer selectResult = 0;
                    Map<Integer, List<DailyTrainSeat>> seatByCarriageIndexMaps = dailyTrainSeats.stream().filter((item) -> item.getSeatType().equals(oneSeatType)).collect(Collectors.groupingBy(DailyTrainSeat::getCarriageIndex));
                    for (Map.Entry<Integer, List<DailyTrainSeat>> entry : seatByCarriageIndexMaps.entrySet()) {
                        ArrayList<Long> selectedSeats = new ArrayList<>();
                        List<DailyTrainSeat> seatList = entry.getValue();
                        selectResult = chooseSeatForEveryOne(infos, seatList, 0, 0, selectedSeats);
                        if (selectResult == infos.size()) {
                            break;
                        }
                    }
                    if (selectResult != infos.size()) {
                        throw new BusinessException(ResultStatusEnum.CODE_510);
                    }
                } else { // 随机座位
                    randomAssignSeat(infos, dailyTrainSeats);
                }
                // 选座结束

                // 更新订单表和座位表,保存乘客定票信息
                log.info("更新订单表车票数据:{}", JSON.toJSONString(infos));
                order.setTickets(JSON.toJSONString(infos));
                order.setStatus(ConfirmOrderStatusEnum.SUCCESS.getCode());
                log.info("要更新的表单数据:{}", JSON.toJSONString(order));
                orderConfirmService.updateRecord(order);
                log.info("更新座位表");
                for (PassengerOrderInformation info : infos) {
                    Long seatId = info.getSeatId();
                    DailyTrainSeat seat = dailySeatService.selectDSeatByKey(seatId);
                    String sell = seat.getSell();
                    // TODO 其实可以使用过或运算，但由于有补零的操作感觉容易出现错误故而使用效率稍微低一些的字符串操作
                    String newSell = StringTool.replaceSubstring(sell, startStationIndex - 1, endStationIndex - 1, '1');
                    // 已知newSell = sell | affectSell 则 affectSell = newSell & ~sell 需要注意的是需要前面补零
                    // 由公式计算不会出错
                    String affectSell = Integer.toBinaryString(Integer.parseInt(newSell, 2) & (~Integer.parseInt(sell, 2)));
                    affectSell = StrUtil.fillBefore(affectSell, '0', sell.length());
                    log.info("更新售卖图,源售卖图：{}，影响的售卖图:{},新的售卖图:{},起始站序:{},终点站序:{}", sell, affectSell, newSell, startStationIndex, endStationIndex);
                    seat.setSell(newSell);
                    seat.setUpdateTime(new Date());
                    dailySeatService.updateDSeat(seat);
                    // 更新车票数目 需要找出车票数目影响的范围
                    // sell  100000
                    // afff  001100 0 1 2 3 4  买的是3-5 影响的最小坐标为 start - 2往前的最后一个0 end - 1往后的最后一个0
                    // newa  101111 // 5 - 6
                    log.info("更新车票数目:{}", JSON.toJSONString(dailyTrainTicket));
                    Integer minStartIndex = 0;
                    for (int t = startStationIndex - 2; t >= 0; t--) {
                        if (newSell.charAt(t) == '1') {
                            minStartIndex = t + 1;
                            break;
                        }
                    }
                    Integer maxStartIndex = startStationIndex;
                    Integer minEndIndex = endStationIndex;
                    Integer maxEndIndex = newSell.length();
                    //110
                    for (int t = endStationIndex - 1; t < newSell.length(); t++) {
                        if (newSell.charAt(t) == '1') {
                            maxEndIndex = t;
                            break;
                        }
                    }
                    maxEndIndex = maxEndIndex + 1;
                    minStartIndex = minStartIndex + 1;
                    // 000
                    // 100
                    // 100
                    log.info("影响站点的区间为初始站{}-{}，终点站{}-{}", minStartIndex, maxStartIndex, minEndIndex, maxEndIndex);
                    dailyTicketService.updateTicketResidueCount(trainCode, date, seat.getSeatType(), minStartIndex, maxStartIndex, minEndIndex, maxEndIndex);
                    // 保存车票
                    Ticket ticket = new Ticket();
                    ticket.setId(idStrUtils.snowFlakeLong());
                    ticket.setMemberId(currentId);
                    ticket.setPassengerId(info.getPassengerId());
                    ticket.setPassengerName(info.getPassengerName());
                    ticket.setTrainDate(date);
                    ticket.setTrainCode(trainCode);
                    ticket.setCarriageIndex(info.getCarriageIndex());
                    ticket.setSeatRow(info.getRow());
                    ticket.setSeatCol(info.getSeatCode());
                    ticket.setStartStation(start);
                    ticket.setEndStation(end);
                    ticket.setStartTime(dailyTrainTicket.getStartTime());
                    ticket.setEndTime(dailyTrainTicket.getEndTime());
                    ticket.setSeatType(info.getSeatType());
                    ticket.setCreateTime(now);
                    ticket.setUpdateTime(now);
                    ticketService.saveRecord(ticket);
                }

            } catch (BusinessException be) {
                if (be.getResultStatusEnum().equals(ResultStatusEnum.CODE_510)) {
                    log.info("订单order:【{}】无票，更新订单表", JSON.toJSONString(order));
                    order.setStatus(ConfirmOrderStatusEnum.EMPTY.getCode());
                    resultErrorMessage = "购票失败,当前所购车票已售罄";
                    if(isChooseSeat){
                        resultErrorMessage = "当前所选座位无票,请重新选择(推荐随机选座)";
                    }
                } else {
                    log.info("订单order:【{}】异常，更新订单表", JSON.toJSONString(order));
                    order.setStatus(ConfirmOrderStatusEnum.FAILURE.getCode());
                }
                order.setUpdateTime(new Date());
                orderConfirmService.updateRecord(order);
            }
            boolean success = order.getStatus().equals(ConfirmOrderStatusEnum.SUCCESS.getCode());
            return success ? Result.ok().data("orderId", orderId) : Result.error().data("orderId", orderId).message(StrUtil.isNotBlank(resultErrorMessage) ? resultErrorMessage : "购票失败");
        } catch (Exception e) {
            log.info("遇到未知异常:{}", e.getMessage());
            throw new RuntimeException(e);
        } finally {
            // 删除锁
            String l = redisTemplate.opsForValue().get(lock_key);
            // 只删除自己的锁
            if (threadID.equals(l)) {
                redisTemplate.delete(lock_key);
            }
        }
//        log.info("车票订单seataID: {}", RootContext.getXID());
//        // 数据校验 1.火车存在 2.购票人是否重复购票或者购票时间冲突
//        log.info("开始执行车票订购接口，接受的数据是：【{}】", JSON.toJSONString(req));
//        String imageCode = req.getImageCode();
//        List<PassengerTicketReq> tickets = req.getTickets();
//        String trainCode = req.getTrainCode();
//        Long trainTicketId = req.getTrainTicketId();
//        String start = req.getStart();
//        String end = req.getEnd();
//        Date date = req.getDate();
//        String type = CheckCodeTypeEnum.TICKET_ORDER.getType();
//        Long currentId = ThreadLocalUtils.getCurrentId();
//        Date now = new Date();
//
//        // 验证码校核
//        String redisCode = (String) redisUtils.get(RedisEnums.CHECK_CODE_ENUM.getPrefix() + currentId + type);
//        if (!imageCode.equals(redisCode)) {
//            log.info("校验码校核失败，存储的校验码是：【{}】， 接受的校验码是：【{}】", redisCode, imageCode);
//            throw new BusinessException(ResultStatusEnum.CODE_505);
//        }
//        DailyTrainTicket dailyTrainTicket = dailyTicketService.selectDTrainByUniqueIndex(date, trainCode, start, end);
//        log.info("车票源数据;根据唯一键查找出来的数据：【{}】", JSON.toJSONString(dailyTrainTicket));
//
//        ConfirmOrder order = new ConfirmOrder();
//        order.setCreateTime(now);
//        order.setUpdateTime(now);
//        Long orderId = idStrUtils.snowFlakeLong();
//        order.setId(orderId);
//        log.info("初始化订单表车票数据:{}", JSON.toJSONString(tickets));
//        order.setTickets(JSON.toJSONString(tickets));
//        order.setDate(date);
//        order.setDailyTrainTicketId(trainTicketId);
//        order.setEnd(end);
//        order.setStart(start);
//        order.setStatus(ConfirmOrderStatusEnum.INIT.getCode());
//        order.setMemberId(currentId);
//        order.setTrainCode(trainCode);
//        // 将购票信息存入数据库
//        int inserts = orderConfirmService.insertRecord(order);
//        try {
//            // 余票查询校验 预减
//            boolean isChooseSeat = StringTool.filedValueIsSameInObjList(tickets, PassengerTicketReq::getSeatTypeCode); // 是否进行选座
//            String oneSeatType = tickets.get(0).getSeatTypeCode();
//            List<PassengerOrderInformation> infos = new ArrayList<>(); // 构造各个乘客相对位置列表
//            for (PassengerTicketReq ticket : tickets) {
//                PassengerOrderInformation passengerOrderInformation = new PassengerOrderInformation();
//                String seat = ticket.getSeat();
//                String seatTypeCode = ticket.getSeatTypeCode();
//                SeatTypeEnum seatTypeEnumByCode = SeatTypeEnum.getSeatTypeEnumByCode(seatTypeCode);
//                if (seatTypeEnumByCode == null) {
//                    log.error("根据座位类型编码:【{}】，查询到的座位类型枚举为null", seatTypeCode);
//                    throw new BusinessException(ResultStatusEnum.CODE_500);
//                }
//                switch (seatTypeEnumByCode) {
//                    case YDZ -> {
//                        Integer ydz = dailyTrainTicket.getYdz();
//                        if (ydz < 1) {
//                            throw new BusinessException(ResultStatusEnum.CODE_510.getCode(), "一等票余票不足");
//                        }
//                        dailyTrainTicket.setYdz(ydz - 1);
//                    }
//                    case EDZ -> {
//                        Integer edz = dailyTrainTicket.getEdz();
//                        if (edz < 1) {
//                            throw new BusinessException(ResultStatusEnum.CODE_510.getCode(), "二等票余票不足");
//                        }
//                        dailyTrainTicket.setEdz(edz - 1);
//                    }
//                    case RWZ -> {
//                        Integer rw = dailyTrainTicket.getRw();
//                        if (rw < 1) {
//                            throw new BusinessException(ResultStatusEnum.CODE_510.getCode(), "软卧余票不足");
//                        }
//                        dailyTrainTicket.setYdz(rw - 1);
//                    }
//                    case YWZ -> {
//                        Integer yw = dailyTrainTicket.getYw();
//                        if (yw < 1) {
//                            throw new BusinessException(ResultStatusEnum.CODE_510.getCode(), "硬卧余票不足");
//                        }
//                        dailyTrainTicket.setYdz(yw - 1);
//                    }
//                }
//                if (isChooseSeat && seat != null) {
//                    // 选座
//                    // 1.计算所有座位的相对位置
//                    String seatCode = String.valueOf(seat.charAt(0)); // ACDF
//                    String rowNumber = String.valueOf(seat.charAt(1)); // 1/2
//                    // 将对应字母转化位置 如果是第二排则增加该类型车座的列数 例如 一等座 C1 => 2 F2 => 8 得出相对位置的值
//                    Integer stringToNumberByEnum = StringTool.stringToNumberByEnum(seatTypeCode, seatCode);
//                    // 计算乘车所选座位的相对位置
//                    int relativeLocation = rowNumber.equals("1") ? stringToNumberByEnum : stringToNumberByEnum + SeatColumnEnum.getColCountBySeatTypeCode(seatTypeCode);
//                    passengerOrderInformation.setIndex(relativeLocation);
//                    passengerOrderInformation.setSeatCode(seatCode);
//                } else {
//                    isChooseSeat = false;
//                }
//                passengerOrderInformation.setPassengerName(ticket.getPassengerName());
//                passengerOrderInformation.setPassengerType(ticket.getPassengerType());
//                passengerOrderInformation.setPassengerId(ticket.getPassengerId());
//                passengerOrderInformation.setSeatType(seatTypeCode);
//                infos.add(passengerOrderInformation);
//            }
//            // 进行选座
//
//            // 1.0 根据火车编码、日期、座位类型查询出所有符合条件的车票
//            DailyTrainSeat dailyTrainSeat = new DailyTrainSeat();
//            dailyTrainSeat.setTrainCode(trainCode);
//            dailyTrainSeat.setDate(date);
//            if (isChooseSeat && StrUtil.isNotBlank(oneSeatType)) {
//                dailyTrainSeat.setSeatType(oneSeatType);
//            }
//            // 2.0 筛选掉车票中和初始站终点站冲突的车票(中途站有被其他乘客购买的情况)
//            Integer startStationIndex = dailyTrainTicket.getStartIndex(); // 起始站从1开始
//            Integer endStationIndex = dailyTrainTicket.getEndIndex();
//            List<DailyTrainSeat> dailyTrainSeats = dailySeatService.selectDSeatByConditionExample(dailyTrainSeat).stream().filter((item) -> {
//                String sell = item.getSell();
//                return !sell.substring(startStationIndex - 1, endStationIndex - 1).contains("1");
//            }).toList();
//
//
//            if (isChooseSeat) { // 选座逻辑
//                log.info("进入选座逻辑");
//                // 根据车厢分组（选座必须是同一车厢内）
//                Integer selectResult = 0;
//                Map<Integer, List<DailyTrainSeat>> seatByCarriageIndexMaps = dailyTrainSeats.stream().filter((item) -> item.getSeatType().equals(oneSeatType)).collect(Collectors.groupingBy(DailyTrainSeat::getCarriageIndex));
//                for (Map.Entry<Integer, List<DailyTrainSeat>> entry : seatByCarriageIndexMaps.entrySet()) {
//                    ArrayList<Long> selectedSeats = new ArrayList<>();
//                    List<DailyTrainSeat> seatList = entry.getValue();
//                    selectResult = chooseSeatForEveryOne(infos, seatList, 0, 0, selectedSeats);
//                    if (selectResult == infos.size()) {
//                        break;
//                    }
//                }
//                if (selectResult != infos.size()) {
//                    throw new BusinessException(ResultStatusEnum.CODE_510);
//                }
//            } else { // 随机座位
//                randomAssignSeat(infos, dailyTrainSeats);
//            }
//            // 选座结束
//
//            // 更新订单表和座位表,保存乘客定票信息
//            log.info("更新订单表车票数据:{}", JSON.toJSONString(infos));
//            order.setTickets(JSON.toJSONString(infos));
//            order.setStatus(ConfirmOrderStatusEnum.SUCCESS.getCode());
//            log.info("要更新的表单数据:{}", JSON.toJSONString(order));
//            orderConfirmService.updateRecord(order);
//            log.info("更新座位表");
//            for (PassengerOrderInformation info : infos) {
//                Long seatId = info.getSeatId();
//                DailyTrainSeat seat = dailySeatService.selectDSeatByKey(seatId);
//                String sell = seat.getSell();
//                // TODO 其实可以使用过或运算，但由于有补零的操作感觉容易出现错误故而使用效率稍微低一些的字符串操作
//                String newSell = StringTool.replaceSubstring(sell, startStationIndex - 1, endStationIndex - 1, '1');
//                // 已知newSell = sell | affectSell 则 affectSell = newSell & ~sell 需要注意的是需要前面补零
//                // 由公式计算不会出错
//                String affectSell = Integer.toBinaryString(Integer.parseInt(newSell, 2) & (~Integer.parseInt(sell, 2)));
//                affectSell = StrUtil.fillBefore(affectSell, '0', sell.length());
//                log.info("更新售卖图,源售卖图：{}，影响的售卖图:{},新的售卖图:{},起始站序:{},终点站序:{}", sell, affectSell, newSell, startStationIndex, endStationIndex);
//                seat.setSell(newSell);
//                seat.setUpdateTime(new Date());
//                dailySeatService.updateDSeat(seat);
//                // 更新车票数目 需要找出车票数目影响的范围
//                // sell  100000
//                // afff  001100 0 1 2 3 4  买的是3-5 影响的最小坐标为 start - 2往前的最后一个0 end - 1往后的最后一个0
//                // newa  101111 // 5 - 6
//                log.info("更新车票数目:{}", JSON.toJSONString(dailyTrainTicket));
//                Integer minStartIndex = 0;
//                for (int t = startStationIndex - 2; t >= 0; t--) {
//                    if (newSell.charAt(t) == '1') {
//                        minStartIndex = t + 1;
//                        break;
//                    }
//                }
//                Integer maxStartIndex = startStationIndex;
//                Integer minEndIndex = endStationIndex;
//                Integer maxEndIndex = newSell.length();
//                //110
//                for (int t = endStationIndex - 1; t < newSell.length(); t++) {
//                    if (newSell.charAt(t) == '1') {
//                        maxEndIndex = t;
//                        break;
//                    }
//                }
//                maxEndIndex = maxEndIndex + 1;
//                minStartIndex = minStartIndex + 1;
//                // 000
//                // 100
//                // 100
//                log.info("影响站点的区间为初始站{}-{}，终点站{}-{}", minStartIndex, maxStartIndex, minEndIndex, maxEndIndex);
//                dailyTicketService.updateTicketResidueCount(trainCode, date, seat.getSeatType(), minStartIndex, maxStartIndex, minEndIndex, maxEndIndex);
//                // 保存车票
//                Ticket ticket = new Ticket();
//                ticket.setId(idStrUtils.snowFlakeLong());
//                ticket.setMemberId(currentId);
//                ticket.setPassengerId(info.getPassengerId());
//                ticket.setPassengerName(info.getPassengerName());
//                ticket.setTrainDate(date);
//                ticket.setTrainCode(trainCode);
//                ticket.setCarriageIndex(info.getCarriageIndex());
//                ticket.setSeatRow(info.getRow());
//                ticket.setSeatCol(info.getSeatCode());
//                ticket.setStartStation(start);
//                ticket.setEndStation(end);
//                ticket.setStartTime(dailyTrainTicket.getStartTime());
//                ticket.setEndTime(dailyTrainTicket.getEndTime());
//                ticket.setSeatType(info.getSeatType());
//                ticket.setCreateTime(now);
//                ticket.setUpdateTime(now);
//                ticketService.saveRecord(ticket);
//            }
//
//        } catch (BusinessException be) {
//            if (be.getResultStatusEnum().equals(ResultStatusEnum.CODE_510)) {
//                log.info("订单order:【{}】无票，更新订单表", JSON.toJSONString(order));
//                order.setStatus(ConfirmOrderStatusEnum.EMPTY.getCode());
//            } else {
//                log.info("订单order:【{}】异常，更新订单表", JSON.toJSONString(order));
//                order.setStatus(ConfirmOrderStatusEnum.FAILURE.getCode());
//            }
//            order.setUpdateTime(new Date());
//            orderConfirmService.updateRecord(order);
//        }
//        boolean success = order.getStatus().equals(ConfirmOrderStatusEnum.SUCCESS.getCode());
//        return success ? Result.ok().data("orderId", orderId) : Result.error().data("orderId", orderId).message("购票失败");
    }

    private @NotNull Thread watchDog(String lock_key, String threadID) {
        Runnable runnable = () -> {
            // 看门狗 每两秒续一次锁
            while (true) {
                String value = redisTemplate.opsForValue().get(lock_key);
                if (threadID.equals(value)) {
                    redisTemplate.expire(lock_key, 5, TimeUnit.SECONDS);
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // 恢复中断状态
                    break; // 退出循环
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.setDaemon(true); // 守护线程
        return thread;
    }

    private void randomAssignSeat(List<PassengerOrderInformation> infos, List<DailyTrainSeat> dailyTrainSeats) {
        List<DailyTrainSeat> temp = new ArrayList<>(dailyTrainSeats);
        for (PassengerOrderInformation info : infos) {
            // 根据座位类型和座位码选择
            Optional<DailyTrainSeat> any = temp.stream().filter((item) -> item.getCol().equals(info.getSeatCode()) && item.getSeatType().equals(info.getSeatType())).findAny();
            if (any.isPresent()) {
                DailyTrainSeat dailyTrainSeat = any.get();
                info.setSeatId(dailyTrainSeat.getId());
                info.setCarriageIndex(dailyTrainSeat.getCarriageIndex());
                info.setRow(dailyTrainSeat.getRow());
                info.setCarriageSeatIndex(dailyTrainSeat.getCarriageSeatIndex());
                temp.remove(dailyTrainSeat); // 防止选重复
            } else {
                throw new BusinessException(ResultStatusEnum.CODE_510);
            }
        }
    }

    /**
     * 为每一个人选择座位信息 并保存在infos中
     *
     * @param infos         乘车人乘票信息
     * @param seats         根据火车编码、日期、车厢号、座位类型筛选出的座位信息
     * @param index         乘车人序号 从0开始表示第一乘车人
     * @param seatIndex     上一次乘车人选择的座位序号 需要从该序号后开始选择 默认为0
     * @param selectedSeats 已被选择的车座
     */
    private Integer chooseSeatForEveryOne(List<PassengerOrderInformation> infos, List<DailyTrainSeat> seats, int index, int seatIndex, ArrayList<Long> selectedSeats) {
        if (index >= infos.size()) {
            return infos.size();
        }
        log.info("正在为:{}号乘客选座，当前选择的座位号是：{}，当前选座结果为:{}", index, seatIndex, JSON.toJSONString(infos));
        PassengerOrderInformation passenger = infos.get(index);
        // 根据座位码和车厢车座序号筛选出符合条件车座
        // 根据座位编码查询所有座位 并按照车厢座位序号排列
        Integer seatIndexTemp = seatIndex; // 浅拷贝
        List<DailyTrainSeat> seatsTemp = seats.stream()
                .filter(item -> item.getCol().equals(passenger.getSeatCode())
                        &&
                        item.getCarriageSeatIndex() > seatIndexTemp
                        &&
                        !selectedSeats.contains(item.getId())).toList();
        log.info("{}号乘客正在从{}个座位中挑选", index, seatsTemp.size());
        if (seatsTemp.isEmpty()) {
            return 0;
        }
        if (index == 0) {
            DailyTrainSeat dailyTrainSeat = seatsTemp.get(0);
            // 选择一个车座 更新index和seatIndex
            passenger.setSeatId(dailyTrainSeat.getId());
            passenger.setCarriageIndex(dailyTrainSeat.getCarriageIndex());
            passenger.setCarriageSeatIndex(dailyTrainSeat.getCarriageSeatIndex());
            passenger.setRow(dailyTrainSeat.getRow());
            index = index + 1;
            seatIndex = dailyTrainSeat.getCarriageSeatIndex();
            // 成功分配去选择下一个乘客座位
            log.info("0号乘客选票完成，其选择的座位是:{}", JSON.toJSONString(passenger));
            selectedSeats.add(dailyTrainSeat.getId());
            return chooseSeatForEveryOne(infos, seats, index, seatIndex, selectedSeats);
        } else {
            // 如果不是第一个乘客则根据前一个乘客选的座位去选择
            PassengerOrderInformation prePassenger = infos.get(index - 1);
            // 计算出遇上一个座位的偏移值 通过与第二个座位位置确认座位信息
            Integer deviation = passenger.getIndex() - prePassenger.getIndex();
            Integer nextSeatIndex = deviation + prePassenger.getCarriageSeatIndex();
            log.info("上一个乘客选择的座位信息：{}, 逻辑上的偏移值为:{}", JSON.toJSONString(prePassenger), deviation);
            log.info("根据上一个乘客({})选择的座位号({})，当前乘客应当选择的座位号是:{}", index - 1, prePassenger.getCarriageSeatIndex(), nextSeatIndex);
            Optional<DailyTrainSeat> first = seatsTemp.stream().filter((item) -> item.getCarriageSeatIndex().equals(nextSeatIndex)).findFirst();
            // 是否分配到座位
            if (first.isPresent()) {
                DailyTrainSeat dailyTrainSeat = first.get();
                passenger.setSeatId(dailyTrainSeat.getId());
                passenger.setCarriageIndex(dailyTrainSeat.getCarriageIndex());
                passenger.setCarriageSeatIndex(dailyTrainSeat.getCarriageSeatIndex());
                passenger.setRow(dailyTrainSeat.getRow());
                index = index + 1;
                seatIndex = dailyTrainSeat.getCarriageSeatIndex();
                log.info("{}号乘客选票完成，其选择的座位是:{}", index - 1, JSON.toJSONString(passenger));

                // 成功分配去选择下一个乘客座位
                if (index == infos.size() - 1) {
                    selectedSeats.add(dailyTrainSeat.getId());
                    return infos.size();
                } else {
                    selectedSeats.add(dailyTrainSeat.getId());
                    return chooseSeatForEveryOne(infos, seats, index, seatIndex, selectedSeats);
                }
            } else {
                // 去重新给上一个用户分配座位，并且分配的座位应该在他选的座位之后
                selectedSeats.remove(index - 1);
                // 重置上一个乘客的状态
                prePassenger.setSeatId(null);
                prePassenger.setCarriageIndex(null);
                prePassenger.setCarriageSeatIndex(null);
                prePassenger.setRow(null);
                return chooseSeatForEveryOne(infos, seats, index - 1, seatIndex, selectedSeats);
            }
        }
    }
}
