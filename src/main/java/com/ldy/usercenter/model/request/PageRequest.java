package com.ldy.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用分页请求参数
 */
@Data
public class PageRequest implements Serializable {

    private static final long serialVersionUID = -6756152083997261741L;

    /**
     * 分页大小
     */
    protected int pageSize;

    /**
     * 当前是第几页
     */
    protected int pageNum;

}
