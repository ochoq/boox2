package com.choqnet.budget.security;

import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityui.role.annotation.ScreenPolicy;

@ResourceRole(name = "AnonymousRole", code = AnonymousRole.CODE, scope = "UI")
public interface AnonymousRole {
    String CODE = "anonymous-role";
    @ScreenPolicy(screenIds = {"UserActivation","UserRegistration","ResetPassword","CapturePwd"})
    void screens();
}