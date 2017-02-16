package my.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity pHttp) throws Exception {
//        ACTUATOR
        pHttp.authorizeRequests()
            .antMatchers("/management/**").hasAnyRole("admin")
            .and().httpBasic();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder pAuth) throws Exception {
//        super.configure(pAuth);
        pAuth.inMemoryAuthentication()
            .withUser("admin").password("admin").roles("admin");
    }
}
