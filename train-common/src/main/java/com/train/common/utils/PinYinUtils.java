package com.train.common.utils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

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
 * <li>Date : 2025/4/23 下午12:56</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2025，. All rights reserved.
 * @Author cqy.
 */
public class PinYinUtils {

        public static String toPinyin(String chinese) {
            HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
            format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
            format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);

            StringBuilder pinyin = new StringBuilder();
            char[] chars = chinese.toCharArray();
            for (char c : chars) {
                if (Character.toString(c).matches("[\\u4E00-\\u9FA5]+")) {
                    try {
                        String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, format);
                        if (pinyinArray != null) {
                            pinyin.append(pinyinArray[0]);
                        }
                    } catch (BadHanyuPinyinOutputFormatCombination e) {
                        e.printStackTrace();
                    }
                } else {
                    pinyin.append(c);
                }
            }
            return pinyin.toString();
        }
    public static void main(String[] args){
        System.out.println(PinYinUtils.toPinyin("上海虹桥"));
    }
}
