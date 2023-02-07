package com.ldy.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ldy.usercenter.common.ErrorCode;
import com.ldy.usercenter.exception.BusinessException;
import com.ldy.usercenter.model.domain.Team;
import com.ldy.usercenter.model.domain.User;
import com.ldy.usercenter.model.domain.UserTeam;
import com.ldy.usercenter.model.dto.TeamQuery;
import com.ldy.usercenter.model.enums.TeamStatusEnum;
import com.ldy.usercenter.model.request.TeamJoinRequest;
import com.ldy.usercenter.model.request.TeamUpdateRequest;
import com.ldy.usercenter.model.vo.TeamUserVO;
import com.ldy.usercenter.model.vo.UserVO;
import com.ldy.usercenter.service.TeamService;
import com.ldy.usercenter.mapper.TeamMapper;
import com.ldy.usercenter.service.UserService;
import com.ldy.usercenter.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;

/**
* @author LDY
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2023-01-19 23:22:32
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, User loginUser) {
        //1. 请求参数是否为空？
        if (team == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        //2. 是否登录，未登录不允许创建

        //3. 校验信息
        //3.1. 队伍人数 > 1 且 <= 20
        if (team.getMaxNum()<1 && team.getMaxNum()>20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "");
        }
        //3.2. 队伍标题 <= 20
        String name = team.getName();
        if (StringUtils.isBlank(name) || name.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "");
        }
        //3.3. 描述 <= 512
        String description = team.getDescription();
        if (StringUtils.isBlank(description) || description.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "");
        }
        //3.4. status 是否公开（int）不传默认为 0（公开）
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        if (status < 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "");
        }
        //3.5. 如果 status 是加密状态，一定要有密码，且密码 <= 32
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (TeamStatusEnum.SECRET.equals(statusEnum)) {
            if (StringUtils.isBlank(team.getPassword()) || team.getPassword().length() > 32){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "");
            }
        }
        //3.6. 超时时间 > 当前时间
        Date expireTime = team.getExpireTime();
        if (new Date().after(expireTime)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "");
        }
        //3.7. 校验用户最多创建 5 个队伍
        // todo 校验有 buy ，可能存在同时创建100个队伍
        Long userId = loginUser.getId();
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId",userId);
        long count = this.count(queryWrapper);
        if (count > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "");
        }
        //4. 插入队伍信息到队伍表
        team.setId(null);
        team.setUserId(userId);
        boolean result = this.save(team);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "");
        }
        //5. 插入用户  => 队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "");
        }
        return team.getId();
    }

    @Override
    public List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        // 组合查询条件
        if (teamQuery != null) {
            Long id = teamQuery.getId();
            if (id != null && id > 0) {
                queryWrapper.eq("id", id);
            }
            String searchTest = teamQuery.getSearchTest();
            if (StringUtils.isBlank(searchTest)) {
                queryWrapper.and(qw -> qw.like("name", searchTest).or().like("description", searchTest));
            }
            String name = teamQuery.getName();
            if (StringUtils.isNotBlank(name)) {
                queryWrapper.like("name", name);
            }
            Integer maxNum = teamQuery.getMaxNum();
            if (maxNum != null && maxNum > 0) {
                queryWrapper.eq("maxNum", maxNum);
            }
            Long userId = teamQuery.getUserId();
            if (userId != null && userId > 0) {
                queryWrapper.eq("userId", userId);
            }
            String description = teamQuery.getDescription();
            if (StringUtils.isBlank(description)) {
                queryWrapper.like("description", description);
            }
            Integer status = teamQuery.getStatus();
            TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
            if (statusEnum == null) {
                statusEnum = TeamStatusEnum.PUBLIC;
            }
            if (!isAdmin && !statusEnum.equals(TeamStatusEnum.PUBLIC)) {
                throw new BusinessException(ErrorCode.NO_AUTO);
            }
            queryWrapper.eq("status", statusEnum.getValue());
        }
        // 不展示已过期的队伍
        // expireTime is null or expireTime > now()
        queryWrapper.and(qw -> qw.gt("expireTime", new Date()).or().isNull("expireTime"));
        List<Team> teamList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }
        List<TeamUserVO> teamUserVOList = new ArrayList<>();
        // 关键查询创建人的用户信息
        for (Team team : teamList) {
            Long userId = team.getUserId();
            if (userId == null) {
                continue;
            }
            User user = userService.getById(userId);
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team, teamUserVO);
            // 脱敏用户信息
            if (user != null) {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user, userVO);
                teamUserVO.setCreateUser(userVO);
            }
            teamUserVOList.add(teamUserVO);
        }
        return teamUserVOList;
    }

    @Override
    public boolean upDateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = teamUpdateRequest.getId();
        if (id == null && id < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team oldTeam = this.getById(id);
        if (oldTeam == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        //只有管理员或者队伍的创建者可以修改
        if (!oldTeam.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTO);
        }
        Integer status = teamUpdateRequest.getStatus();
        String password = teamUpdateRequest.getPassword();
        Team team = new Team();
        team.setName(teamUpdateRequest.getName());
        team.setDescription(teamUpdateRequest.getDescription());
        team.setExpireTime(teamUpdateRequest.getExpireTime());
        team.setStatus(status);
        team.setPassword(password);
        if (team.equals(oldTeam)) {
            return true;
        }
        TeamStatusEnum enumByValue = TeamStatusEnum.getEnumByValue(status);
        if (TeamStatusEnum.SECRET.equals(enumByValue)) {
            if (StringUtils.isBlank(password)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密房间必须设置密码");
            }
        }else {
            teamUpdateRequest.setPassword(null);
        }
        BeanUtils.copyProperties(teamUpdateRequest, oldTeam);
        return this.updateById(oldTeam);
    }

    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //todo 当前缺少加入队伍的用户是否存在
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user", teamJoinRequest.getId());
        long count = userTeamService.count(queryWrapper);
        if (count > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "你加入队伍过多!");
        }
        Long teamId = teamJoinRequest.getId();
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        // 队伍过期
        Date expireTime = team.getExpireTime();
        if (expireTime != null && expireTime.before(new Date())) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍已过期");
        }
        // 禁止加入私有队伍
        Integer status = team.getStatus();
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        if (TeamStatusEnum.PRIVATE.equals(teamStatusEnum)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "禁止加入私有队伍");
        }
        // 密码是否匹配
        String password = teamJoinRequest.getPassword();
        if (TeamStatusEnum.SECRET.equals(teamStatusEnum)) {
            if (password == null && !password.equals(team.getPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不对");
            }
        }
        // 不能重复的加入已经加入的队伍
        Long userId = loginUser.getId();
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.eq("teamId", teamId);
        long hasUserJoinTeam = userTeamService.count(queryWrapper);
        if (hasUserJoinTeam > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "你是这队伍里的成员，无须再加入");
        }
        // 加入队伍人数
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("TeamId", teamId);
        long userIdCount = userTeamService.count(queryWrapper);
        if (userIdCount > 5 && userIdCount < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍超过5个成员了！");
        }
        Team team1 = new Team();

        UserTeam userTeam = new UserTeam();
        userTeam.setTeamId(teamId);
        userTeam.setUserId(loginUser.getId());
        userTeam.setJoinTime(new Date());
        boolean save = userTeamService.save(userTeam);

        return true;
    }

    @Override
    public boolean quitTeam(Long teamId, User loginUser) {
        if (teamId == null && teamId >= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 队伍是否存在
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍不存在");
        }
        // 检验是否已经加入队伍
        QueryWrapper<UserTeam> userTeamQueryWrapper = new  QueryWrapper<>();
        userTeamQueryWrapper.eq("userId", loginUser.getId());
        userTeamQueryWrapper.eq("teamId", teamId);
        long count = userTeamService.count(userTeamQueryWrapper);
        if (count >= 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "已经在队伍");
        }
        // 队伍只剩一个人就解散
        userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        long userTeamNum = userTeamService.count(userTeamQueryWrapper);
        if (userTeamNum <= 1) {
            this.removeById(teamId);
        }else {
            // 自己是队长，队伍还有其他人就转移给其他人，保持先来后到原则
            if(loginUser.getId().equals(team.getUserId())) {
                // 查询当前队伍的所有用户的加入时间
                userTeamQueryWrapper = new QueryWrapper<>();
                userTeamQueryWrapper.eq("teamId", teamId);
                userTeamQueryWrapper.last("order by id asc limit 2");
                List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
                if (CollectionUtils.isEmpty(userTeamList) || userTeamList.size() <= 1) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
                UserTeam userTeam = userTeamList.get(1);
                // 更新当前队伍的队长
                team.setUserId(userTeam.getUserId());
                team.setUpdateTime(new Date());
                boolean result =  this.updateById(team);
                if (!result) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新队伍队长失败");
                }
            }
        }
        userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("userId", loginUser.getId());
        return userTeamService.remove(userTeamQueryWrapper);
    }
}












