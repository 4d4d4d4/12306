package com.train.business.controller;

import com.alibaba.fastjson.JSON;
import com.train.common.base.entity.domain.ConfirmOrder;
import com.train.common.base.entity.domain.PassengerOrderInformation;
import com.train.common.base.entity.resp.OrderConfirmResp;
import com.train.common.base.service.OrderConfirmService;
import com.train.common.resp.Result;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 订单控制类</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/11/21 下午11:22</li>
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
@RequestMapping("/order/confirm")
public class OrderConfirmController {
     @DubboReference(version = "1.0.0",check = false)
     private OrderConfirmService orderService;

     @PostMapping("/allOrders")
     public Result getAllOrders(){
          List<ConfirmOrder> orders =  orderService.selectOrders();
          if(orders == null || orders.isEmpty()){
               return Result.ok();
          }
          Map<String, Integer> mapByCode = new HashMap<>();
          Map<String, Integer> mapByTime = new HashMap<>();
          Map<String, Integer> mapByTimeCode = new HashMap<>();
          SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
          orders.forEach((it) -> {
               String date = simpleDateFormat.format(it.getDate());
               String trainCode = it.getTrainCode();
               String key = date + "_" + trainCode;
              Integer ticketCount= JSON.parseArray((String)it.getTickets(), PassengerOrderInformation.class).size();
               mapByCode.merge(trainCode, ticketCount, Integer::sum);
               mapByTime.merge(date, ticketCount, Integer::sum);
               mapByTimeCode.merge(key, ticketCount, Integer::sum);
          });
          OrderConfirmResp orderConfirmResp = new OrderConfirmResp();
          orderConfirmResp.setMapByCode(mapByCode);
          orderConfirmResp.setMapByTime(mapByTime);
          orderConfirmResp.setMapByTimeCode(mapByTimeCode);
          return  Result.ok().data("data",orderConfirmResp);
     }


}
