package com.train.business.controller;

import com.train.common.base.service.StationService;
import com.train.common.aspect.annotation.GlobalAnnotation;
import com.train.common.base.entity.vo.StationNameVo;
import com.train.common.resp.Result;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 用户车站信息控制类</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/10/8 下午9:05</li>
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
@RequestMapping("/station")
public class StationController {
    @DubboReference(version = "1.0.0", check=false)
    private StationService stationService;

    /**
     * 获取所有车票名称信息
     * @param name 模糊查询的名字
     * @return 返回包含汉语，拼音，拼音首字母的车站信息集合
     */
    @GetMapping("/getStationNameList")
    @GlobalAnnotation(checkLogin = true)
    public Result getStationNameList(String name){
        List<StationNameVo> allStationName = stationService.getAllStationName(name);
        if(name == null){
            name = "";
        }
        return Result.ok().data("result", allStationName);
    }
}
