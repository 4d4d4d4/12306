package com.train.business;

import com.train.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
 * <li>Date : 2024/10/9 上午12:37</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024，. All rights reserved.
 * @Author cqy.
 */
@SpringBootTest
public class BusinessTestApplication {
    @Autowired
    private JwtUtil jwtUtil;
    @Test
    public void test(){
        System.out.println(jwtUtil); // com.train.common.config.JwtUtil@6011176f
        // eyJhbGciOiJkaXIiLCJlbmMiOiJBMTI4Q0JDLUhTMjU2In0..Zi5Pd3LX5j0YqC7sSB-oNg.O_HXT2OyKjQgDtze5SjtV3lzq8yiCbytPz4HzC_MBk_KhfgG7PvWWw5OA1VKa_JOZ6fI8H_Nq_7TJldgfunwuXbjI0Bc0mTR5BuSg1xHTwQ.4hrMKCRXXiXuMIpvIQcgRQ
        Claims claims = jwtUtil.parseJWT("eyJhbGciOiJkaXIiLCJlbmMiOiJBMTI4Q0JDLUhTMjU2In0..Zi5Pd3LX5j0YqC7sSB-oNg.O_HXT2OyKjQgDtze5SjtV3lzq8yiCbytPz4HzC_MBk_KhfgG7PvWWw5OA1VKa_JOZ6fI8H_Nq_7TJldgfunwuXbjI0Bc0mTR5BuSg1xHTwQ.4hrMKCRXXiXuMIpvIQcgRQ");
        System.out.println(claims.get("id"));

    }
}
