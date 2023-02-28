package ca.bc.gov.educ.api.edx;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.transaction.PlatformTransactionManager;

@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "1s")
@EnableRetry
public class EdxApiResourceApplication {

  public static void main(final String[] args) {
    SpringApplication.run(EdxApiResourceApplication.class, args);
  }

  /**
   * Add security exceptions for swagger UI and prometheus.
   */
  @Configuration
  @EnableMethodSecurity
  static
  class WebSecurityConfiguration {

    /**
     * Instantiates a new Web security configuration.
     * This makes sure that security context is propagated to async threads as well.
     */
    public WebSecurityConfiguration() {
      super();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
      http
              .csrf(AbstractHttpConfigurer::disable)
              .authorizeHttpRequests(auth -> auth
                      .requestMatchers("/v3/api-docs/**", "/actuator/health", "/actuator/prometheus","/swagger-ui/**", "/health")
                      .permitAll()
                      .anyRequest()
                      .authenticated())
              .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
              .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);
      return http.build();
    }

}

  @Bean
  public LockProvider lockProvider(@Autowired final JdbcTemplate jdbcTemplate, @Autowired final PlatformTransactionManager transactionManager) {
    return new JdbcTemplateLockProvider(jdbcTemplate, transactionManager, "EDX_SHEDLOCK");
  }

  /**
   * Thread pool task scheduler thread pool task scheduler.
   *
   * @return the thread pool task scheduler
   */
  @Bean(name = "taskExecutor")
  public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
    final ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
    threadPoolTaskScheduler.setPoolSize(2);
    return threadPoolTaskScheduler;
  }
}
