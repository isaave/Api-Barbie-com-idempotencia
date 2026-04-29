package com.barbie.dreamworld_api.infrastructure;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuração global da documentação OpenAPI 3.1 / Swagger UI.
 *
 * Acesse a documentação interativa em:
 *   http://localhost:8080/swagger-ui.html
 *
 * Acesse o JSON da especificação em:
 *   http://localhost:8080/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()

                // ── Informações gerais ────────────────────────────────────────
                .info(new Info()
                        .title("BarbieDream API")
                        .version("1.0.0")
                        .description("""
                                ## Visão Geral

                                API RESTful para gerenciamento centralizado do multiverso Barbie,\
                                 desenvolvida como projeto final da disciplina de **Desenvolvimento\
                                 de Web Services** — curso de Tecnologia em Sistemas para Internet · SENAC.

                                ---

                                ## Domínio de Negócio

                                O sistema permite o controle completo das entidades do universo Barbie:

                                | Entidade     | Descrição                                          |
                                |--------------|----------------------------------------------------|
                                | **Barbie**   | Entidade central. Agrega casa, profissão e eventos |
                                | **Dreamhouse** | Residência oficial (relação 1:1 com Barbie)      |
                                | **Profissão** | Carreira exercida (relação N:1 com Barbie)        |
                                | **Acessório** | Itens colecionáveis (relação 1:N com Barbie)      |
                                | **Evento**   | Galas e eventos sociais (relação N:N com Barbie)   |

                                ---

                                ## Códigos de Status HTTP

                                | Código | Significado                                              |
                                |--------|----------------------------------------------------------|
                                | 200    | Requisição bem-sucedida                                  |
                                | 201    | Recurso criado                                           |
                                | 204    | Operação concluída sem corpo de resposta                 |
                                | 400    | Erro de validação nos campos (fieldErrors detalhado)     |
                                | 404    | Recurso não encontrado                                   |
                                | 409    | Conflito de unicidade (ex: nome duplicado)               |
                                | 422    | Violação de regra de negócio (ex: Dreamhouse ocupada)   |
                                | 429    | Muitas requisições                                 |
                                | 500    | Erro interno inesperado                                  |
                                

                                ---

                                """))
                // ── Servidores ────────────────────────────────────────────────
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor local de desenvolvimento"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("H2 Console: http://localhost:8080/h2-console")
                ))

                // ── Tags com descrições (ordenação no Swagger UI) ─────────────
                .tags(List.of(
                        new Tag()
                                .name("Barbies")
                                .description("Operações sobre a entidade central. Gerencia nome, coleção,"
                                        + " profissão, dreamhouse, acessórios e participação em eventos."),
                        new Tag()
                                .name("Dreamhouses")
                                .description("Residências das Barbies. Relacionamento 1:1 — cada casa"
                                        + " pertence a no máximo uma Barbie."),
                        new Tag()
                                .name("Profissões")
                                .description("Carreiras disponíveis no multiverso Barbie. Relacionamento N:1"
                                        + " — várias Barbies podem exercer a mesma profissão."),
                        new Tag()
                                .name("Acessórios")
                                .description("Itens colecionáveis das Barbies (bolsas, sapatos, joias etc.)."
                                        + " Relacionamento 1:N — uma Barbie pode ter vários acessórios."
                                        + " Filtrável por raridade: COMUM, RARA, COLECIONADOR, EDICAO_LIMITADA."),
                        new Tag()
                                .name("Eventos")
                                .description("Galas e eventos sociais do multiverso. Relacionamento N:N"
                                        + " — uma Barbie participa de vários eventos e um evento tem várias Barbies."
                                        + " Gerencie inscrições via PUT/DELETE /eventos/{id}/participantes/{barbieId}.")
                ));
    }
}