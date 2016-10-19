
package com.oracle.truffle.bf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;


public class BFV0 extends BFImpl {

    private BFParser.Operation[] operations;
    private InputStream in;
    private OutputStream out;

    static class Memory {
        int[] cells;
        int index;

        public Memory() {
            cells = new int[100];
        }


    }

    @Override
    public void prepare(BFParser.Operation[] operations, InputStream in, OutputStream out) {
        this.operations = operations;
        this.in = in;
        this.out = out;
    }

    @Override
    public void run() throws IOException {
        Memory memory = new Memory();
        run(memory, operations, false);
    }

    private void run(Memory memory, BFParser.Operation[] operations, boolean repeat) throws IOException {
        do {
            if (repeat && memory.cells[memory.index] == 0) {
                break;
            }
            for (BFParser.Operation operation : operations) {
                switch (operation.getCode()) {
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
                        memory.cells[memory.index] = in.read();
                        break;
                    case OUT:
                        out.write(memory.cells[memory.index]);
                        break;
                    case REPEAT:
                        BFParser.Repeat r = (BFParser.Repeat) operation;
                        run(memory, r.getChildren(), true);
                }
            }
        } while (repeat);
    }

}
