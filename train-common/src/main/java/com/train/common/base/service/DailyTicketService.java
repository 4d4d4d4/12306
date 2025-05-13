package com.train.common.base.service;

import com.tencentcloudapi.ocr.v20181119.models.TrainTicket;
import com.train.common.base.entity.domain.DailyTrainTicket;
import com.train.common.base.entity.query.TicketQuery;
import com.train.common.base.entity.query.UTrainTicketQuery;
import com.train.common.base.entity.resp.TrainTicketResp;
import com.train.common.base.entity.vo.PaginationResultVo;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.Date;
import java.util.List;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 每日车票接口</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/11/7 下午3:39</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024，. All rights reserved.
 * @Author cqy.
 */
public interface DailyTicketService {
    void batchDelTicketByDate(Date date);

    void batchInsertList(List<DailyTrainTicket> insertList);

    PaginationResultVo<DailyTrainTicket> selectAllByConditionWithPage(TicketQuery query);

    List<TrainTicketResp> queryTicketByCondition(UTrainTicketQuery query);

    DailyTrainTicket selectDTrainByUniqueIndex(Date date, String trainCode, String start, String end);

    void updateRecord(DailyTrainTicket dailyTrainTicket);

    // 修改车票余票数量
    void updateTicketResidueCount(String trainCode, Date date, String seatType, Integer minStartIndex, Integer maxStartIndex, Integer minEndIndex, Integer maxEndIndex);
    void updateAddTicketResidueCount(String trainCode, Date date, String seatType, Integer minStartIndex, Integer maxStartIndex, Integer minEndIndex, Integer maxEndIndex);

    int delDTicketBeforeNow(Date date);

    DailyTrainTicket selectById(DailyTrainTicket dailyTrainTicket);
}
