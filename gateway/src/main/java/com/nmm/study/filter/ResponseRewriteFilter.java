package com.nmm.study.filter;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 响应内容重写服务
 */
@Slf4j
@Component
public class ResponseRewriteFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        Object obj = ReactiveSecurityContextHolder.getContext().map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal).defaultIfEmpty("没有授权");
        log.info("授权信息：{}",obj );

        ServerHttpResponse response = exchange.getResponse();
        DataBufferFactory bufferFactory = response.bufferFactory();

        ServerHttpResponseDecorator nresopnse = new ServerHttpResponseDecorator(response) {
            @Override
            public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
                return writeWith(Flux.from(body).flatMapSequential(p -> p));
            }

            /**
             * 该方法是重写的关键
             * @param body
             * @return
             */
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {

                //自己增加判断
                if (body instanceof Flux) {
                    //注意这里可能会将过长的文件分割
                    Flux<? extends DataBuffer> bodyflux = Flux.from(body);
                    return super.writeWith(bodyflux.map(databuffer -> {
                        byte[] buff = new byte[databuffer.readableByteCount()];
                        databuffer.read(buff);
                        //释放内存
                        DataBufferUtils.release(databuffer);
                        String res = new String(buff, Charset.forName("UTF-8"));
                        return res;
                    }).collect(Collectors.joining())
                            .map(res -> {
                                log.info("重写响应。{}",res);
                                //这里追加内容
                                res += " 追加内容";
                                getHeaders().setContentLength(res.getBytes(Charset.forName("UTF-8")).length);
                                return bufferFactory.wrap(res.getBytes(Charset.forName("UTF-8")));
                            }));
                }
                return super.writeWith(body);
            }
            @Override
            public void beforeCommit(Supplier<? extends Mono<Void>> action) {
                super.beforeCommit(action);
            }
        };

        exchange = exchange.mutate().response(nresopnse).build();

        return chain.filter(exchange);
    }

    /**
     * 生效的前提是我们的filter必须在这之前
     * @return
     */
    @Override
    public int getOrder() {
        return NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 1;
    }
}
