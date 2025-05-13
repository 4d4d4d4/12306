package com.train.common.entity.req;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.sms.v20210111.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.SendSmsResponse;
import com.tencentcloudapi.sms.v20210111.models.SendStatus;
import com.train.common.entity.HttpResponse;
import com.train.common.entity.config.TencentConfig;
import com.train.common.resp.enmus.ResultStatusEnum;
import com.train.common.resp.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 发送腾讯云短信类</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/7/23 下午3:38</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024, . All rights reserved.
 * @Author 16867.
 */
@Component
public class SenderTencentSms {
    private static final Logger log = LoggerFactory.getLogger(SenderTencentSms.class);
    @Autowired
    private TencentConfig tencentConfig;

    private Credential credential; // 身份认证

    private HttpProfile httpProfile; // http选项

    private SmsClient smsClient; // 短信产品实例

    private ClientProfile clientProfile; // 客户端配置对象

    private SendSmsRequest req; // 请求对象

    @PostConstruct
    public void init() {
        credential = new Credential(tencentConfig.getSecretId(), tencentConfig.getSecretKey());

        httpProfile = new HttpProfile();
        httpProfile.setReqMethod("POST");
        httpProfile.setConnTimeout(20); // 超时时间
        httpProfile.setWriteTimeout(10);
        httpProfile.setReadTimeout(10);

        httpProfile.setEndpoint("sms.tencentcloudapi.com");

        clientProfile = new ClientProfile();
        clientProfile.setSignMethod("HmacSHA256");
        clientProfile.setHttpProfile(httpProfile);

        smsClient = new SmsClient(credential, "ap-beijing", clientProfile);

        req = new SendSmsRequest();
        req.setSmsSdkAppId(tencentConfig.getSmsSdkAppId());
        req.setSignName(tencentConfig.getSignName());
        req.setTemplateId(tencentConfig.getTemplateId());
        req.setExtendCode("");
        req.setSenderId("");
    }


    public HttpResponse sendMobileMessage(String mobile, String userSession, List<String> params) {
        HttpResponse httpResponse = new HttpResponse();
        String[] templateParams = new String[params.size()];
        params.toArray(templateParams);
        log.info("手机验证码参数:{}", params);
        req.setPhoneNumberSet(new String[]{mobile});
        req.setTemplateParamSet(templateParams);
        try {
            SendSmsResponse sendSmsResponse = smsClient.SendSms(req);
            httpResponse.setCode(200); // 腾讯云的默认返回值都是200
            httpResponse.setJson(SendSmsResponse.toJsonString(sendSmsResponse)); // 具体的成功失败体现在这里
            SendStatus[] sendStatusSet = sendSmsResponse.getSendStatusSet();
            for (SendStatus sendStatus : sendStatusSet) {
                if (!Objects.equals(sendStatus.getCode(), "Ok")) {
                    log.info("手机号为：{}的短信发送失败, 错误消息：{}", mobile, sendStatus.getMessage());
                } else {
                    log.info("手机号为：{}的短信发送成功", mobile);
                }
            }
        } catch (TencentCloudSDKException e) {
            log.info("手机验证码发送失败,手机号：{},异常信息:{}", mobile, e.toString());
            throw new BusinessException(ResultStatusEnum.CODE_500.getCode(), "手机验证码发送失败");
        }
        return httpResponse;
    }
}
