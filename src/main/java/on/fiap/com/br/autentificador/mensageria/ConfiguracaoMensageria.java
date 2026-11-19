package on.fiap.com.br.autentificador.mensageria;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Estrutura do RabbitMQ usada pela API de autenticação (produtora de eventos).
 * <p>
 * Todas as APIs publicam no mesmo exchange do tipo topic; cada consumidor cria a própria fila
 * e escolhe, pela routing key, quais eventos quer receber. Nomes descritos no CLAUDE.md.
 */
@Configuration
public class ConfiguracaoMensageria {

    /** Exchange compartilhado por todas as APIs. Declarar de novo é inofensivo (é idempotente). */
    public static final String EXCHANGE_EVENTOS = "ajudante-familiar.eventos";

    /** Routing key: "<api que publica>.<o que aconteceu>". */
    public static final String ROTA_ACESSO_REVOGADO = "autenticacao.acesso-revogado";

    @Bean
    public TopicExchange exchangeEventos() {
        return new TopicExchange(EXCHANGE_EVENTOS, true, false);
    }

    /** Mensagens em JSON (o RabbitTemplate do Spring Boot usa este conversor automaticamente). */
    @Bean
    public MessageConverter conversorJson() {
        return new JacksonJsonMessageConverter();
    }
}
