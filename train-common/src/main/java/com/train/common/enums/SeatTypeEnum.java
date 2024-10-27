package com.train.common.enums;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 座位类型枚举类</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/10/9 下午11:07</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024，. All rights reserved.
 * @Author cqy.
 */
public enum SeatTypeEnum {
    YDZ("1", "一等座", new BigDecimal("0.4")),
    EDZ("2", "二等座", new BigDecimal("0.3")),
    RWZ("3", "软卧", new BigDecimal("0.7")),
    YWZ("4", "硬卧", new BigDecimal("0.5"));

    private String code; // 座位类型编码
    private String desc; // 座位类型注释
    private BigDecimal price; // 座位类型对应每公里价格

    SeatTypeEnum() {
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    SeatTypeEnum(String code, String desc, BigDecimal price) {
        this.code = code;
        this.desc = desc;
        this.price = price;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public BigDecimal getPrice() {
        return price;
    }


    @Override
    public String toString() {
        return "SeatTypeEnum{" +
                "code='" + code + '\'' +
                ", desc='" + desc + '\'' +
                ", price=" + price +
                '}';
    }

    public static List<SeatColumnEnum> getColumnsBySeatType(String type){
        List<SeatColumnEnum> result = new ArrayList<>();
        if(type.isEmpty()){
            return result;
        }
        SeatColumnEnum[] enums = SeatColumnEnum.values();
        for (SeatColumnEnum anEnum : enums) {
            if(anEnum.getType().equals(type)){
                result.add(anEnum);
            }
        }
        return result;
    }
    public static SeatTypeEnum getSeatTypeEnumByCode(String type){
        for(SeatTypeEnum en : SeatTypeEnum.values()){
            if(en.getCode().equals(type)){
                return en;
            }
        }
        return null;
    }
    public static void main(String[] args){
        System.out.println(SeatTypeEnum.getColumnsBySeatType(SeatTypeEnum.RWZ.getCode()));
    }
}
