package com.ldy.usercenter.service;

import com.ldy.usercenter.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ldy.usercenter.model.domain.User;
import com.ldy.usercenter.model.dto.TeamQuery;
import com.ldy.usercenter.model.vo.TeamUserVO;

import java.util.List;

/**
* @author LDY
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2023-01-19 23:22:32
*/
public interface TeamService extends IService<Team> {

    /**
     * 创建队伍
     *
     * @param team
     * @param loginUser
     * @return
     */
    long addTeam(Team team, User loginUser);

    /**
     * 搜索队伍
     *
     * @param teamQuery
     * @return
     */
    List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin);
}
