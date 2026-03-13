package com.panaderia.rodrigo.controller;


import com.panaderia.rodrigo.model.Pedido;
import com.panaderia.rodrigo.model.Pedido.EstadoPedido;
import com.panaderia.rodrigo.service.ClienteService;
import com.panaderia.rodrigo.service.PedidoService;
import com.panaderia.rodrigo.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/pedidos")
public class PedidoController {

    @Autowired
    private ClienteService clienteService;
    @Autowired
    private ProductoService productoService;
    @Autowired
    private PedidoService pedidoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    public String listar(Model model) {
        model.addAttribute("pedidos", pedidoService.findAll());
        model.addAttribute("estados", Pedido.EstadoPedido.values());
        model.addAttribute("pendientes", pedidoService.countByEstado(EstadoPedido.PENDIENTE));
        model.addAttribute("enPreparacion", pedidoService.countByEstado(EstadoPedido.EN_PREPARACION));
        return "pedidos/lista";
    }
    @GetMapping("/nuevo")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    public String formularioNuevo(Model model) {
        model.addAttribute("pedido", new Pedido());
        model.addAttribute("clientes", clienteService.findAll());
        model.addAttribute("productos", productoService.findDisponibles());
        return "pedidos/formulario";
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    public String guardar(@ModelAttribute Pedido pedido, RedirectAttributes flash) {
        pedidoService.save(pedido);
        flash.addFlashAttribute("mensaje", "Pedido registrado correctamente");
        return "redirect:/pedidos";
    }

    @GetMapping("/estado/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    public String cambiarEstado(@PathVariable Long id,
                                @RequestParam EstadoPedido estado,
                                RedirectAttributes flash) {
        pedidoService.cambiarEstado(id, estado);
        flash.addFlashAttribute("mensaje", "Estado actualizado: " + estado);
        return "redirect:/pedidos";
    }

    @GetMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        pedidoService.delete(id);
        flash.addFlashAttribute("mensaje", "Pedido eliminado");
        return "redirect:/pedidos";
    }
}
