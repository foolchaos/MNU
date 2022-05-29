package mnu.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.context.request.RequestContextListener
import javax.sql.DataSource

@Configuration
@EnableWebSecurity
class SecurityConfig : WebSecurityConfigurerAdapter() {

    @Autowired
    private var dataSource: DataSource? = null

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun requestContextListener(): RequestContextListener {
        return RequestContextListener()
    }


    override fun configure(web: WebSecurity) {
        web
            .ignoring()
            .antMatchers("/resources/**", "/static/**", "/css/**", "/js/**", "/pics/**")
    }

    override fun configure(http: HttpSecurity) {
        http
            .csrf().disable()
            .authorizeRequests()
            .antMatchers( "/auth/**", "/", "/*.ico", "/img/**", "/*.js", "/bot/**").permitAll()
            .antMatchers("/admin/**").hasAuthority("ADMIN")
            .antMatchers("/sec/**").hasAuthority("SECURITY")
            .antMatchers("/sci/**").hasAuthority("SCIENTIST")
            .antMatchers("/man/**").hasAuthority("MANAGER")
            .antMatchers("/prawn/**").hasAuthority("PRAWN")
            .antMatchers("/client/**").hasAuthority("CUSTOMER")
            .antMatchers("/manufacturer/**").hasAuthority("MANUFACTURER")
            .antMatchers("/profile").hasAnyAuthority("ADMIN", "SECURITY", "SCIENTIST", "MANAGER")
            .antMatchers("/profileClient").hasAnyAuthority("CUSTOMER", "MANUFACTURER")
            .anyRequest().authenticated()
            .and()
            .formLogin()
            .loginPage("/auth/login")
            .successHandler { request, response, authentication -> run {
                val roles = AuthorityUtils.authorityListToSet(authentication.authorities)
                response.status = 200
                response.contentType = MediaType.APPLICATION_JSON_VALUE
                response.outputStream.print("{\"Login\":\"" + request.getParameter("login") + "\"}")
                if (roles.contains("ADMIN"))
                    response.sendRedirect("/admin/main")
                if (roles.contains("MANAGER"))
                    response.sendRedirect("/man/main")
                if (roles.contains("SCIENTIST"))
                    response.sendRedirect("/sci/main")
                if (roles.contains("SECURITY"))
                    response.sendRedirect("/sec/main")
                if (roles.contains("CUSTOMER"))
                    response.sendRedirect("/client/shop/weapon")
                if (roles.contains("MANUFACTURER"))
                    response.sendRedirect("/manufacturer/market/weapon")
                if (roles.contains("PRAWN"))
                    response.sendRedirect("/prawn/main")
            }}
            .failureHandler { request, response, exception ->
                request.session.setAttribute("loginFailed", true)
                response.sendRedirect("/login")
            }
            .usernameParameter("username")
            .passwordParameter("password")
            .permitAll()
            .and()
            .logout()
            .logoutUrl("/auth/logout")
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID")
            .logoutSuccessUrl("/")
            .and()
            .exceptionHandling().authenticationEntryPoint { request, response, authException -> response.sendRedirect("/auth/login") }
    }


    @Autowired
    fun configureGlobal(auth: AuthenticationManagerBuilder) {
        auth.jdbcAuthentication()
            .dataSource(dataSource)
            .passwordEncoder(passwordEncoder())
            .usersByUsernameQuery("select login, password, 1 from users where login=?")
            .authoritiesByUsernameQuery("select u.login, u.role from users u where u.login=?")
    }
}