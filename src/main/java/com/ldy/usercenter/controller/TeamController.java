package com.ldy.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ldy.usercenter.common.BaseResponse;
import com.ldy.usercenter.common.ErrorCode;
import com.ldy.usercenter.common.ResulUtils;
import com.ldy.usercenter.exception.BusinessException;
import com.ldy.usercenter.model.domain.Team;
import com.ldy.usercenter.model.dto.TeamQuery;
import com.ldy.usercenter.model.request.PageRequest;
import com.ldy.usercenter.model.request.TeamAddRequest;
import com.ldy.usercenter.model.vo.TeamUserVO;
import com.ldy.usercenter.service.TeamService;
import com.ldy.usercenter.service.UserService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Api(tags = "队伍模板")
@RequestMapping("/team")
@RestController
@Slf4j
public class TeamController {

    @Resource
    private TeamService teamService;

    @Resource
    private UserService userService;

    @PostMapping("/add")
    public BaseResponse<Long> add(@RequestBody TeamAddRequest addRequest){
        if (addRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(team, addRequest);
        boolean save = teamService.save(team);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "加入失败");
        }
        return ResulUtils.success(team.getId());
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> delete(@RequestBody long id){
        if (id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean remove = teamService.removeById(id);
        if (!remove) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "退出失败");
        }
        return ResulUtils.success(true);
    }

    @PostMapping("/update")
    public BaseResponse<Team> update(@RequestBody Team team){
        if (team == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = teamService.updateById(team);
        if (!b) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "修改失败");
        }
        return ResulUtils.success(team);
    }

    @GetMapping("/get")
    public BaseResponse<Team> get(long id) {
        if (id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team byId = teamService.getById(id);
        if (byId == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "查看失败");
        }
        return ResulUtils.success(byId);
    }

    @GetMapping("/list")
    public BaseResponse<List<TeamUserVO>> list(TeamQuery teamQuery, HttpServletRequest request){
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean isAdmin = userService.isAdmin(request);
        List<TeamUserVO> list = teamService.listTeams(teamQuery, isAdmin);
        if (list == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"查看失败");
        }
        return ResulUtils.success(list);
    }

    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listTeamByPage(TeamQuery teamQuery, PageRequest pageRequest){
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamQuery, team);
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        Page<Team> resultPage = teamService.page(new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize()), queryWrapper);
        if (resultPage == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"查看失败");
        }
        return ResulUtils.success(resultPage);
    }
}
