package com.ldy.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ldy.usercenter.common.BaseResponse;
import com.ldy.usercenter.common.ErrorCode;
import com.ldy.usercenter.common.ResulUtils;
import com.ldy.usercenter.contant.UserConstant;
import com.ldy.usercenter.exception.BusinessException;
import com.ldy.usercenter.model.domain.User;
import com.ldy.usercenter.model.domain.request.UserLoginRequest;
import com.ldy.usercenter.model.domain.request.UserRegisterRequest;
import com.ldy.usercenter.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

import static com.ldy.usercenter.contant.UserConstant.SUER_LOGIN_STATE;

/**
 * 用户接口
 *
 * @author LDY
 */
@Api(tags = "开始模块")
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @ApiOperation(value = "注册接口")
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        if (userRegisterRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount,userPassword,checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long userRegister = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResulUtils.success(userRegister);
    }

    @ApiOperation(value = "登入接口")
    @PostMapping("/login")
    public BaseResponse<User> userLongin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request, HttpServletResponse response){
        if (userLoginRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount,userPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User userLogin = userService.userLogin(userAccount, userPassword, request);
        return ResulUtils.success(userLogin);
    }

    @ApiOperation(value = "退出登入接口")
    @PostMapping("/logout")
    public Integer userLogout(HttpServletRequest request){
        if (request == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return userService.userLogout(request);
    }

    @ApiOperation(value = "当前状态接口")
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request){
        Object userObj = request.getSession().getAttribute(SUER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        long UserId = currentUser.getId();
        // TODO 校验用户是否合法
        User user = userService.getById(UserId);
        User safetyUser = userService.getSafetyUser(user);
        return ResulUtils.success(safetyUser);
    }

    @ApiOperation(value = "查询接口")
    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username,HttpServletRequest request){
        if (!userService.isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTO);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNoneBlank(username)){
            queryWrapper.eq("username",username);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> collect = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResulUtils.success(collect);
    }

    @ApiOperation(value = "查询所有用户接口")
    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers(long pageSize,long pageNum,HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        String redisKey = String.format("ldy:user:recommend:%s", loginUser.getId());
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        // 如果有缓存，直接使用缓存
        Page<User> userPage = (Page<User>) valueOperations.get(redisKey);
        if (userPage != null){
            return ResulUtils.success(userPage);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        Page<User> userIPage = userService.page(new Page<>(pageNum,pageSize),queryWrapper);
        try {
            valueOperations.set(redisKey, userIPage);
        } catch (Exception e) {
            log.error("redis set key error", e);
        }
        return ResulUtils.success(userIPage);
    }


    @ApiOperation(value = "查询标签接口")
    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUsersByTags(@RequestParam(required = false) List<String> tagNameList){
        if (CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"搜索数据为空");
        }
        List<User> userList = userService.searchUsersByTags(tagNameList);
        return ResulUtils.success(userList);
    }

    @ApiOperation(value = "删除接口")
    @PostMapping("delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id,HttpServletRequest request){
        if (!userService.isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTO);
        }
        if (id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(id);
        return ResulUtils.success(b);
    }

    @ApiOperation(value = "修改数据接口")
    @PostMapping("update")
    public BaseResponse<Integer> updateUser(@RequestBody User user,HttpServletRequest request){
        if (user == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null){
            return ResulUtils.error(ErrorCode.NOT_LOGIN);
        }
        int i = userService.updateUser(user, loginUser);
        return ResulUtils.success(i);
    }


}
