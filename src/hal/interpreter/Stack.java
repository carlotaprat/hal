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

import hal.interpreter.core.Context;
import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.types.HalModule;
import hal.interpreter.types.HalObject;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Class to represent the memory of the virtual machine of the
 * interpreter. The memory is organized as a stack of activation
 * records and each entry in the activation record contains is a pair
 * <name of variable,value>.
 */
 
public class Stack
{
    /** Stack of contexts */
    private LinkedList<Context> stack;
    private HalModule module;
    private ReferenceRecord record;

    /**
     * Class to represent an item of the Stack trace.
     * For each function call, the function name and
     * the line number of the call are stored.
     */
    class StackTraceItem {
        public String fname; // Function name
        public int line; // Line number
        public StackTraceItem (String name, int l) {
            fname = name; line = l;
        }
    }

    /** Stack trace to keep track of function calls */
    private LinkedList<StackTraceItem> stackTrace;
    
    /** Constructor of the memory */
    public Stack() {
        stack = new LinkedList<Context>();
        record = null;
        stackTrace = new LinkedList<StackTraceItem>();
    }

    /** Creates a new activation record on the top of the stack */
    public void pushContext(String name, HalObject inst, int line) {
        pushContext(name, inst, module, null, line, false);
    }

    public void pushContext(String name, HalObject inst, HalModule mod, ReferenceRecord parent, int line,
                            boolean method) {
        module = mod;
        record = new ReferenceRecord(parent);
        record.defineVariable("self", inst);
        record.defineVariable("return", null);
        stack.addLast(new Context(mod, record, method));
        stackTrace.addLast(new StackTraceItem(name, line));
    }

    /** Destroys the record activation record */
    public void popContext() {
        stack.removeLast();
        stackTrace.removeLast();

        if (stack.isEmpty()) {
            module = null;
            record = null;
        } else {
            Context last = stack.getLast();
            module = last.module;
            record = last.record;
        }
    }

    public void popUntilFirstLevel() {
        while(stack.size() > 1)
            popContext();

        defineVariable("return", null);
    }

    public HalObject getUnsafeVariable(String name){
        return record.getUnsafeVariable(name);
    }

    public HalObject getVariable(String name) {
        return record.getVariable(name);
    }

    public void defineVariable(String name, HalObject obj) {
        record.defineVariable(name, obj);
    }

    public void defineReturn(HalObject obj) {
        Context current = stack.getLast();

        if(current.isMethod)
            current.record.defineVariable("return", obj);
        else {
            ReferenceRecord parent = current.record.parent;

            if(parent == null)
                throw new RuntimeException("return outside of method");

            while(parent.parent != null)
                parent = parent.parent;

            Iterator<Context> it = stack.descendingIterator();
            boolean found = false;

            while(it.hasNext() && !found) {
                Context c = it.next();
                c.record.defineVariable("return", obj);
                found = c.isMethod && parent == c.record;
            }

            if(!found)
                throw new RuntimeException("return outside of method");
        }
    }

    public Reference getReference(String name) {
        return record.getReference(name);
    }

    public HalModule getCurrentModule() {
        return module;
    }

    public ReferenceRecord getCurrentRecord() {
        return record;
    }

    /**
     * Generates a string with the contents of the stack trace.
     * Each line contains a function name and the line number where
     * the next function is called. Finally, the line number in
     * the record function is written.
     * @param current_line program line executed when this function
     *        is called.
     * @return A string with the contents of the stack trace.
     */ 
    public String getStackTrace(int current_line) {
        int size = stackTrace.size();
        ListIterator<StackTraceItem> itr = stackTrace.listIterator(size);
        ListIterator<Context> itr2 = stack.listIterator(size);
        StringBuffer trace = new StringBuffer("---------------%n| Stack trace |%n---------------%n");
        trace.append("** Depth = ").append(size).append("%n");
        while (itr.hasPrevious()) {
            StackTraceItem it = itr.previous();
            Context c = itr2.previous();
            trace.append("|> ").append(it.fname).append(": line ").append(current_line);
            trace.append(" (").append(c.module.getFullPath()).append(")%n");
            current_line = it.line;
        }
        return trace.toString();
    }

    /**
     * Generates a string with a summarized contents of the stack trace.
     * Only the first and last items of the stack trace are returned.
     * @param current_line program line executed when this function
     *        is called.
     * @param nitems number of function calls returned in the string
     *        at the beginning and at the end of the stack.
     * @return A string with the contents of the stack trace.
     */ 
    public String getStackTrace(int current_line, int nitems) {
        int size = stackTrace.size();
        if (2*nitems >= size) return getStackTrace(current_line);
        ListIterator<StackTraceItem> itr = stackTrace.listIterator(size);
        StringBuffer trace = new StringBuffer("---------------%n| Stack trace |%n---------------%n");
        trace.append("** Depth = ").append(size).append("%n");
        int i;
        for (i = 0; i < nitems; ++i) {
           StackTraceItem it = itr.previous();
           trace.append("|> ").append(it.fname).append(": line ").append(current_line).append("%n");current_line = it.line;
        }
        trace.append("|> ...%n");
        for (; i < size-nitems; ++i) current_line = itr.previous().line;
        for (; i < size; ++i) {
           StackTraceItem it = itr.previous();
           trace.append("|> ").append(it.fname).append(": line ").append(current_line).append("%n");current_line = it.line;
        }
        return trace.toString();
    } 
}
