package com.kp.mall.module.integral_record.controller;

import cn.hutool.core.util.StrUtil;
import com.kp.mall.common.Result;
import com.kp.mall.module.integral_record.entity.IntegralRecord;
import com.kp.mall.module.integral_record.service.IntegralRecordService;
import com.kp.mall.module.user.entity.User;
import com.kp.mall.module.user.service.UserService;
import com.kp.mall.util.JwtUtil;
import com.kp.mall.vo.UserLoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 用户端积分接口：/user/integral
 * 我的积分记录、当前积分余额
 */
@RestController
@RequestMapping("/user/integral")
public class UserIntegralController {

    @Autowired
    private IntegralRecordService integralRecordService;
    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil<UserLoginUser> jwtUtil;

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (StrUtil.isEmpty(authorization)) return null;
        if (!jwtUtil.verifyToken(authorization)) return null;
        UserLoginUser u = jwtUtil.parseToken(authorization, UserLoginUser.class);
        return u != null ? u.getId() : null;
    }

    /**
     * 我的积分记录（分页）
     * 请求体：{ "current": 1, "size": 10, "integralType": null, "integralSource": null }
     */
    @PostMapping("/record")
    public Result record(@RequestBody(required = false) Map<String, Object> params, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        IntegralRecord entity = new IntegralRecord();
        entity.setUserId(userId);
        Integer current = 1;
        Integer size = 10;
        if (params != null) {
            if (params.get("current") != null) current = Integer.valueOf(params.get("current").toString());
            if (params.get("size") != null) size = Integer.valueOf(params.get("size").toString());
            if (params.get("integralType") != null) entity.setIntegralType(Integer.valueOf(params.get("integralType").toString()));
            if (params.get("integralSource") != null) entity.setIntegralSource(Integer.valueOf(params.get("integralSource").toString()));
        }
        return Result.success(integralRecordService.pageQuery(current, size, entity));
    }

    /**
     * 当前用户积分信息（总积分等）
     * 返回：{ totalIntegral: number }
     */
    @GetMapping("/info")
    public Result info(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(new java.util.HashMap<String, Object>() {{
            put("totalIntegral", user.getTotalIntegral() != null ? user.getTotalIntegral() : 0);
        }});
    }
}
