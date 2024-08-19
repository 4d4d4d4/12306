package com.train.common.resp;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 分页返回类</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/8/12 下午4:50</li>
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
public  class  Pagination<T> implements Serializable {
    private List<T> data; // 所有数据
    private Integer current; // 当前页
    private Integer total; // 总数据量
    private Integer size; // 每页大小
}
