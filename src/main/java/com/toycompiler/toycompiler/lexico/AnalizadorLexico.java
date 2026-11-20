
package com.toycompiler.toycompiler.lexico;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalizadorLexico {

    private final AfdIdentificador afdIdentificador =
            new AfdIdentificador();

    private final AfdConstanteNumerica afdNumero =
            new AfdConstanteNumerica();

    private final List<Token> tokens = new ArrayList<>();
    private final List<String> errores = new ArrayList<>();

    private String codigo;
    private int posicion;
    private int linea;
    private int columna;

    private static final Map<String, TipoToken> RESERVADAS =
            new HashMap<>();

    static {
        RESERVADAS.put("func", TipoToken.FUNC);
        RESERVADAS.put("if", TipoToken.IF);
        RESERVADAS.put("then", TipoToken.THEN);
        RESERVADAS.put("else", TipoToken.ELSE);
        RESERVADAS.put("true", TipoToken.TRUE);
        RESERVADAS.put("false", TipoToken.FALSE);
        RESERVADAS.put("and", TipoToken.AND);
        RESERVADAS.put("or", TipoToken.OR);
        RESERVADAS.put("while", TipoToken.WHILE);
        RESERVADAS.put("do", TipoToken.DO);
        RESERVADAS.put("for", TipoToken.FOR);
        RESERVADAS.put("end", TipoToken.END);
        RESERVADAS.put("local", TipoToken.LOCAL);
        RESERVADAS.put("print", TipoToken.PRINT);
        RESERVADAS.put("println", TipoToken.PRINTLN);
        RESERVADAS.put("ret", TipoToken.RET);
    }

    public List<Token> analizar(String codigo) {

        this.codigo = codigo == null ? "" : codigo;

        posicion = 0;
        linea = 1;
        columna = 1;

        tokens.clear();
        errores.clear();

        while (!finCodigo()) {

            char actual = caracterActual();

            // Espacios en blanco
            if (Character.isWhitespace(actual)) {
                avanzar();
                continue;
            }

            // Comentarios
            if (actual == '-' && mirarSiguiente() == '-') {

                while (!finCodigo()
                        && caracterActual() != '\n') {
                    avanzar();
                }

                continue;
            }

            // Identificadores y palabras reservadas
            if (Character.isLetter(actual) || actual == '_') {
                reconocerIdentificador();
                continue;
            }

            // Números
            if (Character.isDigit(actual)) {
                reconocerNumero();
                continue;
            }

            // Cadenas
            if (actual == '"' || actual == '\'') {
                reconocerCadena();
                continue;
            }

            // Operadores y signos
            reconocerSimbolo();
        }

        tokens.add(new Token(
                TipoToken.FIN_ARCHIVO,
                "$",
                linea,
                columna
        ));

        return new ArrayList<>(tokens);
    }

    private void reconocerIdentificador() {

        int inicio = posicion;
        int lineaInicio = linea;
        int columnaInicio = columna;

        while (!finCodigo()
                && (Character.isLetterOrDigit(caracterActual())
                || caracterActual() == '_')) {
            avanzar();
        }

        String lexema = codigo.substring(inicio, posicion);

        if (!afdIdentificador.validar(lexema)) {

            agregarError("Identificador inválido: " + lexema,
                    lineaInicio, columnaInicio);
            return;
        }

        TipoToken tipo = RESERVADAS.get(lexema);

        if (tipo == null) {
            tipo = TipoToken.IDENTIFICADOR;
        }

        agregarToken(tipo, lexema, lineaInicio, columnaInicio);
    }

    private void reconocerNumero() {

        int inicio = posicion;
        int lineaInicio = linea;
        int columnaInicio = columna;

        while (!finCodigo()) {

            char c = caracterActual();

            if (Character.isLetterOrDigit(c) || c == '.') {
                avanzar();
            } else if ((c == '+' || c == '-')
                    && posicion > inicio
                    && (codigo.charAt(posicion - 1) == 'E'
                    || codigo.charAt(posicion - 1) == 'e')) {
                avanzar();
            } else {
                break;
            }
        }

        String lexema = codigo.substring(inicio, posicion);

        if (afdNumero.validar(lexema)) {

            agregarToken(TipoToken.NUMERO, lexema,
                    lineaInicio, columnaInicio);

        } else {

            agregarError("Constante numérica inválida: " + lexema,
                    lineaInicio, columnaInicio);
        }
    }

    private void reconocerCadena() {

        int lineaInicio = linea;
        int columnaInicio = columna;

        char delimitador = caracterActual();

        avanzar();

        int inicio = posicion;

        while (!finCodigo()
                && caracterActual() != delimitador
                && caracterActual() != '\n') {
            avanzar();
        }

        if (finCodigo() || caracterActual() != delimitador) {

            agregarError("Cadena sin cerrar",
                    lineaInicio, columnaInicio);
            return;
        }

        String contenido = codigo.substring(inicio, posicion);

        avanzar();

        agregarToken(TipoToken.CADENA, contenido,
                lineaInicio, columnaInicio);
    }

    private void reconocerSimbolo() {

        int lineaInicio = linea;
        int columnaInicio = columna;

        char actual = caracterActual();

        switch (actual) {

            case ':':

                avanzar();

                if (caracterActualSeguro() == '=') {
                    avanzar();

                    agregarToken(TipoToken.ASIGNACION,
                            ":=", lineaInicio, columnaInicio);
                } else {
                    agregarError("Se esperaba '=' después de ':'",
                            lineaInicio, columnaInicio);
                }

                break;

            case '<':

                avanzar();

                if (caracterActualSeguro() == '=') {
                    avanzar();

                    agregarToken(TipoToken.MENOR_IGUAL,
                            "<=", lineaInicio, columnaInicio);
                } else {
                    agregarToken(TipoToken.MENOR,
                            "<", lineaInicio, columnaInicio);
                }

                break;

            case '>':

                avanzar();

                if (caracterActualSeguro() == '=') {
                    avanzar();

                    agregarToken(TipoToken.MAYOR_IGUAL,
                            ">=", lineaInicio, columnaInicio);
                } else {
                    agregarToken(TipoToken.MAYOR,
                            ">", lineaInicio, columnaInicio);
                }

                break;

            case '=':

                avanzar();

                if (caracterActualSeguro() == '=') {
                    avanzar();

                    agregarToken(TipoToken.IGUAL,
                            "==", lineaInicio, columnaInicio);
                } else {
                    agregarError("Operador '=' inválido. Utilice '==' o ':='",
                            lineaInicio, columnaInicio);
                }

                break;

            case '~':

                avanzar();

                if (caracterActualSeguro() == '=') {
                    avanzar();

                    agregarToken(TipoToken.DIFERENTE,
                            "~=", lineaInicio, columnaInicio);
                } else {
                    agregarToken(TipoToken.NEGACION,
                            "~", lineaInicio, columnaInicio);
                }

                break;

            case '+':
                agregarSimple(TipoToken.MAS, "+",
                        lineaInicio, columnaInicio);
                break;

            case '-':
                agregarSimple(TipoToken.MENOS, "-",
                        lineaInicio, columnaInicio);
                break;

            case '*':
                agregarSimple(TipoToken.MULTIPLICACION, "*",
                        lineaInicio, columnaInicio);
                break;

            case '/':
                agregarSimple(TipoToken.DIVISION, "/",
                        lineaInicio, columnaInicio);
                break;

            case '%':
                agregarSimple(TipoToken.MODULO, "%",
                        lineaInicio, columnaInicio);
                break;

            case '^':
                agregarSimple(TipoToken.POTENCIA, "^",
                        lineaInicio, columnaInicio);
                break;

            case '(':
                agregarSimple(TipoToken.PARENTESIS_ABRE, "(",
                        lineaInicio, columnaInicio);
                break;

            case ')':
                agregarSimple(TipoToken.PARENTESIS_CIERRA, ")",
                        lineaInicio, columnaInicio);
                break;

            case ',':
                agregarSimple(TipoToken.COMA, ",",
                        lineaInicio, columnaInicio);
                break;

            default:

                agregarError("Carácter no reconocido: " + actual,
                        lineaInicio, columnaInicio);

                avanzar();
                break;
        }
    }

    private void agregarSimple(TipoToken tipo, String lexema,
                               int linea, int columna) {

        avanzar();

        agregarToken(tipo, lexema, linea, columna);
    }

    private void agregarToken(TipoToken tipo, String lexema,
                              int linea, int columna) {

        tokens.add(new Token(tipo, lexema, linea, columna));
    }

    private void agregarError(String mensaje,
                              int linea, int columna) {

        errores.add("Línea " + linea + ", columna "
                + columna + ": " + mensaje);
    }

    private char caracterActual() {
        return codigo.charAt(posicion);
    }

    private char caracterActualSeguro() {
        return finCodigo() ? '\0' : caracterActual();
    }

    private char mirarSiguiente() {

        if (posicion + 1 >= codigo.length()) {
            return '\0';
        }

        return codigo.charAt(posicion + 1);
    }

    private boolean finCodigo() {
        return posicion >= codigo.length();
    }

    private void avanzar() {

        if (finCodigo()) {
            return;
        }

        if (codigo.charAt(posicion) == '\n') {
            linea++;
            columna = 1;
        } else {
            columna++;
        }

        posicion++;
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public List<String> getErrores() {
        return errores;
    }

    public boolean hayErrores() {
        return !errores.isEmpty();
    }
}
