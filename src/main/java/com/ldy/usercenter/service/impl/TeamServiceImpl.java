package com.ldy.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ldy.usercenter.model.domain.Team;
import com.ldy.usercenter.service.TeamService;
import com.ldy.usercenter.mapper.TeamMapper;
import org.springframework.stereotype.Service;

/**
* @author LDY
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2023-01-19 23:22:32
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{

}




