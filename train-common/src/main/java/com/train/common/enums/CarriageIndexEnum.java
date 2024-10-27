package com.train.common.enums;

import lombok.Data;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 车厢序号枚举类</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/10/17 下午8:14</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024，. All rights reserved.
 * @Author cqy.
 */
public enum CarriageIndexEnum {
    INDEX_1(1, "一号车厢"),
    INDEX_2(2, "二号车厢"),
    INDEX_3(3, "三号车厢"),
    INDEX_4(4, "四号车厢"),
    INDEX_5(5, "五号车厢"),
    INDEX_6(6, "六号车厢"),
    INDEX_7(7, "七号车厢"),
    INDEX_8(8, "八号车厢");
    private Integer index;
    private String  desc;

    CarriageIndexEnum(Integer index, String desc) {
        this.index = index;
        this.desc = desc;
    }

    public Integer getIndex() {
        return index;
    }

    public String getDesc() {
        return desc;
    }
}
