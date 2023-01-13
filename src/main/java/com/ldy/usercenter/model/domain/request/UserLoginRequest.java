package com.ldy.usercenter.model.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求体
 *
 * @author LDY
 */
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = -7341040489018200099L;

    private String userAccount;

    private String userPassword;
}
