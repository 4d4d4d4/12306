package com.train.member.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.github.pagehelper.PageHelper;
import com.train.common.base.entity.domain.Passenger;
import com.train.common.base.entity.query.PassengerExample;
import com.train.common.base.entity.vo.PassengerListVo;
import com.train.common.base.entity.vo.PassengerSaveVo;
import com.train.common.base.service.PassengerService;
import com.train.common.resp.enmus.ResultStatusEnum;
import com.train.common.resp.exception.BusinessException;
import com.train.common.utils.IdStrUtils;
import com.train.common.utils.ThreadLocalUtils;
import com.train.member.mapper.PassengerMapper;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

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
 * <li>Date : 2024/8/5 上午6:21</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024，. All rights reserved.
 * @Author 16867.
 */
@DubboService(version = "1.0.0", token = "true")
@Service
public class PassengerServiceImpl implements PassengerService {
    @Autowired
    private PassengerMapper passengerMapper;
    @Autowired
    private IdStrUtils idStrUtils;

    @Override
    public void save(PassengerSaveVo passengerSaveVo) {
        // 根据身份证号和会员id查重
        String idCard = passengerSaveVo.getIdCard();
        Long currentId = ThreadLocalUtils.getCurrentId();
        PassengerExample passengerExample = new PassengerExample();
        passengerExample.createCriteria().andIdCardEqualTo(idCard);
        passengerExample.createCriteria().andMemberIdEqualTo(currentId);
        List<Passenger> passengers = passengerMapper.selectByExample(passengerExample);

        if (!passengers.isEmpty()) {
            throw new BusinessException(ResultStatusEnum.CODE_601.getCode(), "乘车人信息已存在该会员账号，请勿重复添加");
        }

        Passenger passenger = BeanUtil.copyProperties(passengerSaveVo, Passenger.class);
        passenger.setMemberId(currentId);
        passenger.setId(idStrUtils.snowFlakeLong());
        Date date = new Date();
        passenger.setCreateTime(date);
        passenger.setUpdateTime(date);
        passengerMapper.insert(passenger);
    }

    /**
     * 条件分页查询
     *
     * @param passengerListVo
     * @return
     */
    @Override
    public List<Passenger> listByCondition(PassengerListVo passengerListVo) {
        PassengerExample passengerExample = new PassengerExample();
        if (!passengerListVo.getName().isEmpty()) {
            passengerExample.createCriteria().andNameLike("%" + passengerListVo.getName() + "%");
            passengerExample.setOrderByClause("create_time");
        }
        PageHelper.startPage(passengerListVo.getPage(), passengerListVo.getSize());
        List<Passenger> passengers = passengerMapper.selectByExample(passengerExample);
        passengerExample.clear();
        return passengers;
    }

    @Override
    public void deleteByIds(List<Long> passengerList) {
        passengerMapper.deleteBatch(passengerList);
    }

    @Override
    public Integer listCount(PassengerListVo passengerListVo) {
        PassengerExample passengerExample = new PassengerExample();
        passengerExample.clear();
        passengerExample.createCriteria().andNameLike("%" + passengerListVo.getName() + "%");
        passengerExample.clear();
        return Math.toIntExact(passengerMapper.countByExample(passengerExample));
    }
}
