package com.train.common.base.entity.query;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

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
 * <li>Date : 2024/8/12 下午4:54</li>
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
public class BasePageQuery {
    @NotNull(message = "当前页不能为空")
    private Integer currentPage;
    @NotNull(message = "页大小不能为空")
    @Max(value = 20, message = "每页数据不得超过20条")
    private Integer pageSize;
}
