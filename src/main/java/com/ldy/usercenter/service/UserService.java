package com.ldy.usercenter.service;

import com.ldy.usercenter.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.ldy.usercenter.contant.UserConstant.ADMIN_ROLE;
import static com.ldy.usercenter.contant.UserConstant.SUER_LOGIN_STATE;

/**
 * @author LDY
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2022-03-27 00:43:01
 */
public interface UserService extends IService<User> {


    /**
     * 用户注册
     *
     * @param userAccount   用户账号
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户id
     */
    public long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账号
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    User getSafetyUser(User originUser);

    /**
     * 用户注销
     * @param request
     * @return
     */
    int userLogout(HttpServletRequest request);

    /**
     * 根据标签搜索用户
     *
     * @param tagList 用户要拥有的标签
     * @return
     */
    List<User> searchUsersByTags(List<String> tagList);

    /**
     * 修改用户数据
     *
     * @param user
     * @param loginUser
     * @return
     */
    int updateUser(User user,User loginUser);

    /**
     * 当前登入用户信息
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param loginUser
     * @return
     */
    boolean isAdmin(User loginUser);



}
