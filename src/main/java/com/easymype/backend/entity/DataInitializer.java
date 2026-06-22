package com.easymype.backend.entity;

import com.easymype.backend.repository.PlantillaTablaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final PlantillaTablaRepository plantillaRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (plantillaRepository.count() > 0) {
            log.info("Plantillas ya inicializadas, se omite carga inicial");
            return;
        }

        log.info("Inicializando plantillas de catálogo global...");

        plantillaRepository.saveAll(List.of(
                crearPlantilla(
                        "Productos Generales",
                        "Inventario básico para cualquier tipo de negocio",
                        "📦",
                        "General",
                        List.of(
                                col("Producto",    1, TipoColumna.TEXTO,    true),
                                col("SKU",         2, TipoColumna.TEXTO,    true),
                                col("Stock",       3, TipoColumna.STOCK,    true),
                                col("Stock Mín.",  4, TipoColumna.NUMERO,   true),
                                col("Precio",      5, TipoColumna.PRECIO,   true),
                                col("Categoría",   6, TipoColumna.TEXTO,    false),
                                col("Proveedor",   7, TipoColumna.TEXTO,    false)
                        )
                ),
                crearPlantilla(
                        "Insumos y Materias Primas",
                        "Control de materiales para producción o manufactura",
                        "🏭",
                        "Manufactura",
                        List.of(
                                col("Insumo",         1, TipoColumna.TEXTO,  true),
                                col("Unidad Medida",  2, TipoColumna.TEXTO,  true),
                                col("Stock",          3, TipoColumna.STOCK,  true),
                                col("Stock Mín.",     4, TipoColumna.NUMERO, true),
                                col("Costo Unitario", 5, TipoColumna.PRECIO, true),
                                col("Proveedor",      6, TipoColumna.TEXTO,  false),
                                col("Lote",           7, TipoColumna.TEXTO,  false)
                        )
                ),
                crearPlantilla(
                        "Productos E-commerce",
                        "Para tiendas online con múltiples variantes",
                        "🛒",
                        "E-commerce",
                        List.of(
                                col("Producto",       1, TipoColumna.TEXTO,  true),
                                col("SKU",            2, TipoColumna.TEXTO,  true),
                                col("Variante",       3, TipoColumna.TEXTO,  false),
                                col("Stock",          4, TipoColumna.STOCK,  true),
                                col("Precio Venta",   5, TipoColumna.PRECIO, true),
                                col("Precio Costo",   6, TipoColumna.PRECIO, false),
                                col("Marketplace",    7, TipoColumna.TEXTO,  false),
                                col("URL Producto",   8, TipoColumna.TEXTO,  false)
                        )
                ),
                crearPlantilla(
                        "Activos Fijos",
                        "Control de equipos, herramientas y activos de la empresa",
                        "🔧",
                        "Administración",
                        List.of(
                                col("Activo",          1, TipoColumna.TEXTO,  true),
                                col("Código",          2, TipoColumna.TEXTO,  true),
                                col("Cantidad",        3, TipoColumna.STOCK,  true),
                                col("Valor Compra",    4, TipoColumna.PRECIO, false),
                                col("Fecha Compra",    5, TipoColumna.TEXTO,  false),
                                col("Estado",          6, TipoColumna.TEXTO,  false),
                                col("Responsable",     7, TipoColumna.TEXTO,  false),
                                col("Ubicación",       8, TipoColumna.TEXTO,  false)
                        )
                ),
                crearPlantilla(
                        "Servicios",
                        "Catálogo de servicios ofrecidos con su disponibilidad",
                        "🎯",
                        "Servicios",
                        List.of(
                                col("Servicio",        1, TipoColumna.TEXTO,  true),
                                col("Código",          2, TipoColumna.TEXTO,  true),
                                col("Disponibilidad",  3, TipoColumna.STOCK,  true),
                                col("Precio",          4, TipoColumna.PRECIO, true),
                                col("Duración (hrs)",  5, TipoColumna.NUMERO, false),
                                col("Descripción",     6, TipoColumna.TEXTO,  false)
                        )
                ),
                crearPlantilla(
                        "Alimentos y Bebidas",
                        "Para restaurantes, cafeterías o negocios de alimentos",
                        "🍽️",
                        "Gastronomía",
                        List.of(
                                col("Producto",        1, TipoColumna.TEXTO,  true),
                                col("Unidad",          2, TipoColumna.TEXTO,  true),
                                col("Stock",           3, TipoColumna.STOCK,  true),
                                col("Stock Mín.",      4, TipoColumna.NUMERO, true),
                                col("Costo",           5, TipoColumna.PRECIO, true),
                                col("Precio Venta",    6, TipoColumna.PRECIO, false),
                                col("Fecha Venc.",     7, TipoColumna.TEXTO,  false),
                                col("Proveedor",       8, TipoColumna.TEXTO,  false)
                        )
                ),
                crearPlantilla(
                        "Ropa y Accesorios",
                        "Para tiendas de moda con tallas y colores",
                        "👗",
                        "Moda",
                        List.of(
                                col("Producto",    1, TipoColumna.TEXTO,  true),
                                col("SKU",         2, TipoColumna.TEXTO,  true),
                                col("Talla",       3, TipoColumna.TEXTO,  false),
                                col("Color",       4, TipoColumna.TEXTO,  false),
                                col("Stock",       5, TipoColumna.STOCK,  true),
                                col("Precio",      6, TipoColumna.PRECIO, true),
                                col("Temporada",   7, TipoColumna.TEXTO,  false),
                                col("Proveedor",   8, TipoColumna.TEXTO,  false)
                        )
                )
        ));

        log.info("7 plantillas inicializadas correctamente");
    }

    private PlantillaTabla crearPlantilla(String nombre, String descripcion, String icono,
                                          String categoria, List<PlantillaColumna> columnas) {
        PlantillaTabla plantilla = PlantillaTabla.builder()
                .nombre(nombre)
                .descripcion(descripcion)
                .icono(icono)
                .categoria(categoria)
                .build();

        columnas.forEach(c -> c.setPlantilla(plantilla));
        plantilla.setColumnas(columnas);
        return plantilla;
    }

    private PlantillaColumna col(String nombre, int orden, TipoColumna tipo, boolean esRequerida) {
        return PlantillaColumna.builder()
                .nombre(nombre)
                .orden(orden)
                .tipo(tipo)
                .esRequerida(esRequerida)
                .build();
    }
}
