package com.hrl.mon.auth.config;
import com.hrl.mon.auth.handler.CustomAuthExceptionHandler;
import com.hrl.mon.auth.handler.OAuthWebResponseExceptionTranslator;
import com.hrl.mon.auth.sms.SmsTokenGranter;
import com.hrl.mon.common.constant.RedisKeyPrefix;
import com.hrl.mon.common.constant.SecurityConstants;
import com.hrl.mon.auth.entity.SystemUser;
import com.hrl.mon.auth.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.CompositeTokenGranter;
import org.springframework.security.oauth2.provider.TokenGranter;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.refresh.RefreshTokenGranter;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Zhifeng.Zeng
 * @description OAuth2???????????????
 */
@Configuration
public class OAuth2Config {

    /**
     * @description ???????????????
     */
    @Configuration
    @EnableResourceServer
    protected static class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

        @Autowired
        private CustomAuthExceptionHandler customAuthExceptionHandler;

        /**
         * ?????????????????????
         * @param resources
         */
        @Override
        public void configure(ResourceServerSecurityConfigurer resources) {
            resources.stateless(false)
                    .accessDeniedHandler(customAuthExceptionHandler)
                    .authenticationEntryPoint(customAuthExceptionHandler);
        }

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                    .and()
                    //??????????????????
                    .authorizeRequests()
                    //?????????????????????,?????????????????????
                    .antMatchers("/oauth/**").permitAll()
                    .antMatchers("/user/sendVerifyCode/**").permitAll() //?????????????????????
                    .antMatchers("/upload/callback").permitAll() //oss??????
                    //OPTIONS?????????????????????
                    .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .anyRequest()
                    .authenticated();

        }
    }

    /**
     * @description ?????????????????????
     */
    @Configuration
    @EnableAuthorizationServer
    protected static class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

        @Autowired
        private AuthenticationManager authenticationManager;

        @Autowired
        private RedisConnectionFactory connectionFactory;

        @Autowired
        private UserDetailsServiceImpl userDetailsService;

        @Autowired
        @Qualifier(value = "smsAuthenticationProvider")
        private AuthenticationProvider smsAuthenticationProvider;

        @Autowired
        private DataSource dataSource;

        /**
         * ??????ClientDetails??????
         * @param clients
         */
        @Override
        public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
            clients.withClientDetails(clientService());
        }

        @Bean
        public RedisTokenStore redisTokenStore() {
            RedisTokenStore redisTokenStore = new RedisTokenStore(connectionFactory);
            redisTokenStore.setPrefix(RedisKeyPrefix.OAUTH_PREFIX);
            return redisTokenStore;
        }

        /**
         * ?????????????????????????????????
         * @return
         */
        @Bean
        public ClientDetailsService clientService() {
            return new JdbcClientDetailsService(dataSource);
        }

        /**
         * ??????TokenService??????
         * @return
         */
        @Bean
        public DefaultTokenServices tokenService(){
            DefaultTokenServices tokenService = new DefaultTokenServices();
            tokenService.setTokenStore(redisTokenStore());
            tokenService.setSupportRefreshToken(true);
            tokenService.setClientDetailsService(clientService());
            tokenService.setTokenEnhancer(tokenEnhancer());
            tokenService.setReuseRefreshToken(false);
            return tokenService;
        }

        /**
         * token????????????????????????????????????
         *
         * @return TokenEnhancer
         */
        @Bean
        public TokenEnhancer tokenEnhancer() {
            return (accessToken, authentication) -> {
//                if (SecurityConstants.CLIENT_CREDENTIALS
//                        .equals(authentication.getOAuth2Request().getGrantType())) {
//                    return accessToken;
//                }
                final Map<String, Object> additionalInfo = new HashMap<>(8);
                SystemUser baseUser = (SystemUser) authentication.getUserAuthentication().getPrincipal();
                additionalInfo.put(SecurityConstants.DETAILS_USER_ID, baseUser.getUserId());
                additionalInfo.put(SecurityConstants.DETAILS_USERNAME, baseUser.getUsername());
                additionalInfo.put(SecurityConstants.DETAILS_TENANT_ID, baseUser.getTenantId());
                additionalInfo.put(SecurityConstants.DETAILS_ORGAN_ID, baseUser.getOrganId());
                ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);
                return accessToken;
            };
        }


        /**
         * @description token????????????????????????redis???????????????????????????????????????????????????????????????
         * @param endpoints
         */
        @Override
        public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
            //token????????????redis
            endpoints.tokenStore(redisTokenStore()).authenticationManager(authenticationManager);
            endpoints.tokenServices(tokenService());
            endpoints.userDetailsService(userDetailsService);
            List<TokenGranter> tokenGranters = new ArrayList<>();
            /**
             * ???????????????AbstractTokenGranter?????????
             */
            //??????token
            tokenGranters.add(new RefreshTokenGranter(endpoints.getTokenServices(),clientService(),
                    endpoints.getOAuth2RequestFactory()));
            //?????????????????????????????? ??????../sms
            tokenGranters.add(new SmsTokenGranter(smsAuthenticationProvider, endpoints.getTokenServices(),
                    clientService(), endpoints.getOAuth2RequestFactory()));
            endpoints.tokenGranter(new CompositeTokenGranter(tokenGranters));
            endpoints.exceptionTranslator(new OAuthWebResponseExceptionTranslator());

        }

        @Override
        public void configure(AuthorizationServerSecurityConfigurer oauthServer) {
            //??????????????????
            // ????????????Query???????????????????????????client_id/client_secret ???????????????????????????
            oauthServer.allowFormAuthenticationForClients().tokenKeyAccess("isAuthenticated()")
                    .checkTokenAccess("permitAll()").passwordEncoder(new BCryptPasswordEncoder());
        }
    }


}
