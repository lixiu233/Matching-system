package com.ldy.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求体
 *
 * @author LDY
 */

@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = -7341040489018200099L;

    private String userAccount;

    private String userPassword;

    private String checkPassword;
}
