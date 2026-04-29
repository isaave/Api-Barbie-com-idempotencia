package com.barbie.dreamworld_api.infrastructure;

import com.barbie.dreamworld_api.model.*;
import com.barbie.dreamworld_api.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class LoadDatabase {

    @Bean
    CommandLineRunner initDatabase(
            ProfissaoRepository profissaoRepo,
            DreamhouseRepository dreamhouseRepo,
            BarbieRepository barbieRepo,
            AcessorioRepository acessorioRepo,
            EventoRepository eventoRepo) {

        return args -> {

            // ── Profissões (Salvando individualmente para garantir o ID no objeto) ────────
            Profissao medica     = new Profissao(); medica.setNomeProfissao("Médica");           medica.setDescricao("Salva vidas no DreamMed");        medica.setAreaAtuacao("Saúde");
            Profissao astronauta = new Profissao(); astronauta.setNomeProfissao("Astronauta");    astronauta.setDescricao("Explora galáxias com estilo"); astronauta.setAreaAtuacao("Ciência");
            Profissao presidenta = new Profissao(); presidenta.setNomeProfissao("Presidenta");    presidenta.setDescricao("Lidera o país com elegância"); presidenta.setAreaAtuacao("Política");
            Profissao chef       = new Profissao(); chef.setNomeProfissao("Chef de Cozinha");     chef.setDescricao("Pratos 5 estrelas cor-de-rosa");     chef.setAreaAtuacao("Gastronomia");
            Profissao fashion    = new Profissao(); fashion.setNomeProfissao("Fashion Designer"); fashion.setDescricao("Dita tendências do multiverso");  fashion.setAreaAtuacao("Moda");

            medica = profissaoRepo.save(medica);
            astronauta = profissaoRepo.save(astronauta);
            presidenta = profissaoRepo.save(presidenta);
            chef = profissaoRepo.save(chef);
            fashion = profissaoRepo.save(fashion);
            log.info("Profissões carregadas: {}", profissaoRepo.count());

            // ── Dreamhouses (Atualizando as referências com o retorno do save) ───────────
            Dreamhouse malibu    = new Dreamhouse(); malibu.setNomeCasa("Malibu Dreamhouse");     malibu.setEndereco("1600 Malibu Road");     malibu.setCor("Rosa");    malibu.setNumeroQuartos(5);
            Dreamhouse space     = new Dreamhouse(); space.setNomeCasa("Space Station");          space.setEndereco("Órbita Terrestre 42");   space.setCor("Branco");   space.setNumeroQuartos(3);
            Dreamhouse pink      = new Dreamhouse(); pink.setNomeCasa("Pink Palace");             pink.setEndereco("Beverly Hills, CA");     pink.setCor("Pink");      pink.setNumeroQuartos(7);
            Dreamhouse garden    = new Dreamhouse(); garden.setNomeCasa("Garden Cottage");        garden.setEndereco("Malibu Canyon");       garden.setCor("Lilás");   garden.setNumeroQuartos(4);
            Dreamhouse penthouse = new Dreamhouse(); penthouse.setNomeCasa("Downtown Penthouse"); penthouse.setEndereco("New York, 5th Ave"); penthouse.setCor("Dourado"); penthouse.setNumeroQuartos(6);

            malibu = dreamhouseRepo.save(malibu);
            space = dreamhouseRepo.save(space);
            pink = dreamhouseRepo.save(pink);
            garden = dreamhouseRepo.save(garden);
            penthouse = dreamhouseRepo.save(penthouse);
            log.info("Dreamhouses carregadas: {}", dreamhouseRepo.count());

            // ── Barbies ───────────────────────────────────────────────────────
            Barbie b1 = new Barbie(); b1.setNome("Barbie Clássica");    b1.setColecao("Dreamtopia");       b1.setProfissao(fashion);    b1.setDreamhouse(malibu);
            Barbie b2 = new Barbie(); b2.setNome("Barbie Astronauta");  b2.setColecao("Carreiras");        b2.setProfissao(astronauta); b2.setDreamhouse(space);
            Barbie b3 = new Barbie(); b3.setNome("Barbie Médica");      b3.setColecao("Carreiras");        b3.setProfissao(medica);     b3.setDreamhouse(pink);
            Barbie b4 = new Barbie(); b4.setNome("Barbie Presidenta");  b4.setColecao("Edição Especial");  b4.setProfissao(presidenta); b4.setDreamhouse(penthouse);
            Barbie b5 = new Barbie(); b5.setNome("Barbie Chef");        b5.setColecao("Sabores do Mundo"); b5.setProfissao(chef);       b5.setDreamhouse(garden);

            b1 = barbieRepo.save(b1);
            b2 = barbieRepo.save(b2);
            b3 = barbieRepo.save(b3);
            b4 = barbieRepo.save(b4);
            b5 = barbieRepo.save(b5);
            log.info("Barbies carregadas: {}", barbieRepo.count());

            // ── Acessórios ────────────────────────────────────────────────────
            Acessorio a1 = new Acessorio(); a1.setNomeItem("Bolsa Matelassê Rosa");     a1.setRaridade(Raridade.RARA);            a1.setPreco(299.90); a1.setCategoria("Bolsa");       a1.setBarbie(b1);
            Acessorio a2 = new Acessorio(); a2.setNomeItem("Scarpin Glitter");           a2.setRaridade(Raridade.COMUM);           a2.setPreco(89.90);  a2.setCategoria("Sapato");      a2.setBarbie(b1);
            Acessorio a3 = new Acessorio(); a3.setNomeItem("Capacete Espacial");         a3.setRaridade(Raridade.COLECIONADOR);    a3.setPreco(450.00); a3.setCategoria("Equipamento"); a3.setBarbie(b2);
            Acessorio a4 = new Acessorio(); a4.setNomeItem("Estetoscópio Dourado");      a4.setRaridade(Raridade.RARA);            a4.setPreco(150.00); a4.setCategoria("Equipamento"); a4.setBarbie(b3);
            Acessorio a5 = new Acessorio(); a5.setNomeItem("Coroa Presidencial");        a5.setRaridade(Raridade.EDICAO_LIMITADA); a5.setPreco(999.99); a5.setCategoria("Joia");        a5.setBarbie(b4);
            Acessorio a6 = new Acessorio(); a6.setNomeItem("Avental Chanel");            a6.setRaridade(Raridade.COLECIONADOR);    a6.setPreco(320.00); a6.setCategoria("Roupa");       a6.setBarbie(b5);
            acessorioRepo.saveAll(List.of(a1, a2, a3, a4, a5, a6));
            log.info("Acessórios carregados: {}", acessorioRepo.count());

            // ── Eventos ───────────────────────────────────────────────────────
            Evento e1 = new Evento(); e1.setNomeEvento("Gala de Malibu");            e1.setDescricao("O maior baile do multiverso");     e1.setDataEvento(LocalDate.of(2026, 12, 31)); e1.setLocal("Malibu Beach Club");
            Evento e2 = new Evento(); e2.setNomeEvento("Fashion Week Dreamworld");   e2.setDescricao("Semana de moda cor-de-rosa");        e2.setDataEvento(LocalDate.of(2026, 9, 15));  e2.setLocal("Paris, França");
            Evento e3 = new Evento(); e3.setNomeEvento("Lançamento Coleção Espacial");e3.setDescricao("Nova linha Astronauta");            e3.setDataEvento(LocalDate.of(2026, 7, 20));  e3.setLocal("Kennedy Space Center");
            Evento e4 = new Evento(); e4.setNomeEvento("Barbie World Summit");       e4.setDescricao("Conferência das Barbies líderes");   e4.setDataEvento(LocalDate.of(2026, 10, 5));  e4.setLocal("New York, EUA");
            Evento e5 = new Evento(); e5.setNomeEvento("Festival Gastronômico Pink"); e5.setDescricao("Sabores do universo Barbie");        e5.setDataEvento(LocalDate.of(2026, 8, 10));  e5.setLocal("São Paulo, Brasil");

            e1 = eventoRepo.save(e1);
            e2 = eventoRepo.save(e2);
            e3 = eventoRepo.save(e3);
            e4 = eventoRepo.save(e4);
            e5 = eventoRepo.save(e5);
            log.info("Eventos carregados: {}", eventoRepo.count());

            // ── Relacionamentos N:N Barbie ↔ Evento ──────────────────────────
            b1.setEventos(List.of(e1, e2));     barbieRepo.save(b1);
            b2.setEventos(List.of(e1, e3));     barbieRepo.save(b2);
            b3.setEventos(List.of(e4));          barbieRepo.save(b3);
            b4.setEventos(List.of(e1, e2, e4)); barbieRepo.save(b4);
            b5.setEventos(List.of(e5, e1));     barbieRepo.save(b5);

            log.info("=== BarbieDream API iniciada com sucesso! ===");
        };
    }
}