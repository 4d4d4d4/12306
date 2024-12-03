package com.train.business.service.impl;

import com.train.business.mapper.ConfirmOrderMapper;
import com.train.common.base.entity.domain.ConfirmOrder;
import com.train.common.base.service.OrderConfirmService;
import org.apache.dubbo.config.annotation.DubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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
}
