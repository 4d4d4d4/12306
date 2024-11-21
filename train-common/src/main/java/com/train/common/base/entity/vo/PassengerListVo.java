package com.train.common.base.entity.vo;

import com.train.common.base.entity.query.BasePageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 乘车人列表查询条件接受类</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/8/12 上午10:28</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024，. All rights reserved.
 * @Author cqy.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PassengerListVo extends BasePageQuery {
    private String name;
    private Long memberId;
}
