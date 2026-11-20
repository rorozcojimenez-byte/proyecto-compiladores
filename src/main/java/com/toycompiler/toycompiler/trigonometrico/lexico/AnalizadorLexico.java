
package com.toycompiler.toycompiler.trigonometrico.lexico;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AnalizadorLexico {

    private final List<Token> tokens = new ArrayList<>();
    private final List<String> errores = new ArrayList<>();

    private String entrada;
    private int posicion;
    private int linea;
    private int columna;

    // ==========================================
    // ANALIZAR ENTRADA
    // ==========================================

    public List<Token> analizar(String entrada) {

        tokens.clear();
        errores.clear();

        this.entrada = entrada;
        this.posicion = 0;
        this.linea = 1;
        this.columna = 1;

        while (!finEntrada()) {

            char actual = caracterActual();

            if (Character.isWhitespace(actual)) {
                avanzar();
            }

            else if (Character.isDigit(actual)) {
                reconocerNumero();
            }

            else if (Character.isLetter(actual)) {
                reconocerIdentificador();
            }

            else {
                reconocerSimbolo();
            }
        }

        tokens.add(new Token(
                TipoToken.FIN_ENTRADA,
                "$",
                linea,
                columna
        ));

        return tokens;
    }


    // ==========================================
    // RECONOCER NÚMEROS
    // ==========================================

    private void reconocerNumero() {

        int inicio = posicion;
        int lineaInicio = linea;
        int columnaInicio = columna;

        boolean tienePunto = false;

        while (!finEntrada()) {

            char actual = caracterActual();

            if (Character.isDigit(actual)) {
                avanzar();
            }

            else if (actual == '.') {

                if (tienePunto) {
                    break;
                }

                tienePunto = true;
                avanzar();
            }

            else {
                break;
            }
        }

        String lexema = entrada.substring(inicio, posicion);

        if (tienePunto && lexema.endsWith(".")) {

            errores.add(
                    "Número decimal incompleto: " + lexema
                    + " en línea " + lineaInicio
                    + ", columna " + columnaInicio
            );

            return;
        }

        agregarToken(
                TipoToken.NUM,
                lexema,
                lineaInicio,
                columnaInicio
        );
    }


    // ==========================================
    // RECONOCER IDENTIFICADORES
    // ==========================================

    private void reconocerIdentificador() {

        int inicio = posicion;
        int lineaInicio = linea;
        int columnaInicio = columna;

        while (!finEntrada() &&
                Character.isLetterOrDigit(caracterActual())) {

            avanzar();
        }

        String lexema = entrada.substring(inicio, posicion);

        String identificador = lexema.toLowerCase(Locale.ROOT);

        switch (identificador) {

            case "sin":
                agregarToken(
                        TipoToken.SIN,
                        lexema,
                        lineaInicio,
                        columnaInicio
                );
                break;

            case "cos":
                agregarToken(
                        TipoToken.COS,
                        lexema,
                        lineaInicio,
                        columnaInicio
                );
                break;

            case "tan":
                agregarToken(
                        TipoToken.TAN,
                        lexema,
                        lineaInicio,
                        columnaInicio
                );
                break;

            case "x":
            case "y":
            case "z":
                agregarToken(
                        TipoToken.VAR,
                        lexema,
                        lineaInicio,
                        columnaInicio
                );
                break;

            default:
                errores.add(
                        "Identificador no reconocido: " + lexema
                        + " en línea " + lineaInicio
                        + ", columna " + columnaInicio
                );
        }
    }


    // ==========================================
    // RECONOCER OPERADORES Y PARÉNTESIS
    // ==========================================

    private void reconocerSimbolo() {

        char actual = caracterActual();

        int lineaInicio = linea;
        int columnaInicio = columna;

        TipoToken tipo = null;

        switch (actual) {

            case '+':
                tipo = TipoToken.MAS;
                break;

            case '-':
                tipo = TipoToken.MENOS;
                break;

            case '*':
                tipo = TipoToken.MULTIPLICACION;
                break;

            case '/':
                tipo = TipoToken.DIVISION;
                break;

            case '^':
                tipo = TipoToken.POTENCIA;
                break;

            case '(':
                tipo = TipoToken.PARENTESIS_IZQUIERDO;
                break;

            case ')':
                tipo = TipoToken.PARENTESIS_DERECHO;
                break;

            default:
                errores.add(
                        "Carácter no reconocido: " + actual
                        + " en línea " + lineaInicio
                        + ", columna " + columnaInicio
                );
        }

        if (tipo != null) {

            agregarToken(
                    tipo,
                    String.valueOf(actual),
                    lineaInicio,
                    columnaInicio
            );
        }

        avanzar();
    }


    // ==========================================
    // AGREGAR TOKEN
    // ==========================================

    private void agregarToken(
            TipoToken tipo,
            String lexema,
            int linea,
            int columna) {

        tokens.add(new Token(
                tipo,
                lexema,
                linea,
                columna
        ));
    }


    // ==========================================
    // CONTROL DE POSICIÓN
    // ==========================================

    private char caracterActual() {
        return entrada.charAt(posicion);
    }

    private boolean finEntrada() {
        return posicion >= entrada.length();
    }

    private void avanzar() {

        if (caracterActual() == '\n') {
            linea++;
            columna = 1;
        }

        else {
            columna++;
        }

        posicion++;
    }


    // ==========================================
    // CONSULTAR RESULTADOS
    // ==========================================

    public List<Token> getTokens() {
        return tokens;
    }

    public List<String> getErrores() {
        return errores;
    }

}

