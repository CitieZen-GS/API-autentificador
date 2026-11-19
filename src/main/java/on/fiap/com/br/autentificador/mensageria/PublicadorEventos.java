package on.fiap.com.br.autentificador.mensageria;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Envia os eventos para o RabbitMQ.
 * <p>
 * Os services não falam com o RabbitMQ diretamente: eles publicam um evento do Spring
 * (ApplicationEventPublisher) e este componente o envia só DEPOIS do commit no banco
 * ({@link TransactionalEventListener}). Assim nenhuma mensagem sai para uma operação que
 * acabou sendo desfeita (rollback).
 * <p>
 * Se o RabbitMQ estiver fora do ar, o logout continua funcionando: o erro só é registrado no log
 * e o token de acesso volta a valer até expirar (o comportamento de antes da mensageria).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PublicadorEventos {

    private final RabbitTemplate rabbitTemplate;

    @TransactionalEventListener
    public void publicar(AcessoRevogadoEvento evento) {
        try {
            rabbitTemplate.convertAndSend(ConfiguracaoMensageria.EXCHANGE_EVENTOS,
                    ConfiguracaoMensageria.ROTA_ACESSO_REVOGADO, evento);
            log.info("Evento {} publicado: jti={}, usuario={} {}", ConfiguracaoMensageria.ROTA_ACESSO_REVOGADO,
                    evento.jti(), evento.tipoUsuario(), evento.idUsuario());
        } catch (AmqpException e) {
            log.error("Falha ao publicar {} (jti={}): o token continuará valendo até expirar",
                    ConfiguracaoMensageria.ROTA_ACESSO_REVOGADO, evento.jti(), e);
        }
    }
}
