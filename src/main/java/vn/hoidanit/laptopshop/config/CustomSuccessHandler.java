package vn.hoidanit.laptopshop.config;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class CustomSuccessHandler implements AuthenticationSuccessHandler {

    // phu trach chuyen huong trang
    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    protected String determineTargetUrl(final Authentication authentication) {

        Map<String, String> roleTargetUrlMap = new HashMap<>();
        roleTargetUrlMap.put("ROLE_USER", "/"); // nếu là role USER sẽ dưa vào "/"
        roleTargetUrlMap.put("ROLE_ADMIN", "/admin");

        // lấy ra quyền hạn của người dùng
        final Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (final GrantedAuthority grantedAuthority : authorities) {
            String authorityName = grantedAuthority.getAuthority(); // lấy ra tên role
            if (roleTargetUrlMap.containsKey(authorityName)) { // lấy ra key
                return roleTargetUrlMap.get(authorityName); // lấy ra value
            }
        }

        // nếu không tìm thấy gì, ném ra exception
        throw new IllegalStateException();
    }

    // dọn dẹp session
    protected void clearAuthenticationAttributes(HttpServletRequest request) {
        // nếu đã tồn tại session rồi, nó sẽ sử dụng lại session đấy mà không tạo mới
        // false -> chỉ khi nào có session mới lấy ra, nếu k có k làm gì cả
        // không truyền phương thức vào getSession(), nếu không có sẽ tạo mới, nếu có
        // rồi thì dùng session có sẵn
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        // redirect về đâu sau khi login thành công
        // authentication là data lưu trữ bên trong security
        String targetUrl = determineTargetUrl(authentication);

        // ~=handler exception
        if (response.isCommitted()) {
            return;
        }

        // redirect
        redirectStrategy.sendRedirect(request, response, targetUrl);
        // dọn dẹp session giúp tăng hiệu năng
        clearAuthenticationAttributes(request);
    }

}
