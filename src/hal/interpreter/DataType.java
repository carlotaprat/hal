/**
 * Copyright (c) 2011, Jordi Cortadella
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the name of the <organization> nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package hal.interpreter;

import hal.interpreter.datatypes.HalBoolean;
import hal.interpreter.exceptions.OperatorNotSupportedException;
import hal.interpreter.exceptions.TypeException;


public abstract class DataType<T>
{
    protected T value;

    public DataType() {
    }

    /**
     * Standard constructor *
     */
    public DataType(T d) {
        value = d;
    }

    /**
     * Copy constructor
     */
    public DataType(DataType<T> d) {
        value = d.value;
    }

    public T getValue() {
        return value;
    }

    /**
     * Copies the value from another data
     */
    public void setData(DataType<T> d) {
        value = d.value;
    }

    /**
     * Returns a string representing the data in textual form.
     */
    public String toString() {
        return (String) __str__().getValue();
    }

    public Integer toInteger() throws TypeException {
        return (Integer) __int__().getValue();
    }

    public Boolean toBoolean() throws TypeException {
        return (Boolean) __bool__().getValue();
    }

    /*
     * UNARY OPERATORS
     */
    public DataType<T> __pos__() {
        throw new OperatorNotSupportedException();
    }

    public DataType<T> __neg__() {
        throw new OperatorNotSupportedException();
    }

    public DataType __not__() {
        throw new OperatorNotSupportedException();
    }

    /*
     * ARITHMETIC OPERATORS
     */
    public DataType<T> __add__(DataType d) {
        throw new OperatorNotSupportedException();
    }

    public DataType<T> __sub__(DataType d) {
        throw new OperatorNotSupportedException();
    }

    public DataType<T> __mul__(DataType d) {
        throw new OperatorNotSupportedException();
    }

    public DataType<T> __div__(DataType d) {
        throw new OperatorNotSupportedException();
    }

    public DataType<T> __mod__(DataType d) {
        throw new OperatorNotSupportedException();
    }

    /*
     * RELATIONAL OPERATORS
     */
    public DataType __eq__(DataType d) {
        return new HalBoolean(value == d.value);
    }

    public DataType __neq__(DataType d) {
        return new HalBoolean(value != d.value);
    }

    public DataType __lt__(DataType d) throws TypeException {
        return new HalBoolean(toInteger() < d.toInteger());
    }

    public DataType __le__(DataType d) throws TypeException {
        return new HalBoolean(toInteger() <= d.toInteger());
    }

    public DataType __gt__(DataType d) throws TypeException {
        return new HalBoolean(toInteger() > d.toInteger());
    }

    public DataType __ge__(DataType d) throws TypeException {
        return new HalBoolean(toInteger() >= d.toInteger());
    }

    /*
     * CONVERSION METHODS
     */
    public abstract DataType __str__();

    public DataType __repr__() {
        return __str__();
    }

    public DataType __int__() {
        throw new TypeException();
    }

    public DataType __bool__() {
        throw new TypeException();
    }

    /*
     * BUILT-IN METHODS
     */
    public DataType __getitem__(DataType index) {
        throw new TypeException();
    }

    public DataType __setitem__(DataType index, DataType item) {
        throw new TypeException();
    }

    public DataType __append__(DataType item) {
        throw new TypeException();
    }
}
