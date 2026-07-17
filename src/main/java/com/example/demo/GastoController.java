package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/")
public class GastoController {

    // Lista em memória para simular o banco de dados
    private final List<Gasto> listaGastos = new ArrayList<>();
    // Mapa para guardar o faturamento inserido para cada mês (Ex: "2026-07" -> 15000.0)
    private final Map<String, Double> faturamentoMensal = new ConcurrentHashMap<>();

    // Enum com todas as categorias solicitadas pelos seus pais
    public enum Categoria {
        ABASTECIMENTO("Abastecimento"),
        SALARIO("Salário"),
        AJUDA_DE_CUSTO("Ajuda de Custo"),
        PECAS("Peças"),
        MECANICO("Mecânico"),
        DIARIAS("Diárias"),
        SEGURO_CARRO("Seguro de Carro"),
        SEGURO_CARGA("Seguro de Carga"),
        MONITORAMENTO("Monitoramento"),
        CONVENIO_MEDICO("Convênio Médico"),
        ALUGUEL_GALPAO("Aluguel Galpão"),
        AGUA("Água"),
        LUZ("Luz"),
        OUTROS("Outros");

        private final String nomeExibicao;
        Categoria(String nomeExibicao) { this.nomeExibicao = nomeExibicao; }
        public String getNomeExibicao() { return nomeExibicao; }
    }

    // Classe interna que representa o Gasto
    public static class Gasto {
        private static long contadorId = 1;
        private Long id;
        private String descricao;
        private double valor;
        private String tipo; // "NORMAL" ou "MULTA"
        private Categoria categoria;
        private LocalDate data;

        public Gasto(String descricao, double valor, String tipo, Categoria categoria, LocalDate data) {
            this.id = contadorId++;
            this.descricao = descricao;
            this.valor = valor;
            this.tipo = tipo;
            this.categoria = categoria;
            this.data = data != null ? data : LocalDate.now();
        }

        // Getters e Setters
        public Long getId() { return id; }
        public String getDescricao() { return descricao; }
        public void setDescricao(String descricao) { this.descricao = descricao; }
        public double getValor() { return valor; }
        public void setValor(double valor) { this.valor = valor; }
        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }
        public Categoria getCategoria() { return categoria; }
        public void setCategoria(Categoria categoria) { this.categoria = categoria; }
        public LocalDate getData() { return data; }
        public void setData(LocalDate data) { this.data = data; }
    }

    public GastoController() {
        // Dados de exemplo para o sistema não iniciar vazio
        listaGastos.add(new Gasto("Diesel Caminhão Volvo", 1200.50, "NORMAL", Categoria.ABASTECIMENTO, LocalDate.now()));
        listaGastos.add(new Gasto("Conserto de Embreagem", 850.00, "NORMAL", Categoria.MECANICO, LocalDate.now()));
        listaGastos.add(new Gasto("Excesso de velocidade Rod. Dutra", 195.23, "MULTA", Categoria.OUTROS, LocalDate.now()));
    }

    @GetMapping
    public String index(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) Categoria categoria,
            @RequestParam(required = false) String mes, // Formato "YYYY-MM"
            Model model) {

        List<Gasto> gastosFiltrados = new ArrayList<>(listaGastos);

        // 1. Filtro por Tipo (NORMAL ou MULTA)
        if (tipo != null && !tipo.isEmpty()) {
            gastosFiltrados = gastosFiltrados.stream()
                    .filter(g -> g.getTipo().equalsIgnoreCase(tipo))
                    .collect(Collectors.toList());
        }

        // 2. Filtro por Categoria
        if (categoria != null) {
            gastosFiltrados = gastosFiltrados.stream()
                    .filter(g -> g.getCategoria() == categoria)
                    .collect(Collectors.toList());
        }

        // 3. Filtro por Mês (Ano-Mês)
        String mesSelecionado = (mes != null && !mes.isEmpty()) ? mes : LocalDate.now().toString().substring(0, 7);
        gastosFiltrados = gastosFiltrados.stream()
                .filter(g -> g.getData().toString().startsWith(mesSelecionado))
                .collect(Collectors.toList());

        // 4. Cálculos Financeiros do Mês Selecionado
        double totalGastos = gastosFiltrados.stream().mapToDouble(Gasto::getValor).sum();
        double faturamento = faturamentoMensal.getOrDefault(mesSelecionado, 0.0);
        double saldoFinal = faturamento - totalGastos;

        // Enviando dados para o Thymeleaf na tela
        model.addAttribute("gastos", gastosFiltrados);
        model.addAttribute("categorias", Categoria.values());
        model.addAttribute("tipoSelecionado", tipo);
        model.addAttribute("categoriaSelecionada", categoria);
        model.addAttribute("mesSelecionado", mesSelecionado);
        
        // Valores financeiros formatados
        model.addAttribute("totalGastos", totalGastos);
        model.addAttribute("faturamento", faturamento);
        model.addAttribute("saldoFinal", saldoFinal);

        return "index";
    }

    @PostMapping("/salvar")
    public String salvarGasto(
            @RequestParam String descricao,
            @RequestParam double valor,
            @RequestParam String tipo,
            @RequestParam Categoria categoria,
            @RequestParam String data) {

        LocalDate dataGasto = (data != null && !data.isEmpty()) ? LocalDate.parse(data) : LocalDate.now();
        listaGastos.add(new Gasto(descricao, valor, tipo, categoria, dataGasto));
        
        // Redireciona mantendo o foco no mês do gasto cadastrado
        String mesGasto = dataGasto.toString().substring(0, 7);
        return "redirect:/?mes=" + mesGasto;
    }

    @PostMapping("/faturamento")
    public String salvarFaturamento(@RequestParam String mes, @RequestParam double valor) {
        faturamentoMensal.put(mes, valor);
        return "redirect:/?mes=" + mes;
    }

    @GetMapping("/deletar/{id}")
    public String deletarGasto(@PathVariable Long id, @RequestParam(required = false) String mes) {
        listaGastos.removeIf(g -> g.getId().equals(id));
        return "redirect:/?mes=" + (mes != null ? mes : "");
    }
}