package com.ldy.usercenter.once;

import com.alibaba.excel.EasyExcel;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ImportUser {
    public static void main(String[] args) {
        String fileName = "F:\\java_bian\\user-center\\src\\main\\resources\\userEx.xlsx";
        List<UserInfo> userInfosList = EasyExcel.read(fileName).head(UserInfo.class).sheet().doReadSync();
        System.out.println("总数 = " + userInfosList.size());
        Map<String, List<UserInfo>> stringListMap = userInfosList.stream()
                .filter(userInfo -> StringUtils.isNotEmpty(userInfo.getUsername()))
                .collect(Collectors.groupingBy(UserInfo::getUsername));
        System.out.println("不重复昵称数 = " + stringListMap.keySet().size());
    }
}
