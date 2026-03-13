package com.panaderia.rodrigo.controller;

import com.panaderia.rodrigo.model.Cliente;
import com.panaderia.rodrigo.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    public String listar(@RequestParam(required = false) String buscar, Model model) {
        if (buscar != null && !buscar.isBlank()) {
            model.addAttribute("clientes", clienteService.buscarPorNombre(buscar));
            model.addAttribute("buscar", buscar);
        } else {
            model.addAttribute("clientes", clienteService.findAll());
        }
        return "clientes/lista";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasRole('ADMIN')")
    public String formularioNuevo(Model model) {
        model.addAttribute("cliente", new Cliente());
        model.addAttribute("titulo", "Nuevo Cliente");
        return "clientes/formulario";
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasRole('ADMIN')")
    public String guardar(@Valid @ModelAttribute Cliente cliente,
                          BindingResult result,
                          Model model,
                          RedirectAttributes flash) {
        if (result.hasErrors()) {
            model.addAttribute("titulo", "Nuevo Cliente");
            return "clientes/formulario";
        }
        try {
            clienteService.save(cliente);
            flash.addFlashAttribute("mensaje", "Cliente registrado correctamente ✅");
        } catch (RuntimeException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/clientes";
    }

    @GetMapping("/editar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String formularioEditar(@PathVariable Long id, Model model) {
        model.addAttribute("cliente", clienteService.findById(id));
        model.addAttribute("titulo", "Editar Cliente");
        return "clientes/formulario";
    }

    @GetMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        clienteService.delete(id);
        flash.addFlashAttribute("mensaje", "Cliente eliminado correctamente 🗑️");
        return "redirect:/clientes";
    }
}
