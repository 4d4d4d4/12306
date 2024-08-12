package com.train.member.controller;

import com.train.common.aspect.annotation.GlobalAnnotation;
import com.train.common.base.entity.domain.Passenger;
import com.train.common.resp.Result;
import com.train.member.entity.vo.PassengerListVo;
import com.train.member.entity.vo.PassengerSaveVo;
import com.train.member.service.PassengerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    @Autowired
    private PassengerService passengerService;

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
        return Result.ok().data("data",passengerList);
    }

}
