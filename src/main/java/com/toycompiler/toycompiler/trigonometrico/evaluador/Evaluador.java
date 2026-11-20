
package com.toycompiler.toycompiler.trigonometrico.evaluador;

import java.util.List;
import java.util.Map;

import com.toycompiler.toycompiler.trigonometrico.lexico.Token;
import com.toycompiler.toycompiler.trigonometrico.lexico.TipoToken;

public class Evaluador {

    private List<Token> tokens;
    private Map<String, Double> variables;
    private int indice;

    public double evaluar(List<Token> tokens, Map<String, Double> variables) {

        this.tokens = tokens;
        this.variables = variables;
        this.indice = 0;

        double resultado = analizarE();

        if (!verificar(TipoToken.FIN_ENTRADA)) {
            throw new IllegalArgumentException(
                    "Hay elementos adicionales en la expresión."
            );
        }

        return resultado;
    }

    // E -> T A
    private double analizarE() {

        double resultado = analizarT();

        while (verificar(TipoToken.MAS)
                || verificar(TipoToken.MENOS)) {

            if (verificar(TipoToken.MAS)) {
                coincidir(TipoToken.MAS);
                resultado += analizarT();

            } else {
                coincidir(TipoToken.MENOS);
                resultado -= analizarT();
            }
        }

        return resultado;
    }

    // T -> F B
    private double analizarT() {

        double resultado = analizarF();

        while (verificar(TipoToken.MULTIPLICACION)
                || verificar(TipoToken.DIVISION)) {

            if (verificar(TipoToken.MULTIPLICACION)) {

                coincidir(TipoToken.MULTIPLICACION);
                resultado *= analizarF();

            } else {

                coincidir(TipoToken.DIVISION);

                double divisor = analizarF();

                if (divisor == 0.0) {
                    throw new ArithmeticException(
                            "No se puede dividir entre cero."
                    );
                }

                resultado /= divisor;
            }
        }

        return resultado;
    }

    // F -> + F | - F | P
    private double analizarF() {

        if (verificar(TipoToken.MAS)) {

            coincidir(TipoToken.MAS);
            return analizarF();

        } else if (verificar(TipoToken.MENOS)) {

            coincidir(TipoToken.MENOS);
            return -analizarF();

        }

        return analizarP();
    }

    // P -> R C
    private double analizarP() {

        double base = analizarR();

        if (verificar(TipoToken.POTENCIA)) {

            coincidir(TipoToken.POTENCIA);

            double exponente = analizarF();

            return Math.pow(base, exponente);
        }

        return base;
    }

    // R -> NUM | VAR | ( E ) | SIN ( E ) | COS ( E ) | TAN ( E )
    private double analizarR() {

        if (verificar(TipoToken.NUM)) {

            Token token = actual();
            coincidir(TipoToken.NUM);

            return Double.parseDouble(token.getLexema());

        } else if (verificar(TipoToken.VAR)) {

            Token token = actual();
            coincidir(TipoToken.VAR);

            String nombre = token.getLexema().toLowerCase();

            if (!variables.containsKey(nombre)) {
                throw new IllegalArgumentException(
                        "No se proporcionó un valor para la variable "
                        + nombre + "."
                );
            }

            return variables.get(nombre);

        } else if (verificar(TipoToken.PARENTESIS_IZQUIERDO)) {

            coincidir(TipoToken.PARENTESIS_IZQUIERDO);

            double resultado = analizarE();

            coincidir(TipoToken.PARENTESIS_DERECHO);

            return resultado;

        } else if (verificar(TipoToken.SIN)) {

            coincidir(TipoToken.SIN);
            coincidir(TipoToken.PARENTESIS_IZQUIERDO);

            double angulo = analizarE();

            coincidir(TipoToken.PARENTESIS_DERECHO);

            return Math.sin(Math.toRadians(angulo));

        } else if (verificar(TipoToken.COS)) {

            coincidir(TipoToken.COS);
            coincidir(TipoToken.PARENTESIS_IZQUIERDO);

            double angulo = analizarE();

            coincidir(TipoToken.PARENTESIS_DERECHO);

            return Math.cos(Math.toRadians(angulo));

        } else if (verificar(TipoToken.TAN)) {

            coincidir(TipoToken.TAN);
            coincidir(TipoToken.PARENTESIS_IZQUIERDO);

            double angulo = analizarE();

            coincidir(TipoToken.PARENTESIS_DERECHO);

            if (Math.abs(Math.cos(Math.toRadians(angulo))) < 1E-12) {
                throw new ArithmeticException(
                        "La tangente no está definida para ese ángulo."
                );
            }

            return Math.tan(Math.toRadians(angulo));
        }

        throw new IllegalArgumentException(
                "Se esperaba un número, variable, función o paréntesis."
        );
    }

    // VERIFICAR TOKEN ACTUAL
    private boolean verificar(TipoToken tipo) {

        return indice < tokens.size()
                && tokens.get(indice).getTipo() == tipo;
    }

    // CONSUMIR TOKEN
    private void coincidir(TipoToken tipo) {

        if (verificar(tipo)) {
            indice++;
        } else {
            throw new IllegalArgumentException(
                    "Se esperaba el token " + tipo + "."
            );
        }
    }

    // OBTENER TOKEN ACTUAL
    private Token actual() {

        if (indice >= tokens.size()) {
            throw new IllegalArgumentException(
                    "Fin inesperado de la expresión."
            );
        }

        return tokens.get(indice);
    }
}

