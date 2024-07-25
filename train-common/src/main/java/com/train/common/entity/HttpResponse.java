package com.train.common.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : httpClient接受类</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : 1.0.0</li>
 * <li>Date : 2024/7/23 下午12:44</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024, . All rights reserved.
 * @Author 16867.
 */
@Data
public class HttpResponse {
    public HttpResponse() {
    }

    public HttpResponse(Integer code, String json) {
        this.code = code;
        this.json = json;
    }

    private Integer code;
    private String json;
}
