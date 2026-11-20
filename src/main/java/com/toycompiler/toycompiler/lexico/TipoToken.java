package com.toycompiler.toycompiler.lexico;


public enum TipoToken {

    // Palabras reservadas
    FUNC,
    IF,
    THEN,
    ELSE,
    TRUE,
    FALSE,
    AND,
    OR,
    WHILE,
    DO,
    FOR,
    END,
    LOCAL,
    PRINT,
    PRINTLN,
    RET,

    // Elementos generales
    IDENTIFICADOR,
    NUMERO,
    CADENA,

    // Asignacion
    ASIGNACION,

    // Operadores aritmeticos
    MAS,
    MENOS,
    MULTIPLICACION,
    DIVISION,
    MODULO,
    POTENCIA,

    // Operadores relacionales
    MENOR,
    MAYOR,
    MENOR_IGUAL,
    MAYOR_IGUAL,
    IGUAL,
    DIFERENTE,

    // Operadores logicos
    NEGACION,

    // Signos de agrupacion
    PARENTESIS_ABRE,
    PARENTESIS_CIERRA,
    COMA,

    // Control
    FIN_ARCHIVO
}
