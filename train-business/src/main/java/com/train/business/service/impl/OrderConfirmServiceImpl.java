package com.train.business.service.impl;

import com.train.business.mapper.ConfirmOrderMapper;
import com.train.common.base.entity.domain.ConfirmOrder;
import com.train.common.base.entity.domain.Member;
import com.train.common.base.entity.query.ConfirmOrderExample;
import com.train.common.base.service.OrderConfirmService;
import com.train.common.enums.ConfirmOrderStatusEnum;
import com.train.common.utils.ThreadLocalUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 订单接口实现</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/11/21 下午11:24</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024，. All rights reserved.
 * @Author cqy.
 */
@DubboService(version = "1.0.0", token = "true")
public class OrderConfirmServiceImpl implements OrderConfirmService {
    private static final Logger log = LoggerFactory.getLogger(OrderConfirmServiceImpl.class);
    @Autowired
    private ConfirmOrderMapper confirmOrderMapper;
    @Override
    public int insertRecord(ConfirmOrder order) {
        return confirmOrderMapper.insert(order);
    }

    @Override
    public void updateRecord(ConfirmOrder order) {
        log.info("更新表单数据：{}", order);
        confirmOrderMapper.updateByPrimaryKeySelective(order);
    }

    @Override
    public List<ConfirmOrder> selectOrders(Member member) {
        ConfirmOrderExample example = new ConfirmOrderExample();
        example.createCriteria().andMemberIdEqualTo(member.getId());
        log.info("接收到的数据：{}",  confirmOrderMapper.selectpassenger(member.getId()));
        return  confirmOrderMapper.selectpassenger(member.getId());
    }

    @Override
    public ConfirmOrder selectOrderById(Long ticketId) {
        return confirmOrderMapper.selectByPrimaryKey(ticketId);
    }

    @Override
    public List<ConfirmOrder> selectOrders() {
        ConfirmOrderExample example = new ConfirmOrderExample();
        ConfirmOrderExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo(ConfirmOrderStatusEnum.SUCCESS.getCode());
        List<ConfirmOrder> confirmOrders = confirmOrderMapper.selectByExample(example);
        return confirmOrders;
    }
}
