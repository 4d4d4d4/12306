package com.train.common.utils;

import com.train.common.resp.enmus.ResultStatusEnum;
import com.train.common.resp.exception.BusinessException;
import com.train.common.entity.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.util.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : http远程请求工具</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : 1.0.0</li>
 * <li>Date : 2024/7/23 下午12:33</li>
 * <li>Author : 16867</li>
 * <li>History : http</li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024, . All rights reserved.
 * @Author 16867.
 */
public class HttpClientUtils {

    private static final Logger log = LoggerFactory.getLogger(HttpClientUtils.class);

    public static HttpResponse httpGet(String scheme, String host, String path, Map<String, String> params) {
        DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory();
        UriBuilder builder = uriBuilderFactory.builder();

        builder.scheme(scheme);

        builder.host(host);

        builder.path(path);

        LinkedMultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            multiValueMap.add(entry.getKey(), entry.getValue());
        }
        builder.queryParams(multiValueMap);
        URI build = builder.build();
        try (final CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(build);
            HttpResponse result = httpClient.execute(httpGet, response -> {
                InputStream content = response.getEntity().getContent();
                byte[] bytes = new byte[256];
                int index = -1;
                ByteArrayOutputStream outputStream = (new ByteArrayOutputStream());
                while ((index = content.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, index);
                }
                String string = outputStream.toString();
                return new HttpResponse(response.getStatusLine().getStatusCode(), string);
            });
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            log.info("get请求发送失败，url:{},异常:{}", scheme + "//" + host + path, e.getMessage());

            throw new BusinessException(ResultStatusEnum.CODE_500.getCode(), "服务器网络异常");
        }
    }


    public static HttpResponse httpPost(String url, String path, Map<String, String> params) {
        ArrayList<NameValuePair> basicNameValuePairList = new ArrayList<>();
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(url);
        uriComponentsBuilder.path(path);
        for (Map.Entry<String, String> entry : params.entrySet()) {
            BasicNameValuePair basicNameValuePair = new BasicNameValuePair(entry.getKey(), (String) entry.getValue());
            basicNameValuePairList.add(basicNameValuePair);
        }
        UriComponents build = uriComponentsBuilder.build();

        try (final CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(build.toUri());
            UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(basicNameValuePairList, StandardCharsets.UTF_8);
            httpPost.setEntity(urlEncodedFormEntity);


            HttpResponse result = httpClient.execute(httpPost, response -> {
                InputStream content = response.getEntity().getContent();
                byte[] bytes = new byte[256];
                int index = -1;
                ByteArrayOutputStream outputStream = (new ByteArrayOutputStream());
                while ((index = content.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, index);
                }
                String string = outputStream.toString();
                return new HttpResponse(response.getStatusLine().getStatusCode(), string);
            });
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
