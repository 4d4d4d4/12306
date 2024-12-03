package com.train.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.train.common.base.entity.domain.Ticket;
import com.train.common.base.service.TicketService;
import com.train.common.utils.IdStrUtils;
import com.train.member.mapper.TicketMapper;
import org.apache.dubbo.config.annotation.DubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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
 * <li>Date : 2024/11/26 下午8:48</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024，. All rights reserved.
 * @Author cqy.
 */
@DubboService(version = "1.0.0",token = "true")
public class TicketServiceImpl implements TicketService {
    private static final Logger log = LoggerFactory.getLogger(TicketServiceImpl.class);
    @Autowired
    private TicketMapper ticketMapper;
    @Autowired
    private IdStrUtils idStrUtils;
    @Override
    public void saveRecord(Ticket ticket) {
        log.info("插入车票数据：{}", JSON.toJSONString(ticket));
        ticketMapper.insert(ticket);
    }
}
