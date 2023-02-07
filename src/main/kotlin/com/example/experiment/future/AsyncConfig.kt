package com.example.experiment.future

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

/**
 * custom한 스레드풀
 * 풀의 사이즈는 3이고 기본 스레드풀과 이름으로 구분지을 수 있도록 prefix 설정
 */
@EnableAsync
@Configuration
class AsyncConfig {
    companion object {
        const val EXECUTOR_NAME = "threadPoolTaskExecutor"
        private const val POOL_SIZE = 3
    }

    @Bean(name = [EXECUTOR_NAME])
    fun threadPoolTaskExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = POOL_SIZE
        executor.maxPoolSize = POOL_SIZE
        executor.setThreadNamePrefix("learning-thread-")
        return executor
    }
}
