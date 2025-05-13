package com.train.member.controller;

import com.alibaba.fastjson.JSON;
import com.train.common.aspect.annotation.GlobalAnnotation;
import com.train.common.base.entity.domain.*;
import com.train.common.base.entity.query.TicketQuery;
import com.train.common.base.entity.vo.PassengerTicketsVo;
import com.train.common.base.service.DailyTicketService;
import com.train.common.base.service.OrderConfirmService;
import com.train.common.resp.Pagination;
import com.train.common.base.entity.dto.PassengerDto;
import com.train.common.base.entity.vo.PassengerListVo;
import com.train.common.base.entity.vo.PassengerSaveVo;
import com.train.common.base.service.PassengerService;
import com.train.common.resp.Result;
import com.train.common.utils.ThreadLocalUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 乘车人相关controller</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/8/5 上午6:20</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024，. All rights reserved.
 * @Author 16867.
 */
@RestController
@RequestMapping("passenger")
public class PassengerController {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(PassengerController.class);

    @Autowired
    private PassengerService passengerService;
    @DubboReference(version = "1.0.0", check = false)
    private OrderConfirmService orderConfirmService;
    @DubboReference(version = "1.0.0", check = false)
    private DailyTicketService dailyTicketService;

    @PostMapping("save")
    @GlobalAnnotation(checkLogin = true)
    public Result savePassenger(@Validated @RequestBody PassengerSaveVo passengerSaveVo){
        passengerService.save(passengerSaveVo);
        return Result.ok();
    }

    @PostMapping("/list")
    @GlobalAnnotation(checkLogin = true)
    public Result listPassenger(@RequestBody PassengerListVo passengerListVo){
        List<Passenger> passengerList = passengerService.listByCondition(passengerListVo);
        List<PassengerDto> passengerDtoList = passengerList.stream().map((item) -> {
            PassengerDto passengerDto = new PassengerDto();
            BeanUtils.copyProperties(item, passengerDto);
            passengerDto.setId(String.valueOf(item.getId()));
            return passengerDto;
        }).toList();
        Pagination<PassengerDto> passengerDtoPagination = new Pagination<>();
        passengerDtoPagination.setData(passengerDtoList);
        passengerDtoPagination.setCurrent(passengerListVo.getCurrentPage());
        passengerDtoPagination.setSize(passengerListVo.getPageSize());
        Integer total = passengerService.listCount(passengerListVo);
        passengerDtoPagination.setTotal(total);
        return Result.ok().data("data",passengerDtoPagination);
    }
    @PostMapping("/deleteBatch")
    @GlobalAnnotation(checkLogin = true)
    public Result deleteBatchPassenger(@RequestBody List<Long> passengerList){
        if(passengerList.isEmpty()){
            return Result.ok();
        }
        passengerService.deleteByIds(passengerList);
        return Result.ok();
    }

    /**
     * 查询乘客票数据
     * @param passengerListVo
     * @return
     */
    @PostMapping("/selectPassengerTickets")
    @GlobalAnnotation(checkLogin = true)
    public Result selectMineTickets(@RequestBody PassengerTicketsVo passengerListVo){
        Long currentId = ThreadLocalUtils.getCurrentId(); // 获取当前用户ID
        Member member = new Member();
        member.setId(currentId);
        List<ConfirmOrder> list = orderConfirmService.selectOrders(member);
        List<PassengerSelecetOrderInformation> tickets = new ArrayList<>();
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        list.stream().forEach((item) ->{
            PassengerSelecetOrderInformation psi = new PassengerSelecetOrderInformation();
            Date date = item.getDate();
            String start = item.getStart();
            String end = item.getEnd();
            String trainCode = item.getTrainCode();
            Long orderId = item.getId();
            List<PassengerSelecetOrderInformation> list1 = JSON.parseArray(item.getTickets().toString(), PassengerOrderInformation.class).stream().filter(ticket -> ticket.getPassengerId()
                            .equals(passengerListVo.getPassengerId()))
                    .map((it) -> {
                        BeanUtils.copyProperties(it, psi);
                        psi.setStart(start);
                        psi.setOrderId(orderId);
                        psi.setDate(date);
                        psi.setEnd(end);
                        psi.setTrainCode(trainCode);
                        psi.setTicketId(item.getDailyTrainTicketId());
                        DailyTrainTicket dailyTrainTicket = new DailyTrainTicket();
                        dailyTrainTicket.setId(item.getDailyTrainTicketId());
                        DailyTrainTicket dt = dailyTicketService.selectById(dailyTrainTicket);
                        if(dt != null){
                            psi.setStartAndEndTime(timeFormat.format(dt.getStartTime()) + "---" +timeFormat.format(dt.getEndTime()));
                        }
                        return psi;
                    }).toList();
            tickets.addAll(list1);
        });
        log.info("返回的数据:{}", JSON.toJSONString(tickets));
        return Result.ok().data("data", tickets);
    }
}
