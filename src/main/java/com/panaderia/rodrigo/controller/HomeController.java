package com.panaderia.rodrigo.controller;


import com.panaderia.rodrigo.model.Pedido;
import com.panaderia.rodrigo.service.ClienteService;
import com.panaderia.rodrigo.service.PedidoService;
import com.panaderia.rodrigo.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @Autowired
    private ProductoService productoService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private PedidoService pedidoService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("totalproductos", productoService.findAll());
        model.addAttribute("totalClientes", clienteService.count());
        model.addAttribute("totalPedidos", pedidoService.count());
        model.addAttribute("pedidosPendientes", pedidoService.countByEstado(Pedido.EstadoPedido.PENDIENTE));
        model.addAttribute("stockBajo", productoService.findStockBajo());
        model.addAttribute("ultimosPedidos", pedidoService.findAll().stream().limit(5).toList());
        return "index";
    }

}