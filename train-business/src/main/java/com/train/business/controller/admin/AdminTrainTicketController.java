package com.train.business.controller.admin;

import com.tencentcloudapi.ocr.v20181119.models.TrainTicket;
import com.train.common.base.entity.domain.DailyTrainTicket;
import com.train.common.base.entity.domain.TrainCarriage;
import com.train.common.base.entity.query.CarriageQuery;
import com.train.common.base.entity.query.TicketQuery;
import com.train.common.base.entity.vo.DailyTicketVo;
import com.train.common.base.entity.vo.PaginationResultVo;
import com.train.common.base.service.DailyTicketService;
import com.train.common.resp.Result;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 车票管理</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/11/9 下午3:53</li>
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
@RequestMapping("/admin/ticket")
public class AdminTrainTicketController {
    @DubboReference(version = "1.0.0", check = false)
    private DailyTicketService dailyTicketService;

    @PostMapping("/listByCondition")
    public Result selectTicketsByConditionWithPage(@RequestBody TicketQuery query){
        PaginationResultVo<DailyTrainTicket> result = dailyTicketService.selectAllByConditionWithPage(query);
        return Result.ok().data("result", result);
    }
    @PostMapping("/queryTicket")
    public Result queryTicker(@RequestBody DailyTicketVo ticketVo){

        return Result.ok();
    }
}
