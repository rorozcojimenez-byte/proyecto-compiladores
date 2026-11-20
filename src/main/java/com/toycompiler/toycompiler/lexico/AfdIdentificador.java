
package com.toycompiler.toycompiler.lexico;

public class AfdIdentificador {

    private final int[][] A = {

        {1, 2, 1}, // Estado inicial
        {1, 1, 1}, // Estado de aceptación
        {2, 2, 2}  // Estado de error

    };

    private int getIndiceSimbolo(char c) {

        if (Character.isLetter(c)) return 0;

        if (Character.isDigit(c)) return 1;

        if (c == '_') return 2;

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

            if (estado == 2) {
                return false;
            }

            i++;
        }

        return estado == 1;
    }
}
