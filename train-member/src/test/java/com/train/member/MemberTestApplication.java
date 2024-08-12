package com.train.member;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.train.common.base.entity.query.MemberExample;
import com.train.common.base.entity.domain.Member;
import com.train.common.utils.JwtUtil;
import com.train.member.mapper.MemberMapper;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

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
 * <li>Date : 2024/7/23 上午9:20</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024, . All rights reserved.
 * @Author 16867.
 */
@SpringBootTest
public class MemberTestApplication {
    @Autowired
    private MemberMapper memberMapper;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void test() throws JsonProcessingException {
//        MemberExample memberExample = new MemberExample();
//        memberExample.createCriteria().andMobileEqualTo("15132769045");
//        List<Member> members = memberMapper.selectByExample(memberExample);
//        System.out.println(members.get(0));
        String test = "PassengerSaveVo(memberId=12312, name=test, idCard=130925200104151447, type=1)";

        JsonNode jsonNode = objectMapper.readTree(test);
        Iterator<String> stringIterator = jsonNode.fieldNames();
        List<JsonNode> list = new ArrayList<>();
        while (stringIterator.hasNext()) {
            String next = stringIterator.next();
            JsonNode jsonNode1 = jsonNode.get(next);

            list.add(jsonNode1);
        }
        String argumentsJson = objectMapper.writeValueAsString(test);
    }

    @Test
    public void test2() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("memberId", 1816734016573935616L);
        String jwt = JwtUtil.createJWT(map);
        System.out.println(jwt);
        Claims claims = JwtUtil.parseJWT(jwt);
        System.out.println(claims.get("memberId"));
    }
}
