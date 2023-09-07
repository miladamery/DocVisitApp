package ir.milad.DocVisitApp.infra.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.WebContentInterceptor;

import java.util.concurrent.TimeUnit;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        WebContentInterceptor interceptor = new WebContentInterceptor();
        interceptor.addCacheMapping(CacheControl.maxAge(3, TimeUnit.MINUTES)
                .noTransform()
                .mustRevalidate(), "/fonts/**", "/metronic/**", "/static/**", "/css/**", "/js/**", "/images/**", "/img/**", "/lib/**", "/favicon.ico");
        registry.addInterceptor(interceptor);
    }
}
