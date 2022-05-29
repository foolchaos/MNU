rootProject.name = "mnu"

pluginManagement {

    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "org.jetbrains.kotlin.jvm" -> useVersion("1.6.21")
                "org.jetbrains.kotlin.plugin.spring" -> useVersion("1.6.21")
                "org.jetbrains.kotlin.plugin.jpa" -> useVersion("1.6.21")
                "org.jetbrains.kotlin.plugin.allopen" -> useVersion("1.6.21")
                "org.jetbrains.kotlin.plugin.noarg" -> useVersion("1.6.21")
                "org.jetbrains.kotlin.kapt" -> useVersion("1.6.21")

                "org.springframework.boot" -> useVersion("2.5.6")
                "io.spring.dependency-management" -> useVersion("1.0.11.RELEASE")
            }
        }
    }
}
