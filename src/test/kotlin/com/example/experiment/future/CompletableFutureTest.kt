package com.example.experiment.future

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture

// 참고 자료: https://wbluke.tistory.com/50
class CompletableFutureTest {
    private val log = LoggerFactory.getLogger(CompletableFutureTest::class.java)

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

    /**
     * supplyAsync
     * 파라미터로 Supplier 인터페이스를 받아서 반환값이 존재하는 메서드
     * 비동기 상황에서의 작업을 콜백 함수로 넘기고 작업 수행 여부와 관계 없이 CompletableFuture 객체로 다음 로직을 이어나갈 수 있다.
     * join() 문은 CompletableFuture가 끝나기를 기다리는 Blocking 메서드
     */
    @Test
    fun supplyAsync() {
        // given
        val message = "Hello"
        val messageFuture = CompletableFuture.supplyAsync {
            sayMessage(message)
        }

        // when
        val result = messageFuture.join()

        // then
        assertThat(result).isEqualTo("Say Hello")
    }

    /**
     * runAsync
     * 파라미터로 Runnable 인터페이스를 받아서 반환값이 존재하지 않는다.
     * join()을 사용하여 Blocking을 건다.
     */
    @Test
    fun runAsync() {
        // given
        val message = "Hello"
        val messageFuture = CompletableFuture.runAsync { sayMessage(message) }

        // when - then
        messageFuture.join()
    }

    /**
     * completedFuture
     * 이미 완료된 작업이나 정적인 값을 CompletableFuture로 감쌀 필요가 있을 때
     */
    @Test
    fun completedFuture() {
        // given
        val message = "Hello"
        val messageFuture = CompletableFuture.completedFuture(message)

        // when
        val result = messageFuture.join()

        // then
        assertThat(result).isEqualTo("Hello")
    }

    /**
     * thenApply
     * 반환형이 존재하는 메소드
     */
    @Test
    fun thenApply() {
        /* given */
        val message = "Hello"
        val messageFuture = CompletableFuture.supplyAsync { sayMessage(message) }

        /* when */
        val result = messageFuture
            .thenApply { saidMessage: String -> "Applied message : $saidMessage" }
            .join()

        /* then */
        assertThat(result).isEqualTo("Applied message : Say Hello")
    }

    /**
     * thenAccept
     * 반환형이 없는 메서드 (콜백 작업을 받는다)
     */
    @Test
    fun thenAccept() {
        /* given */
        val message = "Hello"
        val messageFuture = CompletableFuture.supplyAsync { sayMessage(message) }

        /* when - then */
        messageFuture
            .thenAccept { saidMessage: String ->
                val acceptedMessage = "accepted message : $saidMessage"
                log.info("thenAccept {}", acceptedMessage)
            }
            .join()
    }

    /**
     * allOf
     * 여러 CompletableFuture를 한번에 Blocking할 때 사용
     */
    @Test
    fun allOf() {
        // given
        val messages = listOf<String>("Hello", "Hi", "Bye")
        val messageFutures = messages.map { CompletableFuture.supplyAsync { sayMessage(it) } }

        // when
        val saidMessages = CompletableFuture.allOf(messageFutures.toTypedArray()[0])
            .thenApply { messageFutures.map { it.join() } }
            .join()

        // then
        val expectedMessages: List<String> = listOf("Say Hello", "Say Hi", "Say Bye")
        assertThat(expectedMessages == saidMessages).isTrue
    }

    /**
     * thenCompose
     * 또 다른 CompletableFuture를 파이프라인 형식으로 연결해서 실행할 수 있는 기능
     */
    @Test
    fun thenCompose() {
        /* given */
        val message = "Hello"
        val messageFuture = CompletableFuture.supplyAsync { sayMessage(message) }

        /* when */
        val result = messageFuture
            .thenCompose { saidMessage: String ->
                CompletableFuture.supplyAsync {
                    sleepOneSecond()
                    "$saidMessage!"
                }
            }
            .join()

        /* then */
        assertThat(result).isEqualTo("Say Hello!")
    }

    /**
     * thenCombine
     * 앞 단계의 결과를 인자로 받지 않고,
     * 전혀 다른 CompletableFuture를 첫번째 인자로, 두 CompletableFuture의 결과를 두번째 인자로 받아 결과를 연산하는 BiFunction 인터페이스를 받음.
     */
    @Test
    fun thenCombine() {
        /* given */
        val message = "Hello"
        val messageFuture = CompletableFuture.supplyAsync { sayMessage(message) }

        /* when */
        val result = messageFuture
            .thenCombine(
                CompletableFuture.supplyAsync {
                    sleepOneSecond()
                    "!"
                }) { message1: String, message2: String -> message1 + message2 }
            .join()

        /* then */assertThat(result).isEqualTo("Say Hello!")
    }
}
