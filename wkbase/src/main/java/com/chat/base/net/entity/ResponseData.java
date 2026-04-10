package com.chat.base.net.entity;

/**
 * 通用响应数据容器（带数据的响应）
 * @param <T> 数据类型
 */
public class ResponseData<T> {
    public int status;
    public String msg;
    public T data;
}
