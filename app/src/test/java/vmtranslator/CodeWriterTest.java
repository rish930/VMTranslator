package vmtranslator;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

public class CodeWriterTest {
    @Test
    void testWriteAssembly() throws IOException {
        String filename = "test_asm.asm";
        CodeWriter cw = new CodeWriter(filename);
        try {
            cw.writePushPop("push", "argument", 2);
            cw.writePushPop("push", "local", 10);
            cw.writeArithmetic("add");
        } catch (InvalidCommandException ice) {
            System.out.println("invalid command");
            ice.printStackTrace();
        }
        cw.close();

        String expected =
        """
        // push argument 2
        @2
        D=A
        @ARG
        A=D+M
        D=M
        @SP
        A=M
        M=D
        @SP
        M=M+1
        // push local 10
        @10
        D=A
        @LCL
        A=D+M
        D=M
        @SP
        A=M
        M=D
        @SP
        M=M+1
        // add
        @SP
        M=M-1
        A=M
        D=M
        @SP
        A=M-1
        M=D+M
        """;
        // File pathname = new File(filename);
        String myContent = Files.readString(Paths.get(filename));
        assertTrue(myContent.equals(expected));

        File file = new File(filename);
        file.delete();
    }
    
}
