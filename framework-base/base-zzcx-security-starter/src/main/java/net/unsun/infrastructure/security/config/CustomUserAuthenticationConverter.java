package net.unsun.infrastructure.security.config;

import net.unsun.infrastructure.security.base.UserDetail;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.provider.token.UserAuthenticationConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @program: unsun-framework
 * @author: Tokey
 * @create: 2019-11-28 11:12
 */
public class CustomUserAuthenticationConverter implements UserAuthenticationConverter {
    private static final String N_A = "N/A";

    @Override
    public Map<String, ?> convertUserAuthentication(Authentication userAuthentication) {
        return null;
    }

    // map 是check-token 返回的全部信息
    @Override
    public Authentication extractAuthentication(Map<String, ?> map) {
        if (map.containsKey(USERNAME)) {
            String username = (String) map.get(USERNAME);
            Object authorities = map.get("authorities");
            List<GrantedAuthority> grantedAuthorities = null;
            if (authorities != null) {
                List<String> roles = (List<String>) authorities;
                grantedAuthorities = new ArrayList<>(roles.size());
                for (String authority : roles) {
                    grantedAuthorities.add(new SimpleGrantedAuthority(authority));
                }
            }
            UserDetail user = new UserDetail(username, "", grantedAuthorities);
            user.setUserId(Long.parseLong(map.get("userId").toString()));
            user.setSystemType(map.containsKey("systemType") ? map.get("systemType").toString() : "");
            user.setUserNickName(map.containsKey("userNickName") ? (null == map.get("userNickName") ? username : map.get("userNickName").toString()) : "");
            if (map.containsKey("userAvatar")) {
                Object avatar = map.get("userAvatar");
                if (null != avatar && !"".equals(avatar)) {
                    user.setUserAvatar(avatar.toString());
                }
            }
            if (map.containsKey("userNo")) {
                Object avatar = map.get("userNo");
                if (null != avatar && !"".equals(avatar)) {
                    user.setUserNo(avatar.toString());
                }
            }
            if (map.containsKey("userProfessional")) {
                Object avatar = map.get("userProfessional");
                if (null != avatar && !"".equals(avatar)) {
                    user.setUserProfessional(avatar.toString());
                }
            }
            if (map.containsKey("schoolId")) {
                Object avatar = map.get("schoolId");
                if (null != avatar && !"".equals(avatar)) {
                    user.setUserId(Long.parseLong(avatar.toString()));
                }
            }
            return new UsernamePasswordAuthenticationToken(user, N_A, grantedAuthorities);
        }
        return null;
    }
}