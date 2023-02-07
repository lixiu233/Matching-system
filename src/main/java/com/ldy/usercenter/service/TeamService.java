package com.ldy.usercenter.service;

import com.ldy.usercenter.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ldy.usercenter.model.domain.User;
import com.ldy.usercenter.model.dto.TeamQuery;
import com.ldy.usercenter.model.request.TeamJoinRequest;
import com.ldy.usercenter.model.request.TeamUpdateRequest;
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
     * @param isAdmin
     * @return
     */
    List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin);

    /**
     * 更新队伍
     *
     * @param request
     * @param loginUser
     * @return
     */
    boolean upDateTeam(TeamUpdateRequest request, User loginUser);

    /**
     * 加入队伍
     *
     * @param request
     * @param loginUser
     * @return
     */
    boolean joinTeam(TeamJoinRequest request, User loginUser);

    /**
     * 退出队伍
     *
     * @param teamId
     * @return
     */
    boolean quitTeam(Long teamId, User loginUser);
}
