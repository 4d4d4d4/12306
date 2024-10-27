package com.train.common.enums;

import com.alibaba.fastjson.JSON;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
     * <dd>本类用于 : 座位列枚举</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/10/16 上午12:02</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024，. All rights reserved.
 * @Author cqy.
 */
public enum SeatColumnEnum {
    // 一等座列号
    YDZ_A("A","一等座A列", "1"),
    YDZ_B("B","一等座B列", "1"),
    YDZ_C("C","一等座C列", "1"),
    YDZ_F("F","一等座F列", "1"),
    // 二等座列号
    EDZ_A("A","二等座A列", "2"),
    EDZ_B("B","二等座B列", "2"),
    EDZ_C("C","二等座C列", "2"),
    EDZ_D("D","二等座D列", "2"),
    EDZ_F("F","二等座F列", "2"),
    // 软卧列号
    RW_A("A", "软卧下铺", "3"),
    RW_B("B", "软卧上铺", "3"),
    // 硬卧列号
    YW_A("A", "硬卧下铺", "4"),
    YW_B("B", "硬卧中铺", "4"),
    YW_C("C", "硬卧上铺", "4");
    private String code; // 座位前缀(编号)
    private String desc; // 注释
    private String type; // 对应的车厢类型编码 SeatTypeEnum

    SeatColumnEnum(String code, String desc, String type) {
        this.code = code;
        this.desc = desc;
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return this.name() + ":{" +
                "code:'" + code + '\'' +
                ", desc:'" + desc + '\'' +
                ", type:'" + type + '\'' +
                '}' + ',';
    }

    public static void main(String[] args){
        SeatColumnEnum[] values = SeatColumnEnum.values();
        for(SeatColumnEnum value : values){
            System.out.println(value);
        }
    }
}
