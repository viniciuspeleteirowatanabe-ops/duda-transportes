package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.List;

// --- INTERFACE AQUI FORA PARA O SPRING ENCONTRAR DIRETO ---
@Repository
interface GastoRepository extends JpaRepository<GastoController.Gasto, Integer> {
    List<GastoController.Gasto> findByTipo(String tipo);
}

@Controller
public class GastoController {

    @Autowired
    private GastoRepository repository;

    @GetMapping("/")
    public String exibirPagina(@RequestParam(required = false) String filtroTipo, Model model) {
        List<Gasto> gastosFiltrados;

        if (filtroTipo != null && !filtroTipo.isEmpty() && !filtroTipo.equals("TODOS")) {
            gastosFiltrados = repository.findByTipo(filtroTipo);
        } else {
            gastosFiltrados = repository.findAll();
        }

        model.addAttribute("gastos", gastosFiltrados);
        model.addAttribute("filtroAtual", filtroTipo != null ? filtroTipo : "TODOS");
        model.addAttribute("gastoForm", new Gasto()); 
        return "index";
    }

    @PostMapping("/adicionar")
    public String salvarGasto(@RequestParam(required = false) Integer id, @RequestParam String descricao, 
                              @RequestParam Double valor, @RequestParam String tipo) {
        Gasto gasto;
        if (id == null || id == 0) {
            gasto = new Gasto(); 
        } else {
            gasto = repository.findById(id).orElse(new Gasto()); 
        }
        
        gasto.setDescricao(descricao);
        gasto.setValor(valor);
        gasto.setTipo(tipo);
        
        repository.save(gasto); 
        return "redirect:/";
    }

    @GetMapping("/editar/{id}")
    public String carregarEdicao(@PathVariable int id, Model model) {
        Gasto gastoParaEditar = repository.findById(id).orElse(null);

        model.addAttribute("gastos", repository.findAll());
        model.addAttribute("gastoForm", gastoParaEditar);
        model.addAttribute("filtroAtual", "TODOS");
        return "index";
    }

    @GetMapping("/deletar/{id}")
    public String deletarGasto(@PathVariable int id) {
        repository.deleteById(id); 
        return "redirect:/";
    }

    // --- ESTRUTURA DO BANCO DE DADOS (ENTIDADE) ---
    @Entity
    public static class Gasto {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY) 
        private Integer id;
        private String descricao;
        private Double valor;
        private String tipo; 

        public Gasto() {}

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getDescricao() { return descricao; }
        public void setDescricao(String d) { this.descricao = d; }
        public Double getValor() { return valor; }
        public void setValor(Double v) { this.valor = v; }
        public String getTipo() { return tipo; }
        public void setTipo(String t) { this.tipo = t; }
    }
}