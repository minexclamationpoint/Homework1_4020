package edu.ufl.cise.cop4020fa23.exceptions;

import edu.ufl.cise.cop4020fa23.SourceLocation;

public class TypeCheckException extends Exception{

    public TypeCheckException() {
    }

    public TypeCheckException(String message) {
        super(message);
    }

    public TypeCheckException(SourceLocation location, String message) {
        super(location + ": " + message);
    
    }
}
