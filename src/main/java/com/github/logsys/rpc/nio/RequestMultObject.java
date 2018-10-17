package com.github.logsys.rpc.nio;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by liuchunlong on 2018/10/17.
 */
@Data
public class RequestMultObject implements Serializable {

    private static final long serialVersionUID = 3132836600205356306L;

    public RequestMultObject(Class<?> calzz, String methodName, Class<?>[] paramTypes, Object[] args) {
        this.calzz = calzz;
        this.methodName = methodName;
        this.paramTypes = paramTypes;
        this.args = args;
    }

    /**
     * 请求ID
     */
    private Long requestId;

    /**
     * 服务提供者接口
     */
    private Class<?> calzz;

    /**
     * 请求的方法名称
     */
    private String methodName;

    /**
     * 参数类型
     */
    private Class<?>[] paramTypes;

    /**
     * 参数
     */
    private Object[] args;
}
