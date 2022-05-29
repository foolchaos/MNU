package mnu.config

import org.thymeleaf.spring5.view.ThymeleafViewResolver
import org.springframework.web.servlet.ViewResolver
import org.thymeleaf.spring5.SpringTemplateEngine
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.thymeleaf.extras.springsecurity5.dialect.SpringSecurityDialect


@Configuration
class WebConfig : WebMvcConfigurer {

    @Bean
    fun templateResolver(): ClassLoaderTemplateResolver {

        val templateResolver = ClassLoaderTemplateResolver()

        templateResolver.prefix = "templates/"
        templateResolver.isCacheable = false
        templateResolver.suffix = ".html"
        templateResolver.setTemplateMode("HTML")
        templateResolver.characterEncoding = "UTF-8"

        return templateResolver
    }

    @Bean
    fun templateEngine(): SpringTemplateEngine {

        val templateEngine = SpringTemplateEngine()
        templateEngine.setAdditionalDialects(setOf(SpringSecurityDialect()))
        templateEngine.setTemplateResolver(templateResolver())

        return templateEngine
    }

    @Bean
    fun viewResolver(): ViewResolver {

        val viewResolver = ThymeleafViewResolver()

        viewResolver.templateEngine = templateEngine()
        viewResolver.characterEncoding = "UTF-8"

        return viewResolver
    }
}