package com.ldy.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 队伍修改请求体
 *
 * @author LDY
 */
@Data
public class TeamJoinRequest implements Serializable {

    private static final long serialVersionUID = -7341040489018200099L;

    /**
     * id
     */
    private Long id;

    /**
     * 密码
     */
    private String password;
}
