package com.train.business.controller;

import com.train.common.base.entity.query.UTrainTicketQuery;
import com.train.common.base.entity.resp.TrainTicketResp;
import com.train.common.base.service.DailyTicketService;
import com.train.common.entity.CreateImageCode;
import com.train.common.enums.RedisEnums;
import com.train.common.resp.Result;
import com.train.common.resp.enmus.ResultStatusEnum;
import com.train.common.resp.exception.BusinessException;
import com.train.common.utils.RedisUtils;
import com.train.common.utils.ThreadLocalUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotNull;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

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
    @Autowired
    private RedisUtils redisUtils;

    @PostMapping("/queryTicketByCondition")
    public Result queryTicketByCondition(@RequestBody  UTrainTicketQuery query){
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
    @PostMapping("/order/confirm")
    public Result confirmOrder(){
        return Result.ok();
    }
}
