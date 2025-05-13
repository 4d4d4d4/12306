package com.train.member.controller;

import com.train.common.aspect.annotation.GlobalAnnotation;
import com.train.common.base.entity.dto.MemberDto;
import com.train.common.base.entity.domain.Member;
import com.train.common.base.entity.query.MemberExample;
import com.train.common.base.entity.vo.MemberLoginVo;
import com.train.common.base.service.MemberService;
import com.train.common.entity.RedisMobileSms;
import com.train.common.entity.req.SenderTencentSms;
import com.train.common.enums.RedisEnums;
import com.train.common.resp.Result;
import com.train.common.resp.enmus.ResultStatusEnum;
import com.train.common.resp.exception.BusinessException;
import com.train.common.entity.CreateImageCode;
import com.train.common.utils.IdStrUtils;
import com.train.common.utils.RedisUtils;
import com.train.common.utils.ThreadLocalUtils;
import com.train.member.utils.WeatherResult;
import com.train.member.utils.WeatherUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Classname MemberController
 * @Description 什么也没有写哦~
 * @Date 2024/7/17 下午2:41
 * @Created by 憧憬
 */
@RestController
@RequestMapping("member")
public class MemberController {
    private static final Logger log = LoggerFactory.getLogger(MemberController.class);
    @Autowired
    private MemberService memberService;

    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private SenderTencentSms senderTencentSms;

    @RequestMapping("/count")
    public int count() {
        return memberService.count();
    }

    @RequestMapping("/register")
    public Long register(@Validated @RequestBody Member member) {
        return memberService.register(member);
    }

    /**
     * 生成图片验证码
     *
     * @param response
     * @param type     验证码类型 用于 0.登录/注册
     */
    @RequestMapping(value = "/checkCode", produces = {"image/jpeg"})
    public void checkCode(HttpServletResponse response, HttpSession session, String type) {
        CreateImageCode createImageCode = new CreateImageCode(130, 30, 5, 10);
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/jpeg");

        String code = createImageCode.getCode();
        String sessionId = session.getId();
        try {
            redisUtils.setEx(RedisEnums.CHECK_CODE_ENUM.getPrefix() + sessionId + type, code, RedisEnums.CHECK_CODE_ENUM.getTime() * 10);
            createImageCode.write(response.getOutputStream());
        } catch (IOException e) {
            log.info("验证码写入失败：{}", e.getMessage());
            throw new BusinessException(ResultStatusEnum.CODE_500.getCode(), "验证码异常，请联系管理员");
        }

    }

    // 发送手机验证码
    @PostMapping("/mobileSms")
    public Result mobileSmsCode(@RequestBody Member member) {
        String mobile = member.getMobile();
        if (mobile == null || mobile.isEmpty()) {
            throw new BusinessException(ResultStatusEnum.CODE_501);
        }
        // 尝试获取之前发送的验证码
        String key = RedisEnums.MOBILE_SMS_ENUM.getPrefix() + mobile;
        RedisMobileSms redisMobileSms = (RedisMobileSms) redisUtils.get(key);
        String mobileCode = IdStrUtils.checkIntegerCode();

        if (redisMobileSms != null) {
            if (!redisMobileSms.getMobile().equals(mobile)) { // 如果获取的手机号验证码不是自己的手机验证码则删除
                redisUtils.remove(key);
                throw new BusinessException(ResultStatusEnum.CODE_502);
            }
            // 剩余过期时间 单位是秒
            long ttl = redisUtils.getTTL(key);
            // 如果十分钟内发送多于3次短信则禁止
            if (redisMobileSms.getCount() > 3) {
                throw new BusinessException(ResultStatusEnum.CODE_503.getCode(), ResultStatusEnum.CODE_503.getDescription().replace("{}", (ttl / 60) + ""));
            }
            List<String> params = new ArrayList<>();
            params.add(mobileCode);
            // 发送成功后保存至缓存中
            redisMobileSms.setCode(mobileCode);
            redisMobileSms.setCount(redisMobileSms.getCount() + 1);
            boolean b = redisUtils.setEx(key, redisMobileSms, ttl);
            if (b) {
                // 测试阶段不发送短信
                senderTencentSms.sendMobileMessage(mobile, "", params);
            }
        } else {
            // 第一次发送验证码
            List<String> params = new ArrayList<>();
            params.add(mobileCode);
            senderTencentSms.sendMobileMessage(mobile, "", params);
            // 发送成功后保存至缓存中
            redisMobileSms = new RedisMobileSms();
            redisMobileSms.setMobile(mobile);
            redisMobileSms.setCount(1);
            redisMobileSms.setCode(mobileCode);
            redisUtils.setEx(key, redisMobileSms, RedisEnums.MOBILE_SMS_ENUM.getTime() * 10); // 十分钟过期时间
        }
        return Result.ok();
    }

    // 登录接口
    @PostMapping("/login")
    public Result memberLogin(HttpSession session, @Validated @RequestBody MemberLoginVo login) {
        String type = login.getType();
        if (type == null || !type.equals("0")) {
            throw new BusinessException(ResultStatusEnum.CODE_504);
        }

        String mobile = login.getMobile();
        String checkCode = login.getCheckCode();
        String mobileSms = login.getMobileSms();
        String sessionId = session.getId();
        String redisCheckCode = (String) redisUtils.get(RedisEnums.CHECK_CODE_ENUM.getPrefix() + sessionId + type);
        // 删除使用过的验证码
        redisUtils.remove(RedisEnums.CHECK_CODE_ENUM.getPrefix() + sessionId + type);
        if ((redisCheckCode == null) || !redisCheckCode.equalsIgnoreCase(checkCode)) {
            throw new BusinessException(ResultStatusEnum.CODE_505);
        }
//     TODO      由于手机短信验证码是收费的，所以非正式上线前省略掉(已完成测试)
//        checkMobileCode(mobile, mobileSms);

        MemberDto result = memberService.registerOrLoginMember(mobile);

        return Result.ok().data("data", result);
    }

    private void checkMobileCode(String mobile, String mobileSms) {
        String key = RedisEnums.MOBILE_SMS_ENUM.getPrefix() + mobile;
        RedisMobileSms redisMobileSms = (RedisMobileSms) redisUtils.get(key);
        if(redisMobileSms == null){
            throw new BusinessException(ResultStatusEnum.CODE_506);
        }
        String redisMobile = redisMobileSms.getMobile();
        // 手机号获取到的不是自己对应的缓存
        if(redisMobile == null || !redisMobile.equals(mobile)){
            redisUtils.remove(key);
            throw new BusinessException(ResultStatusEnum.CODE_506);
        }
        String redisMobileSmsCode = redisMobileSms.getCode();
        if(redisMobileSmsCode == null || !redisMobileSmsCode.equalsIgnoreCase(mobileSms)){
            throw new BusinessException(ResultStatusEnum.CODE_507);
        }
    }

    @PostMapping("/selectById")
    @GlobalAnnotation(checkLogin = true)
    public Result selectById(){
        Long currentId = ThreadLocalUtils.getCurrentId();
        MemberExample memberExample = new MemberExample();
        memberExample.createCriteria().andIdEqualTo(currentId);
        List<Member> members = memberService.selectMemberList(memberExample);
        return Result.ok().data("data", members.get(0));
    }
    @PostMapping("/getWeatherByCity")
    public Result getWeatherByCity(@RequestBody Map<String,String> map){
        String city = map.get("city");
        Map<String, WeatherResult> weather = WeatherUtils.getWeather(city);
        return Result.ok().data("data", weather);
    }
}
