package com.toycompiler.toycompiler.lexico;


public class AfdConstanteNumerica {

    private final int[][] A = {

        {1,7,8,2},  // Estado inicial
        {1,3,4,8},  // Digitos
        {1,7,8,8},  // Signo
        {3,8,4,8},  // Parte decimal
        {6,8,8,5},  // Exponente
        {6,8,8,8},  // Signo del exponente
        {6,8,8,8},  // Digitos del exponente
        {3,8,8,8},  // Punto decimal
        {8,8,8,8}   // Error

    };

    private int getIndiceSimbolo(char c) {

        if (Character.isDigit(c)) return 0;

        if (c == '.') return 1;

        if (c == 'E' || c == 'e') return 2;

        if (c == '+' || c == '-') return 3;

        return -1;
    }

    public boolean validar(String cadena) {

        if (cadena == null || cadena.isEmpty()) {
            return false;
        }

        int estado = 0;
        int i = 0;

        while (i < cadena.length()) {

            char simbolo = cadena.charAt(i);

            int indice = getIndiceSimbolo(simbolo);

            if (indice == -1) {
                return false;
            }

            estado = A[estado][indice];

            if (estado == 8) {
                return false;
            }

            i++;
        }

        return estado == 1 || estado == 3 || estado == 6;
    }
}

