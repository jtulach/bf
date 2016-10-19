
package com.oracle.truffle.bf;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RootNode;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;


public class BFV1 extends BFImpl {

    private BFParser.Operation[] operations;
    private InputStream in;
    private OutputStream out;
    private RootCallTarget program;

    static class Memory {
        int[] cells;
        int index;
        final InputStream in;
        final OutputStream out;

        public Memory(InputStream in, OutputStream out) {
            cells = new int[100];
            this.in = in;
            this.out = out;
        }


    }

    @Override
    public void prepare(BFParser.Operation[] operations, InputStream in, OutputStream out) {
        this.operations = operations;
        this.in = in;
        this.out = out;
        this.program = Truffle.getRuntime().createCallTarget(new BlockNode(TruffleLanguage.class, false, OpNode.convert(operations)));
    }

    @Override
    public void run() throws IOException {
        Memory memory = new Memory(in, out);
        program.call(memory);
    }

    private static class BlockNode extends RootNode {
        private final boolean repeat;
        @Children
        private final Node[] nodes;

        public BlockNode(Class<? extends TruffleLanguage> language, boolean repeat, Node[] nodes) {
            super(language, null, null);
            this.nodes = nodes;
            this.repeat = repeat;
        }

        @Override
        public Object execute(VirtualFrame vf) {
            Memory memory = (Memory) vf.getArguments()[0];
            if (repeat) {
                for (;;) {
                    if (repeat && memory.cells[memory.index] == 0) {
                        break;
                    }
                    executeChildren(memory, vf);
                }
            } else {
                executeChildren(memory, vf);
            }
            return null;
        }

        @ExplodeLoop
        private void executeChildren(Memory memory, VirtualFrame vf) {
            for (Node n : nodes) {
                if (n instanceof BlockNode) {
                    ((BlockNode) n).execute(vf);
                } else {
                    OpNode op = (OpNode) n;
                    try {
                        op.execute(memory);
                    } catch (IOException ex) {
                        throw new IllegalStateException(ex);
                    }
                }
            }
        }

    }

    private static class OpNode extends Node {

        static Node[] convert(BFParser.Operation[] operations) {
            Node[] arr = new Node[operations.length];
            for (int i = 0; i < operations.length; i++) {
                final BFParser.Operation op = operations[i];
                if (op.getCode() == BFParser.OpCode.REPEAT) {
                    BFParser.Repeat r = (BFParser.Repeat) op;
                    arr[i] = new BlockNode(TruffleLanguage.class, true, convert(r.getChildren()));
                } else {
                    arr[i] = new OpNode(op.getCode());
                }
            }
            return arr;
        }
        private final BFParser.OpCode operation;

        public OpNode(BFParser.OpCode operation) {
            this.operation = operation;
        }

        public void execute(Memory memory) throws IOException {
                switch (operation) {
                    case DEC:
                        memory.cells[memory.index]--;
                        break;
                    case INC:
                        memory.cells[memory.index]++;
                        break;
                    case LEFT:
                        memory.index--;
                        break;
                    case RIGHT:
                        if (++memory.index >= memory.cells.length) {
                            memory.cells = Arrays.copyOf(memory.cells, memory.cells.length * 2);
                        }
                        break;
                    case IN:
                        memory.cells[memory.index] = read(memory);
                        break;
                    case OUT:
                        write(memory);
                        break;
                    default:
                        assert false;
                }
        }

        @CompilerDirectives.TruffleBoundary
        private static int read(Memory memory) throws IOException {
            return memory.in.read();
        }

        @CompilerDirectives.TruffleBoundary
        private static void write(Memory memory) throws IOException {
            memory.out.write(memory.cells[memory.index]);
        }
    }
}
