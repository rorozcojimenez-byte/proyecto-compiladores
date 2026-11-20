
package com.toycompiler.toycompiler.trigonometrico.lexico;

public enum TipoToken {

    // Números y variables
    NUM,
    VAR,

    // Funciones trigonométricas
    SIN,
    COS,
    TAN,

    // Operadores aritméticos
    MAS,
    MENOS,
    MULTIPLICACION,
    DIVISION,
    POTENCIA,

    // Agrupación
    PARENTESIS_IZQUIERDO,
    PARENTESIS_DERECHO,

    // Fin de entrada
    FIN_ENTRADA

}
