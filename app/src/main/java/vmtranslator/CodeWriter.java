package vmtranslator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class CodeWriter {
    /*
    * writes the assembly code that implements the parsed command
    */
    BufferedWriter bw;

    CodeWriter(String filename) throws IOException {
        this.bw = new BufferedWriter(new FileWriter(filename));
    }

    void writeArithmetic(String command) {
        
    }

    void writePushPop(String command, String segment, int index) {
        // writes to the output file assembly code
        // that implements the given command
        // is either C_PUSH or C_POP
        /*
        // segment = local/argument/this/that
        push segment i
        // take value from local memory segment
        @<index>
        D=A
        @<SEGMENT SYMBOL>
        A=M
        A=A+D
        D=M // RAM[*(LCL+i)]
        @SP
        A=M
        M=D // *SP=RAM[*(LCL+i)]
        @SP
        M=M+1 // SP++
        // push the value to stack

        pop local/argument/this/that i

        */
    }

    void close() {
        // closes the output file
        // TODO
    }
}
