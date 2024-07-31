//package com.train.member;
//
//import com.train.common.tool.entity.config.TencentConfig;
//import com.train.common.tool.entity.req.SenderTencentSms;
//import com.train.common.tool.utils.IdStrUtils;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.util.ArrayList;
//
///**
// * <dl>
// * <dt><b>类功能概述</b></dt>
// * <dd>本类用于 : </dd>
// * </dl>
// * <dl>
// * <dt><b>版本历史</b></dt>
// * <dd>
// * <ul>
// * <li>Version : </li>
// * <li>Date : 2024/7/23 上午9:20</li>
// * <li>Author : 16867</li>
// * <li>History : </li>
// * </ul>
// * </dd>
// * </dl>
// *
// * @Copyright Copyright &copy; 2024, . All rights reserved.
// * @Author 16867.
// */
//@SpringBootTest
//public class MemberTestApplication {
//
//    @Autowired
//    private IdStrUtils idSnowFlakeUtils;
//
//    @Test
//    public void test() {
//        Long l = idSnowFlakeUtils.snowFlakeLong();
//        System.out.println(l);
//    }
//
//    @Autowired
//    private TencentConfig tencentConfig;
//
//    @Test
//    public void test2() {
//        System.out.println(tencentConfig.getSecretId());
//        System.out.println(tencentConfig.getSecretKey());
//        System.out.println(tencentConfig.getSignName());
//        System.out.println(tencentConfig.getSmsSdkAppId());
//        System.out.println(tencentConfig.getTemplateId());
//    }
//
//    @Autowired
//    private SenderTencentSms senderTencentSms;
//
//    @Test
//    public void test3() {
//        ArrayList<String> list = new ArrayList<>();
//        list.add(IdStrUtils.checkCodeUUId());
////        list.add("5");
//        System.out.println(senderTencentSms.sendMobileMessage("15511767539", "", list));
//    }
//}
