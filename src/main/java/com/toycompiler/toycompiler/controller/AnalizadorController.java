
package com.toycompiler.toycompiler.controller;

import com.toycompiler.toycompiler.lexico.AnalizadorLexico;
import com.toycompiler.toycompiler.lexico.Token;
import com.toycompiler.toycompiler.sintactico.AnalizadorSintactico;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AnalizadorController {

    @PostMapping("/analizar")
    public Map<String, Object> analizar(
            @RequestBody Map<String, String> datos) {

        String codigo = datos.get("codigo");

        // ANÁLISIS LÉXICO
        AnalizadorLexico lexico = new AnalizadorLexico();

        List<Token> tokens = lexico.analizar(codigo);

        List<String> erroresLexicos =
                new ArrayList<>(lexico.getErrores());

        // ANÁLISIS SINTÁCTICO
        List<String> erroresSintacticos = new ArrayList<>();

        boolean aceptado = false;

        if (erroresLexicos.isEmpty()) {

            AnalizadorSintactico sintactico =
                    new AnalizadorSintactico();

            aceptado = sintactico.analizar(tokens);

            erroresSintacticos =
                    new ArrayList<>(sintactico.getErrores());
        }

        return Map.of(
                "tokens", tokens,
                "erroresLexicos", erroresLexicos,
                "erroresSintacticos", erroresSintacticos,
                "aceptado", aceptado
        );
    }
}
