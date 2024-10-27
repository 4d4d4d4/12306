package com.train.common.base.entity.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List; /**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 分页数据返回类</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/10/11 下午10:50</li>
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
public  class  PaginationResultVo<T> implements Serializable {
    private Integer pageSize; // 分页大小
    private Integer currentPage; // 当前页
    private Integer pageCount; // 页数总量
    private Integer dataCount; // 数据总量
    private List<T> result; // 返回结果集
}
