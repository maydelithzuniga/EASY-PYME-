package com.easymype.backend.config;

import com.easymype.backend.entity.PlantillaColumna;
import com.easymype.backend.entity.PlantillaTabla;
import com.easymype.backend.entity.TipoColumna;
import com.easymype.backend.repository.PlantillaTablaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor


public class DataInicializador implements ApplicationRunner {

    private final PlantillaTablaRepository plantillaRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (plantillaRepository.count() > 0) return; // ya inicializado

        plantillaRepository.save(PlantillaTabla.builder()
                .nombre("Productos")
                .icono("package")
                .categoria("E-commerce")
                .columnas(List.of(
                        PlantillaColumna.builder().nombre("Nombre").orden(1).tipo(TipoColumna.TEXTO).esRequerida(true).build(),
                        PlantillaColumna.builder().nombre("SKU").orden(2).tipo(TipoColumna.TEXTO).esRequerida(true).build(),
                        PlantillaColumna.builder().nombre("Stock").orden(3).tipo(TipoColumna.STOCK).esRequerida(true).build(),
                        PlantillaColumna.builder().nombre("Precio").orden(4).tipo(TipoColumna.PRECIO).esRequerida(true).build(),
                        PlantillaColumna.builder().nombre("Categoría").orden(5).tipo(TipoColumna.CATEGORIA).esRequerida(false).build()
                ))
                .build()
        );


    }

}
