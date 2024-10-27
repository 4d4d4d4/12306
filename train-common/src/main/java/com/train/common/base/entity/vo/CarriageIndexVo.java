package com.train.common.base.entity.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 车厢序号前端返回类</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/10/17 下午8:10</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024，. All rights reserved.
 * @Author cqy.
 */
@Data
public class CarriageIndexVo implements Serializable {
    private Integer value; // 车序号
    private String label; // 注释
    private String seatType; // 座位类型

    private Integer rowCount;

    private Integer colCount;
    private boolean disable; // 数据库中是否存在
}
