package com.github.logsys.heartbeat;

import lombok.Data;

import java.io.Serializable;
import java.nio.channels.SelectionKey;
import java.util.List;

/**
 * Created by liuchunlong on 2018/10/14.
 */
@Data
public class Pojo implements Serializable {

    /**
     * 序列化
     */
    private static final long serialVersionUID = -8868529619983791261L;

    private String name;

    private int age;

    private List<String> likes;

    public Pojo(String name, int age, List<String> likes) {
        this.name = name;
        this.age = age;
        this.likes = likes;
    }
}
