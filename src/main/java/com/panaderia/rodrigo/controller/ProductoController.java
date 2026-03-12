package com.panaderia.rodrigo.controller;

import com.panaderia.rodrigo.model.Producto;
import com.panaderia.rodrigo.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/productos")

public class ProductoController {
    @Autowired
    private ProductoService productoService;

    @GetMapping
    public String listar(Model model){
        model.addAttribute("productos", productoService.findAll());
        // template/productos/lista.html
        return "productos/lista";
    }

    @GetMapping("/nuevo")
    public String formulario(Model model){
        model.addAttribute("producto", new Producto());
        return "productos/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Producto producto){
        productoService.save(producto);
        return "redirect:/productos";
    }
}
