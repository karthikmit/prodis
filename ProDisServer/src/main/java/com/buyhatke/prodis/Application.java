package com.buyhatke.prodis;

import com.buyhatke.prodis.api.ProdisAPIHandler;
import com.buyhatke.prodis.core.ProDictFacade;
import com.squareup.okhttp.OkHttpClient;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import reactor.core.Environment;
import reactor.core.Reactor;
import reactor.core.composable.Stream;
import reactor.core.spec.Reactors;
import reactor.net.NetServer;
import reactor.net.config.ServerSocketOptions;
import reactor.net.netty.NettyServerSocketOptions;
import reactor.net.netty.tcp.NettyTcpServer;
import reactor.net.tcp.spec.TcpServerSpec;
import reactor.spring.context.config.EnableReactor;

import java.util.concurrent.CountDownLatch;

/**
 * Spring Boot App for CacheX
 */
@EnableAutoConfiguration
@Configuration
@ComponentScan("com.buyhatke.prodis")
@EnableReactor
public class Application {

    // This makes the server to dispatch using the same thread ...
    public static final String SYNCHRONOUS_DISPATCHER = "sync";

    @Bean
    public Reactor reactor(Environment env) {
        Reactor reactor = Reactors.reactor(env, Environment.RING_BUFFER);

        return reactor;
    }

    @Bean
    public ServerSocketOptions serverSocketOptions() {
        return new NettyServerSocketOptions()
                .pipelineConfigurer(pipeline -> pipeline.addLast(new HttpServerCodec())
                        .addLast(new HttpObjectAggregator(16 * 1024 * 1024)));
    }

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient();
    }

    @Bean
    public NetServer<FullHttpRequest, FullHttpResponse> netServerStart(Environment environment,
                                                                       ServerSocketOptions socketOptions,
                                                                       CountDownLatch closeLatch,
                                                                       ProdisAPIHandler prodisAPIHandler) throws InterruptedException {
        NetServer<FullHttpRequest, FullHttpResponse> server = new TcpServerSpec<FullHttpRequest, FullHttpResponse>(
                NettyTcpServer.class)
                .env(environment).dispatcher(SYNCHRONOUS_DISPATCHER).options(socketOptions)
                .consume(ch -> {
                    // filter requests by URI via the input Stream
                    Stream<FullHttpRequest> in = ch.in();

                    // General APIs will be handled with this ..
                    in.filter((FullHttpRequest req) -> req.getUri().startsWith("/"))
                            .when(Throwable.class, prodisAPIHandler.errorHandler(ch))
                            .consume(prodisAPIHandler.handleApi(ch));

                    // shutdown this app
                    in.filter((FullHttpRequest req) -> "/shutdown".equals(req.getUri()))
                            .consume(req -> closeLatch.countDown());
                })
                .get();

        server.start().await();

        return server;
    }

    @Bean
    public CountDownLatch closeLatch() {
        return new CountDownLatch(1);
    }

    public static void main(String... args) throws InterruptedException {
        ApplicationContext ctx = SpringApplication.run(Application.class, args);
        ProDictFacade proDictFacade = ctx.getBean(ProDictFacade.class);

        // Just to make sure Main thread is hanging around ..
        // And the latch count down happens at Shutdown API Call.
        CountDownLatch closeLatch = ctx.getBean(CountDownLatch.class);
        closeLatch.await();

        // This would happen only during close. Missing to do this, might result in cache information loss.
        proDictFacade.close();
    }

}