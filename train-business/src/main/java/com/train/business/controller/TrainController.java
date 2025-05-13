package com.train.business.controller;

import com.train.business.mapper.TrainMapper;
import com.train.common.base.entity.domain.Train;
import com.train.common.base.service.TrainService;
import com.train.common.resp.Result;
import com.train.common.utils.IdStrUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : </dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2025/5/5 上午11:45</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2025，. All rights reserved.
 * @Author cqy.
 */
@RestController
@RequestMapping("/train")
public class TrainController {
    @Autowired
    private IdStrUtils idStrUtils;
    @Autowired
    private TrainService trainService;
    @GetMapping("/test")
    public Result test(){
        Train train = new Train();
        Long id = idStrUtils.snowFlakeLong();
        train.setId(id);
        train.setCode("123");
        trainService.insert(train);
        return Result.ok().data("t",train);
    }

}
