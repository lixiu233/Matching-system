package com.ldy.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ldy.usercenter.mapper.UserTeamMapper;
import com.ldy.usercenter.model.domain.UserTeam;
import com.ldy.usercenter.service.UserTeamService;
import org.springframework.stereotype.Service;

/**
* @author LDY
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2023-01-20 00:04:24
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService {

}




