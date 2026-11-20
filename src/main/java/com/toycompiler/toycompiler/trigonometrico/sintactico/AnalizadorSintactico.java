
package com.toycompiler.toycompiler.trigonometrico.sintactico;

import com.toycompiler.toycompiler.trigonometrico.lexico.Token;
import com.toycompiler.toycompiler.trigonometrico.lexico.TipoToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class AnalizadorSintactico {

    private Stack<String> pila;
    private List<Token> tokens;
    private List<String> errores;
    private int indice;
    private Token tokenActual;

    private static final String FIN = "$";

    public boolean analizar(List<Token> entrada) {

        pila = new Stack<>();
        tokens = new ArrayList<>();
        errores = new ArrayList<>();
        indice = 0;
        tokenActual = null;

        if (entrada == null || entrada.isEmpty()) {
            errores.add("No hay tokens para analizar.");
            return false;
        }

        tokens.addAll(entrada);

        pila.push(FIN);
        pila.push("<E>");

        while (!pila.isEmpty() && errores.isEmpty()) {

            actualizarToken();

            if (tokenActual == null) {
                error("Fin inesperado de la expresión.");
                break;
            }

            String cima = pila.peek();

            switch (cima) {

                // EXPRESIÓN

                case "<E>":

                    pila.pop();
                    agregar("<T>", "<A>");

                    break;

                case "<A>":

                    pila.pop();

                    if (es(TipoToken.MAS)) {

                        agregar("MAS", "<T>", "<A>");

                    } else if (es(TipoToken.MENOS)) {

                        agregar("MENOS", "<T>", "<A>");

                    } else if (es(TipoToken.PARENTESIS_DERECHO)
                            || esFin()) {

                        // A -> ε

                    } else {

                        error("Se esperaba '+' o '-'.");
                    }

                    break;

                // TÉRMINOS

                case "<T>":

                    pila.pop();
                    agregar("<F>", "<B>");

                    break;

                case "<B>":

                    pila.pop();

                    if (es(TipoToken.MULTIPLICACION)) {

                        agregar("MULTIPLICACION", "<F>", "<B>");

                    } else if (es(TipoToken.DIVISION)) {

                        agregar("DIVISION", "<F>", "<B>");

                    } else if (es(TipoToken.MAS)
                            || es(TipoToken.MENOS)
                            || es(TipoToken.PARENTESIS_DERECHO)
                            || esFin()) {

                        // B -> ε

                    } else {

                        error("Se esperaba '*' o '/'.");
                    }

                    break;

                // FACTORES

                case "<F>":

                    pila.pop();

                    if (es(TipoToken.MAS)) {

                        agregar("MAS", "<F>");

                    } else if (es(TipoToken.MENOS)) {

                        agregar("MENOS", "<F>");

                    } else {

                        agregar("<P>");
                    }

                    break;

                case "<P>":

                    pila.pop();
                    agregar("<R>", "<C>");

                    break;

                case "<C>":

                    pila.pop();

                    if (es(TipoToken.POTENCIA)) {

                        agregar("POTENCIA", "<F>");

                    } else if (es(TipoToken.MULTIPLICACION)
                            || es(TipoToken.DIVISION)
                            || es(TipoToken.MAS)
                            || es(TipoToken.MENOS)
                            || es(TipoToken.PARENTESIS_DERECHO)
                            || esFin()) {

                        // C -> ε

                    } else {

                        error("Se esperaba '^'.");
                    }

                    break;

                // ELEMENTOS PRIMARIOS

                case "<R>":

                    pila.pop();

                    if (es(TipoToken.NUM)) {

                        pila.push("NUM");

                    } else if (es(TipoToken.VAR)) {

                        pila.push("VAR");

                    } else if (es(TipoToken.PARENTESIS_IZQUIERDO)) {

                        agregar(
                                "PARENTESIS_IZQUIERDO",
                                "<E>",
                                "PARENTESIS_DERECHO"
                        );

                    } else if (es(TipoToken.SIN)) {

                        agregar(
                                "SIN",
                                "PARENTESIS_IZQUIERDO",
                                "<E>",
                                "PARENTESIS_DERECHO"
                        );

                    } else if (es(TipoToken.COS)) {

                        agregar(
                                "COS",
                                "PARENTESIS_IZQUIERDO",
                                "<E>",
                                "PARENTESIS_DERECHO"
                        );

                    } else if (es(TipoToken.TAN)) {

                        agregar(
                                "TAN",
                                "PARENTESIS_IZQUIERDO",
                                "<E>",
                                "PARENTESIS_DERECHO"
                        );

                    } else {

                        error("Expresión no válida. Se esperaba un número, variable, función o paréntesis.");
                    }

                    break;

                // TERMINALES

                default:

                    if (coincideTerminal(cima)) {

                        pila.pop();
                        avanzar();

                    } else {

                        error("Se esperaba " + cima + ".");
                    }

                    break;
            }
        }

        return errores.isEmpty()
                && pila.isEmpty()
                && indice >= tokens.size();
    }

    // ACTUALIZACIÓN DEL TOKEN

    private void actualizarToken() {

        tokenActual = indice < tokens.size()
                ? tokens.get(indice)
                : null;
    }

    // AVANZAR AL SIGUIENTE TOKEN

    private void avanzar() {
        indice++;
    }

    // VERIFICAR TIPO DE TOKEN

    private boolean es(TipoToken tipo) {

        return tokenActual != null
                && tokenActual.getTipo() == tipo;
    }

    // VERIFICAR FIN DE ENTRADA

    private boolean esFin() {

        return tokenActual == null
                || tokenActual.getTipo() == TipoToken.FIN_ENTRADA;
    }

    // COMPARACIÓN DE TERMINALES

    private boolean coincideTerminal(String terminal) {

        if (tokenActual == null) {
            return false;
        }

        if (terminal.equals(FIN)) {
            return esFin();
        }

        if (terminal.equals("NUM")) {
            return es(TipoToken.NUM);
        }

        if (terminal.equals("VAR")) {
            return es(TipoToken.VAR);
        }

        if (terminal.equals("SIN")) {
            return es(TipoToken.SIN);
        }

        if (terminal.equals("COS")) {
            return es(TipoToken.COS);
        }

        if (terminal.equals("TAN")) {
            return es(TipoToken.TAN);
        }

        if (terminal.equals("MAS")) {
            return es(TipoToken.MAS);
        }

        if (terminal.equals("MENOS")) {
            return es(TipoToken.MENOS);
        }

        if (terminal.equals("MULTIPLICACION")) {
            return es(TipoToken.MULTIPLICACION);
        }

        if (terminal.equals("DIVISION")) {
            return es(TipoToken.DIVISION);
        }

        if (terminal.equals("POTENCIA")) {
            return es(TipoToken.POTENCIA);
        }

        if (terminal.equals("PARENTESIS_IZQUIERDO")) {
            return es(TipoToken.PARENTESIS_IZQUIERDO);
        }

        if (terminal.equals("PARENTESIS_DERECHO")) {
            return es(TipoToken.PARENTESIS_DERECHO);
        }

        return false;
    }

    // MANEJO DE LA PILA

    private void agregar(String... elementos) {

        for (int i = elementos.length - 1; i >= 0; i--) {
            pila.push(elementos[i]);
        }
    }

    // REGISTRO DE ERRORES

    private void error(String mensaje) {

        if (tokenActual == null || esFin()) {

            errores.add(
                    "Error sintáctico al final de la expresión: "
                    + mensaje
            );

        } else {

            errores.add(
                    "Error sintáctico en línea "
                    + tokenActual.getLinea()
                    + ", columna "
                    + tokenActual.getColumna()
                    + ": "
                    + mensaje
                    + " Elemento encontrado: '"
                    + tokenActual.getLexema()
                    + "'."
            );
        }
    }

    // RESULTADOS

    public List<String> getErrores() {
        return errores;
    }

    public boolean hayErrores() {
        return errores != null && !errores.isEmpty();
    }
}
