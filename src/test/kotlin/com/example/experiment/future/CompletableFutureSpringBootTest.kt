package com.example.experiment.future

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.function.Supplier

@SpringBootTest
class CompletableFutureSpringBootTest (
    @Qualifier("threadPoolTaskExecutor")
    private val threadPoolTaskExecutor: Executor
){
    @DisplayName("thenApply() : 처음 진행한 스레드가 쭉 이어서 진행한다.")
    @Test
    fun thenApplyWithSameThread() {
        /* given */
        val message = "Hello"
        // 인자로 Executor를 지정하면 해당 스레드 풀의 스레드가 비동기 작업을 진행하게 된다.
        val messageFuture: CompletableFuture<String> = CompletableFuture.supplyAsync(
            { sayMessage(message) }, threadPoolTaskExecutor
        )

        /* when */
        val result: String = messageFuture
            // thenApply에서 같은 스레드가 이어서 작업을 하게 된다.
            .thenApply { saidMessage ->
                log.info("thenApply() : Same Thread")
                "Applied message : $saidMessage"
            }
            .join()

        /* then */
        assertThat(result).isEqualTo("Applied message : Say Hello")
    }

    /**
     * Async가 붙은 메소드는 처음 작업을 수행한 스레드가 아닌 다른 스레드가 후속 작업을 진행한다.
     * 후속 작업이 첫 작업과 같은 스레드 풀에서 진행된다면 해당 스레드 풀의 다른 스레드가 이 작업을 이어받아서 수행한다.
     */
    @DisplayName("thenApplyAsync() : 스레드 풀을 지정하지 않으면 기본 스레드 풀의 새로운 스레드가 async하게 진행한다.")
    @Test
    fun thenApplyAsync() {
        /* given */
        val message = "Hello"
        val messageFuture = CompletableFuture.supplyAsync(
            { sayMessage(message) }, threadPoolTaskExecutor
        )

        /* when */
        val result = messageFuture
            .thenApplyAsync { saidMessage: String ->
                log.info("thenApplyAsync() : New thread in another thread pool")
                "Applied message : $saidMessage"
            }
            .join()

        /* then */
        assertThat(result).isEqualTo("Applied message : Say Hello")
    }

    @DisplayName("handleAsync() : 지정한 스레드 풀의 새로운 스레드가 async하게 진행한다.")
    @Test
    fun thenApplyAsyncWithAnotherThreadPool() {
        /* given */
        val message = "Hello"
        val messageFuture = CompletableFuture.supplyAsync(
            { sayMessage(message) }, threadPoolTaskExecutor
        )

        /* when */
        val result = messageFuture
            .thenApplyAsync({ saidMessage: String ->
                log.info("thenApplyAsync() : New thread in same thread pool")
                "Applied message : $saidMessage"
            }, threadPoolTaskExecutor)
            .join()

        /* then */
        assertThat(result).isEqualTo("Applied message : Say Hello")
    }

    private val log = LoggerFactory.getLogger(CompletableFutureSpringBootTest::class.java)

    /**
     * 파라미터로 문자열 메시지가 들어오면
     * 1초 쉰 후에 Say를 붙여서 return하는 메서드
     */
    private fun sayMessage(message: String): String {
        sleepOneSecond()

        val saidMessage = "Say $message"
        log.info("Said Messsage = $saidMessage")

        return saidMessage
    }

    private fun sleepOneSecond() {
        try {
            log.info("start to sleep 1 second.")
            Thread.sleep(1000)
            log.info("end to sleep 1 second.")
        } catch (e: InterruptedException) {
            throw IllegalStateException()
        }
    }
}
