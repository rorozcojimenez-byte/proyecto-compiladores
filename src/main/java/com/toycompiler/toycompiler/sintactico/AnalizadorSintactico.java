
package com.toycompiler.toycompiler.sintactico;

import com.toycompiler.toycompiler.lexico.Token;
import com.toycompiler.toycompiler.lexico.TipoToken;

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

        prepararTokens(entrada);

        pila.push(FIN);
        pila.push("<Programa>");

        while (!pila.isEmpty() && errores.isEmpty()) {

            actualizarToken();

            if (tokenActual == null) {
                error("Fin inesperado del código.");
                break;
            }

            String cima = pila.peek();

            switch (cima) {

                // PROGRAMA

                case "<Programa>":

                    pila.pop();

                    if (!es(TipoToken.FUNC)) {
                        error("El programa debe comenzar con func main().");
                        break;
                    }

                    agregar(
                            "FUNC",
                            "MAIN",
                            "PARENTESIS_ABRE",
                            "PARENTESIS_CIERRA",
                            "<Bloque>",
                            "END",
                            "<FuncionesGlobales>"
                    );

                    break;

                case "<FuncionesGlobales>":

                    pila.pop();

                    if (es(TipoToken.FUNC)) {
                        agregar("<Funcion>", "<FuncionesGlobales>");
                    }

                    break;

                case "<Funcion>":

                    pila.pop();

                    agregar(
                            "FUNC",
                            "IDENTIFICADOR",
                            "PARENTESIS_ABRE",
                            "<Parametros>",
                            "PARENTESIS_CIERRA",
                            "<Bloque>",
                            "END"
                    );

                    break;

                case "<Parametros>":

                    pila.pop();

                    if (es(TipoToken.IDENTIFICADOR)) {
                        agregar("IDENTIFICADOR", "<ColaParametros>");
                    }

                    break;

                case "<ColaParametros>":

                    pila.pop();

                    if (es(TipoToken.COMA)) {
                        agregar(
                                "COMA",
                                "IDENTIFICADOR",
                                "<ColaParametros>"
                        );
                    }

                    break;

                // BLOQUES Y SENTENCIAS

                case "<Bloque>":

                    pila.pop();
                    agregar("<ListaSentencias>");

                    break;

                case "<ListaSentencias>":

                    pila.pop();

                    saltarSeparadores();

                    if (es(TipoToken.END)
                            || es(TipoToken.ELSE)
                            || esFin()) {
                        break;
                    }

                    agregar("<Sentencia>", "<ListaSentencias>");

                    break;

                case "<Sentencia>":

                    pila.pop();

                    if (es(TipoToken.LOCAL)) {

                        agregar("<Declaracion>", "<SepOpcional>");

                    } else if (es(TipoToken.PRINT)
                            || es(TipoToken.PRINTLN)) {

                        agregar("<Impresion>", "<SepOpcional>");

                    } else if (es(TipoToken.RET)) {

                        agregar("RET", "<Expresion>", "<SepOpcional>");

                    } else if (es(TipoToken.IF)) {

                        agregar("<Condicional>");

                    } else if (es(TipoToken.WHILE)) {

                        agregar("<While>");

                    } else if (es(TipoToken.FOR)) {

                        agregar("<For>");

                    } else if (es(TipoToken.IDENTIFICADOR)) {

                        if (siguienteEs(TipoToken.ASIGNACION)) {

                            agregar(
                                    "IDENTIFICADOR",
                                    "ASIGNACION",
                                    "<Expresion>",
                                    "<SepOpcional>"
                            );

                        } else if (siguienteEs(TipoToken.PARENTESIS_ABRE)) {

                            agregar("<Llamada>", "<SepOpcional>");

                        } else {

                            agregar("<Expresion>", "<SepOpcional>");
                        }

                    } else if (iniciaExpresion()) {

                        agregar("<Expresion>", "<SepOpcional>");

                    } else {

                        error("No se esperaba este elemento al iniciar una sentencia.");
                    }

                    break;

                case "<SepOpcional>":

                    pila.pop();

                    if (esSeparador()) {
                        pila.push("SEP");
                    }

                    break;

                case "<Declaracion>":

                    pila.pop();

                    agregar(
                            "LOCAL",
                            "IDENTIFICADOR",
                            "ASIGNACION",
                            "<Expresion>"
                    );

                    break;

                case "<Impresion>":

                    pila.pop();

                    if (es(TipoToken.PRINT)) {
                        agregar("PRINT", "<Expresion>");
                    } else {
                        agregar("PRINTLN", "<Expresion>");
                    }

                    break;

                case "<Llamada>":

                    pila.pop();

                    agregar(
                            "IDENTIFICADOR",
                            "PARENTESIS_ABRE",
                            "<Argumentos>",
                            "PARENTESIS_CIERRA"
                    );

                    break;

                case "<Argumentos>":

                    pila.pop();

                    if (iniciaExpresion()) {
                        agregar("<Expresion>", "<ColaArgumentos>");
                    }

                    break;

                case "<ColaArgumentos>":

                    pila.pop();

                    if (es(TipoToken.COMA)) {
                        agregar(
                                "COMA",
                                "<Expresion>",
                                "<ColaArgumentos>"
                        );
                    }

                    break;

                // CONDICIONALES

                case "<Condicional>":

                    pila.pop();

                    agregar(
                            "IF",
                            "<Expresion>",
                            "THEN",
                            "<Bloque>",
                            "<Sino>",
                            "END"
                    );

                    break;

                case "<Sino>":

                    pila.pop();

                    if (es(TipoToken.ELSE)) {
                        agregar("ELSE", "<Bloque>");
                    }

                    break;

                // CICLOS

                case "<While>":

                    pila.pop();

                    agregar(
                            "WHILE",
                            "<Expresion>",
                            "DO",
                            "<Bloque>",
                            "END"
                    );

                    break;

                case "<For>":

                    pila.pop();

                    agregar(
                            "FOR",
                            "IDENTIFICADOR",
                            "ASIGNACION",
                            "<Expresion>",
                            "COMA",
                            "<Expresion>",
                            "<Paso>",
                            "DO",
                            "<Bloque>",
                            "END"
                    );

                    break;

                case "<Paso>":

                    pila.pop();

                    if (es(TipoToken.COMA)) {
                        agregar("COMA", "<Expresion>");
                    }

                    break;

                // EXPRESIONES

                case "<Expresion>":

                    pila.pop();
                    agregar("<Or>");

                    break;

                case "<Or>":

                    pila.pop();
                    agregar("<And>", "<OrPrima>");

                    break;

                case "<OrPrima>":

                    pila.pop();

                    if (es(TipoToken.OR)) {
                        agregar("OR", "<And>", "<OrPrima>");
                    }

                    break;

                case "<And>":

                    pila.pop();
                    agregar("<Igualdad>", "<AndPrima>");

                    break;

                case "<AndPrima>":

                    pila.pop();

                    if (es(TipoToken.AND)) {
                        agregar("AND", "<Igualdad>", "<AndPrima>");
                    }

                    break;

                case "<Igualdad>":

                    pila.pop();
                    agregar("<Comparacion>", "<IgualdadPrima>");

                    break;

                case "<IgualdadPrima>":

                    pila.pop();

                    if (es(TipoToken.IGUAL)) {

                        agregar(
                                "IGUAL",
                                "<Comparacion>",
                                "<IgualdadPrima>"
                        );

                    } else if (es(TipoToken.DIFERENTE)) {

                        agregar(
                                "DIFERENTE",
                                "<Comparacion>",
                                "<IgualdadPrima>"
                        );
                    }

                    break;

                case "<Comparacion>":

                    pila.pop();
                    agregar("<Aditiva>", "<ComparacionPrima>");

                    break;

                case "<ComparacionPrima>":

                    pila.pop();

                    if (es(TipoToken.MENOR)) {

                        agregar(
                                "MENOR",
                                "<Aditiva>",
                                "<ComparacionPrima>"
                        );

                    } else if (es(TipoToken.MENOR_IGUAL)) {

                        agregar(
                                "MENOR_IGUAL",
                                "<Aditiva>",
                                "<ComparacionPrima>"
                        );

                    } else if (es(TipoToken.MAYOR)) {

                        agregar(
                                "MAYOR",
                                "<Aditiva>",
                                "<ComparacionPrima>"
                        );

                    } else if (es(TipoToken.MAYOR_IGUAL)) {

                        agregar(
                                "MAYOR_IGUAL",
                                "<Aditiva>",
                                "<ComparacionPrima>"
                        );
                    }

                    break;

                case "<Aditiva>":

                    pila.pop();
                    agregar("<Multiplicativa>", "<AditivaPrima>");

                    break;

                case "<AditivaPrima>":

                    pila.pop();

                    if (es(TipoToken.MAS)) {

                        agregar(
                                "MAS",
                                "<Multiplicativa>",
                                "<AditivaPrima>"
                        );

                    } else if (es(TipoToken.MENOS)) {

                        agregar(
                                "MENOS",
                                "<Multiplicativa>",
                                "<AditivaPrima>"
                        );
                    }

                    break;

                case "<Multiplicativa>":

                    pila.pop();
                    agregar("<Modulo>", "<MultiplicativaPrima>");

                    break;

                case "<MultiplicativaPrima>":

                    pila.pop();

                    if (es(TipoToken.MULTIPLICACION)) {

                        agregar(
                                "MULTIPLICACION",
                                "<Modulo>",
                                "<MultiplicativaPrima>"
                        );

                    } else if (es(TipoToken.DIVISION)) {

                        agregar(
                                "DIVISION",
                                "<Modulo>",
                                "<MultiplicativaPrima>"
                        );
                    }

                    break;

                case "<Modulo>":

                    pila.pop();
                    agregar("<Unaria>", "<ModuloPrima>");

                    break;

                case "<ModuloPrima>":

                    pila.pop();

                    if (es(TipoToken.MODULO)) {
                        agregar("MODULO", "<Unaria>", "<ModuloPrima>");
                    }

                    break;

                case "<Unaria>":

                    pila.pop();

                    if (es(TipoToken.MENOS)) {

                        agregar("MENOS", "<Unaria>");

                    } else if (es(TipoToken.NEGACION)) {

                        agregar("NEGACION", "<Unaria>");

                    } else {

                        agregar("<Potencia>");
                    }

                    break;

                case "<Potencia>":

                    pila.pop();
                    agregar("<Primaria>", "<PotenciaPrima>");

                    break;

                case "<PotenciaPrima>":

                    pila.pop();

                    if (es(TipoToken.POTENCIA)) {
                        agregar("POTENCIA", "<Unaria>");
                    }

                    break;

                case "<Primaria>":

                    pila.pop();

                    if (es(TipoToken.NUMERO)) {

                        pila.push("NUMERO");

                    } else if (es(TipoToken.CADENA)) {

                        pila.push("CADENA");

                    } else if (es(TipoToken.TRUE)) {

                        pila.push("TRUE");

                    } else if (es(TipoToken.FALSE)) {

                        pila.push("FALSE");

                    } else if (es(TipoToken.IDENTIFICADOR)) {

                        agregar("IDENTIFICADOR", "<PrimariaID>");

                    } else if (es(TipoToken.PARENTESIS_ABRE)) {

                        agregar(
                                "PARENTESIS_ABRE",
                                "<Expresion>",
                                "PARENTESIS_CIERRA"
                        );

                    } else {

                        error("Expresión no válida.");
                    }

                    break;

                case "<PrimariaID>":

                    pila.pop();

                    if (es(TipoToken.PARENTESIS_ABRE)) {

                        agregar(
                                "PARENTESIS_ABRE",
                                "<Argumentos>",
                                "PARENTESIS_CIERRA"
                        );
                    }

                    break;

                // TERMINALES

                default:

                    if (cima.equals("SEP") && esSeparador()) {

                        pila.pop();
                        avanzar();

                    } else if (coincideTerminal(cima)) {

                        pila.pop();
                        avanzar();

                    } else {

                        error("Se esperaba " + cima + ".");
                    }

                    break;
            }
        }

        return errores.isEmpty() && indice >= tokens.size();
    }

    // PREPARACIÓN DE TOKENS

    private void prepararTokens(List<Token> entrada) {

        Token anterior = null;

        for (Token token : entrada) {

            if (token.getTipo() == TipoToken.FIN_ARCHIVO) {
                continue;
            }

            if (anterior != null
                    && token.getLinea() > anterior.getLinea()) {

                tokens.add(new Token(
                        TipoToken.FIN_ARCHIVO,
                        "SEP",
                        token.getLinea(),
                        token.getColumna()
                ));
            }

            tokens.add(token);
            anterior = token;
        }

        if (anterior != null) {

            tokens.add(new Token(
                    TipoToken.FIN_ARCHIVO,
                    FIN,
                    anterior.getLinea(),
                    anterior.getColumna()
            ));
        }
    }

    // CONTROL DE TOKENS

    private void actualizarToken() {

        tokenActual = indice < tokens.size()
                ? tokens.get(indice)
                : null;
    }

    private void avanzar() {
        indice++;
    }

    private boolean es(TipoToken tipo) {

        return tokenActual != null
                && tokenActual.getTipo() == tipo;
    }

    private boolean esFin() {

        return tokenActual == null
                || tokenActual.getLexema().equals(FIN);
    }

    private boolean esSeparador() {

        return tokenActual != null
                && tokenActual.getLexema().equals("SEP");
    }

    private boolean siguienteEs(TipoToken tipo) {

        int siguiente = indice + 1;

        if (siguiente < tokens.size()
                && tokens.get(siguiente).getLexema().equals("SEP")) {
            siguiente++;
        }

        return siguiente < tokens.size()
                && tokens.get(siguiente).getTipo() == tipo;
    }

    private boolean iniciaExpresion() {

        return es(TipoToken.IDENTIFICADOR)
                || es(TipoToken.NUMERO)
                || es(TipoToken.CADENA)
                || es(TipoToken.TRUE)
                || es(TipoToken.FALSE)
                || es(TipoToken.PARENTESIS_ABRE)
                || es(TipoToken.MENOS)
                || es(TipoToken.NEGACION);
    }

    private void saltarSeparadores() {

        while (esSeparador()) {
            avanzar();
            actualizarToken();
        }
    }

    // COMPARACIÓN DE TERMINALES

    private boolean coincideTerminal(String terminal) {

        if (tokenActual == null) {
            return false;
        }

        // MARCADOR DE FIN
        if (terminal.equals(FIN)) {
            return tokenActual.getLexema().equals(FIN);
        }

        // IDENTIFICADORES Y LITERALES
        if (terminal.equals("MAIN")) {
            return es(TipoToken.IDENTIFICADOR)
                    && tokenActual.getLexema().equals("main");
        }

        if (terminal.equals("IDENTIFICADOR")) return es(TipoToken.IDENTIFICADOR);
        if (terminal.equals("NUMERO")) return es(TipoToken.NUMERO);
        if (terminal.equals("CADENA")) return es(TipoToken.CADENA);

        // PALABRAS RESERVADAS
        if (terminal.equals("FUNC")) return es(TipoToken.FUNC);
        if (terminal.equals("IF")) return es(TipoToken.IF);
        if (terminal.equals("THEN")) return es(TipoToken.THEN);
        if (terminal.equals("ELSE")) return es(TipoToken.ELSE);
        if (terminal.equals("TRUE")) return es(TipoToken.TRUE);
        if (terminal.equals("FALSE")) return es(TipoToken.FALSE);
        if (terminal.equals("AND")) return es(TipoToken.AND);
        if (terminal.equals("OR")) return es(TipoToken.OR);
        if (terminal.equals("WHILE")) return es(TipoToken.WHILE);
        if (terminal.equals("DO")) return es(TipoToken.DO);
        if (terminal.equals("FOR")) return es(TipoToken.FOR);
        if (terminal.equals("END")) return es(TipoToken.END);
        if (terminal.equals("LOCAL")) return es(TipoToken.LOCAL);
        if (terminal.equals("PRINT")) return es(TipoToken.PRINT);
        if (terminal.equals("PRINTLN")) return es(TipoToken.PRINTLN);
        if (terminal.equals("RET")) return es(TipoToken.RET);

        // OPERADORES
        if (terminal.equals("ASIGNACION")) return es(TipoToken.ASIGNACION);
        if (terminal.equals("MAS")) return es(TipoToken.MAS);
        if (terminal.equals("MENOS")) return es(TipoToken.MENOS);
        if (terminal.equals("MULTIPLICACION")) return es(TipoToken.MULTIPLICACION);
        if (terminal.equals("DIVISION")) return es(TipoToken.DIVISION);
        if (terminal.equals("MODULO")) return es(TipoToken.MODULO);
        if (terminal.equals("POTENCIA")) return es(TipoToken.POTENCIA);
        if (terminal.equals("MENOR")) return es(TipoToken.MENOR);
        if (terminal.equals("MAYOR")) return es(TipoToken.MAYOR);
        if (terminal.equals("MENOR_IGUAL")) return es(TipoToken.MENOR_IGUAL);
        if (terminal.equals("MAYOR_IGUAL")) return es(TipoToken.MAYOR_IGUAL);
        if (terminal.equals("IGUAL")) return es(TipoToken.IGUAL);
        if (terminal.equals("DIFERENTE")) return es(TipoToken.DIFERENTE);
        if (terminal.equals("NEGACION")) return es(TipoToken.NEGACION);

        // DELIMITADORES
        if (terminal.equals("PARENTESIS_ABRE")) return es(TipoToken.PARENTESIS_ABRE);
        if (terminal.equals("PARENTESIS_CIERRA")) return es(TipoToken.PARENTESIS_CIERRA);
        if (terminal.equals("COMA")) return es(TipoToken.COMA);

        return false;
    }

    // MANEJO DE PILA

    private void agregar(String... elementos) {

        for (int i = elementos.length - 1; i >= 0; i--) {
            pila.push(elementos[i]);
        }
    }

    // ERRORES

    private void error(String mensaje) {

        if (tokenActual == null || esFin()) {

            errores.add(
                    "Error sintáctico al final del código: " + mensaje
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
