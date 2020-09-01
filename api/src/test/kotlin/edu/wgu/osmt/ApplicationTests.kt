package edu.wgu.osmt

import edu.wgu.osmt.config.AppConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("dev") // TODO change to a test profile
@ConfigurationPropertiesScan("edu.wgu.osmt.config")
class ApplicationTests {

    @Autowired
    private lateinit var appConfig: AppConfig

    @Test
    fun exampleTest() {
        assertThat(appConfig).isNotNull
    }

}
