package com.panaderia.rodrigo.loader;

import com.panaderia.rodrigo.model.Pedido;
import com.panaderia.rodrigo.model.Producto;
import com.panaderia.rodrigo.repository.ClienteRepository;
import com.panaderia.rodrigo.repository.PedidoRepository;
import com.panaderia.rodrigo.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {
    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Override
    public void run(String... args) {

        //Productos
        Producto p1 = productoRepository.save(Producto.builder()
                .nombre("Medialunas de Manteca")
                .categoria("Factura")
                .precio(350.0)
                .stock(60)
                .descripcion("Medialunas clásicas de manteca, crujientes y doradas")
                .disponible(true)
                .build());

        Producto p2 = productoRepository.save(Producto.builder()
                .nombre("Marraqueta")
                .categoria("Pan")
                .precio(120.0)
                .stock(100)
                .descripcion("Marraqueta artesanal, crocante por fuera y suave por dentro")
                .disponible(true)
                .build());

        Producto p3 = productoRepository.save(Producto.builder()
                .nombre("Torta de Chocolate")
                .categoria("Torta")
                .precio(4800.0)
                .stock(5)
                .descripcion("Torta húmeda de chocolate con ganache")
                .disponible(true)
                .build());

        Producto p4 = productoRepository.save(Producto.builder()
                .nombre("Croissant")
                .categoria("Factura")
                .precio(280.0)
                .stock(40)
                .descripcion("Croissant hojaldrado, estilo francés")
                .disponible(true)
                .build());

        Producto p5 = productoRepository.save(Producto.builder()
                .nombre("Bizcochuelo Vainilla")
                .categoria("Bizcochuelo")
                .precio(2200.0)
                .stock(8)
                .descripcion("Bizcochuelo esponjoso de vainilla")
                .disponible(true)
                .build());

        Producto p6 = productoRepository.save(Producto.builder()
                .nombre("Facturas Surtidas (x6)")
                .categoria("Factura")
                .precio(900.0)
                .stock(20)
                .descripcion("Caja de 6 facturas surtidas: vigilante, cañoncito, bomba")
                .disponible(true)
                .build());

        Producto p7 = productoRepository.save(Producto.builder()
                .nombre("Pan de Campo")
                .categoria("Pan")
                .precio(350.0)
                .stock(15)
                .descripcion("Pan de campo rústico, ideal para picadas")
                .disponible(true)
                .build());

        //Clientes
        Cliente c1 = clienteRepository.save(Cliente.builder()
                .nombre("María González")
                .email("maria.gonzalez@email.com")
                .telefono("011-4567-8901")
                .direccion("Av. Corrientes 1234, CABA")
                .build());

        Cliente c2 = clienteRepository.save(Cliente.builder()
                .nombre("Juan Pérez")
                .email("juan.perez@email.com")
                .telefono("011-5678-9012")
                .direccion("Belgrano 456, CABA")
                .build());

        Cliente c3 = clienteRepository.save(Cliente.builder()
                .nombre("Ana Rodríguez")
                .email("ana.rodriguez@email.com")
                .telefono("011-6789-0123")
                .direccion("San Martín 789, Buenos Aires")
                .build());

        //Pedidos

        pedidoRepository.save(Pedido.builder()
                .cliente(c1)
                .productos(List.of(p1, p2))
                .fechaPedido(LocalDateTime.now().minusHours(2))
                .estado(Pedido.EstadoPedido.EN_PREPARACION)
                .total(p1.getPrecio() + p2.getPrecio())
                .observaciones("Sin azúcar en las medialunas")
                .build());

        pedidoRepository.save(Pedido.builder()
                .cliente(c2)
                .productos(List.of(p3))
                .fechaPedido(LocalDateTime.now().minusDays(1))
                .estado(Pedido.EstadoPedido.ENTREGADO)
                .total(p3.getPrecio())
                .observaciones("Para cumpleaños, incluir vela")
                .build());

        pedidoRepository.save(Pedido.builder()
                .cliente(c3)
                .productos(List.of(p4, p6, p7))
                .fechaPedido(LocalDateTime.now())
                .estado(Pedido.EstadoPedido.PENDIENTE)
                .total(p4.getPrecio() + p6.getPrecio() + p7.getPrecio())
                .observaciones(null)
                .build());


        System.out.println("  ¡Panadería Rodrigo lista!");
        System.out.println("  Productos cargados: " + productoRepository.count());
        System.out.println("  Clientes cargados:  " + clienteRepository.count());
        System.out.println("  Pedidos cargados:   " + pedidoRepository.count());
        System.out.println("  URL: http://localhost:8080");
        System.out.println("  H2:  http://localhost:8080/h2-console");
        System.out.println("  Admin: rodrigo / admin123");
        System.out.println("  Empleado: empleado / emp123");

    }

}