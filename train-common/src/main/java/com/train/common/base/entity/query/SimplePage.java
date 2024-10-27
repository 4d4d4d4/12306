package com.train.common.base.entity.query;

import lombok.Data;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 分页查询基础类</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/10/11 下午10:38</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024，. All rights reserved.
 * @Author cqy.
 */
public class SimplePage {
    public final static Integer PAGE_20 = 20;
    private Integer totalCount; // 全部数据的总大小
    private Integer currentPage; // 当前所在界面
    private Integer pageSize; // 当前页面大小
    private Integer pageTotal; // 总页数
    private Integer start; // 数据开始索引
    private Integer end; // 数据结束索引



    public Integer getTotalCount() {
        return totalCount;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public Integer getPageTotal() {
        return pageTotal;
    }

    public Integer getStart() {
        return start;
    }

    public Integer getEnd() {
        return end;
    }

    public SimplePage(Integer totalCount, Integer currentPage, Integer pageSize) {
        this.totalCount = totalCount;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        action();

    }

    public SimplePage(Integer totalCount, Integer currentPage, Integer pageSize, Integer pageTotal) {
        this.totalCount = totalCount;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.pageTotal = pageTotal;
        action();
    }
    private void action(){
        // 检查每页数据大小定义
        if(this.pageSize == null || this.pageSize < 5){
            this.pageSize = SimplePage.PAGE_20;
        }
        // 计算总页数
        if(this.totalCount == null || this.totalCount < 1){
            this.totalCount = 1;
        }
        this.pageTotal = (this.totalCount % this.pageSize == 0) ?  (this.totalCount / this.pageSize) : (this.totalCount / this.pageSize + 1);

        // 检查当前页码
        if(this.currentPage == null || this.currentPage < 1){
            currentPage = 1;
        }
        if(this.currentPage > pageTotal){
            this.currentPage = pageTotal;
        }
        // 计算开始和结束索引
        this.start =  (this.currentPage -1) * this.pageSize;
        this.end  = this.pageSize; // mysql在数据量超过查询结果的数据量时会自动处理多余数据

    }
}
