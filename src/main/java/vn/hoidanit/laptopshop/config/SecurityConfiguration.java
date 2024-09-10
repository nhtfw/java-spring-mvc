package vn.hoidanit.laptopshop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.session.security.web.authentication.SpringSessionRememberMeServices;

import jakarta.servlet.DispatcherType;
import vn.hoidanit.laptopshop.service.CustomUserDetailsService;
import vn.hoidanit.laptopshop.service.UserService;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {

    // cấu hình password encoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // cấu hình UserDetailsService(giúp query thông tin user), mặc định là
    // InmemoryUserDetailsService
    @Bean
    public UserDetailsService userDetailsService(UserService userService) {
        return new CustomUserDetailsService(userService);
    }

    // nạp tất cả những thông tin customize vào
    @Bean
    public DaoAuthenticationProvider authProvider(
            PasswordEncoder passwordEncoder,
            UserDetailsService userDetailsService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        // authProvider.setHideUserNotFoundExceptions(false);
        return authProvider;
    }

    // login success
    @Bean
    public AuthenticationSuccessHandler customSuccessHandler() {
        return new CustomSuccessHandler();
    }

    // gia hạn session bằng remember me - config remember me
    @Bean
    public SpringSessionRememberMeServices rememberMeServices() {
        SpringSessionRememberMeServices rememberMeServices = new SpringSessionRememberMeServices();
        // optionally customize
        rememberMeServices.setAlwaysRemember(true);
        return rememberMeServices;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .dispatcherTypeMatchers(DispatcherType.FORWARD,
                                DispatcherType.INCLUDE) //
                        .permitAll()

                        // "/product/**" -> ** là gì không quan trọng
                        .requestMatchers("/", "/login", "/register", "/product/**", "/client/**", "/css/**",
                                "/js/**",
                                "/images/**")// tất cả các đường link này đều được permit(cho phép)
                        .permitAll()

                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        .anyRequest().authenticated())//

                .sessionManagement((sessionManagement) -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.ALWAYS) // luôn tạo mới session nếu như chưa có
                                                                             // session
                        .invalidSessionUrl("/logout?expired") // nếu hết hạn session -> logout
                        .maximumSessions(1) // giới hạn 1 tải khoản đăng nhập tại một thời điểm, nếu đã đăng nhập từ
                                            // thiết bị A rồi, thì thiết bị B không đăng nhập được bằng tài khoản đó nữa
                        .maxSessionsPreventsLogin(false)) // nếu người thứ 2 đăng nhập vào sau, người trước sẽ bị đá ra.
                                                          // Nếu là true, người thứ 2 phải chờ người 1 hết phiên đăng
                                                          // nhập

                // mỗi một lần logout ra, sẽ xóa cookies và báo cho server, session hết hạn
                .logout(logout -> logout.deleteCookies("JSESSIONID").invalidateHttpSession(true))

                .rememberMe(rememberMe -> rememberMe.rememberMeServices(rememberMeServices()))
                .formLogin(formLogin -> formLogin
                        .loginPage("/login") // url
                        .failureUrl("/login?error") // khi login fail
                        .successHandler(customSuccessHandler()) // khi login thanh cong
                        .permitAll()) // ai cũng có quyền vào
                .exceptionHandling(ex -> ex.accessDeniedPage("/access_deny")); // handler loi 403 forbidden
        // .logout((logout) -> logout.logoutSuccessUrl("/"));
        return http.build();
    }

}
