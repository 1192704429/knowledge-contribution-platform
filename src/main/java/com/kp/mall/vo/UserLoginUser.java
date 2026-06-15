package com.kp.mall.vo;

import lombok.Data;

/**
 * @author wuming
 * @description: 用户登录信息VO
 */
@Data
public class UserLoginUser {
    private Long id;
    private String name;
    private String avatar;
    private String email;
    private String phone;
    private Integer totalIntegral;
    /** 今日是否已签到（仅 /user/info 返回时有效） */
    private Boolean signedToday;
}
