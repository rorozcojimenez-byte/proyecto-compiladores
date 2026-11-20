
package com.toycompiler.toycompiler.controller;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.toycompiler.toycompiler.trigonometrico.lexico.AnalizadorLexico;
import com.toycompiler.toycompiler.trigonometrico.lexico.Token;
import com.toycompiler.toycompiler.trigonometrico.lexico.TipoToken;
import com.toycompiler.toycompiler.trigonometrico.sintactico.AnalizadorSintactico;
import com.toycompiler.toycompiler.trigonometrico.evaluador.Evaluador;

@Controller
@RequestMapping("/trigonometrico")
public class TrigonometricoController {

    @GetMapping("/analizador")
    public String mostrarAnalizador() {
        return "trigonometrico/analizador";
    }

    @GetMapping("/gramatica")
    public String mostrarGramatica() {
        return "trigonometrico/gramatica";
    }

    // PRIMER PASO: ANÁLISIS LÉXICO Y SINTÁCTICO

    @PostMapping("/analizar")
    public String analizarExpresion(
            @RequestParam("codigo") String codigo,
            Model model) {

        AnalizadorLexico lexico = new AnalizadorLexico();
        List<Token> tokens = lexico.analizar(codigo);

        List<String> erroresLexicos =
                new ArrayList<>(lexico.getErrores());

        AnalizadorSintactico sintactico = new AnalizadorSintactico();

        boolean aceptado = false;

        // Inicializamos la lista para evitar valores nulos.
        List<String> erroresSintacticos = new ArrayList<>();

        if (erroresLexicos.isEmpty()) {

            aceptado = sintactico.analizar(tokens);

            if (sintactico.getErrores() != null) {
                erroresSintacticos.addAll(sintactico.getErrores());
            }
        }

        model.addAttribute("codigo", codigo);
        model.addAttribute("tokens", tokens);
        model.addAttribute("errores", erroresLexicos);
        model.addAttribute("erroresSintacticos", erroresSintacticos);
        model.addAttribute("aceptado", aceptado);
        model.addAttribute("analizado", true);

        if (erroresLexicos.isEmpty() && aceptado) {

            Set<String> variables = obtenerVariables(tokens);

            model.addAttribute("variables", variables);

            if (variables.isEmpty()) {

                try {
                    Evaluador evaluador = new Evaluador();

                    double resultado = evaluador.evaluar(
                            tokens,
                            new HashMap<>()
                    );

                    model.addAttribute("resultado", resultado);
                    model.addAttribute("evaluado", true);

                } catch (RuntimeException e) {
                    model.addAttribute("errorEvaluacion", e.getMessage());
                }

            } else {
                model.addAttribute("solicitarValores", true);
            }
        }

        return "trigonometrico/analizador";
    }


    // SEGUNDO PASO: EVALUACIÓN DE LA EXPRESIÓN

    @PostMapping("/evaluar")
    public String evaluarExpresion(
            @RequestParam("codigo") String codigo,
            @RequestParam Map<String, String> parametros,
            Model model) {

        // Volvemos a realizar los análisis para validar la entrada.

        AnalizadorLexico lexico = new AnalizadorLexico();
        List<Token> tokens = lexico.analizar(codigo);

        List<String> erroresLexicos =
                new ArrayList<>(lexico.getErrores());

        AnalizadorSintactico sintactico = new AnalizadorSintactico();

        boolean aceptado = false;

        // Inicializamos la lista para evitar valores nulos.
        List<String> erroresSintacticos = new ArrayList<>();

        if (erroresLexicos.isEmpty()) {

            aceptado = sintactico.analizar(tokens);

            if (sintactico.getErrores() != null) {
                erroresSintacticos.addAll(sintactico.getErrores());
            }
        }

        model.addAttribute("codigo", codigo);
        model.addAttribute("tokens", tokens);
        model.addAttribute("errores", erroresLexicos);
        model.addAttribute("erroresSintacticos", erroresSintacticos);
        model.addAttribute("aceptado", aceptado);
        model.addAttribute("analizado", true);

        if (!erroresLexicos.isEmpty() || !aceptado) {
            return "trigonometrico/analizador";
        }

        Set<String> variables = obtenerVariables(tokens);
        Map<String, Double> valores = new HashMap<>();

        try {

            for (String variable : variables) {

                String valorRecibido = parametros.get(variable);

                if (valorRecibido == null || valorRecibido.isBlank()) {
                    throw new IllegalArgumentException(
                            "Debes ingresar un valor para la variable "
                            + variable + "."
                    );
                }

                double valor = Double.parseDouble(
                        valorRecibido.trim().replace(",", ".")
                );

                if (!Double.isFinite(valor)) {
                    throw new IllegalArgumentException(
                            "El valor de " + variable
                            + " debe ser un número válido."
                    );
                }

                valores.put(variable, valor);
            }

            Evaluador evaluador = new Evaluador();

            double resultado = evaluador.evaluar(tokens, valores);

            if (!Double.isFinite(resultado)) {
                throw new ArithmeticException(
                        "El resultado no es un número finito."
                );
            }

            model.addAttribute("resultado", resultado);
            model.addAttribute("evaluado", true);

        } catch (NumberFormatException e) {

            model.addAttribute(
                    "errorEvaluacion",
                    "Los valores de las variables deben ser números."
            );

        } catch (RuntimeException e) {

            model.addAttribute("errorEvaluacion", e.getMessage());
        }

        model.addAttribute("variables", variables);
        model.addAttribute("valoresIngresados", valores);
        model.addAttribute("solicitarValores", true);

        return "trigonometrico/analizador";
    }


    // IDENTIFICAR LAS VARIABLES QUE APARECEN EN LA EXPRESIÓN

    private Set<String> obtenerVariables(List<Token> tokens) {

        Set<String> variables = new LinkedHashSet<>();

        for (Token token : tokens) {

            if (token.getTipo() == TipoToken.VAR) {
                variables.add(token.getLexema().toLowerCase());
            }
        }

        return variables;
    }
}
